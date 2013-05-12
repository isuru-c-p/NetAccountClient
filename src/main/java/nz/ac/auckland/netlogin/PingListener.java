package nz.ac.auckland.netlogin;

public interface PingListener {

	public void update(int ipUsage, NetLoginPlan plan);

	public void disconnected();

}
