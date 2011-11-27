package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.cs.des.C_Block;
import nz.ac.auckland.netlogin.NetLoginPreferences;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public abstract class AbstractGSSAuthenticator implements Authenticator {

    protected abstract String getUserName() throws LoginException;

    protected abstract void initializeContext() throws LoginException;

    protected abstract byte[] initSecContext(byte[] gssToken) throws LoginException;

    protected abstract byte[] unwrap(byte[] wrapper) throws LoginException;

    public static String getServicePrincipalName() {
        String serverName = NetLoginPreferences.getInstance().getServer();
        String realmName = NetLoginPreferences.getInstance().getRealm();
        return "netlogin/" + serverName + "@" + realmName;
    }

    public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
        cleanup();

        initializeContext();
        byte[] outToken = initSecContext(null);
        if (outToken == null) throw new LoginException("No authentication token produced");

        ByteArrayOutputStream outStream = new ByteArrayOutputStream(outToken.length + 4);
        outStream.write("gss:".getBytes());
        outStream.write(outToken);

        String userPrincipal = getUserName();
		String userName = userPrincipal.split("@", 2)[0];

        return new AuthenticationRequest(userName, outStream.toByteArray());
    }

    public LoginComplete validateResponse(byte[] serverResponse) throws LoginException, IOException {
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(serverResponse));

        int gssTokenSize = in.readInt();
        System.err.println("GSS token is " + gssTokenSize + " bytes");
        byte[] gssToken = new byte[gssTokenSize];
        in.readFully(gssToken);

        int payloadWrappedSize = in.readInt();
        System.err.println("Wrapped payload is " + payloadWrappedSize + " bytes");
        byte[] payloadWrapped = new byte[payloadWrappedSize];
        in.readFully(payloadWrapped);

        if (gssToken.length != 0) initSecContext(gssToken);
        byte[] payload = unwrap(payloadWrapped);
        System.err.println("Payload is " + payload.length + " bytes");

        DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload));
        int serverNonce = payloadIn.readInt();
        int sessionKeySize = payloadIn.readInt();
        byte[] sessionKey = new byte[sessionKeySize];
        payloadIn.readFully(sessionKey);

		cleanup();

        return new LoginComplete(serverNonce, new C_Block(sessionKey));
    }

    protected void cleanup() {
        // do nothing by default
    }

    protected void finalize() throws Throwable {
        cleanup();
        super.finalize();
    }

}
