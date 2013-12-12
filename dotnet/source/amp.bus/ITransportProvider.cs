using System;
using cmf.bus;


namespace amp.bus
{
    public interface ITransportProvider : IDisposable
    {
        event Action<IEnvelopeDispatcher> OnEnvelopeReceived;


        void Send(Envelope env);

        void Register(IRegistration registration);

        void Unregister(IRegistration registration);
    }
}
