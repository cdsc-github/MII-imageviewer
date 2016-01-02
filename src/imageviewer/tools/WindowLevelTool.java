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
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.graphics.IndicatorBar;
import imageviewer.ui.swing.undo.WindowLevelEdit;

// =======================================================================

public class WindowLevelTool extends ImagingTool implements Tool {

	private static int MIN_PIXEL_VALUE=0;
	private static final double SENSITIVITY=0.25;

	// =======================================================================

	Point start=new Point();
	int maxPixelValue=0, maxDelta=0, window=0, level=0, newWindow=0, newLevel=0;
	float panelWidth=1, panelHeight=1;
	boolean isRescaled=false;
	double[] rescaleValues=null;
	RenderingProperties rp=null;

	public WindowLevelTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Window/level");}

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
			start=e.getPoint();
			panelWidth=(float)ip.getWidth();
			panelHeight=(float)ip.getHeight();
			rp=ip.getPipelineRenderer().getRenderingProperties();
			maxPixelValue=(Integer)rp.getProperty(RenderingProperties.MAX_PIXEL);
			WindowLevel wl=(WindowLevel)rp.getProperty(RenderingProperties.WINDOW_LEVEL);
			rescaleValues=new double[] {wl.getRescaleSlope(),wl.getRescaleIntercept()};
			isRescaled=wl.isRescaled();
			MIN_PIXEL_VALUE=(isRescaled) ? -1024 : 0;
			window=newWindow=wl.getWindow();
			level=newLevel=wl.getLevel();
			maxDelta=(int)((double)maxPixelValue*SENSITIVITY);
			addIndicatorBar(ip,new WindowLevelIndicatorBar());
			ip.repaint();
		}
	}

	// =======================================================================

	public void execute(MouseEvent e) {

		int deltaX=e.getX()-start.x;
		int deltaY=-(e.getY()-start.y);
		int windowDelta=(int)(SENSITIVITY*maxPixelValue*((float)deltaX/panelWidth));
		int levelDelta=(int)(SENSITIVITY*maxPixelValue*((float)deltaY/panelHeight));

		newWindow=window+windowDelta;
		if (newWindow>maxPixelValue) newWindow=maxPixelValue;
		if (newWindow<MIN_PIXEL_VALUE) newWindow=MIN_PIXEL_VALUE;

		newLevel=level+levelDelta;
		if (newLevel>maxPixelValue) newLevel=maxPixelValue;
		if (newLevel<MIN_PIXEL_VALUE) newLevel=MIN_PIXEL_VALUE;

		// ((WindowLevelIndicatorPanel) getIndicatorPanel()).setWindowLevel(newWindow,newLevel);

		// Set the new values for the image; this will fire the
		// corresponding changes that will be required to the rendering
		// pipeline.

		WindowLevel wl=new WindowLevel(newWindow,newLevel,rescaleValues[0],rescaleValues[1]);
		wl.setRescaled(isRescaled);
		rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {wl});
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ip.repaint();
		}
	}

	// =======================================================================

	public void finish(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			WindowLevel wl=new WindowLevel(newWindow,newLevel,rescaleValues[0],rescaleValues[1]);
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {wl});
			WindowLevelEdit wle=new WindowLevelEdit(ip,new WindowLevel(window,level),wl);
			ApplicationContext.postEdit(wle);
			removeIndicatorBar(ip);
			ip.repaint();
		}
	}

	// =======================================================================

	private class WindowLevelIndicatorBar extends IndicatorBar {

		double winPixelRatio=0;

		public WindowLevelIndicatorBar() {

			super(); 
			winPixelRatio=((double)BAR_WIDTH/(double)maxPixelValue);
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

			// First, paint the basic bar given the alpha composite. Then paint
			// the specific bar information given for window/level settings.

			paintBar(g2);
			double winHalf=((double)newWindow/2)*winPixelRatio;
			int levelX=(int)(winPixelRatio*newLevel);
			int minX=(int)(levelX-winHalf);
			int maxX=(int)(levelX+winHalf);
			if (minX<2) minX=2;
			if (maxX>(BAR_WIDTH-2)) maxX=(BAR_WIDTH-2);

			g2.setPaint(Color.black);
			Rectangle minBarRect=new Rectangle(minX,3,(maxX-minX),BAR_HEIGHT-4);
			g2.fill(minBarRect);
			g2.setColor(Color.black);
			g2.drawLine(minX,3,minX,BAR_HEIGHT-2);
			g2.drawLine(minX,3,maxX-1,3);

			String str=new String("("+newWindow+"/"+newLevel+")");
			paintText(g2,str,BAR_WIDTH+1,1);
			g2.dispose();
		}
	}
}
