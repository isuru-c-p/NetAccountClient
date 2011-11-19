import nz.ac.auckland.netlogin.gui.LoginDialog;

import java.awt.*;
import java.beans.EventHandler;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.lang.reflect.Method;
import java.awt.Window;
import static java.awt.GridBagConstraints.VERTICAL;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.WEST;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.NONE;

public class NetLoginGUI extends JPanel implements PingListener {

	private String username;

	private Window window;
    private JFrame windowAsFrame;
    private JDialog windowAsDialog;

	private JLabel upiTitle = new JLabel("NetID/UPI:  ");
	private JLabel planTitle = new JLabel("Internet Plan:  ");
	private JLabel usageTitle = new JLabel("MBs used this month:  ");
	private JLabel statusLabel = new JLabel("Not Connected");
	private JLabel planLabel = new JLabel("");
	private JLabel usageLabel = new JLabel("");

	private NetLoginPreferences p = new NetLoginPreferences();
	private JButton connectButton = new JButton("Connect...");

	private JMenuItem loginMenuItem;
	private JMenuItem logoutMenuItem;
	private JMenuItem changePassMenuItem;

	private LoginDialog loginDialog;

	private NetLoginConnection netLoginConnection = null;
	private boolean connected = false;

	private final Color labelColor = new Color(51, 102, 255);
    
