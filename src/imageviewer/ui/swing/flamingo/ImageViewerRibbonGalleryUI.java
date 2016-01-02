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
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ComponentUI;

import org.jvnet.flamingo.common.JCommandToggleButton;
import org.jvnet.flamingo.common.JIconPopupPanel;
import org.jvnet.flamingo.common.PopupPanelManager;
import org.jvnet.flamingo.ribbon.ui.BasicRibbonGalleryUI;

// =======================================================================

public class ImageViewerRibbonGalleryUI extends BasicRibbonGalleryUI {

	public static final Color BACKGROUND_COLOR=UIManager.getColor("JRibbonGallery.background");
	public static final Color SCROLL_BUTTON_TOP_LIGHT=new Color(219,223,228);
	public static final Color SCROLL_BUTTON_TOP_DARK=new Color(216,221,226);
	public static final Color SCROLL_BUTTON_BOTTOM_LIGHT=new Color(205,210,216);
	public static final Color SCROLL_BUTTON_BOTTOM_DARK=new Color(180,185,194);
	public static final Color BORDER_COLOR=new Color(142,142,142);

	private static final Color ROLLOVER_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopLight");
	private static final Color ROLLOVER_TOP_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopDark");
	private static final Color ROLLOVER_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomLight");
	private static final Color ROLLOVER_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomDark");

	private static final Polygon DOWN_ARROW=new Polygon(new int[] {0,5,2},new int[] {0,0,3},3);   
	private static final Polygon UP_ARROW=new Polygon(new int[] {0,5,2},new int[] {3,3,0},3);   

	public static ComponentUI createUI(JComponent c) {return new ImageViewerRibbonGalleryUI();}

	public void installUI(JComponent c) {super.installUI(c); c.setOpaque(false);}
	protected void installDefaults() {super.installDefaults(); margin=new Insets(1,1,1,1); ribbonGallery.setBorder(new EmptyBorder(0,1,0,0));}

	// =======================================================================

	protected void installListeners() {

		super.installListeners();
		expandActionButton.removeActionListener(expandListener);
		expandListener=createExpandActionListener();
		expandActionButton.addActionListener(expandListener);
	}

