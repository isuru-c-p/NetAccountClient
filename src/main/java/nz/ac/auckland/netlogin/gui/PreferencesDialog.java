package nz.ac.auckland.netlogin.gui;

import nz.ac.auckland.netlogin.NetLoginPreferences;
import nz.ac.auckland.netlogin.negotiation.AuthenticatorFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import java.beans.EventHandler;
import java.util.Collection;

public class PreferencesDialog {

	private NetLoginPreferences preferences;
	private Shell dialog;
	private Button okButton;
	private Button cancelButton;
	private Combo credentialSourceCombo;
	private Text serverText;
	private Text realmText;
	private Combo reconnectCombo;

	public PreferencesDialog(Shell shell, NetLoginPreferences preferences) {
		this.preferences = preferences;
		createComponents(shell);
		registerEvents();
	}

	public void open() {
		read();
		dialog.open();
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
		SWTHelper.selectComboItem(credentialSourceCombo, preferences.getCredentialSource());
        realmText.setText(preferences.getRealm());
		SWTHelper.selectComboItem(reconnectCombo, preferences.getReconnect() ? "Yes" : "No");
	}

	public void save() {
		preferences.setServer(serverText.getText());
		preferences.setCredentialSource(credentialSourceCombo.getText());
        preferences.setRealm(realmText.getText());
		preferences.setReconnect(reconnectCombo.getText().equals("Yes"));
		preferences.savePreferences();
	}

	protected void createComponents(Shell parent) {
		dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		dialog.setLayout(SWTHelper.createMinimalGridLayout());
		dialog.setText("NetLogin - Preferences");

		Composite formPanel = SWTHelper.createForm(dialog);
		formPanel.setLayoutData(SWTHelper.formLayoutData());

		SWTHelper.createFormLabel(formPanel, "Server:");
		serverText = new Text(formPanel, SWT.BORDER);
		serverText.setLayoutData(SWTHelper.formLayoutData());

		SWTHelper.createFormLabel(formPanel, "Credentials:");
		Collection<String> credentialSources = AuthenticatorFactory.getInstance().getNames();
		String[] credentialSourcesArray = credentialSources.toArray(new String[credentialSources.size()]);
		credentialSourceCombo = new Combo(formPanel, SWT.DROP_DOWN);
		credentialSourceCombo.setItems(credentialSourcesArray);
		credentialSourceCombo.setLayoutData(SWTHelper.formLayoutData());

		SWTHelper.createFormLabel(formPanel, "Server realm:");
		realmText = new Text(formPanel, SWT.BORDER);
		realmText.setLayoutData(SWTHelper.formLayoutData());

		SWTHelper.createFormLabel(formPanel, "Reconnect:");
		reconnectCombo = new Combo(formPanel, SWT.DROP_DOWN);
		reconnectCombo.setItems(new String[]{"Yes", "No"});
		reconnectCombo.setLayoutData(SWTHelper.formLayoutData());

		Composite buttonPanel = SWTHelper.createButtonPanel(dialog);
		okButton = SWTHelper.createButton(buttonPanel, "Ok");
		dialog.setDefaultButton(okButton);
		cancelButton = SWTHelper.createButton(buttonPanel, "Cancel");

		dialog.pack();
	}

	protected void registerEvents() {
		okButton.addSelectionListener(EventHandler.create(SelectionListener.class, this, "done"));
		cancelButton.addSelectionListener(EventHandler.create(SelectionListener.class, this, "close"));
	}

}
