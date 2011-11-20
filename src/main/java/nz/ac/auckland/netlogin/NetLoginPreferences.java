package nz.ac.auckland.netlogin;

import java.util.Properties;

public class NetLoginPreferences {
	
	private Properties props = new Properties();

	public NetLoginPreferences(){
		loadProperties();
	}

	private void loadProperties(){
		props.put("useAltServer", "false");
		props.put("altServer", "gate.ec.auckland.ac.nz");
		props.put("useStaticPingPort", "false");
	}

	public void savePreferences() {
	}

	public boolean getUseAltServer(){
		return props.getProperty("useAltServer").equals("true");
	}

	public String getAltServer(){
		return props.getProperty("altServer");
	}

	public boolean getUseStaticPingPort(){
		return props.getProperty("useStaticPingPort").equals("true");
	}

}
