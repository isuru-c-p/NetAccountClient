package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.cs.des.C_Block;
import javax.security.auth.login.LoginException;
import java.io.IOException;

public interface Authenticator {

	public String getName();

	public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException;

	public LoginComplete validateResponse(byte[] serverResponse) throws LoginException, IOException;

	public static class AuthenticationRequest {

		private String username;
		private byte[] payload;

		public AuthenticationRequest(String username, byte[] payload) {
			this.username = username;
			this.payload = payload;
		}

		public String getUsername() {
			return username;
		}

		public byte[] getPayload() {
			return payload;
		}

	}

	public static class LoginComplete {

		private int clientNonce;
		private int serverNonce;
		private C_Block sessionKey;

		public LoginComplete(int clientNonce, int serverNonce, C_Block sessionKey) {
			this.clientNonce = clientNonce;
			this.serverNonce = serverNonce;
			this.sessionKey = sessionKey;
		}

		public int getClientNonce() {
			return clientNonce;
		}

		public int getServerNonce() {
			return serverNonce;
		}

		public C_Block getSessionKey() {
			return sessionKey;
		}
		
	}

}