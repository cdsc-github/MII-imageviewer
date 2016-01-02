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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import imageviewer.ui.graphics.EllipticalGradientPaint;
import imageviewer.ui.swing.ToggleButtonUI;

// =======================================================================

public class ImageViewerToggleTabButtonUI extends ToggleButtonUI {

	private static final Color TAB_ROLLOVER_HIGHLIGHT_LIGHT=UIManager.getColor("ToggleTabButton.rolloverTabHighlightLight");
	private static final Color TAB_ROLLOVER_HIGHLIGHT_DARK=UIManager.getColor("ToggleTabButton.rolloverTabHighlightDark");
	private static final Color TAB_SELECT_HIGHLIGHT_LIGHT=UIManager.getColor("ToggleTabButton.selectedTabHighlightLight");
	private static final Color TAB_SELECT_HIGHLIGHT_DARK=UIManager.getColor("ToggleTabButton.selectedTabHighlightDark");
	private static final Color TOGGLE_TAB_FOREGROUND=UIManager.getColor("ToggleTabButton.foreground");
	private static final Color TOGGLE_TAB_FOREGROUND_SELECTED=UIManager.getColor("ToggleTabButton.foregroundSelected");

	private static final Color ROLLOVER_GLOW=UIManager.getColor("ToggleTabButton.rolloverTabGlow");
	private static final Color ROLLOVER_BORDER=UIManager.getColor("ToggleTabButton.rolloverBorder");

	private static final Border TOGGLE_TAB_BORDER=UIManager.getBorder("ToggleTabButton.border");

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new ImageViewerToggleTabButtonUI();}

	MouseAdapter ma=null;

	public void installDefaults(final AbstractButton b) {

		super.installDefaults(b); 
		b.setBorder(TOGGLE_TAB_BORDER); 
		b.setRolloverEnabled(true);
		b.setOpaque(false);
		b.setFocusPainted(false);
		borderPaintsFocus=false;
		ma=new MouseAdapter() {
			public void mouseEntered(MouseEvent me) {b.repaint();}  // For some reason, have to repaint from the parent...
			public void mouseExited(MouseEvent me) {b.repaint();}
			public void mouseMoved(MouseEvent me) {b.repaint();}};
		b.addMouseListener(ma);
		b.addMouseMotionListener(ma);
	}

	public void uninstallDefaults(AbstractButton b) {b.removeMouseListener(ma); b.removeMouseMotionListener(ma); ma=null;}

	protected void paintFocus(Graphics g, AbstractButton b, Rectangle viewRect, Rectangle textRect, Rectangle iconRect) {}

	// =======================================================================

	public void update(Graphics g, JComponent c) {paint(g,c);}

	protected void paintTab(Graphics2D g2, Component c, boolean isSelected, boolean isRollover) {

		if ((isRollover)||(isSelected)) {
			g2.setBackground(Color.black);
			int w=c.getWidth();
			int h=c.getHeight();
			GradientPaint gp=(!isSelected) ? new GradientPaint(0,0,TAB_ROLLOVER_HIGHLIGHT_LIGHT,0,h/2,TAB_ROLLOVER_HIGHLIGHT_DARK) :
				new GradientPaint(0,0,TAB_SELECT_HIGHLIGHT_LIGHT,0,h/2,TAB_SELECT_HIGHLIGHT_DARK);
			Shape s=new RoundRectangle2D.Double(2,0,w-4,h,8,6);                  
			Area a=new Area(s);
			a.add(new Area(new Rectangle2D.Double(0,h-7,w,7)));
			a.subtract(new Area(new RoundRectangle2D.Double(-5,0,7,h,4,4)));                  
			a.subtract(new Area(new RoundRectangle2D.Double(w-2,0,7,h,4,4)));                  
			g2.setPaint(gp);
			g2.fill(a);
			if (!isSelected) {
				EllipticalGradientPaint egp=new EllipticalGradientPaint(w/2,h,ROLLOVER_GLOW,new Point2D.Double(w-4,20),TAB_ROLLOVER_HIGHLIGHT_DARK);
				g2.setPaint(egp);
				g2.fillRect(2,h-15,w-4,15);
				g2.setColor(ROLLOVER_BORDER);
				g2.draw(a);
			}
		}
	}

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
	
		AbstractButton b=(AbstractButton)c;
		ButtonModel model=b.getModel();
		Dimension size=b.getSize();
		FontMetrics fm=g2.getFontMetrics();
		Insets i=c.getInsets();

		Rectangle viewRect=new Rectangle(size);
		viewRect.x+=i.left;
		viewRect.y+=i.top;
		viewRect.width-=(i.right+viewRect.x);
		viewRect.height-=(i.bottom+viewRect.y);

		Rectangle iconRect=new Rectangle();
		Rectangle textRect=new Rectangle();
		Font f=c.getFont();
		g2.setFont(f);
		String text=SwingUtilities.layoutCompoundLabel(c,fm,b.getText(),b.getIcon(),b.getVerticalAlignment(),b.getHorizontalAlignment(),
																									 b.getVerticalTextPosition(),b.getHorizontalTextPosition(),viewRect,iconRect,
																									 textRect,(b.getText()==null) ? 0 : b.getIconTextGap());

		paintTab(g2,c,model.isSelected(),model.isRollover());
		c.setForeground((model.isSelected()) ? TOGGLE_TAB_FOREGROUND_SELECTED : TOGGLE_TAB_FOREGROUND);
		c.setBackground((model.isSelected()) ? Color.white : Color.black);
		if (text!=null && !text.equals("")) {
			View v=(View)c.getClientProperty(BasicHTML.propertyKey);
			if (v!=null) v.paint(g2,textRect); else paintText(g2,c,textRect,text);
		}
		g2.dispose();
	}
}
