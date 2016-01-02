/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.annotation;

import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;

// =======================================================================
// Adapted from http://www.multires.caltech.edu/teaching/demos/java/

public class FourPointCurve implements Shape {

	public static final int MAX_LEVELS=10;

	ArrayList<Point2D.Double> controlPoints=new ArrayList<Point2D.Double>();
	boolean closedCurve=false, interpolated=true, regularSpacing=true;
	double sweight=0, weightScale=0.05;  
	int levels=3, lastLength=-1;
	GeneralPath gp=null;

	double[][] v=new double[4][2], pa=new double[4][3];
	double[][] line=null;

	public FourPointCurve() {}
	public FourPointCurve(ArrayList<Point2D.Double> controlPoints) {this.controlPoints=controlPoints; generateCurve();}

	// =======================================================================

	public ArrayList<Point2D.Double> getControlPoints() {return controlPoints;}
	public double getSWeight() {return sweight;}
	public double getWeightScale() {return weightScale;}

	public boolean isClosedCurve() {return closedCurve;}
	public boolean isInterpolated() {return interpolated;}
	public boolean isRegularSpacing() {return regularSpacing;}

	public GeneralPath getGeneralPath() {return gp;}
	
	public void setControlPoints(ArrayList<Point2D.Double> x) {controlPoints=x;}
	public void setClosedCurve(boolean x) {closedCurve=x;}
	public void setInterpoloated(boolean x) {interpolated=x;}
	public void setRegularSpacing(boolean x) {regularSpacing=x;}
	public void setSWeight(double x) {sweight=(x<0) ? 0 : (x*weightScale);}
	public void setWeightScale(double x) {weightScale=x;}

	// =======================================================================

	public void addPoint(double x, double y) {controlPoints.add(new Point2D.Double(x,y)); gp=generateCurve();}

	// =======================================================================
  // Given (p.length) points this method uses the Neville algorithm to find
  // the value at xx of the polynomial of degree (pa.length-1)
  // interpolating the points pa

  private double[] neville(double[][] pa, int start, double t, int step) {

		for (int i=0; i<4; i++) {
			v[i][0]=pa[start+i*step][1];
			v[i][1]=pa[start+i*step][2];
			for (int j=i-1; j>=0; j--) {
				try {
					double div=(t-pa[start+i*step][0])/(pa[start+i*step][0]-pa[start+j*step][0]);
					v[j][0]=v[j+1][0]+(v[j+1][0]-v[j][0])*div;
					v[j][1]=v[j+1][1]+(v[j+1][1]-v[j][1])*div;
				} catch (ArithmeticException e) {}
			}
		}
		return v[0];
	}

	// =======================================================================
  
  private void cascadeRegular(double[][] line, int total, int levels, int periodic, double w) {

		double a=-1+w;
		double b=9-w;
		for (int l=0; l<levels; l++) {
			int step=(1 << (levels-l));
			int hstep=step >> 1;
			if (periodic==1) {
				for (int i=0; i<total; i+=step) {
					line[(i+hstep) % total][1]=(a*line[(i+(total-step)) % total][1]+b*line[i][1]+b*line[(i+step) % total][1]+a*line[(i+2*step) % total][1])*0.0625f;
					line[(i+hstep) % total][2]=(a*line[(i+(total-step)) % total][2]+b*line[i][2]+b*line[(i+step) % total][2]+a*line[(i+2*step) % total][2])*0.0625f;
				}
			} else {
				double c=5+3*w, d=15-7*w, e=-5+5*w, f=1-w;
				for (int i=0; i<total-step; i+=step) {
					if (i==0) {
						line[hstep][1]=(c*line[0][1]+d*line[step][1]+e*line[2*step][1]+f*line[3*step][1])*0.0625f;
						line[hstep][2]=(c*line[0][2]+d*line[step][2]+e*line[2*step][2]+f*line[3*step][2])*0.0625f;
					} else if (i==total-1-step) {
						line[i+hstep][1]=(f*line[i-2*step][1]+e*line[i-step][1]+d*line[i][1]+c*line[i+step][1])*0.0625f;
						line[i+hstep][2]=(f*line[i-2*step][2]+e*line[i-step][2]+d*line[i][2]+c*line[i+step][2])*0.0625f;
					} else {
						line[i+hstep][1]=(a*line[i-step][1]+b*line[i][1]+b*line[i+step][1]+a*line[i+2*step][1])*0.0625f;
						line[i+hstep][2]=(a*line[i-step][2]+b*line[i][2]+b*line[i+step][2]+a*line[i+2*step][2])*0.0625f;
					}
				}
			}
		}
	}

