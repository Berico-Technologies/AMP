using System;
using System.Collections;
using System.IO;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using amp.bus;
using amp.messaging;
using amp.rabbit.topology;
using cmf.bus;
using Common.Logging;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using RabbitMQ.Client.Exceptions;
using RabbitMQ.Util;
using amp.rabbit.connection;

namespace amp.rabbit.dispatch
{
    public class RabbitListener
    {
        public event Action<IEnvelopeDispatcher> OnEnvelopeReceived;
        public event Func<IRegistration, bool> OnClose;


        protected IRegistration _registration;
        protected volatile bool _shouldContinue;
        protected volatile bool _isRunning;
        protected ILog _log;
        protected ConsumingRoute _consumingRoute;
        protected IConnectionManager _connectionManager;
        protected ManualResetEvent _startEvent;
        protected ManualResetEvent _stoppedListeningEvent;
        protected bool _connectionClosed;


        public RabbitListener(IRegistration registration, ConsumingRoute route, IConnectionManager connectionManager)
        {
            _registration = registration;
            _consumingRoute = route;
            _connectionManager = connectionManager;
            _connectionManager.ConnectionClosed += Handle_OnConnectionClosed;
            _connectionManager.ConnectionReconnected += Handle_OnConnectionReconnected;
            _startEvent = new ManualResetEvent(false);
            _stoppedListeningEvent = new ManualResetEvent(true);

            _log = LogManager.GetLogger(this.GetType());
        }
 
        public void Start()
        {
            StartOnThread();
            _startEvent.WaitOne(TimeSpan.FromSeconds(30));
        }

        private void StartOnThread()
        {
            _log.Debug("Enter Start");
            _isRunning = true;
            //Do actuall listening on a bacground thread.
            Thread listenerThread = new Thread(Listen);
            listenerThread.Name = string.Format("Lisener for {0}", _consumingRoute.Queue.Name);
            listenerThread.Start();
        }

        private void Restart()
        {
            //Only restart if stop has not been called in the mean time (or conceivably, we never started to begin with).
            if (_isRunning)
                StartOnThread();
        }

        private void Listen()
        {
            _log.Debug("Enter Listen");

            //Don't start listining until last invocation has stopped.  
            _stoppedListeningEvent.WaitOne();
            _log.Debug("Proceeding...");

            _shouldContinue = true;
            _stoppedListeningEvent.Reset();
            try
            {
                using (IModel channel = _connectionManager.CreateModel())
                {
                    channel.ModelShutdown += Handle_OnModelShutdown;

                    // first, declare the exchange and queue
                    var exchange = _consumingRoute.Exchange;
                    var queue = _consumingRoute.Queue;
                    channel.ExchangeDeclare(exchange.Name, exchange.ExchangeType, exchange.IsDurable,
                        exchange.IsAutoDelete, exchange.ArgumentsAsDictionary);
                    channel.QueueDeclare(queue.Name, queue.IsDurable, false, queue.IsAutoDelete,queue.ArgumentsAsDictionary);

                    foreach (var routingKey in _consumingRoute.RoutingKeys)
                    {
                        channel.QueueBind(queue.Name, exchange.Name, routingKey);
                    }

                    // next, create a basic consumer
                    QueueingBasicConsumer consumer = new QueueingBasicConsumer(channel);

                    // and tell it to start consuming messages, storing the consumer tag
                    string consumerTag = channel.BasicConsume(queue.Name, false, consumer);

                    // signal the wait event that we've begun listening
                    _startEvent.Set();

                    _log.Debug("Will now continuously listen for events using routing key(s): " + string.Join(", " , _consumingRoute.RoutingKeys));
                    while (_shouldContinue)
                    {
                        try
                        {
                            object result = null;

                            if (false == consumer.Queue.Dequeue(100, out result))
                            {
                                continue;
                            }
                            BasicDeliverEventArgs e = result as BasicDeliverEventArgs;
                            if (null == e)
                            {
                                continue;
                            }
                            else
                            {
                                this.LogMessage(e);
                            }

                            IBasicProperties props = e.BasicProperties;

                            Envelope env = new Envelope();
                            env.SetReceiptTime(DateTime.Now);
                            env.Payload = e.Body;
                            foreach (DictionaryEntry entry in props.Headers)
                            {
                                try
                                {
                                    string key = entry.Key as string;
                                    string value = Encoding.UTF8.GetString((byte[]) entry.Value);
                                    _log.Debug("Adding header to envelope: {" + key + ":" + value + "}");

                                    env.Headers.Add(key, value);
                                }
                                catch { }
                            }

                            if (this.ShouldRaiseEvent(_registration.Filter, env))
                            {
                                RabbitEnvelopeDispatcher dispatcher = new RabbitEnvelopeDispatcher(_registration, env,
                                    channel, e.DeliveryTag);
                                this.Raise_OnEnvelopeReceivedEvent(dispatcher);
                            }
                        }
                        catch (EndOfStreamException)
                        {
                            // The consumer was closed.
                            _shouldContinue = false;
                        }
                        catch (AlreadyClosedException)
                        {
                            // The consumer was closed.
                            _shouldContinue = false;
                        }
                        catch (OperationInterruptedException)
                        {
                            // The consumer was removed, either through
                            // channel or connection closure, or through the
                            // action of IModel.BasicCancel().
                            _shouldContinue = false;
                        }
                        catch(Exception ex)
                        {
                            _log.Error("Error trying to poll the queue.", ex);
                        }
                    }
                    _log.Debug("No longer listening for events");

                    try { if(channel.IsOpen) channel.BasicCancel(consumerTag); }
                    catch (OperationInterruptedException) { }
                }
            }
            catch (Exception ex)
            {
                _log.Error("Error while attempting on start listening.", ex);
            }
            finally
            {
                _stoppedListeningEvent.Set();

                _log.Debug("Leave Listen");
            }
        }

