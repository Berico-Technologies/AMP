using System;
using System.Net;
using amp.rabbit;
using amp.rabbit.topology;
using NUnit.Framework;
using RabbitMQ.Client;
using Spring.Context;
using Spring.Context.Support;

namespace amp.tests.integration.Rabbit
{
    [TestFixture]
    public class TokenConnectionFactoryTests
    {
        protected IApplicationContext _context;
        protected TokenConnectionFactory _factory;

        [TestFixtureSetUp]
        public virtual void TestFixtureSetUp()
        {
            _context = new XmlApplicationContext(Config.Authorization.AnubisTwoWaySsl, Config.Topology.GtsSSL);
            _factory = _context.GetObject("IRabbitConnectionFactory") as TokenConnectionFactory;

            //Hack so that we don't have to validate the Anubis server certificate.
            ServicePointManager.ServerCertificateValidationCallback = delegate { return true; };
        }

        [TestFixtureTearDown]
        public virtual void TestFixtureTearDown()
        {
            //Reverse the Hack
            ServicePointManager.ServerCertificateValidationCallback = null;

            _context.Dispose();
        }

        [Test]
        public void Should_be_able_to_get_token_from_Anubis()
        {
            //var rabbitFactory = new Mock<ConnectionFactory>();
            var rabbitFactory = new ConnectionFactory();
            _factory.ConfigureConnectionFactory(rabbitFactory, new Exchange());

            Assert.That(rabbitFactory.UserName, Is.Not.Null);
            Assert.That(rabbitFactory.Password, Is.Not.Null);
        }

        public class TestEvent
        {
            public Guid Id { get; set; }

            public TestEvent()
            {
                Id = Guid.NewGuid();
            }
        }
    }
}