	private ActionListener createExpandActionListener() {

		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PopupPanelManager.defaultManager().hidePopups(ribbonGallery);
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						PopupFactory popupFactory=PopupFactory.getSharedInstance();
						JIconPopupPanel iconPopupGallery=(JIconPopupPanel)ribbonGallery.getClientProperty("popupIconGallery");
						ribbonGallery.setShowingPopupGallery(true);
						if (iconPopupGallery==null) return;
						iconPopupGallery.doLayout();
						int x=ribbonGallery.getLocationOnScreen().x;
						int y=ribbonGallery.getLocationOnScreen().y;
						Rectangle scrBounds=ribbonGallery.getGraphicsConfiguration().getBounds();
						int pw=iconPopupGallery.getPreferredSize().width;
						if ((x+pw)>(scrBounds.x+scrBounds.width)) x=scrBounds.x+scrBounds.width-pw;
						int ph=iconPopupGallery.getPreferredSize().height;
						if ((y+ph)>(scrBounds.y+scrBounds.height)) y=scrBounds.y+scrBounds.height-ph;
						Popup popup=popupFactory.getPopup(ribbonGallery,iconPopupGallery,x,y);
						ribbonGallery.repaint();
						PopupPanelManager.defaultManager().addPopup(ribbonGallery,popup,iconPopupGallery);
					}
				});
			}
		};
	}

	// =======================================================================

	protected AbstractButton createScrollUpButton() {

		JButton jb=new JButton() {
			public void paintComponent(Graphics g) {
				Graphics2D g2=(Graphics2D)g.create();
				boolean isMouseOver=(getMousePosition(true)!=null);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
				int width=getWidth()-1;
				int height=getHeight();
				int halfHeight=(int)Math.round(height/2);
				Shape s1=new RoundRectangle2D.Double(0,0,width,halfHeight,6,6);				
				Area a1=new Area(s1);
				a1.add(new Area(new Rectangle2D.Double(0,0,6,6)));
				a1.add(new Area(new Rectangle2D.Double(0,halfHeight-6,width,6)));
				GradientPaint gp1=new GradientPaint(0,0,(!isEnabled()) ? SCROLL_BUTTON_TOP_LIGHT.brighter() : ((isMouseOver) ? ROLLOVER_TOP_HIGHLIGHT_LIGHT : SCROLL_BUTTON_TOP_LIGHT),
																						0,halfHeight,(!isEnabled()) ? SCROLL_BUTTON_TOP_DARK : ((isMouseOver) ? ROLLOVER_TOP_HIGHLIGHT_DARK : SCROLL_BUTTON_TOP_DARK));
				g2.setPaint(gp1);
				g2.fill(a1);
				GradientPaint gp2=new GradientPaint(0,height/2,(!isEnabled()) ? SCROLL_BUTTON_BOTTOM_DARK : ((isMouseOver) ? ROLLOVER_BOTTOM_HIGHLIGHT_DARK : SCROLL_BUTTON_BOTTOM_DARK),
																						0,height,(!isEnabled()) ? SCROLL_BUTTON_BOTTOM_LIGHT.brighter() : ((isMouseOver) ? ROLLOVER_BOTTOM_HIGHLIGHT_LIGHT : SCROLL_BUTTON_BOTTOM_LIGHT));
				g2.setPaint(gp2);
				g2.fillRect(0,height/2,width,height/2);
				g2.setColor(BORDER_COLOR);
				a1.add(new Area(new Rectangle2D.Double(0,height/2,width,height/2)));
				g2.draw(a1);
				g2.setColor((isEnabled()) ? Color.darkGray : Color.gray);
				g2.translate(width-10,halfHeight-1);
				g2.fill(UP_ARROW);
				g2.dispose();
			}
		};
		jb.setBorder(null);
		jb.setOpaque(false);
		return jb;
	}

	// =======================================================================

	protected AbstractButton createScrollDownButton() {

		JButton jb=new JButton() {
			public void paintComponent(Graphics g) {
				Graphics2D g2=(Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
				boolean isMouseOver=(getMousePosition(true)!=null);
				int width=getWidth()-1;
				int height=getHeight();
				int halfHeight=(int)Math.round(height/2);
				GradientPaint gp1=new GradientPaint(0,0,(!isEnabled()) ? SCROLL_BUTTON_TOP_LIGHT.brighter() : ((isMouseOver) ? ROLLOVER_TOP_HIGHLIGHT_LIGHT : SCROLL_BUTTON_TOP_LIGHT),
																						0,halfHeight,(!isEnabled()) ? SCROLL_BUTTON_TOP_DARK : ((isMouseOver) ? ROLLOVER_TOP_HIGHLIGHT_DARK : SCROLL_BUTTON_TOP_DARK));
				g2.setPaint(gp1);
				g2.fillRect(0,0,width,height/2);
				GradientPaint gp2=new GradientPaint(0,height/2,(!isEnabled()) ? SCROLL_BUTTON_BOTTOM_DARK : ((isMouseOver) ? ROLLOVER_BOTTOM_HIGHLIGHT_DARK : SCROLL_BUTTON_BOTTOM_DARK),
																						0,height,(!isEnabled()) ? SCROLL_BUTTON_BOTTOM_LIGHT.brighter() : ((isMouseOver) ? ROLLOVER_BOTTOM_HIGHLIGHT_LIGHT : SCROLL_BUTTON_BOTTOM_LIGHT));
				g2.setPaint(gp2);
				g2.fillRect(0,height/2,width,height/2);

				g2.setColor(BORDER_COLOR);
				g2.drawRect(0,0,width,height);
				g2.setColor((isEnabled()) ? Color.darkGray : Color.gray);
				g2.translate(width-10,halfHeight-1);
				g2.fill(DOWN_ARROW);
				g2.dispose();
			}
		};
		jb.setBorder(null);
		jb.setOpaque(false);
		return jb;
	}

	// =======================================================================

	protected AbstractButton createExpandButton() {

		JButton jb=new JButton() {
			public void paintComponent(Graphics g) {
				Graphics2D g2=(Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
				boolean isMouseOver=(getMousePosition(true)!=null);
				int width=getWidth()-1;
				int height=getHeight()-3;
				int halfHeight=(int)Math.round(height/2);
				GradientPaint gp1=new GradientPaint(0,0,(!isEnabled()) ? SCROLL_BUTTON_TOP_LIGHT.brighter() : ((isMouseOver) ? ROLLOVER_TOP_HIGHLIGHT_LIGHT : SCROLL_BUTTON_TOP_LIGHT),
																						0,halfHeight,(!isEnabled()) ? SCROLL_BUTTON_TOP_DARK : ((isMouseOver) ? ROLLOVER_TOP_HIGHLIGHT_DARK : SCROLL_BUTTON_TOP_DARK));
				g2.setPaint(gp1);
				g2.fillRect(0,0,width,halfHeight);
				Shape s1=new RoundRectangle2D.Double(0,halfHeight,width,halfHeight+1,6,6);				
				Area a1=new Area(s1);
				a1.add(new Area(new Rectangle2D.Double(0,height-6,6,6)));
				a1.add(new Area(new Rectangle2D.Double(0,halfHeight,width,6)));
				GradientPaint gp2=new GradientPaint(0,height/2,(!isEnabled()) ? SCROLL_BUTTON_BOTTOM_DARK : ((isMouseOver) ? ROLLOVER_BOTTOM_HIGHLIGHT_DARK : SCROLL_BUTTON_BOTTOM_DARK),
																						0,height,(!isEnabled()) ? SCROLL_BUTTON_BOTTOM_LIGHT.brighter() : ((isMouseOver) ? ROLLOVER_BOTTOM_HIGHLIGHT_LIGHT : SCROLL_BUTTON_BOTTOM_LIGHT));
				g2.setPaint(gp2);
				g2.fill(a1);
				a1.add(new Area(new Rectangle2D.Double(0,0,width,halfHeight)));
				g2.setColor(BORDER_COLOR);
				g2.draw(a1);
				g2.setColor((isEnabled()) ? Color.darkGray : Color.gray);
				g2.drawLine(getWidth()-11,getHeight()/2-3,getWidth()-7,getHeight()/2-3);
				g2.translate(getWidth()-11,getHeight()/2-1);
				g2.fill(DOWN_ARROW);
				g2.dispose();
			}
		};
		jb.setBorder(null);
		jb.setOpaque(false);
		return jb;
	}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		paintRibbonGalleryBackground(g2);
		paintRibbonGalleryBorder(g2);
		g2.dispose();
	}

	protected void paintRibbonGalleryBorder(Graphics graphics) {}

	protected void paintRibbonGalleryBackground(Graphics graphics) {

		Rectangle toFill=new Rectangle(0,0,ribbonGallery.getWidth(),ribbonGallery.getHeight());
		graphics.setColor(BACKGROUND_COLOR);
		graphics.fillRect(toFill.x,toFill.y,toFill.width-17,toFill.height-3);
		graphics.setColor(BORDER_COLOR);
		graphics.drawRect(toFill.x,toFill.y,toFill.width-16,toFill.height-3);
		graphics.setColor(new Color(64,64,64,150));
		graphics.drawLine(toFill.x+1,toFill.y,toFill.x+toFill.width-18,toFill.y);
		graphics.drawLine(toFill.x+1,toFill.y+1,toFill.x+1,toFill.y+toFill.height-4);
	}

	protected int computeMaxButtonWidth() {

		int maxWidth=0;
		for (int loop=0, n=ribbonGallery.getButtonCount(); loop<n; loop++) {
			JCommandToggleButton jctb=ribbonGallery.getButtonAt(loop);
			int buttonWidth=jctb.getPreferredSize().width;
			if (buttonWidth>maxWidth) maxWidth=buttonWidth;
		}
		return maxWidth;
	}

	public int getPreferredWidth(int buttonCount, int availableHeight) {

		Insets borderInsets=ribbonGallery.getInsets();
		return (buttonCount*computeMaxButtonWidth())+((buttonCount-1)*ImageViewerBandControlPanelUI.X_BORDER)+17+borderInsets.left+borderInsets.right;
	}

	// =======================================================================

	protected LayoutManager createLayoutManager() {return new ImageViewerRibbonGalleryLayout();}

	// =======================================================================

	private class ImageViewerRibbonGalleryLayout implements LayoutManager {

		public void addLayoutComponent(String name, Component c) {}
		public void removeLayoutComponent(Component c) {}

		public Dimension preferredLayoutSize(Container c) {return new Dimension(ribbonGallery.getPreferredWidth(ribbonGallery.getState(),c.getHeight()),c.getHeight());}
		public Dimension minimumLayoutSize(Container c) {return preferredLayoutSize(c);}

		public void layoutContainer(Container c) {
	
			int width=c.getWidth();
			int height=c.getHeight();
			Insets borderInsets=ribbonGallery.getBorder().getBorderInsets(ribbonGallery);

			int totalButtonHeight=height;
			int buttonY=(height-totalButtonHeight)/2;
			int buttonHeight=totalButtonHeight/3;
			int buttonWidth=17;
			int buttonX=width-buttonWidth;

			scrollDownButton.setPreferredSize(new Dimension(buttonWidth,buttonHeight+1));
			scrollUpButton.setPreferredSize(new Dimension(buttonWidth,buttonHeight+1));
			expandActionButton.setPreferredSize(new Dimension(buttonWidth,buttonHeight-1));
			buttonStrip.setBounds(buttonX,buttonY,buttonWidth,totalButtonHeight);
		
			for (int i=0; i<ribbonGallery.getButtonCount(); i++) {
				JCommandToggleButton currButton=ribbonGallery.getButtonAt(i);
				currButton.setVisible(false);
			}

			int currIndex=firstButtonIndex;
			int startX=borderInsets.left;
			int maxButtonWidth=computeMaxButtonWidth();
			if (currIndex<0) {
				startX=buttonX;
				currIndex=lastButtonIndex;
				while (currIndex>=0) {
					firstButtonIndex=currIndex;
					JCommandToggleButton currButton=ribbonGallery.getButtonAt(currIndex);
					int currButtonWidth=maxButtonWidth; 
					int currStartX=startX-currButtonWidth-ImageViewerBandControlPanelUI.X_BORDER;
					if (currStartX<0)	break;
					startX=currStartX;
					currIndex--;
				}
			}

			currIndex=firstButtonIndex;
			startX=borderInsets.left;
			visibleButtonsCount=0;
			while (currIndex<ribbonGallery.getButtonCount()) {
				JCommandToggleButton currButton=ribbonGallery.getButtonAt(currIndex);
				int currButtonWidth=maxButtonWidth;
				int currButtonHeight=currButton.getPreferredSize().height;
				int nextStartX=startX+currButtonWidth+ImageViewerBandControlPanelUI.X_BORDER;
				if (nextStartX>=buttonX) break;
				int topY=(height-currButtonHeight)/2;
				currButton.setVisible(true);
				currButton.setBounds(startX+3,topY+2,currButtonWidth,currButtonHeight-7);
				startX=nextStartX;
				currIndex++;
				visibleButtonsCount++;
			}
			scrollDownButton.setEnabled(currIndex<ribbonGallery.getButtonCount());
			scrollUpButton.setEnabled(firstButtonIndex>0);
		}
	}
}
