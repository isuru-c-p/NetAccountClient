package nz.ac.auckland.netlogin;

public interface PingListener {

	public void connected(String username, int ipUsage, int userPlanFlags);

	public void update(int ipUsage, int userPlanFlags, String message);

	public void disconnected();

}
