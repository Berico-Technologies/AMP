using System;
using amp.bus;
using Common.Logging;
using RabbitMQ.Client;

using cmf.bus;

namespace amp.rabbit
{
    public class RabbitEnvelopeDispatcher : IEnvelopeDispatcher
    {
        protected IRegistration _registration;
        protected IModel _channel;
        protected ulong _deliveryTag;
        protected ILog _log;


        public Envelope Envelope
        {
            get;
            protected set;
        }


        public RabbitEnvelopeDispatcher(IRegistration registration, Envelope envelope, IModel channel, ulong deliveryTag)
        {
            _registration = registration;
            _channel = channel;
            _deliveryTag = deliveryTag;

            this.Envelope = envelope;

            _log = LogManager.GetLogger(this.GetType());
        }


        public virtual void Dispatch()
        {
            _log.Debug("Enter Dispatch()");
            this.Dispatch(this.Envelope);
            _log.Debug("Leave Dispatch()");
        }

        public virtual void Dispatch(Envelope env)
        {
            _log.Debug("Enter Dispatch(env)");
            object maybeNull = null;

            try
            {
                // this may be null, or it may be any other kind of object
                maybeNull = _registration.Handle(env);
                _log.Debug("Dispatched envelope to registration");
            }
            catch (Exception ex)
            {
                try { _registration.HandleFailed(env, ex); }
                catch { }
            }

            this.RespondToMessage(maybeNull);
            _log.Debug("Leave Dispatch(env)");
        }

        public virtual void Fail(Exception ex)
        {
            _log.Debug("Enter Fail");

            object maybeNull = null;

            try { maybeNull = _registration.HandleFailed(this.Envelope, ex); }
            catch { }

            this.RespondToMessage(maybeNull);

            _log.Debug("Leave Fail");
        }


        protected virtual void RespondToMessage(object maybeNull)
        {
            _log.Debug("Enter RespondToMessage");

            // we accept an envelope instead of dispatching the envelope in our
            // state because whoever is consuming us may have mutated it
            DeliveryOutcomes result = DeliveryOutcomes.Null;

            // by convention, if handlers return nothing, assume acknowledgement
            // if the object is not null and a DeliveryOutcome, cast it
            // else, assume acknowledgement
            if (null == maybeNull) { result = DeliveryOutcomes.Acknowledge; }
            else if (maybeNull is DeliveryOutcomes) { result = (DeliveryOutcomes)maybeNull; }
            else { result = DeliveryOutcomes.Acknowledge; }

            _log.Info("DeliveryOutcome of handled event is: " + result);
            switch (result)
            {
                case DeliveryOutcomes.Acknowledge:
                    _channel.BasicAck(_deliveryTag, false);
                    break;
                case DeliveryOutcomes.Null:
                    _channel.BasicAck(_deliveryTag, false);
                    break;
                case DeliveryOutcomes.Reject:
                    _channel.BasicReject(_deliveryTag, false);
                    break;
                case DeliveryOutcomes.Exception:
                    _channel.BasicNack(_deliveryTag, false, false);
                    break;
            }

            _log.Debug("Leave RespondToMessage");
        }
    }
}
