/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.graphics;

import java.awt.Color;
import java.awt.PaintContext;
import java.awt.RenderingHints;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import java.util.HashMap;
import java.util.Map;

public class EllipticalGradientContext implements PaintContext {

	protected Point2D mPoint, mRadius; 
	protected Color mC1, mC2;
	Ellipse2D.Double ellipse;
	Line2D.Double line;
	Map<Double,Double> lookup;
	double R;
 
	public EllipticalGradientContext(Point2D p, Color c1, Point2D r, Color c2) {

		mPoint=p;
		mC1=c1;
		mRadius=r;
		mC2=c2;
		double x=p.getX()-mRadius.getX();
		double y=p.getY()-mRadius.getY();
		double w=2*mRadius.getX();
		double h=2*mRadius.getY();
		ellipse=new Ellipse2D.Double(x,y,w,h);
		line=new Line2D.Double();
		R=Point2D.distance(0,0,r.getX(),r.getY());
		initLookup();
	}

	// =======================================================================
 
	public void dispose() {}

	// =======================================================================
 
	public ColorModel getColorModel() {return ColorModel.getRGBdefault();}

	// =======================================================================
 
	public Raster getRaster(int x, int y, int w, int h) {

		WritableRaster raster=getColorModel().createCompatibleWritableRaster(w,h);
		int[] data=new int[w*h*4];
		for(int j=0; j<h; j++) {
			for(int i=0; i<w; i++) {
				double distance=mPoint.distance(x+i,y+j);
				double dy=y+j-mPoint.getY();
				double dx=x+i-mPoint.getX();
				double theta=Math.atan2(dy, dx);
				double xp=mPoint.getX()+R*Math.cos(theta);
				double yp=mPoint.getY()+R*Math.sin(theta);
				line.setLine(mPoint.getX(), mPoint.getY(), xp, yp);
				double roundDegrees=Math.round(Math.toDegrees(theta));
				double radius=lookup.get(Double.valueOf(roundDegrees));
				double ratio=distance/radius;
 				if (ratio>1.0) ratio=1.0;
 				int base=(j*w+i)*4;
				data[base+0]=(int)(mC1.getRed()+ratio*(mC2.getRed()-mC1.getRed()));
				data[base+1]=(int)(mC1.getGreen()+ratio*(mC2.getGreen()-mC1.getGreen()));
				data[base+2]=(int)(mC1.getBlue()+ratio*(mC2.getBlue()-mC1.getBlue()));
				data[base+3]=(int)(mC1.getAlpha()+ratio*(mC2.getAlpha()-mC1.getAlpha()));
			}
		}
		raster.setPixels(0,0,w,h,data);
		return raster;
	}

	// =======================================================================
 
	private double getRadius() {

		double[] coords=new double[6];
		Point2D.Double p=new Point2D.Double();
		double minDistance=Double.MAX_VALUE;
		double flatness=0.005;
		PathIterator pit=ellipse.getPathIterator(null,flatness);
		while (!pit.isDone()) {
			int segment=pit.currentSegment(coords);
			double distance=line.ptSegDist(coords[0],coords[1]);
			if (distance<minDistance) {
				minDistance=distance;
				p.x=coords[0];
				p.y=coords[1];
			}
			pit.next();
		}
		return mPoint.distance(p);
	}

	// =======================================================================
 
	private void initLookup() {

		lookup=new HashMap<Double, Double>();
		for(int j=-180; j<=180; j++) {
			Double key=Double.valueOf(j);
			double theta=Math.toRadians(j);
			double xp=mPoint.getX()+R*Math.cos(theta);
			double yp=mPoint.getY()+R*Math.sin(theta);
			line.setLine(mPoint.getX(),mPoint.getY(),xp,yp);
			Double value=Double.valueOf(getRadius());
			lookup.put(key,value);
		}
		double theta=-0.0;
		Double key=Double.valueOf(theta);
		double xp=mPoint.getX()+R*Math.cos(theta);
		double yp=mPoint.getY()+R*Math.sin(theta);
		line.setLine(mPoint.getX(),mPoint.getY(),xp,yp);
		Double value=Double.valueOf(getRadius());
		lookup.put(key,value);
	}
}
