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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;

import org.jvnet.flamingo.common.JButtonStrip;
import org.jvnet.flamingo.common.JButtonStrip.StripOrientation;
import org.jvnet.flamingo.common.ui.BasicButtonStripUI;

public class ImageViewerButtonStripUI extends BasicButtonStripUI {

	private static final Color BOTTOM_STRIP_LIGHT=UIManager.getColor("JRibbon.buttonStripBottomLight");
	private static final Color BOTTOM_STRIP_DARK=UIManager.getColor("JRibbon.buttonStripBottomDark");
	private static final Color TOP_STRIP_LIGHT=UIManager.getColor("JRibbon.buttonStripTopLight");
	private static final Color TOP_STRIP_DARK=UIManager.getColor("JRibbon.buttonStripTopDark");
	private static final Color BUTTON_STRIP_HIGHLIGHT=UIManager.getColor("ButtonStrip.highlight");
	private static final Color BUTTON_STRIP_DARK_HIGHLIGHT=UIManager.getColor("ButtonStrip.darkHighlight");

	private static final Border BAND_BORDER=UIManager.getBorder("ButtonStrip.border");

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new ImageViewerButtonStripUI();}
	
	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

		if (buttonStrip.getOrientation()==StripOrientation.HORIZONTAL) {
			int width=c.getWidth();
			int height=c.getHeight();
			Shape s1=new RoundRectangle2D.Double(0,0,width,10,5,5);
			Area a1=new Area(s1);
			a1.add(new Area(new Rectangle2D.Double(0,5,width,5)));
			GradientPaint gp1=new GradientPaint(0,0,TOP_STRIP_DARK,0,10,TOP_STRIP_LIGHT);
			g2.setPaint(gp1);
			g2.fill(a1);
			Shape s2=new RoundRectangle2D.Double(0,10,width,11,5,5);
			Area a2=new Area(s2);
			a2.add(new Area(new Rectangle2D.Double(0,10,width,5)));
			GradientPaint gp2=new GradientPaint(0,10,BOTTOM_STRIP_DARK,0,25,BOTTOM_STRIP_LIGHT);
			g2.setPaint(gp2);
			g2.fill(a2);			
			super.paint(g,c);
			g2.setColor(BUTTON_STRIP_HIGHLIGHT);
			g2.drawRoundRect(1,1,c.getWidth()-3,c.getHeight()-3,5,5);
			BAND_BORDER.paintBorder(c,g2,0,0,c.getWidth(),c.getHeight());
		} else {
			super.paint(g,c);
		}
	}

	// =======================================================================

	protected void paintStripButtonBorder(Graphics g, AbstractButton button, boolean isFirst, boolean isLast) {

		if (buttonStrip.getOrientation()==StripOrientation.HORIZONTAL) {
			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			if (!isFirst) {
				g2.setColor((button.isEnabled()) ? BUTTON_STRIP_HIGHLIGHT : BUTTON_STRIP_HIGHLIGHT.darker());
				g2.drawLine(0,1,0,button.getHeight()-2);
			}
			if (!isLast) {
				g2.setColor((button.isEnabled()) ? BUTTON_STRIP_DARK_HIGHLIGHT : BUTTON_STRIP_DARK_HIGHLIGHT.darker());
				g2.drawLine(button.getWidth()-1,1,button.getWidth()-1,button.getHeight()-2);
			}
		}
	}
}
