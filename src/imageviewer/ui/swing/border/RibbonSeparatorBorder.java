/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import imageviewer.ui.swing.flamingo.RibbonConstants;

// =======================================================================

public class RibbonSeparatorBorder extends AbstractBorder {

	boolean leftBorder=true, rightBorder=true;

	public RibbonSeparatorBorder() {}
	public RibbonSeparatorBorder(boolean leftBorder, boolean rightBorder) {this.leftBorder=leftBorder; this.rightBorder=rightBorder;}

	// =======================================================================
	// Draw the left border only if the component is not the leftmost in
	// the container and that there's not an element before it that has
	// a similar right border.

	private boolean shouldDrawLeftElement(Component c) {

		Container parent=c.getParent();
		if (parent==null) return false;
		Rectangle bounds=c.getBounds();
		int minX=bounds.x;
		Component[] peers=parent.getComponents();
		for (int loop=0; loop<peers.length; loop++) {
			if (peers[loop] instanceof JComponent) {
				JComponent jc=(JComponent)peers[loop];
				Rectangle r=jc.getBounds();
				if (r.x<minX) {
					Border b=jc.getBorder();
					if (b instanceof RibbonSeparatorBorder) return false;
					minX=bounds.x;
				}
			}
		}
		return (minX==bounds.x) ? false : true;
 	}

	// =======================================================================
	// Always draw the right border unless it's the last component.

	private boolean shouldDrawRightElement(Component c) {

		Container parent=c.getParent();
		if (parent==null) return false;
		Rectangle bounds=c.getBounds();
		int targetX=bounds.x+bounds.width;
		Component[] peers=parent.getComponents();
		for (int loop=0; loop<peers.length; loop++) {
			Rectangle r=peers[loop].getBounds();
			if (r.x>targetX) return true;
		}
		return false;
	}

	// =======================================================================

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
		if ((leftBorder)&&(shouldDrawLeftElement(c))) {
			g2.setColor(new Color(128,128,128,128));
			g2.drawLine(0,4,0,RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET-10);
			g2.setColor(new Color(255,255,255,128));
			g2.drawLine(1,4,1,RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET-10);
		}
		if ((rightBorder)&&(shouldDrawRightElement(c))) {
			g2.setColor(new Color(128,128,128,128));
			g2.drawLine(w-2,4,w-2,RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET-10);
			g2.setColor(new Color(255,255,255,128));
			g2.drawLine(w-1,4,w-1,RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET-10);
		}
		g2.dispose();
	}
	
	// =======================================================================
	
	public Insets getBorderInsets(Component c) {return new Insets(0,(shouldDrawLeftElement(c)) ? 2 : 0,0,(shouldDrawRightElement(c)) ? 2 : 0);}
	public boolean isBorderOpaque() {return false;}
}
