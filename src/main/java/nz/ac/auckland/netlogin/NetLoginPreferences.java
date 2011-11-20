package nz.ac.auckland.netlogin;

import org.apache.commons.beanutils.PropertyUtils;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.prefs.Preferences;

public class NetLoginPreferences {
	
	private String server;
	private String credentialSource;

	public NetLoginPreferences() {
		loadPreferences();
	}

	public Properties defaultPreferences() {
		try {
			Properties defaults = new Properties();
			defaults.load(getClass().getResourceAsStream("/netlogin.properties"));
			return defaults;
		} catch (IOException e) {
			// treat this as a fatal error
			throw new RuntimeException("Unable to load default preferences: " + e.getMessage());
		}
	}

	public void loadPreferences() {
		Properties defaults = defaultPreferences();
		Preferences preferences = Preferences.userNodeForPackage(NetLogin.class);
		for(PropertyDescriptor desc : PropertyUtils.getPropertyDescriptors(this)) {
			if (desc.getReadMethod() == null || desc.getWriteMethod() == null) continue;

			String name = desc.getName();
			String defaultValue = defaults.getProperty(name);
			String value = preferences.get(name, defaultValue);
			try {
				PropertyUtils.setProperty(this, name, value);
			} catch (IllegalAccessException e) {
				System.err.printf("Unable to load preference %s: %s\n", name, e.getMessage());
			} catch (InvocationTargetException e) {
				System.err.printf("Unable to load preference %s: %s\n", name, e.getMessage());
			} catch (NoSuchMethodException e) {
				System.err.printf("Unable to load preference %s: %s\n", name, e.getMessage());
			}
		}
	}

	public void savePreferences() {
		Preferences preferences = Preferences.userNodeForPackage(NetLogin.class);
		for(java.beans.PropertyDescriptor desc : PropertyUtils.getPropertyDescriptors(this)) {
			if (desc.getReadMethod() == null || desc.getWriteMethod() == null) continue;

			String name = desc.getName();
			try {
				Object value = PropertyUtils.getProperty(this, name);
				String valueStr = value == null ? "" : value.toString();
				preferences.put(name, valueStr);
			} catch (IllegalAccessException e) {
				System.err.printf("Unable to save preference %s: %s\n", name, e.getMessage());
			} catch (InvocationTargetException e) {
				System.err.printf("Unable to save preference %s: %s\n", name, e.getMessage());
			} catch (NoSuchMethodException e) {
				System.err.printf("Unable to save preference %s: %s\n", name, e.getMessage());
			}
		}
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getCredentialSource() {
		return credentialSource;
	}

	public void setCredentialSource(String credentialSource) {
		this.credentialSource = credentialSource;
	}

	public boolean getUseStaticPingPort() {
		return false;
	}

}