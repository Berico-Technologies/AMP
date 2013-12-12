using System;
using System.Collections.Generic;
using amp.rabbit.topology;
using Common.Logging;
using RabbitMQ.Client;

namespace amp.rabbit.connection
{
    public abstract class BaseConnectionFactory : IRabbitConnectionFactory
    {
        protected ILog _log;
        protected IDictionary<Broker, IConnectionManager> _connectionManagers;

        protected BaseConnectionFactory()
        {
            _connectionManagers = new Dictionary<Broker, IConnectionManager>();
            _log = LogManager.GetLogger(this.GetType());
        }

        public IConnectionManager ConnectTo(Broker broker)
        {
            _log.Debug("Getting connection for broker: " + broker.ToString());
            IConnectionManager manager = null;

            // first, see if we have a cached connection
            if (_connectionManagers.ContainsKey(broker))
            {
                manager = _connectionManagers[broker];
            }
            else
            {
                _log.Debug("No connection to the exchange was cached: creating");
                manager = this.CreateConnectionManager(broker);

                // add the new connection to the cache
                _connectionManagers[broker] = manager;
            }

            return manager;
        }

        public void Dispose()
        {
            foreach (IConnectionManager conn in _connectionManagers.Values)
            {
                try { conn.Dispose(); }
                catch { }
            }
        }

        protected virtual IConnectionManager CreateConnectionManager(Broker broker)
        {
            _log.Debug("Enter CreateConnectionManager");

            ConnectionFactory cf = new ConnectionFactory();
            ConfigureConnectionFactory(cf, broker);
            try
            {
                IConnectionManager connection = new ConnectionManager(cf);
                _log.Debug("Leave CreateConnectionManager");
                return connection;
            }
            catch (Exception e)
            {
                _log.Error("Unable to establish connection with RabbitMQ.", e);
                throw e;
            }
        }

        public virtual void ConfigureConnectionFactory(ConnectionFactory factory, Broker broker)
        {
            factory.HostName = broker.Hostname;
            factory.VirtualHost = broker.VirtualHost;
            factory.Port = broker.Port;
        }
    }
}