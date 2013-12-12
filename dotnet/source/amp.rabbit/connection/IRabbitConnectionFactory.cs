using System;
using amp.rabbit.topology;

namespace amp.rabbit.connection
{
    public interface IRabbitConnectionFactory : IDisposable
    {
        IConnectionManager ConnectTo(Broker broker);
    }
}
