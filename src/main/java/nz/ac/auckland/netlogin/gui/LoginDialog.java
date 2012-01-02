package nz.ac.auckland.netlogin.gui;

import nz.ac.auckland.netlogin.LoginCancelled;
import nz.ac.auckland.netlogin.negotiation.CredentialsCallback;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import java.beans.EventHandler;

public class LoginDialog implements CredentialsCallback {

	private volatile boolean awaitingCredentials = false;
	private volatile String user = null;
	private volatile String password = null;

	private Shell dialog;
	private Text userText;
	private Text passwordText;
	private Button loginButton;
	private Button cancelButton;

	public LoginDialog(Shell shell) {
		createComponents(shell);
		registerEvents();
	}

	protected void registerEvents() {
		// enable the login button if the username and password are supplied
		ModifyListener detailsValidator = EventHandler.create(ModifyListener.class, this, "validateDetails");
		userText.addModifyListener(detailsValidator);
		passwordText.addModifyListener(detailsValidator);

		// process the login event
		SelectionListener loginAction = EventHandler.create(SelectionListener.class, this, "login");
		loginButton.addSelectionListener(loginAction);
		passwordText.addSelectionListener(loginAction);

		// close the dialog
		cancelButton.addSelectionListener(EventHandler.create(SelectionListener.class, this, "close"));
	}

	protected void createComponents(Shell shell) {
		dialog = new Shell(shell, (SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL) & ~SWT.CLOSE);
		dialog.setLayout(SWTHelper.createMinimalGridLayout());
		dialog.setText("NetLogin - Login");

		Composite formPanel = SWTHelper.createForm(dialog);
		formPanel.setLayoutData(SWTHelper.formLayoutData());

		SWTHelper.createFormLabel(formPanel, "NetID/UPI:");
		userText = new Text(formPanel, SWT.BORDER);
		userText.setLayoutData(SWTHelper.formLayoutData());

		SWTHelper.createFormLabel(formPanel, "Password:");
		passwordText = new Text(formPanel, SWT.BORDER);
		passwordText.setEchoChar('\u25CF');
		passwordText.setLayoutData(SWTHelper.formLayoutData());

//		rememberMeCheckbox = new JCheckBox("Remember NetID/UPI");
//		JPanel rememberPanel = new JPanel(new FlowLayout());
//		rememberPanel.add(rememberMeCheckbox);

		Composite buttonPanel = SWTHelper.createButtonPanel(dialog);
		loginButton = SWTHelper.createButton(buttonPanel, "Connect");
		loginButton.setEnabled(false);
		dialog.setDefaultButton(loginButton);
		cancelButton = SWTHelper.createButton(buttonPanel, "Cancel");

		dialog.pack();
	}

	public void open() {
		awaitingCredentials = true;
        password = null;

		dialog.getDisplay().asyncExec(new Runnable() {
			public void run() {
				dialog.open();
				userText.setFocus();
			}
		});
	}

	private void waitForClose() {
		// wait until the dialog is closed
		while (awaitingCredentials) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore and continue
			}
		}
	}

	public void login() {
		this.user = userText.getText();
		this.password = passwordText.getText();
		close();
	}

	public void close() {
        clearPassword();
		dialog.setVisible(false);
		awaitingCredentials = false;
	}

	public void clearPassword() {
		passwordText.setText("");
		passwordText.update(); // update the buffer now to prevent flicker when then dialog is next opened
	}

	public void validateDetails() {
		if (!passwordText.getText().equals("") && !userText.getText().equals("")) {
			loginButton.setEnabled(true);
		} else {
			loginButton.setEnabled(false);
		}
	}

	public boolean requestCredentials() throws LoginCancelled {
		open();
		waitForClose();
		if (user == null || password == null) throw new LoginCancelled();
		return true;
	}

	public String getUsername() {
		return user;
	}

	public String retrievePassword() {
		String password = this.password;
        this.password = null;
		return password;
	}

}
