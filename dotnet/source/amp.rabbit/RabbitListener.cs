using System;
using System.Collections;
using System.IO;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using amp.messaging;
using Common.Logging;
using RabbitMQ.Client;
using RabbitMQ.Client.Events;
using RabbitMQ.Client.Exceptions;
using RabbitMQ.Util;

using cmf.bus;
using amp.bus;
using amp.rabbit.topology;

namespace amp.rabbit
{
    public class RabbitListener
    {
        public event Action<IEnvelopeDispatcher> OnEnvelopeReceived;
        public event Func<IRegistration, bool> OnClose;


        protected IRegistration _registration;
        protected bool _shouldContinue;
        protected ILog _log;
        protected Exchange _exchange;
        protected IConnection _connection;


        public RabbitListener(IRegistration registration, Exchange exchange, IConnection connection)
        {
            _registration = registration;
            _exchange = exchange;
            _connection = connection;

            _log = LogManager.GetLogger(this.GetType());
        }


        public void Start(object manualResetEvent)
        {
            ManualResetEvent startEvent = manualResetEvent as ManualResetEvent;

            _log.Debug("Enter Start");
            _shouldContinue = true;

            using (IModel channel = _connection.CreateModel())
            {
                // first, declare the exchange and queue
                channel.ExchangeDeclare(_exchange.Name, _exchange.ExchangeType, _exchange.IsDurable, _exchange.IsAutoDelete, _exchange.Arguments);
                channel.QueueDeclare(_exchange.QueueName, _exchange.IsDurable, false, _exchange.IsAutoDelete, _exchange.Arguments);
                channel.QueueBind(_exchange.QueueName, _exchange.Name, _exchange.RoutingKey, _exchange.Arguments);

                // next, create a basic consumer
                QueueingBasicConsumer consumer = new QueueingBasicConsumer(channel);

                // and tell it to start consuming messages, storing the consumer tag
                string consumerTag = channel.BasicConsume(_exchange.QueueName, false, consumer);

                // signal the wait event that we've begun listening
                startEvent.Set();

                _log.Debug("Will now continuously listen for events using routing key: " + _exchange.RoutingKey);
                while (_shouldContinue)
                {
                    try
                    {
                        object result = null;

                        if (false == consumer.Queue.Dequeue(100, out result)) { continue; }
                        BasicDeliverEventArgs e = result as BasicDeliverEventArgs;
                        if (null == e) { continue; }
                        else { this.LogMessage(e); }

                        IBasicProperties props = e.BasicProperties;

                        Envelope env = new Envelope();
                        env.SetReceiptTime(DateTime.Now);
                        env.Payload = e.Body;
                        foreach (DictionaryEntry entry in props.Headers)
                        {
                            try
                            {
                                string key = entry.Key as string;
                                string value = Encoding.UTF8.GetString((byte[])entry.Value);
                                _log.Debug("Adding header to envelope: {" + key + ":" + value + "}");

                                env.Headers.Add(key, value);
                            }
                            catch { }
                        }

                        if (this.ShouldRaiseEvent(_registration.Filter, env))
                        {
                            RabbitEnvelopeDispatcher dispatcher = new RabbitEnvelopeDispatcher(_registration, env, channel, e.DeliveryTag);
                            this.Raise_OnEnvelopeReceivedEvent(dispatcher);
                        }
                    }
                    catch (OperationInterruptedException)
                    {
                        // The consumer was removed, either through
                        // channel or connection closure, or through the
                        // action of IModel.BasicCancel().
                        _shouldContinue = false;
                    }
                    catch { }
                }
                _log.Debug("No longer listening for events");

                try { channel.BasicCancel(consumerTag); }
                catch (OperationInterruptedException) { }
            }

            _log.Debug("Leave Start");
        }

        public void Stop()
        {
            _log.Debug("Enter Stop");
            _shouldContinue = false;
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
