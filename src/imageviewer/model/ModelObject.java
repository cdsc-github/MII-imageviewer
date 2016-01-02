/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.util.ArrayList;

// =======================================================================

public interface ModelObject {

	public ArrayList<? extends ModelListener> getListeners();

	public Object getClosingKey();                 // Primary key for this object; if this object is closed, other objects dependent on this key are closed.
	public Object getDependentKey();               // The key of the object that this object is dependent on; if the dependent key is closed, this object closes.

	public void addListener(ModelListener ml);
	public void close();                           // Close the object
	public void fireModelEvent(ModelEvent me);
	public void registerModel();                   // Register the closeable object to the CloseableTable
	public void removeListener(ModelListener ml);
	public void removeListeners();
	public void unregisterModel();                 // Unregister the closeable object to the CloseableTable
}
