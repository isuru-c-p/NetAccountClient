package nz.ac.auckland.netlogin.negotiation;

import nz.ac.auckland.netlogin.NetLoginPreferences;
import org.ietf.jgss.*;
import javax.security.auth.login.LoginException;
import java.io.IOException;

public class GSSAPIAuthenticator implements Authenticator {

    private static final GSSCredential DEFAULT_CREDENTIAL = null;
    private final Oid KRB5_MECHANISM;
	private final Oid KRB5_PRINCIPAL_NAME_TYPE;
    private final GSSManager manager;
    private GSSContext context;

    public GSSAPIAuthenticator() throws GSSException {
        setSystemPropertyDefault("javax.security.auth.useSubjectCredsOnly", "false");
        setSystemPropertyDefault("java.security.auth.login.config", GSSAPIAuthenticator.class.getResource("/login.config").toString());
		
        KRB5_MECHANISM = new Oid("1.2.840.113554.1.2.2");
        KRB5_PRINCIPAL_NAME_TYPE = new Oid("1.2.840.113554.1.2.2.1");
        manager = GSSManager.getInstance();
    }

    private static void setSystemPropertyDefault(String name, String value) {
        if (System.getProperty(name) == null) {
            System.setProperty(name, value);
        }
    }

    public String getName() {
        return "GSSAPI";
    }

    public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
        try {
            context = createContext();

		    byte[] inToken = new byte[0];
            byte[] outToken = context.initSecContext(inToken, 0, inToken.length);
            
            String username = context.getSrcName().toString();
            return new AuthenticationRequest(username, outToken);
        } catch (GSSException e) {
            System.err.println("GSSAPI: " + e.getMessage());
            throw new LoginException("GSSAPI: " + e.getMessage());
        }
    }

    public LoginComplete validateResponse(byte[] inToken) throws LoginException, IOException {

        try {
            byte[] outToken = context.initSecContext(inToken, 0, inToken.length);
            if (!context.isEstablished()) throw new LoginException("Trust not established after one exchanges");
            throw new LoginException("Now we need to transfer the data!");
        } catch (GSSException e) {
            System.err.println("GSSAPI: " + e.getMessage());
            throw new LoginException("GSSAPI: " + e.getMessage());
        }

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
