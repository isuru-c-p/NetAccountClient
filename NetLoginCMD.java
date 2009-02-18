import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;

public class NetLoginCMD {
	    
	    String upi;
	    String password;
	    private NetLoginConnection netLoginConnection = null;
		private boolean displayStatus = false;
		
	    
		public NetLoginCMD(String upi, String password) {
			this.upi=upi;
			this.password=password;
			try{
				netLoginConnection = new NetLoginConnection(this);
				netLoginConnection.logincmdline(upi, password);
			}catch(Exception e)
			{
				System.out.println(e);
			}
		}
		
		public void update(int balance, boolean onPeak, boolean connected) {
			if (connected) {
				System.out.println("Upi:"+this.upi+" Status:Connected");
			} else {
				//disconnect(); // to make sure
				System.out.println("Disconnected");
				displayStatus = false;
				System.exit(0);
			}
		}

		/* new update menthod for client version >=3 netlogin */
		public void updateV3(int ip_usage, int user_plan_flags, boolean connected,String message) {
			String plan_name = "";
			if (connected) {
				float MBs_usage = (float) (Math.round((ip_usage / 1024.0) * 100)) / 100;
				user_plan_flags = user_plan_flags & 0x0F000000;
				switch (user_plan_flags) {
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
				if (this.displayStatus)
					System.out.print("..");
				else
				{
					System.out.println("Status:Connected");
					System.out.println("Upi:"+this.upi);
					System.out.println("Internet Plan:"+plan_name+"\nMBs used this month:"+MBs_usage+"MBs");
					System.out.print("Pingd is active:.");
					this.displayStatus=true;
				}
			} else {
			//	disconnect(); // to make sure
				System.out.println("Disconnected");
				displayStatus = false;
				System.exit(0);
			}
		}
}
