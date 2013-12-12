using System;
using cmf.bus;


namespace amp.bus
{
    public interface ITransportProvider : IDisposable
    {

        //TODO: Why does ITransportProvider define an OnEnvelopeReceived and IEnvelopeReceiver does not?
        event Action<IEnvelopeDispatcher> OnEnvelopeReceived;


        void Send(Envelope env);

        void Register(IRegistration registration);

        void Unregister(IRegistration registration);
    }
}
