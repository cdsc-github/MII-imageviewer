/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.LookAndFeel;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicPopupMenuUI;

// =======================================================================

public class PopupMenuUI extends BasicPopupMenuUI {

	public static ComponentUI createUI(JComponent c) {return new PopupMenuUI();}

	public void installUI(JComponent c) {super.installUI(c); popupMenu.setOpaque(false);}

	public void installDefaults() {super.installDefaults();	LookAndFeel.installBorder(popupMenu,"PopupMenu.border");}

	public Popup getPopup(JPopupMenu popup, int x, int y) {

		Popup pp=super.getPopup(popup,x,y);
		JPanel panel=(JPanel)popup.getParent();
		panel.setOpaque(false);
		return pp;
	}
}
