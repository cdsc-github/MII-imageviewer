/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import javax.swing.MenuSelectionManager;

import javax.swing.plaf.ComponentUI;

// =======================================================================

public class RadioButtonMenuItemUI extends MenuItemUI {

  public static ComponentUI createUI(JComponent c) {return new RadioButtonMenuItemUI();}

	protected String getPropertyPrefix() {return "RadioButtonMenuItem";}

	protected boolean iconBorderEnabled() {return true;}

	public void processMouseEvent(JMenuItem item, MouseEvent e, MenuElement path[], MenuSelectionManager manager) {
  
		Point p=e.getPoint();
		if (p.x>=0 && p.x<item.getWidth() && p.y>=0 && p.y<item.getHeight()) {
			if (e.getID()==MouseEvent.MOUSE_RELEASED) {
				manager.clearSelectedPath();
				item.doClick(0);
			} else {
				manager.setSelectedPath(path);
			}
		} else if (item.getModel().isArmed()) {
			MenuElement newPath[]=new MenuElement[path.length-1];
			for(int i=0, c=path.length-1; i<c; i++)	newPath[i]=path[i];
			manager.setSelectedPath(newPath);
		}
	}
}

