/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering.event;

import java.util.EventObject;

public class GroupPropertyChangeEvent extends EventObject {

	private static int EVENT_ID_COUNTER=0;

	// =======================================================================

	protected ImagePropertyChangeEvent ipce=null;
	int eventID=0;

	public GroupPropertyChangeEvent(Object source, ImagePropertyChangeEvent ipce) {

		super(source);
		this.ipce=ipce;
		eventID=EVENT_ID_COUNTER++;
	}

	// =======================================================================

	public ImagePropertyChangeEvent getImagePropertyChangeEvent() {return ipce;}
	public int getEventID() {return eventID;}

	public void setSource(Object x) {source=x;}
	public void setImagePropertyChangeEvent(ImagePropertyChangeEvent x) {ipce=x;}

	// =======================================================================

	public String toString() {return new String("[ID "+eventID+"] "+ipce);}
}
