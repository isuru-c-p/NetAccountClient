package nz.ac.auckland.netlogin;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import nz.ac.auckland.netlogin.cli.NetLoginCLI;
import nz.ac.auckland.netlogin.gui.NetLoginGUI;
import java.io.IOException;
import java.util.Arrays;

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
			
				// CMD Line with upi and retrievePassword
				if (options.has("u")) {
					String password = null;
					String upi= options.valueOf("u").toString();
					
					if (upi == null || upi.length() == 0) {
						System.err.println("Please type your upi with option -u");
						System.exit(0);
					}
					
					if (options.has("p")) {
						try {
							password = options.valueOf("p").toString();
							if (password != null && password.length() == 0) password = null;
						} catch (Exception e) {
							password = null;
						}
					}
										
					if (options.has("g")) {
						// still use GUI interface
						new NetLoginGUI(upi, password);
					} else {
						// pure command line with console
						new NetLoginCLI(upi, password);
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
