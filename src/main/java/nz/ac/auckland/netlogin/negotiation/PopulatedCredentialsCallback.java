package nz.ac.auckland.netlogin.negotiation;

public class PopulatedCredentialsCallback implements CredentialsCallback {

	private String username;
	private String password;

	public PopulatedCredentialsCallback(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public boolean requestCredentials() {
		return true;
	}

	public String getUsername() {
		return username;
	}

	public String retrievePassword() {
		String password = this.password;
		this.password = null;
		return password;
	}

}
