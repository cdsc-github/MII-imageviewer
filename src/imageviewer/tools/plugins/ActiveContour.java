/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;

import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;

import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;

import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.TiledImage;

// =======================================================================

public class ActiveContour {

	private static int DEFAULT_KERNEL_SIZE=2;
	private static int MAX_ITERATIONS=40;
	private static int MAX_RADIUS=75;
	private static int MAX_POINTS=9162;
	private static int RESOLUTION=3;

	private static double ALPHA=10;
	private static double BETA=1;
	private static double GAMMA=10;
	private static double EDGE_THRESHOLD=0.05;

	// =======================================================================

	double[] xCircular=null, yCircular=null, eEdge=null;
	ArrayList xNeigh=new ArrayList(), yNeigh=new ArrayList();
	ArrayList iterationPolygons=new ArrayList();
	int[] numNeigh=null;
	boolean storeIterations=false;

	public ActiveContour() {}

	// =======================================================================

	private KernelJAI makeGaussianKernel(int size) {

    float g[]=new float[size*size];
    double weight[]=new double[size];
    double sigma=(double)size/3.0;
    double s=-1.5*sigma;
    for (int i=0; i<size; i++, s+=3.0*sigma/((double)size-1.0)) weight[i]=1.0/(Math.sqrt(6.28)*sigma)*Math.exp(-1.0*s*s/(2.0*sigma*sigma));
    for (int v=0; v<size; v++) {
      for (int u=0; u<size; u++) {
				g[v*size+u]=(float)(weight[u]*weight[v]);
      }
    }
		return new KernelJAI(size,size,g);
  }

	// =======================================================================

	public PlanarImage preprocessImage(RenderedImage ri) {return preprocessImage(ri,KernelJAI.GRADIENT_MASK_SOBEL_HORIZONTAL,KernelJAI.GRADIENT_MASK_SOBEL_VERTICAL);}

	public PlanarImage preprocessImage(RenderedImage ri, KernelJAI hKern, KernelJAI vKern) {

		// Convert the image into a planarImage object for JAI.  Determine
		// the maximum pixel value for normalization. Format the image
		// into a float data type. Divide the image by a constant (the max
		// value) to get a normalized image.

		PlanarImage pi=(ri instanceof PlanarImage) ? (PlanarImage)ri : new TiledImage(ri,false);
		ParameterBlock pb=new ParameterBlock();
		pb.addSource(pi);
		pb.add(DataBuffer.TYPE_FLOAT);
		PlanarImage floatImage=JAI.create("format",pb);
		PlanarImage gaussianImage=JAI.create("convolve",floatImage,makeGaussianKernel(DEFAULT_KERNEL_SIZE));
		PlanarImage gradientMagnitudeImage=JAI.create("gradientmagnitude",gaussianImage,hKern,vKern);
		PlanarImage extrema=JAI.create("extrema",gradientMagnitudeImage);
		double[] maximums=(double[])extrema.getProperty("maximum");
		pb=new ParameterBlock();
		double[] constants={maximums[0]};
		pb.addSource(gradientMagnitudeImage);
		pb.add(constants);
		PlanarImage normalizedImage=JAI.create("dividebyconst",pb);		

		// Do cleanup and return
		
		pi.dispose(); pi=null;
		floatImage.dispose(); floatImage=null;
		gaussianImage.dispose(); gaussianImage=null;
		gradientMagnitudeImage.dispose(); gradientMagnitudeImage=null;
		extrema.dispose(); extrema=null;
		return normalizedImage; 
	}

	// =======================================================================
	// Get the arc lengths for each point. An arc length is the distance
	// from the first point (x0,y0) to a point (x,y).

	private double[] parameterize(double[] x, double[] y){

		double[] arcLength=new double[x.length+1]; 		                                                        // Precursor to arcLength
		arcLength[0]=0; 
		for (int i=0; i<(x.length-1); i++) arcLength[i+1]=Math.sqrt(square(x[i]-x[i+1])+square(y[i]-y[i+1]));	// Distances from point a to point b=a+1		
		arcLength[x.length]=Math.sqrt(square(x[x.length-1]-x[0])+square(y[x.length-1]-y[0]));	                // Add the distances together to get the actual arcLength;
		for (int i=1; i<arcLength.length; i++) arcLength[i]=arcLength[i]+arcLength[i-1];
		return arcLength;		
	}
	
	// =======================================================================
	// Interpolate so that each point on the contour is evenly spaced by
	// a desired resolution.

