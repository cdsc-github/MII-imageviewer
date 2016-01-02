/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicMenuUI;

// =======================================================================

public class MenuUI extends BasicMenuUI {

	MenuItemRenderer mir=null;

  public static ComponentUI createUI(JComponent c) {return new MenuUI();}

	protected void installDefaults() {

		super.installDefaults();
		if (arrowIcon==null || arrowIcon instanceof UIResource) arrowIcon=UIManager.getIcon("Menu.arrowIcon");
		mir=new MenuItemRenderer(menuItem,false,acceleratorFont,selectionForeground,disabledForeground,
														 acceleratorForeground,acceleratorSelectionForeground);
		defaultTextIconGap=2;
	}

	// =======================================================================

	protected void uninstallDefaults() {super.uninstallDefaults(); mir=null;}

	// =======================================================================

	protected void paintMenuItem(Graphics g, JComponent c, Icon aCheckIcon, Icon anArrowIcon, Color background, Color foreground, int textIconGap) {
		
		if (!((JMenu)menuItem).isTopLevelMenu()) {
			mir.paintMenuItem(g,c,aCheckIcon,anArrowIcon,background,foreground,textIconGap);
		} else {
			super.paintMenuItem(g,c,aCheckIcon,anArrowIcon,background,foreground,textIconGap);
		}
	}

	// =======================================================================

	protected Dimension getPreferredMenuItemSize(JComponent c, Icon aCheckIcon, Icon anArrowIcon, int textIconGap) {

		return (!((JMenu)menuItem).isTopLevelMenu()) ? 
			mir.getPreferredMenuItemSize(c,aCheckIcon,anArrowIcon,textIconGap) : 
			super.getPreferredMenuItemSize(c,aCheckIcon,anArrowIcon,textIconGap);
	}
}
