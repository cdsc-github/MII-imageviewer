/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D.Double;

import java.util.ArrayList;

import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.RenderingUtil;

import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.annotation.ControlPoint.Location;

//=======================================================================

public class Annotation extends StylizedShape {

	public static enum AnnotationType {
		
		LINE, ARROW, TEXT, BOX, ELLIPSE, POLYGON, CURVE, POLYLINE;
		
		public static AnnotationType getAnnotationType(String s) {
			AnnotationType at=AnnotationType.LINE;
			if (s==null || s.equals("") || s.equalsIgnoreCase("LINE")) {
				at=AnnotationType.LINE;
			} else if (s.equalsIgnoreCase("ARROW")) {
				at=AnnotationType.ARROW;
			} else if (s.equalsIgnoreCase("TEXT")) {
				at=AnnotationType.TEXT;
			} else if (s.equalsIgnoreCase("BOX")) {
				at=AnnotationType.BOX;
			} else if (s.equalsIgnoreCase("ELLIPSE")) {
				at=AnnotationType.ELLIPSE;
			} else if (s.equalsIgnoreCase("POLYGON")) {
				at=AnnotationType.POLYGON;
			} else if (s.equalsIgnoreCase("CURVE")) {
				at=AnnotationType.CURVE;
			} else if (s.equalsIgnoreCase("POLYLINE")) {
				at=AnnotationType.POLYLINE;
			} else if (s.equalsIgnoreCase("CURVE")) {
				at=AnnotationType.CURVE;
			} 
			return at;
		}
	}

  private static final float AANG=(float)Math.PI/8;
  private static final int ALEN=10;
  private static final Font DEFAULT_FONT=new Font("Tahoma",Font.PLAIN,9);
  private static final Font ANNOTATION_FONT=new Font("Tahoma",Font.PLAIN,14);

	//=======================================================================

	String annotationText=null;
	AnnotationType type=null;
	ArrayList<ControlPoint> controlPoints=null;
	ArrayList<Point2D.Double> points=null;

	public Annotation(String typeName) {super(); this.type=AnnotationType.getAnnotationType(typeName);}
	public Annotation(AnnotationType type) {super(); this.type=type;}
	public Annotation(AnnotationType type, Shape baseShape) {super(); this.type=type; this.baseShape=baseShape;}
	public Annotation(AnnotationType type, Shape baseShape, boolean paintFlag) {super(); this.type=type; this.baseShape=baseShape; this.paintFlag=paintFlag;}
	
	public String getText() {return annotationText;}
	public AnnotationType getAnnotationType() {return type;}

	public void setText(String x) {annotationText=x;}

	// =======================================================================
	// Create the control points for the annotation object.  Need to
	// specify that this annotation is the parent, the location of the
	// control point (conceptually and its id), and the physical
	// location of the control point.

