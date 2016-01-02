/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.wl;

import java.io.Serializable;

import java.util.Hashtable;
import java.util.LinkedList;

import javax.media.jai.LookupTableJAI;

import imageviewer.util.BasicCache;

public class WindowLevel implements Serializable {

	public static BasicCache LUT_CACHE=new BasicCache(10);

	// =======================================================================

	int window=0, level=0;
	double rescaleSlope=0, rescaleIntercept=0;
	boolean useRescale=false;

	public WindowLevel() {}
	public WindowLevel(int window, int level) {this.window=window; this.level=level;}
	public WindowLevel(int window, int level, double rescaleSlope, double rescaleIntercept) {

		this.window=window; 
		this.level=level;
		this.rescaleSlope=rescaleSlope;
		this.rescaleIntercept=rescaleIntercept;
	}

	// =======================================================================

	public int getWindow() {return window;}
	public int getLevel() {return level;}

	public double getRescaleSlope() {return rescaleSlope;}
	public double getRescaleIntercept() {return rescaleIntercept;}

	public void setWindow(int x) {window=x;}
	public void setLevel(int x) {level=x;}
	public void setRescaleSlope(double x) {rescaleSlope=x;}
	public void setRescaleIntercept(double x) {rescaleIntercept=x;}
	public void setRescaled(boolean x) {useRescale=x;}

	public boolean isRescaled() {return useRescale;}

	// =======================================================================

	public String toString() {return new String("Window/Level ["+window+","+level+"]");}

	// =======================================================================
	// Note that the method below transforms the image from a single-band
	// into a three-band (RGB) grayscale image.

	public static LookupTableJAI createLinearGrayscaleLookupTable(WindowLevel wl, int maxInputRange, int maxOutputRange) {

		String lutKey=(wl.toString()+"|"+maxInputRange+"|"+maxOutputRange);
		LookupTableJAI ltj=(LookupTableJAI)LUT_CACHE.doLookup(lutKey);
		if (ltj!=null) return ltj;

		int window=wl.getWindow();
		int level=wl.getLevel();
		int midWindow=(int)(window/2);
		int targetWindowStart=level-midWindow;
		int targetWindowEnd=level+midWindow;
		int adjustedWindowStart=(int)((targetWindowStart-wl.getRescaleIntercept())/wl.getRescaleSlope());
		int adjustedWindowEnd=(int)((targetWindowEnd-wl.getRescaleIntercept())/wl.getRescaleSlope());

		int minOutputRange=0;
		int windowStart=(wl.isRescaled()) ? adjustedWindowStart : targetWindowStart;
		int windowEnd=(wl.isRescaled()) ? adjustedWindowEnd : targetWindowEnd;

		if (windowStart<=0) windowStart=0;
		if (windowStart>maxInputRange) windowStart=maxInputRange;
		if (windowEnd<0) windowEnd=0;
		if (windowEnd>maxInputRange) windowEnd=maxInputRange;

		byte[][] lut=new byte[3][maxInputRange+1];
		double windowMappingRatio=((maxOutputRange-minOutputRange)/(double)(window));

		// Ensure that the lookup table that is created does not go beyond
		// the scope of the allocated byte array; odd window/level
		// settings beyond the given pixel range may be specified, in
		// which case we just cap the range to the maximum
		// (maxInputRange).

		for (int i=0; i<Math.min(windowStart,maxInputRange); i++) lut[0][i]=lut[1][i]=lut[2][i]=(byte)minOutputRange; 
		for (int i=Math.min(windowStart,maxInputRange); i<Math.min(windowEnd,maxInputRange); i++) {
			double x=((i-windowStart)*windowMappingRatio);
			lut[0][i]=lut[1][i]=lut[2][i]=(byte)x; 
		}
		for (int i=Math.min(windowEnd,maxInputRange); i<maxInputRange; i++) lut[0][i]=lut[1][i]=lut[2][i]=(byte)maxOutputRange;
		ltj=new LookupTableJAI(lut);
		LUT_CACHE.add(lutKey,ltj);
		return ltj;
	}

	// =======================================================================

	public static LookupTableJAI createLinearLuminanceLookupTable(WindowLevel wl, int threshold, int maxInputRange, int maxOutputRange) {

		String lutKey=(wl.toString()+"|"+maxInputRange+"|"+maxOutputRange);
		LookupTableJAI ltj=(LookupTableJAI)LUT_CACHE.doLookup(lutKey);
		if (ltj!=null) return ltj;

		int window=wl.getWindow();
		int level=wl.getLevel();
		int midWindow=(int)(window/2);
		int targetWindowStart=level-midWindow;
		int targetWindowEnd=level+midWindow;
		int adjustedWindowStart=(int)((targetWindowStart-wl.getRescaleIntercept())/wl.getRescaleSlope());
		int adjustedWindowEnd=(int)((targetWindowEnd-wl.getRescaleIntercept())/wl.getRescaleSlope());

		int minOutputRange=0;
		int windowStart=(wl.isRescaled()) ? adjustedWindowStart : targetWindowStart;
		int windowEnd=(wl.isRescaled()) ? adjustedWindowEnd : targetWindowEnd;

		if (windowStart<=0) windowStart=0;
		if (windowStart>maxInputRange) windowStart=maxInputRange;
		windowStart=(int)Math.max(windowStart,threshold);
		if (windowEnd<0) windowEnd=0;
		if (windowEnd>maxInputRange) windowEnd=maxInputRange;

		byte[] lut=new byte[maxInputRange+1];
		double windowMappingRatio=((maxOutputRange-minOutputRange)/(double)(window));

		// Ensure that the lookup table that is created does not go beyond
		// the scope of the allocated byte array; odd window/level
		// settings beyond the given pixel range may be specified, in
		// which case we just cap the range to the maximum
		// (maxInputRange).

		for (int i=0; i<Math.min(windowStart,maxInputRange); i++) lut[i]=(byte)minOutputRange; 
		for (int i=Math.min(windowStart,maxInputRange); i<Math.min(windowEnd,maxInputRange); i++) {
			double x=((i-windowStart)*windowMappingRatio);
			lut[i]=(byte)x; 
		}
		for (int i=Math.min(windowEnd,maxInputRange); i<maxInputRange; i++) lut[i]=(byte)maxOutputRange;
		ltj=new LookupTableJAI(lut);
		LUT_CACHE.add(lutKey,ltj);
		return ltj;
	}		

