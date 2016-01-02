/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// =======================================================================
// Taken from the source code for java.awt.Polygon JDK 1.5.  Adapted
// to use double coordinates to store the polygon information so there
// are no rounding errors in dealing with transformations.

public class Polygon2D implements Shape, java.io.Serializable {

	public int npoints;
	public double xpoints[], ypoints[];
    
	protected Rectangle bounds;
    
	public Polygon2D() {xpoints=new double[4]; ypoints=new double[4];}

	public Polygon2D(double xpoints[], double ypoints[], int npoints) {

		if (npoints>xpoints.length || npoints>ypoints.length) {
			throw new IndexOutOfBoundsException("npoints > xpoints.length || npoints > ypoints.length");
		}
		this.npoints=npoints;
		this.xpoints=new double[npoints];
		this.ypoints=new double[npoints];
		System.arraycopy(xpoints,0,this.xpoints,0,npoints);
		System.arraycopy(ypoints,0,this.ypoints,0,npoints);	
	}

	// =======================================================================

	public void reset() {npoints=0;	bounds=null;}
	public void invalidate() {bounds=null;}

	public boolean contains(Point p) {return contains(p.x,p.y);}
	public boolean contains(Point2D p) {return contains(p.getX(),p.getY());}
	public boolean intersects(Rectangle2D r) {return intersects(r.getX(),r.getY(),r.getWidth(),r.getHeight());}
	public boolean contains(Rectangle2D r) {return contains(r.getX(),r.getY(),r.getWidth(),r.getHeight());}

	public PathIterator getPathIterator(AffineTransform at) {return new Polygon2DPathIterator(this, at);}
	public PathIterator getPathIterator(AffineTransform at, double flatness) {return getPathIterator(at);}

	public Rectangle2D getBounds2D() {return getBounds();}

	// =======================================================================

	public void translate(double deltaX, double deltaY) {

		for (int i=0; i<npoints; i++) {
	    xpoints[i]+=deltaX;
	    ypoints[i]+=deltaY;
		}
		if (bounds!=null) bounds.translate((int)Math.round(deltaX),(int)Math.round(deltaY));
	}

	// =======================================================================

	private void calculateBounds(double xpoints[], double ypoints[], int npoints) {

		double boundsMinX=Integer.MAX_VALUE;
		double boundsMinY=Integer.MAX_VALUE;
		double boundsMaxX=Integer.MIN_VALUE;
		double boundsMaxY=Integer.MIN_VALUE;
	
		for (int i=0; i<npoints; i++) {
	    double x=xpoints[i];
	    boundsMinX=Math.min(boundsMinX,x);
	    boundsMaxX=Math.max(boundsMaxX,x);
	    double y=ypoints[i];
	    boundsMinY=Math.min(boundsMinY,y);
	    boundsMaxY=Math.max(boundsMaxY,y);
		}
		bounds=new Rectangle((int)boundsMinX,(int)boundsMinY,(int)(boundsMaxX-boundsMinX),(int)(boundsMaxY-boundsMinY));
	}

	// =======================================================================

	private void updateBounds(double x, double y) {

		if (x<bounds.x) {
	    bounds.width=(int)(bounds.width+(bounds.x-x));
	    bounds.x=(int)Math.round(x);
		}	else {
	    bounds.width=(int)Math.max(bounds.width,x-bounds.x);
		}
		if (y<bounds.y) {
	    bounds.height=(int)(bounds.height+(bounds.y-y));
	    bounds.y=(int)Math.round(y);
		}	else {
	    bounds.height=(int)Math.max(bounds.height,y-bounds.y);
		}
	}	

	// =======================================================================

	public void addPoint(double x, double y) {

		if (npoints==xpoints.length) {
	    double tmp[]=new double[npoints*2];
	    System.arraycopy(xpoints,0,tmp,0,npoints);
	    xpoints=tmp;
	    tmp=new double[npoints*2];
	    System.arraycopy(ypoints,0,tmp,0,npoints);
	    ypoints=tmp;
		}
		xpoints[npoints]=x;
		ypoints[npoints]=y;
		npoints++;
		if (bounds!=null) updateBounds(x,y);
	}

