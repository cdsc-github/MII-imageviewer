/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

// =======================================================================
// Adapted from JGoodies

public class MinimumSizedCheckIcon extends MinimumSizedIcon {

	JMenuItem menuItem=null;
	
	public MinimumSizedCheckIcon(Icon icon, JMenuItem menuItem) {super(icon);	this.menuItem=menuItem;}
	
	// =======================================================================

	public void paintIcon(Component c, Graphics g, int x, int y) {paintState(g,x,y); super.paintIcon(c,g,x,y);}

	// =======================================================================

	private void paintState(Graphics g, int x, int y) {

		ButtonModel model=menuItem.getModel();
		int w=getIconWidth();
		int h=getIconHeight();
		
		g.translate(x,y);
		if (model.isSelected()) {
			Color background=UIManager.getColor("controlHighlight");
			Color upColor=background.brighter();
			Color downColor=background.darker();
			g.setColor(background);
			g.fillRect(0,0,w,h);
			g.setColor(model.isSelected() ? downColor : upColor);
			g.drawLine(0,0,w-2,0);
			g.drawLine(0,0,0,h-2);
			g.setColor(model.isSelected() ? upColor: downColor);
			g.drawLine(0,h-1,w-1,h-1);
			g.drawLine(w-1,0,w-1,h-1);
		}
		g.translate(-x,-y);
		g.setColor(UIManager.getColor("textText"));
	}
}
