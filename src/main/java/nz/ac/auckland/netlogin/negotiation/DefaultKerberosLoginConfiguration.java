package nz.ac.auckland.netlogin.negotiation;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import java.util.HashMap;
import java.util.Map;

/**
 * This would normally be in a login.config file, however ConfigFile doesn't like loading
 * jar-in-jar login files, so we'd need to write it to the file system.
 * This is easier.
 */
public class DefaultKerberosLoginConfiguration extends Configuration {

    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
        if (name.equals("com.sun.security.jgss.krb5.initiate")) {
            Map<String, String> options = new HashMap<String, String>();
            options.put("useTicketCache", "true");
            options.put("doNotPrompt", "true");

            return new AppConfigurationEntry[] {
                new AppConfigurationEntry("com.sun.security.auth.module.Krb5LoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                    options)
            };
        }
        return null;
    }

}