	// =======================================================================

  private double dist(double[] a, double[] b) {return Math.pow((a[2]-b[2])*(a[2]-b[2])+(a[1]-b[1])*(a[1]-b[1]),0.25);}

	// =======================================================================
    
  private void cascadeIrregular(double[][] line, int total, int levels, int periodic, double w) {

		for (int l=0; l<levels; l++) {
			int step=(1 << (levels-l));
			int hstep=step >> 1;
			line[0][0]=(periodic==1) ? dist(line[total-step],line[0]) : 0;
			for (int i=step; i<total; i+=step) line[i][0]=dist(line[i-step],line[i]);
			if (periodic==1) {
				for (int i=0; i<total; i+=step) {
					if (i==0){

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[0][0];
						pa[2][0]=pa[1][0]+line[step][0];
						pa[3][0]=pa[2][0]+line[2*step][0];
						pa[0][1]=line[total-step][1]; 
						pa[0][2]=line[total-step][2];
						pa[1][1]=line[0][1]; 
						pa[1][2]=line[0][2];
						pa[2][1]=line[step][1]; 
						pa[2][2]=line[step][2];
						pa[3][1]=line[2*step][1]; 
						pa[3][2]=line[2*step][2];
						line[i+hstep][0]=line[i+step][0];

					} else if (i==total-2*step) {

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[i][0];
						pa[2][0]=pa[1][0]+line[i+step][0];
						pa[3][0]=pa[2][0]+line[0][0];
						pa[0][1]=line[i-step][1]; 
						pa[0][2]=line[i-step][2];
						pa[1][1]=line[i][1]; 
						pa[1][2]=line[i][2];
						pa[2][1]=line[i+step][1]; 
						pa[2][2]=line[i+step][2];
						pa[3][1]=line[0][1]; 
						pa[3][2]=line[0][2];
						line[i+hstep][0]=line[i+step][0];

					} else if (i==total-step) {

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[i][0];
						pa[2][0]=pa[1][0]+line[0][0];
						pa[3][0]=pa[2][0]+line[step][0];
						pa[0][1]=line[i-step][1]; 
						pa[0][2]=line[i-step][2];
						pa[1][1]=line[i][1]; 
						pa[1][2]=line[i][2];
						pa[2][1]=line[0][1]; 
						pa[2][2]=line[0][2];
						pa[3][1]=line[step][1]; 
						pa[3][2]=line[step][2];
						line[i+hstep][0]=line[0][0];

					} else {

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[i][0];
						pa[2][0]=pa[1][0]+line[i+step][0];
						pa[3][0]=pa[2][0]+line[i+2*step][0];
						pa[0][1]=line[i-step][1]; 
						pa[0][2]=line[i-step][2];
						pa[1][1]=line[i][1]; 
						pa[1][2]=line[i][2];
						pa[2][1]=line[i+step][1]; 
						pa[2][2]=line[i+step][2];
						pa[3][1]=line[i+2*step][1]; 
						pa[3][2]=line[i+2*step][2];
						line[i+hstep][0]=line[i][0];
					}

					double[] v=neville(pa,0,0.5f*(pa[1][0]+pa[2][0]),1);
					line[i+hstep][1]=(1-w)*v[0]+w*0.5f*(pa[1][1]+pa[2][1]);
					line[i+hstep][2]=(1-w)*v[1]+w*0.5f*(pa[1][2]+pa[2][2]);
				}

			} else {

				for (int i=0; i<total-step; i+=step) {

					if (i==0) {

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[step][0];
						pa[2][0]=pa[1][0]+line[2*step][0];
						pa[3][0]=pa[2][0]+line[3*step][0];
						pa[0][1]=line[0][1]; 
						pa[0][2]=line[0][2];
						pa[1][1]=line[step][1]; 
						pa[1][2]=line[step][2];
						pa[2][1]=line[2*step][1]; 
						pa[2][2]=line[2*step][2];
						pa[3][1]=line[3*step][1]; 
						pa[3][2]=line[3*step][2];
						double[] v=neville(pa,0,0.5f*(pa[0][0]+pa[1][0]),1);
						line[hstep][0]=line[step][0];
						line[i+hstep][1]=(1-w)*v[0]+w*0.5f*(pa[0][1]+pa[1][1]);
						line[i+hstep][2]=(1-w)*v[1]+w*0.5f*(pa[0][2]+pa[1][2]);

					} else if (i==total-1-step) {

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[i-step][0];
						pa[2][0]=pa[1][0]+line[i][0];
						pa[3][0]=pa[2][0]+line[i+step][0];
						pa[0][1]=line[i-2*step][1]; 
						pa[0][2]=line[i-2*step][2];
						pa[1][1]=line[i-step][1]; 
						pa[1][2]=line[i-step][2];
						pa[2][1]=line[i][1]; 
						pa[2][2]=line[i][2];
						pa[3][1]=line[i+step][1]; 
						pa[3][2]=line[i+step][2];
						double[] v=neville(pa,0,0.5f*(pa[2][0]+pa[3][0]),1);
						line[i+hstep][0]=line[i+step][0];
						line[i+hstep][1]=(1-w)*v[0]+w*0.5f*(pa[2][1]+pa[3][1]);
						line[i+hstep][2]=(1-w)*v[1]+w*0.5f*(pa[2][2]+pa[3][2]);

					} else {

						pa[0][0]=0;
						pa[1][0]=pa[0][0]+line[i][0];
						pa[2][0]=pa[1][0]+line[i+step][0];
						pa[3][0]=pa[2][0]+line[i+2*step][0];
						pa[0][1]=line[i-step][1]; 
						pa[0][2]=line[i-step][2];
						pa[1][1]=line[i][1]; 
						pa[1][2]=line[i][2];
						pa[2][1]=line[i+step][1]; 
						pa[2][2]=line[i+step][2];
						pa[3][1]=line[i+2*step][1]; 
						pa[3][2]=line[i+2*step][2];
						double[] v=neville(pa,0,0.5f*(pa[1][0]+pa[2][0]),1);
						line[i+hstep][0]=line[i+step][0];
						line[i+hstep][1]=(1-w)*v[0]+w*0.5f*(pa[1][1]+pa[2][1]);
						line[i+hstep][2]=(1-w)*v[1]+w*0.5f*(pa[1][2]+pa[2][2]);
					}
				}
			}
		}
	}

