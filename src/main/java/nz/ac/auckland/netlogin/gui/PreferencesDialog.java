package nz.ac.auckland.netlogin.gui;

import nz.ac.auckland.netlogin.NetLoginPreferences;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.beans.EventHandler;

public class PreferencesDialog {

	private NetLoginPreferences preferences;
	private JDialog dialog;
//	private JTextField userText;
//	private JTextField passwordText;
//	private JButton loginButton;
//	private JButton cancelButton;
//	private JCheckBox rememberMeCheckbox;
//	private JLabel userLabel;
//	private JLabel passwordLabel;

	public PreferencesDialog(NetLoginPreferences preferences) {
		this.preferences = preferences;
		createComponents();
		registerEvents();
		layout();
	}

	public void open() {
		dialog.setVisible(true);
	}

	public void close() {
		dialog.setVisible(false);
	}

	public void done() {
		save();
		close();
	}

	public void save() {
//				p.put("useStaticPingPort", useStaticPingPortCB.isSelected() ? "true" : "false");
//				p.put("useAltServer", altServerCB.isSelected() ? "true" : "false");
//				p.put("altServer", altServerTF.getText());
	}

	protected void createComponents() {
		showPreferencesDialog();
	}

	protected void registerEvents() {
	}

	protected void layout() {
	}

	public void showPreferencesDialog() {
		dialog = new JDialog();
		dialog.setResizable(false);

		JPanel panel = new JPanel();
		GridBagLayout gbl = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		panel.setLayout( gbl );
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.insets = new Insets( 1, 1, 1, 1 );

		final JCheckBox altServerCB = new JCheckBox("Alternate server", preferences.getUseAltServer());
		final JTextField altServerTF = new JTextField(preferences.getAltServer());
		final JCheckBox useStaticPingPortCB = new JCheckBox("Static ping port", preferences.getUseStaticPingPort());
		
		altServerCB.addChangeListener( new ChangeListener() {
			public void stateChanged( ChangeEvent e ){
				boolean useAltServer = ((JCheckBox)e.getSource()).isSelected();
				altServerTF.setEnabled(useAltServer);
			}
		});
        altServerTF.setEnabled(altServerCB.isSelected());
		addExternal(panel, gbc, 0, 0, altServerCB, GridBagConstraints.NONE, GridBagConstraints.WEST);
		gbc.weightx = 7.0;
		addExternal( panel, gbc, 1, 0, altServerTF, GridBagConstraints.HORIZONTAL, GridBagConstraints.WEST );
		gbc.weightx = 1.0;
		addExternal( panel, gbc, 0, 1, useStaticPingPortCB, GridBagConstraints.NONE, GridBagConstraints.WEST );

		JButton okB = new JButton("OK");
		okB.addActionListener(EventHandler.create(ActionListener.class, this, "done"));
		addExternal( panel, gbc, 0, 2, okB, GridBagConstraints.NONE, GridBagConstraints.EAST );

		JButton cancelB = new JButton("Cancel");
		cancelB.addActionListener(EventHandler.create(ActionListener.class, this, "close"));
		addExternal( panel, gbc, 1, 2, cancelB, GridBagConstraints.NONE, GridBagConstraints.EAST );

		dialog.setContentPane(panel);
		dialog.setBounds(100, 100, 300, 130);
		dialog.setTitle("NetLogin - Preferences");
        dialog.setLocationRelativeTo(null);
	}

	private void addExternal( JPanel panel, GridBagConstraints constraints, int x, int y,
		JComponent c, int fill, int anchor ) {
		constraints.gridx = x;
		constraints.gridy = y;
		constraints.fill = fill;
		constraints.anchor = anchor;
		panel.add(c,constraints);
	}

}
