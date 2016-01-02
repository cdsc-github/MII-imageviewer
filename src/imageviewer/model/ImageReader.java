/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.io.File;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Observable;
import java.util.Set;

import org.apache.log4j.Logger;

import imageviewer.tools.AnnotationReader;
import imageviewer.tools.AnnotationReaderManager;

/**
 * Abstract class for ImageReader implementations.  Provides generic image opening support.  
 */
public abstract class ImageReader {

	protected static Logger LOG=Logger.getLogger("imageviewer.reader");
	public static final int MINIMUM_FILE_SIZE=128;

	// =======================================================================

	/**
	 * Default constructor. 
	 */
	public ImageReader() {}

	// =======================================================================

	/**
	 * Implement reading of a directory.
	 * 
	 * @param dir
	 * @param recurse
	 * @return
	 */
	public abstract ImageSequence readDirectory(String dir, boolean recurse);

	public abstract ArrayList<? extends Image> readImages(String[] files);

	/**
	 * Implement reading of a file. 
	 * 
	 * @param filename
	 * @return
	 */
	public abstract ArrayList<? extends Image> readFile(String filename);

	public abstract ArrayList<? extends ImageSequenceGroup> organizeByStudy(ImageSequence is);
	public abstract ArrayList<? extends ImageSequenceGroup> organizeByStudy(ArrayList<? extends Image> unsortedImages);

	public abstract ArrayList<? extends Image> readImages(String dir, boolean recurse);

	// =======================================================================

	public void setParameters(ArrayList<Integer> paramList) {}

	// =======================================================================

	/**
	 * Encapsulates the opening of a set of images, providing Observer
	 * pattern support, and notifying when images are opened with the
	 * number of images opened.
	 */
	public class OpenImageSetTask extends Observable {

		ArrayList<File> targetFiles=new ArrayList<File>();
		boolean recurse=true;
		int minimumFileSize=MINIMUM_FILE_SIZE;

		/**
		 * Open directory.
		 * 
		 * @param dirPath
		 * @param recurse
		 */
		public OpenImageSetTask(String dirPath, boolean recurse) {this.recurse=recurse; getFiles(new File(dirPath),targetFiles);}
		
		/**
		 * Open directory but skip files of insufficient size.
		 * 
		 * @param dirPath
		 * @param recurse
		 * @param minimumFileSize
		 */
		public OpenImageSetTask(String dirPath, boolean recurse, int minimumFileSize) {

			this.recurse=recurse; 
			this.minimumFileSize=minimumFileSize;
			getFiles(new File(dirPath),targetFiles);
		}

		public OpenImageSetTask(String[] files) {

			recurse=false;
			for (int loop=0; loop<files.length; loop++) {
				File f=new File(files[loop]);
				if ((f.exists())&&(f.isFile())) targetFiles.add(f);
			}
		}

		// =======================================================================

		/**
		 * Indicate number of files to open.
		 * 
		 * @return
		 */
		public int getTaskLength() {return ((targetFiles==null) ? 0 : targetFiles.size());}

		// =======================================================================

		/**
		 * Open all images. 
		 * 
		 * @return
		 */
		public ArrayList<Image> openImages() {return openImages(targetFiles,null);}

		/**
		 * Open all images with a given extension. 
		 * 
		 * @return
		 */
		public ArrayList<Image> openImages(String extension) {return openImages(targetFiles,extension);}

		/**
		 * Open a given set of images.
		 * 
		 * @param files
		 * @return
		 */
		protected ArrayList<Image> openImages(ArrayList<File> files, String extension) {

			ArrayList<Image> images=new ArrayList<Image>();
			HashMap<File,AnnotationReader> annotationFiles=new HashMap<File,AnnotationReader>();
			AnnotationReaderManager arm=AnnotationReaderManager.getInstance();
			for (int loop=0, n=files.size(); loop<n; loop++) {
				File aFile=files.get(loop);
				if ((extension!=null)&&(!aFile.getName().contains(extension))) {
					AnnotationReader ar=arm.findReader(aFile);
					if (ar!=null) annotationFiles.put(aFile,ar); else	continue;
				} else {
													 
					// Skip files that are not of sufficient size...

					if (aFile.length()>minimumFileSize) {
						try {
							ArrayList<? extends Image> readImages=readFile(aFile.getAbsolutePath());
							if (readImages!=null) {
								images.addAll(readImages);
								setChanged();
								notifyObservers(new Integer(loop+1));
							} else {
								AnnotationReader ar=arm.findReader(aFile);
								if (ar!=null) annotationFiles.put(aFile,ar);
							}
						} catch (Exception exc) {
							LOG.error("Unable to read image ("+aFile.getAbsolutePath()+"), skipping...");
							exc.printStackTrace();
						}
					}
				}
			}
			Set s=annotationFiles.keySet();
			if (!s.isEmpty()) {
				Iterator i=s.iterator();
				for (int loop=0; i.hasNext(); loop++) {
					File f=(File)i.next();
					AnnotationReader ar=annotationFiles.get(f);
					ar.readFile(f,images);
				}
			}
			return images;
		}

		// =======================================================================

		private void getFiles(File f, ArrayList<File> holder) {

			// Handle the case difference between a single file, or a
			// directory containing multiple files. The problem is handled
			// recursively, as a sub-directory may be contained wihtin.

			if (f.isFile()) {
				holder.add(f);		
			} else if (recurse) {
				File[] filenames=f.listFiles();
				for (int loop=0; loop<filenames.length; loop++) {
					File aFile=filenames[loop];
					getFiles(aFile,holder);
				}
			}
		}
	}

}
