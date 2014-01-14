package amp.bus;


import cmf.bus.Envelope;
import cmf.bus.IDisposable;
import cmf.bus.IRegistration;

public interface ITransportProvider extends IDisposable {

    void onEnvelopeReceived(IEnvelopeReceivedCallback callback);

    void register(IRegistration registration) throws Exception;

    void send(Envelope envelope) throws Exception;

    void unregister(IRegistration registration) throws Exception;
}
