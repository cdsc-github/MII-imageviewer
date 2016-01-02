/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.util.EventObject;

// =======================================================================

public class ModelEvent extends EventObject {

	public enum EventType {NONE,CLOSE,CLOSE_ALL,CLOSE_PARENT}

	EventType et=EventType.NONE;
	Object target=null;

	public ModelEvent(Object source, EventType et) {super(source); this.et=et;}
	public ModelEvent(Object source, Object target, EventType et) {super(source); this.target=target; this.et=et;}

	// =======================================================================

	public EventType getEvent() {return et;}
	public Object getTarget() {return target;}
}
