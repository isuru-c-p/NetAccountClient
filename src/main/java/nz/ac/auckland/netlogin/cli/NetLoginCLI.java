package nz.ac.auckland.netlogin.cli;

import nz.ac.auckland.netlogin.NetLoginConnection;
import nz.ac.auckland.netlogin.PingListener;
import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import nz.ac.auckland.netlogin.negotiation.PopulatedCredentialsCallback;

public class NetLoginCLI implements PingListener {
		
	private String upi;
	private NetLoginConnection netLoginConnection;
	private boolean displayStatus = false;

	public NetLoginCLI(String upi, String password) {
		this.upi = upi;

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
			netLoginConnection.login(NetLoginConnection.AUTHD_SERVER, callback);
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	public void connected(int ipUsage, int planFlags) {
		System.out.println("UPI:" + upi + " Status:Connected");
		update(ipUsage, planFlags, null);
	}

	public void disconnected() {
			System.out.println("Disconnected");
			displayStatus = false;
			System.exit(0);
	}

	public void update(int ipUsage, int planFlags, String message) {
		float MBs_usage = (float) (Math.round((ipUsage / 1024.0) * 100)) / 100;
		planFlags = planFlags & 0x0F000000;

		String plan_name;
		switch (planFlags) {
		case 0x01000000: // STATUS_UNLIMITED:
			plan_name = "Unlimited";
			break;
		case 0x02000000: // STATUS_SPONSORED:
			plan_name = "Sponsored";
			break;
		case 0x03000000: // STATUS_PREMIUM:
			plan_name = "Premium";
			break;
		case 0x04000000: // STATUS_STANDARD:
			plan_name = "Standard";
			break;
		case 0x05000000: // STATUS_NOACCESS:
			plan_name = "No Access";
			break;
		default:
			plan_name = "";
		}

		if (this.displayStatus) {
			System.out.print("..");
		} else {
			System.out.println("Status:Connected");
			System.out.println("UPI:" + upi);
			System.out.println("Internet Plan:" + plan_name + "\nMBs used this month:" + MBs_usage + "MBs");
			System.out.print("Pingd is active:.");
			this.displayStatus = true;
		}
	}
}
