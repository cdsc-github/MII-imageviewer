/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.event;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.apache.log4j.Logger;

// =======================================================================

public class MenuActionListenerFactory {

	private static final Hashtable<Class,MenuActionListener> CACHE=new Hashtable<Class,MenuActionListener>();
	private static final Logger LOG=Logger.getLogger("imageViewer.system");

	public static MenuActionListener getListener(Class c) {

		MenuActionListener mal=CACHE.get(c);
		if (mal==null) {
			final Class[] constructorParameterTypes={};
			final Object[] constructorParameters={};		
			try {
				Constructor newConstructor=c.getConstructor(constructorParameterTypes);	
				mal=(MenuActionListener)newConstructor.newInstance(constructorParameters);
				CACHE.put(c,mal);
			} catch (Exception exc) {
				LOG.error("Unable to create listener: "+c);
			}
		}
		return mal;
	}
}
