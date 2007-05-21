import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

class NetLoginPreferences{
	private Properties p = new Properties();
	private final Font globalFont = new Font( "Dialog", Font.PLAIN, 12 );

	public NetLoginPreferences(){
		loadProperties();
	}

	private void loadProperties(){
		if( System.getProperty( "os.name" ).equals( "Linux" ) ){
			p.put( "mainDialogX", "50" );
			p.put( "mainDialogY", "50" );
			p.put( "mainDialogWidth", "150" );
			p.put( "mainDialogHeight", "85" );

			p.put( "loginDialogX", "100" );
			p.put( "loginDialogY", "100" );
			p.put( "loginDialogWidth", "200" );
			p.put( "loginDialogHeight", "80" );
			p.put( "useAltServer", "false" );
			p.put( "altServer", "gate.ec.auckland.ac.nz" );
			p.put( "useStaticPingPort", "false" );
		}else if( System.getProperty( "os.name" ).equals( "Mac OS X" ) ){
			p.put( "mainDialogX", "50" );
			p.put( "mainDialogY", "50" );
			p.put( "mainDialogWidth", "150" );
			p.put( "mainDialogHeight", "110" );

			p.put( "loginDialogX", "100" );
			p.put( "loginDialogY", "100" );
			p.put( "loginDialogWidth", "200" );
			p.put( "loginDialogHeight", "130" );
			p.put( "useAltServer", "false" );
			p.put( "altServer", "gate.ec.auckland.ac.nz" );
			p.put( "useStaticPingPort", "false" );
		}else{
			System.err.println( "Unknown OS: " + System.getProperty( "os.name" ) + ", using defaults.");
			p.put( "mainDialogX", "50" );
			p.put( "mainDialogY", "50" );
			p.put( "mainDialogWidth", "150" );
			p.put( "mainDialogHeight", "110" );

			p.put( "loginDialogX", "100" );
			p.put( "loginDialogY", "100" );
			p.put( "loginDialogWidth", "200" );
			p.put( "loginDialogHeight", "130" );
			p.put( "useAltServer", "false" );
			p.put( "altServer", "gate.ec.auckland.ac.nz" );
			p.put( "useStaticPingPort", "false" );
		}

/*
 * We dont save or load preferences anymore, because getting window
 * attributes often returned rubbish
 *
		if( System.getProperty( "os.name" ).equals( "Linux" ) ||
			System.getProperty( "os.name" ).equals( "Mac OS X" ) ||
			System.getProperty( "os.name" ).indexOf( "nix" ) != -1 ){
			// were on a unix of some sort.
			try{
				p.load( new FileInputStream( System.getProperty( "user.home" ) + "/.JNetLogin/preferences" ) );
			}catch( Exception e ){
				// prolly a new user, fall back on defaults
				File f = new File( System.getProperty( "user.home" ) + "/.JNetLogin");
				try{
					f.mkdirs();
				} catch( Exception ex ) {
					System.err.println( "Unable to create ~/.JNetLogin: " + ex.getMessage() );
				}
				p.put( "mainDialogX", "50" );
				p.put( "mainDialogY", "50" );
				p.put( "mainDialogWidth", "150" );
				p.put( "mainDialogHeight", "110" );

				p.put( "loginDialogX", "100" );
				p.put( "loginDialogY", "100" );
				p.put( "loginDialogWidth", "200" );
				p.put( "loginDialogHeight", "130" );
				p.put( "useAltServer", "false" );
				p.put( "altServer", "gate.ec.auckland.ac.nz" );
				p.put( "useStaticPingPort", "false" );
			}
		} else {
			// were on unknown (prolly windoze), fall back on defaults
			System.err.println( "Unknown OS: " + System.getProperty( "os.name" ) );
			p = new Properties();
			p.put( "mainDialogX", "50" );
			p.put( "mainDialogY", "50" );
			p.put( "mainDialogWidth", "180" );
			p.put( "mainDialogHeight", "120" );

			p.put( "loginDialogX", "100" );
			p.put( "loginDialogY", "100" );
			p.put( "loginDialogWidth", "200" );
			p.put( "loginDialogHeight", "120" );
			p.put( "useAltServer", "false" );
			p.put( "altServer", "gate.ec.auckland.ac.nz" );
			p.put( "useStaticPingPort", "false" );
		}
 */
	}

