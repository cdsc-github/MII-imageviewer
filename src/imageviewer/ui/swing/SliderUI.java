/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;

import javax.swing.JComponent;
import javax.swing.JSlider;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalSliderUI;

// =======================================================================

public class SliderUI extends MetalSliderUI {

	protected static final GradientPaint HORIZONTAL_PAINT1=new GradientPaint(0,0,Color.white,0,4,Color.gray);
	protected static final GradientPaint HORIZONTAL_PAINT2=new GradientPaint(0,0,Color.gray,0,4,Color.darkGray);

	// =======================================================================
	// Create once and reuse so we don't keep allocating 'em...

	protected static final Ellipse2D.Double lCircle=new Ellipse2D.Double(0,0,6,6);
	protected static final Ellipse2D.Double rCircle=new Ellipse2D.Double(0,0,6,6);
	protected static final Rectangle lRect=new Rectangle(0,0,1,6);
	protected static final Rectangle rRect=new Rectangle(0,0,1,6);

	public static ComponentUI createUI(JComponent c) {return new SliderUI();}

	// =======================================================================
	// Override defaults...

	protected int getThumbOverhang() {return 0;}
	protected Dimension getThumbSize() {return (slider.getOrientation()==JSlider.HORIZONTAL) ? new Dimension(3,10) : new Dimension(10,3);}

	public void paintThumb(Graphics g) {}
	
	// =======================================================================

	public void paintTrack(Graphics g) {

		Graphics2D g2=(Graphics2D)g;
		Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		boolean leftToRight=slider.getComponentOrientation().isLeftToRight();

		if (slider.getOrientation()==JSlider.HORIZONTAL) {

			// Compute the left side bar...
			
			lCircle.x=trackRect.x;
			lCircle.y=trackRect.y;
			lRect.x=trackRect.x+3;
			lRect.y=trackRect.y;
			lRect.width=(int)Math.min(thumbRect.x-(trackRect.x+3),trackRect.x+trackRect.width-12);
			Area leftBar=new Area(lCircle);
			leftBar.add(new Area(lRect));
			g2.setPaint(HORIZONTAL_PAINT1);
			g2.fill(leftBar);
			
			// Compute the right side bar...
			
			rCircle.x=trackRect.x+trackRect.width-6;
			rCircle.y=trackRect.y;
			rRect.x=(int)Math.max(trackRect.x+8,thumbRect.x+thumbRect.width);
			rRect.y=trackRect.y;
			rRect.width=trackRect.x+trackRect.width-3-rRect.x;
			Area rightBar=new Area(rRect);
			rightBar.add(new Area(rCircle));
			g2.setPaint(HORIZONTAL_PAINT2);
			g2.fill(rightBar);

			// Outline both bars...
			
			g2.setColor(Color.darkGray);
			g2.draw(leftBar);
			g2.draw(rightBar);
		} else {

		}
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
	}

	// =======================================================================
	// More overrides from MetalSliderUI and BasicSliderUI...

  public int getTickLength() {return 7;}

	protected void paintMinorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {g.setColor(Color.darkGray);	g.drawLine(x,1,x,tickBounds.height/2-1);}
	protected void paintMajorTickForHorizSlider(Graphics g, Rectangle tickBounds, int x) {g.setColor(Color.darkGray); g.drawLine(x,1,x,tickBounds.height-2);}

}
