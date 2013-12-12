package amp.rabbit.dispatch;

/**
 * Defines a callback method that handles the start of a IRabbitListener
 */
public interface IListenerStartedCallback {

    /**
     * Raised when the IRabbitListener is started.
     */
    public void onStart();
}
