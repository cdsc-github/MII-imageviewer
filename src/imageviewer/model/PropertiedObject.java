/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

/**
 * Interface delineating an object in our model with properties.
 * No guarantees specified concerning defaults.  
 */
public interface PropertiedObject {

	/**
	 * Retrieve a property of our object. 
	 * 
	 * @param name
	 * @return
	 */
	public Object getProperty(String name);
	
	/**
	 * Assign a property to our object. 
	 * 
	 * @param name
	 * @param value
	 */
	public void setProperty(String name, Object value);
}
