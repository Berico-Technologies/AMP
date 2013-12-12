using System;
using System.Net;
using System.Threading;
using cmf.eventing;
using NUnit.Framework;
using Spring.Context;
using Spring.Context.Support;

namespace amp.tests.integration.ConfigScenarios
{
    /// <summary>
    /// Tests ability to connect and do basic using simple topo, basic auth in the clear,
    /// but with credentials from Anubis.
    /// </summary>
    [TestFixture]
    public class GtsBasicAuth
    {
        protected IApplicationContext _context;
        protected IEventBus _bus;

        protected virtual string[] ConfigFiles
        {
            get
            {
                return new string[] { Config.Bus.All, Config.Authorization.AnubisTwoWaySsl, Config.Topology.GtsSSL};
            }
        }

        [TestFixtureSetUp]
        public virtual void TestFixtureSetUp()
        {
            _context = new XmlApplicationContext(ConfigFiles);
            _bus = _context.GetObject("IEventBus") as IEventBus;

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
        public void Should_be_able_to_publish_and_subscribe()
        {
            TestEvent receivedEvent = null;
            _bus.Subscribe<TestEvent>((@event, headers) =>
            {
                receivedEvent = @event;
            });

            TestEvent sentEvent = new TestEvent();

            _bus.Publish(sentEvent);

            var end = DateTime.Now.AddSeconds(5);
            while (receivedEvent == null && DateTime.Now < end)
            {
                Thread.Sleep(100);
            }

            Assert.That(receivedEvent, Is.Not.Null, "Event not received within timeout period.");
            Assert.That(receivedEvent.Id, Is.EqualTo(sentEvent.Id), "Received and Sent events were not the same.");
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
