/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.analyze;

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

public class AnalyzeReader extends ImageReader {

	public static Comparator<AnalyzeImage> DEFAULT_COMPARATOR=new AnalyzeImageComparator();

	private static final String DEFAULT_HEADER_EXT=new String(".hdr");
	private static final String DEFAULT_IMAGE_EXT=new String(".img");

	String headerExtension=DEFAULT_HEADER_EXT, imageExtension=DEFAULT_IMAGE_EXT;

	public AnalyzeReader() {super();}
	public AnalyzeReader(String headerExtension, String imageExtension) {super(); this.headerExtension=headerExtension; this.imageExtension=imageExtension;}

	// =======================================================================

	public ImageSequence readDirectory(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return createSequence(oist);
	}

	public ArrayList<Image> readImages(String[] files) {

		OpenImageSetTask oist=new OpenImageSetTask(files);
		return oist.openImages(imageExtension);
	}

	public ArrayList<Image> readImages(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return oist.openImages(imageExtension);
	}

	private ImageSequence createSequence(OpenImageSetTask oist) {

		ArrayList<Image> images=oist.openImages(imageExtension);
		return new AnalyzeImageSeries(images);
	}

	// =======================================================================

	public ArrayList<AnalyzeImageStudy> organizeByStudy(ImageSequence unsortedImageSequence) {return organizeByStudy(unsortedImageSequence.getSequence());}

	public ArrayList<AnalyzeImageStudy> organizeByStudy(ArrayList<? extends Image> unsortedImages) {

		ArrayList<AnalyzeImageStudy> studyList=new ArrayList<AnalyzeImageStudy>();
		ArrayList<AnalyzeImageSeries> seriesList=new ArrayList<AnalyzeImageSeries>();
		AnalyzeImage[] allImages=new AnalyzeImage[unsortedImages.size()];
		unsortedImages.toArray(allImages);

		ArrayList<AnalyzeImage[]> series=separateImages(allImages);                      // Separate by file
		for (int i=0, n=series.size(); i<n; i++) {
			AnalyzeImage[] seriesImages=series.get(i);
			List<AnalyzeImage> imageList=Arrays.asList(seriesImages);
			Collections.sort(imageList,DEFAULT_COMPARATOR);
			imageList.toArray(seriesImages);
			AnalyzeImageSeries ais=new AnalyzeImageSeries(seriesImages);
			seriesList.add(ais);
		}
		LOG.info("Sorted Analyze images into "+series.size()+" series.");
		studyList.add(new AnalyzeImageStudy(seriesList));
		return studyList;
	}

	// =======================================================================

	public ArrayList<AnalyzeImage> readFile(String filename) {
		
		try {

			ArrayList<AnalyzeImage> al=new ArrayList<AnalyzeImage>();
			AnalyzeHeader ah=new AnalyzeHeader();
			boolean headerFlag=ah.readAnalyzeHeader(filename.replace(imageExtension,headerExtension));

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

				long filePosition=0;

				for (int loop=0; loop<numImages; loop++) {
					try {
						AnalyzeRenderedImage ari=new AnalyzeRenderedImage(filename,filePosition,ah.getBitsAllocated(),ah.getFileType(),maxPixelValue,
																															imageWidth,imageHeight,tileWidth,tileHeight,ah.getByteOrder());
						TiledImage ti=new TiledImage(ari,tileWidth,tileHeight);					
						AnalyzeImage ai=new AnalyzeImage(ti,filename,filePosition);
						if (calibrationMax>=1) ai.setMaxPixelValue(maxPixelValue);
						ai.setAnalyzeHeader(ah);
						ai.setBitDepth(ah.getBitsAllocated()); 
						al.add(ai);
						ai=null;
						ti=null;
						ari=null;
					} catch (Exception exc) {
						LOG.error("Unable to process Analyze image file: "+filename);
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

	public static TiledImage readFile(AnalyzeHeader ah, String filename, long filePosition) {

		int imageWidth=ah.getWidth();
		int imageHeight=ah.getHeight();
		float calibrationMax=ah.getCalibrationMax();
		int maxPixelValue=(calibrationMax<1) ? 4096 : (int)calibrationMax;
		int tileWidth=(imageWidth<512) ? imageWidth : ((((imageWidth % 512)==0) ? 512 : ((imageWidth % 2)==0) ? imageWidth/2 : imageWidth));
		int tileHeight=(imageHeight<512) ? imageHeight : ((((imageHeight % 512)==0) ? 512 : ((imageHeight % 2)==0) ? imageHeight/2 : imageHeight));
		AnalyzeRenderedImage ari=new AnalyzeRenderedImage(filename,filePosition,ah.getBitsAllocated(),ah.getFileType(),maxPixelValue,
																											imageWidth,imageHeight,tileWidth,tileHeight,ah.getByteOrder());
		TiledImage ti=new TiledImage(ari,tileWidth,tileHeight);
		ari=null;
		return ti;
	}

	// =======================================================================

	public ArrayList<AnalyzeImage[]> separateImages(AnalyzeImage[] images) {

		Hashtable categories=new Hashtable();

		for (int loop=0; loop<images.length; loop++) {
			AnalyzeHeader ah=images[loop].getAnalyzeHeader();
			String filename=ah.getFilename();
			if (categories.containsKey(filename)) {
				((ArrayList)categories.get(filename)).add(images[loop]);
			} else {
				ArrayList al=new ArrayList();
				al.add(images[loop]);
				categories.put(filename,al);
			}
		}

		ArrayList<AnalyzeImage[]> alist=new ArrayList<AnalyzeImage[]>();
		Set valueSet=categories.keySet();
		Iterator iter=valueSet.iterator();

		while (iter.hasNext()) {
			Object key=iter.next();
			ArrayList iAL=(ArrayList)categories.get(key);
			if (iAL!=null) {
				AnalyzeImage[] itmp=new AnalyzeImage[iAL.size()];
				itmp=(AnalyzeImage[])iAL.toArray(itmp); 
				alist.add(itmp);
			}
		}
		return (alist);
	}

	// =======================================================================

	public static class AnalyzeImageComparator implements Comparator<AnalyzeImage> {

		/**
		 * Compare two AnalyzeImage objects.  Sort by filePosition.
		 * 
		 * @param obj1
		 * @param obj2
		 * @return
		 */
		public int compare(AnalyzeImage obj1, AnalyzeImage obj2) {

			AnalyzeImage img1=(AnalyzeImage)obj1;
			AnalyzeImage img2=(AnalyzeImage)obj2;
		
			long filePosition1=img1.getFilePosition();
			long filePosition2=img2.getFilePosition();
			long diff=(filePosition1-filePosition2);
			return (diff>0) ? 1 : (diff<0) ? -1 : 0;
		}
	}

	// =======================================================================
	
	public static void main(String args[]) {

		if (args.length==1) {
			AnalyzeReader ar=new AnalyzeReader();
			ImageSequence is=ar.readDirectory(args[0],true);
			System.err.println("Read "+is.size()+" Analyze images from specified directory.");
		}
	}
}
