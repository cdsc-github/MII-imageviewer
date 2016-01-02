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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.text.View;

import org.jvnet.flamingo.common.JCommandButton;

// =======================================================================

public class RibbonMenuPanel extends JPanel {

	private static final Font BUTTON_FONT=UIManager.getFont("Button.font");

	boolean isLarge=false;
	int h=0, w=0, buttonSize=0;

	public RibbonMenuPanel(boolean isLarge, int buttonSize, int maxPopupWidth, ArrayList<JComponent> componentList) {

		super();
		this.isLarge=isLarge;
		this.buttonSize=buttonSize;
		setLayout(null);
		setOpaque(false);
		Insets i=new Insets(1,1,1,1);
		BufferedImage bi=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2=(Graphics2D)bi.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setFont(BUTTON_FONT);
		FontMetrics fm=g2.getFontMetrics();
		for (final JComponent jc : componentList) {
			if (jc instanceof MenuButton) {
				MenuButton mb=(MenuButton)jc;
				if (!isLarge) {
					Rectangle2D r=fm.getStringBounds(mb.getTitle(),g2);
					if (r.getWidth()>w) w=(int)r.getWidth();
				} else {
					View v=(View)mb.getClientProperty("html");
					int textWidth=(int)Math.min(v.getPreferredSpan(View.X_AXIS),maxPopupWidth-buttonSize-10);
					if (textWidth>w) w=textWidth;
				}
			}
			add(jc);
		}
		bi.flush();
		g2.dispose();
		w+=(isLarge) ? 58 : 40;
		Component[] children=getComponents();
		for (int loop=0, y=1; loop<children.length; loop++) {
			if (children[loop] instanceof JButton) {
				((JButton)children[loop]).setBounds(0,y,w,buttonSize);
			
				y+=buttonSize;
				h+=buttonSize;
			} else {
				((JComponent)children[loop]).setBounds(24,y,w-24,2);
				y+=2;
				h+=2;
			}
		}
		h+=6;
	}

	// =======================================================================

	public void paintComponent(Graphics g) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		if (!isLarge) {
			g2.setColor(new Color(255,255,255,64));
			g2.fillRect(2,2,buttonSize-1,getHeight()-2);
			g2.setColor(new Color(0,0,0,64));
			g2.drawLine(buttonSize,2,buttonSize,getHeight()-1);
			g2.setColor(new Color(255,255,255,64));
			g2.drawLine(buttonSize-1,2,buttonSize-1,getHeight()-1);
		}
		g2.dispose();
		super.paintComponent(g);
	}

	// =======================================================================

	public int getComputedWidth() {return w;}
	public int getComputedHeight() {return h;}
}