	private double[] interpolate(double[] d, double[] x) {

		double[] x2=new double[x.length+1];	 		                          // Make x circular
		System.arraycopy(x,0,x2,0,x.length);
		x2[x.length]=x2[0];                     		                      // Create array with desired number of pixels between contour points
		int numArcPoints=(int)(d[d.length-1]/RESOLUTION+1);
		double[] di=new double[numArcPoints];
		for (int i=0; i<numArcPoints; i++) di[i]=i*RESOLUTION;
		CubicSpline x2d=new CubicSpline(d,x2);                            // Create spline of f(arcDist)=x; f(d)=x2
		double[] xi=new double[di.length]; 		                            // Interpolate di into the spline of the object x2d 
		for (int i=0; i<di.length; i++) xi[i]=x2d.interpolate(di[i]);
		return xi;
	}

	// =======================================================================

	private double square(double x) {return x*x;}
	private double mean(double[] p) {double sum=0; for (int i=0; i<p.length; i++) sum+=p[i]; return (sum/p.length);}
  private double minimum(double[] x) {double min=x[0]; for (int i=1; i<x.length; i++) if (x[i]<min) min=x[i]; return min;}
	private double computeNormal(double[] xCircular, double[] yCircular, int n) {return -1/((yCircular[n+2]-yCircular[n])/(xCircular[n+2]-xCircular[n]));}

	private int nearestElementIndex(double[] array, double value) {

		double diff=Math.abs(array[0]-value);
		int nearest=0;
		for(int i=1; i<array.length; i++){
			if (Math.abs(array[i]-value)<diff){
				diff=Math.abs(array[i]-value);
				nearest=i;
			}
		}
		return nearest;
	}

	// =======================================================================
	
	private int prepareArrays(double[] x, double[] y) { 

		int n=x.length;
		xCircular=new double[n+2];                          // "Circularize" the list to help dealing with the first and last points
		xCircular[0]=x[n-1];
		xCircular[n+1]=x[0];
		System.arraycopy(x,0,xCircular,1,n);
		yCircular=new double[n+2];
		yCircular[0]=y[n-1];
		yCircular[n+1]=y[0];
		System.arraycopy(y,0,yCircular,1,y.length);		
		xNeigh.clear();
		yNeigh.clear();
		numNeigh=new int[n];
		eEdge=new double[n];
		return n;
	}

	// =======================================================================
	// 1. Get a normal neighborhood around each point
	// 2. Grows outward until an edge is hit
	// 3. Use the edge value as a spring constant
	// 4. Compute the energy of each point in the normal neighborhood
	// 5. Move to the point that has the lowest energy		
	
	private double[][] computeNormalNeighborhood(double[] x, double[] y, float[] dataArray, int dimY, int numBands) {

		int counter=0;
		if (storeIterations) addIterationPolygon(x,y);
		while (counter<MAX_ITERATIONS) {

			double[] d=parameterize(x,y);
			x=interpolate(d,x);	           
			y=interpolate(d,y);
			int pointsMoved=0;
			int n=prepareArrays(x,y); 
			if (n>MAX_POINTS) break;

			for (int loop=0; loop<n; loop++) {

				double[] xNeighTemp=new double[MAX_RADIUS];
				double[] yNeighTemp=new double[MAX_RADIUS];
				double eEdgeValue=dataArray[(int)(Math.round(y[loop])*numBands*dimY)+(int)(Math.round(x[loop])*numBands)];
		
				// Edge is strong enough -> keep the point stationary; else
				// extend normals.

				if (eEdgeValue>=EDGE_THRESHOLD){	                                                 
					xNeighTemp[0]=x[loop];
					yNeighTemp[0]=y[loop];
					numNeigh[loop]=1;	
				} else {                                                                           
					double normal=computeNormal(xCircular,yCircular,loop);
					double u=(double)Math.cos(Math.atan(normal));
					double v=(double)Math.sin(Math.atan(normal));
					double normalCoeff=0;
					int i=0;
					while (eEdgeValue<EDGE_THRESHOLD) {	          // Keep searching for an edge
						normalCoeff+=(double)(i*Math.pow(-1,i+1));	// Oscillates around the starting point to look for an edge
						xNeighTemp[i]=u*normalCoeff+x[loop];	      // Get normal neighborhood around each point on the curve
						yNeighTemp[i]=v*normalCoeff+y[loop];
						if (xNeighTemp[i]<0) xNeighTemp[i]=0;
						if (yNeighTemp[i]<0) yNeighTemp[i]=0;
						int index=(int)(Math.round(yNeighTemp[i])*numBands*dimY)+(int)(Math.round(xNeighTemp[i])*numBands);
						if (index>dataArray.length) {eEdgeValue=0; break;}
						eEdgeValue=dataArray[index];
						if (++i==MAX_RADIUS) {eEdgeValue=0; break;} // No energy contribution from the edge
					}
					numNeigh[loop]=i;
				}
				eEdge[loop]=eEdgeValue;
				xNeigh.add(xNeighTemp);	
				yNeigh.add(yNeighTemp);
			}
			
			for (int j=0; j<n; j++) { 	                      // Compute the energy of each point on the contour
				
				int l=numNeigh[j];
				double[] xnn=(double[])xNeigh.get(j);
				double[] ynn=(double[])yNeigh.get(j);
				double[] eCont=new double[l];
				double[] eCurv=new double[l];
				double[] eEdgeHooksLaw=new double[l];
				double[] eTotal=new double[l];
				
				for (int i=0; i<l; i++){
					double distX1Y1=Math.sqrt(square(xnn[i]-xCircular[j+2])+square(ynn[i]-yCircular[j+2])); 				        // eCont
					eCont[i]=square(RESOLUTION-distX1Y1);
					eCurv[i]=square(xCircular[j]-2*xnn[i]+xCircular[j+2])+square(yCircular[j]-2*ynn[i]+yCircular[j+2]);     // eCurv
					double distToEdge=Math.sqrt(square(xnn[i]-xnn[l-1])+square(ynn[i]-ynn[l-1]));                           // eEdgeHooksLaw
					eEdgeHooksLaw[i]=0.5*eEdge[j]*distToEdge;
				}
				double meanECont=Math.abs(mean(eCont));
				double meanECurv=Math.abs(mean(eCurv));
				double meanEEdgeHooksLaw=Math.abs(mean(eEdgeHooksLaw));
				for (int i=0; i<l; i++) {
					eTotal[i]=(ALPHA*eCont[i]/meanECont)+(BETA*eCurv[i]/meanECurv)+(GAMMA*eEdgeHooksLaw[i]/meanEEdgeHooksLaw);
				}
				
				double minEnergy=minimum(eTotal);
				int minEnergyIndex=nearestElementIndex(eTotal,minEnergy);
				x[j]=xnn[minEnergyIndex];
				y[j]=ynn[minEnergyIndex];
				if (minEnergyIndex!=0) pointsMoved++;
			}
			if (storeIterations) addIterationPolygon(x,y);
			counter++;
		}
		return new double[][] {x,y};
	}

