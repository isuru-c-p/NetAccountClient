package nz.ac.auckland.netlogin.negotiation;

public interface CredentialsCallback {

	boolean requestCredentials();

	String getUsername();

	/**
	 * Get the password, clears the password after returning it.
	 * @return the password
	 */
	String retrievePassword();

}
