/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.processing;

import java.awt.Color;
import java.util.Hashtable;

// =======================================================================

public class ColorMap {

	Hashtable lookupTable=new Hashtable();
	int range=0;

	public ColorMap() {}

	// =======================================================================

	public int getRange() {return range;}
	public Hashtable getLookupTable() {return lookupTable;}

	public void setRange(int x) {range=x;}
	public void setLookupTable(Hashtable x) {lookupTable=x;}

	// =======================================================================

	public void setColor(int index, Color c) {lookupTable.put(index,c);}

	public int[] lookup(int index) {

		Color c=(Color)lookupTable.get(index);
		return (c!=null) ? (new int[] {c.getRed(),c.getGreen(),c.getBlue(),c.getAlpha()}) : (new int[] {255,255,255,0});
	}

	// =======================================================================

	public static ColorMap createLinearLuminanceTable() {

		ColorMap icm=new ColorMap();
		icm.setRange(256);
		for (int loop=0; loop<256; loop++) icm.setColor(loop,new Color(loop,loop,loop,loop));
		return icm;
	}

	// =======================================================================

	public static ColorMap createLinearSpectrumTable(int lowerRange, int upperRange, int size) {

		ColorMap icm=new ColorMap();
		icm.setRange(size);
		float diff=upperRange-lowerRange;
		for (int loop=0; loop<lowerRange; loop++) icm.setColor(loop,new Color(0,0,0,0));
		for (int loop=lowerRange; loop<upperRange; loop++) icm.setColor(loop,new Color(Color.HSBtoRGB(loop/(float)diff,1,1)));
		for (int loop=upperRange; loop<size; loop++) icm.setColor(loop,new Color(Color.HSBtoRGB(1,1,1)));
		return icm;
	}

	// =======================================================================

	public static ColorMap createRedBlueSpectrumTable(int lowerRange, int upperRange, int size) {

		ColorMap icm=new ColorMap();
		icm.setRange(size);
		float diff=upperRange-lowerRange;
		for (int loop=0; loop<lowerRange; loop++) icm.setColor(loop,new Color(0,0,0,0));
		for (int loop=lowerRange; loop<upperRange; loop++) {
			float f=loop/diff;
			if (f>1) f=1;
			icm.setColor(loop,new Color(f,0,1-f,1));
		}
		for (int loop=upperRange; loop<size; loop++) icm.setColor(loop,new Color(1,0,0,1));
		return icm;
	}

	// =======================================================================

	public static ColorMap createLinearTable() {

		ColorMap icm=new ColorMap();
		icm.setRange(256);
		for (int loop=0; loop<256; loop++) icm.setColor(loop,new Color(loop,loop,loop,255));
		return icm;
	}
}
