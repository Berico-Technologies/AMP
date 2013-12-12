package amp.rabbit;

import amp.bus.IEnvelopeReceivedCallback;
import cmf.bus.IDisposable;

/**
 * Defines the interface of a component that is able to listen for messages on a RabbitMQ queue.
 */
public interface IRabbitListener extends IDisposable {

    /**
     * Adds an onClose callback to be raised when this listener closes.
     * @param callback listener
     */
    public void onClose(IListenerCloseCallback callback);

    /**
     * Adds an onEnvelopeReceived callback to be raised when a message is received.
     * @param callback listener
     */
    public void onEnvelopeReceived(IEnvelopeReceivedCallback callback);

    /**
     * Adds an onConnection error callback to be raised if this listener fails to listen for messages.
     * @param callback listener
     */
    public void onConnectionError(IOnConnectionErrorCallback callback);

    /**
     * Adds an onStarted callback to be raised when this listener has begun listening for messages.
     */
    public void onStarted(IListenerStartedCallback callback);


    /**
     * Begins listening for messages
     */
    public void start();


    /**
     * Stops listening for messages
     */
    public void stop();
}
