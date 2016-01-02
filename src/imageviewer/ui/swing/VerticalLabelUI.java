/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicLabelUI;
import javax.swing.text.View;

// =======================================================================

public class VerticalLabelUI extends BasicLabelUI {

	static {labelUI=new VerticalLabelUI(false);}

	private static Rectangle paintIconR=new Rectangle();
	private static Rectangle paintTextR=new Rectangle();
	private static Rectangle paintViewR=new Rectangle();
	private static Insets paintViewInsets=new Insets(0,0,0,0);

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

	// =======================================================================
	
	protected boolean clockwise;

	public VerticalLabelUI(boolean clockwise) {super();	this.clockwise=clockwise;}
	
	// =======================================================================

	public Dimension getPreferredSize(JComponent c) {
		Dimension dim=super.getPreferredSize(c);
		return new Dimension(dim.height,dim.width);
	}	

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		JLabel label=(JLabel)c;
		String text=label.getText();
		Icon icon=(label.isEnabled()) ? label.getIcon() : label.getDisabledIcon();
		if ((icon==null)&&(text==null)) return;

		FontMetrics fm=g.getFontMetrics();
		paintViewInsets=c.getInsets(paintViewInsets);

		if (clockwise) {
			paintViewR.x=paintViewInsets.top;
			paintViewR.y=paintViewInsets.right;
		} else {
			paintViewR.x=paintViewInsets.bottom;
			paintViewR.y=paintViewInsets.left;
		}

		// paintViewR.x=paintViewInsets.left;
		// paintViewR.y=paintViewInsets.top;
    	
		// Use inverted height & width

		paintViewR.height=c.getWidth()-(paintViewInsets.left+paintViewInsets.right);
		paintViewR.width=c.getHeight()-(paintViewInsets.top+paintViewInsets.bottom);
		paintIconR.x=paintIconR.y=paintIconR.width=paintIconR.height=0;
		paintTextR.x=paintTextR.y=paintTextR.width=paintTextR.height=0;

		String clippedText=layoutCL(label,fm,text,icon,paintViewR,paintIconR,paintTextR);

		Graphics2D g2=(Graphics2D)g;
		AffineTransform tr=g2.getTransform();
		if (clockwise) {
			g2.rotate(Math.PI/2); 
			g2.translate(0,-c.getWidth());
		}	else {
			g2.rotate(-Math.PI/2); 
			g2.translate(-c.getHeight(),0);
		}

		if (icon!=null) icon.paintIcon(c,g,paintIconR.x,paintIconR.y);

		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);
    if (text!=null) {
	    View v=(View)c.getClientProperty(BasicHTML.propertyKey);
	    if (v!=null) {
				v.paint(g2,paintTextR);
	    } else {
				int textX=paintTextR.x;
				int textY=paintTextR.y+fm.getAscent();
				if (label.isEnabled()) {
					paintEnabledText(label,g2,clippedText,textX,textY);
				}	else {
					paintDisabledText(label,g2,clippedText,textX,textY);
				}
			}
		}
		g2.setComposite(oldComposite);
		g2.setTransform(tr);
	}

	// =======================================================================

  protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {

		Graphics2D g2=(Graphics2D)g;
		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);	
		super.paintEnabledText(l,g2,s,textX,textY);
		g2.setComposite(oldComposite);
	}

	protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
		
		Graphics2D g2=(Graphics2D)g;
		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);	
		super.paintDisabledText(l,g2,s,textX,textY);
		g2.setComposite(oldComposite);
	}
}
