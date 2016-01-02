/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Point;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.graphics.IndicatorBar;

// =======================================================================

public abstract class ImagingTool {

	protected IndicatorBar ib=null;
	protected FloatingPanel toolDialog=null;

	public ImagingTool() {}

	// =======================================================================

	protected void addIndicatorBar(JPanel panel, IndicatorBar ib) {

		this.ib=ib;
		ib.setLocation(3,panel.getHeight()-ib.getHeight());
		panel.add(ib);
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	public void keyPressed(KeyEvent e) {}
	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}

	// =======================================================================

	protected void removeIndicatorBar(JPanel panel) {panel.remove(ib); ib=null;}

	// =======================================================================

	protected Point2D.Double translateToImage(RenderingProperties rp, Point p) {

		double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
		double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
		double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
		float rotationAngle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();		
		int sourceWidth=((Integer)rp.getProperty(RenderingProperties.SOURCE_WIDTH)).intValue();
		int sourceHeight=((Integer)rp.getProperty(RenderingProperties.SOURCE_HEIGHT)).intValue();
		boolean isVFlip=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();	
		boolean isHFlip=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();	
		double vFlip=(isVFlip) ? -1 : 1;
		double hFlip=(isHFlip) ? -1 : 1;
		double yTranslate=(isVFlip) ? -sourceHeight : 0;
		double xTranslate=(isHFlip) ? -sourceWidth : 0;

		double pX=(double)((p.x-translateX)/scale);
		double pY=(double)((p.y-translateY)/scale);
		AffineTransform matrix=AffineTransform.getRotateInstance((double)rotationAngle,sourceWidth/2,sourceHeight/2);
		matrix.scale(hFlip,vFlip);
		matrix.translate(xTranslate,yTranslate);
		Point2D.Double p1=new Point2D.Double(pX,pY);
		Point2D.Double p2=new Point2D.Double();
		try {matrix.inverseTransform(p1,p2);} catch (Exception exc) {}
		return new Point2D.Double(p2.x,p2.y);
	}

	// =======================================================================
	// Add it to the palette layer.  The ApplicationPanel will take
	// care of repainting issues.

	protected void showToolDialog() {ApplicationPanel.getInstance().addFloatingPanel(toolDialog,JLayeredPane.PALETTE_LAYER);}
	protected void hideToolDialog() {ApplicationPanel.getInstance().removeFloatingPanel(toolDialog);}

}