	// =======================================================================
	// Forward the shape interface stuff to the created internal generalPath.

	public boolean contains(double x, double y) {if (gp==null) gp=generateCurve(); return gp.contains(x,y);}
	public boolean contains(double x, double y, double w, double h) {if (gp==null) gp=generateCurve(); return gp.contains(x,y,w,h);}
	public boolean contains(Point2D p) {if (gp==null) gp=generateCurve(); return gp.contains(p);}
	public boolean contains(Rectangle2D r) {if (gp==null) gp=generateCurve(); return gp.contains(r);}

	public boolean intersects(double x, double y, double w, double h) {if (gp==null) gp=generateCurve(); return gp.intersects(x,y,w,h);}
	public boolean intersects(Rectangle2D r) {if (gp==null) gp=generateCurve(); return gp.intersects(r);}

	public Rectangle getBounds() {if (gp==null) gp=generateCurve(); return gp.getBounds();}
	public Rectangle2D getBounds2D() {if (gp==null) gp=generateCurve(); return gp.getBounds2D();}
	public PathIterator getPathIterator(AffineTransform at) {if (gp==null) gp=generateCurve(); return gp.getPathIterator(at);}
	public PathIterator getPathIterator(AffineTransform at, double flatness) {if (gp==null) gp=generateCurve(); return gp.getPathIterator(at,flatness);}

	// =======================================================================

	public void update() {gp=generateCurve();}

	// =======================================================================

