using System;
using System.Threading;
using Common.Logging;
using RabbitMQ.Client;

namespace amp.rabbit.connection
{
    /// <summary>
    /// Wrapps and manages a single connection, reconnecting if it fails.
    /// </summary>
    public class ConnectionManager : IConnectionManager
    {
        protected ILog _log;

        private readonly ManualResetEvent _connectionEvent = new ManualResetEvent(true);
        private readonly ConnectionFactory _factory;
        private IConnection _connection;
        private volatile bool _isDisposed;
        
        public event ConnectionClosedEventHandler ConnectionClosed;

        public event ConnectionReconnectedEventHandler ConnectionReconnected;

        public ConnectionManager(ConnectionFactory factory)
        {
            _log = LogManager.GetLogger(this.GetType());
            _factory = factory;
            _connection = _factory.CreateConnection();
            _connection.ConnectionShutdown += Handle_OnConnectionShutdown;
        }

        private void Handle_OnConnectionShutdown(IConnection connection, ShutdownEventArgs reason)
        {
            //Keep people from tyring to use the connection while we are reconnecting.
            _connectionEvent.Reset();

            bool shouldAttemtToReopen = reason.Initiator != ShutdownInitiator.Application;
                
            if (ConnectionClosed != null)
            {
                try
                {
                    ConnectionClosed(shouldAttemtToReopen);
                }
                catch (Exception ex)
                {
                    _log.Warn("ConnectionClosed handler threw an exception.", ex);
                }
            }

            if (shouldAttemtToReopen)
            {
                //Do actual reconnect attempts on background thread
                Thread reconnectThread = new Thread(AttemptToReconnect);
                reconnectThread.Name = string.Format("Connection Reconnect Thread {0}", this.GetHashCode());
                reconnectThread.Start();
            }
            else
            {
                //Not going to re-open so let people try and blow up.  Better than hanging!
                _connectionEvent.Set();
            }
        }

        private void AttemptToReconnect()
        {
            //TODO: Make timeout and attempt frequency configurable.
            DateTime endTime = DateTime.Now + TimeSpan.FromMinutes(5);
            while (!_isDisposed && endTime > DateTime.Now)
            {
                try
                {
                    _connection = _factory.CreateConnection();
                    _connection.ConnectionShutdown += Handle_OnConnectionShutdown;
                    //Permit access to the connection once again.
                    _connectionEvent.Set();
                    _log.Info("Successfully reconnected.");

                    if (ConnectionReconnected != null)
                    {
                        try
                        {
                            ConnectionReconnected();
                        }
                        catch (Exception ex)
                        {
                            _log.Warn("ConnectionReconnected handler threw an exception.", ex);
                        }
                    }

                    return;
                }
                catch (Exception ex)
                {
                    _log.Error("Reconnect attempt failed.", ex);
                    Thread.Sleep(TimeSpan.FromSeconds(1));
                }
            }

            if(!_isDisposed)
                _log.Info("Failed to reconnect in the time allowed.  Will no longer attempt.");
            
            //release access to the connection. Better to let attempts fail that to have them hang.
            _connectionEvent.Set();
        }

        public IModel CreateModel()
        {
            //Ensure we are not in the midst of a reconnect attempt. If so wait till we reconnect.
            _connectionEvent.WaitOne();
            return _connection.CreateModel();
        }

        public void Dispose()
        {
            _isDisposed = true;

            if (_connection != null && _connection.IsOpen)
                _connection.Close();
        }
    }
}
