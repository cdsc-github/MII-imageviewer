/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.event.ActionEvent;
import java.security.Principal;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException; 

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import imageviewer.ui.dialog.LoginDialog;
import imageviewer.ui.dialog.LoginInterface;

import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================
// A bit of a weird class to handle both the login information and the
// login actions that the client performs for login/logout. The static
// internal class is used to remember the current logins and subject
// information associated with the individual login. The
// instance-level portion of this class handles the menu event actions
// in the imageviewer client. There's not really a good reason why
// they can't be separated, except that it was nice to put all login
// functionality into a single module here (anti-class proliferation!).

public class UserManager implements MenuActionListener {

	public static final String NO_LOGIN=new String("Not logged in");

	private static final Logger LOG=Logger.getLogger("imageviewer.login");
	private static final LoginStore LS=new LoginStore();

	// =======================================================================
	// Convenience static methods to route to the final static class
	// instance of the LoginStore.

	public static LoginStore getLoginStore() {return LS;}
	public static Subject getCurrentUser() {return LS.getCurrentUser();}
	public static String getCurrentUserName() {return LS.getCurrentUserName();}
	public static boolean isLoggedIn() {return (LS.getCurrentUser()!=null);}

	public static void setCurrentUser(Subject s, LoginContext lc) {LS.setCurrentUser(s,lc);}

	// =======================================================================

	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}
		if (actionCommand.compareTo("Login")==0) {
			doLogin();
		} else if (actionCommand.compareTo("Logout")==0) {
			doLogout(LS.getCurrentLoginContext());
		} 
	}

	// =======================================================================
	// Open the login panel on the screen, allow for login to the
	// imageviewer context.

	public void doLogin() {doLogin("Enter your username and password below to login to imageViewer.","imageviewer",new LoginUpdater());}

	public void doLogin(String dialogText, String loginContextName, LoginInterface li) {

		LoginDialog ld=new LoginDialog(dialogText,loginContextName,li);
		if (li instanceof LoginUpdater) ((LoginUpdater)li).setDialog(ld);
		FloatingPanel fp=new FloatingPanel(ld,"Login");
		fp.setAlpha(0.85f);
		ApplicationPanel.getInstance().centerFloatingPanel(fp);
		ApplicationPanel.getInstance().addFloatingPanel(fp);
	}

	// =======================================================================

	public void doLogout(LoginContext lc) {

		try {
			if (lc!=null) {
				int response=ApplicationPanel.getInstance().showDialog("Are you sure you want to logout of imageViewer? Some functionality will be disabled until you login again.",
																															 null,JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
				if (response!=JOptionPane.OK_OPTION) return;
				lc.logout();
				ApplicationPanel.getInstance().updateLogin(null,null);
				ApplicationPanel.getInstance().addStatusMessage("Logout was successfully completed");
				MenuAction.ACTIONS.get("Login").setEnabled(true);	
				MenuAction.ACTIONS.get("Logout").setEnabled(false);
			}
		} catch (Exception exc) {
			LOG.error("Unable to properly logout of imageviewer.");
			exc.printStackTrace();
		}
	}

	// =======================================================================

	public void toggleLogin() {if (isLoggedIn()) doLogout(LS.getCurrentLoginContext()); else doLogin();}

	// =======================================================================
	// Specifically handle logins and logouts for imageviewer

	private class LoginUpdater implements LoginInterface {

		LoginDialog ld=null;

		public void setDialog(LoginDialog x) {ld=x;}

		public void loginSuccessful() {

			MenuAction.ACTIONS.get("Login").setEnabled(false);	
			MenuAction.ACTIONS.get("Logout").setEnabled(true);
			ApplicationPanel.getInstance().updateLogin(ld.getSubject(),ld.getLoginContext());
			ApplicationPanel.getInstance().addStatusMessage("Login successfully completed");
		}

		public void loginUnsuccessful() {LOG.info("Unsuccessful attempt to login to imageviewer.");}
		public void loginCancelled() {}
	}

	// =======================================================================

	private static class LoginStore {

		Hashtable<String,Subject> subjectTable=new Hashtable<String,Subject>();
		Hashtable<Subject,LoginContext> contextTable=new Hashtable<Subject,LoginContext>();
		LoginContext currentLoginContext=null;
		Subject currentUser=null;

		private LoginStore() {}

		// =======================================================================

		public Subject getCurrentUser() {return currentUser;}
		public Subject findSubject(String loginContextName) {return subjectTable.get(loginContextName);}
		public LoginContext getCurrentLoginContext() {return currentLoginContext;}
		public LoginContext findLoginContext(Subject s) {return contextTable.get(s);}

		public void addUserLogin(String loginContextName, Subject s, LoginContext lc) {subjectTable.put(loginContextName,s); contextTable.put(s,lc);}
		public void removeUserLogin(String loginContextName) {Subject s=subjectTable.remove(loginContextName); if (s!=null) contextTable.remove(s);}
		public void setCurrentUser(Subject s, LoginContext lc) {currentUser=s; currentLoginContext=lc; if (s!=null) LOG.debug("Current user logged in: "+getCurrentUserName());}

		public String findLoginContextName(Subject s) {

			for (Enumeration<String> e=subjectTable.keys(); e.hasMoreElements();) {
				String key=e.nextElement(); 
				if (subjectTable.get(key)==s) return key;
			} 
			return null;
		}

		public String getCurrentUserName() {

			if (currentUser!=null) {
				Set<Principal> principalSet=currentUser.getPrincipals();
				Iterator<Principal> i=principalSet.iterator();
				Principal p=i.next();
				return p.getName();
			} else return NO_LOGIN;
		}
	}
}
