/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.UIManager;

import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.text.View;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.jvnet.flamingo.common.JCommandButton;

// =======================================================================

public class RibbonUtil {

	private static final Color BACKGROUND_PAINT1=UIManager.getColor("RibbonBand.backgroundPaintLight");
	private static final Color BACKGROUND_PAINT2=UIManager.getColor("RibbonBand.backgroundPaintDark");
	private static final Color FOREGROUND=UIManager.getColor("RibbonBand.foreground");

	private static final Color TAB_HIGHLIGHT_LIGHT_TOP=UIManager.getColor("TabbedPane.tabHighlightLightTop");
	private static final Color TAB_HIGHLIGHT_DARK_TOP=UIManager.getColor("TabbedPane.tabHighlightDarkTop");
	private static final Color TAB_HIGHLIGHT_LIGHT_BOTTOM=UIManager.getColor("TabbedPane.tabHighlightLightBottom");
	private static final Color TAB_HIGHLIGHT_DARK_BOTTOM=UIManager.getColor("TabbedPane.tabHighlightDarkBottom");

	private static final Font BAND_FONT=UIManager.getFont("RibbonBand.font");

	private static final Graphics2D UTIL_GRAPHICS;

	static {

		BufferedImage bi=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		UTIL_GRAPHICS=(Graphics2D)bi.createGraphics();
		bi.flush();
	}

	// =======================================================================

	private RibbonUtil() {}

	public static void paintBandTitle(Graphics g, Rectangle titleRectangle, String title, boolean isUnderMouse, boolean hasExpandIcon) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

		Shape s=new RoundRectangle2D.Double(titleRectangle.x,titleRectangle.y,titleRectangle.width,titleRectangle.height,8,8);
		GradientPaint gp1=new GradientPaint(0,titleRectangle.y+titleRectangle.height/2,new Color(75,75,75),0,titleRectangle.y+titleRectangle.height,new Color(100,100,100));
		g2.setPaint(gp1);
		g2.fill(s);
		GradientPaint gp2=new GradientPaint(0,titleRectangle.y,new Color(140,140,160),0,titleRectangle.y+titleRectangle.height/2,new Color(105,105,105));
		g2.setPaint(gp2);
		g2.fill(new Rectangle2D.Double(titleRectangle.x,titleRectangle.y,titleRectangle.width,titleRectangle.height/2-1)); 
		g2.setFont(BAND_FONT);
		int y=titleRectangle.y+(titleRectangle.height+g2.getFontMetrics().getAscent())/2;
		g2.setBackground(Color.black);
		g2.setColor(FOREGROUND);
		if (title!=null) {
			Rectangle2D titleBounds=g2.getFontMetrics().getStringBounds(title,g2);
			double tmpX=((titleRectangle.width-titleBounds.getWidth())/2);
			double xCoord=(hasExpandIcon) ? (((titleRectangle.x+tmpX+titleBounds.getWidth()+5)>(titleRectangle.x+titleRectangle.width-10)) ? 3 : tmpX) : tmpX;
			BasicGraphicsUtils.drawString(g2,title,0,(int)(titleRectangle.x+xCoord),y-2); 
		}
		g2.dispose();
	}

	public static int getTitleWidth(String title) {

		UTIL_GRAPHICS.setFont(BAND_FONT);
		Rectangle2D titleBounds=UTIL_GRAPHICS.getFontMetrics().getStringBounds(title,UTIL_GRAPHICS);
		return (int)titleBounds.getWidth();
	}
}
