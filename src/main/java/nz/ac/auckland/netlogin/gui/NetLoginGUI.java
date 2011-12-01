package nz.ac.auckland.netlogin.gui;

import nz.ac.auckland.netlogin.NetLoginConnection;
import nz.ac.auckland.netlogin.NetLoginPlan;
import nz.ac.auckland.netlogin.NetLoginPreferences;
import nz.ac.auckland.netlogin.PingListener;
import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import nz.ac.auckland.netlogin.negotiation.PopulatedCredentialsCallback;
import nz.ac.auckland.netlogin.util.SpringUtilities;
import nz.ac.auckland.netlogin.util.SystemSettings;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.*;
import java.beans.EventHandler;
import java.lang.reflect.Method;

public class NetLoginGUI extends JPanel implements PingListener {

	private JFrame window;

	private JLabel userLabel;
	private JLabel planLabel;
	private JLabel usageLabel;

	private NetLoginPreferences preferences;

	private JButton connectButton;
	private JMenuItem loginMenuItem;
	private JMenuItem logoutMenuItem;

	private LoginDialog loginDialog;
	private AboutDialog aboutDialog;
	private PreferencesDialog preferencesDialog;

	private NetLoginConnection netLoginConnection;
	private boolean connected = false;

	private boolean useSystemTray = true;
	private TrayIcon trayIcon;

	static String helpURL = "http://www.ec.auckland.ac.nz/docs/net-student.htm";
	static String passwdChangeURL = "https://iam.auckland.ac.nz/password/change";

    private JPanel mainPanel;
	private JPanel bodyPanel;

    public NetLoginGUI() {
		initialize();
		openWindow();
		netLoginConnection.monitor();
	}

	public NetLoginGUI(String upi, String password) {
		initialize();
		login(new PopulatedCredentialsCallback(upi, password));
		minimizeWindow();
		netLoginConnection.monitor();
	}

	private void initialize() {
		preferences = NetLoginPreferences.getInstance();
		netLoginConnection = new NetLoginConnection(this);

		loadLookAndFeel();
		initBody();
		createWindow();
		initTrayIcon();
	}

	private void loadLookAndFeel() {
		try {
            SystemSettings.setSystemPropertyDefault("apple.laf.useScreenMenuBar", "true");
            SystemSettings.setSystemPropertyDefault("com.apple.macos.useScreenMenuBar", "true"); // historical
            SystemSettings.setSystemPropertyDefault("com.apple.mrj.application.apple.menu.about.name", "NetLogin");
            SystemSettings.setSystemPropertyDefault("com.apple.mrj.application.live-resize", "true");

			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// ignore - continue with the default look and feel
		}
	}

