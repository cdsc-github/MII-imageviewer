/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.event;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import imageviewer.system.SaveStack;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.FloatingPanel;

import imageviewer.ui.dialog.OpenDialog;
import imageviewer.ui.dialog.PreferencesDialog;

import imageviewer.ui.swing.MenuAction;

// =======================================================================

public class BasicMenuActionListener implements MenuActionListener {

	private static final JCheckBox ASK_CB=new JCheckBox("Do not show this message again.",false);

	public BasicMenuActionListener() {}
	
	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}

		if (actionCommand.compareTo("Open")==0) {
			OpenDialog od=OpenDialog.getInstance();
			if (!od.isShowing()) {
				FloatingPanel fp=new FloatingPanel(od,"Open");
				fp.setAlpha(0.85f);
				ApplicationPanel.getInstance().centerFloatingPanel(fp);
				ApplicationPanel.getInstance().addFloatingPanel(fp);
			}
		} else if (actionCommand.compareTo("Save")==0) {
			Boolean askOnSave=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.ASK_ON_SAVE);
			if ((askOnSave==null)||(askOnSave.booleanValue())) {
				int response=ApplicationPanel.getInstance().showDialog("Are you sure you want to save all items? Any newly created data/information will be stored in the archive.",
																															 new JComponent[] {ASK_CB},JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
				if (ASK_CB.isSelected()) ApplicationContext.getContext().setProperty(ApplicationContext.ASK_ON_SAVE,new Boolean(false));
				if (response!=JOptionPane.OK_OPTION) return;
			}
			SaveStack.getInstance().saveAll();
		} else if (actionCommand.compareTo("Preferences")==0) {
			FloatingPanel fp=new FloatingPanel(new PreferencesDialog(),"Preferences");
			fp.setAlpha(0.85f);
			ApplicationPanel.getInstance().centerFloatingPanel(fp);
			ApplicationPanel.getInstance().addFloatingPanel(fp);
		} else if (actionCommand.compareTo("Quit")==0) {
			
		} else if (actionCommand.compareTo("Undo")==0) {
			ApplicationContext.undo();
		} else if (actionCommand.compareTo("Redo")==0) {
			ApplicationContext.redo();
		}
	}
}
