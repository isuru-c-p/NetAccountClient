public interface PingListener {

	public void connected(int ipUsage, int userPlanFlags);

	public void update(int ipUsage, int userPlanFlags, String message);

	public void disconnected();

}
