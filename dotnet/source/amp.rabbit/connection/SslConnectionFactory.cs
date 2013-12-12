using amp.rabbit.topology;
using RabbitMQ.Client;

namespace amp.rabbit.connection
{
    public class SslConnectionFactory : BaseConnectionFactory
    {
        public override void ConfigureConnectionFactory(ConnectionFactory factory, Broker broker)
        {
            base.ConfigureConnectionFactory(factory, broker);

            factory.Ssl.ServerName = broker.Hostname;
            factory.Ssl.Enabled = true;
        }
    }
}