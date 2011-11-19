import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Method;
import java.awt.Window;

public class NetLoginGUI extends JPanel implements PingListener {

	private Window window;
    private JFrame windowAsFrame;
    private JDialog windowAsDialog;

	private static final long serialVersionUID = 1L;
	private JLabel upiTitle = new JLabel("NetID/UPI:  ");
	private JLabel planTitle = new JLabel("Internet Plan:  ");
	private JLabel usageTitle = new JLabel("MBs used this month:  ");
	private JLabel statusLabel = new JLabel("Not Connected");
	private JLabel planLabel = new JLabel("");
	private JLabel usageLabel = new JLabel("");
	private JTextField loginTF = new JTextField();
	private JTextField passwordTF = new JPasswordField();
	private NetLoginPreferences p = new NetLoginPreferences();
	private JButton connectButton = new JButton("Connect...");
	private JMenuItem loginMenuItem;
	private JMenuItem changePWMenuItem;
	private JDialog loginDialog;
	private NetLoginConnection netLoginConnection = null;
	private boolean connected = false;
	private final Font globalFont = new Font("Dialog", Font.PLAIN, 12);
	private final Font globalTitleFont = new Font("Dialog", Font.BOLD, 12);
	private final Color globalTitleColor = new Color(51, 102, 255);
    
	private boolean useSystemTray = true;
	private TrayIcon trayIcon;
	private String plan_name = "";
	
	static String versionNumber = "3.0.4";
	static String helpURL = "http://www.ec.auckland.ac.nz/docs/net-student.htm";
	static String passwdChangeURL = "https://iam.auckland.ac.nz/password/change";
	static String icon_imagename="jnetlogin16x16.gif";
	static String aboutInfo = "JNetLogin Client Version "+ versionNumber
		+ "\nCopyright(C) 2001-2011 The University of Auckland.\n"
            + "Release under terms of the GNU GPL. \n";

    private JPanel mainPanel;

    private Image iconDefault;
    private Image iconConnected;
    private Image iconConnecting;
    private Image iconDisconnected;

    public NetLoginGUI() {
		initBody();
		createWindow();
        initTrayIcon();
		openWindow();
	}

	// No makeLoginDialog(), directly login
	public NetLoginGUI(String upi, String password) {
		initBody();
		createWindow();
		login(upi, password);
        initTrayIcon();
		minimizeWindow();
	}
	
