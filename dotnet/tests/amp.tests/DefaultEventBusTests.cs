using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

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
        [ExpectedException(typeof(EventException))]
        public void Should_Throw_An_Exception_When_Payload_Is_Empty()
        {
            // create an empty event
            var ev = new {Name = "test"};


            // mock up an envelope bus
            var envBusMock = new Mock<IEnvelopeBus>();


            // create the event bus we're testing
            DefaultEventBus bus = new DefaultEventBus(envBusMock.Object);

            // send the event through the bus
            bus.Publish(ev);


            // make sure that the envelope bus was never given an envelope
            envBusMock.Verify(envBus => envBus.Send(It.IsAny<Envelope>()), Times.Never());
        }

        [Test]
        public void Should_Receive_Events_Even_When_Inbound_Chain_Is_Null()
        {
            // create an envelope with content
            var env = new Envelope() {Payload = Encoding.UTF8.GetBytes("Test")};


            // mock up an envelope bus
            var envBusMock = new Mock<IEnvelopeBus>();
            // mock up an IEventHandler
            var handlerMock = new Mock<IEventHandler>();


            // the event bus we're testing - explicity set inbound chain to null
            DefaultEventBus bus = new DefaultEventBus(envBusMock.Object);
            bus.InboundChain = null;

            // simulate an incoming event
            bus.InterceptEvent(handlerMock.Object, env);


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


            // create the extended event bus so we can call a protected method
            ExtendedDefaultEventBus bus = new ExtendedDefaultEventBus(envBusMock.Object);
            bus.OutboundChain = null;

            bus.Publish(ev);


            // make sure that the envelope bus was given an envelope
            envBusMock.Verify(envBus => envBus.Send(It.IsAny<Envelope>()), Times.Once());
        }

        [Test]
        public void Should_Sort_Processor_Chains_Even_When_Added_Out_Of_Order()
        {
            Dictionary<int, IEventProcessor> unsortedChain = new Dictionary<int, IEventProcessor>();


            // mock a few processors
            var procMock1 = new Mock<IEventProcessor>();
            var procMock2 = new Mock<IEventProcessor>();
            var procMock3 = new Mock<IEventProcessor>();

            // add them out of order
            unsortedChain.Add(2, procMock2.Object);
            unsortedChain.Add(3, procMock3.Object);
            unsortedChain.Add(1, procMock1.Object);

            IEnumerable<IEventProcessor> sortedChain = unsortedChain.Sort();

            Assert.AreSame(procMock1.Object, sortedChain.ElementAt(0));
            Assert.AreSame(procMock2.Object, sortedChain.ElementAt(1));
            Assert.AreSame(procMock3.Object, sortedChain.ElementAt(2));
        }
    }



    public class ExtendedDefaultEventBus : DefaultEventBus
    {
        public ExtendedDefaultEventBus(IEnvelopeBus envBus) : base(envBus)
        {
        }

        public override void ProcessEvent(EventContext context, IEnumerable<IEventProcessor> processorChain, Action processingComplete)
        {
            // set the payload of the envelope to something, or an exception will be thrown
            context.Envelope = new Envelope() {Payload = Encoding.UTF8.GetBytes("Test")};

            base.ProcessEvent(context, processorChain, processingComplete);
        }
    }
}
