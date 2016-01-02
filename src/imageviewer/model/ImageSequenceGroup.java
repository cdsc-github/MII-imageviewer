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

// =======================================================================
/**
 * A group of ImageSeqence objects.  
 *
 */
public abstract class ImageSequenceGroup implements PropertiedObject, ImageSequenceGroupProperties, ModelObject {

	protected ArrayList<ImageSequence> groups=new ArrayList<ImageSequence>();
	protected ArrayList<ModelListener> listeners=new ArrayList<ModelListener>();
	protected HashMap<String,Object> properties=new HashMap<String,Object>();

	private boolean isClosing=false;

	/**
	 * Default constructor. 
	 */
	public ImageSequenceGroup() {}

	// =======================================================================

	/**
	 * Get all ImageSequences in the group.
	 * @return
	 */
	public ArrayList<ImageSequence> getGroups() {return groups;}
	
	/**
	 * Get all of our properties.
	 *   
	 * @return
	 */
	public HashMap<String,Object> getProperties() {return properties;}
	
	/**
	 * Get the ImageSequence at the given position.  Retrieves from our internal
	 * representation of images in the sequence.
	 * 
	 * @param x - index of sequence requested
	 * @return
	 */
	public ImageSequence getSequence(int x) {return groups.get(x);}

	/**
	 * Get the ImageSequence at the given position.  Retrieves from our internal
	 * representation of images in the sequence.
	 * 
	 * @param x - index of sequence requested
	 * @return
	 */
	public ImageSequence get(int x) {return groups.get(x);}

	/** 
	 * Returns the number of items in our sequence. 
	 * 
	 * @return
	 */
	public int size() {return groups.size();}

	/**
	 * Assign a list of ImageSequence to this group.  Replaces the old one.
	 * @param x
	 */
	public void setGroups(ArrayList<ImageSequence> x) {groups=x;}
	
	/**
	 * Assigns properties to our group. Replaces old properties.  
	 * 
	 * @param x
	 */
	public void setProperties(HashMap<String,Object> x) {properties=x;}

	/**
	 * Adds a new sequence to the group.
	 * 
	 * @param x
	 */
	public void addSequence(ImageSequence x) {groups.add(x); x.setGroupParent(this);}
	
	/**
	 * Removes an ImageSequence from our group. Unregister the model
	 * first if it's the last object; we need to do this so we have the
	 * appropriate information for the model registry.
	 *
	 * @param x
	 */
	public void removeSequence(ImageSequence x) {if (groups.contains(x)&&(groups.size()==1)) unregisterModel();	groups.remove(x);}

	/**
	 * Adds a new sequence to the group.
	 * 
	 * @param x
	 */
	public void add(ImageSequence x) {addSequence(x);}
	
	/**
	 * Removes an ImageSequence from our group
	 * @param x
	 */
	public void remove(ImageSequence x) {removeSequence(x);}

	/**
	 * Adds all items to our group from a Collection. 
	 * @param x
	 */
	public void addAll(Collection<? extends ImageSequence> x) {groups.addAll(x); for (ImageSequence is : x) is.setGroupParent(this);}

	/* (non-Javadoc)
	 * @see imageviewer.model.PropertiedObject#setProperty(java.lang.String, java.lang.Object)
	 */
	public void setProperty(String x, Object o) {properties.put(x,o);}
	
	/* (non-Javadoc)
	 * @see imageviewer.model.PropertiedObject#getProperty(java.lang.String)
	 */
	public Object getProperty(String x) {return properties.get(x);}

	/**
	 * Is group empty?
	 * @return
	 */
	public boolean isEmpty() {return groups.isEmpty();}

	// =======================================================================

	public void registerModel() {if (getClosingKey()!=null) ModelRegistry.getInstance().map(getClosingKey(),this); ModelRegistry.getInstance().addDependent(getDependentKey(),this);}
	public void unregisterModel() {ModelRegistry.getInstance().removeDependent(getDependentKey(),this); if (getClosingKey()!=null) ModelRegistry.getInstance().unmap(getClosingKey(),this);}

	public void close() {

		if (isClosing) return;
		isClosing=true;
		ArrayList<ModelObject> dependents=ModelRegistry.getInstance().getDependents(getClosingKey());
		if (dependents!=null) {
			dependents=(ArrayList<ModelObject>)dependents.clone();
			for (ModelObject mo : dependents) mo.close();
		}
		unregisterModel();
		isClosing=false;
	}

	public void addListener(ModelListener ml) {listeners.add(ml);}
	public void removeListener(ModelListener ml) {listeners.remove(ml); if (listeners.isEmpty()) close();}
	public void removeListeners() {listeners.clear(); close();}
	public void fireModelEvent(ModelEvent me) {ArrayList<ModelListener> al=(ArrayList<ModelListener>)listeners.clone(); for (ModelListener ml : al) ml.processModelEvent(me);}

	public ArrayList<ModelListener> getListeners() {return listeners;}
}