	// =======================================================================

	public static LookupTableJAI createLinearLuminanceAlphaLookupTable(WindowLevel wl, int threshold, int maxInputRange, int maxOutputRange) {

		String lutKey=(wl.toString()+"|"+maxInputRange+"|"+maxOutputRange);
		LookupTableJAI ltj=(LookupTableJAI)LUT_CACHE.doLookup(lutKey);
		if (ltj!=null) return ltj;

		int window=wl.getWindow();
		int level=wl.getLevel();
		int midWindow=(int)(window/2);
		int targetWindowStart=level-midWindow;
		int targetWindowEnd=level+midWindow;
		int adjustedWindowStart=(int)((targetWindowStart-wl.getRescaleIntercept())/wl.getRescaleSlope());
		int adjustedWindowEnd=(int)((targetWindowEnd-wl.getRescaleIntercept())/wl.getRescaleSlope());

		int minOutputRange=0;
		int windowStart=(wl.isRescaled()) ? adjustedWindowStart : targetWindowStart;
		int windowEnd=(wl.isRescaled()) ? adjustedWindowEnd : targetWindowEnd;

		if (windowStart<=0) windowStart=0;
		if (windowStart>maxInputRange) windowStart=maxInputRange;
		windowStart=(int)Math.max(windowStart,threshold);
		if (windowEnd<0) windowEnd=0;
		if (windowEnd>maxInputRange) windowEnd=maxInputRange;

		byte[][] lut=new byte[2][maxInputRange+1];
		double windowMappingRatio=((maxOutputRange-minOutputRange)/(double)(window));

		// Ensure that the lookup table that is created does not go beyond
		// the scope of the allocated byte array; odd window/level
		// settings beyond the given pixel range may be specified, in
		// which case we just cap the range to the maximum
		// (maxInputRange).

		for (int i=0; i<Math.min(windowStart,maxInputRange); i++) lut[0][i]=lut[1][i]=(byte)minOutputRange; 
		for (int i=Math.min(windowStart,maxInputRange); i<Math.min(windowEnd,maxInputRange); i++) {
			double x=((i-windowStart)*windowMappingRatio);
			lut[0][i]=lut[1][i]=(byte)x; 
		}
		for (int i=Math.min(windowEnd,maxInputRange); i<maxInputRange; i++) lut[0][i]=lut[1][i]=(byte)maxOutputRange;
		ltj=new LookupTableJAI(lut);
		LUT_CACHE.add(lutKey,ltj);
		return ltj;
	}		

	// =======================================================================

	public static LookupTableJAI createAlphaMaskLookupTable(WindowLevel wl, int threshold, int maxInputRange, int maxOutputRange) {

		String lutKey=(wl.toString()+"|"+maxInputRange+"|"+maxOutputRange);
		LookupTableJAI ltj=(LookupTableJAI)LUT_CACHE.doLookup(lutKey);
		if (ltj!=null) return ltj;

		int window=wl.getWindow();
		int level=wl.getLevel();
		int midWindow=(int)(window/2);
		int targetWindowStart=level-midWindow;
		int targetWindowEnd=level+midWindow;
		int adjustedWindowStart=(int)((targetWindowStart-wl.getRescaleIntercept())/wl.getRescaleSlope());
		int adjustedWindowEnd=(int)((targetWindowEnd-wl.getRescaleIntercept())/wl.getRescaleSlope());

		int minOutputRange=0;
		int windowStart=(wl.isRescaled()) ? adjustedWindowStart : targetWindowStart;
		int windowEnd=(wl.isRescaled()) ? adjustedWindowEnd : targetWindowEnd;

		if (windowStart<=0) windowStart=0;
		if (windowStart>maxInputRange) windowStart=maxInputRange;
		windowStart=(int)Math.max(windowStart,threshold);
		if (windowEnd<0) windowEnd=0;
		if (windowEnd>maxInputRange) windowEnd=maxInputRange;

		byte[][] lut=new byte[2][maxInputRange+1];
		double windowMappingRatio=((maxOutputRange-minOutputRange)/(double)(window));

		// Ensure that the lookup table that is created does not go beyond
		// the scope of the allocated byte array; odd window/level
		// settings beyond the given pixel range may be specified, in
		// which case we just cap the range to the maximum
		// (maxInputRange).

		for (int i=0; i<Math.min(windowStart,maxInputRange); i++) lut[0][i]=(byte)minOutputRange; 
		for (int i=Math.min(windowStart,maxInputRange); i<Math.min(windowEnd,maxInputRange); i++) {
			double x=((i-windowStart)*windowMappingRatio);
			lut[0][i]=(byte)x; 
		}
		for (int i=Math.min(windowEnd,maxInputRange); i<maxInputRange; i++) lut[0][i]=(byte)maxOutputRange;
		for (int i=0; i<maxInputRange; i++) lut[1][i]=(i<threshold)? (byte)0 : (byte)255;
		ltj=new LookupTableJAI(lut);
		LUT_CACHE.add(lutKey,ltj);
		return ltj;
	}		
}
