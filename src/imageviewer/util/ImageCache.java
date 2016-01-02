/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.media.jai.JAI;

import org.apache.log4j.Logger;

import imageviewer.model.Image;
import imageviewer.ui.ApplicationContext;

// =======================================================================

public class ImageCache extends BasicCache {

	private static final int DEFAULT_IMAGE_CACHE_SIZE=((Integer)ApplicationContext.getContext().getProperty(ApplicationContext.DEFAULT_IMAGE_CACHE_SIZE)).intValue();
	private static final int MAX_ITERATIONS=3;

	private static final Runtime RUNTIME=Runtime.getRuntime();
	private static final ImageCache DEFAULT_CACHE=new ImageCache();

	private static float MEMORY_THRESHOLD=((Float)ApplicationContext.getContext().getProperty(ApplicationContext.MEMORY_THRESHOLD)).floatValue();
	private static float MAX_MEMORY_THRESHOLD=((Float)ApplicationContext.getContext().getProperty(ApplicationContext.MAXIMUM_MEMORY_THRESHOLD)).floatValue();

	// =======================================================================

	boolean obeyFixedCacheSize=false;
	boolean maxMemoryReached=false;

	public ImageCache() {super();}
	public ImageCache(int cacheSize) {super(cacheSize); obeyFixedCacheSize=true;}

	// =======================================================================

	public static ImageCache getDefaultImageCache() {return DEFAULT_CACHE;}

	// =======================================================================
	// Determine if the memory heap is full or not by comparing the amount
	// of reported free memory versus the total memory. If the amount
	// exceeds a given threshold, then return true.

	private boolean isHeapFull() {

		double freeMemory=RUNTIME.freeMemory();
		double totalMemory=RUNTIME.totalMemory();
		double percentUsed=((totalMemory-freeMemory)/totalMemory);
		return (percentUsed>=MEMORY_THRESHOLD);
	}

	// =======================================================================
	// Add an image to the cache. Check to make sure that the heap isn't full
	// or that any size limitations are handled accordingly.  Then add the
	// key and object into the cache.

	public void add(Image i) {

		if ((obeyFixedCacheSize)&&(keys.size()==cacheSize)) {
			LOG.warn("Fixed image cache size exceeded, removing older cached images.");
			removeOldCacheItems();
		}

		if (isHeapFull()) {System.gc();	if (isHeapFull()) removeOldCacheItems();}
		Image[] sources=i.getSources();

		// Loop through the sources given for this image. Check to see if
		// it's in the linked list cache already; if so, then join it to
		// that existing group and move that cachedImageWrapper to the
		// end.

		if (sources!=null) {
			int listSize=keys.size();
			for (int loop=0; loop<sources.length; loop++) {
				Image aSource=sources[loop];
				String cacheKey=aSource.getKey();
				boolean isCached=keys.contains(cacheKey);
				if (isCached) {
					keys.remove(cacheKey);
					keys.add(cacheKey);
					CachedImageWrapper ciw=(CachedImageWrapper)cache.get(cacheKey);
					ciw.addDependentImage(i);
				}
			}
		} 
		
		String cacheKey=i.getKey();
		cache.put(cacheKey,new CachedImageWrapper(i));
		keys.add(cacheKey);
	}

	// =======================================================================
	// Convenience methods for removing or clearing the cache completely.

	public void removeAll() {

		for (Iterator i=keys.iterator(); i.hasNext();) {
			CachedImageWrapper ciw=(CachedImageWrapper)cache.get((String)i.next());
			remove(ciw.getSource(),false);
		}
		keys.clear();
	}

	public void remove(Image i) {remove(i,true);}