	public void savePreferences(){
/*
 * We dont save or load preferences anymore, because getting window
 * attributes often returned rubbish
 *
		if( System.getProperty( "os.name" ).equals( "Linux" ) ||
			System.getProperty( "os.name" ).equals( "Mac OS X" ) ||
			System.getProperty( "os.name" ).indexOf( "nix" ) != -1 ){
			// were on a unix
			try{
				p.store( new FileOutputStream( System.getProperty( "user.home" ) + "/.JNetLogin/preferences" ),
						"JNetLogin Preferences" );
			}catch( Exception e ){
				System.err.println( "Unable to save preferences: " + e.getMessage() );
			}
		} else {
			System.err.println( "Not saving preferences for unknown OS" );
		}
 */
	}

	public void showPreferencesDialog(){
		final JDialog preferencesDialog = new JDialog();
		JPanel panel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout( gbl );
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets( 1, 1, 1, 1 );

		final JCheckBox altServerCB = new JCheckBox( "Alternate server", p.getProperty( "useAltServer" ).equals( "true" ) );
		altServerCB.setFont( globalFont );
		final JTextField altServerTF = new JTextField( p.getProperty( "altServer" ) );
		final JCheckBox useStaticPingPortCB = new JCheckBox( "Static ping port", p.getProperty( "useStaticPingPort" ).equals( "true" ) );
		useStaticPingPortCB.setFont( globalFont );
		altServerCB.addChangeListener( new ChangeListener(){
					public void stateChanged( ChangeEvent e ){
						if( ((JCheckBox)e.getSource()).isSelected() )
							altServerTF.setEnabled( true );
						else
							altServerTF.setEnabled( false );
					}
				});
		addExternal( panel, gbc, 0, 0, altServerCB, GridBagConstraints.NONE, GridBagConstraints.WEST );
		gbc.weightx = 7.0;
		addExternal( panel, gbc, 1, 0, altServerTF, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
		gbc.weightx = 1.0;
		addExternal( panel, gbc, 0, 1, useStaticPingPortCB, GridBagConstraints.NONE, GridBagConstraints.WEST );

		JButton okB = new JButton( "OK" );
		okB.setFont( globalFont );
		okB.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent e ){
						p.put( "useStaticPingPort", useStaticPingPortCB.isSelected()?"true":"false" );
						p.put( "useAltServer", altServerCB.isSelected()?"true":"false" );
						p.put( "altServer", altServerTF.getText() );
						preferencesDialog.dispose();
					}
				});
		addExternal( panel, gbc, 0, 2, okB, GridBagConstraints.NONE, GridBagConstraints.EAST );
		JButton cancelB = new JButton( "Cancel" );
		cancelB.setFont( globalFont );
		cancelB.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent e ){
						preferencesDialog.dispose();
					}
				});
		addExternal( panel, gbc, 1, 2, cancelB, GridBagConstraints.NONE, GridBagConstraints.EAST );

		preferencesDialog.setContentPane( panel );
		preferencesDialog.setBounds( 100, 100, 300, 130 );
		preferencesDialog.setTitle( "JNetLogin - Preferences" );
		preferencesDialog.show();
	}

	private void addExternal( JPanel panel, GridBagConstraints constraints, int x, int y,
		JComponent c, int fill, int anchor ) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.fill = fill;
		constraints.anchor = anchor; 
		panel.add(c,constraints);
	}

	public boolean getUseAltServer(){
		return p.getProperty( "useAltServer" ).equals( "true" );
	}

	public String getAltServer(){
		return p.getProperty( "altServer" );
	}

	public boolean getUseStaticPingPort(){
		return p.getProperty( "useStaticPingPort" ).equals( "true" );
	}

	public void setMainDialogBounds( Rectangle r ){
		p.put( "mainDialogX", "" + r.x );
		p.put( "mainDialogY", "" + r.y );
		p.put( "mainDialogWidth", "" + r.width );
		p.put( "mainDialogHeight", "" + r.height );
	}

	public Rectangle getMainDialogBounds(){
		return new Rectangle( Integer.parseInt( p.getProperty( "mainDialogX" ) ),
				Integer.parseInt( p.getProperty( "mainDialogY" ) ),
				Integer.parseInt( p.getProperty( "mainDialogWidth" ) ),
				Integer.parseInt( p.getProperty( "mainDialogHeight" ) ) );
	}

	public void setLoginDialogBounds( Rectangle r ){
		p.put( "loginDialogX", "" + r.x );
		p.put( "loginDialogY", "" + r.y );
		p.put( "loginDialogWidth", "" + r.width );
		p.put( "loginDialogHeight", "" + r.height );
	}

	public Rectangle getLoginDialogBounds(){
		return new Rectangle( Integer.parseInt( p.getProperty( "loginDialogX" ) ),
				Integer.parseInt( p.getProperty( "loginDialogY" ) ),
				Integer.parseInt( p.getProperty( "loginDialogWidth" ) ),
				Integer.parseInt( p.getProperty( "loginDialogHeight" ) ) );
	}
}
