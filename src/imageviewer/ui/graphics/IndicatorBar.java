/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.graphics;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.geom.RoundRectangle2D;
import java.awt.geom.RoundRectangle2D.Double;

import javax.swing.JPanel;
import javax.swing.plaf.FontUIResource;

import imageviewer.ui.swing.TextRenderer;

// ============================================================================	

public abstract class IndicatorBar extends JPanel {

	protected static final int BAR_WIDTH=75;
	protected static final int BAR_HEIGHT=10;

	protected static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC);
	protected static final AlphaComposite BAR_COMPOSITE=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.6F);
	protected static final AlphaComposite BORDER_FONT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.75f);

	protected static final GradientPaint BAR_PAINT=new GradientPaint(40,0,Color.white,40,10,new Color(35,35,35));

	protected static final RoundRectangle2D.Double BAR=new RoundRectangle2D.Double(0,1,BAR_WIDTH,BAR_HEIGHT+1,4,4);
	protected static final RoundRectangle2D.Double INNER_BAR=new RoundRectangle2D.Double(2,3,BAR_WIDTH-4,BAR_HEIGHT-3,0,0);

	protected static final Font DEFAULT_FONT=new Font("Tahoma",Font.PLAIN,9);

	// ============================================================================	

	public IndicatorBar() {

		super();
		setLayout(null);
		setOpaque(false);
		setSize(BAR_WIDTH+100,BAR_HEIGHT+5);
	}

	// ============================================================================	

	protected void paintBar(Graphics2D g2) {

		g2.setComposite(BAR_COMPOSITE);
		g2.setPaint(BAR_PAINT);
		g2.fill(BAR);
		g2.setPaint(Color.lightGray);
		g2.fill(INNER_BAR);
	}

	protected void paintText(Graphics2D g2, String str, int x, int y) {

		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		
		g2.setPaint(Color.black);
		g2.setComposite(BAR_COMPOSITE);
		g2.setFont(DEFAULT_FONT);
		g2.fill(new RoundRectangle2D.Double(x,y,(int)(DEFAULT_FONT.getStringBounds(str,g2.getFontRenderContext()).getWidth())+6,BAR_HEIGHT+1,4,4));
		g2.setColor(Color.white);
		g2.setComposite(DEFAULT_AC);
		TextRenderer.drawString(this,g2,str,x+3,BAR_HEIGHT-1);
	}
}
