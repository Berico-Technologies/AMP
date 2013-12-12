using System;
using System.Collections.Generic;
using System.Text;
using amp.messaging;
using Moq;
using NUnit.Framework;

using cmf.bus;
using cmf.eventing;

using amp.eventing;

namespace amp.tests
{
    [TestFixture]
    public class DefaultEventBusTests
    {
        [Test]
        //TODO: Resolve the issue state in the Ignore attribute.
        [Ignore(("This test appears to be in opposition to the final test. Need to resolve this."))]
        [ExpectedException(typeof(MessageException))]
        public void Should_Throw_An_Exception_When_Payload_Is_Empty()
        {
            // create an empty event
            var ev = new {Name = "test"};


            // mock up an envelope bus
            var envBusMock = new Mock<IEnvelopeBus>();


            // create the event bus we're testing
            DefaultEventBus bus = new DefaultEventBus(envBusMock.Object, new List<IMessageProcessor>(), new List<IMessageProcessor>());

            // send the event through the bus
            bus.Publish(ev);


            // make sure that the envelope bus was never given an envelope
            envBusMock.Verify(envBus => envBus.Send(It.IsAny<Envelope>()), Times.Never());
        }

        [Test]
        public void Should_Receive_Events_Even_When_Inbound_Chain_Is_Null()
        {
            IRegistration registration = null;

            // create an envelope with content
            var env = new Envelope() {Payload = Encoding.UTF8.GetBytes("Test")};


            // mock up an envelope bus
            var envBusMock = new Mock<IEnvelopeBus>();
            envBusMock.Setup(envBus => envBus.Register(It.IsAny<IRegistration>()))
                .Callback<IRegistration>(r => { registration = r; });
            // mock up an IEventHandler
            var handlerMock = new Mock<IEventHandler>();


            // the event bus we're testing - explicity set inbound chain to null
            DefaultEventBus bus = new DefaultEventBus(envBusMock.Object, null, null);


            //wire up handler
            bus.Subscribe(handlerMock.Object);

            //Simulate received event.
            registration.Handle(env);

            // make sure the handler's Handle method was called
            handlerMock.Verify(handler => handler.Handle(It.IsAny<object>(), It.IsAny<IDictionary<string, string>>()), Times.Once());
        }

        [Test]
        public void Should_Send_Events_Even_When_Outbound_Chain_Is_Null()
        {
            // create an event
            var ev = new {Name = "test"};


            // mock an envelope bus
            var envBusMock = new Mock<IEnvelopeBus>();


            DefaultEventBus bus = new DefaultEventBus(envBusMock.Object, null,  null);
            
            bus.Publish(ev);


            // make sure that the envelope bus was given an envelope
            envBusMock.Verify(envBus => envBus.Send(It.IsAny<Envelope>()), Times.Once());
        }
    
    }
}
