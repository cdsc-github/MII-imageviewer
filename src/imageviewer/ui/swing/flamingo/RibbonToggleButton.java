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
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

// =======================================================================

public class RibbonToggleButton extends JToggleButton {

	private static final Color BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopLight");
	private static final Color BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightTopDark");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomLight");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("JRibbon.buttonStripButtonHighlightBottomDark");

	private static final Color BOTTOM_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonHighlightBottomLight");
	private static final Color BOTTOM_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonHighlightBottomDark");
	private static final Color TOP_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonHighlightTopLight");
	private static final Color TOP_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonHighlightTopDark");
	private static final Color ROLLOVER_BOTTOM_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonRolloverBottomLight");
	private static final Color ROLLOVER_BOTTOM_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonRolloverBottomDark");
	private static final Color ROLLOVER_TOP_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonRolloverTopLight");
	private static final Color ROLLOVER_TOP_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonRolloverTopDark");

	public RibbonToggleButton(Icon icon, boolean isSelected) {super(icon,isSelected); initialize();}
	public RibbonToggleButton(String name, boolean isSelected) {super(name,isSelected); initialize();}
	public RibbonToggleButton(String name, Icon icon, boolean isSelected) {super(name,icon,isSelected); initialize();}

	private void initialize() {

		setBorder(null);
		setOpaque(false);
		setRolloverEnabled(true);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setPreferredSize(new Dimension(24,22)); 
		putClientProperty("ToggleButton.buttonPaintsRollover",Boolean.FALSE);
		putClientProperty("ToggleButton.buttonPaintsPress",Boolean.FALSE);
	}

	// =======================================================================

	public void paintComponent(Graphics g) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		if (!isEnabled()) {
			g2.setPaint(new Color(0,0,0,32));
			g2.fillRect(1,1,22,20);
		} else if (isSelected()) {
			boolean b=(getModel().isRollover());
			GradientPaint gp1=new GradientPaint(0,0,b ? ROLLOVER_TOP_TOGGLE_LIGHT : TOP_TOGGLE_LIGHT,0,8,b ? ROLLOVER_TOP_TOGGLE_DARK : TOP_TOGGLE_DARK);
			GradientPaint gp2=new GradientPaint(0,8,b ? ROLLOVER_BOTTOM_TOGGLE_DARK : BOTTOM_TOGGLE_DARK,0,20,b ? ROLLOVER_BOTTOM_TOGGLE_LIGHT : BOTTOM_TOGGLE_LIGHT);
			g2.setPaint(gp1);
			g2.fillRect(1,1,22,8);
			g2.setPaint(gp2);
			g2.fillRect(1,8,22,13);
		} else if (getModel().isRollover()) {
			GradientPaint gp1=new GradientPaint(0,0,BUTTON_TOP_HIGHLIGHT_LIGHT,0,8,BUTTON_TOP_HIGHLIGHT_DARK);
			GradientPaint gp2=new GradientPaint(0,8,BUTTON_BOTTOM_HIGHLIGHT_DARK,0,20,BUTTON_BOTTOM_HIGHLIGHT_LIGHT);
			g2.setPaint(gp1);
			g2.fillRect(1,1,22,8);
			g2.setPaint(gp2);
			g2.fillRect(1,8,22,13);
		}
		super.paintComponent(g);
	}
}
