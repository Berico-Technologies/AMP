using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography.X509Certificates;
using System.Text;

using Common.Logging;
using RabbitMQ.Client;

using amp.bus.rabbit.topology;
using amp.bus.security;

namespace amp.bus.rabbit
{
    public class CertificateConnectionFactory : IRabbitConnectionFactory, IDisposable
    {
        protected ICertificateProvider _certProvider;
        protected IDictionary<Exchange, IConnection> _connections;
        protected ILog _log;


        public CertificateConnectionFactory(ICertificateProvider certificateProvider)
        {
            _certProvider = certificateProvider;
            _connections = new Dictionary<Exchange, IConnection>();

            _log = LogManager.GetLogger(this.GetType());
        }


        public IConnection ConnectTo(Exchange exchange)
        {
            _log.Debug("Enter ConnectTo");

            IConnection conn = null;

            if (_connections.ContainsKey(exchange))
            {
                conn = _connections[exchange];
                if (!conn.IsOpen)
                {
                    _log.Info("Cached connection to RabbitMQ was closed: reconnecting");
                    conn = this.CreateConnection(exchange);
                }
            }
            else
            {
                conn = this.CreateConnection(exchange);
                _connections[exchange] = conn;
            }

            _log.Debug("Leave ConnectTo");
            return conn;
        }

        public void Dispose()
        {
            try { _connections.ToList().ForEach(kvp => kvp.Value.Dispose()); }
            catch { }
        }


        protected virtual IConnection CreateConnection(Exchange ex)
        {
            _log.Debug("Enter CreateConnection");

            // try to get a certificate
            X509Certificate2 cert = _certProvider.GetCertificate();
            if (null != cert)
            {
                _log.Info("A certificate was located with subject: " + cert.Subject);
            }
            else
            {
                throw new RabbitException("Cannot connect to an exchange because no certificate was found");
            }

            IConnection conn = null;

            // we use the rabbit connection factory, just like normal
            ConnectionFactory cf = new ConnectionFactory();

            // set the hostname and the port
            cf.HostName = ex.HostName;
            cf.VirtualHost = ex.VirtualHost;
            cf.Port = ex.Port;

            // now, let's set the connection factory's ssl-specific settings
            // NOTE: it's absolutely required that what you set as Ssl.ServerName be
            //       what's on your rabbitmq server's certificate (its CN - common name)
            cf.AuthMechanisms = new AuthMechanismFactory[] { new ExternalMechanismFactory() };
            cf.Ssl.Certs = new X509CertificateCollection(new X509Certificate[] { cert });
            cf.Ssl.ServerName = ex.HostName;
            cf.Ssl.Enabled = true;
            cf.Ssl.Version = System.Security.Authentication.SslProtocols.Tls;

            // we either now create an SSL connection or a default "guest/guest" connection
            try
            {
                conn = cf.CreateConnection();
            }
            catch (Exception e) 
            {
                _log.Error("Unable to establish connection with RabbitMQ.", e);
                throw e;
            }

            _log.Debug("Leave CreateConnection");
            return conn;
        }
    }
}
