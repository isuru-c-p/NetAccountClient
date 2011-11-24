package nz.ac.auckland.netlogin.negotiation;

public class NoCredentialsCallback implements CredentialsCallback {

	public NoCredentialsCallback() {
	}

	public boolean requestCredentials() {
		return false;
	}

	public String getUsername() {
		throw new IllegalStateException("No credentials are available");
	}

	public String retrievePassword() {
		throw new IllegalStateException("No credentials are available");
	}

}
