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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.jvnet.flamingo.common.JPopupPanel;
import org.jvnet.flamingo.common.PopupActionListener;
import org.jvnet.flamingo.common.PopupPanelManager;

// =======================================================================

public class RibbonStripButton extends JButton {

	private static final Color BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopLight");
	private static final Color BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopDark");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomLight");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomDark");

	private static final Polygon ARROW=new Polygon(new int[] {0,5,2},new int[] {0,0,3},3);   

	// =======================================================================

	JPopupPanel popupPanel=null;
	boolean hasPopup;

	public RibbonStripButton(Icon icon, boolean hasPopup) {

		super(icon);
		setPreferredSize(new Dimension(24+((hasPopup) ? 10 : 0),22)); 
		setBorder(null);
		setOpaque(false);
		this.hasPopup=hasPopup;
		setContentAreaFilled(false);
		addMouseMotionListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent me) {repaint();}
			public void mouseExited(MouseEvent me) {repaint();}
			public void mouseMoved(MouseEvent me) {repaint();}});
		if (hasPopup) {
			addActionListener(createPopupActionListener()); 
		}
	}

	// =======================================================================

	public JPopupPanel getPopupPanel() {return popupPanel;}
	public void setPopupPanel(JPopupPanel x) {popupPanel=x;}

	// =======================================================================

	public void paintComponent(Graphics g) {

		Point p=getMousePosition(true);
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		if (!isEnabled()) {
			g2.setPaint(new Color(0,0,0,32));
			g2.fillRect(1,1,22,20);
			if (hasPopup) g2.fillRect(23,1,10,20);
		} else if (isSelected()||(p!=null)) {
			if (!hasPopup) {
				GradientPaint gp1=new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT,0,8,BUTTON_TOP_HIGHLIGHT_DARK);
				GradientPaint gp2=new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_DARK,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT);
				g2.setPaint(gp1);
				g2.fillRect(1,1,22,7);
				g2.setPaint(gp2);
				g2.fillRect(1,8,22,13);
			} else {
				GradientPaint gp1=(p.x<24) ? new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT,0,8,BUTTON_TOP_HIGHLIGHT_DARK) : 
					new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT.brighter(),0,8,BUTTON_TOP_HIGHLIGHT_LIGHT);
				GradientPaint gp2=(p.x<24) ? new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_DARK,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT) : 
					new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_LIGHT,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT.brighter());
				GradientPaint gp3=(p.x<24) ? new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT.brighter(),0,8,BUTTON_TOP_HIGHLIGHT_LIGHT) : 
					new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT,0,8,BUTTON_TOP_HIGHLIGHT_DARK);
				GradientPaint gp4=(p.x<24) ? new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_LIGHT,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT.brighter()) : 
					new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_DARK,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT);
				g2.setPaint(gp1);
				g2.fillRect(1,1,22,7);
				g2.setPaint(gp2);
				g2.fillRect(1,8,22,13);
				g2.setPaint(gp3);
				g2.fillRect(23,1,10,8);
				g2.setPaint(gp4);
				g2.fillRect(23,8,10,12);
				g2.setColor(new Color(128,128,128,128));
				g2.drawLine(22,0,22,21);
				g2.setColor(new Color(255,255,255,128));
				g2.drawLine(23,0,23,21);
			}
		}
		if (hasPopup) {
			g2.setColor(Color.darkGray);
			g2.translate(getWidth()-8,getHeight()/2-1);
			g2.fill(ARROW);
			g2.dispose();
			g.translate(-5,0);
		}
		super.paintComponent(g);
	}

	// =======================================================================

	private PopupActionListener createPopupActionListener() {

		return new PopupActionListener() {

			public void actionPerformed(ActionEvent ae) {
			
				Point p=((JButton)ae.getSource()).getMousePosition();
				if (p.x<24) return;
				PopupPanelManager.defaultManager().hidePopups(RibbonStripButton.this);
				final JPopupPanel jpp=getPopupPanel();
				if (jpp!=null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							PopupFactory popupFactory=PopupFactory.getSharedInstance();
							int x=getLocationOnScreen().x;
							int y=getLocationOnScreen().y+getSize().height;
							Rectangle scrBounds=getGraphicsConfiguration().getBounds();
							int pw=jpp.getPreferredSize().width;
							if ((x+pw)>(scrBounds.x+scrBounds.width)) x=scrBounds.x+scrBounds.width-pw;
							int ph=jpp.getPreferredSize().height;
							if ((y+ph)>(scrBounds.y+scrBounds.height)) y=scrBounds.y+scrBounds.height-ph;
							Popup popup=popupFactory.getPopup(RibbonStripButton.this,jpp,x,y);
							PopupPanelManager.defaultManager().addPopup(RibbonStripButton.this,popup,jpp);
						}
					});
				}
			}
		};
	}
}
