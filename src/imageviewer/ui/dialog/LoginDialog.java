/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException; 

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import org.jdesktop.swingx.JXPanel;

import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.FloatingPanel;

// =======================================================================

public class LoginDialog extends JXPanel implements ActionListener {

	private static final Icon LOGIN_ICON=UIManager.getIcon("Dialog.loginIcon");

	// =======================================================================

	JButton clearButton=new JButton("Clear");
	JButton loginButton=new JButton("Login");
	JButton cancelButton=new JButton("Cancel");
	JTextField ltf=new JTextField(16);
	JPasswordField pwf=new JPasswordField();

	LoginContext lc=null;
	LoginInterface li=null;
	Subject s=null;

	public LoginDialog(String loginDialogText, String loginContextName, LoginInterface li) {

		super();
		FormLayout fl=new FormLayout("pref,2dlu,right:pref:grow,2dlu,pref,2dlu,pref","top:pref,4px:grow,pref,2px,pref,2px:grow,pref,2px,bottom:pref");
		setLayout(fl);
		setBorder(new EmptyBorder(5,5,5,5));
		setPreferredSize(new Dimension(260,128));

		JTextArea loginDescription=DialogUtil.createTextArea(loginDialogText);
		JLabel loginIcon=new JLabel(LOGIN_ICON);
		JLabel loginLabel=new JLabel("Username",JLabel.TRAILING);
		JLabel passwordLabel=new JLabel("Password",JLabel.TRAILING);
		JSeparator separator=new JSeparator();
		separator.setPreferredSize(new Dimension(245,5));

		clearButton.setActionCommand("clear");
		clearButton.addActionListener(this);
		loginButton.setActionCommand("login");
		loginButton.addActionListener(this);
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		pwf.setActionCommand("login");
		pwf.addActionListener(this);

		CellConstraints cc=new CellConstraints();
		add(loginDescription,cc.xywh(1,1,7,1));
		add(loginIcon,cc.xywh(1,3,1,3));
		add(loginLabel,cc.xy(3,3));
		add(ltf,cc.xywh(5,3,3,1));
		add(passwordLabel,cc.xy(3,5));
		add(pwf,cc.xywh(5,5,3,1));
		add(separator,cc.xywh(1,7,7,1));
		add(clearButton,cc.xy(1,9));
		add(loginButton,cc.xy(5,9));
		add(cancelButton,cc.xy(7,9));

		try {
			this.li=li;
			lc=new LoginContext(loginContextName,new LoginCallbackHandler());
		} catch (Exception exc) {}
	}

	// =======================================================================

	private void doClose() {

		FloatingPanel fp=(FloatingPanel)SwingUtilities.getAncestorOfClass(FloatingPanel.class,this);
		if (fp!=null) fp.actionPerformed(new ActionEvent(this,1,"close"));			
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		if (("close".equals(e.getActionCommand()))||("cancel".equals(e.getActionCommand()))) {
			li.loginCancelled();
			doClose();
		} else if ("clear".equals(e.getActionCommand())) {
			ltf.setText("");
			pwf.setText("");
		} else if ("login".equals(e.getActionCommand())) {
			try {
				lc.login();
				s=lc.getSubject();
				Set<Principal> principalSet=s.getPrincipals();
				for (Principal p : principalSet) {
					if (p.getName().compareToIgnoreCase("guest")==0) {
						try {lc.logout();} catch (Exception exc1) {}
						getParent().setVisible(false);
						ApplicationPanel.getInstance().showDialog("An unrecognized username was entered.  Please enter a different username and password to try again.",
																											null,JOptionPane.WARNING_MESSAGE,JOptionPane.DEFAULT_OPTION);	
						getParent().setVisible(true);
						return;
					}
				}
			} catch (LoginException lexc) {
				getParent().setVisible(false);
				ApplicationPanel.getInstance().showDialog("The entered username/password combination was not accepted.  Please retype the password and try again.",
																									null,JOptionPane.WARNING_MESSAGE,JOptionPane.DEFAULT_OPTION);
				getParent().setVisible(true);
				return;
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			li.loginSuccessful();
 			doClose();
		} 
	}

	// =======================================================================

	public Subject getSubject() {return s;}
	public LoginContext getLoginContext() {return lc;}

	// =======================================================================

	private class LoginCallbackHandler implements CallbackHandler {

    public LoginCallbackHandler() {}

    public void handle(Callback[] callbacks) throws UnsupportedCallbackException {

			for (int i=0; i<callbacks.length; i++) {
				if (callbacks[i] instanceof NameCallback) {
					((NameCallback)callbacks[i]).setName(ltf.getText());
				} else if (callbacks[i] instanceof PasswordCallback) {
					((PasswordCallback)callbacks[i]).setPassword(pwf.getPassword());
				} else {
					throw(new UnsupportedCallbackException(callbacks[i], "Callback class not supported"));
				}
			}
    }
	}
}
