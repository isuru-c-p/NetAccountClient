package nz.ac.auckland.netlogin.negotiation;

import javax.security.auth.login.LoginException;
import java.io.IOException;

public class GSSAPIAuthenticator implements Authenticator {


    public String getName() {
        return "GSSAPI";
    }

    public AuthenticationRequest startAuthentication(CredentialsCallback callback) throws LoginException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public LoginComplete validateResponse(byte[] serverResponse) throws LoginException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
    
}
