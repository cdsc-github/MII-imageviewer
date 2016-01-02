/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.io.File;

import java.util.ArrayList;

import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;

import imageviewer.system.ImageReaderManager;
import imageviewer.system.ImageViewerClientNode;

import imageviewer.ui.ApplicationPanel;

// =======================================================================

public class OpenDialogFileThread extends Thread {

	boolean applyLayoutRules=false, recurse=false;
	File[] selectedFiles=null;
	String readAsType=null;
	ArrayList<Integer> paramList=null;

	public OpenDialogFileThread(File[] selectedFiles, String readAsType, boolean recurse, boolean applyLayoutRules, ArrayList<Integer> paramList) {

		this.selectedFiles=selectedFiles;
		this.readAsType=readAsType;
		this.recurse=recurse;
		this.applyLayoutRules=applyLayoutRules;
		this.paramList=paramList;
	}

	// =======================================================================

	private void addFiles(ArrayList<String> filePaths, File f) {

		if (f.isDirectory()) {
			File[] filenames=f.listFiles();
			for (int loop=0; loop<filenames.length; loop++) {
				File subFile=filenames[loop];
				if ((subFile.isDirectory())&&(recurse)) addFiles(filePaths,subFile); else if (subFile.isFile()) filePaths.add(subFile.getAbsolutePath());
			}
		} else {
			filePaths.add(f.getAbsolutePath());
		}
	}

	public void run() {

		if (!applyLayoutRules) {
			ImageReader ir=ImageReaderManager.getInstance().getImageReader(readAsType);
			ir.setParameters(paramList);
			if (ir==null) {
				ApplicationPanel.getInstance().addStatusMessage("Error in configuration, could not find appropriate image reader");
				return;
			}
			for (int loop=0; loop<selectedFiles.length; loop++) {
				File f=selectedFiles[loop];
				ArrayList<String> filePaths=new ArrayList<String>();
				if (f.isDirectory()) addFiles(filePaths,f); else if (f.isFile()) filePaths.add(f.getAbsolutePath());
				if (!filePaths.isEmpty()) {
					String[] fileSet=new String[filePaths.size()];
					filePaths.toArray(fileSet);
					ApplicationPanel.getInstance().addImages(ir.organizeByStudy(ir.readImages(fileSet)));
					ApplicationPanel.getInstance().addStatusMessage("Opened "+f.getAbsolutePath(),5000);
				}
			}
		} else { 
				
			// User wants to apply layouts per study grouping, so we need to
			// wait until each "group" is read in before applying layout logic.
			
		}
	}
}

