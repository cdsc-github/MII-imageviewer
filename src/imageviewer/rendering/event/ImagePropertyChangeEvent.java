/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.event;

import java.util.EventObject;

public class ImagePropertyChangeEvent extends EventObject {

	private static int EVENT_ID_COUNTER=0;

	// =======================================================================

	protected String[] propertyName=null;
	protected Object[] oldValue=null, newValue=null;
	int eventID=0;

	public ImagePropertyChangeEvent(Object source, String[] propertyName, Object[] oldValue, Object[] newValue) {

		super(source);
		this.propertyName=propertyName;
		this.oldValue=oldValue;
		this.newValue=newValue;
		eventID=EVENT_ID_COUNTER++;
	}

	// =======================================================================

	public String[] getPropertyName() {return propertyName;}
	public Object[] getOldValue() {return oldValue;}
	public Object[] getNewValue() {return newValue;}

	public int getEventID() {return eventID;}

	public void setSource(Object x) {source=x;}
	public void setOldValue(Object[] x) {oldValue=x;}
	public void setNewValue(Object[] x) {newValue=x;}

	// =======================================================================

	public String toString() {return new String("[ID "+eventID+"] "+propertyName);}
}