	private GeneralPath generateCurve() {

		GeneralPath newGP=new GeneralPath();
		int np=controlPoints.size();
		if (np>=4) {
			int periodic=(closedCurve) ? 1 : 0;
			int total=(np-(1-periodic))*(1 << levels)+(1-periodic);
			if (total>lastLength) line=new double[total][3];
			lastLength=total;
			for (int i=0; i<np; i++) {
				line[i*(1<<levels)][1]=controlPoints.get(i).x;
				line[i*(1<<levels)][2]=controlPoints.get(i).y;
			}
			if (regularSpacing) cascadeRegular(line,total,levels,periodic,sweight); else cascadeIrregular(line,total,levels,periodic,sweight);
			newGP.moveTo(line[0][1],line[0][2]);
			for (int i=1; i<total; i++) newGP.lineTo(line[i][1],line[i][2]);
			if (closedCurve) newGP.lineTo(line[0][1],line[0][2]);
		}
		return newGP;
	}

	// =======================================================================

	public void renderCurve(Graphics2D g2, boolean plotCurvature, boolean plotCurvePoints, boolean plotControlPoints) {

		int np=controlPoints.size();
		if (np>=4) {
			int periodic=(closedCurve) ? 1 : 0;
			int total=(np-(1-periodic))*(1 << levels)+(1-periodic);
			if (total>lastLength) line=new double[total][3];
			lastLength=total;
			for (int i=0; i<np; i++) {
				line[i*(1<<levels)][1]=controlPoints.get(i).x;
				line[i*(1<<levels)][2]=controlPoints.get(i).y;
			}

			if (regularSpacing) cascadeRegular(line,total,levels,periodic,sweight); else cascadeIrregular(line,total,levels,periodic,sweight);

			if (plotCurvature) {
				if (regularSpacing) {
					for (int i=1; i<total-1; i++) {
						double ddx=line[i+1][1]-2*line[i][1]+line[i-1][1];
						double ddy=line[i+1][2]-2*line[i][2]+line[i-1][2];
						double dx=line[i-1][2]-line[i+1][2];
						double dy=line[i+1][1]-line[i-1][1];
						double proj=(1 << (levels << 1 ))*(ddx*dx+ddy*dy)/(4*(dx*dx+dy*dy));
						dx*=proj; 
						dy*=proj;
						g2.drawLine((int)line[i][1],(int)line[i][2],(int)(line[i][1]-dx),(int)(line[i][2]-dy));
					}
				} else {
					for (int i=1; i<total-1; i++) {
						double ddx=2*((line[i+1][1]-line[i][1])/(line[i+1][0])-(line[i][1]-line[i-1][1])/(line[i][0]))/(line[i+1][0]+line[i][0]);
						double ddy=2*((line[i+1][2]-line[i][2])/(line[i+1][0])-(line[i][2]-line[i-1][2])/(line[i][0]))/(line[i+1][0]+line[i][0]);
						double dx=line[i-1][2]-line[i+1][2];
						double dy=line[i+1][1]-line[i-1][1];
						double proj=32*2*(1 << levels)*(ddx*dx+ddy*dy)/(dx*dx+dy*dy);
						dx*=proj; 
						dy*=proj;
						g2.drawLine((int)line[i][1],(int)line[i][2],(int)(line[i][1]-dx),(int)(line[i][2]-dy));
					}
				}
			}

			for (int i=0; i<total-1; i++) g2.drawLine((int)line[i][1],(int)line[i][2],(int)line[i+1][1],(int)line[i+1][2]);
			if (closedCurve) g2.drawLine((int)line[total-1][1],(int)line[total-1][2],(int)line[0][1],(int)line[0][2]);
			if (plotCurvePoints) for (int i=0; i<total-1; i++) g2.fillRect((int)line[i][1]-2,(int)line[i][2]-2,5,5);
		}
		if (plotControlPoints) for (Point2D.Double p : controlPoints) g2.fillOval((int)p.x-3,(int)p.y-3,7,7);
	}

	// =======================================================================

	public static void main(String[] args) {

		FourPointCurve fpc=new FourPointCurve();
		fpc.addPoint(142,88);
		fpc.addPoint(259,38);
		fpc.addPoint(395,109);
		fpc.addPoint(332,178);
	}
}
