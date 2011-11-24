package nz.ac.auckland.netlogin;

public interface PingListener {

	public void connecting();

	public void connectionFailed();

	public void connected(String username, int ipUsage, NetLoginPlan plan);

	public void update(int ipUsage, NetLoginPlan plan, String message);

	public void disconnected();

}
