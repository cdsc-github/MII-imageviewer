/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import java.util.ArrayList;

import javax.swing.JPanel;

import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.RenderingUtil;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.Selectable;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.CPAnnotationEdit;

//=======================================================================

public class ControlPoint extends JPanel implements MouseListener, MouseMotionListener {

	public static final double SIZE=7;

	public static final boolean IN_PROGRESS=false;
	public static final boolean IS_FINAL=true;

	//=======================================================================

	public static enum Location {
		
		NORTH, NORTHEAST, EAST, SOUTHEAST, SOUTH, SOUTHWEST, WEST, NORTHWEST, OTHER;
		
		public static Location getAnnotationType(String s) {
			Location l=Location.OTHER;
			if (s==null || s.equals("") || s.equalsIgnoreCase("NORTH")) {
				l=Location.NORTH;
			} else if (s.equalsIgnoreCase("NORTHEAST")) {
				l=Location.NORTHEAST;
			} else if (s.equalsIgnoreCase("EAST")) {
				l=Location.EAST;
			} else if (s.equalsIgnoreCase("SOUTHEAST")) {
				l=Location.SOUTHEAST;
			} else if (s.equalsIgnoreCase("SOUTH")) {
				l=Location.SOUTH;
			} else if (s.equalsIgnoreCase("SOUTHWEST")) {
				l=Location.SOUTHWEST;
			} else if (s.equalsIgnoreCase("WEST")) {
				l=Location.WEST;
			} else if (s.equalsIgnoreCase("NORTHWEST")) {
				l=Location.NORTHWEST;
			} 
			return l;
		}
	}

	//=======================================================================

	private static final Color LITE_CONTROL_RED=new Color(255,180,180);
	private static final Color DARK_CONTROL_RED=new Color(128,0,0);

	protected static final Cursor CROSSHAIR_CURSOR=new Cursor(Cursor.CROSSHAIR_CURSOR);
	protected static final Cursor DEFAULT_CURSOR=new Cursor(Cursor.DEFAULT_CURSOR);

	// =======================================================================

	public Location type=Location.OTHER;
	public int id=0;

	Point2D.Double startPoint=new Point2D.Double(), tmpPoint=new Point2D.Double(), currentPoint=new Point2D.Double();
	Point2D.Double baseCoordinates=null, delta0=null, delta1=null;
	Point offset=null;
	Selectable selectedObject=null;
	double[] m=new double[2], b=new double[2];

	public ControlPoint(Selectable selectedObject, Location type, int id, double x, double y) {

		super(); 
		this.selectedObject=selectedObject; 
		this.type=type;
		this.id=id;
		baseCoordinates=new Point2D.Double(x,y);         // Coordinates are in image space
		setLayout(null); 
		setOpaque(true);
		setBounds((int)x,(int)y,(int)SIZE,(int)SIZE);    // Set the initial location, but it will be recomputed when painted.
		addMouseMotionListener(this);
		addMouseListener(this);
	}

	// =======================================================================

	public Location getType() {return type;}

	public Point2D.Double getBaseCoordinates() {return baseCoordinates;}
	public Point2D.Double getLocation(RenderingProperties rp) {return RenderingUtil.translateToViewport(rp,baseCoordinates);}

	public void updateLocation(RenderingProperties rp) {Point2D.Double p=RenderingUtil.translateToViewport(rp,baseCoordinates);	setLocation((int)Math.round(p.x-SIZE/2),(int)Math.round(p.y-SIZE/2));}

	public void updateXY(double x, double y) {baseCoordinates.x=x; baseCoordinates.y=y; setLocation((int)Math.round(baseCoordinates.x-SIZE/2),(int)Math.round(baseCoordinates.y-SIZE/2));}
	public void updateX(double x) {baseCoordinates.x=x; setLocation((int)Math.round(baseCoordinates.x-SIZE/2),(int)Math.round(baseCoordinates.y-SIZE/2));}
	public void updateY(double y) {baseCoordinates.y=y; setLocation((int)Math.round(baseCoordinates.x-SIZE/2),(int)Math.round(baseCoordinates.y-SIZE/2));}

	// =======================================================================

