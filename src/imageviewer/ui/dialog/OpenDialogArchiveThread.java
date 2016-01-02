/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import imageserver.model.ImageInstance;
import imageserver.model.Series;

import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;

import imageviewer.system.ImageReaderManager;
import imageviewer.system.ImageViewerClientNode;

import imageviewer.ui.ApplicationPanel;

// =======================================================================

public class OpenDialogArchiveThread extends Thread {

	boolean applyLayoutRules=false;
	TreeSet<Series> seriesList=null;

	public OpenDialogArchiveThread(TreeSet<Series> seriesList, boolean applyLayoutRules) {this.seriesList=seriesList; this.applyLayoutRules=applyLayoutRules;}

	// =======================================================================

	private String[] computeFilePaths(Series s) {

		ArrayList<String> fileList=new ArrayList<String>();
		try {
			ImageViewerClientNode.getInstance().beginTransaction();
			ImageViewerClientNode.getInstance().load(s,s.getId());
			s.load();
			Set<ImageInstance> images=s.getImageInstances();
			Iterator i=images.iterator();
			while (i.hasNext()) {
				ImageInstance ii=(ImageInstance)i.next();
				fileList.add(ii.getFilePath());
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		} finally {
			ImageViewerClientNode.getInstance().commitTransaction();
		}
		String[] fileListArray=new String[fileList.size()];
		fileList.toArray(fileListArray);
		return fileListArray;
	}

	// =======================================================================

	public void run() {

		if (!applyLayoutRules) {
			Iterator i=seriesList.iterator();
			int listSize=seriesList.size(), counter=1;
			while (i.hasNext()) {
				Series s=(Series)i.next();
				String imageType=s.getImageType();
				String[] fileSet=computeFilePaths(s);
				ImageReader ir=ImageReaderManager.getInstance().getImageReader(imageType);
				ApplicationPanel.getInstance().addImages(ir.organizeByStudy(ir.readImages(fileSet)));
				ApplicationPanel.getInstance().addStatusMessage("Opened "+counter+"/"+listSize+" series...");
				counter++;
			}
			ApplicationPanel.getInstance().addStatusMessage("Completed opening of requested images",5000);
		} else { 
				
			// User wants to apply layouts per study grouping, so we need to
			// wait until each "group" is read in before applying layout logic.
			
		}
	}
}