	public void createWindow() {
        window = new JFrame("NetLogin");
        window.setContentPane(mainPanel);
        window.setResizable(false);
        window.setJMenuBar(createMenuBar());

        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                minimizeWindow();
            }
        });

		window.setBounds(12, 12, 270, 160);
		window.setLocationRelativeTo(null);
		window.setIconImages(Icons.getInstance().getWindowIcons());
	}
	
	public void login() {
		login(loginDialog);
	}

	protected void login(CredentialsCallback callback) {
		netLoginConnection.login(callback);
	}

	public boolean isSystemTraySupported() {
		return useSystemTray && SystemTray.isSupported();
	}
	
	public void openWindow() {
		window.setExtendedState(Frame.NORMAL);
		window.setVisible(true);
		window.toFront();
	}
	
	public void minimizeWindow() {
		if (isSystemTraySupported()) {
			window.setVisible(false);
		} else {
			window.setExtendedState(Frame.ICONIFIED);
		}
	}

	public void initBody() {
		loginDialog = new LoginDialog();
		preferencesDialog = new PreferencesDialog(preferences);
		aboutDialog = new AboutDialog();

		JLabel upiTitle = new JLabel("NetID/UPI:", JLabel.RIGHT);
		JLabel planTitle = new JLabel("Internet Plan:", JLabel.RIGHT);
		JLabel usageTitle = new JLabel("Used this month:", JLabel.RIGHT);
		userLabel = new JLabel("Not Connected");
		planLabel = new JLabel();
		usageLabel = new JLabel();

		Font labelFont = upiTitle.getFont();
		Font valueFont = upiTitle.getFont().deriveFont(Font.BOLD);
		Color labelColor = Color.BLACK;
		Color valueColor = Color.BLACK; //new Color(51, 102, 255);

		userLabel.setFont(valueFont);
		planLabel.setFont(valueFont);
		usageLabel.setFont(valueFont);

		userLabel.setForeground(valueColor);
		planLabel.setForeground(valueColor);
		usageLabel.setForeground(valueColor);

		upiTitle.setFont(labelFont);
		planTitle.setFont(labelFont);
		usageTitle.setFont(labelFont);

		upiTitle.setForeground(labelColor);
		planTitle.setForeground(labelColor);
		usageTitle.setForeground(labelColor);

		connectButton = new JButton("Connect");
		connectButton.setToolTipText("Login to NetAccount");
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!connected) {
					login();
					netLoginConnection.monitor();
                } else {
					netLoginConnection.unmonitor();
					disconnect();
                }
			}
		});

		JPanel connectedPanel = new JPanel(new SpringLayout());
		connectedPanel.add(upiTitle);
		connectedPanel.add(userLabel);
		connectedPanel.add(planTitle);
		connectedPanel.add(planLabel);
		connectedPanel.add(usageTitle);
		connectedPanel.add(usageLabel);
		SpringUtilities.makeCompactGrid(connectedPanel, 3, 2, 5, 5, 5, 5);

		JPanel disconnectedPanel = createMessagePanel("Not Connected", valueFont, valueColor, null);

		JLabel connectingLabel = new JLabel("Connecting...");
		connectingLabel.setFont(valueFont);
		connectingLabel.setForeground(valueColor);

        JProgressBar connectingProgress = new JProgressBar();
        connectingProgress.setIndeterminate(true);

		JComponent connectingPanel = alignVertically(createFlow(connectingLabel), createFlow(connectingProgress));

		bodyPanel = new JPanel(new CardLayout());
		bodyPanel.add("disconnected", alignVertically(disconnectedPanel));
		bodyPanel.add("connecting", alignVertically(connectingPanel));
		bodyPanel.add("connected", alignHorizontally(connectedPanel));

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 7));
		buttonPanel.add(connectButton);

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(bodyPanel);
		mainPanel.add(new JSeparator());
		mainPanel.add(buttonPanel);
	}

    private JPanel createFlow(JComponent component) {
        JPanel flowPanel = new JPanel(new FlowLayout());
        flowPanel.add(component);
        return flowPanel;
    }

	private JPanel createMessagePanel(String text, Font font, Color color, Image image) {
		JLabel messageLabel = new JLabel(text);
		messageLabel.setFont(font);
		messageLabel.setForeground(color);
		if (image != null) messageLabel.setIcon(new ImageIcon(image));
		JPanel messagePanel = new JPanel(new FlowLayout());
		messagePanel.add(messageLabel);
		return messagePanel;
	}

	private JComponent alignHorizontally(JComponent... components) {
		Box connectingPanel = Box.createHorizontalBox();
		connectingPanel.add(Box.createHorizontalGlue());
        for(JComponent component : components) {
            connectingPanel.add(component);
        }
		connectingPanel.add(Box.createHorizontalGlue());
		return connectingPanel;
	}

	private JComponent alignVertically(JComponent... components) {
		Box connectingPanel = Box.createVerticalBox();
		connectingPanel.add(Box.createVerticalGlue());
        for(JComponent component : components) {
            connectingPanel.add(component);
        }
		connectingPanel.add(Box.createVerticalGlue());
		return connectingPanel;
	}

	private void disconnect() {
		netLoginConnection.logout();
	}

	public void connecting() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connectButton.setEnabled(false);
				((CardLayout)bodyPanel.getLayout()).show(bodyPanel, "connecting");
			}
		});
	}

	public void connectionFailed(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connectButton.setEnabled(true);
				((CardLayout)bodyPanel.getLayout()).show(bodyPanel, "disconnected");
				if (message != null) JOptionPane.showMessageDialog(NetLoginGUI.this, "NetLogin - " + message);
			}
		});
	}

	public void connected(final String username, int ipUsage, NetLoginPlan plan) {
		this.connected = true;
		update(ipUsage, plan);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				userLabel.setText(username);
				((CardLayout)bodyPanel.getLayout()).show(bodyPanel, "connected");

				connectButton.setToolTipText("Disconnect from NetAccount");
				connectButton.setText("Disconnect");
				connectButton.setEnabled(true);
				
				loginMenuItem.setEnabled(false);
				logoutMenuItem.setEnabled(true);
				updateTrayLabel();
			}
		});
	}

	public void disconnected() {
		this.connected = false;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				connectButton.setText("Connect");
				((CardLayout)bodyPanel.getLayout()).show(bodyPanel, "disconnected");

				loginMenuItem.setEnabled(true);
				logoutMenuItem.setEnabled(false);
				updateTrayLabel();
			}
		});
	}

	public void update(final int ipUsage, final NetLoginPlan plan) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				float ipUsageMb = (float) (Math.round((ipUsage / 1024.0) * 100)) / 100;
				usageLabel.setText("" + ipUsageMb + " MBs");
				usageLabel.invalidate();
				planLabel.setText(plan.toString());
				planLabel.invalidate();
				planLabel.getParent().validate();
			}
		});
	}

	private JMenuBar createMenuBar() {
		loginMenuItem = new JMenuItem("Login", 'l');
		logoutMenuItem = new JMenuItem("Logout", 'l');
		logoutMenuItem.setEnabled(false);
		JMenuItem changePassMenuItem = new JMenuItem("Change Password", 'p');
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

		loginMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "login"));
		logoutMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "disconnect"));
		preferencesMenuItem.addActionListener(EventHandler.create(ActionListener.class, preferencesDialog, "open"));
		changePassMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "changePassword"));
		quitMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "quit"));
		aboutMenuItem.addActionListener(EventHandler.create(ActionListener.class, aboutDialog, "open"));
		chargeRatesMenuItem.addActionListener(EventHandler.create(ActionListener.class, this, "showChargeRates"));

		return menuBar;
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

		String label = "Disconnected";
		if (connected) label = "Connected";
		trayIcon.setToolTip(label);

        if (connected) {
            trayIcon.setImage(Icons.getInstance().getConnectedIcon());
        } else {
            trayIcon.setImage(Icons.getInstance().getDisconnectedIcon());
        }
	}

	private void initTrayIcon() {
        if (!isSystemTraySupported()) return;
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
		
		trayIcon = new TrayIcon(Icons.getInstance().getDefaultIcon(), "NetLogin", popup);
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
