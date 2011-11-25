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

public class GSSAPIAuthenticator extends AbstractGSSAuthenticator {

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

    protected String getUserName() throws LoginException {
        try {
            return context.getSrcName().toString().split("@", 2)[0];
        } catch (GSSException e) {
            System.err.println("GSSAPI: " + e.getMessage());
            throw new LoginException("GSSAPI: " + e.getMessage());
        }
    }

    protected void initializeContext() throws LoginException {
        try {
            GSSName serverName = manager.createName(getServicePrincipalName(), KRB5_PRINCIPAL_NAME_TYPE);

            context = manager.createContext(
                    serverName,
                    KRB5_MECHANISM,
                    DEFAULT_CREDENTIAL,
                    GSSContext.DEFAULT_LIFETIME);

            context.requestMutualAuth(true); // mutual authentication
            context.requestConf(true); // confidentiality
            context.requestInteg(true); // integrity
            context.requestCredDeleg(false);
        } catch (GSSException e) {
            System.err.println("GSSAPI: " + e.getMessage());
            throw new LoginException("GSSAPI: " + e.getMessage());
        }
    }

    protected byte[] initSecContext(byte[] gssToken) throws LoginException {
        if (gssToken == null) gssToken = new byte[0];
        try {
            return context.initSecContext(gssToken, 0, gssToken.length);
        } catch (GSSException e) {
            System.err.println("GSSAPI: " + e.getMessage());
            throw new LoginException("GSSAPI: " + e.getMessage());
        }
    }

    protected byte[] unwrap(byte[] wrapper) throws LoginException {
        try {
            MessageProp messageProp = new MessageProp(0, true);
            return context.unwrap(wrapper, 0, wrapper.length, messageProp);
        } catch (GSSException e) {
            System.err.println("GSSAPI: " + e.getMessage());
            throw new LoginException("GSSAPI: " + e.getMessage());
        }
    }

}
