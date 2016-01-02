/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins.fp;

// =======================================================================

public class Cell {

	public int x=0, y=0;
	public double distance=Double.POSITIVE_INFINITY;
	
	boolean marked=false, processed=false;
	int[] rgbValue=new int[3];
	int position=-1;

	public Cell(int x, int y, int r, int g, int b) {this.x=x; this.y=y;	rgbValue[0]=r; rgbValue[1]=g; rgbValue[2]=b;}

	// =======================================================================

	public int[] getRGB() {return rgbValue;}

	public int getPosition() {return position;}
	public double getDistance() {return distance;}

	public boolean isMarked() {return marked;}
	public boolean isProcessed() {return processed;}

	public void setMarked(boolean x) {marked=x;}
	public void setProcessed(boolean x) {processed=x;}
	public void setPosition(int x) {position=x;}
	public void setRGB(int[] x) {rgbValue=x;}
	public void setRGB(int r, int g, int b) {rgbValue[0]=r; rgbValue[1]=g; rgbValue[2]=b;}
	public void setDistance(double x) {distance=x;}

	// =======================================================================

	public void reset() {distance=Double.POSITIVE_INFINITY; rgbValue=new int[3]; position=-1; marked=false; processed=false;}
}
