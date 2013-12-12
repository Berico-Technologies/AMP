using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Moq;
using NUnit.Framework;

using cmf.bus;
using amp.bus;


namespace amp.tests.eventing
{
    [TestFixture]
    public class DefaultEnvelopeBusTests
    {
        MockRepository _mocker;


        [SetUp]
        public void Setup()
        {
            _mocker = new MockRepository(MockBehavior.Loose);
        }


        [Test]
        public void Should_Give_Registrations_To_Transport_Layer()
        {
            Mock<ITransportProvider> txMock = _mocker.Create<ITransportProvider>();
            Mock<IRegistration> regMock = _mocker.Create<IRegistration>();

            DefaultEnvelopeBus bus = new DefaultEnvelopeBus(txMock.Object);
            bus.Register(regMock.Object);

            txMock.Verify(tx => tx.Register(regMock.Object), Times.Once());
        }

        [Test]
        public void Should_Dispatch_Envelopes_Even_When_InboundChain_Is_Null()
        {
            // create a mostly empty envelope
            Envelope env = new Envelope() { Payload = Encoding.UTF8.GetBytes("Test") };
            
            // create the mock dispatcher and transport provider
            Mock<IEnvelopeDispatcher> dispatcherMock = _mocker.Create<IEnvelopeDispatcher>();
            Mock<ITransportProvider> txMock = _mocker.Create<ITransportProvider>();

            // setup the dispatcher to return the envelope when the getter is called
            dispatcherMock.Setup(d => d.Envelope).Returns(env);

            // create the unit under test with the mock transport and null processing chains
            DefaultEnvelopeBus bus = new DefaultEnvelopeBus(txMock.Object);
            bus.InboundChain = null;
            bus.OutboundChain = null;

            // have the transport mock raise its envelope event
            txMock.Raise(tx => tx.OnEnvelopeReceived += null, dispatcherMock.Object);

            // verify that the dispatcher's dispatch method was called
            // (this implies successful inbound chain processing)
            dispatcherMock.Verify(d => d.Dispatch(env), Times.Once());
        }

        [Test]
        public void Should_Send_Envelopes_Even_When_OutboundChain_Is_Null()
        {
            Envelope env = new Envelope() { Payload = Encoding.UTF8.GetBytes("Test") };

            Mock<ITransportProvider> txMock = _mocker.Create<ITransportProvider>();

            DefaultEnvelopeBus bus = new DefaultEnvelopeBus(txMock.Object);
            bus.InboundChain = null;
            bus.OutboundChain = null;

            bus.Send(env);

            txMock.Verify(tx => tx.Send(env), Times.Once());
        }

        [Test]
        public void Should_Send_Outgoing_Envelopes_Through_the_Chain()
        {
            // create an envelope
            Envelope env = new Envelope() { Payload = Encoding.UTF8.GetBytes("Test") };

            // mock a transport provider and envelope processor
            Mock<ITransportProvider> txMock = _mocker.Create<ITransportProvider>();
            Mock<IEnvelopeProcessor> procMock = _mocker.Create<IEnvelopeProcessor>();

            // setup the processor to call its continuation
            procMock
                .Setup(
                    proc => proc.ProcessEnvelope(It.IsAny<EnvelopeContext>(), It.IsAny<Action>()))
                .Callback<EnvelopeContext, Action>(
                    (newCtx, continuation) => continuation());

            // create the unit under test and give it the transport provider and processor chain
            DefaultEnvelopeBus bus = new DefaultEnvelopeBus(txMock.Object);
            bus.OutboundChain = new Dictionary<int, IEnvelopeProcessor>();
            bus.OutboundChain.Add(0, procMock.Object);
            
            // send the envelope
            bus.Send(env);

            // verify the expected calls
            procMock.Verify(
                proc => proc.ProcessEnvelope(It.IsAny<EnvelopeContext>(), It.IsAny<Action>()), Times.Once());
            txMock.Verify(tx => tx.Send(env), Times.Once());
        }

