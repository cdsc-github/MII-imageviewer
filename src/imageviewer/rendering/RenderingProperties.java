/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;

import imageviewer.rendering.wl.WindowLevel;
import imageviewer.rendering.event.ImagePropertyChangeEvent;
import imageviewer.rendering.event.ImagePropertyChangeListener;
import imageviewer.ui.VisualProperties;
import imageviewer.util.SerializeClone;

// =======================================================================

public class RenderingProperties implements VisualProperties {

	public static final String ROTATION=new String("__ROTATION");
	public static final String TRANSLATE_X=new String("__TRANSLATE_X");
	public static final String TRANSLATE_Y=new String("__TRANSLATE_Y");
	public static final String SCALE=new String("__SCALE");
	public static final String VERTICAL_FLIP=new String("__V_FLIP");
	public static final String HORIZONTAL_FLIP=new String("__H_FLIP");
	public static final String WINDOW_LEVEL=new String("__WINDOW/LEVEL");
	public static final String MAX_PIXEL=new String("__MAX_PIXEL");
	public static final String SOURCE_WIDTH=new String("__SOURCE_WIDTH");
	public static final String SOURCE_HEIGHT=new String("__SOURCE_HEIGHT");

	public static final String MAGIC_LENS_FIXED=new String("__MAGIC_LENS_FIXED");
	public static final String MAGIC_LENS_WIDTH=new String("__MAGIC_LENS_WIDTH");
	public static final String MAGIC_LENS_HEIGHT=new String("__MAGIC_LENS_HEIGHT");
	public static final String MAGIC_LENS_HIGHLIGHT=new String("__MAGIC_LENS_HIGHLIGHT");
	public static final String MAGIC_LENS_SCALE=new String("__MAGIC_LENS_SCALE");

	// =======================================================================

	HashMap<String,Object> properties=new HashMap<String,Object>();
	ArrayList<ImagePropertyChangeListener> listeners=new ArrayList();

	public RenderingProperties() {super(); initialize(0.0f,0,0,1,false,false,new WindowLevel(),0,0,0);}
	public RenderingProperties(WindowLevel wl) {super(); initialize(0.0f,0,0,1,false,false,wl,0,0,0);}
	public RenderingProperties(double scale, WindowLevel wl) {super(); initialize(0.0f,0,0,scale,false,false,wl,0,0,0);}
	public RenderingProperties(WindowLevel wl, int maxPixelValue, int width, int height) {super(); initialize(0.0f,0,0,1,false,false,wl,maxPixelValue,width,height);}

	public RenderingProperties(float rotation, double translateX, double translateY, double scale, boolean verticalFlip, 
														 boolean horizontalFlip, WindowLevel wl, int maxPixelValue, int width, int height) {

		super(); 
		initialize(rotation,translateX,translateY,scale,verticalFlip,horizontalFlip,wl,maxPixelValue,width,height);
	}

	public RenderingProperties(HashMap<String,Object> properties) {this.properties=properties;}

	// =======================================================================

	private void initialize(float rotation, double translateX, double translateY, double scale, boolean verticalFlip, boolean horizontalFlip, 
													WindowLevel wl, int maxPixelValue, int width, int height) {

		properties.put(ROTATION,new Float(rotation));
		properties.put(TRANSLATE_X,new Double(translateX));
		properties.put(TRANSLATE_Y,new Double(translateY));
		properties.put(SCALE,new Double(scale));
		properties.put(VERTICAL_FLIP,new Boolean(verticalFlip));
		properties.put(HORIZONTAL_FLIP,new Boolean(horizontalFlip));
		properties.put(WINDOW_LEVEL,wl);
		properties.put(MAX_PIXEL,new Integer(maxPixelValue));
		properties.put(SOURCE_WIDTH,new Integer(width));
		properties.put(SOURCE_HEIGHT,new Integer(height));
	}

	// =======================================================================

	public HashMap getProperties() {return properties;}
	public Object getProperty(String x) {return properties.get(x);}

	public void setProperty(String x, Object o) {

		Object oldValue=properties.put(x,o);
		ImagePropertyChangeEvent ipce=new ImagePropertyChangeEvent(this,new String[] {x},new Object[] {oldValue},new Object[] {o});
		firePropertyChange(ipce);
	}

	public void setProperties(String[] x, Object[] o) {

		Object[] oldValues=new Object[x.length];
		for (int loop=0; loop<x.length; loop++) oldValues[loop]=properties.put(x[loop],o[loop]);
		ImagePropertyChangeEvent ipce=new ImagePropertyChangeEvent(this,x,oldValues,o);
		firePropertyChange(ipce);
	}

	// =======================================================================

	public void addListener(ImagePropertyChangeListener x) {listeners.add(x);}
	public void removeListener(ImagePropertyChangeListener x) {listeners.remove(x);}

	public void firePropertyChange(ImagePropertyChangeEvent ipce) {

		for (int loop=0, n=listeners.size(); loop<n; loop++) {
			ImagePropertyChangeListener ipcl=listeners.get(loop);
			ipcl.propertyChangeEvent(ipce);
		}
	}

	// =======================================================================
	// Because of the underlying hashmap, we use a serialized clone of
	// the properties because it's a pain to try and do a deep copy
	// otherwise. Note that for this to work, all objects in the
	// properties hashmap need to be serializable. Also note that the
	// listeners are not copied; these need to be assigned later.

	public RenderingProperties copy() {return new RenderingProperties((HashMap<String,Object>)SerializeClone.copy(properties));}
}
