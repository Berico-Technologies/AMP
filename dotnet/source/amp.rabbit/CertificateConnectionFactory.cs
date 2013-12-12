using System.Security.Cryptography.X509Certificates;
using RabbitMQ.Client;

using amp.rabbit.topology;
using amp.bus.security;

namespace amp.rabbit
{
    public class CertificateConnectionFactory : BaseConnectionFactory
    {
        protected ICertificateProvider _certProvider;
    
        public CertificateConnectionFactory(ICertificateProvider certificateProvider)
        {
            _certProvider = certificateProvider;
        }

        public override void ConfigureConnectionFactory(ConnectionFactory factory, Exchange exchange)
        {
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

            base.ConfigureConnectionFactory(factory, exchange);

            // let's set the connection factory's ssl-specific settings
            // NOTE: it's absolutely required that what you set as Ssl.ServerName be
            //       what's on your rabbitmq server's certificate (its CN - common name)
            factory.AuthMechanisms = new AuthMechanismFactory[] { new ExternalMechanismFactory() };
            factory.Ssl.Certs = new X509CertificateCollection(new X509Certificate[] { cert });
            factory.Ssl.ServerName = exchange.HostName;
            factory.Ssl.Enabled = true;
            // TLS will enable more secure cipher suites to use in the exchange, encryption, and HMAC algorithms 
            // used on a secure connection. Also, if the Windows OS the client runs on has the FIPS Mode security
            // policy enabled (Windows STIG), this will ensure successful connections to the Message Broker.
            factory.Ssl.Version = System.Security.Authentication.SslProtocols.Tls;

        }
    }
}