	// =======================================================================

	public Rectangle getBounds() {

		if (npoints==0) return new Rectangle();
		if (bounds==null) calculateBounds(xpoints,ypoints,npoints);
		return bounds.getBounds();
	}

	// =======================================================================

	public boolean contains(double x, double y) {

		if (npoints<=2 || !getBounds().contains(x,y)) return false;
		int hits=0;

		double lastx=xpoints[npoints-1];
		double lasty=ypoints[npoints-1];
		double curx, cury;

		// Walk the edges of the polygon

		for (int i=0; i<npoints; lastx=curx, lasty=cury, i++) {
	    curx=xpoints[i];
	    cury=ypoints[i];
	    if (cury==lasty) continue;

	    double leftx;
	    if (curx<lastx) {
				if (x>=lastx) {
					continue;
				}
				leftx=curx;
	    } else {
				if (x>=curx) continue;
				leftx=lastx;
	    }

	    double test1, test2;
	    if (cury<lasty) {
				if (y<cury || y>=lasty) continue;
				if (x<leftx) {
					hits++;
					continue;
				}
				test1=x-curx;
				test2=y-cury;
	    } else {
				if (y<lasty || y>=cury) continue;
				if (x<leftx) {
					hits++;
					continue;
				}
				test1=x-lastx;
				test2=y-lasty;
	    }

	    if (test1<(test2/(lasty-cury)*(lastx-curx))) hits++;
		}
		return ((hits & 1)!=0);
	}

	// =======================================================================

	private sun.awt.geom.Crossings getCrossings(double xlo, double ylo, double xhi, double yhi) {

		sun.awt.geom.Crossings cross=new sun.awt.geom.Crossings.EvenOdd(xlo,ylo,xhi,yhi);
		double lastx=xpoints[npoints-1];
		double lasty=ypoints[npoints-1];
		double curx, cury;

		for (int i=0; i<npoints; i++) { 		// Walk the edges of the polygon
	    curx=xpoints[i];
	    cury=ypoints[i];
	    if (cross.accumulateLine(lastx,lasty,curx,cury)) return null;
	    lastx=curx;
	    lasty=cury;
		}
		return cross;
	}

	// =======================================================================

	public boolean intersects(double x, double y, double w, double h) {

		if (npoints <= 0 || !getBounds().intersects(x,y,w,h)) return false;
		sun.awt.geom.Crossings cross=getCrossings(x,y,x+w,y+h);
		return (cross==null || !cross.isEmpty());
	}

	// =======================================================================

	public boolean contains(double x, double y, double w, double h) {

		if (npoints<=0 || !getBounds().intersects(x,y,w,h)) return false;
		sun.awt.geom.Crossings cross=getCrossings(x, y, x+w, y+h);
		return (cross!=null && cross.covers(y, y+h));
	}

	// =======================================================================

	private class Polygon2DPathIterator implements PathIterator {

		Polygon2D poly;
		AffineTransform transform;
		int index;

		public Polygon2DPathIterator(Polygon2D pg, AffineTransform at) {poly=pg; transform=at; if (pg.npoints==0) index=1;}

		public boolean isDone() {return (index>poly.npoints);}
		public void next() {index++;}
		public int getWindingRule() {return WIND_EVEN_ODD;}

		public int currentSegment(float[] coords) {

	    if (index>=poly.npoints) return SEG_CLOSE;
	    coords[0]=(float)poly.xpoints[index];
	    coords[1]=(float)poly.ypoints[index];
	    if (transform!=null) transform.transform(coords,0,coords,0,1);
	    return (index==0) ? SEG_MOVETO : SEG_LINETO;
		}

		public int currentSegment(double[] coords) {

	    if (index>=poly.npoints) return SEG_CLOSE;
	    coords[0]=poly.xpoints[index];
	    coords[1]=poly.ypoints[index];
	    if (transform!=null) transform.transform(coords,0,coords,0,1);
	    return (index==0) ? SEG_MOVETO : SEG_LINETO;
		}
	}
}
