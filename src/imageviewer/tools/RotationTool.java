/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.EventObject;

import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.graphics.IndicatorBar;
import imageviewer.ui.swing.undo.RotationEdit;

// =======================================================================

public class RotationTool extends ImagingTool implements Tool {

	private static final double DEGREES_90=Math.toRadians(90);
	private static final double DEGREES_180=Math.toRadians(180);
	private static final double DEGREES_270=Math.toRadians(270);

	// =======================================================================

	double angle=0, originalAngle=0;
	int midX=0, midY=0;
	RenderingProperties rp=null;

	public RotationTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Rotate");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {execute(e);}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {prepare(e);}
	public void mouseReleased(MouseEvent e) {finish(e);}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	// =======================================================================

	public void prepare(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			midX=(int)(ip.getWidth()/2);
			midY=(int)(ip.getHeight()/2);
			rp=ip.getPipelineRenderer().getRenderingProperties();
			originalAngle=angle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();
			addIndicatorBar(ip,new RotationIndicatorBar());
			ip.repaint();
			execute(e);
		}
	}

	// =======================================================================

	public void execute(MouseEvent e) {

		// Rotate the image to the angle formed between the midpoint of
		// the image panel (not the image!) and the current location of
		// the mouse pointer.  First, compute the tangent formed between
		// the center of the panel and the current location of the mouse.

		int x=e.getX();
		int y=e.getY();

		double opposite=(double) (midY-y);
		if (opposite<0) opposite=-opposite;
		double adjacent=(double)(x-midX);
		if (adjacent<0) adjacent=-adjacent;

		// Handle the case when a 90 or 270 degree rotation is required;
		// the difference will be whether the y-point is above or below
		// the vertical midpoint.  Manage the 0/180 degree rotation
		// likewise.  If an angle computation is required, determine what
		// to do based on the corresponding quadrant (do nothing for Quad
		// I).

		angle=0;
		if (adjacent==0) {
			if (y>midY) angle=DEGREES_180;
		} else if (opposite==0) {
			if (x<midX) angle=DEGREES_90;
			if (x>midX) angle=DEGREES_270;
		} else {
			double tangentialAngle=Math.toDegrees(Math.atan((opposite/adjacent)));
			if ((x<midX)&&(y<midY)) tangentialAngle=180-tangentialAngle;
			if ((x<midX)&&(y>midY)) tangentialAngle+=180;
			if ((x>midX)&&(y>midY)) tangentialAngle=360-tangentialAngle;
			angle=Math.toRadians(tangentialAngle-90);
		}
		rp.setProperties(new String[] {RenderingProperties.ROTATION},new Object[] {new Float(-angle)});
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ip.repaint();
		}
	}

	// =======================================================================

	public void finish(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			RotationEdit re=new RotationEdit(ip,originalAngle,angle);
			ApplicationContext.postEdit(re);
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.ROTATION},new Object[] {new Float(-angle)});
			removeIndicatorBar(ip);
			ip.repaint();
		}
	}

	// =======================================================================

	private class RotationIndicatorBar extends IndicatorBar {

		double angleRatio=0;

		public RotationIndicatorBar() {super(); angleRatio=(BAR_WIDTH-4)/6.28;}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

			// First, paint the basic bar given the alpha composite. Then paint
			// the specific bar information given for the angle rotation settings.

			paintBar(g2);
			double x=(angle>=0) ? (angle*angleRatio) : ((6.28+angle)*angleRatio);
			g2.setPaint(Color.black);
			g2.fillRect(2+(int)x,2,1,BAR_HEIGHT-2);
			String str=(angle>=0) ? (new String((int)Math.toDegrees(angle)+" degrees")) : (new String((360+(int)Math.toDegrees(angle))+" degrees"));
			paintText(g2,str,BAR_WIDTH+1,1);
			g2.dispose();
		}
	}
}