	public ArrayList<ControlPoint> getControlPoints() {

		if (controlPoints!=null) return controlPoints;
		controlPoints=new ArrayList<ControlPoint>();
		switch (type) {
		
		case TEXT: {
			controlPoints.add(new ControlPoint(this,Location.OTHER,0,((Line2D.Double)baseShape).x1, ((Line2D.Double)baseShape).y1));
			break;
		}

		    case LINE: {controlPoints.add(new ControlPoint(this,Location.OTHER,0,((Line2D.Double)baseShape).x1,((Line2D.Double)baseShape).y1));
				            controlPoints.add(new ControlPoint(this,Location.OTHER,1,((Line2D.Double)baseShape).x2,((Line2D.Double)baseShape).y2));}
									 break;
		   case ARROW: {Point2D.Double[] pArray=computeArrowCoordinates((GeneralPath)baseShape);
				            controlPoints.add(new ControlPoint(this,Location.OTHER,0,pArray[0].x,pArray[0].y));
										controlPoints.add(new ControlPoint(this,Location.OTHER,1,pArray[1].x,pArray[1].y));}
				           break;
		 case ELLIPSE:
		     case BOX: {Polygon2D p=(Polygon2D)baseShape;
									  controlPoints.add(new ControlPoint(this,Location.NORTH,0,(p.xpoints[0]+p.xpoints[1])/2,(p.ypoints[0]+p.ypoints[1])/2));
										controlPoints.add(new ControlPoint(this,Location.NORTHEAST,1,p.xpoints[1],p.ypoints[1]));
										controlPoints.add(new ControlPoint(this,Location.EAST,2,(p.xpoints[1]+p.xpoints[2])/2,(p.ypoints[1]+p.ypoints[2])/2));
										controlPoints.add(new ControlPoint(this,Location.SOUTHEAST,3,p.xpoints[2],p.ypoints[2]));
										controlPoints.add(new ControlPoint(this,Location.SOUTH,4,(p.xpoints[2]+p.xpoints[3])/2,(p.ypoints[2]+p.ypoints[3])/2));
										controlPoints.add(new ControlPoint(this,Location.SOUTHWEST,5,p.xpoints[3],p.ypoints[3]));
										controlPoints.add(new ControlPoint(this,Location.WEST,6,(p.xpoints[3]+p.xpoints[0])/2,(p.ypoints[3]+p.ypoints[0])/2));
										controlPoints.add(new ControlPoint(this,Location.NORTHWEST,7,p.xpoints[0],p.ypoints[0]));}
									 break;
		 case POLYGON: break;
		case POLYLINE: {Polygon2D p=(Polygon2D)baseShape;
										for (int loop=0; loop<p.npoints; loop++) controlPoints.add(new ControlPoint(this,Location.OTHER,loop,p.xpoints[loop],p.ypoints[loop]));}
			             break;
		   case CURVE: {FourPointCurve fpc=(FourPointCurve)baseShape;
										 ArrayList<Point2D.Double> pList=fpc.getControlPoints();
										 int count=0;
										 for (Point2D.Double p : pList) controlPoints.add(new ControlPoint(this,Location.OTHER,count++,p.x,p.y));}
			             break;
		      default: break;
		}
		return controlPoints;
	}

	public void setControlPoints(ArrayList<ControlPoint> x) {controlPoints=x;}

	public ControlPoint getControlPoint(Location x) {

		if (controlPoints!=null) {for (ControlPoint cp : controlPoints) if (cp.getType()==x) return cp;}
		return null;
	}

	//=======================================================================

	public void controlPointMoveStart(ControlPoint cp, Point2D.Double p1) {}         

