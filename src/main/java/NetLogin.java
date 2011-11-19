import java.io.*;
import java.util.Arrays;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/*
 * NetLogin
 * @author Mikey - 2001
 * @author Jason Liu - 2009
 * @author Robert Egglestone - 2011
 */
public class NetLogin {
	
	public static void main(String[] args) {
		
		if (args.length==0) {
			// no command line startup
			new NetLoginGUI();
		} else {
			// command line startup
			OptionParser parser = new OptionParser() {
				{
					accepts("u").withRequiredArg().ofType(String.class).describedAs("upi");
					accepts("p").withOptionalArg().ofType(String.class).describedAs("password");
					acceptsAll(Arrays.asList("g", "GUI", "gui"), "command line for upi and password input, but still display GUI interface");
					acceptsAll(Arrays.asList("h", "?"), "show help");
				}
			};
			try {
				OptionSet options = parser.parse(args);

				// HELP print
				if (options.has("?")) {
					parser.printHelpOn( System.err );
					System.exit(0);
				}
			
				// CMD Line with upi and password
				if (options.has("u")) {
					String password = null;
					String upi= options.valueOf("u").toString();
					
					if (upi == null || upi.length() == 0) {
						System.err.println("Please type your upi with option -u");
						System.exit(0);
					}
					
					if (options.has("p")) {
						try {
							password= options.valueOf("p").toString();
						} catch(Exception e) {
							password=null;
						}
					}
					
					if (password == null || password.length() == 0) {
						try {
							password = new String(ConsolePasswordField.getPassword(System.in, "Enter password: "));
						} catch(IOException ioe) {
							ioe.printStackTrace();
						}
					}
					
					if(password == null) {
						 System.err.println("No password entered");
						 System.exit(0);
					}
					
					if (options.has("g")) {
						// still use GUI interface
						new NetLoginGUI(upi,password);
					} else {
						// pure command line with console
						new NetLoginCMD(upi,password);
					}
				}
				
			} catch(Exception e) {
				System.err.println(e.getMessage());
				try {
					parser.printHelpOn(System.err);
				} catch (IOException e2) {
					System.err.println("Unable to display help: " + e2.getMessage());
				}
				System.exit(0);
			}
			
		}
	}
}
