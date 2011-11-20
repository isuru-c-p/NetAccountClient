package nz.ac.auckland.netlogin.gui;

import nz.ac.auckland.netlogin.NetLoginPreferences;
import nz.ac.auckland.netlogin.negotiation.AuthenticatorFactory;
import nz.ac.auckland.netlogin.util.SpringUtilities;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.util.Vector;

public class PreferencesDialog {

	private NetLoginPreferences preferences;
	private JDialog dialog;
	private JButton okButton;
	private JButton cancelButton;
	private JLabel credentialSourceLabel;
	private JComboBox credentialSourceCombo;
	private JLabel serverLabel;
	private JTextField serverText;

	public PreferencesDialog(NetLoginPreferences preferences) {
		this.preferences = preferences;
		createComponents();
		registerEvents();
		layout();
	}

	public void open() {
		read();
		dialog.setVisible(true);
	}

	public void close() {
		dialog.setVisible(false);
	}

	public void done() {
		save();
		close();
	}

	public void read() {
		serverText.setText(preferences.getServer());
		credentialSourceCombo.setSelectedItem(preferences.getCredentialSource());
	}

	public void save() {
		preferences.setServer(serverText.getText());
		preferences.setCredentialSource((String)credentialSourceCombo.getSelectedItem());
		preferences.savePreferences();
	}

	protected void createComponents() {
		dialog = new JDialog();
		dialog.setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
		dialog.setResizable(false);
		dialog.setTitle("NetLogin - Preferences");

		okButton = new JButton("Ok");
		okButton.setDefaultCapable(true);

		cancelButton = new JButton("Cancel");

        Vector<String> credentialSources = new Vector<String>(AuthenticatorFactory.getInstance().getNames());

		credentialSourceLabel = new JLabel("Credentials:", JLabel.TRAILING);
		credentialSourceCombo = new JComboBox(credentialSources);

		serverLabel = new JLabel("Server:", JLabel.TRAILING);
		serverText = new JTextField(20);
	}

	protected void registerEvents() {
		okButton.addActionListener(EventHandler.create(ActionListener.class, this, "done"));
		cancelButton.addActionListener(EventHandler.create(ActionListener.class, this, "close"));
	}

	protected void layout() {
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);

		JPanel formPanel = new JPanel(new SpringLayout());
		formPanel.add(serverLabel);
		formPanel.add(serverText);
		formPanel.add(credentialSourceLabel);
		formPanel.add(credentialSourceCombo);
		SpringUtilities.makeCompactGrid(formPanel, 2, 2, 5, 5, 5, 5);

		Box bodyPanel = Box.createVerticalBox();
		bodyPanel.add(formPanel);
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

}
