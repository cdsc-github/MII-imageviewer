/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import java.util.EventObject;

import imageviewer.model.DataLayer;
import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.AddAnnotationEdit;

// =======================================================================

public abstract class BasicAnnotationTool extends ImagingTool implements Tool {

	protected RenderingProperties rp=null;
	protected Annotation a=null;
	protected ImagePanel ip=null;
	protected Point startPoint=null, endPoint=null;
	protected Point2D.Double startPointTranslated=null, endPointTranslated=null;

	public BasicAnnotationTool() {super();}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}

	protected abstract Annotation createAnnotation(Point2D.Double p);
	protected abstract Annotation createAnnotation(Point p);
	protected abstract void setShapeCoordinate();

	// =======================================================================

	protected void reset() {if ((a!=null)&&(ip!=null)) {ip.removeTemporaryShape(a); ip.repaint();} startPoint=null; endPoint=null; ip=null; a=null;}

	// =======================================================================

	public void mousePressed(MouseEvent e) {
		
		if (e.getSource() instanceof ImagePanel) {
			ip=(ImagePanel)e.getSource();
			rp=ip.getPipelineRenderer().getRenderingProperties();
			startPoint=e.getPoint();
			startPointTranslated=translateToImage(rp,startPoint);
			a=createAnnotation(startPointTranslated); 
			ip.addTemporaryShape(a);
			ip.repaint();
		}
	}

	// =======================================================================

	public void mouseDragged(MouseEvent e) {

		if (a!=null) {
			endPoint=e.getPoint(); 
			endPointTranslated=translateToImage(rp,endPoint);
			setShapeCoordinate(); 
			ip.repaint();
		}
	}
	
	// =======================================================================

	public void mouseReleased(MouseEvent e) {

		if (a!=null) {
			ip.removeTemporaryShape(a);
			if (((startPoint!=null)&&(endPoint!=null))&&((startPoint.x!=endPoint.x)||(startPoint.y!=endPoint.y))) {
				ShapeDataLayer sdl=(ShapeDataLayer)ip.getSource().findDataLayer(DataLayer.SHAPE);
				if (sdl==null) {
					sdl=new ShapeDataLayer();
					ip.getSource().addDataLayer(sdl);
				}
				sdl.addShape(a);
				AddAnnotationEdit aae=new AddAnnotationEdit(ip,sdl,a);
				ApplicationContext.postEdit(aae);
			}
			a=null;
		}
		reset();
	}

	// =======================================================================

	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}
	public void mouseMoved(MouseEvent e) {}
}
