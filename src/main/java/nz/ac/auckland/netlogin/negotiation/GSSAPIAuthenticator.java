package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.cs.des.C_Block;
import nz.ac.auckland.netlogin.NetLoginPreferences;
import nz.ac.auckland.netlogin.util.SystemSettings;
import org.ietf.jgss.*;
import javax.security.auth.login.LoginException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class GSSAPIAuthenticator implements Authenticator {

    private static final GSSCredential DEFAULT_CREDENTIAL = null;
    private final Oid KRB5_MECHANISM;
	private final Oid KRB5_PRINCIPAL_NAME_TYPE;
    private final GSSManager manager;
    private GSSContext context;

    public GSSAPIAuthenticator() throws GSSException {
        SystemSettings.setSystemPropertyDefault("javax.security.auth.useSubjectCredsOnly", "false");
        SystemSettings.setSystemPropertyDefault("java.security.auth.login.config", GSSAPIAuthenticator.class.getResource("/login.config").toString());
		
        KRB5_MECHANISM = new Oid("1.2.840.113554.1.2.2");
        KRB5_PRINCIPAL_NAME_TYPE = new Oid("1.2.840.113554.1.2.2.1");
        manager = GSSManager.getInstance();
    }

    public String getName() {
        return "GSSAPI";
    }

    public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
        try {
            context = createContext();

			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			outStream.write("gss:".getBytes());
            context.initSecContext(null, outStream);
            
            String username = context.getSrcName().toString().split("@", 2)[0];

            return new AuthenticationRequest(username, outStream.toByteArray());
        } catch (GSSException e) {
            System.err.println("GSSAPI request: " + e.getMessage());
            throw new LoginException("GSSAPI request: " + e.getMessage());
        }
    }

    public LoginComplete validateResponse(byte[] inToken) throws LoginException, IOException {

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(inToken));

        int gssTokenSize = in.readInt();
        byte[] gssToken = new byte[gssTokenSize];
        in.readFully(gssToken);

        int payloadWrappedSize = in.readInt();
        byte[] payloadWrapped = new byte[payloadWrappedSize];
        in.readFully(payloadWrapped);

        byte[] payload;
        try {
            context.initSecContext(gssToken, 0, gssToken.length);
            if (!context.isEstablished()) throw new LoginException("Trust not established after one exchange");

            MessageProp messageProp = new MessageProp(0, true);
            payload = context.unwrap(payloadWrapped, 0, payloadWrapped.length, messageProp);
        } catch (GSSException e) {
            System.err.println("GSSAPI response: " + e.getMessage());
            throw new LoginException("GSSAPI response: " + e.getMessage());
        }

        DataInputStream payloadIn = new DataInputStream(new ByteArrayInputStream(payload));
        int clientNonce = payloadIn.readInt();
        int serverNonce = payloadIn.readInt();
        int sessionKeySize = payloadIn.readInt();
        byte[] sessionKey = new byte[sessionKeySize];
        payloadIn.readFully(sessionKey);

        return new LoginComplete(clientNonce, serverNonce, new C_Block(sessionKey));
    }

    public static String getServicePrincipalName() {
        String serverName = NetLoginPreferences.getInstance().getServer();
        String realmName = NetLoginPreferences.getInstance().getRealm();
        return "netlogin/" + serverName + "@" + realmName;
    }

    private GSSContext createContext() throws GSSException {
        GSSName serverName = manager.createName(getServicePrincipalName(), KRB5_PRINCIPAL_NAME_TYPE);

        GSSContext context = manager.createContext(
                serverName,
                KRB5_MECHANISM,
                DEFAULT_CREDENTIAL,
                GSSContext.DEFAULT_LIFETIME);

        context.requestMutualAuth(true); // mutual authentication
        context.requestConf(true); // confidentiality
        context.requestInteg(true); // integrity
        context.requestCredDeleg(false);

        return context;
    }

}
