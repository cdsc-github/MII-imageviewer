/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jvnet.flamingo.common.JCommandButtonPanel;
import org.jvnet.flamingo.common.ui.BasicCommandButtonPanelUI;

// =======================================================================

public class ImageViewerCommandButtonPanelUI extends BasicCommandButtonPanelUI {

	private static final Color LABEL_TEXT=UIManager.getColor("PopupGallery.labelForeground");
	private static final Color LABEL_BACKGROUND=UIManager.getColor("PopupGallery.labelBackground");

	public static ComponentUI createUI(JComponent c) {return new ImageViewerCommandButtonPanelUI((JCommandButtonPanel)c);}

	// =======================================================================
	
	public ImageViewerCommandButtonPanelUI(JCommandButtonPanel c) {super(c);}

	// =======================================================================

	protected void paintGroupBackground(Graphics g, int groupIndex, int x, int y, int width, int height) {}

	protected void paintGroupTitle(Graphics g, String groupTitle, int groupIndex, int x, int y, int width, int height) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

		int titleHeight=getGroupTitleHeight();
		g2.setPaint(LABEL_BACKGROUND);
		if (y<=4) {
			Area a1=new Area(new RoundRectangle2D.Double(x+2,0,width,6,8,8));
			a1.add(new Area(new Rectangle(x+2,y,width,titleHeight)));
			g2.fill(a1);
		} else {
			g2.fillRect(x+2,y,width-3,titleHeight);
		}
		g2.drawLine(x+2,y+titleHeight,width,y+titleHeight);
		g2.setColor(new Color(0,0,0,96));
		g2.drawLine(x+2,y+titleHeight+1,width,y+titleHeight+1);
		g2.setColor(LABEL_TEXT);
		g2.drawString(groupTitle,x+5,y+titleHeight-4);
		g2.dispose();
	}
}