	public void remove(Image i, boolean removeKey) {

		// Unload the image, if possible.  Then remove the object from the
		// linked list and the hashtable.  Find all of its dependent
		// images, and remove those images recurseively.  Also, find the
		// sources for this image, and remove this image as a dependent.
		
		if (i.isUnloadable()) i.unload();
		String cacheKey=i.getKey();

		CachedImageWrapper ciw=(CachedImageWrapper)cache.remove(cacheKey);
		if (ciw!=null) {
			ArrayList<Image> dependentImages=ciw.getDependentImages();
			for (int loop=0, n=dependentImages.size(); loop<n; loop++) {
				Image dependentImage=dependentImages.get(loop);
				remove(dependentImage);
			}
			ciw.clearDependencies();
			ciw.setSource(null);
		} 
		Image[] sources=i.getSources();
		if (sources!=null) {
			for (int loop=0; loop<sources.length; loop++) {
				Image aSource=sources[loop];
				String sourceKey=aSource.getKey();
				if (keys.contains(sourceKey)) {
					CachedImageWrapper sourceWrapper=(CachedImageWrapper)cache.get(sourceKey);
					sourceWrapper.removeDependentImage(i);
				}
			}
		}
		if (removeKey) keys.remove(cacheKey);
	}

	// =======================================================================

	private void increaseMemoryThreshold() {

		float newThreshold=MEMORY_THRESHOLD+0.05F;
		if (newThreshold>=MAX_MEMORY_THRESHOLD) {
			newThreshold=MAX_MEMORY_THRESHOLD;
			maxMemoryReached=true;
			LOG.error("Memory is at maximum: Increase Java heap size to improve performance");
		}
		ApplicationContext.getContext().setProperty(ApplicationContext.MEMORY_THRESHOLD,new Float(newThreshold));
		LOG.warn("Heap memory exceeds current limit, increased threshold to "+(newThreshold*100)+"%.");
		MEMORY_THRESHOLD=newThreshold;
	}

	// =======================================================================
	// Use a simple first-in, first-out (FIFO) mechanism on the queue to
	// remove an image from the image cache.  Attempt to remove a
	// quarter of the images from the cache each iteration; if a maximum
	// number of iterations is reached, end the removal and give a
	// warning in the log.  When max memory threshold is reached, try
	// emptying the JAI tile cache...

	public boolean removeOldCacheItems() {

		int counter=0;
		int totalRemoved=0;
		while ((isHeapFull())&&(counter<MAX_ITERATIONS)) {
			int n=keys.size();
			int targetIndex=(int)Math.round(n*0.25);
			if (targetIndex<=1) {
				if (!maxMemoryReached) increaseMemoryThreshold(); 
				JAI.getDefaultInstance().getTileCache().flush();
				System.gc();
				return true;
			} else {
				totalRemoved+=targetIndex;
				for (int loop=0; loop<targetIndex; loop++) {
					String key=(String)keys.poll();
					if (key!=null) {
						CachedImageWrapper ciw=(CachedImageWrapper)cache.get(key);
						if (ciw!=null) {
							Image i=ciw.getSource();
							remove(i);
						} else {
							LOG.error("Invalid image cache key: "+key);  
						}
					}
				}
			}
			System.gc();
			counter++;
		}
		if (totalRemoved!=0) LOG.debug("Removed "+totalRemoved+" image(s) from memory by unloading ["+keys.size()+" image(s) cached]");
		return (totalRemoved!=0);
	}

	// =======================================================================

	private class CachedImageWrapper {

		Image source=null;
		ArrayList<Image> dependentImages=new ArrayList<Image>();

		public CachedImageWrapper(Image source) {this.source=source;}

		public Image getSource() {return source;}
		public ArrayList<Image> getDependentImages() {return dependentImages;}

		public void addDependentImage(Image x) {dependentImages.add(x);}
		public void removeDependentImage(Image x) {dependentImages.remove(x);}
		public void clearDependencies() {dependentImages.clear();}
		public void setSource(Image x) {source=x;}

		protected void finalize() throws Throwable {super.finalize(); source=null; dependentImages.clear();}
	}
}