	public void createWindow() {
		if (isSystemTraySupported()) {
			windowAsDialog = new JDialog((Frame)null, "NetLogin");
			windowAsDialog.setContentPane(mainPanel);
            windowAsDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            windowAsDialog.setResizable(false);
			window = windowAsDialog;
		} else {
			windowAsFrame = new JFrame("NetLogin");
			windowAsFrame.setContentPane(mainPanel);
            windowAsFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
            windowAsFrame.setResizable(false);
			window = windowAsFrame;
		}

		makeMenuBar();

		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
					minimizeWindow();
			}
		});

		window.setBounds(p.getMainDialogBounds());
		window.setBounds(12, 12, 270, 160);
		window.setLocationRelativeTo(null);
		window.setIconImage(new ImageIcon(icon_imagename).getImage());
	}
	
	public void login(String upi, String password) {
		netLoginConnection = new NetLoginConnection(this);
		netLoginConnection.setUseStaticPingPort(p.getUseStaticPingPort());
		try {
			netLoginConnection.login(upi, password);
			loginDialog.setVisible(false);
		} catch (IOException ex) {
			showError(ex.getMessage());
		}
		statusLabel.setText(upi);
		loginTF.setText(upi);
	}
	
	public boolean isSystemTraySupported() {
		return useSystemTray && SystemTray.isSupported();
	}
	
	public void openWindow() {
		if (!isSystemTraySupported()) windowAsFrame.setExtendedState(Frame.NORMAL);
		window.setVisible(true);
		window.toFront();
	}
	
	public void minimizeWindow() {
		if (isSystemTraySupported()) {
			window.setVisible(false);
		} else {
			windowAsFrame.setExtendedState(Frame.ICONIFIED);
		}
	}

	public void initBody() {
		if (netLoginConnection == null) {
			netLoginConnection = new NetLoginConnection(this);
		}
		
		makeLoginDialog();

        mainPanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setLayout(gbl);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;

		upiTitle.setFont(globalTitleFont);
		planTitle.setFont(globalTitleFont);
		usageTitle.setFont(globalTitleFont);
		upiTitle.setForeground(globalTitleColor);
		planTitle.setForeground(globalTitleColor);
		usageTitle.setForeground(globalTitleColor);

		statusLabel.setFont(globalFont);
		planLabel.setFont(globalFont);
		usageLabel.setFont(globalFont);

		connectButton.setFont(globalTitleFont);
		connectButton.setForeground(globalTitleColor);
		connectButton.setToolTipText("Login to NetAccount");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!connected) {
					loginDialog.setVisible(true);
                } else {
					disconnect();
                }
			}
		});

		addExternal(mainPanel, gbc, 0, 0, upiTitle,
				GridBagConstraints.VERTICAL, GridBagConstraints.EAST);
		addExternal(mainPanel, gbc, 0, 1, planTitle,
				GridBagConstraints.VERTICAL, GridBagConstraints.EAST);
		addExternal(mainPanel, gbc, 0, 2, usageTitle,
				GridBagConstraints.VERTICAL, GridBagConstraints.EAST);
		addExternal(mainPanel, gbc, 1, 0, statusLabel,
				GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
		addExternal(mainPanel, gbc, 1, 1, planLabel,
				GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
		addExternal(mainPanel, gbc, 1, 2, usageLabel,
				GridBagConstraints.VERTICAL, GridBagConstraints.WEST);
		addExternal(mainPanel, gbc, 0, 3, new JSeparator(),
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		addExternal(mainPanel, gbc, 1, 3, new JSeparator(),
				GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER);
		addExternal(mainPanel, gbc, 1, 4, connectButton,
                GridBagConstraints.NONE, GridBagConstraints.CENTER);
	}

	private void addExternal(JPanel panel, GridBagConstraints constraints,
			int x, int y, JComponent c, int fill, int anchor) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.fill = fill;
		constraints.anchor = anchor;
		panel.add(c, constraints);
	}

	private void disconnect() {
		netLoginConnection.logout();
		statusLabel.setText("Not Connected");
		planLabel.setText("");
		usageLabel.setText("");
		connectButton.setText("Connect...");
		connected = false;
		loginMenuItem.setEnabled(true);
	}

	public void update(int balance, boolean onPeak, boolean connected, String message) {
		/* block function of display instant message */
		/*
		 * if (message.length() > 0) { JOptionPane.showMessageDialog(this,
		 * message, "You have a message", JOptionPane.INFORMATION_MESSAGE); }
		 */
		update(balance, onPeak, connected);
	}

	public void update(int balance, boolean onPeak, boolean connected) {
		if (connected) {
			this.connected = true;
			statusLabel.setText(loginTF.getText());
			connectButton.setToolTipText("Disconnect from NetAccount");
			connectButton.setText("Disconnect");
			loginMenuItem.setEnabled(false);
		} else {
			disconnect(); // to make sure
		}
		updateTrayLabel();
	}

	/* new update menthod for client version >=3 netlogin */
	public void updateV3(int ip_usage, int user_plan_flags, boolean connected,
			String message) {
		
		if (connected) {
			this.connected = true;
			float MBs_usage = (float) (Math.round((ip_usage / 1024.0) * 100)) / 100;
			usageLabel.setText("" + MBs_usage + "MBs");

			user_plan_flags = user_plan_flags & 0x0F000000;
			switch (user_plan_flags) {
			case 0x01000000: // STATUS_UNLIMITED: =>Staff
				plan_name = "Staff";
				break;
			case 0x02000000: // STATUS_SPONSORED:
				plan_name = "Sponsored";
				break;
			case 0x03000000: // STATUS_PREMIUM: =>undergraduate
				plan_name = "Undergraduate";
				break;
			case 0x04000000: // STATUS_STANDARD: => exceeded
				plan_name = "ExceededAllowance";
				break;
			case 0x05000000: // STATUS_NOACCESS:
				plan_name = "NoAccess";
				break;
			case 0x06000000: // STATUS_PostGraduate:
				plan_name = "Postgraduate";
				break;
			default:
				plan_name = "";
			}
			planLabel.setText(plan_name);
			statusLabel.setText(loginTF.getText());
			connectButton.setToolTipText("Disconnect from NetAccount");
			connectButton.setText("Disconnect");
			loginMenuItem.setEnabled(false);
		} else {
			disconnect(); // to make sure
		}
	}

	public void showAbout() {
		JOptionPane.showMessageDialog(this,aboutInfo);
	}

	private void makeLoginDialog() {
		loginDialog = new JDialog();
		JPanel panel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout(gbl);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(1, 1, 1, 1);

		final JButton button = new JButton("Login");
		button.setFont(globalFont);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				netLoginConnection.setUseStaticPingPort(p
						.getUseStaticPingPort());
				try {
					if (p.getUseAltServer()) {
						netLoginConnection.login(p.getAltServer(), loginTF.getText(), passwordTF.getText());
                    } else {
						netLoginConnection.login(loginTF.getText(), passwordTF.getText());
                    }
					loginDialog.setVisible(false);
				} catch (IOException ex) {
					showError(ex.getMessage());
				}
				((JButton) e.getSource()).setEnabled(false);
				passwordTF.setText("");
			}
		});
		button.setEnabled(false);

		passwordTF.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				if (!passwordTF.getText().equals("")
						&& !loginTF.getText().equals(""))
					button.setEnabled(true);
				else
					button.setEnabled(false);
			}
		});
		loginTF.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				if (!passwordTF.getText().equals("")
						&& !loginTF.getText().equals(""))
					button.setEnabled(true);
				else
					button.setEnabled(false);
			}
		});
		passwordTF.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				netLoginConnection.setUseStaticPingPort(p
						.getUseStaticPingPort());
				try {
					if (p.getUseAltServer()) {
						netLoginConnection.login(p.getAltServer(), loginTF.getText(), passwordTF.getText());
                    } else {
						netLoginConnection.login(loginTF.getText(), passwordTF.getText());
                    }
					loginDialog.setVisible(false);
				} catch (IOException ex) {
					showError(ex.getMessage());
				}
				button.setEnabled(false);
				passwordTF.setText("");
			}
		});
		JLabel label = new JLabel("UPI:");
		label.setFont(globalFont);
		addExternal(panel, gbc, 0, 0, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST);
		gbc.weightx = 5.0;
		addExternal(panel, gbc, 1, 0, loginTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST);
		gbc.weightx = 5.0;
		label = new JLabel("Password:");
		label.setFont(new Font("Dialog", Font.PLAIN, 12));
		addExternal(panel, gbc, 0, 1, label, GridBagConstraints.VERTICAL,
				GridBagConstraints.EAST);
		addExternal(panel, gbc, 1, 1, passwordTF, GridBagConstraints.BOTH,
				GridBagConstraints.WEST);
		addExternal(panel, gbc, 1, 2, button, GridBagConstraints.NONE,
				GridBagConstraints.CENTER);
		button.setSelected(true);
		loginDialog.setContentPane(panel);
		loginDialog.setTitle("Login");
		loginDialog.setVisible(false);
		loginDialog.setBounds(p.getLoginDialogBounds());
		loginDialog.setBounds(12, 12, 270, 120);
		loginDialog.setLocationRelativeTo(null);
		loginDialog.setResizable(false);
		loginDialog.setIconImage(new ImageIcon(icon_imagename).getImage());
	}

	private void showError(String errorMsg) {
		JOptionPane.showMessageDialog(this, "JNetLogin - " + errorMsg);
		disconnect();
	}

	private void makeMenuBar() {
		JMenu netLoginMenu = new JMenu("NetLogin");
		JMenu helpMenu = new JMenu("Help");
		JMenuItem menuItem = new JMenuItem("Login");

		netLoginMenu.setFont(globalFont);
		helpMenu.setFont(globalFont);
		menuItem.setFont(globalFont);

		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loginDialog.setBounds(p.getLoginDialogBounds());
				loginDialog.setLocationRelativeTo(null);
				loginDialog.setVisible(true);
			}
		});
		netLoginMenu.add(menuItem);
		loginMenuItem = menuItem;

		menuItem = new JMenuItem("Preferences");
		menuItem.setFont(globalFont);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				p.showPreferencesDialog();
			}
		});
		netLoginMenu.add(menuItem);
		menuItem = new JMenuItem("Change Password");
		menuItem.setFont(globalFont);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// changePassword();
				/* open web-based change password */
				openURL(passwdChangeURL);
			}
		});
		menuItem.setEnabled(true);
		changePWMenuItem = menuItem;
		netLoginMenu.add(menuItem);
		netLoginMenu.addSeparator();

		menuItem = new JMenuItem("Quit");
		menuItem.setFont(globalFont);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		netLoginMenu.add(menuItem);

		menuItem = new JMenuItem("About");
		menuItem.setFont(globalFont);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAbout();
			}
		});
		helpMenu.add(menuItem);

		menuItem = new JMenuItem("Show Charge Rates...");
		menuItem.setFont(globalFont);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openURL(helpURL);
			}
		});
		helpMenu.add(menuItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(netLoginMenu);
		menuBar.add(helpMenu);

        if (windowAsDialog != null) windowAsDialog.setJMenuBar(menuBar);
        if (windowAsFrame != null) windowAsFrame.setJMenuBar(menuBar);
	}

	public static void openURL(String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac")) {// Mac OS
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
				openURL.invoke(null, url);
			} else if (osName.startsWith("Windows")) {// Windows
				Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
			} else { // Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
				String browser = null;
				for (int count = 0; count < browsers.length && browser == null; count++) {
					if (Runtime.getRuntime().exec(
							new String[] { "which", browsers[count] })
							.waitFor() == 0) {
						browser = browsers[count];
					}
				}
				if (browser == null) {
					throw new Exception("Could not find web browser");
				} else {
					Runtime.getRuntime().exec(new String[] { browser, url });
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void updateTrayLabel() {
		if (trayIcon == null) return;

		String label = "Status:Disconnected";
		if (connected) label = "Status:Connected" + " InternetPlan:" + plan_name;
        
		trayIcon.setToolTip(label);

        if (connected) {
            trayIcon.setImage(iconConnected);
        } else {
            trayIcon.setImage(iconDisconnected);
        }
        
	}

	private void initTrayIcon() {
        if (!isSystemTraySupported()) return;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        iconDefault = toolkit.getImage(this.getClass().getResource("StatusIcon.png"));
        iconConnected = toolkit.getImage(this.getClass().getResource("StatusIconConnected.png"));
        iconConnecting = toolkit.getImage(this.getClass().getResource("StatusIconConnecting.png"));
        iconDisconnected = toolkit.getImage(this.getClass().getResource("StatusIconDisconnected.png"));

		PopupMenu popup = new PopupMenu();
		
		MenuItem helpItem = new MenuItem("Help");
		ActionListener helpListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openURL(helpURL);
			}
		};
		helpItem.addActionListener(helpListener);
		
		MenuItem rateItem = new MenuItem("Show Charge Rates");
		ActionListener rateListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openURL(helpURL);
			}
		};
		rateItem.addActionListener(rateListener);
		
		MenuItem passwordItem = new MenuItem("Change Password");
		ActionListener passwordListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openURL(passwdChangeURL);
			}
		};
		passwordItem.addActionListener(passwordListener);
		
		MenuItem openItem = new MenuItem("Open JNetLogin");
		ActionListener showListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openWindow();
			}
		};
		openItem.addActionListener(showListener);
	
		MenuItem exitItem = new MenuItem("Exit");
		ActionListener exitListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		exitItem.addActionListener(exitListener);
		
		popup.add(helpItem);
		popup.addSeparator();
		popup.add(rateItem);
		popup.add(passwordItem);
		popup.addSeparator();
		popup.add(openItem);
		popup.add(exitItem);
		
		trayIcon = new TrayIcon(iconDefault, "NetLogin", popup);
		trayIcon.setImageAutoSize(false);
		trayIcon.setToolTip("NetLogin");
		trayIcon.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				openWindow();
			}
		});
				
		SystemTray tray = SystemTray.getSystemTray();
		try {
			tray.add(trayIcon); 
	   } catch (Exception e) {
			System.err.println("Unable to add system tray: " + e.getMessage());
		}
	}

}
