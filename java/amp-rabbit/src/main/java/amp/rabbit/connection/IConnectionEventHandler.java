package amp.rabbit.connection;

public interface IConnectionEventHandler {

	void onConnectionClosed(boolean willAttemptToReconnect);

	void onConnectionReconnected();
}
