package nz.ac.auckland.netlogin.cli;

import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import java.io.*;
import java.util.*;

/**
 * This class prompts the user for a password and attempts to mask input with "*"
 */
public class ConsolePasswordField implements CredentialsCallback {

	private String username;
	private String password;

	public ConsolePasswordField(String username) {
		this.username = username;
	}

	public boolean requestCredentials() {
		try {
			char[] passwordChars = requestPassword(System.in, "Enter password: ");
			this.password = new String(passwordChars);
			return !password.isEmpty();
		} catch (IOException e) {
			return false;
		}
	}

	public String getUsername() {
		return username;
	}

	public String retrievePassword() {
		String password = this.password;
		this.password = null;
		return password;
	}

  /**
   *@param in stream to be used (e.g. System.in)
   *@param prompt The prompt to display to the user.
   *@return The retrievePassword as entered by the user.
   */
   public char[] requestPassword(InputStream in, String prompt) throws IOException {
      MaskingThread maskingthread = new MaskingThread(prompt);
      Thread thread = new Thread(maskingthread);
      thread.start();
	
      char[] lineBuffer;
      char[] buf;


      buf = lineBuffer = new char[128];

      int room = buf.length;
      int offset = 0;
      int c;

      loop:   while (true) {
         switch (c = in.read()) {
            case -1:
            case '\n':
               break loop;

            case '\r':
               int c2 = in.read();
               if ((c2 != '\n') && (c2 != -1)) {
                  if (!(in instanceof PushbackInputStream)) {
                     in = new PushbackInputStream(in);
                  }
                  ((PushbackInputStream)in).unread(c2);
                } else {
                  break loop;
                }

                default:
                   if (--room < 0) {
                      buf = new char[offset + 128];
                      room = buf.length - offset - 1;
                      System.arraycopy(lineBuffer, 0, buf, 0, offset);
                      Arrays.fill(lineBuffer, ' ');
                      lineBuffer = buf;
                   }
                   buf[offset++] = (char) c;
                   break;
         }
      }
      maskingthread.stopMasking();
      if (offset == 0) {
         return null;
      }
      char[] ret = new char[offset];
      System.arraycopy(buf, 0, ret, 0, offset);
      Arrays.fill(buf, ' ');
      return ret;
   }

}

/**
 * This class attempts to erase characters echoed to the console.
 */
class MaskingThread extends Thread {
   private volatile boolean stop;
   private char echochar = '*';

  /**
   *@param prompt The prompt displayed to the user
   */
   public MaskingThread(String prompt) {
      System.out.print(prompt);
   }

  /**
   * Begin masking until asked to stop.
   */
   public void run() {

      int priority = Thread.currentThread().getPriority();
      Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

      try {
         stop = true;
         while(stop) {
           System.out.print("\010" + echochar);
           try {
              // attempt masking at this rate
              Thread.currentThread().sleep(1);
           }catch (InterruptedException iex) {
              Thread.currentThread().interrupt();
              return;
           }
         }
      } finally { // restore the original priority
         Thread.currentThread().setPriority(priority);
      }
   }

  /**
   * Instruct the thread to stop masking.
   */
   public void stopMasking() {
      this.stop = false;
   }
}