	public void controlPointMoveDrag(ControlPoint cp, RenderingProperties rp, Point2D.Double[] delta, Point2D.Double p) {          

		switch (type) {
		
		case TEXT: Line2D.Double point = (Line2D.Double)baseShape;
				point.x1 = p.x; point.y1=p.y;
				point.x2 = p.x; point.y2=p.y;
			break;

		    case LINE: Line2D.Double l=(Line2D.Double)baseShape;
					         if (cp.id==0) {l.x1=p.x; l.y1=p.y;} else {l.x2=p.x; l.y2=p.y;}
									 break;
		   case ARROW: Point2D.Double[] pArray=computeArrowCoordinates((GeneralPath)baseShape);
				           baseShape=(cp.id==0) ? createArrowLine(p.x,p.y,pArray[1].x,pArray[1].y) : createArrowLine(pArray[0].x,pArray[0].y,p.x,p.y);
				           break;
		 case ELLIPSE:
		     case BOX: Polygon2D poly=(Polygon2D)baseShape;                         

					         switch (cp.id) {

										 // Need to update the other control points! Unfortunately, not straightforward because of 
										 // the differences between the image space and the viewport space and the fact that we want to preserve 
										 // the 90 degree angle between the points (objects may be rotated at various angles).  The delta contribution 
										 // in X/Y is passed in for a given point, and must be added to the corresponding polygon corners.

									   case 0: poly.xpoints[0]+=delta[0].x;
											       poly.ypoints[0]+=delta[0].y;
														 poly.xpoints[1]+=delta[0].x;
														 poly.ypoints[1]+=delta[0].y;
														 controlPoints.get(1).updateXY(poly.xpoints[1],poly.ypoints[1]);
														 controlPoints.get(7).updateXY(poly.xpoints[0],poly.ypoints[0]);
														 controlPoints.get(2).updateXY((poly.xpoints[1]+poly.xpoints[2])/2,(poly.ypoints[1]+poly.ypoints[2])/2);
														 controlPoints.get(6).updateXY((poly.xpoints[3]+poly.xpoints[0])/2,(poly.ypoints[3]+poly.ypoints[0])/2);
														 break;
									   case 1: poly.xpoints[0]+=delta[1].x;
											       poly.ypoints[0]+=delta[1].y;
														 poly.xpoints[1]+=(delta[1].x+delta[2].x);
														 poly.ypoints[1]+=(delta[1].y+delta[2].y);
														 poly.xpoints[2]+=delta[2].x;
														 poly.ypoints[2]+=delta[2].y;
														 for (ControlPoint points : controlPoints) points.setVisible(false);
														 controlPoints.get(7).updateXY(poly.xpoints[0],poly.ypoints[0]);
														 controlPoints.get(1).updateXY(poly.xpoints[1],poly.ypoints[1]);
														 controlPoints.get(3).updateXY(poly.xpoints[2],poly.ypoints[2]);
														 controlPoints.get(0).updateXY((poly.xpoints[0]+poly.xpoints[1])/2,(poly.ypoints[0]+poly.ypoints[1])/2);
														 controlPoints.get(2).updateXY((poly.xpoints[1]+poly.xpoints[2])/2,(poly.ypoints[1]+poly.ypoints[2])/2);
														 controlPoints.get(4).updateXY((poly.xpoints[2]+poly.xpoints[3])/2,(poly.ypoints[2]+poly.ypoints[3])/2);
														 controlPoints.get(6).updateXY((poly.xpoints[3]+poly.xpoints[0])/2,(poly.ypoints[3]+poly.ypoints[0])/2);
														 for (ControlPoint points : controlPoints) points.setVisible(true);
														 break;
									   case 2: poly.xpoints[1]+=delta[0].x;
														 poly.ypoints[1]+=delta[0].y;
														 poly.xpoints[2]+=delta[0].x;
														 poly.ypoints[2]+=delta[0].y;
														 controlPoints.get(1).updateXY(poly.xpoints[1],poly.ypoints[1]);
														 controlPoints.get(3).updateXY(poly.xpoints[2],poly.ypoints[2]);
														 controlPoints.get(0).updateXY((poly.xpoints[0]+poly.xpoints[1])/2,(poly.ypoints[0]+poly.ypoints[1])/2);
														 controlPoints.get(4).updateXY((poly.xpoints[2]+poly.xpoints[3])/2,(poly.ypoints[2]+poly.ypoints[3])/2);
														 break;
									   case 3: poly.xpoints[3]+=delta[1].x;
											       poly.ypoints[3]+=delta[1].y;
														 poly.xpoints[2]+=(delta[1].x+delta[2].x);
														 poly.ypoints[2]+=(delta[1].y+delta[2].y);
														 poly.xpoints[1]+=delta[2].x;
														 poly.ypoints[1]+=delta[2].y;
														 for (ControlPoint points : controlPoints) points.setVisible(false);
														 controlPoints.get(5).updateXY(poly.xpoints[3],poly.ypoints[3]);
														 controlPoints.get(1).updateXY(poly.xpoints[1],poly.ypoints[1]);
														 controlPoints.get(3).updateXY(poly.xpoints[2],poly.ypoints[2]);
														 controlPoints.get(0).updateXY((poly.xpoints[0]+poly.xpoints[1])/2,(poly.ypoints[0]+poly.ypoints[1])/2);
														 controlPoints.get(2).updateXY((poly.xpoints[1]+poly.xpoints[2])/2,(poly.ypoints[1]+poly.ypoints[2])/2);
														 controlPoints.get(4).updateXY((poly.xpoints[2]+poly.xpoints[3])/2,(poly.ypoints[2]+poly.ypoints[3])/2);
														 controlPoints.get(6).updateXY((poly.xpoints[3]+poly.xpoints[0])/2,(poly.ypoints[3]+poly.ypoints[0])/2);
														 for (ControlPoint points : controlPoints) points.setVisible(true);
														 break;
									   case 4: poly.xpoints[2]+=delta[0].x;
											       poly.ypoints[2]+=delta[0].y;
														 poly.xpoints[3]+=delta[0].x;
														 poly.ypoints[3]+=delta[0].y;
														 controlPoints.get(3).updateXY(poly.xpoints[2],poly.ypoints[2]);
														 controlPoints.get(5).updateXY(poly.xpoints[3],poly.ypoints[3]);
														 controlPoints.get(2).updateXY((poly.xpoints[1]+poly.xpoints[2])/2,(poly.ypoints[1]+poly.ypoints[2])/2);
														 controlPoints.get(6).updateXY((poly.xpoints[3]+poly.xpoints[0])/2,(poly.ypoints[3]+poly.ypoints[0])/2);
														 break;
									   case 5: poly.xpoints[2]+=delta[1].x;
											       poly.ypoints[2]+=delta[1].y;
														 poly.xpoints[3]+=(delta[1].x+delta[2].x);
														 poly.ypoints[3]+=(delta[1].y+delta[2].y);
														 poly.xpoints[0]+=delta[2].x;
														 poly.ypoints[0]+=delta[2].y;
														 for (ControlPoint points : controlPoints) points.setVisible(false);
														 controlPoints.get(5).updateXY(poly.xpoints[3],poly.ypoints[3]);
														 controlPoints.get(7).updateXY(poly.xpoints[0],poly.ypoints[0]);
														 controlPoints.get(3).updateXY(poly.xpoints[2],poly.ypoints[2]);
														 controlPoints.get(0).updateXY((poly.xpoints[0]+poly.xpoints[1])/2,(poly.ypoints[0]+poly.ypoints[1])/2);
														 controlPoints.get(2).updateXY((poly.xpoints[1]+poly.xpoints[2])/2,(poly.ypoints[1]+poly.ypoints[2])/2);
														 controlPoints.get(4).updateXY((poly.xpoints[2]+poly.xpoints[3])/2,(poly.ypoints[2]+poly.ypoints[3])/2);
														 controlPoints.get(6).updateXY((poly.xpoints[3]+poly.xpoints[0])/2,(poly.ypoints[3]+poly.ypoints[0])/2);
														 for (ControlPoint points : controlPoints) points.setVisible(true);
														 break;
									   case 6: poly.xpoints[0]+=delta[0].x;
														 poly.ypoints[0]+=delta[0].y;
														 poly.xpoints[3]+=delta[0].x;
														 poly.ypoints[3]+=delta[0].y;
														 controlPoints.get(7).updateXY(poly.xpoints[0],poly.ypoints[0]);
														 controlPoints.get(5).updateXY(poly.xpoints[3],poly.ypoints[3]);
														 controlPoints.get(0).updateXY((poly.xpoints[0]+poly.xpoints[1])/2,(poly.ypoints[0]+poly.ypoints[1])/2);
														 controlPoints.get(4).updateXY((poly.xpoints[2]+poly.xpoints[3])/2,(poly.ypoints[2]+poly.ypoints[3])/2);
														 break;
									   case 7: poly.xpoints[1]+=delta[1].x;
											       poly.ypoints[1]+=delta[1].y;
														 poly.xpoints[0]+=(delta[1].x+delta[2].x);
														 poly.ypoints[0]+=(delta[1].y+delta[2].y);
														 poly.xpoints[3]+=delta[2].x;
														 poly.ypoints[3]+=delta[2].y;
														 for (ControlPoint points : controlPoints) points.setVisible(false);
														 controlPoints.get(7).updateXY(poly.xpoints[0],poly.ypoints[0]);
														 controlPoints.get(1).updateXY(poly.xpoints[1],poly.ypoints[1]);
														 controlPoints.get(5).updateXY(poly.xpoints[3],poly.ypoints[3]);
														 controlPoints.get(0).updateXY((poly.xpoints[0]+poly.xpoints[1])/2,(poly.ypoints[0]+poly.ypoints[1])/2);
														 controlPoints.get(2).updateXY((poly.xpoints[1]+poly.xpoints[2])/2,(poly.ypoints[1]+poly.ypoints[2])/2);
														 controlPoints.get(4).updateXY((poly.xpoints[2]+poly.xpoints[3])/2,(poly.ypoints[2]+poly.ypoints[3])/2);
														 controlPoints.get(6).updateXY((poly.xpoints[3]+poly.xpoints[0])/2,(poly.ypoints[3]+poly.ypoints[0])/2);
														 for (ControlPoint points : controlPoints) points.setVisible(true);
														 break;
									 }
									 break;
		 case POLYGON: break;
		case POLYLINE: {Polygon2D polyline=(Polygon2D)baseShape;   
			             polyline.xpoints[cp.id]=p.x;
									 polyline.ypoints[cp.id]=p.y;}
									 break;
		  case CURVE: {FourPointCurve fpc=(FourPointCurve)baseShape;
									 ArrayList<Point2D.Double> pList=fpc.getControlPoints();
									 Point2D.Double p2d=pList.get(cp.id);
									 p2d.x=p.x;
									 p2d.y=p.y;
									 fpc.update();}
			             break;
		      default: break;
		 }	
	}

