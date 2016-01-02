/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.geom.Point2D;

import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

import org.jvnet.flamingo.common.JPopupPanel;
import org.jvnet.flamingo.common.JIconPopupPanel;
import org.jvnet.flamingo.common.ui.BasicPopupPanelUI;

import imageviewer.ui.swing.BackgroundPainter;

// =======================================================================

public class ImageViewerPopupPanelUI extends BasicPopupPanelUI implements BackgroundPainter {

	private static final Color RIBBON_TOP_LIGHT=UIManager.getColor("JRibbon.ribbonBackgroundTopLight");
	private static final Color RIBBON_TOP_DARK=UIManager.getColor("JRibbon.ribbonBackgroundTopDark");
	private static final Color RIBBON_BOTTOM_LIGHT=UIManager.getColor("JRibbon.ribbonBackgroundBottomLight");
	private static final Color RIBBON_BOTTOM_DARK=UIManager.getColor("JRibbon.ribbonBackgroundBottomDark");

	private static final Color BORDER_HIGHLIGHT=new Color(255,255,255,75);

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new ImageViewerPopupPanelUI();}

	public void installUI(JComponent c) {

		JPopupPanel p=(JPopupPanel)c;
		super.installUI(p);
		installDefaults();
		p.setOpaque(false);
	}

	public void paintBackground(JComponent jc, Graphics g, int x, int y, int w, int h) {

		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		Point2D start=new Point2D.Float(0,0);
		Point2D end=new Point2D.Float(0,h);
		float[] dist={0.0f,(float)(x+40)/(float)(x+h),0.8f};
		Color[] colors={RIBBON_TOP_LIGHT,RIBBON_BOTTOM_DARK,RIBBON_BOTTOM_LIGHT};
		LinearGradientPaint lgp=new LinearGradientPaint(start,end,dist,colors);
		g2.setPaint(lgp);
		boolean paintBandTitle=(jc.getClientProperty("paintBandTitle")==Boolean.FALSE) ? false : true;
		g2.fillRoundRect(x,y+2,w-3,h-((!paintBandTitle) ? 4 : RibbonConstants.BAND_HEADER_HEIGHT),6,6);
		g2.setColor(BORDER_HIGHLIGHT);
		g2.drawLine(x+3,y+3,w-6,y+3);
		g2.drawLine(x+1,y+6,x+1,h-6);
		if (paintBandTitle) {
			System.err.println(jc+" "+paintBandTitle);
			RibbonUtil.paintBandTitle(g,new Rectangle(x,y+h-RibbonConstants.BAND_HEADER_HEIGHT-1,w-3,RibbonConstants.BAND_HEADER_HEIGHT-2),
																(String)(jc.getClientProperty(ImageViewerRibbonBandUI.RIBBON_TITLE_TEXT)),true,false);
		}
	}
}
