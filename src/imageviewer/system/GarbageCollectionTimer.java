/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.system;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import imageviewer.ui.ApplicationContext;
import imageviewer.util.ImageCache;

// =======================================================================
// Basic extension of the timerTask class that handles checking the
// state of the heap and executing any garbage collection as needed.
// The thread is timed to execute every 500 ms.

public class GarbageCollectionTimer extends TimerTask {

	private static final Runtime RUNTIME=Runtime.getRuntime();
	private static final float MEGABYTES=1024*1024;

	private static Logger LOG=Logger.getLogger("imageviewer.system");

	// =======================================================================

	private boolean isHeapFull(float memoryThreshold) {

		double freeMemory=RUNTIME.freeMemory();
		double totalMemory=RUNTIME.totalMemory();
		double percentUsed=(totalMemory-freeMemory)/totalMemory;
		return (percentUsed>=memoryThreshold);
	}

	// =======================================================================
	// Skip if this task is too old by checking the scheduledExecutionTime
	// difference with the current time.

	public void run() {

		if (System.currentTimeMillis()-scheduledExecutionTime()>=1500) return; 
		float memoryThreshold=((Float)ApplicationContext.getContext().getProperty(ApplicationContext.MEMORY_THRESHOLD)).floatValue();		
		if (!isHeapFull(memoryThreshold)) return;
		LOG.warn("Heap at or over "+(100*memoryThreshold)+"% of capacity: ["+((RUNTIME.totalMemory()-RUNTIME.freeMemory())/MEGABYTES)+"/"+(RUNTIME.totalMemory()/MEGABYTES)+" MB]");
		System.gc();
		if (!isHeapFull(memoryThreshold)) {
			LOG.info("Memory available after GC: ["+((RUNTIME.totalMemory()-RUNTIME.freeMemory())/MEGABYTES)+"/"+(RUNTIME.totalMemory()/MEGABYTES)+" MB]");
			return;
		}
		boolean removed=ImageCache.getDefaultImageCache().removeOldCacheItems();
		if (removed) LOG.info("Memory now available: ["+((RUNTIME.totalMemory()-RUNTIME.freeMemory())/MEGABYTES)+"/"+(RUNTIME.totalMemory()/MEGABYTES)+" MB]");
	}
}