	public void paintComponent(Graphics g) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		Ellipse2D.Double e1=new Ellipse2D.Double(0,0,(int)SIZE,(int)SIZE);
		g2.setColor(Color.black);
		g2.fill(e1);
		Ellipse2D.Double e2=new Ellipse2D.Double(1,1,(int)SIZE-2,(int)SIZE-2);
		g2.setColor(Color.white);
		g2.fill(e2);
		g2.dispose();
	}

	// =======================================================================

	private void computeAxes(RenderingProperties rp) {

		Point2D.Double northPoint=null, southPoint=null, eastPoint=null, westPoint=null;
		for (ControlPoint cp : selectedObject.getControlPoints()) {
			switch (cp.getType()) {
		  	case NORTH: northPoint=cp.getLocation(rp); break;
			  case SOUTH: southPoint=cp.getLocation(rp); break;
			   case EAST: eastPoint=cp.getLocation(rp); break;
			   case WEST: westPoint=cp.getLocation(rp); break;
			}
		}
	
		double dY=northPoint.y-southPoint.y;
		double dX=northPoint.x-southPoint.x;
		if (Math.abs(dX)<=0.0001) {                                          
			m[0]=0;
			b[0]=0;
		} else {
			m[0]=dY/dX;
			b[0]=northPoint.y-(m[0]*northPoint.x);
		}
		
		dY=eastPoint.y-westPoint.y;
		dX=eastPoint.x-westPoint.x;
		if (Math.abs(dY)<=0.0001) {                                   
			m[1]=0;
			b[1]=0;
		} else {
			if (dX!=0) {
				m[1]=dY/dX;
				b[1]=eastPoint.y-(m[1]*eastPoint.x);
			} 
		}
	}

	private void computeNorthSouthProjection(Point2D.Double p0, Point cursor, Point2D.Double p) {

		if ((m[0]!=0)&&(b[0]!=0)) {
			if (Math.abs(m[0])<1) {
				p.x=p0.x+cursor.x;
				p.y=(m[0]*p.x)+b[0];
			} else {
				p.y=p0.y+cursor.y;
				p.x=(p.y-b[0])/m[0];
			}
		} else {
			p.x=p0.x;                                                              
			p.y=p0.y+cursor.y;
		}
	}

	private void computeEastWestProjection(Point2D.Double p0, Point cursor, Point2D.Double p) {

		if ((m[1]!=0)&&(b[1]!=0)) {
			if (Math.abs(m[1])>1) {
				p.y=p0.y+cursor.y;
				p.x=(p.y-b[1])/m[1];
			} else {
				p.x=p0.x+cursor.x;
				p.y=(m[1]*p.x)+b[1];
			}
		} else {
			p.x=p0.x+cursor.x;
			p.y=p0.y;                        
		}
	}

	// =======================================================================

	private Point2D.Double computeNorthSouthMove(RenderingProperties rp, ControlPoint cp, Point cursor) {

		Point2D.Double p0=cp.getLocation(rp);
		Point2D.Double bc=cp.getBaseCoordinates();
		Point2D.Double projection=new Point2D.Double();
		computeNorthSouthProjection(p0,cursor,projection);
		Point2D.Double bct=RenderingUtil.translateToImage(rp,projection);
		double x=bct.x-bc.x; if (Math.abs(x)<0.1) x=0;
		double y=bct.y-bc.y; if (Math.abs(y)<0.1) y=0;
		return new Point2D.Double(x,y);
	}

	private Point2D.Double computeEastWestMove(RenderingProperties rp, ControlPoint cp, Point cursor) {

		Point2D.Double p0=cp.getLocation(rp);
		Point2D.Double bc=cp.getBaseCoordinates();
		Point2D.Double projection=new Point2D.Double();
		computeEastWestProjection(p0,cursor,projection);
		Point2D.Double bct=RenderingUtil.translateToImage(rp,projection);
		double x=bct.x-bc.x; if (Math.abs(x)<0.1) x=0;
		double y=bct.y-bc.y; if (Math.abs(y)<0.1) y=0;
		return new Point2D.Double(x,y);
	}

	// =======================================================================

	public void mousePressed(MouseEvent e) {
		
		RenderingProperties rp=((ImagePanel)getParent()).getPipelineRenderer().getRenderingProperties();
		tmpPoint.x=baseCoordinates.x;
		tmpPoint.y=baseCoordinates.y;                                        // Store the current location of the control point in image space
		offset=e.getPoint();
		startPoint.x=getLocation(rp).x;                                      // StartPoint coordinates are in the viewport space, offset 
		startPoint.y=getLocation(rp).y;      
		selectedObject.controlPointMoveStart(this,baseCoordinates);
	}

	public void mouseDragged(MouseEvent e) {doMove(e.getPoint());}        // Check: do you need to substract the offset?

	public void mouseReleased(MouseEvent e) {

		Point p0=e.getPoint();
		Point p1=getLocation();
		CPAnnotationEdit cpae=new CPAnnotationEdit((ImagePanel)getParent(),this,new Point2D.Double(startPoint.x,startPoint.y),
																							 new Point2D.Double(p1.x,p1.y),new Point2D.Double(p0.x,p0.y),new Point2D.Double(offset.x,offset.y));
		ApplicationContext.postEdit(cpae);		
	}

	// =======================================================================

	public void doMove(Point p) {

		RenderingProperties rp=((ImagePanel)getParent()).getPipelineRenderer().getRenderingProperties();
		
		if (selectedObject.hasRotationAxis()) {                                                            // Objects with a rotation axis will constrain the resizing along the axes...
			computeAxes(rp);
			switch (type) {
				    case NORTH: case SOUTH: case EAST:
			       case WEST: Point2D.Double p0=getLocation(rp);                                             // p0 is current location in viewport space
							          if ((type==Location.NORTH)||(type==Location.SOUTH)) {
													computeNorthSouthProjection(p0,p,currentPoint);                              // Put the new position into the currentPoint
												} else if ((type==Location.WEST)||(type==Location.EAST)) {
													computeEastWestProjection(p0,p,currentPoint);
												}
												Point2D.Double d0=new Point2D.Double(baseCoordinates.x,baseCoordinates.y);     // Store the previous location in image space temporarily
												baseCoordinates=RenderingUtil.translateToImage(rp,currentPoint);               // Transform the currentPoint to the image space, used in the updateLocation method
												delta0=new Point2D.Double(baseCoordinates.x-d0.x,baseCoordinates.y-d0.y);      // Compute the difference in the location
												Point2D.Double[] delta=new Point2D.Double[] {delta0,null,null};
												selectedObject.controlPointMoveDrag(this,rp,delta,baseCoordinates);
												setLocation((int)(currentPoint.x-SIZE/2),(int)(currentPoint.y-SIZE/2));        // Set the location of the control point to the new dragged location, in viewport space
												getParent().repaint();
												break;
			  case NORTHEAST: {ControlPoint ncp=selectedObject.getControlPoint(Location.NORTH);
					               ControlPoint ecp=selectedObject.getControlPoint(Location.EAST);
												 delta0=computeNorthSouthMove(rp,ncp,p);
												 delta1=computeEastWestMove(rp,ecp,p);
												 selectedObject.controlPointMoveDrag(this,rp,new Point2D.Double[] {null,delta0,delta1},null);}
												break;
			  case NORTHWEST: {ControlPoint ncp=selectedObject.getControlPoint(Location.NORTH);
					               ControlPoint wcp=selectedObject.getControlPoint(Location.WEST);
												 delta0=computeNorthSouthMove(rp,ncp,p);
												 delta1=computeEastWestMove(rp,wcp,p);
												 selectedObject.controlPointMoveDrag(this,rp,new Point2D.Double[] {null,delta0,delta1},null);}
												break;
			  case SOUTHEAST: {ControlPoint scp=selectedObject.getControlPoint(Location.SOUTH);
					               ControlPoint ecp=selectedObject.getControlPoint(Location.EAST);
												 delta0=computeNorthSouthMove(rp,scp,p);
												 delta1=computeEastWestMove(rp,ecp,p);
												 selectedObject.controlPointMoveDrag(this,rp,new Point2D.Double[] {null,delta0,delta1},null);}
												break;
			  case SOUTHWEST: {ControlPoint scp=selectedObject.getControlPoint(Location.SOUTH);
					               ControlPoint wcp=selectedObject.getControlPoint(Location.WEST);
												 delta0=computeNorthSouthMove(rp,scp,p);
												 delta1=computeEastWestMove(rp,wcp,p);
												 selectedObject.controlPointMoveDrag(this,rp,new Point2D.Double[] {null,delta0,delta1},null);}
												break;
			}
		} else {

			currentPoint.x=getLocation().x+p.x;                                             
			currentPoint.y=getLocation().y+p.y;
			baseCoordinates=RenderingUtil.translateToImage(rp,currentPoint);                     
			setLocation((int)currentPoint.x,(int)currentPoint.y);                                
			selectedObject.controlPointMoveDrag(this,rp,null,baseCoordinates);
			getParent().repaint();
		}
	}

	// =======================================================================

	public void mouseEntered(MouseEvent e) {getParent().setCursor(CROSSHAIR_CURSOR); ((MouseListener)getParent()).mouseEntered(e);}
	public void mouseExited(MouseEvent e) {getParent().setCursor(DEFAULT_CURSOR); ((MouseListener)getParent()).mouseExited(e);}

	public void mouseMoved(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) {}
}
