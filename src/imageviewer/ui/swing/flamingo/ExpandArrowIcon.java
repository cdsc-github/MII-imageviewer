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
import java.awt.RenderingHints;

import javax.swing.Icon;

public class ExpandArrowIcon implements Icon {

	private static final Color ICON_COLOR1=new Color(101,104,122);
	private static final Color ICON_COLOR2=new Color(235,235,235);

	public void paintIcon(Component c, Graphics g, int x, int y) {

		Graphics2D g2=(Graphics2D)g;
		Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.translate(x,y);
		g2.setColor(ICON_COLOR1);
		g2.drawLine(0,0,5,0);
		g2.drawLine(0,0,0,5);
		g2.setColor(ICON_COLOR2);
		g2.drawLine(1,1,5,1);
		g2.drawLine(1,1,1,5);
		g2.drawLine(4,7,7,7);
		g2.drawLine(7,3,7,7);
		g2.drawLine(4,3,4,3);
		g2.setColor(ICON_COLOR1);
		g2.drawLine(3,3,3,3);
		g2.drawLine(3,6,3,6);
		g2.drawLine(6,3,6,3);
		g2.fillRect(4,4,3,3);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
	}
  
	public int getIconWidth()  {return 8;}
	public int getIconHeight() {return 8;}
}
