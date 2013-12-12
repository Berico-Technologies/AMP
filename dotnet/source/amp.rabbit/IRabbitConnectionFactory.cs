using System;
using RabbitMQ.Client;

using amp.rabbit.topology;

namespace amp.rabbit
{
    public interface IRabbitConnectionFactory : IDisposable
    {
        IConnection ConnectTo(Exchange exchange);
    }
}
