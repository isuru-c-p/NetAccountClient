package nz.ac.auckland.netlogin.cli;

import nz.ac.auckland.netlogin.NetLoginConnection;
import nz.ac.auckland.netlogin.NetLoginPlan;
import nz.ac.auckland.netlogin.PingListener;
import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import nz.ac.auckland.netlogin.negotiation.PopulatedCredentialsCallback;

public class NetLoginCLI implements PingListener {
	
	private NetLoginConnection netLoginConnection;
	private boolean displayStatus = false;

	public NetLoginCLI(String upi, String password) {
		CredentialsCallback callback;
		if (password == null) {
			callback = new ConsolePasswordField(upi);
		} else {
			callback = new PopulatedCredentialsCallback(upi, password);
		}
		
		authenticate(callback);
	}

	public void authenticate(CredentialsCallback callback) {
		try {
			netLoginConnection = new NetLoginConnection(this);
			netLoginConnection.login(callback);
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void connecting() {
	}

	public void connectionFailed(String message) {
		if (message != null) System.out.println(message);
	}

	public void connected(String username, int ipUsage, NetLoginPlan plan) {
		System.out.println("Status:Connected");
		System.out.println("UPI:" + username);

		update(ipUsage, plan);
	}

	public void disconnected() {
			System.out.println("Disconnected");
			displayStatus = false;
			System.exit(0);
	}

	public void update(int ipUsage, NetLoginPlan plan) {
		float MBs_usage = (float) (Math.round((ipUsage / 1024.0) * 100)) / 100;
        
		if (this.displayStatus) {
			System.out.print("..");
		} else {
			System.out.println("Status:Connected");
			System.out.println("Internet Plan:" + plan.toString() + "\nMBs used this month:" + MBs_usage + "MBs");
			System.out.print("Pingd is active:.");
			this.displayStatus = true;
		}
	}
}
