/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.plaf.ComponentUI;

import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import imageviewer.ui.swing.ComboBoxUI;
import imageviewer.ui.swing.IconFactory;

// =======================================================================

public class RibbonComboBoxUI extends ComboBoxUI {

	public static ComponentUI createUI(JComponent b) {return new RibbonComboBoxUI();}

	// =======================================================================

	public void installUI(JComponent c) {super.installUI(c); c.setOpaque(false); c.setForeground(Color.black);}

  protected JButton createArrowButton() {return new RibbonComboBoxButton(comboBox,IconFactory.getComboBoxButtonIconDark(),comboBox.isEditable(),currentValuePane,listBox);}
}
