/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import imageviewer.model.ModelEvent.EventType;
import imageviewer.util.ImageCache;
	
// =======================================================================
/**
 * A sequence of images.  Essentially an ArrayList with properties and
 * listeners.
 *
 */
public abstract class ImageSequence implements PropertiedObject, ImageSequenceProperties, ModelObject {

	protected ArrayList<Image> sequence=new ArrayList<Image>();
	protected ArrayList<ModelListener> listeners=new ArrayList<ModelListener>();
	protected HashMap<String,Object> properties=new HashMap<String,Object>();
	protected ImageSequenceGroup groupParent=null;

	/**
	 * Default constructor. 
	 */
	public ImageSequence() {}

	// =======================================================================
	/**
	 * Get our sequence of images.
	 * @return
	 */
	public ArrayList<Image> getSequence() {return sequence;}
	
	/**
	 * Get all properties associated with our sequence.
	 * 
	 * @return
	 */
	public HashMap<String,Object> getProperties() {return properties;}

	/**
	 * Get the image at the given index. 
	 * 
	 * @param x
	 * @return
	 */
	public Image getImage(int x) {return (Image)sequence.get(x);}
	
	/**
	 * Collections syntax for getting at given index.
	 *  
	 * @param x
	 * @return
	 */
	public Image get(int x) {return (Image)sequence.get(x);}

	/**
	 * Returns number of images in sequence. 
	 * 
	 * @return
	 */
	public int size() {return sequence.size();}
	
	/**
	 * Returns the index at which this Image is at.
	 * @param i
	 * @return
	 */
	public int indexOf(Image i) {return sequence.indexOf(i);}

	/**
	 * Assign the sequence of images to this list.  Replaces the old.
	 * @param x
	 */
	public void setSequence(ArrayList<Image> x) {sequence=x;}
	
	/**
	 * Assign properties of this sequence.  Replaces the old.
	 * 
	 * @param x
	 */
	public void setProperties(HashMap<String,Object> x) {properties=x;}
	
	/**
	 * Remove this image from this sequence. 
	 * 
	 * @param x
	 */
	public void removeImage(Image x) {sequence.remove(x);}
	
	/**
	 * Set this image at this position.
	 * 
	 * @param x
	 * @param i
	 */
	public void setImage(int x, Image i) {sequence.set(x,i);}
	
	/**
	 * Add a new image to the sequence (at the end).
	 * @param x
	 */
	public void addImage(Image x) {sequence.add(x);}

	/**
	 * Add the specified element to the end of the sequence. 
	 * 
	 * @param x
	 */
	public void add(Image x) {sequence.add(x);}
	
	/**
	 * Add all the items in this collection to the sequence. 
	 * 
	 * @param x
	 */
	public void addAll(Collection<? extends Image> x) {sequence.addAll(x);}

	/* (non-Javadoc)
	 * @see imageviewer.model.PropertiedObject#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String x, Object o) {properties.put(x,o);}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.PropertiedObject#getProperty(java.lang.String)
	 */
	public Object getProperty(String x) {return properties.get(x);}

	/**
	 * Test if this list has no elements.
	 * 
	 * @return
	 */
	public boolean isEmpty() {return sequence.isEmpty();}

	public ImageSequenceGroup getGroupParent() {return groupParent;}
	public void setGroupParent(ImageSequenceGroup x) {groupParent=x;}

	// =======================================================================

	/**
	 * Long description for the sequence.
	 * @return
	 */
	public String getShortDescription() {return "image series";}
	
	
	/**
	 * Short description for the sequence. 
	 * @return
	 */
	public String[] getLongDescription() {return new String[] {"image series"};}

	// =======================================================================

	/**
	 * Maximum pixel value of images within the sequence. 
	 * 
	 * @return
	 */
	public int getMaxPixelValue() {

		int maxValue=0;
		for (int loop=0, n=sequence.size(); loop<n; loop++) {
			Image i=(Image)sequence.get(loop);
			int imageMaxValue=i.getMaxPixelValue();
			maxValue=(maxValue<imageMaxValue) ? imageMaxValue : maxValue;
		}
		return maxValue;
	}

	// =======================================================================
	/**
	 * Get the next image in the sequence, given the current one.  Returns null if 
	 * at the end of the list, or the image is not found.
	 * 
	 * @param current
	 * @return
	 */
	public Image next(Image current) {

		int targetIndex=sequence.indexOf(current);
		if (targetIndex!=-1) {
			if (targetIndex==(sequence.size()-1)) targetIndex=-1;
			return (Image)sequence.get(targetIndex+1);
		}
		return null;
	}

	/**
	 * Get the previous image in the sequence, given the current.  Returns null if 
	 * at the beginning of the list, or the image is not found.
	 * 
	 * @param current
	 * @return
	 */
	public Image previous(Image current) {

		int targetIndex=sequence.indexOf(current);
		if (targetIndex!=-1) {
			if (targetIndex==0) targetIndex=sequence.size();
			return (Image)sequence.get(targetIndex-1);
		}
		return null;
	}

	// =======================================================================
	// This model needs to know who listens to it so that we can handle
	// events that may propagate in the GUI (e.g., a close for a series
	// could close all instances of it; a study close should close all
	// series, etc.).

	private void clearImageCache() {for (Image i : sequence) {i.unload();	ImageCache.getDefaultImageCache().remove(i);}	System.gc();}

	public ArrayList<ModelListener> getListeners() {return listeners;}

	public void addListener(ModelListener ml) {listeners.add(ml);}
	public void removeListener(ModelListener ml) {listeners.remove(ml); if (listeners.isEmpty()) {clearImageCache(); close();}}
	public void removeListeners() {close(); listeners.clear(); clearImageCache();}

	public void fireModelEvent(ModelEvent me) {

		switch (me.getEvent()) {
		     case CLOSE_ALL: removeListeners(); break;
		  case CLOSE_PARENT: groupParent.close(); break;
		}
	}

	public void registerModel() {if (getClosingKey()!=null) ModelRegistry.getInstance().map(getClosingKey(),this); ModelRegistry.getInstance().addDependent(getDependentKey(),this);}
	public void unregisterModel() {ModelRegistry.getInstance().removeDependent(getDependentKey(),this); if (getClosingKey()!=null) ModelRegistry.getInstance().unmap(getClosingKey(),this);}

	// Clone the list if needed, as we may modify the underling listener
	// arrayList through the firing event.  We want to ensure that all
	// listeners get the event. Probably could sync on this object, but
	// it would get too complicated. For parent closures, find the
	// parent associated with this sequence and let it handle the event.

	public void close() {

		ArrayList<ModelListener> al=(ArrayList<ModelListener>)listeners.clone(); 
		ModelEvent me=new ModelEvent(this,EventType.CLOSE);
		for (ModelListener ml : al) ml.processModelEvent(me);
		unregisterModel();
		groupParent.remove(this);
	}
}
