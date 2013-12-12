using System;
using System.Threading;
using amp.commanding;
using NUnit.Framework;
using Spring.Context;
using Spring.Context.Support;

namespace amp.tests.integration.Commanding
{
    [TestFixture]
    public class DefaultCommandBusTests
    {
        protected IApplicationContext _context;
        protected ICommandBus _bus;

        [TestFixtureSetUp]
        public virtual void TestFixtureSetUp()
        {
            _context = new XmlApplicationContext(Config.Bus.All, Config.Authorization.AnubisOneWaySsl, Config.Topology.GtsSSL);
            _bus = _context.GetObject("ICommandBus") as ICommandBus;
        }

        [TestFixtureTearDown]
        public virtual void TestFixtureTearDown()
        {
            _context.Dispose();
        }

        [Test]
        public void Should_be_able_to_publish_and_subscribe()
        {
            
            TestCommand receivedCommand = null;
            _bus.ReceiveCommand<TestCommand>((@command, headers) =>
            {
                receivedCommand = @command;
            });

            TestCommand sentCommand = new TestCommand();

            _bus.Send(sentCommand);

            var end = DateTime.Now.AddSeconds(5);
            while (receivedCommand == null && DateTime.Now < end)
            {
                Thread.Sleep(100);
            }

            Assert.That(receivedCommand, Is.Not.Null, "Command not received within timeout period.");
            Assert.That(receivedCommand.Id, Is.EqualTo(sentCommand.Id), "Received and Sent commands were not the same.");
        }

        public class TestCommand
        {
            public Guid Id { get; set; }

            public TestCommand()
            {
                Id = Guid.NewGuid();
            }
        }
    }
}
