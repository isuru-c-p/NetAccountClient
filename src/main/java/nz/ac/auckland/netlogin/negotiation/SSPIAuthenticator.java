package nz.ac.auckland.netlogin.negotiation;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.platform.win32.Secur32;
import com.sun.jna.platform.win32.Sspi;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.ptr.NativeLongByReference;
import javax.security.auth.login.LoginException;
import java.io.IOException;

/**
 * SSPI is a Microsoft protocol for authenticating between client and server.
 * It is <a href="http://msdn.microsoft.com/en-us/library/aa380496(VS.85).aspx">interoperable with GSSAPI, subject to conditions</a>.
 */
public class SSPIAuthenticator implements Authenticator {

	private String authenticationMechanism = "Kerberos";

    public SSPIAuthenticator() throws ClassNotFoundException {
        // attempt to load Secur32 to see if the native libs are available
        Class.forName("com.sun.jna.platform.win32.Secur32", true, SSPIAuthenticator.class.getClassLoader());
    }

    public String getName() {
        return "SSPI";
    }
	
	// http://code.dblock.org/jna-acquirecredentialshandle-initializesecuritycontext-and-acceptsecuritycontext-establishing-an-authenticated-connection
	// obtaining a spn - http://msdn.microsoft.com/en-us/library/ff649429.aspx
	public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
		String targetPrincipal = GSSAPIAuthenticator.getServicePrincipalName();
		byte[] outToken = createToken(targetPrincipal);
		return new AuthenticationRequest(targetPrincipal, outToken);
	}

	public LoginComplete validateResponse(byte[] serverResponse) throws LoginException, IOException {
		throw new LoginException("Phase two not implemented");
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Please supply the principal to connect to, for example netlogin/gate.ec.auckland.ac.nz@AD.EC.AUCKLAND.AC.NZ");
			System.exit(0);
		}
	}

	public byte[] createToken(String targetPrincipal) throws LoginException {
		Sspi.CredHandle phClientCredential = null;
		Sspi.CtxtHandle phClientContext = null;

		try {
			// client ----------- acquire outbound credential handle
			phClientCredential = new Sspi.CredHandle();
			Sspi.TimeStamp ptsClientExpiry = new Sspi.TimeStamp();

			int responseCode = Secur32.INSTANCE.AcquireCredentialsHandle(
					null, authenticationMechanism, new NativeLong(Sspi.SECPKG_CRED_OUTBOUND), null, null,
					null, null, phClientCredential, ptsClientExpiry);

			if (responseCode != W32Errors.SEC_E_OK) {
				throw new RuntimeException("Unable to get credentials handle");
			}

			// client ----------- security context
			phClientContext = new Sspi.CtxtHandle();
			NativeLongByReference pfClientContextAttr = new NativeLongByReference();

			// server ----------- security context
			Sspi.SecBufferDesc pbServerToken = new Sspi.SecBufferDesc(Sspi.SECBUFFER_TOKEN, Sspi.MAX_TOKEN_SIZE);

			// client ----------- initialize security context, produce a client token
			// client token returned is always new
			Sspi.SecBufferDesc pbClientToken = new Sspi.SecBufferDesc(Sspi.SECBUFFER_TOKEN, Sspi.MAX_TOKEN_SIZE);

			// server token is empty the first time
			int clientRc = Secur32.INSTANCE.InitializeSecurityContext(
					phClientCredential,
					phClientContext.isNull() ? null : phClientContext,
					targetPrincipal,
					new NativeLong(Sspi.ISC_REQ_CONNECTION | Sspi.ISC_REQ_CONFIDENTIALITY | Sspi.ISC_REQ_INTEGRITY | Sspi.ISC_REQ_REPLAY_DETECT),
					new NativeLong(0),
					new NativeLong(Sspi.SECURITY_NATIVE_DREP),
					pbServerToken,
					new NativeLong(0),
					phClientContext,
					pbClientToken,
					pfClientContextAttr,
					null);

			if (clientRc != W32Errors.SEC_E_OK && clientRc != W32Errors.SEC_I_CONTINUE_NEEDED) {
				handleError(clientRc);
			}

			return pbClientToken.getBytes();

		} finally {
			// release client context
			if (phClientContext != null) Secur32.INSTANCE.DeleteSecurityContext(phClientContext);
			if (phClientCredential != null) Secur32.INSTANCE.FreeCredentialsHandle(phClientCredential);
		}
	}

	private void handleError(int clientRc) throws LoginException {
		Win32Exception exception = new Win32Exception(clientRc);
		System.err.printf("SSPI: %1$#x: %2$s\n", exception.getHR().intValue(), exception.getMessage().trim());
		
//		// check for security errors
//		switch(exception.getHR().intValue()) {
//			case W32Errors.SEC_E_NO_CREDENTIALS: //  No credentials are available in the security package.
//			case W32Errors.SEC_E_INVALID_HANDLE: // The handle passed to the function is invalid.
//			case W32Errors.SEC_E_TARGET_UNKNOWN: // The target was not recognized.
//			case W32Errors.SEC_E_LOGON_DENIED: // The logon failed.
//			case W32Errors.SEC_E_INTERNAL_ERROR: // The Local Security Authority cannot be contacted.
//			case W32Errors.SEC_E_NO_AUTHENTICATING_AUTHORITY: // No authority could be contacted for authentication.
//		}
//
//		// check for generic errors
//		switch(exception.getHR().intValue() & 0xFFFF) {
//			case W32Errors.ERROR_ACCESS_DISABLED_NO_SAFER_UI_BY_POLICY: // Access to %1 has been restricted by your Administrator by policy rule %2.
//		}

		throw new LoginException(exception.getMessage());
	}

}