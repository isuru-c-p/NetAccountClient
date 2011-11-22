package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.netlogin.NetLoginPreferences;

import java.util.*;

public class AuthenticatorFactory {

	private static AuthenticatorFactory instance;
	private Map<String, Authenticator> authenticators;

	public static AuthenticatorFactory getInstance() {
		if (instance == null) instance = new AuthenticatorFactory();
		return instance;
	}

	public AuthenticatorFactory() {
		authenticators = new LinkedHashMap<String, Authenticator>();
		loadAuthenticator("nz.ac.auckland.netlogin.negotiation.DefaultAuthenticator");
		loadAuthenticator("nz.ac.auckland.netlogin.negotiation.SSPIAuthenticator");
        loadAuthenticator("nz.ac.auckland.netlogin.negotiation.GSSAPIAuthenticator");
		loadAuthenticator("nz.ac.auckland.netlogin.negotiation.PasswordAuthenticator");
	}

    public Collection<String> getNames() {
        return authenticators.keySet();
    }

	public void loadAuthenticator(String className) {
		try {
			Class clazz = Class.forName(className);
			if (!Authenticator.class.isAssignableFrom(clazz)) {
				System.err.printf("Unable to load %s, not an authenticator\n", className);
				return;
			}

			Authenticator authenticator = (Authenticator)clazz.newInstance();
			authenticators.put(authenticator.getName(), authenticator);
		} catch (ClassNotFoundException e) {
			System.err.printf("Authenticator not found %s\n", className);
        } catch (UnsatisfiedLinkError e) {
            System.err.printf("Authenticator not available %s\n", className);
		} catch (Exception e) {
			System.err.printf("Unable to load authenticator %s: %s\n", className, e.getMessage());
		}
	}

	public Authenticator getSelectedAuthenticator() {
		if (authenticators.isEmpty()) {
			throw new RuntimeException("No authenticators are available");
		}

		NetLoginPreferences preferences = NetLoginPreferences.getInstance();
		String credentialSource = preferences.getCredentialSource();

		// look for the specific item
		if (credentialSource != null) {
            Authenticator authenticator = authenticators.get(credentialSource);
            if (authenticator != null) return authenticator;
		}

		// fall back to the first item
		return authenticators.values().iterator().next();
	}

	public Collection<Authenticator> getAuthenticators() {
		return authenticators.values();
	}

}
