using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using Common.Logging;

using cmf.bus;
using cmf.eventing;

using amp.bus;


namespace amp.eventing
{
    public class DefaultEventBus : IEventBus
    {
        protected IEnvelopeBus _envBus;
        protected ILog _log;


        public IDictionary<int, IEventProcessor> InboundChain { get; set; }
        public IDictionary<int, IEventProcessor> OutboundChain { get; set; }


        public DefaultEventBus(IEnvelopeBus envBus)
        {
            _envBus = envBus;
            _log = LogManager.GetLogger(this.GetType());
        }


        public virtual void Publish(object ev)
        {
            _log.Debug("Enter Publish");

            if (null == ev) { throw new ArgumentNullException("ev", "Cannot publish a null event"); }

            try
            {
                // create an envelope and set its pattern to pub/sub
                var env = new Envelope();
                env.SetMessagePattern(EnvelopeHeaderConstants.MESSAGE_PATTERN_PUBSUB);

                // create a processing context
                var ctx = new EventContext(EventContext.Directions.Out, env, ev);

                this.ProcessEvent(ctx, this.OutboundChain.Sort(), () =>
                {
                    // don't send empty envelopes!
                    if (null == ctx.Envelope.Payload) { throw new EventException("The event resulted in an empty payload!"); }

                    _envBus.Send(ctx.Envelope);
                });
            }
            catch (Exception ex)
            {
                _log.Error("Exception publishing an event", ex);
                throw;
            }

            _log.Debug("Leave Publish");
        }

        public virtual void Subscribe(IEventHandler handler)
        {
            _log.Debug("Enter Subscribe");

            EventRegistration registration = new EventRegistration(handler, this.InterceptEvent);
            _envBus.Register(registration);

            _log.Debug("Leave Subscribe");
        }

        public virtual void Subscribe<TEvent>(Action<TEvent, IDictionary<string, string>> handler) where TEvent : class
        {
            this.Subscribe(new TypedEventHandler<TEvent>(handler));
        }

        public virtual object InterceptEvent(IEventHandler handler, Envelope env)
        {
            _log.Debug("Enter InterceptEvent");

            object result = null;
            var context = new EventContext(EventContext.Directions.In, env);

            this.ProcessEvent(context, this.InboundChain.Sort(), () =>
            {
                _log.Info("Completed inbound processing - invoking IEventHandler");

                try
                {
                    result = handler.Handle(context.Event, env.Headers);
                    _log.Debug("IEventHandler returned without exception");
                }
                catch (Exception ex)
                {
                    _log.Warn("Exception invoking IEventHandler", ex);
                    result = handler.HandleFailed(env, ex);
                }
            });

            _log.Debug("Leave InterceptEvent");
            return result;
        }

        public virtual void Dispose()
        {
            _log.Info("The event bus client will now be disposed");
            _envBus.Dispose();
        }


        // recursive function that processes envelopes
        public virtual void ProcessEvent(
            EventContext context,
            IEnumerable<IEventProcessor> processorChain,
            Action processingComplete)
        {
            // if the chain is null or empty, complete processing
            if ((null == processorChain) || (!processorChain.Any()))
            {
                processingComplete();
                return;
            }

            // grab the next procesor
            IEventProcessor nextProcessor = processorChain.First();

            // let it process the envelope and pass its "next" processor: a
            // method that recursively calls this function with the current
            // processor removed
            nextProcessor.ProcessEvent(context, () =>
            {
                this.ProcessEvent(context, processorChain.Skip(1), processingComplete);
            });
        }
    }




    public static class ChainExtensions
    {
        public static IEnumerable<IEventProcessor> Sort(this IDictionary<int, IEventProcessor> chain)
        {
            IEnumerable<IEventProcessor> sortedChain = new List<IEventProcessor>();

            if (null != chain)
            {
                sortedChain = chain
                    .OrderBy(kvp => kvp.Key)
                    .Select<KeyValuePair<int, IEventProcessor>, IEventProcessor>(kvp => kvp.Value);
            }

            return sortedChain;
        }
    }
}
