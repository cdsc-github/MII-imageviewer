/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.vhd;

import java.util.Comparator;

import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.media.jai.TiledImage;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

import imageviewer.model.Image;
import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;

// =======================================================================

/**
 * Reader class which sorts images based on echo, series, and study information
 * and opens the file data for display on screen.
 *
 * @author Alex Bui
 * @version $Revision: 1.0 $ $Date: 2005/10/30 22:13:34 $
 * @author Brian Burns, Jean Garcia, Kyle Singleton, Jamal Madni, Agatha Lee
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class VhdReader extends ImageReader {

	public static Comparator<VhdImage> DEFAULT_COMPARATOR=new VhdImageComparator();

	//Visible Human Dataset File Extention Types - Not implemented in current setup
	private static final String VHD_MRI_T1_EXT=new String(".t1");
	private static final String VHD_MRI_T2_EXT=new String(".t2");
	private static final String VHD_MRI_PD_EXT=new String(".pd");
	private static final String VHD_CT_FROZEN_EXT=new String(".fro");
	private static final String VHD_CT_NORMAL_EXT=new String(".fre");
	private static final String VHD_FULLCOLOR_EXT=new String(".raw");
	private static final String VHD_UNALIGNED_EXT=new String(".rgb");

	private static final String DEFAULT_HEADER_EXT=new String(".hdr");
	private static final String DEFAULT_IMAGE_EXT=new String(".img");

	String headerExtension=DEFAULT_HEADER_EXT, imageExtension=DEFAULT_IMAGE_EXT;

	public VhdReader() {super();}
	//public VhdReader(String headerExtension, String imageExtension) {super(); this.headerExtension=headerExtension; this.imageExtension=imageExtension;}

	// =======================================================================

    	/**
	 * Reads a directory to find images to open
	 *
	 * @param dir - string for the directory to be read
	 * @param recurse - value indicating if recursion is to be used
	 * @return the image sequence found in the directory (or recursed directories)
	 */
	public ImageSequence readDirectory(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return createSequence(oist);
	}

    	/**
	 * Opens a set of files into an array list of Images.
	 *
	 * @param files - array of selected files to open
	 * @return the array list of images files opened
	 */
	public ArrayList<Image> readImages(String[] files) {

		OpenImageSetTask oist=new OpenImageSetTask(files);
		return oist.openImages();
	}

    	/**
	 * Reads a directory to find images to open
	 *
	 * @param dir - string for the directory to be read
	 * @param recurse - value indicating if recursion is to be used
	 * @return the array list of image files opened
	 */

	public ArrayList<Image> readImages(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return oist.openImages();
	}

    	/**
	 * Builds an image series from a set of images to be opened
	 *
	 * @param oist - set task of images
	 * @return the image sequence of opened images
	 */
	private ImageSequence createSequence(OpenImageSetTask oist) {

		ArrayList<Image> images=oist.openImages();
		return new VhdImageSeries(images);
	}

	// =======================================================================
    	/**
	 * Calls for organization of an sequence of images if they have not been sorted.
	 *
	 * @param unsortedImageSequence - set of unsorted image studies
	 * @return the array list of organized image studies
	 */
	public ArrayList<VhdImageStudy> organizeByStudy(ImageSequence unsortedImageSequence) {return organizeByStudy(unsortedImageSequence.getSequence());}

    	/**
	 * Uses header information to sort images into studies and series.
	 *
	 * @param unsortedImages - an array list of images to be sorted
	 * @return the array list of organized image studies
	 */
	public ArrayList<VhdImageStudy> organizeByStudy(ArrayList<? extends Image> unsortedImages) {

	    int seriesCount = 0;
		ArrayList<VhdImageStudy> studyList=new ArrayList<VhdImageStudy>();
		ArrayList<VhdImageSeries> seriesList=new ArrayList<VhdImageSeries>();
		VhdImage[] allImages=new VhdImage[unsortedImages.size()];
		unsortedImages.toArray(allImages);

		ArrayList<VhdImage[]>exams = separateImages ("Exam Number", allImages);
		    for (int i=0, numExams=exams.size(); i<numExams; i++) {    
			VhdImage[] exam = exams.get(i); 
         		ArrayList<VhdImage[]> series=separateImages("Series Number", exam);                      // separate by exam #, series # -- Jean borrowed pieces from DICOM reader
		      for (int j=0, n=series.size(); j<n; j++) {
			  VhdImage[] seriesImg = series.get(j);
			  ArrayList<VhdImage[]> echos=separateImages("Echo Number", seriesImg);
			  for (int k=0, numEchos=echos.size(); k<numEchos; k++) {
			      VhdImage[] echoImages=echos.get(k);                                                // KYLE - Echo Sort separates T2 and PD images based on a MR header tag
			      List<VhdImage> imageList=Arrays.asList(echoImages);
			      Collections.sort(imageList,DEFAULT_COMPARATOR);
			      imageList.toArray(echoImages);
			      VhdImageSeries ais=new VhdImageSeries(echoImages);
			      seriesList.add(ais);
			      seriesCount++;
			  }
		      }

                      studyList.add(new VhdImageStudy(seriesList));
		    }

		ArrayList<VhdImageStudy> finalList=new ArrayList<VhdImageStudy>();
		for (int loop=studyList.size()-1; loop>=0; loop--) {
			VhdImageStudy vis1=studyList.get(loop);
			finalList.add(vis1);
		}

		LOG.debug("Sorted Vhd images into "+finalList.size()+" studies from "+seriesCount+" series.");
		return finalList;

	}

	// =======================================================================

    	/**
	 * Reads the data for an image.  Generates the rendered 
	 * and tiled images needed to create a series of images for display.
	 *
	 * @param filename - the file to be opened
	 * @return the array list of VHD images opened
	 */
	public ArrayList<VhdImage> readFile(String filename) {

		try {

			ArrayList<VhdImage> al=new ArrayList<VhdImage>();
			VhdHeader ah=new VhdHeader();
			boolean headerFlag=ah.readVhdHeader(filename);

			if (headerFlag) {

				// Sometimes the calibration max isn't set, so guess a value
				// of 4096 as the upper limit if the value given is less than
				// 1. Leave it to the tiled image later to compute the
				// histogram for the max pixel value.

				float calibrationMax=ah.getCalibrationMax();
				int maxPixelValue=(calibrationMax<1) ? 4096 : (int)calibrationMax;
				int imageWidth=ah.getWidth();
				int imageHeight=ah.getHeight();
				int tileWidth=(imageWidth<512) ? imageWidth : ((((imageWidth % 512)==0) ? 512 : ((imageWidth % 2)==0) ? imageWidth/2 : imageWidth));
				int tileHeight=(imageHeight<512) ? imageHeight : ((((imageHeight % 512)==0) ? 512 : ((imageHeight % 2)==0) ? imageHeight/2 : imageHeight));
				int numImages=ah.getNumSlices();

				long filePosition=ah.getHeaderSize();

				for (int loop=0; loop<numImages; loop++) {
					try {
						VhdRenderedImage ari=new VhdRenderedImage(filename,filePosition,ah.getBitsAllocated(),ah.getFileType(),maxPixelValue,
																															imageWidth,imageHeight,tileWidth,tileHeight,ah.getByteOrder());
						TiledImage ti=new TiledImage(ari,tileWidth,tileHeight);
						VhdImage ai=new VhdImage(ti,filename,filePosition);
						if (calibrationMax>=1) ai.setMaxPixelValue(maxPixelValue);
						ai.setVhdHeader(ah);
						ai.setBitDepth(ah.getBitsAllocated());
						al.add(ai);
						ai=null;
						ti=null;
						ari=null;
					} catch (Exception exc) {
						LOG.error("Unable to process Vhd image file: "+filename);
						exc.printStackTrace();
					}
					filePosition+=((imageWidth*imageHeight*ah.getBitsAllocated())/8);
				}
				return al;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================
	// Convenience method to handle the issue of loading/unloading a given
	// image.

    	/**
	 * Reads the data for an image.  Generates the rendered image that
	 * is tiled to fit onto the screen.
	 *
	 * @param ah - header information of the image
	 * @param filename - the name of the file to read from
	 * @param filePosition - position in the file that is being read
	 * @return a tiled image for use in display
	 */
	public static TiledImage readFile(VhdHeader ah, String filename, long filePosition) {

		int imageWidth=ah.getWidth();
		int imageHeight=ah.getHeight();
		float calibrationMax=ah.getCalibrationMax();
		int maxPixelValue=(calibrationMax<1) ? 4096 : (int)calibrationMax;
		int tileWidth=(imageWidth<512) ? imageWidth : ((((imageWidth % 512)==0) ? 512 : ((imageWidth % 2)==0) ? imageWidth/2 : imageWidth));
		int tileHeight=(imageHeight<512) ? imageHeight : ((((imageHeight % 512)==0) ? 512 : ((imageHeight % 2)==0) ? imageHeight/2 : imageHeight));
		VhdRenderedImage ari=new VhdRenderedImage(filename,filePosition,ah.getBitsAllocated(),ah.getFileType(),maxPixelValue,
																											imageWidth,imageHeight,tileWidth,tileHeight,ah.getByteOrder());
		TiledImage ti=new TiledImage(ari,tileWidth,tileHeight);
		ari=null;
		return ti;
	}

	// =======================================================================

    	/**
	 * Uses  values from the image headers to separate images based
	 * on the requested property of the images - echo, series, study
	 *
	 * @param propertyName - property type to use for separation
	 * @param images - array of images to separate
	 * @return an array list of sorted image arrays
	 */
	public ArrayList<VhdImage[]> separateImages(String propertyName, VhdImage[] images) {

	    // Jean modified this function from DICOM reader

		Hashtable categories=new Hashtable();

		for (int loop=0; loop<images.length; loop++) {
			VhdHeader vh=images[loop].getVhdHeader();
			Object propValue = new Object();
			if (propertyName == "Exam Number")
			    propValue=vh.getExamNumber();

			else if (propertyName == "Series Number")
			    propValue = vh.getSeriesNumber();
			
			else if (propertyName == "Echo Number")
			    propValue = vh.getEchoNumber();

			if (categories.containsKey(propValue)) {
				((ArrayList)categories.get(propValue)).add(images[loop]);
			} else {
				ArrayList al=new ArrayList();
				al.add(images[loop]);
				categories.put(propValue,al);
			}
		}

		ArrayList<VhdImage[]> alist=new ArrayList<VhdImage[]>();
		Set valueSet=categories.keySet();
		Iterator iter=valueSet.iterator();

		while (iter.hasNext()) {
			Object key=iter.next();
			ArrayList iAL=(ArrayList)categories.get(key);
			if (iAL!=null) {
				VhdImage[] itmp=new VhdImage[iAL.size()];
				itmp=(VhdImage[])iAL.toArray(itmp); 
				alist.add(itmp);
			}
		}
		return (alist);

	}

	// =======================================================================

	public static class VhdImageComparator implements Comparator<VhdImage> {

		/**
		 * Compare two VhdImage objects.  Sort by filePosition.
		 *
		 * @param obj1
		 * @param obj2
		 * @return
		 */
		public int compare(VhdImage obj1, VhdImage obj2) {

			VhdImage img1=(VhdImage)obj1;
			VhdImage img2=(VhdImage)obj2;

			long filePosition1=img1.getFilePosition();
			long filePosition2=img2.getFilePosition();
			long diff=(filePosition1-filePosition2);
			return (diff>0) ? 1 : (diff<0) ? -1 : 0;
		}
	}

	// =======================================================================

	public static void main(String args[]) {

		if (args.length==1) {
			VhdReader ar=new VhdReader();
			ImageSequence is=ar.readDirectory(args[0],true);
			System.err.println("Read "+is.size()+" Vhd images from specified directory.");
		}
	}
}
