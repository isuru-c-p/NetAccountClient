package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.netlogin.LoginCancelled;

public interface CredentialsCallback {

	boolean requestCredentials() throws LoginCancelled;

	String getUsername();

	/**
	 * Get the password, clears the password after returning it.
	 * @return the password
	 */
	String retrievePassword();

}
