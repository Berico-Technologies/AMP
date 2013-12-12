using System;
using RabbitMQ.Client;

namespace amp.rabbit.connection
{
    public delegate void ConnectionClosedEventHandler(bool willAttemtToReopen);
    public delegate void ConnectionReconnectedEventHandler();
        
    public interface IConnectionManager : IDisposable
    {
        event ConnectionClosedEventHandler ConnectionClosed;

        event ConnectionReconnectedEventHandler ConnectionReconnected;

        IModel CreateModel();
    }
}