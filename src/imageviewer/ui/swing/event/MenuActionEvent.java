/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.event;

import java.awt.event.ActionEvent;

import imageviewer.ui.swing.MenuAction;

// =======================================================================

public class MenuActionEvent {

	MenuAction ma=null;
	ActionEvent ae=null;

	public MenuActionEvent(ActionEvent ae) {this.ae=ae;}
	public MenuActionEvent(ActionEvent ae, MenuAction ma) {this.ae=ae; this.ma=ma;}

	// =======================================================================

	public ActionEvent getActionEvent() {return ae;}
	public MenuAction getMenuAction() {return ma;}

	public void setActionEvent(ActionEvent x) {ae=x;}
	public void setMenuAction(MenuAction x) {ma=x;}
}
