using System.Collections.Generic;
using System.Threading;
using amp.tests.messaging;
using Moq;
using NUnit.Framework;
using RabbitMQ.Client;

using amp.messaging;
using amp.rabbit.connection;
using amp.rabbit.topology;
using amp.rabbit.transport;
using cmf.bus;

namespace amp.tests.rabbit.connection
{
    [TestFixture]
    public class ConnectionManagementTests
    {
        private IList<Mock<IConnection>> _connections;
        private IList<Mock<IModel>> _models;
        private Mock<ConnectionFactory> _rmqFactory;
        
        private RabbitTransportProvider _transport;

        private ManualResetEvent _modelCreateSignal;

        [SetUp]
        public void Setup()
        {
            _connections = new List<Mock<IConnection>>();
            _models = new List<Mock<IModel>>();
            _modelCreateSignal = new ManualResetEvent(false);

            _rmqFactory = new Mock<ConnectionFactory>();
            
            _rmqFactory.Setup(r => r.CreateConnection()).Returns(() =>
            {
                var connection = new Mock<IConnection>();
                _connections.Add(connection);

                connection.Setup(c => c.CreateModel()).Returns(() =>
                {
                    var model = new Mock<IModel>();
                    _models.Add(model);

                    model.Setup(m => m.CreateBasicProperties()).Returns(() =>
                    {
                        return new Mock<IBasicProperties>().Object;
                    });

                    _modelCreateSignal.Set();
                    
                    return model.Object;
                });

                connection.Setup(c => c.IsOpen).Returns(true);

                return connection.Object;
            });

            _transport = new RabbitTransportProvider(
                new SimpleTopologyService(null, "test", "nowhere.com", "/", 0),
                new TestConnectionFactory(_rmqFactory.Object),
                new SimpleRoutingInfoCache(100));
        }

        [Test]
        public void Send_should_create_a_connection_and_a_channel()
        {
            SendAMessage();

            _rmqFactory.Verify(r => r.CreateConnection(), "Connection to RabbitMQ was not created.");
            _connections[0].Verify(c => c.CreateModel(), "Channel for sent was not created.");
        }

        [Test]
        public void Register_should_create_a_connection_and_a_channel()
        {
            RegisterNullHandler();

            _rmqFactory.Verify(r => r.CreateConnection(), "Connection to RabbitMQ was not created.");
            _connections[0].Verify(c => c.CreateModel(), "Channel for listening was not created.");
        }

        [Test]
        public void Register_should_restart_on_new_channel_if_channel_fails()
        {
            RegisterNullHandler();

            SimulateChannelClosure(ShutdownInitiator.Peer);

            _rmqFactory.Verify(r => r.CreateConnection(), Times.Once(), "Connection to RabbitMQ incorrectly recreated.");
            _connections[0].Verify(c => c.CreateModel(), Times.Exactly(2), "Channel for listening was not recreated.");
        }

        [Test]
        public void Register_should_not_restart_if_channel_is_closed_by_application()
        {
            RegisterNullHandler();

            SimulateChannelClosure(ShutdownInitiator.Application);

            _rmqFactory.Verify(r => r.CreateConnection(), Times.Once(), "Connection to RabbitMQ incorrectly recreated.");
            _connections[0].Verify(c => c.CreateModel(), Times.Once(), "Channel for listening was recreated.");
        }

        [Test]
        public void Register_should_restart_on_new_connection_if_connection_fails()
        {
            RegisterNullHandler();

            SimulateConnectionClosure(ShutdownInitiator.Peer);

            _rmqFactory.Verify(r => r.CreateConnection(), Times.Exactly(2), "Connection to RabbitMQ was not recreated.");
            _connections[1].Verify(c => c.CreateModel(), Times.Once(), "Channel for listening was not recreated.");
            _connections[0].Verify(c => c.CreateModel(), Times.Once(), "Inappropriate attempts were made to recreate channel on closed connection.");
        }

        [Test]
        public void Register_should_not_restart_if_connection_is_closed_by_application()
        {
            RegisterNullHandler();

            SimulateConnectionClosure(ShutdownInitiator.Application);

            _rmqFactory.Verify(r => r.CreateConnection(), Times.Once(), "Connection to RabbitMQ incorrectly recreated.");
        }

        [Test]
        public void ClosingTheTransportShouldCloseAllConnections()
        {
            SendAMessage();
            RegisterNullHandler();

            _transport.Dispose();

            foreach (Mock<IConnection> connection in _connections)
            {
                connection.Verify(c => c.Close(), "Connection was not closed.");
            }
        }

        private void SendAMessage()
        {
            var env = new Envelope();
            env.Headers.Add(EnvelopeHeaderConstants.MESSAGE_TOPIC, "testing");

            _transport.Send(env);
        }

        private void RegisterNullHandler()
        {
            var env = new Envelope();
            env.Headers.Add(EnvelopeHeaderConstants.MESSAGE_TOPIC, "testing");

            _transport.Register(new MessageRegistration(e => null, new NullHandler()));

            _modelCreateSignal.WaitOne(250); //Registration happens on a background thread.
        }

        private void SimulateChannelClosure(ShutdownInitiator initiator)
        {
            _modelCreateSignal.Reset();

            _models[0].Raise(m => m.ModelShutdown += null, new ShutdownEventArgs(initiator, 0, "Testing..."));

            _modelCreateSignal.WaitOne(250); //Registration happens on a background thread.
        }

        private void SimulateConnectionClosure(ShutdownInitiator initiator)
        {
            _modelCreateSignal.Reset();

            _connections[0].Raise(c => c.ConnectionShutdown += null, new ShutdownEventArgs(initiator, 0, "Testing..."));

            _modelCreateSignal.WaitOne(250); //Registration happens on a background thread.
        }

        private class TestConnectionFactory : BaseConnectionFactory
        {
            readonly ConnectionFactory _factory;
            
            public TestConnectionFactory(ConnectionFactory factory)
            {
                _factory = factory;
            }

            protected override IConnectionManager CreateConnectionManager(Broker broker)
            {
                return new ConnectionManager(_factory);    
            }
        }
    }
}
