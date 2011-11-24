package nz.ac.auckland.netlogin;

public class ConnectionWatcher implements Runnable {

	private final NetLoginConnection connection;
	private NetLoginPreferences preferences;
	private boolean reconnect = true;

	public ConnectionWatcher(NetLoginConnection connection) {
		this.connection = connection;
		this.preferences = NetLoginPreferences.getInstance();
		new Thread(this, "Automatic Reconnect").start();
	}

	@SuppressWarnings({"InfiniteLoopStatement"})
	public void run() {
		while (true) {
			synchronized (connection) {
				if (reconnect && preferences.getReconnect() && connection.getState() == ConnectionState.DISCONNECTED) {
					connection.automaticLogin();
				}
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// ignore and continue
			}
		}
	}

	public void monitor() {
		reconnect = true;
	}

	public void unmonitor() {
		reconnect = false;
	}

}