	private boolean useSystemTray = true;
	private TrayIcon trayIcon;
	private String planName = "";
	
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
		loadLookAndFeel();
		initBody();
		createWindow();
        initTrayIcon();
		openWindow();
	}

	public NetLoginGUI(String upi, String password) {
		loadLookAndFeel();
		initBody();
		createWindow();
		login(upi, password);
        initTrayIcon();
		minimizeWindow();
	}

	private void loadLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// ignore - continue with the default look and feel
		}
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
		this.username = upi; // for displaying in the user interface
		netLoginConnection = new NetLoginConnection(this);
		netLoginConnection.setUseStaticPingPort(p.getUseStaticPingPort());
		try {
			netLoginConnection.login(upi, password);
		} catch (IOException ex) {
			showError(ex.getMessage());
		}
		statusLabel.setText(upi);
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

		loginDialog = new LoginDialog();
		loginDialog.addLoginListener(new LoginDialog.LoginListener() {
			public void login(String username, String password) {
				netLoginConnection.setUseStaticPingPort(p.getUseStaticPingPort());
				try {
					if (p.getUseAltServer()) {
						netLoginConnection.login(p.getAltServer(), username, password);
					} else {
						netLoginConnection.login(username, password);
					}
				} catch (IOException ex) {
					showError(ex.getMessage());
				}
			}
		});

		mainPanel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.setLayout(gbl);
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.anchor = CENTER;

		upiTitle.setForeground(labelColor);
		planTitle.setForeground(labelColor);
		usageTitle.setForeground(labelColor);

		connectButton.setToolTipText("Login to NetAccount");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!connected) {
					loginDialog.open();
                } else {
					disconnect();
                }
			}
		});

		addExternal(mainPanel, gbc, 0, 0, upiTitle, VERTICAL, EAST);
		addExternal(mainPanel, gbc, 0, 1, planTitle, VERTICAL, EAST);
		addExternal(mainPanel, gbc, 0, 2, usageTitle, VERTICAL, EAST);
		addExternal(mainPanel, gbc, 1, 0, statusLabel, VERTICAL, WEST);
		addExternal(mainPanel, gbc, 1, 1, planLabel, VERTICAL, WEST);
		addExternal(mainPanel, gbc, 1, 2, usageLabel, VERTICAL, WEST);
		addExternal(mainPanel, gbc, 0, 3, new JSeparator(), HORIZONTAL, CENTER);
		addExternal(mainPanel, gbc, 1, 3, new JSeparator(), HORIZONTAL, CENTER);
		addExternal(mainPanel, gbc, 1, 4, connectButton, NONE, CENTER);
	}

	private void addExternal(JPanel panel, GridBagConstraints constraints, int x, int y, JComponent c, int fill, int anchor) {
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
		logoutMenuItem.setEnabled(false);
	}

	public void connected(int ipUsage, int planFlags) {
		this.connected = true;
		statusLabel.setText(username);
		connectButton.setToolTipText("Disconnect from NetAccount");
		connectButton.setText("Disconnect");
		loginMenuItem.setEnabled(false);
		logoutMenuItem.setEnabled(true);

		update(ipUsage, planFlags, null);
	}

	public void disconnected() {
		disconnect();
		updateTrayLabel();
	}

	public void update(int ip_usage, int planFlags, String message) {
		float MBs_usage = (float) (Math.round((ip_usage / 1024.0) * 100)) / 100;
		usageLabel.setText("" + MBs_usage + "MBs");

		planFlags = planFlags & 0x0F000000;
		switch (planFlags) {
		case 0x01000000: // STATUS_UNLIMITED: =>Staff
			planName = "Staff";
			break;
		case 0x02000000: // STATUS_SPONSORED:
			planName = "Sponsored";
			break;
		case 0x03000000: // STATUS_PREMIUM: =>undergraduate
			planName = "Undergraduate";
			break;
		case 0x04000000: // STATUS_STANDARD: => exceeded
			planName = "ExceededAllowance";
			break;
		case 0x05000000: // STATUS_NOACCESS:
			planName = "NoAccess";
			break;
		case 0x06000000: // STATUS_PostGraduate:
			planName = "Postgraduate";
			break;
		default:
			planName = "";
		}
		
		planLabel.setText(planName);
		updateTrayLabel();
	}

	public void showAbout() {
		JOptionPane.showMessageDialog(this,aboutInfo);
	}

	private void showError(String errorMsg) {
		JOptionPane.showMessageDialog(this, "NetLogin - " + errorMsg);
		disconnect();
	}

	private void makeMenuBar() {
		loginMenuItem = new JMenuItem("Login", 'l');
		logoutMenuItem = new JMenuItem("Logout", 'l');
		logoutMenuItem.setEnabled(false);
		changePassMenuItem = new JMenuItem("Change Password", 'p');
		JMenuItem preferencesMenuItem = new JMenuItem("Preferences", 'r');
		JMenuItem quitMenuItem = new JMenuItem("Exit", 'x');
		JMenuItem aboutMenuItem = new JMenuItem("About", 'a');
		JMenuItem chargeRatesMenuItem = new JMenuItem("Show Charge Rates", 'c');

		JMenu netLoginMenu = new JMenu("NetLogin");
		netLoginMenu.setMnemonic('n');
		netLoginMenu.add(loginMenuItem);
		netLoginMenu.add(logoutMenuItem);
		netLoginMenu.addSeparator();
		netLoginMenu.add(preferencesMenuItem);
		netLoginMenu.addSeparator();
		netLoginMenu.add(quitMenuItem);

		JMenu servicesMenu = new JMenu("Services");
		servicesMenu.setMnemonic('s');
		servicesMenu.add(changePassMenuItem);
		servicesMenu.add(chargeRatesMenuItem);

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic('h');
		helpMenu.add(aboutMenuItem);

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(netLoginMenu);
		menuBar.add(servicesMenu);
		menuBar.add(helpMenu);

		loginMenuItem.addActionListener(EventHandler.create(ActionListener.class, loginDialog, "open"));
		logoutMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "disconnect"));
		preferencesMenuItem.addActionListener(EventHandler.create(ActionListener.class, p, "showPreferencesDialog"));
		changePassMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "changePassword"));
		quitMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "quit"));
		aboutMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "showAbout"));
		chargeRatesMenuItem.addActionListener(EventHandler.create(ActionListener.class, p, "showChargeRates"));

        if (windowAsDialog != null) windowAsDialog.setJMenuBar(menuBar);
        if (windowAsFrame != null) windowAsFrame.setJMenuBar(menuBar);
	}

	public void showChargeRates() {
		openURL(helpURL);
	}

	public void changePassword() {
		openURL(passwdChangeURL);
	}

	public void quit() {
		System.exit(0);
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
		if (connected) label = "Status:Connected" + " InternetPlan:" + planName;
        
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
		
		MenuItem rateItem = new MenuItem("Show Charge Rates");
		MenuItem passwordItem = new MenuItem("Change Password");
		MenuItem openItem = new MenuItem("Open NetLogin");
		MenuItem exitItem = new MenuItem("Exit");

		rateItem.addActionListener(EventHandler.create(ActionListener.class, this, "showChargeRates"));
		passwordItem.addActionListener(EventHandler.create(ActionListener.class, this, "changePassword"));
		openItem.addActionListener(EventHandler.create(ActionListener.class, this, "openWindow"));
		exitItem.addActionListener(EventHandler.create(ActionListener.class, this, "quit"));
		
		popup.add(passwordItem);
		popup.add(rateItem);
		popup.addSeparator();
		popup.add(openItem);
		popup.add(exitItem);
		
		trayIcon = new TrayIcon(iconDefault, "NetLogin", popup);
		trayIcon.setImageAutoSize(false);
		trayIcon.setToolTip("NetLogin");

		// only open if the primary button was pressed, so we don't conflict with the context menu
		trayIcon.addMouseListener(new MouseInputAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) openWindow();
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
