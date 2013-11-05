﻿using System.Security.Cryptography.X509Certificates;
using amp.bus.security;
using amp.rabbit.topology;
using RabbitMQ.Client;

namespace amp.rabbit.connection
{
    public class CertificateConnectionFactory : BaseConnectionFactory
    {
        protected ICertificateProvider _certProvider;
    
        public CertificateConnectionFactory(ICertificateProvider certificateProvider)
        {
            _certProvider = certificateProvider;
        }

        public override void ConfigureConnectionFactory(ConnectionFactory factory, Broker broker)
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

            base.ConfigureConnectionFactory(factory, broker);

            // let's set the connection factory's ssl-specific settings
            // NOTE: it's absolutely required that what you set as Ssl.ServerName be
            //       what's on your rabbitmq server's certificate (its CN - common name)
            factory.AuthMechanisms = new AuthMechanismFactory[] { new ExternalMechanismFactory() };
            factory.Ssl.Certs = new X509CertificateCollection(new X509Certificate[] { cert });
            factory.Ssl.ServerName = broker.Hostname;
            factory.Ssl.Enabled = true;

        }
    }
}