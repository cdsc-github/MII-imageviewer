/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.JComponent;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuItemUI;

// =======================================================================

public class MenuItemUI extends BasicMenuItemUI {

	protected static final AlphaComposite ALPHA_MENU_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.9f);

	// =======================================================================

	MenuItemRenderer mir=null;

	protected void installDefaults() {

		super.installDefaults();
		mir=new MenuItemRenderer(menuItem,iconBorderEnabled(),acceleratorFont,selectionForeground,disabledForeground,acceleratorForeground,acceleratorSelectionForeground);
		defaultTextIconGap=2;
	}

	// =======================================================================

  public static ComponentUI createUI(JComponent c) {return new MenuItemUI();}

	// =======================================================================

	protected boolean iconBorderEnabled() {return false;}

	// =======================================================================

	protected void uninstallDefaults() {super.uninstallDefaults(); mir=null;}

	// =======================================================================
	
	protected Dimension getPreferredMenuItemSize(JComponent c, Icon aCheckIcon, Icon anArrowIcon, int textIconGap) {

		Dimension size=mir.getPreferredMenuItemSize(c,aCheckIcon,anArrowIcon,textIconGap);
		return new Dimension(size.width,size.height);
	}

	protected void paintMenuItem(Graphics g, JComponent c, Icon aCheckIcon, Icon anArrowIcon, Color background, Color foreground, int textIconGap) {

		mir.paintMenuItem(g,c,aCheckIcon,anArrowIcon,background,foreground,textIconGap);
	}

	// =======================================================================
	// Paint the regular menu into a buffered image, rather than
	// directly to the graphic.  Then use the buffered image and render
	// it to the graphic using an alpha composite; this provides the
	// illusion of semi-transparent menus.

	public void paint(Graphics g, JComponent c) {

		BufferedImage bi=new BufferedImage(c.getWidth(),c.getHeight(),BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2=bi.createGraphics();
		g2.setColor(g.getColor());
		super.paint(g2,c);
		Graphics2D gx=(Graphics2D)g;
	  gx.setComposite(ALPHA_MENU_AC);
		gx.drawImage(bi,0,0,null);
		g2.dispose();
		bi.flush();
		bi=null;
	}
}
