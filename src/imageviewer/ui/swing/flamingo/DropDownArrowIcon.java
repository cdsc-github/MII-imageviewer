/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;

import javax.swing.Icon;

public class DropDownArrowIcon implements Icon {

	private static final Polygon ARROW=new Polygon(new int[] {0,5,2},new int[] {0,0,3},3);   
	private static final Color ARROW_COLOR=new Color(32,32,32,180);

	public void paintIcon(Component c, Graphics g, int x, int y) {

		Graphics2D g2=(Graphics2D)g.create();
		Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.translate(x,y);
		g2.setColor(ARROW_COLOR);
		g2.fill(ARROW);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
		g2.dispose();
	}
  
	public int getIconWidth()  {return 7;}
	public int getIconHeight() {return 4;}
}
