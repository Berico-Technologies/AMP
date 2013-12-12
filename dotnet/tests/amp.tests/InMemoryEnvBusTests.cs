using System;
using System.Text;
using System.Threading;

using Moq;
using NUnit.Framework;

using cmf.bus;
using cmf.bus.support;

namespace amp.tests
{
    [TestFixture]
    public class InMemoryEnvBusTests
    {
        private MockRepository _mocker;


        [TestFixtureSetUp]
        public void TestFixtureSetup()
        {
            _mocker = new MockRepository(MockBehavior.Loose);
        }


        [Test]
        public void Should_Receive_Envelopes_When_Filter_Returns_True()
        {
            InMemoryEnvelopeBus bus = new InMemoryEnvelopeBus();
            AutoResetEvent waitHandle = new AutoResetEvent(false);

            Envelope env = new Envelope();
            env.Payload = Encoding.UTF8.GetBytes("Test");

            Mock<IRegistration> reg = _mocker.Create<IRegistration>();
            reg.Setup(r => r.Filter).Returns(new Predicate<Envelope>(e => true));
            reg.Setup(r => r.Handle(env)).Callback(() => waitHandle.Set());

            bus.Register(reg.Object);

            bus.Send(env);
            waitHandle.WaitOne(5000);

            reg.Verify(r => r.Handle(env), Times.Once());
        }

        [Test]
        public void Should_Not_Receive_Envelopes_When_Filter_Returns_False()
        {
            InMemoryEnvelopeBus bus = new InMemoryEnvelopeBus();

            Envelope env = new Envelope();
            env.Payload = Encoding.UTF8.GetBytes("Test");

            Mock<IRegistration> reg = _mocker.Create<IRegistration>();
            reg.Setup(r => r.Filter).Returns(new Predicate<Envelope>(e => false));

            bus.Register(reg.Object);

            bus.Send(env);

            reg.Verify(r => r.Handle(env), Times.Never());
        }

        [Test]
        public void Should_Receive_Many_Envelopes_When_Filter_Returns_True()
        {
            InMemoryEnvelopeBus bus = new InMemoryEnvelopeBus();
            AutoResetEvent waitHandle = new AutoResetEvent(false);

            Envelope env = new Envelope();
            env.Payload = Encoding.UTF8.GetBytes("Test");

            Mock<IRegistration> reg = _mocker.Create<IRegistration>();
            reg.Setup(r => r.Filter).Returns(new Predicate<Envelope>(e => true));
            reg.Setup(r => r.Handle(env)).Callback(() => waitHandle.Set());

            bus.Register(reg.Object);

            bus.Send(env);
            waitHandle.WaitOne(5000);

            bus.Send(env);
            waitHandle.WaitOne(5000);

            bus.Send(env);
            waitHandle.WaitOne(5000);

            bus.Send(env);
            waitHandle.WaitOne(5000);

            bus.Send(env);
            waitHandle.WaitOne(5000);

            reg.Verify(r => r.Handle(env), Times.Exactly(5));
        }
    }
}