	// =======================================================================

	public ArrayList getIterationPolygons() {return iterationPolygons;}
	private void addIterationPolygon(double[] x, double[] y) {iterationPolygons.add(getPolygon(x,y));}

	// =======================================================================

	public Polygon getPolygon(double[] x, double[] y) {

		Polygon p=new Polygon(); 
		for (int loop=0; loop<x.length; loop++)	p.addPoint((int)Math.round(x[loop]),(int)Math.round(y[loop]));
		return p;
	}

	// =======================================================================

	public GeneralPath generateNormalsDisplay(double[] x, double[] y) {

		int n=prepareArrays(x,y); 
		double[] neighborhoodTemp={0,1,-1,2,-2,3,-3};
	
		for (int loop=0; loop<n; loop++) {

			double normal=computeNormal(xCircular,yCircular,loop);
			double u=(double)Math.cos(Math.atan(normal));
			double v=(double)Math.sin(Math.atan(normal));
			double[] xNeighTemp=new double[neighborhoodTemp.length];
			double[] yNeighTemp=new double[neighborhoodTemp.length];
			
			for (int i=0; i<neighborhoodTemp.length; i++){
				xNeighTemp[i]=u*neighborhoodTemp[i]+x[loop];
				yNeighTemp[i]=v*neighborhoodTemp[i]+y[loop];
			}
			xNeigh.add(xNeighTemp);
			yNeigh.add(yNeighTemp);
			numNeigh[loop]=neighborhoodTemp.length-1;
		}

		GeneralPath gp=new GeneralPath();
		for (int loop=0; loop<n; loop++){
			double[] xnn=(double[])xNeigh.get(loop);
			double[] ynn=(double[])yNeigh.get(loop);
			int l=numNeigh[loop];
			Polygon p=new Polygon(new int[] {(int)xnn[l-2],(int)xnn[l-1]},new int[] {(int)ynn[l-2],(int)ynn[l-1]},2);
			gp.append(p,false);
		}
		return gp;
	}

	// =======================================================================

	public Object process(ArrayList<Point2D.Double> initialContour, RenderedImage ri, boolean storeIterations) {

		int n=initialContour.size();
		this.storeIterations=storeIterations;
		double[] x=new double[n], y=new double[n];
		for (int loop=0; loop<n; loop++) {
			Point2D.Double p=initialContour.get(loop);
			x[loop]=p.x;
			y[loop]=p.y;
		}
		PlanarImage pi=preprocessImage(ri);
		RenderedImage[] src={pi};
		RasterAccessor ra=new RasterAccessor(pi.getData(),new Rectangle(0,0,pi.getWidth(),pi.getHeight()),(RasterAccessor.findCompatibleTags(src,pi))[0],pi.getColorModel());
		double[][] contour=computeNormalNeighborhood(x,y,ra.getFloatDataArray(0),pi.getWidth(),ri.getSampleModel().getNumBands());
		pi.dispose();
		pi=null;
		return contour; 
	}

