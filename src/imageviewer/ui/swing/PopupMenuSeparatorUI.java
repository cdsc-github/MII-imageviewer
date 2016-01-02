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

import javax.swing.JComponent;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalSeparatorUI;

public class PopupMenuSeparatorUI extends MetalSeparatorUI {

	private static final Color BACKGROUND_COLOR=(Color)UIManager.get("PopupMenuSeparator.background");
	private static final Color FOREGROUND_COLOR=(Color)UIManager.get("PopupMenuSeparator.foreground");

	public static ComponentUI createUI(JComponent c) {return new PopupMenuSeparatorUI();}

	public void paint(Graphics g, JComponent c) {

		Dimension s=c.getSize();
		g.setColor(BACKGROUND_COLOR);
		g.drawLine(0,1,s.width,1);
		g.setColor(FOREGROUND_COLOR);
		g.drawLine(0,2,s.width,2);
		g.drawLine(0,0,0,0);
		g.drawLine(0,3,0,3);
	}
}
