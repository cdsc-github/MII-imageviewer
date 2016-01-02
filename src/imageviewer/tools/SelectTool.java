/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import java.util.ArrayList;
import java.util.EventObject;

import imageviewer.model.DataLayer;
import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.annotation.Selectable;
import imageviewer.ui.annotation.Polygon2D;
import imageviewer.ui.annotation.Annotation.AnnotationType;

import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.DeleteAnnotationEdit;

// =======================================================================

public class SelectTool extends ImagingTool implements Tool {

	private static final Point selectionPoint=new Point();
	private static final Annotation selectionRectangle;

	static {
		selectionRectangle=Annotation.createBox(0,0,0,0);
    float dash[]={3.5f};
		selectionRectangle.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_SQUARE,BasicStroke.JOIN_MITER,2.5f,dash,0.0f));
		selectionRectangle.setStrokeColor(Color.white);
	}

	// =======================================================================

	RenderingProperties rp=null;
	ImagePanel ip=null;
	boolean rectFlag=false;

	public SelectTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {ip=null;}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Select");}

	// =======================================================================

	public void mousePressed(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip2=(ImagePanel)e.getSource();
			if ((ip2!=ip)&&(ip!=null)) {
				removeSelections();
				ApplicationContext.getContext().clearSelections();     
			}
			ip=ip2;
			rp=ip.getPipelineRenderer().getRenderingProperties();
			Point p=e.getPoint();
			selectionPoint.x=p.x; 
			selectionPoint.y=p.y;

			// Do we already have a selection and we're still on it? If so,
			// we are initiating a drag move, rather than a drag
			// selection...Basically, check to see if the point we are on
			// was already selected...

			// Point q=translateToImage(rp,selectionPoint);			
			// ArrayList<Selectable> currentSelections=ApplicationContext.getContext().getSelections();
			// for (Selectable s : currentSelections) {
			// 
			//	}
			rectFlag=true;
		}
	}

	// =======================================================================

	public void mouseDragged(MouseEvent e) {

		Point p=e.getPoint();
		if (rectFlag) {ip.addPanelOverlayShape(selectionRectangle); rectFlag=false;}
		Polygon2D polyRect=(Polygon2D)selectionRectangle.getBaseShape();
		polyRect.xpoints[0]=selectionPoint.x; polyRect.ypoints[0]=selectionPoint.y;
		polyRect.xpoints[1]=p.x; polyRect.ypoints[1]=selectionPoint.y;
		polyRect.xpoints[2]=p.x; polyRect.ypoints[2]=p.y;
		polyRect.xpoints[3]=selectionPoint.x; polyRect.ypoints[3]=p.y;
		ip.repaint();
	}

	// =======================================================================

	private void removeSelections() {

		ArrayList<Selectable> al=ApplicationContext.getContext().getSelections();
		for (Selectable s : al) {
			if (s.isSelected()) {
				ArrayList<ControlPoint> controlPoints=s.getControlPoints();
				if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
					for (ControlPoint cp : controlPoints) {Container c=cp.getParent(); c.remove(cp); c.repaint();}
				}
			}
			s.deselect();
		} 
	}

	// =======================================================================

	public void mouseReleased(MouseEvent e) {

		if (ip==null) return;
		ip.removePanelOverlayShape(selectionRectangle);
		Point p=e.getPoint();

		boolean addSelection=e.isShiftDown();
		boolean subtractSelection=e.isControlDown();

		// Find what was selected...

		ArrayList selections=new ArrayList();
		ArrayList dataLayers=ip.getSource().getDataLayers();
		if (dataLayers!=null) {
			if ((p.x==selectionPoint.x)&&(p.y==selectionPoint.y)) {
				Point2D.Double q=translateToImage(rp,selectionPoint);
				for (int loop=0, n=dataLayers.size(); loop<n; loop++) {
					DataLayer dl=(DataLayer)dataLayers.get(loop);
					if (dl.canSelect()) selections.addAll(dl.getSelections(selectionPoint));
				}
			} else {

				Point q1=new Point(Math.min(selectionPoint.x,p.x),Math.min(selectionPoint.y,p.y));  // This point is the top-left in viewport space
				Point q2=new Point(Math.max(selectionPoint.x,p.x),Math.max(selectionPoint.y,p.y));  // This point is the bottom-right in viewport space

				// In this case, it's really a polygon and not a square, as
				// the points could be rotated out of the axis.  Blech.  So
				// determine what the four different points are, and create a
				// polygon.

				Point2D.Double p1=translateToImage(rp,q1);
				Point2D.Double p2=translateToImage(rp,new Point(q2.x,q1.y));
				Point2D.Double p3=translateToImage(rp,q2);
				Point2D.Double p4=translateToImage(rp,new Point(q1.x,q2.y));
				Polygon polygon=new Polygon(new int[] {(int)p1.x,(int)p2.x,(int)p3.x,(int)p4.x}, new int[] {(int)p1.y,(int)p2.y,(int)p3.y,(int)p4.y},4);
				for (int loop=0, n=dataLayers.size(); loop<n; loop++) {
					DataLayer dl=(DataLayer)dataLayers.get(loop);
					if (dl.canSelect()) selections.addAll(dl.getSelections(polygon));
				}
			}
		}

		if (selections.isEmpty()) {
			removeSelections();
			ApplicationContext.getContext().clearSelections();                          // Remove all current selections; the respective objects need to be informed!
		} else {
			if (subtractSelection) {
				ApplicationContext.getContext().removeSelections(selections);             // Subtract from current selections, as ctrl was down
				for (int loop=0, n=selections.size(); loop<n; loop++) {
					Object o=selections.get(loop);
					if (o instanceof Selectable) {
						Selectable s=(Selectable)o;
						if (s.isSelected()) {
							ArrayList<ControlPoint> controlPoints=s.getControlPoints();
							if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
								for (ControlPoint cp : controlPoints) cp.getParent().remove(cp);
							}
						}
						s.deselect();
					} 
				}
			} else {
				if (!addSelection) {
					removeSelections(); 
					ApplicationContext.getContext().clearSelections();
				}
				ApplicationContext.getContext().addSelections(selections);                // Add to current selections or replace, as shift was down
				for (int loop=0, n=selections.size(); loop<n; loop++) {
					Object o=selections.get(loop);
					if (o instanceof Selectable) {
						Selectable s=(Selectable)o;
						s.select();
						ArrayList<ControlPoint> controlPoints=s.getControlPoints();
						if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
							for (ControlPoint cp : controlPoints) ip.add(cp);
						}
					}
				}
			}
		}
		ip.repaint();
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	// =======================================================================

	public void keyPressed(KeyEvent e) {

		if (e.isConsumed()) return;
		if ((e.getKeyCode()==KeyEvent.VK_DELETE)) {
			ArrayList<Selectable> al=(ArrayList<Selectable>)ApplicationContext.getContext().getSelections().clone();
			removeSelections();
			ApplicationContext.getContext().clearSelections(); 
			for (Selectable s : al) s.delete();
			DeleteAnnotationEdit dae=new DeleteAnnotationEdit(ip,(ShapeDataLayer)ip.getSource().findDataLayer(DataLayer.SHAPE),al);
			ApplicationContext.postEdit(dae);
			e.consume();
		}
	}
	
	// =======================================================================
	
	public void keyTyped(KeyEvent e) {
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel currentPanel=(ImagePanel)e.getSource();
			if (currentPanel!=ip){
				e.consume();
			}
			else{
				ArrayList<Selectable> al=(ArrayList<Selectable>)ApplicationContext.getContext().getSelections().clone();
				//Only edit if a single text item is selected
				if (al.size()==1){
					Annotation theItem = (Annotation)al.get(0);
					if (theItem.getAnnotationType() == AnnotationType.TEXT){
						theItem.addText(Character.toString(e.getKeyChar()));
					}
					currentPanel=(ImagePanel)e.getSource();
					currentPanel.repaint();
				}
				//Check to see if a shortcut W/L key was pressed
				else {
					if (e.getKeyChar() == '1' ){
						//Soft tissue W/L
						AutoSoftContrastTool.changeWindowLevel(currentPanel);
					} else if (e.getKeyChar() == '2' ){
						//Lung W/L
						AutoLungContrastTool.changeWindowLevel(currentPanel);
					} else if (e.getKeyChar() == '4' ){
						//Bone W/L
						AutoBoneContrastTool.changeWindowLevel(currentPanel);
					} else if (e.getKeyChar() == '7' ){
						//Vascular W/L
					}
				}
			}
		}
	}
}
