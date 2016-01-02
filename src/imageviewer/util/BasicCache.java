/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.util.Hashtable;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.Logger;

// =======================================================================

public class BasicCache {

	protected static Logger LOG=Logger.getLogger("imageviewer.cache");

	// =======================================================================

	protected Hashtable<String,Object> cache=new Hashtable<String,Object>();
	protected ConcurrentLinkedQueue<String> keys=new ConcurrentLinkedQueue<String>();
	protected int cacheSize=10;

	public BasicCache() {}
	public BasicCache(int cacheSize) {this.cacheSize=cacheSize;}

	// =======================================================================
	
	public Hashtable<String,Object> getCache() {return cache;}
	public ConcurrentLinkedQueue<String> getKeys() {return keys;}
	public int getCacheSize() {return cacheSize;}

	public void setCache(Hashtable<String,Object> x) {cache=x;}
	public void setKeys(ConcurrentLinkedQueue<String> x) {keys=x;}
	public void setCacheSize(int x) {cacheSize=x;}

	// =======================================================================

	public Object doLookup(String cacheKey) {

		boolean isCached=keys.contains(cacheKey);
		if (isCached) {
			keys.remove(cacheKey);
			keys.add(cacheKey);
			return cache.get(cacheKey);
		}
		return null;
	}

	// =======================================================================

	public void add(String cacheKey, Object cacheObject) {

		if (keys.size()==cacheSize) {
			Object o=keys.poll();
			cache.remove(o);
		}
		cache.put(cacheKey,cacheObject);
		keys.add(cacheKey);
	}

	// =======================================================================

	public void clear() {keys.clear(); cache.clear();}
}
