package nz.ac.auckland.netlogin.negotiation;

import javax.security.auth.login.CredentialNotFoundException;
import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.util.List;

public class DefaultAuthenticator implements Authenticator {

	private Authenticator delegate;

	public String getName() {
		return "Default";
	}

	public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
		List<Authenticator> authenticators = AuthenticatorFactory.getInstance().getAuthenticators();
		for(Authenticator delegate : authenticators) {
			if (delegate == this) continue;

			this.delegate = delegate;
			try {
				return delegate.startAuthentication(callback);
			} catch (CredentialNotFoundException e) {
				// user elected not to supply credentials, stop processing
				throw e;
			} catch (LoginException e) {
				// try the next one!
			} catch (IOException e) {
				// try the next one!
			}
		}
		throw new LoginException("No credentials source is available");
	}

	public LoginComplete validateResponse(byte[] serverResponse) throws LoginException, IOException {
		return delegate.validateResponse(serverResponse);
	}

}
