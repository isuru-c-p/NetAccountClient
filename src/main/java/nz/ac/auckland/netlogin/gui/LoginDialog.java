package nz.ac.auckland.netlogin.gui;

import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import nz.ac.auckland.netlogin.util.SpringUtilities;
import javax.swing.*;
import javax.swing.event.CaretListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.beans.EventHandler;
import java.util.EventListener;

public class LoginDialog implements CredentialsCallback {

	private boolean valid = false;
	private JDialog dialog;
	private JTextField userText;
	private JTextField passwordText;
	private JButton loginButton;
	private JButton cancelButton;
	private JCheckBox rememberMeCheckbox;
	private JLabel userLabel;
	private JLabel passwordLabel;

	public LoginDialog() {
		createComponents();
		registerEvents();
		layout();
	}

	protected void layout() {
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(loginButton);
		buttonPanel.add(cancelButton);

		JPanel formPanel = new JPanel(new SpringLayout());
		formPanel.add(userLabel);
		formPanel.add(userText);
		formPanel.add(passwordLabel);
		formPanel.add(passwordText);
		SpringUtilities.makeCompactGrid(formPanel, 2, 2, 5, 5, 5, 5);

		JPanel rememberPanel = new JPanel(new FlowLayout());
		rememberPanel.add(rememberMeCheckbox);

		Box bodyPanel = Box.createVerticalBox();
		bodyPanel.add(formPanel);
		// todo: enable remember upi panel - bodyPanel.add(rememberPanel);
		bodyPanel.add(buttonPanel);

		JPanel marginPanel = new JPanel(new BorderLayout());
		marginPanel.add(bodyPanel, BorderLayout.CENTER);
		marginPanel.add(Box.createVerticalStrut(10), BorderLayout.NORTH);
		marginPanel.add(Box.createVerticalStrut(10), BorderLayout.SOUTH);
		marginPanel.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
		marginPanel.add(Box.createHorizontalStrut(20), BorderLayout.EAST);

		dialog.setContentPane(marginPanel);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
	}

	protected void registerEvents() {
		// clear the retrievePassword dialog when the window closes
		dialog.addWindowListener(EventHandler.create(WindowListener.class, this, "clearPassword"));

		// enable the login button if the username and retrievePassword are supplied
		CaretListener detailsValidator = EventHandler.create(CaretListener.class, this, "validateDetails");
		userText.addCaretListener(detailsValidator);
		passwordText.addCaretListener(detailsValidator);

		// process the login event
		ActionListener loginAction = EventHandler.create(ActionListener.class, this, "login");
		loginButton.addActionListener(loginAction);
		passwordText.addActionListener(loginAction);

		// close the dialog
		cancelButton.addActionListener(EventHandler.create(ActionListener.class, this, "cancel"));
	}

	protected void createComponents() {
		dialog = new JDialog();
		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setTitle("NetLogin - Login");
		dialog.setVisible(false);
		dialog.setResizable(false);

		userText = new JTextField(20);
		passwordText = new JPasswordField(20);

		loginButton = new JButton("Connect");
		loginButton.setEnabled(false);
		loginButton.setDefaultCapable(true);

		cancelButton = new JButton("Cancel");

		rememberMeCheckbox = new JCheckBox("Remember NetID/UPI");

		userLabel = new JLabel("NetID/UPI:", JLabel.TRAILING);
		passwordLabel = new JLabel("Password:", JLabel.TRAILING);
	}

	public void open() {
		valid = false;
		clearPassword();
		dialog.setVisible(true);
	}

	public void close() {
		dialog.setVisible(false);
	}

	public void clearPassword() {
		passwordText.setText("");
	}

	public void validateDetails() {
		if (!passwordText.getText().equals("") && !userText.getText().equals("")) {
			loginButton.setEnabled(true);
		} else {
			loginButton.setEnabled(false);
		}
	}

	public void login() {
		valid = true;
		close();
	}

	public void cancel() {
		clearPassword();
		close();
	}

	public boolean requestCredentials() {
		open();

		// wait until the dialog is closed
		while (dialog.isVisible()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore and continue
			}
		}

		return valid;
	}

	public String getUsername() {
		return userText.getText();
	}

	public String retrievePassword() {
		String password = passwordText.getText();
		clearPassword();
		return password;
	}

	public static interface LoginListener extends EventListener {
		public void login(String username, String password);
	}

}
