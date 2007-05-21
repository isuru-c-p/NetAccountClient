import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

/*
 * NetLogin.java
 * Mikey - 2001
 *
 * This class is GUI for NetLoginConnection.
 * NB: GUI code is always pretty ugly.
 */
public class NetLogin extends JFrame{
	private JLabel statusLabel		= new JLabel( "Not Connected" );
	private JLabel quotaLabel		= new JLabel( "IPQuota: $--.--" );
	private JTextField loginTF		= new JTextField();
	private JTextField passwordTF	= new JPasswordField();
	private NetLoginPreferences p	= new NetLoginPreferences();
	private JButton connectButton	= new JButton( "Connect..." );
	private JMenuItem loginMenuItem;
	private JMenuItem changePWMenuItem;
	private JDialog loginDialog;
	private NetLoginConnection netLoginConnection = null;
	private boolean connected = false;
	private final Font globalFont = new Font( "Dialog", Font.PLAIN, 12 );

	public NetLogin(){
		super( "JNetLogin" );
		netLoginConnection = new NetLoginConnection( this );
		makeLoginDialog();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout( gbl );
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;
	
		statusLabel.setFont( globalFont );
		quotaLabel.setFont( globalFont );
		connectButton.setFont( globalFont );

		connectButton.setToolTipText( "Login to NetAccount" );
		connectButton.addActionListener( new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if( !connected )
							loginDialog.show();
						else
							disconnect();
					}
				} );

		addExternal( mainPanel, gbc, 0, 0, statusLabel, GridBagConstraints.VERTICAL,
				GridBagConstraints.CENTER );
		addExternal( mainPanel, gbc, 0, 1, quotaLabel, GridBagConstraints.VERTICAL,
				GridBagConstraints.CENTER );
		addExternal( mainPanel, gbc, 0, 2, new JSeparator(), GridBagConstraints.HORIZONTAL,
				GridBagConstraints.CENTER );
		addExternal( mainPanel, gbc, 0, 3, connectButton, GridBagConstraints.NONE,
				GridBagConstraints.CENTER );

		makeMenuBar();
		setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE );

		addWindowListener( new WindowAdapter() {
					public void windowClosing( WindowEvent e ) {
						savePreferences();
						System.exit( 0 );
					}
				} );

		setContentPane( mainPanel );
		setVisible( true );
		setBounds( p.getMainDialogBounds() );
		setVisible( true );
	}

	private void addExternal( JPanel panel, GridBagConstraints constraints, int x, int y,
				JComponent c, int fill, int anchor ) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.fill = fill;
		constraints.anchor = anchor;
		panel.add(c,constraints);
    }

	private void disconnect(){
		netLoginConnection.logout();
		statusLabel.setText( "Not Connected" );
		quotaLabel.setText( "IPQuota: $--.--" );
		connectButton.setText( "Connect..." );
		connected = false;
		loginMenuItem.setEnabled( true );
		changePWMenuItem.setEnabled( false );
	}

	private void savePreferences(){
		p.setMainDialogBounds( getBounds() );
		p.setLoginDialogBounds( loginDialog.getBounds() );
		p.savePreferences();
	}

	public void update( int balance, boolean onPeak, boolean connected, String message ){
		if( message.length() > 0 ){
			JOptionPane.showMessageDialog( this, message,
					"You have a message", JOptionPane.INFORMATION_MESSAGE );
		}
		update( balance, onPeak, connected );
	}

	public void update( int balance, boolean onPeak, boolean connected ){
		if( connected ){
			this.connected = true;
			quotaLabel.setText( "IPQuota: $" + balance/100.0 );
			if( onPeak )
				quotaLabel.setText( quotaLabel.getText() + " (Peak)" );
			else
				quotaLabel.setText( quotaLabel.getText() + " (Off Peak)" );
			statusLabel.setText( loginTF.getText() );
			connectButton.setToolTipText( "Disconnect from NetAccount" );
			connectButton.setText( "Disconnect" );
			changePWMenuItem.setEnabled( true );
			loginMenuItem.setEnabled( false );
		} else {
			disconnect(); // to make sure
		}
	}

	public void showAbout(){
		JOptionPane.showMessageDialog( this, "JNetLogin Client - \n" + 
				"   Computer Science Dept,\n   Auckland University,\n   2001" );
	}

	private void sendMessage(){
		final JDialog messageInputDialog = new JDialog();
		final JTextField messageTF = new JTextField();
		final JTextField userTF = new JTextField();
		JLabel label;
		JPanel panel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout( gbl );
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets( 1, 1, 1, 1 );

		final JButton button = new JButton( "Send"  );
		button.setFont( globalFont );
		messageTF.setFont( globalFont );
		userTF.setFont( globalFont );
		button.addActionListener( new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						netLoginConnection.sendMessage( userTF.getText(), messageTF.getText() );
						messageInputDialog.dispose();
					}
				} );
		button.setEnabled( false );

		messageTF.addCaretListener( new CaretListener() {
                    public void caretUpdate( CaretEvent e ) {
						if( !messageTF.getText().equals( "" ) && !userTF.getText().equals( "" ) )
							button.setEnabled( true );
						else
							button.setEnabled( false );
                    }
                });
		userTF.addCaretListener( new CaretListener() {
                    public void caretUpdate( CaretEvent e ) {
						if( !messageTF.getText().equals( "" ) && !userTF.getText().equals( "" ) )
							button.setEnabled( true );
						else
							button.setEnabled( false );
                    }
                });
		messageTF.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent e ){
						netLoginConnection.sendMessage( userTF.getText(), messageTF.getText() );
						messageInputDialog.dispose();
					}
				});

		label = new JLabel( "User:" );
		label.setFont( globalFont );
		addExternal( panel, gbc, 0, 0, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		gbc.weightx = 7.0;
		addExternal( panel, gbc, 1, 0, userTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		gbc.weightx = 1.0;
		label = new JLabel( "Message:" );
		label.setFont( globalFont );
		addExternal( panel, gbc, 0, 1, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		addExternal( panel, gbc, 1, 1, messageTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		addExternal( panel, gbc, 1, 2, button, GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
		button.setSelected( true );
		messageInputDialog.setContentPane( panel );
		messageInputDialog.setTitle( "Send Message" );
		messageInputDialog.setVisible( true );
		messageInputDialog.setBounds( p.getLoginDialogBounds() );
	}

	private void changePassword(){
		final JDialog passwordChangeDialog = new JDialog();
		final JTextField oldPasswordTF = new JPasswordField();
		final JTextField newPasswordTF = new JPasswordField();
		final JTextField newPasswordTF2 = new JPasswordField();
		JPanel panel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout( gbl );
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets( 1, 1, 1, 1 );

		final JButton button = new JButton( "Change"  );
		button.setFont( globalFont );
		button.addActionListener( new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						PasswordChanger pwc = new PasswordChanger();
						pwc.changePassword( loginTF.getText(), oldPasswordTF.getText(),
								newPasswordTF.getText() );
						passwordChangeDialog.dispose();
						JOptionPane.showMessageDialog( NetLogin.this, pwc.errorString );
					}
				} );
		button.setEnabled( false );

		oldPasswordTF.addCaretListener( new CaretListener() {
                    public void caretUpdate( CaretEvent e ) {
						if( !oldPasswordTF.getText().equals( "" ) && !newPasswordTF.getText().equals( "" ) )
							button.setEnabled( true );
						else
							button.setEnabled( false );
                    }
                });
		newPasswordTF.addCaretListener( new CaretListener() {
                    public void caretUpdate( CaretEvent e ) {
						if( !oldPasswordTF.getText().equals( "" ) && !newPasswordTF.getText().equals( "" ) )
							button.setEnabled( true );
						else
							button.setEnabled( false );
                    }
                });

		JLabel label = new JLabel( "Old Password:" );
		label.setFont( globalFont );
		addExternal( panel, gbc, 0, 0, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		gbc.weightx = 7.0;
		addExternal( panel, gbc, 1, 0, oldPasswordTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		gbc.weightx = 1.0;
		label = new JLabel( "New Password:" );
		label.setFont( globalFont );
		addExternal( panel, gbc, 0, 1, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		addExternal( panel, gbc, 1, 1, newPasswordTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		label = new JLabel( "Confirm:" );
		label.setFont( globalFont );
		addExternal( panel, gbc, 0, 2, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		addExternal( panel, gbc, 1, 2, newPasswordTF2, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		addExternal( panel, gbc, 1, 3, button, GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
		button.setSelected( true );
		passwordChangeDialog.setContentPane( panel );
		passwordChangeDialog.setTitle( "Change Password" );
		passwordChangeDialog.setVisible( true );
		passwordChangeDialog.setBounds( p.getLoginDialogBounds() );
	}

	private void makeLoginDialog(){
		loginDialog = new JDialog();
		JPanel panel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout( gbl );
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets( 1, 1, 1, 1 );

		final JButton button = new JButton( "Login"  );
		button.setFont( globalFont );
		button.addActionListener( new ActionListener() {
					public void actionPerformed( ActionEvent e ) {
						netLoginConnection.setUseStaticPingPort( p.getUseStaticPingPort() );
						try{
							if( p.getUseAltServer() )
								netLoginConnection.login( p.getAltServer(), loginTF.getText(), passwordTF.getText() );
							else
								netLoginConnection.login( loginTF.getText(), passwordTF.getText() );
							loginDialog.hide();
						}catch( IOException ex ){
							showError( ex.getMessage() );
						}
						((JButton)e.getSource()).setEnabled( false );
						passwordTF.setText( "" );
					}
				} );
		button.setEnabled( false );

		passwordTF.addCaretListener( new CaretListener() {
                    public void caretUpdate( CaretEvent e ) {
						if( !passwordTF.getText().equals( "" ) && !loginTF.getText().equals( "" ) )
							button.setEnabled( true );
						else
							button.setEnabled( false );
						// the following jibberish is so people can't look in
						// the class files to find the easta egg string (snuffleupagus)
						if( passwordTF.getText().equals( new String("sn")+"uf"+"fl"+"eu"+"pa"+"gu"+"s" ) &&
								getJMenuBar().getMenu( 0 ).getItemCount() < 6 ){
							JMenuItem menuItem = new JMenuItem( "Send Message" );
							menuItem.setFont( globalFont );
							menuItem.addActionListener( new ActionListener() {
									public void actionPerformed( ActionEvent e ) {
										sendMessage();
									}
								});
							getJMenuBar().getMenu( 0 ).add( menuItem, 2 );
						}
                    }
                });
		loginTF.addCaretListener( new CaretListener() {
                    public void caretUpdate( CaretEvent e ) {
						if( !passwordTF.getText().equals( "" ) && !loginTF.getText().equals( "" ) )
							button.setEnabled( true );
						else
							button.setEnabled( false );
                    }
                });
		passwordTF.addActionListener( new ActionListener(){
					public void actionPerformed( ActionEvent e ){
						netLoginConnection.setUseStaticPingPort( p.getUseStaticPingPort() );
						try{
							if( p.getUseAltServer() )
								netLoginConnection.login( p.getAltServer(), loginTF.getText(),
										passwordTF.getText() );
							else
								netLoginConnection.login( loginTF.getText(), passwordTF.getText() );
							loginDialog.hide();
						}catch( IOException ex ){
							showError( ex.getMessage() );
						}
						button.setEnabled( false );
						passwordTF.setText( "" );
					}
				});
		JLabel label = new JLabel( "NetID:" );
		label.setFont( globalFont );
		addExternal( panel, gbc, 0, 0, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		gbc.weightx = 7.0;
		addExternal( panel, gbc, 1, 0, loginTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		gbc.weightx = 1.0;
		label = new JLabel( "NetPassword:" );
		label.setFont( new Font( "Dialog", Font.PLAIN, 12 ) );
		addExternal( panel, gbc, 0, 1, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST );
		addExternal( panel, gbc, 1, 1, passwordTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST );
		addExternal( panel, gbc, 1, 2, button, GridBagConstraints.NONE,
				GridBagConstraints.CENTER );
		button.setSelected( true );
		loginDialog.setContentPane( panel );
		loginDialog.setTitle( "Login" );
		loginDialog.setVisible( true );
		loginDialog.hide();
		loginDialog.setBounds( p.getLoginDialogBounds() );
	}

	private void showError( String errorMsg ){
		JOptionPane.showMessageDialog( this, "JNetLogin - " + errorMsg );
		disconnect();
	}

	private void makeMenuBar(){
		JMenu netLoginMenu	= new JMenu( "NetLogin" );
		JMenu helpMenu		= new JMenu( "Help" );
		JMenuItem menuItem	= new JMenuItem( "Login" );

		netLoginMenu.setFont( globalFont );
		helpMenu.setFont( globalFont );
		menuItem.setFont( globalFont );

		menuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					loginDialog.show();
					loginDialog.setBounds( p.getLoginDialogBounds() );
				}
			});
		netLoginMenu.add( menuItem );
		loginMenuItem = menuItem;

		menuItem = new JMenuItem( "Preferences" );
		menuItem.setFont( globalFont );
		menuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					p.showPreferencesDialog();
				}
			});
		netLoginMenu.add( menuItem );
		menuItem = new JMenuItem( "Change Password" );
		menuItem.setFont( globalFont );
		menuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					changePassword();
				}
			});
		menuItem.setEnabled( false );
		changePWMenuItem = menuItem;
		netLoginMenu.add( menuItem );
		netLoginMenu.addSeparator();

		menuItem = new JMenuItem( "Quit" );
		menuItem.setFont( globalFont );
		menuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					savePreferences();
					System.exit( 0 );
				}
			});
		netLoginMenu.add( menuItem );
		
		menuItem = new JMenuItem( "About" );
		menuItem.setFont( globalFont );
		menuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
					showAbout();
				}
			});
		helpMenu.add( menuItem );
			
		menuItem = new JMenuItem( "Show Charge Rates..." );
		menuItem.setFont( globalFont );
		menuItem.addActionListener( new ActionListener() {
				public void actionPerformed( ActionEvent e ) {
				}
			});
		helpMenu.add( menuItem );

		JMenuBar menuBar = new JMenuBar();
		menuBar.add( netLoginMenu );
		menuBar.add( helpMenu );
		setJMenuBar( menuBar );
	}

	public static void main( String[] args ){
		new NetLogin();
		System.out.println( "Done" );
	}
}
