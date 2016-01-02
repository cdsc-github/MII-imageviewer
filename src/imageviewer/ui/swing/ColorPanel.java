/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

// =======================================================================

public class ColorPanel extends JPanel implements ActionListener {

	private static final Color[] DEFAULT_COLORS={Color.red,Color.yellow,Color.green,Color.cyan,Color.magenta,
																							 Color.red.darker(),Color.orange,Color.green.darker(),Color.blue,Color.magenta.darker(),
																							 Color.pink,Color.black,Color.darkGray,Color.gray,Color.white};

	// =======================================================================

	Color currentColor=null;

	public ColorPanel() {this(DEFAULT_COLORS);}

	public ColorPanel(Color[] palette) {

		super(new GridLayout(3,5,1,1)); 
		setOpaque(false);
		for (int loop=0, n=palette.length; loop<n; loop++) {
			JButton colorButton=createColorButton(palette[loop]);
			add(colorButton);
		}
		currentColor=palette[0];
		setBorder(new EmptyBorder(3,5,1,0));
	}

	// =======================================================================

	private JButton createColorButton(final Color buttonColor) {

		Point2D start=new Point2D.Float(0,0);
		Point2D end=new Point2D.Float(0,20);
		float[] dist={0.0f,0.25f,0.8f};
		Color[] colors={buttonColor.brighter(),buttonColor.darker(),buttonColor};
		final LinearGradientPaint lgp=new LinearGradientPaint(start,end,dist,colors);

		Icon i=new Icon() {
			public int getIconHeight() {return 20;}
			public int getIconWidth() {return 20;}	
			public void paintIcon(Component c, Graphics g, int x, int y) {
				Graphics2D g2=(Graphics2D)g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
				g2.setPaint(lgp);
				g2.fillRoundRect(x+1,y+1,18,18,4,4);
				g2.setColor(new Color(255,255,255,64));
				g2.drawLine(x+1,y+2,x+1,y+17);
				g2.dispose();
			}
		};
		JButton jb=new JButton(i);
		jb.setBorder(null);
		jb.setOpaque(false);
		jb.setSize(new Dimension(24,24));
		jb.putClientProperty("__buttonColor",buttonColor);
		jb.addActionListener(this);
		jb.setContentAreaFilled(false);
		return jb;
	}

	// =======================================================================

	public Color getCurrentColor() {return currentColor;}

	// =======================================================================

	public void actionPerformed(ActionEvent ae) {

		JButton jb=(JButton)ae.getSource();
		currentColor=(Color)jb.getClientProperty("__buttonColor");
	}
}