	public void controlPointMoveEnd(ControlPoint cp, Point2D.Double p1, Point2D.Double p2) {}

	//=======================================================================

	public void deselect() {super.deselect(); controlPoints=null;}

	//=======================================================================

	public boolean hasRotationAxis() {return ((type==AnnotationType.ELLIPSE)||(type==AnnotationType.BOX));}

	public static Annotation createText(double x0, double y0, String annoteText) {Annotation textA = new Annotation(AnnotationType.TEXT, new Line2D.Double(x0,y0,x0,y0), true); textA.setText(annoteText); return textA;}
	public static Annotation createLine(double x0, double y0, double x1, double y1) {return new Annotation(AnnotationType.LINE,new Line2D.Double(x0,y0,x1,y1));}
	public static Annotation createArrow(double x0, double y0, double x1, double y1) {Annotation arrow=new Annotation(AnnotationType.ARROW,createArrowLine(x0,y0,x1,y1)); arrow.setFilled(true); return arrow;}
	public static Annotation createBox(double x0, double y0, double x1, double y1) {return new Annotation(AnnotationType.BOX,new Polygon2D(new double[] {x0,x1,x1,x0},new double[] {y0,y0,y1,y1},4));}
	public static Annotation createPolygon(double[] xPoints, double[] yPoints) {return new Annotation(AnnotationType.POLYGON,new Polygon2D(xPoints,yPoints,Math.min(xPoints.length,yPoints.length)));}
	public static Annotation createPolyLine() {return new Annotation(AnnotationType.POLYLINE,new Polygon2D(),true);}
	public static Annotation createEllipse(double x0, double y0, double x1, double y1) {return new Annotation(AnnotationType.ELLIPSE,new Polygon2D(new double[] {x0,x1,x1,x0},new double[] {y0,y0,y1,y1},4),true);}
	public static Annotation createFreehandCurve() {return new Annotation(AnnotationType.CURVE,new FourPointCurve(),true);}