	// =======================================================================

	public void flush() {
		
		xCircular=yCircular=eEdge=null;
		xNeigh.clear();
		yNeigh.clear();
		iterationPolygons.clear();
		numNeigh=null;
	}

	// =======================================================================

	public class CubicSpline {

		double[] y=null;                  // y=f(x) tabulated function
		double[] x=null;                  // x in tabulated function f(x)
		double[] d2ydx2=null;             // second derivatives of y
		double yp1=0;                     // first derivative at point one
		double ypn=0;                     // first derivative at point n
		int nPoints=0;                    // no. of tabulated points
		boolean derivCalculated=false;    // true when the derivatives have been calculated

		public CubicSpline(double[] x, double[] y) {

			nPoints=x.length;
			if (nPoints!=y.length) throw new IllegalArgumentException("Arrays x and y are of different length"+nPoints+" "+y.length);
			if (nPoints<3) throw new IllegalArgumentException("A minimum of three data points are needed.");
			this.x=new double[nPoints];
			this.y=new double[nPoints];
			d2ydx2=new double[nPoints];
			for(int i=0; i<nPoints; i++) {this.x[i]=x[i];	this.y[i]=y[i];}
			yp1=1e40;
			ypn=1e40;
		}

		// =======================================================================
		//  Calculates the second derivatives of the tabulated function for
		//  use by the cubic spline interpolation method (interpolate). This
		//  method follows the procedure in Numerical Methods C language
		//  procedure for calculating second derivatives.

		public void calculateDerivative() {

			double[] u=new double[nPoints];
			double p=0, qn=0, sig=0, un=0;

			if (yp1>0.99e30) {
				d2ydx2[0]=u[0]=0;
			}	else {
				d2ydx2[0]=-0.5;
				u[0]=(3.0/(x[1]-x[0]))*((y[1]-y[0])/(x[1]-x[0])-yp1);
			}

			for(int i=1; i<=nPoints-2; i++) {
				sig=(x[i]-x[i-1])/(x[i+1]-x[i-1]);
				p=sig*d2ydx2[i-1]+2.0;
				d2ydx2[i]=(sig-1.0)/p;
				u[i]=(y[i+1]-y[i])/(x[i+1]-x[i])-(y[i]-y[i-1])/(x[i]-x[i-1]);
				u[i]=(6.0*u[i]/(x[i+1]-x[i-1])-sig*u[i-1])/p;
			}

			if (ypn>0.99e30) {
				qn=un=0.0;
			}	else {
				qn=0.5;
				un=(3.0/(x[nPoints-1]-x[nPoints-2]))*(ypn-(y[nPoints-1]-y[nPoints-2])/(x[nPoints-1]-x[nPoints-2]));
			}

			d2ydx2[nPoints-1]=(un-qn*u[nPoints-2])/(qn*d2ydx2[nPoints-2]+1.0);
			for (int k=nPoints-2; k>=0; k--) d2ydx2[k]=d2ydx2[k]*d2ydx2[k+1]+u[k];
			derivCalculated=true;
		}

		// =======================================================================
		// Returns an interpolated value of y for a value of x from a
		// tabulated function y=f(x) after the data has been entered via a
		// constructor.  The derivatives are calculated, bt calculateDerivative(), on
		// the first call to this method ands are then stored for use on all
		// subsequent calls.

		public double interpolate(double xx) {

			if ((xx<x[0])||(xx>x[nPoints-1])) throw new IllegalArgumentException("x ("+xx+") is outside the range of data points ("+x[0]+" to "+x[nPoints-1]);
			if (!derivCalculated) calculateDerivative();
			int k=0, klo=0, khi=nPoints-1;
			double h=0,b=0,a=0, yy=0;

			while (khi-klo>1) {
				k=(khi+klo)>>1;
				if (x[k]>xx) {
					khi=k;
				}	else {
					klo=k;
				}
			}
			h=x[khi]-x[klo];
			if (h==0) {
				throw new IllegalArgumentException("Two values of x are identical: point "+klo+" ("+x[klo]+") and point "+khi+" ("+x[khi]+")" );
			}	else {
				a=(x[khi]-xx)/h;
				b=(xx-x[klo])/h;
				yy=a*y[klo]+b*y[khi]+((a*a*a-a)*d2ydx2[klo]+(b*b*b-b)*d2ydx2[khi])*(h*h)/6.0;
			}
			return yy;
		}
	}
}