        [Test]
        public void Should_Send_Incoming_Envelopes_Through_the_Chain()
        {
            // create an envelope and context
            Envelope env = new Envelope() { Payload = Encoding.UTF8.GetBytes("Test") };
            EnvelopeContext ctx = new EnvelopeContext(EnvelopeContext.Directions.In, env);

            // mock a transport provider and envelope processor
            Mock<ITransportProvider> txMock = _mocker.Create<ITransportProvider>();
            Mock<IEnvelopeProcessor> procMock = _mocker.Create<IEnvelopeProcessor>();
            Mock<IEnvelopeDispatcher> dispatcherMock = _mocker.Create<IEnvelopeDispatcher>();

            dispatcherMock.SetupGet<Envelope>(disp => disp.Envelope).Returns(env);

            DefaultEnvelopeBus bus = new DefaultEnvelopeBus(txMock.Object);
            bus.OutboundChain = null;
            bus.InboundChain = new Dictionary<int, IEnvelopeProcessor>();
            bus.InboundChain.Add(0, procMock.Object);

            txMock.Raise(tx => tx.OnEnvelopeReceived += null, dispatcherMock.Object);

            procMock.Verify(
                proc => proc.ProcessEnvelope(It.IsAny<EnvelopeContext>(), It.IsAny<Action>()), Times.Once());
        }

        [Test]
        public void Should_Not_Continue_Processing_If_Processor_Does_Not_Call_Continuation()
        {
            // create an envelope and context
            Envelope env = new Envelope() { Payload = Encoding.UTF8.GetBytes("Test") };
            EnvelopeContext ctx = new EnvelopeContext(EnvelopeContext.Directions.In, env);

            // mock a transport provider and envelope processor
            Mock<ITransportProvider> txMock = _mocker.Create<ITransportProvider>();
            Mock<IEnvelopeProcessor> procMock = _mocker.Create<IEnvelopeProcessor>();

            // create a processor chain and add the mock processor to it
            List<IEnvelopeProcessor> processorChain = new List<IEnvelopeProcessor>();
            processorChain.Add(procMock.Object);

            // the continuation that, if called, fails the test
            Action continuation = new Action( () =>
                Assert.Fail("The continuation action should not have been called"));

            // this is what we're testing (extended class gives public access to protected method)
            ExtendedDefaultEnvelopeBus bus = new ExtendedDefaultEnvelopeBus(txMock.Object);
            bus.PublicProcessEnvelope(ctx, processorChain, continuation);

            // make sure that the processor was called once
            procMock.Verify(proc => proc.ProcessEnvelope(ctx, It.IsAny<Action>()), Times.Once());

            // and that since it did nothing, the transport provider didn't get the envelope
            txMock.Verify(tx => tx.Send(env), Times.Never());
        }

        [Test]
        public void Should_Sort_Processor_Chains_Even_When_Added_Out_Of_Order()
        {
            var unsortedChain = new Dictionary<int, IEnvelopeProcessor>();


            // mock a few processors
            var procMock1 = new Mock<IEnvelopeProcessor>();
            var procMock2 = new Mock<IEnvelopeProcessor>();
            var procMock3 = new Mock<IEnvelopeProcessor>();

            // add them out of order
            unsortedChain.Add(2, procMock2.Object);
            unsortedChain.Add(3, procMock3.Object);
            unsortedChain.Add(1, procMock1.Object);

            IEnumerable<IEnvelopeProcessor> sortedChain = unsortedChain.Sort();

            Assert.AreSame(procMock1.Object, sortedChain.ElementAt(0));
            Assert.AreSame(procMock2.Object, sortedChain.ElementAt(1));
            Assert.AreSame(procMock3.Object, sortedChain.ElementAt(2));
        }
    }





    public class ExtendedDefaultEnvelopeBus : DefaultEnvelopeBus
    {
        public ExtendedDefaultEnvelopeBus(ITransportProvider transportProvider)
            : base(transportProvider)
        {
        }


        public void PublicProcessEnvelope(EnvelopeContext context, IEnumerable<IEnvelopeProcessor> processorChain, Action processingComplete)
        {
            this.ProcessEnvelope(context, processorChain, processingComplete);
        }
    }
}
