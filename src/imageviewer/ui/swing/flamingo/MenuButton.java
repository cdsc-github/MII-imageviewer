/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.UIManager;

import org.jvnet.flamingo.common.JCommandButton;

// =======================================================================

public abstract class MenuButton extends JButton {

	private static final Color BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.topHighlightLight");
	private static final Color BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.topHighlightDark");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.bottomHighlightLight");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.bottomHighlightDark");
	private static final Color BUTTON_BORDER=UIManager.getColor("RibbonBandButton.borderColor");

	private static final Font BUTTON_FONT=UIManager.getFont("Button.font");

	// =======================================================================

	protected String textTitle=null;

	BasicResizableIcon bsi=null;
	Insets i=null;

	public MenuButton(JCommandButton jcb, Insets i) {super(); bsi=(BasicResizableIcon)((BasicResizableIcon)(jcb.getIcon())).clone(); this.i=i;}
	public MenuButton(BasicResizableIcon bsi) {this(bsi,new Insets(1,1,1,1));}
	public MenuButton(BasicResizableIcon bsi, Insets i) {super(); this.bsi=bsi; this.i=i;}

	// =======================================================================

	public void paintComponent(Graphics g) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		boolean isMouseOver=(getMousePosition(true)!=null);
		int width=getWidth()-(i.left+i.right+4);
		int height=getHeight()-(i.top+i.bottom);
		if (isMouseOver) {
			int buttonHalf=(int)Math.round(height/2);
			Shape s1=new RoundRectangle2D.Double(i.left+1,i.top+1,width,buttonHalf,4,4);				
			Area a1=new Area(s1);
			a1.add(new Area(new Rectangle(i.left,i.top+buttonHalf-6,width,6)));
			GradientPaint gp1=new GradientPaint(i.left,i.top,BUTTON_TOP_HIGHLIGHT_LIGHT,i.left,i.top+buttonHalf,BUTTON_TOP_HIGHLIGHT_DARK);
			g2.setPaint(gp1);
			g2.fill(a1);
			Shape s2=new RoundRectangle2D.Double(i.left+1,i.top+buttonHalf,width,buttonHalf,4,4);				
			Area a2=new Area(s2);
			a2.add(new Area(new Rectangle(i.left,i.top+buttonHalf,width,4)));
			GradientPaint gp2=new GradientPaint(i.left,i.top+buttonHalf,BUTTON_BOTTOM_HIGHLIGHT_DARK,i.left,i.top+height,BUTTON_BOTTOM_HIGHLIGHT_LIGHT);
			g2.setPaint(gp2);
			g2.fill(a2);
			g2.setColor(BUTTON_BORDER);
			g2.drawRoundRect(i.left,i.top,width,height,6,6);
		} 
		g2.dispose();
	}

	// =======================================================================

	protected void paintSmallMenuButton(Graphics g, int buttonSize, int iconSize) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		int height=getHeight()-(i.top+i.bottom);
		bsi.setDimension(new Dimension(iconSize,iconSize));
		bsi.paintIcon(this,g2,i.left+3,i.top+((height-iconSize)/2));
		g2.setFont(BUTTON_FONT);
		FontMetrics fm=g2.getFontMetrics();
		g2.setColor(Color.darkGray);
		Rectangle2D r=fm.getStringBounds(textTitle,g2);
		g2.drawString(textTitle,i.left+buttonSize+5,2+(int)((height-r.getHeight())/2)+fm.getAscent());
		g2.dispose();
	}

	protected void paintSmallToggleMenuButton(Graphics g, int buttonSize, int iconSize) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		int height=getHeight()-(i.top+i.bottom);
		bsi.setDimension(new Dimension(iconSize,iconSize));
		if (isSelected())	bsi.paintIcon(this,g2,i.left+2,1+i.top+((height-iconSize)/2));
		g2.setFont(BUTTON_FONT);
		FontMetrics fm=g2.getFontMetrics();
		g2.setColor(Color.darkGray);
		Rectangle2D r=fm.getStringBounds(textTitle,g2);
		g2.drawString(textTitle,i.left+buttonSize+5,2+(int)((height-r.getHeight())/2)+fm.getAscent());
		g2.dispose();
	}

	// =======================================================================

	public String getTitle() {return textTitle;}
	public void setTitle(String x) {textTitle=x;}
}
