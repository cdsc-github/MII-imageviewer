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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.event.MouseEvent;

import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.EventObject;

import imageviewer.model.DataLayer;
import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;

import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.Polygon2D;
import imageviewer.ui.annotation.StylizedShape;

import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.AddAnnotationEdit;

// =======================================================================

public class PolyLineTool extends BasicAnnotationTool {

	private static final Color LAST_POINT_COLOR=new Color(0,113,188);
	private static final Color POINT_COLOR=Color.white;

	// =======================================================================

	StylizedShape lastPoint=null;

	public PolyLineTool() {super();}

	// =======================================================================

	public String getToolName() {return new String("Polyline");}

	// =======================================================================

	public void startTool(EventObject e) {a=null;}
	public void endTool(EventObject e) {if ((a!=null)&&(ip!=null)) createPolyLine();}

	// =======================================================================

	protected Annotation createAnnotation(Point2D.Double p) {return Annotation.createPolyLine();}
	protected Annotation createAnnotation(Point p) {return Annotation.createPolyLine();}

	// =======================================================================

	public void mousePressed(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel currentPanel=(ImagePanel)e.getSource();
			if ((currentPanel!=ip)&&(ip!=null)) createPolyLine();
			ip=currentPanel;
			rp=ip.getPipelineRenderer().getRenderingProperties();
			if (a==null) {
				startPoint=e.getPoint();
				startPointTranslated=translateToImage(rp,startPoint);
				a=createAnnotation(startPointTranslated); 
				ip.addTemporaryShape(a);
				ip.repaint();
				ip.setCursor(getCursor());
			}
		}
	}

	// =======================================================================

	private void createPolyLine() {

		if (a!=null) {
			ip.removeTemporaryShape(a);
			Polygon2D p=(Polygon2D)a.getBaseShape();
			if (p.xpoints.length>1) {
				ShapeDataLayer sdl=(ShapeDataLayer)ip.getSource().findDataLayer(DataLayer.SHAPE);
				if (sdl==null) {
					sdl=new ShapeDataLayer();
					ip.getSource().addDataLayer(sdl);
				}
				sdl.addShape(a);
				AddAnnotationEdit aae=new AddAnnotationEdit(ip,sdl,a);
				ApplicationContext.postEdit(aae);
			}
			ip.setCursor(Cursor.getDefaultCursor());
			ip.clearPanelOverlayShapes();
			ip.repaint();
			ip.setCursor(Cursor.getDefaultCursor());
			ip=null;
			a=null;
		}
		reset();
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {

		int clickCount=e.getClickCount();
		ip=(ImagePanel)e.getSource();
		if (clickCount==1) {
			Point p=e.getPoint();
			Point2D.Double translatedPoint=translateToImage(rp,p);
			Polygon2D polygon=(Polygon2D)a.getBaseShape();
			polygon.addPoint(translatedPoint.x,translatedPoint.y);
			if (lastPoint!=null) lastPoint.setColor(POINT_COLOR);
			StylizedShape ss=new StylizedShape(new Ellipse2D.Double(p.x-3,p.y-3,6,6));
			ss.setStroked(false);
			ss.setFilled(true);
			ss.setColor(LAST_POINT_COLOR);
			ip.addPanelOverlayShape(ss);
			lastPoint=ss;
			ip.repaint();
		} else if (clickCount==2) {
			createPolyLine();
		}
	}

	public void mouseDragged(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	// =======================================================================

	protected void setShapeCoordinate() {}

}