	//=======================================================================

	private void renderEllipse(Graphics2D g2, boolean flag) {

		// For an ellipse, the bounding box specified by a 4-point
		// polygon defines the axis of rotation, the height, and the
		// width of the object. The rotation axis should be determined
		// by the east-west axis relative to the screen.

		Polygon2D p=(Polygon2D)baseShape;
		Point2D.Double north=new Point2D.Double((p.xpoints[0]+p.xpoints[1])/2,(p.ypoints[0]+p.ypoints[1])/2);
		Point2D.Double east=new Point2D.Double((p.xpoints[1]+p.xpoints[2])/2,(p.ypoints[1]+p.ypoints[2])/2);
		Point2D.Double south=new Point2D.Double((p.xpoints[2]+p.xpoints[3])/2,(p.ypoints[2]+p.ypoints[3])/2);
		Point2D.Double west=new Point2D.Double((p.xpoints[3]+p.xpoints[0])/2,(p.ypoints[3]+p.ypoints[0])/2);
		double eHeight=north.distance(south);
		double eWidth=east.distance(west);
		
		double dY=east.y-west.y;
		double dX=east.x-west.x;
		
		Ellipse2D.Double ellipse=new Ellipse2D.Double(p.xpoints[0],p.ypoints[0],eWidth,eHeight);
	
		if (dY==0) {
			if (flag) g2.draw(ellipse); else g2.fill(ellipse);
		} else {
			double m=dY/dX;
			double b=west.y-(m*west.x);
			Point2D.Double xIntercept=new Point2D.Double(-b/m,0);
			double hDistance=west.distance(xIntercept);
			double oDistance=west.distance(new Point2D.Double(west.x,0));
			double angle=Math.asin(oDistance/hDistance);
			double quadAngle=(m>=0) ? angle : ((Math.PI*2)-angle);
			if (dX<0) quadAngle+=Math.PI;
			g2.rotate(quadAngle,p.xpoints[0],p.ypoints[0]);
			if (flag) g2.draw(ellipse); else g2.fill(ellipse);
			g2.rotate(-quadAngle,p.xpoints[0],p.ypoints[0]);
		} 
	}

