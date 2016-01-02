/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.event.ActionEvent;
import javax.swing.AbstractButton;

import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;

// =======================================================================

public class RibbonActionEventWrapper extends MenuActionEvent {

	RibbonActionWrapper raw=null;
	AbstractButton ab=null;
	String ribbonActionCommand=null;

	public RibbonActionEventWrapper(ActionEvent ae, AbstractButton ab) {

		super(ae); 
		this.ab=ab; 
		raw=new RibbonActionWrapper();
	}

	public RibbonActionEventWrapper(ActionEvent ae, String ribbonActionCommand) {

		super(ae); 
		this.ribbonActionCommand=ribbonActionCommand; 
		raw=new RibbonActionWrapper();
	}

	public RibbonActionWrapper getMenuAction() {return raw;}

	public void setButton(AbstractButton ab) {this.ab=ab;}
	public void setRibbonActionCommand(String x) {ribbonActionCommand=x;}

	private class RibbonActionWrapper extends MenuAction {

		public RibbonActionWrapper() {super((ab!=null) ? ab.getActionCommand() : ribbonActionCommand);}
		public AbstractButton getMenuItem() {return ab;}
		public void setEnabled(boolean b) {super.setEnabled(b); if (ab!=null) {ab.setEnabled(b); ab.repaint();}}
	}
}
