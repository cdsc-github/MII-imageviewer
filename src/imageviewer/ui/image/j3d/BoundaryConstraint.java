/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

public class BoundaryConstraint {

	boolean xAxis=false, yAxis=false, zAxis=false;
	double[] bounds=new double[] {-Double.MAX_VALUE,-Double.MAX_VALUE,-Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE};

	public BoundaryConstraint() {}

	public BoundaryConstraint(boolean xAxis, boolean yAxis, boolean zAxis, double[] bounds) {this.xAxis=xAxis; this.yAxis=yAxis; this.zAxis=zAxis; this.bounds=bounds;}

	// =======================================================================

	public boolean isXAxis() {return xAxis;}
	public boolean isYAxis() {return yAxis;}
	public boolean isZAxis() {return zAxis;}

	public double[] getBounds() {return bounds;}

	public void setXAxis(boolean x) {xAxis=x;}
	public void setYAxis(boolean x) {yAxis=x;}
	public void setZAxis(boolean x) {zAxis=x;}
	public void setBounds(double[] x) {bounds=x;}
	
}