	//=======================================================================

	private void renderPolyLine(Graphics2D g2) {

		// Iterate through the associated polygon and draw lines from
		// point to point; this will bypass the associated errors in using
		// a generalPath, too. Because it's a polyline, the object isn't
		// closed, and we don't do anything for fills.

		Polygon2D p=(Polygon2D)baseShape;
		int[] newX=new int[p.npoints];
		int[] newY=new int[p.npoints];
		for (int loop=0; loop<p.npoints; loop++) {
			newX[loop]=(int)Math.round(p.xpoints[loop]); 
			newY[loop]=(int)Math.round(p.ypoints[loop]);
		}
		g2.drawPolyline(newX,newY,p.npoints);
	}

	//=======================================================================

	private void renderFreehandCurve(Graphics2D g2) {

		FourPointCurve fpc=(FourPointCurve)baseShape;
		GeneralPath gp=fpc.getGeneralPath();
		if (gp!=null)	g2.draw(gp);
	}
	
	//=======================================================================

	private void renderText(Graphics2D g2) {

		g2.setFont(ANNOTATION_FONT);
		g2.drawString(getText(), (int)((Line2D.Double)baseShape).x1, (int)((Line2D.Double)baseShape).y1);
		g2.setFont(DEFAULT_FONT);
		
	}

	//=======================================================================

	public void draw(Graphics2D g2) {

		switch (type) {
			  case TEXT: renderText(g2); break;
		     case CURVE: renderFreehandCurve(g2); break;
		   case ELLIPSE: renderEllipse(g2,true); break;
		  case POLYLINE: renderPolyLine(g2); break;
		}
	}
	
	public void fill(Graphics2D g2) {if (type==AnnotationType.ELLIPSE) renderEllipse(g2,false);}

	//=======================================================================

	public static Point2D.Double[] computeArrowCoordinates(GeneralPath gp) {

		Point2D.Double[] pArray=new Point2D.Double[2];
		double[] coords=new double[2];
		PathIterator pi=gp.getPathIterator(null);
		pi.currentSegment(coords);
		pArray[0]=new Point2D.Double(coords[0],coords[1]);
		pi.next(); pi.next();
		pi.currentSegment(coords);
		pArray[1]=new Point2D.Double(coords[0],coords[1]);
		return pArray;
	}

	public static GeneralPath createArrowLine(double x0, double y0, double x1, double y1) {

		float theta=(float)Math.atan((double)(y1-y0)/(double)(x1-x0));
    float len=(float)Math.sqrt(Math.pow((x1-x0),2)+Math.pow((y1-y0),2));
		float side=(x1<x0) ? -1 : 1;

		// Ideally, we should use a fixed distance from the end point,
		// rather than try and calculate a percentage, as that mucks it up
		// for shorter lengths...The easiest way would be to "draw" a
		// circle around the point (x1,y1) and then compute the
		// line-circle intersection point.  I'm too lazy to do the math
		// right now, though...

		GeneralPath gp=new GeneralPath();
		gp.reset();
		gp.moveTo((float)x0,(float)y0);
		gp.lineTo((float)(x0+((x1-x0)*0.9725f)),(float)(y0+((y1-y0)*0.9725f)));
		gp.moveTo((float)x1,(float)y1);
		gp.lineTo((float)(x1-side*ALEN*(float)Math.cos(theta+AANG)),(float)(y1-side*ALEN*(float)Math.sin(theta+AANG)));
		gp.quadTo((float)(x0+((x1-x0)*0.9725f)),(float)(y0+((y1-y0)*0.9725f)),(float)(x1-side*ALEN*Math.cos(theta-AANG)),
							(float)(y1-side*ALEN*Math.sin(theta-AANG)));
		gp.closePath();
		return gp;
	}
	
	public void addText(String typedChar) {
		String currentString = getText();
		
		if (currentString.equals("Type to add text")){
			currentString = "";
		}
		if (typedChar.equals("\b")){
			if (currentString.length() > 0)
				currentString = currentString.substring(0,currentString.length()-1);
			if (currentString.equals("")){
				currentString = "Type to add text";
			}
		} else {
			currentString = currentString + typedChar;
		}
		setText(currentString);
		
	}
}
