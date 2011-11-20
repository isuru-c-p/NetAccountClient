package nz.ac.auckland.netlogin;

import org.apache.commons.beanutils.PropertyUtils;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.prefs.Preferences;

public class NetLoginPreferences {
	
	private static NetLoginPreferences instance;
	private String server = "gate.ec.auckland.ac.nz";
	private String credentialSource = "Default";

	public static NetLoginPreferences getInstance() {
		if (instance == null) instance = new NetLoginPreferences();
		return instance;
	}

	private NetLoginPreferences() {
		loadPreferences();
	}

	public void loadPreferences() {
		Preferences preferences = Preferences.userNodeForPackage(NetLogin.class);
		for(PropertyDescriptor desc : PropertyUtils.getPropertyDescriptors(this)) {
			if (desc.getReadMethod() == null || desc.getWriteMethod() == null) continue;

			String name = desc.getName();
			String value = preferences.get(name, null);
			if (value == null) continue;

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