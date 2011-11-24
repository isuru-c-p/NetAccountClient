package nz.ac.auckland.netlogin;

public interface PingListener {

	public void connecting();

	public void connectionFailed(String message);

	public void connected(String username, int ipUsage, NetLoginPlan plan);

	public void update(int ipUsage, NetLoginPlan plan);

	public void disconnected();

}
