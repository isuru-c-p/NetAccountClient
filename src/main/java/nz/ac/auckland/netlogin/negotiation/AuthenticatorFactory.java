package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.netlogin.NetLoginPreferences;
import java.util.ArrayList;
import java.util.List;

public class AuthenticatorFactory {

	private static AuthenticatorFactory instance;
	private List<Authenticator> authenticators;

	public static AuthenticatorFactory getInstance() {
		if (instance == null) instance = new AuthenticatorFactory();
		return instance;
	}

	public AuthenticatorFactory() {
		authenticators = new ArrayList<Authenticator>();
		loadAuthenticator("nz.ac.auckland.netlogin.negotiation.DefaultAuthenticator");
		loadAuthenticator("nz.ac.auckland.netlogin.negotiation.password.PasswordAuthenticator");
		loadAuthenticator("nz.ac.auckland.netlogin.negotiation.sspi.SSPINegotiation");
	}

	public void loadAuthenticator(String className) {
		try {
			Class clazz = Class.forName(className);
			if (!Authenticator.class.isAssignableFrom(clazz)) {
				System.err.printf("Unable to load %s, not an authenticator", className);
				return;
			}

			Authenticator authenticator = (Authenticator)clazz.newInstance();
			authenticators.add(authenticator);
		} catch (ClassNotFoundException e) {
			System.err.printf("Authenticator not found %s\n", className);
		} catch (Exception e) {
			System.err.printf("Unable to load authenticator %s: %s\n", className, e.getMessage());
		}
	}

	public Authenticator getDefaultAuthenticator() {
		if (authenticators.isEmpty()) {
			throw new RuntimeException("No authenticators are available");
		}

		NetLoginPreferences preferences = NetLoginPreferences.getInstance();
		String credentialSource = preferences.getCredentialSource();

		// look for the specific item
		if (credentialSource != null) {
			for(Authenticator authenticator : authenticators) {
				if (authenticator.getName().equals(credentialSource)) return authenticator;
			}
		}

		// fall back to the first item
		return authenticators.get(0);
	}

	public List<Authenticator> getAuthenticators() {
		return authenticators;
	}

}
