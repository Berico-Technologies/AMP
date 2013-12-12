package amp.rabbit;


public interface IOnConnectionErrorCallback {

	void onConnectionError(RabbitListener listener);
}