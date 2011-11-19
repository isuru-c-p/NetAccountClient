public interface PingListener {

	public void update(int balance, boolean onPeak, boolean connected);

	public void updateV3(int ip_usage, int user_plan_flags, boolean connected,String message);

}
