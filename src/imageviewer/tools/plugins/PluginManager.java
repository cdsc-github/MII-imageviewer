/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools.plugins;

import java.lang.reflect.Constructor;
import java.util.Hashtable;

import org.apache.log4j.Logger;

// =======================================================================

public class PluginManager {

	private static final Hashtable<Class,Plugin> CACHE=new Hashtable<Class,Plugin>();
	private static final Logger LOG=Logger.getLogger("imageViewer.plugin");

	public static Plugin getPlugin(Class c) {

		Plugin p=CACHE.get(c);
		if (p==null) {
			final Class[] constructorParameterTypes={};
			final Object[] constructorParameters={};		
			try {
				Constructor newConstructor=c.getConstructor(constructorParameterTypes);	
				p=(Plugin)newConstructor.newInstance(constructorParameters);
				CACHE.put(c,p);
			} catch (Exception exc) {
				LOG.error("Unable to create plugin: "+c);
			}
		}
		return p;
	}
}

