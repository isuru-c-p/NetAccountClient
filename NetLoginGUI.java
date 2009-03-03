import java.awt.*;
import java.io.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Method;

public class NetLoginGUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JLabel upititle = new JLabel("UPI:  ");
	private JLabel plantitle = new JLabel("Internet Plan:  ");
	private JLabel usagetitle = new JLabel("MBs used this month:  ");
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

	static String helpURL = "http://ec.auckland.ac.nz/net.htm";
	static String passwdChangeURL = "https://admin.ec.auckland.ac.nz/Passwd/";

	private TrayIcon trayIcon;
	private String plan_name = "";

	public NetLoginGUI() {
		// super("JNetLogin");
		NetLoginGUIBody();
	}

	// No makeLoginDialog(), directly login
	public NetLoginGUI(String upi, String password) {
		NetLoginGUIBody();
		netLoginConnection = new NetLoginConnection(this);
		netLoginConnection.setUseStaticPingPort(p.getUseStaticPingPort());
		try {
			netLoginConnection.login(upi, password);
			loginDialog.setVisible(false);
		} catch (IOException ex) {
			showError(ex.getMessage());
			// System.exit(0);
		}
		statusLabel.setText(upi);
		loginTF.setText(upi);
	}

	public void NetLoginGUIBody() {
		// super("JNetLogin");
		if (netLoginConnection == null)
			netLoginConnection = new NetLoginConnection(this);
		makeLoginDialog();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(gbl);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.CENTER;

		upititle.setFont(globalTitleFont);
		plantitle.setFont(globalTitleFont);
		usagetitle.setFont(globalTitleFont);
		upititle.setForeground(globalTitleColor);
		plantitle.setForeground(globalTitleColor);
		usagetitle.setForeground(globalTitleColor);

		statusLabel.setFont(globalFont);
		planLabel.setFont(globalFont);
		usageLabel.setFont(globalFont);

		connectButton.setFont(globalTitleFont);
		connectButton.setForeground(globalTitleColor);
		connectButton.setToolTipText("Login to NetAccount");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!connected)
					loginDialog.setVisible(true);
				else
					disconnect();
			}
		});

		addExternal(mainPanel, gbc, 0, 0, upititle,
				GridBagConstraints.VERTICAL, GridBagConstraints.EAST);
		addExternal(mainPanel, gbc, 0, 1, plantitle,
				GridBagConstraints.VERTICAL, GridBagConstraints.EAST);
		addExternal(mainPanel, gbc, 0, 2, usagetitle,
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

		makeMenuBar();
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
					savePreferences();
					System.exit(0);
			}
			public void windowIconified(WindowEvent e) {
				if (SystemTray.isSupported()) {
					setVisible(false);
					minimizeToTray();
				}
	        }
		});

		setContentPane(mainPanel);
		setBounds(p.getMainDialogBounds());
		setBounds(12, 12, 270, 160);
		setVisible(true);
		setLocationRelativeTo(null);
		setResizable(false);
		//pack();
		setIconImage(new ImageIcon("jnetlogin.gif").getImage());
		// init tray Icon()
		initTrayIcon(); 

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
		changePWMenuItem.setEnabled(false);
	}

	private void savePreferences() {
		p.setMainDialogBounds(getBounds());
		p.setLoginDialogBounds(loginDialog.getBounds());
		p.savePreferences();
	}

	public void update(int balance, boolean onPeak, boolean connected,
			String message) {
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
			changePWMenuItem.setEnabled(true);
			loginMenuItem.setEnabled(false);
		} else {
			disconnect(); // to make sure
		}
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
			case 0x01000000: // STATUS_UNLIMITED:
				plan_name = "Unlimited";
				break;
			case 0x02000000: // STATUS_SPONSORED:
				plan_name = "Sponsored";
				break;
			case 0x03000000: // STATUS_PREMIUM:
				plan_name = "Premium";
				break;
			case 0x04000000: // STATUS_STANDARD:
				plan_name = "Standard";
				break;
			case 0x05000000: // STATUS_NOACCESS:
				plan_name = "No Access";
				break;
			default:
				plan_name = "";
			}
			planLabel.setText(plan_name);
			statusLabel.setText(loginTF.getText());
			connectButton.setToolTipText("Disconnect from NetAccount");
			connectButton.setText("Disconnect");
			changePWMenuItem.setEnabled(true);
			loginMenuItem.setEnabled(false);
		} else {
			disconnect(); // to make sure
		}
	}

	public void showAbout() {
		JOptionPane.showMessageDialog(this, "JNetLogin Client Version 3.0.1\n"
				+ "Copyright(C) 2001-2009 The University of Auckland.\n"
				+ "Release under terms of the GNU GPL. \n");
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
					if (p.getUseAltServer())
						netLoginConnection.login(p.getAltServer(), loginTF
								.getText(), passwordTF.getText());
					else
						netLoginConnection.login(loginTF.getText(), passwordTF
								.getText());
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
					if (p.getUseAltServer())
						netLoginConnection.login(p.getAltServer(), loginTF
								.getText(), passwordTF.getText());
					else
						netLoginConnection.login(loginTF.getText(), passwordTF
								.getText());
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
		// loginDialog.setVisible(true);
		loginDialog.setVisible(false);
		loginDialog.setBounds(p.getLoginDialogBounds());
		loginDialog.setLocationRelativeTo(null);
		loginDialog.setResizable(false);
		loginDialog.setIconImage(new ImageIcon("jnetlogin.gif").getImage());
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
				loginDialog.setVisible(true);
				loginDialog.setBounds(p.getLoginDialogBounds());
				loginDialog.setLocationRelativeTo(null);
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
		menuItem.setEnabled(false);
		changePWMenuItem = menuItem;
		netLoginMenu.add(menuItem);
		netLoginMenu.addSeparator();

		menuItem = new JMenuItem("Quit");
		menuItem.setFont(globalFont);
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				savePreferences();
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
		setJMenuBar(menuBar);
	}

	public static void openURL(String url) {
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac")) {// Mac OS
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] { String.class });
				openURL.invoke(null, new Object[] { url });
			} else if (osName.startsWith("Windows")) {// Windows
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			} else { // Unix or Linux
				String[] browsers = { "firefox", "opera", "konqueror",
						"epiphany", "mozilla", "netscape" };
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

	public void minimizeToTray() {
		String label="Status:Discounnted";
		if (connected) label="Status:Connected"+" InternetPlan:"+plan_name;
		SystemTray tray = SystemTray.getSystemTray(); 
		try{
	     tray.add(trayIcon); 
	     trayIcon.setToolTip(label);
	   }catch(Exception e)
		{
			System.out.println("add trayIcon error"+e);
		}
	}

	private void initTrayIcon() {
		Image image = Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("jnetlogin.gif"));
		PopupMenu popup = new PopupMenu();
		
		 MouseListener mouseListener = new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				SystemTray.getSystemTray().remove(trayIcon);
				setState(Frame.NORMAL); 
				setVisible(true);
				toFront();               
             }
			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}
         };
		
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
				SystemTray.getSystemTray().remove(trayIcon);
				setState(Frame.NORMAL); 
				setVisible(true);
				toFront();
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
		
		trayIcon = new TrayIcon(image, "MyIcon", popup);
		trayIcon.setImageAutoSize(true);
		trayIcon.addMouseListener(mouseListener);
	}

}
