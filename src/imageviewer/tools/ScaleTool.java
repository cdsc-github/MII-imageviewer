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
import imageviewer.ui.swing.undo.ScaleEdit;

// =======================================================================

public class ScaleTool extends ImagingTool implements Tool {

	protected static final double MIN_ZOOM=0.1;
	protected static final double MAX_ZOOM=10.0;

	// =======================================================================

	double scale=0, startScale=0, startTranslateX=0, startTranslateY=0, heightRatio=0;
	double newX=0, newY=0, imageWidth=0, imageHeight=0;
	RenderingProperties rp=null;
	Point start=null;
	boolean imageCenteredZoom=false;

	public ScaleTool() {}
	public ScaleTool(boolean imageCenteredZoom) {this.imageCenteredZoom=imageCenteredZoom;}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Scale image");}

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

	public boolean isImageCenteredZoom() {return imageCenteredZoom;}
	public void setImageCenteredZoom(boolean x) {imageCenteredZoom=x;}

	// =======================================================================

	public void prepare(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			rp=ip.getPipelineRenderer().getRenderingProperties();
			startScale=scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
			start=e.getPoint();
			heightRatio=512/(double)ip.getHeight();
			startTranslateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
			startTranslateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
			imageWidth=ip.getSource().getWidth()/2;
			imageHeight=ip.getSource().getHeight()/2;
			addIndicatorBar(ip,new ScaleIndicatorBar());
			ip.repaint();
		}
	}

	// =======================================================================

	public void execute(MouseEvent e) {

		double delta=-((e.getY()-start.y)*0.005*heightRatio);
		scale=startScale+delta;
		if (scale<MIN_ZOOM) scale=MIN_ZOOM;
		if (scale>MAX_ZOOM) scale=MAX_ZOOM;

		// Compute the translation required to move the image so that it
		// is centered around the starting mouse point.

		if (imageCenteredZoom) {
			newX=(startTranslateX*scale/startScale)-((imageWidth*scale/2*startScale))+(imageWidth/2);
			newY=(startTranslateY*scale/startScale)-((imageHeight*scale/2*startScale))+(imageHeight/2);
		} else {
			newX=(startTranslateX*scale/startScale)-((start.x*scale/startScale))+start.x;
			newY=(startTranslateY*scale/startScale)-((start.y*scale/startScale))+start.y;
		}

		rp.setProperties(new String[] {RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
										 new Object[] {new Double(scale),new Double(newX),new Double(newY)});
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ip.repaint();
		}
	}

	// =======================================================================

	public void finish(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ScaleEdit se=new ScaleEdit(ip,startScale,startTranslateX,startTranslateY,scale,newX,newY);
			ApplicationContext.postEdit(se);
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
																 new Object[] {new Double(scale),new Double(newX),new Double(newY)});
			removeIndicatorBar(ip);
			ip.repaint();
		}
	}

	// =======================================================================

	private class ScaleIndicatorBar extends IndicatorBar {

		double scaleRatio=0;

		public ScaleIndicatorBar() {super(); scaleRatio=(BAR_WIDTH-4)/(MAX_ZOOM-MIN_ZOOM);}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

			// First, paint the basic bar given the alpha composite. Then paint
			// the specific bar information given for the scale rotation settings.

			paintBar(g2);
			g2.setPaint(Color.black);
			g2.fillRect(2+(int)(scaleRatio*scale),2,1,BAR_HEIGHT-2);
			String str=String.format("%.2fx",scale);
			paintText(g2,str,BAR_WIDTH+1,1);
			g2.dispose();
		}
	}
}