        private void Handle_OnModelShutdown(IModel model, ShutdownEventArgs reason)
        {
            _log.Debug(string.Format("Enter Handle_OnModelShutdown, Initiator: {0}", reason.Initiator));
            
            _shouldContinue = false;
            
            //If we the shutdown wasn't deliberate on our part, attempt to restart
            if (reason.Initiator != ShutdownInitiator.Application)
            {
                _log.Debug("Attempting restart on new channel.");
                //Move to a background thread so that rabbit can raise the connection closed event if that is the cause.
                new Thread(() =>
                {
                    Thread.Sleep(100); //Give rabbit a chance to raise the connection closed event.
                    if(_connectionClosed)
                        _log.Debug("Connection is clossed; aborting restart attempt.");
                    else
                        //Now restart only if the connection is not closed.  Otherwise we will restart in the OnConnectionReconnected event.
                        Restart();
                }).Start();
            }
            _log.Debug("Leave Handle_OnModelShutdown");
        }

        private void Handle_OnConnectionClosed(bool willAttemtToReopen)
        {
            _log.Debug(string.Format("Enter Handle_OnConnectionClosed, WillAttemptReopen: {0}", willAttemtToReopen));
            _connectionClosed = true;
            _shouldContinue = false;
            _log.Debug("Leave Handle_OnConnectionClosed");
        }

        private void Handle_OnConnectionReconnected()
        {
            _log.Debug("Enter Handle_OnConnectionReconnected");
            _connectionClosed = false;
            Restart();
            _log.Debug("Leave Handle_OnConnectionReconnected");
        }

        public void Stop()
        {
            _log.Debug("Enter Stop");
            _shouldContinue = false;
            _isRunning = false; 
            _log.Debug("Leave Stop");
        }


        protected virtual bool ShouldRaiseEvent(Predicate<Envelope> filter, Envelope env)
        {
            // if there's no filter, the client wants it.  Otherwise, see if they want it.
            return (null == filter) ? true : filter(env);
        }

        protected virtual void Raise_OnCloseEvent(IRegistration registration)
        {
            if (null != this.OnClose)
            {
                try { this.OnClose(registration); }
                catch { }
            }
        }

        protected virtual void Raise_OnEnvelopeReceivedEvent(RabbitEnvelopeDispatcher dispatcher)
        {
            if (null != this.OnEnvelopeReceived)
            {
                Task.Factory.StartNew(() => this.OnEnvelopeReceived(dispatcher));
            }
        }

        protected virtual void LogMessage(BasicDeliverEventArgs eventArgs)
        {
            StringBuilder buffer = new StringBuilder();
            buffer.AppendLine("Got a message from the queue -- ");

            using (StringWriter writer = new StringWriter(buffer))
            {
                DebugUtil.DumpProperties(eventArgs, writer, 0);
            }

            _log.Debug(buffer.ToString());
        }
    }
}
