using System;
using System.Collections.Generic;
using amp.rabbit.topology;
using Common.Logging;
using RabbitMQ.Client;

namespace amp.rabbit
{
    public abstract class BaseConnectionFactory : IRabbitConnectionFactory
    {
        protected ILog _log;
        protected IDictionary<Exchange, IConnection> _connections;

        protected BaseConnectionFactory()
        {
            _connections = new Dictionary<Exchange, IConnection>();
            _log = LogManager.GetLogger(this.GetType());
        }

        public IConnection ConnectTo(Exchange exchange)
        {
            _log.Debug("Getting connection for exchange: " + exchange.ToString());
            IConnection connection = null;

            // first, see if we have a cached connection
            if (_connections.ContainsKey(exchange))
            {
                connection = _connections[exchange];

                if (!connection.IsOpen)
                {
                    _log.Info("Cached connection to RabbitMQ was closed: reconnecting");
                    connection = this.CreateConnection(exchange);
                }
            }
            else
            {
                _log.Debug("No connection to the exchange was cached: creating");
                connection = this.CreateConnection(exchange);

                // add the new connection to the cache
                _connections[exchange] = connection;
            }

            return connection;
        }

        public void Dispose()
        {
            foreach (IConnection conn in _connections.Values)
            {
                try { conn.Close(); }
                catch { }
            }
        }

        private IConnection CreateConnection(Exchange exchange)
        {
            _log.Debug("Enter CreateConnection");

            ConnectionFactory cf = new ConnectionFactory();
            ConfigureConnectionFactory(cf, exchange);
            try
            {
                IConnection connection = cf.CreateConnection();
                _log.Debug("Leave CreateConnection");
                return connection;
            }
            catch (Exception e)
            {
                _log.Error("Unable to establish connection with RabbitMQ.", e);
                throw e;
            }
        }

        public virtual void ConfigureConnectionFactory(ConnectionFactory factory, Exchange exchange)
        {
            factory.HostName = exchange.HostName;
            factory.VirtualHost = exchange.VirtualHost;
            factory.Port = exchange.Port;
        }
    }
}