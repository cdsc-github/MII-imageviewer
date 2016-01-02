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
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.Polygon;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.CellRendererPane;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;

import imageviewer.ui.swing.ComboBoxButton;

// =======================================================================

public class RibbonComboBoxButton extends ComboBoxButton {

	private static final Polygon ARROW=new Polygon(new int[] {0,5,2},new int[] {0,0,3},3);   

	private static final Color BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopLight");
	private static final Color BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopDark");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomLight");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomDark");

	public RibbonComboBoxButton(JComboBox comboBox, Icon comboIcon, boolean iconOnly, CellRendererPane rendererPane, JList listBox) {

		super(comboBox,comboIcon,iconOnly,rendererPane,listBox);
		setMargin(null);
		setBorder(new LineBorder(new Color(137,137,137),1));
		addMouseMotionListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent me) {repaint();}
			public void mouseExited(MouseEvent me) {repaint();}
			public void mouseMoved(MouseEvent me) {repaint();}});
	}

	// =======================================================================
	// Override the default paint behavior for a JButton.  Note that the
	// *entire* JComboBox region is really a button, so it needs special
	// painting.

	protected void paintComboBoxBackground(Graphics g, Insets insets, int width, int height) {

		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		Point p=getMousePosition(true);
		boolean popupShowing=(((RibbonComboBoxUI)(comboBox.getUI())).isPopupVisible(comboBox));
		if ((p==null)&&(!popupShowing)) {
			g2.setColor(new Color(232,232,232));
			g2.fillRect(0,0,getWidth(),getHeight());
		} else {
			int w=getWidth();
			int h=getHeight();
			g2.setColor(Color.white);
			g2.fillRect(0,0,w-14,h);
			if (((p!=null)&&(p.x>=w-10))||popupShowing) {
				GradientPaint gp1=new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT,0,8,BUTTON_TOP_HIGHLIGHT_DARK);
				g2.setPaint(gp1);
				g2.fillRect(w-14,0,14,8);
				GradientPaint gp2=new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_DARK,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT);
				g2.setPaint(gp2);
				g2.fillRect(w-14,8,14,h-8);
			} else {
				GradientPaint gp1=new GradientPaint(0,0,new Color(235,239,244),0,8,new Color(229,234,240));
				g2.setPaint(gp1);
				g2.fillRect(w-14,0,14,8);
				GradientPaint gp2=new GradientPaint(0,8,new Color(223,228,235),0,15,new Color(239,242,246));
				g2.setPaint(gp2);
				g2.fillRect(w-14,8,14,h-8);
			}
			g2.setColor(new Color(137,137,137));
			g2.drawLine(w-14,0,w-14,h);
		}
	}

	protected void paintComboBoxArrowIcon(Graphics g, int x, int y) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.setColor(Color.darkGray);
		g2.translate(getWidth()-9,getHeight()/2-1);
		g2.fill(ARROW);
		g2.dispose();
	}
}
