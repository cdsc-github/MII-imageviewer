/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom;

import static imageviewer.model.dicom.DICOMHeader.IMAGE_ECHO_NUMBER;
import static imageviewer.model.dicom.DICOMHeader.PATIENT_ID;
import static imageviewer.model.dicom.DICOMHeader.SERIES_DESCRIPTION;
import static imageviewer.model.dicom.DICOMHeader.SERIES_INSTANCE_UID;
import static imageviewer.model.dicom.DICOMHeader.SERIES_NUMBER;
import static imageviewer.model.dicom.DICOMHeader.STUDY_DATE;
import static imageviewer.model.dicom.DICOMHeader.STUDY_DESCRIPTION;

import imageviewer.model.Image;
import imageviewer.model.ImageProperties;
import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;

import java.io.FileInputStream;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.media.jai.TiledImage;

import com.sun.media.imageio.stream.FileChannelImageInputStream;

// =======================================================================
/**
 * An ImageReader implementation for DICOM objects.  
 */
public class DICOMReader extends ImageReader {

	public static Comparator<DICOMImage> DEFAULT_COMPARATOR=new DICOMImageComparator();
	public static Comparator<DICOMImage> DEFAULT_IMAGE_COMPARATOR=new DICOMSeriesNumInstanceNumComparator();
	public static Comparator<DICOMImageSeries> DEFAULT_SERIES_COMPARATOR=new DICOMSeriesDateComparator();
	public static Comparator<DICOMImageStudy> DEFAULT_STUDY_COMPARATOR=new DICOMStudyDateComparator();

	public DICOMReader() {super();}

	// =======================================================================

	/**
	 * Organizes the images by series and timestamps, then recollapse
	 * the images into a single sequence.
	 * 
	 * @see imageviewer.model.ImageReader#readDirectory(java.lang.String, boolean)
	 */
	public ImageSequence readDirectory(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return createSequence(oist);
	}

	// =======================================================================

	public ArrayList<Image>readImages(String[] files) {

		OpenImageSetTask oist=new OpenImageSetTask(files);
		return oist.openImages();
	}

	// =======================================================================

	private ImageSequence createSequence(OpenImageSetTask oist) {

		ArrayList<Image> unsortedImages=oist.openImages();
		DICOMImage[] images=new DICOMImage[unsortedImages.size()];
		unsortedImages.toArray(images);
		sortImages(images,DEFAULT_COMPARATOR);
		return new DICOMImageSeries(images);
	}

	// =======================================================================

	public ArrayList<Image> readImages(String dir, boolean recurse) {

		OpenImageSetTask oist=new OpenImageSetTask(dir,recurse);
		return oist.openImages();
	}

	// =======================================================================
	/**
	 *	This utility method has the responsibility of returning an
	 *	arraylist of separated images, recursing through a given
	 *	directory to find DICOM images.  The images are separated by
	 *	date, study description (UID doesn't work for some unknown
	 *	reason), then series (first by series UID, then by series
	 * 	descriptor), and finally sorted by image number. This new class
	 *	is based on a refactor of methods contained within the set and
	 * 	image classes, repsectively, relocating their static methods.
	 *  
	 * @param unsortedImageSequence
	 * @return
	 */
	public ArrayList<DICOMImageStudy> organizeByStudy(ArrayList<? extends Image> unsortedImages) {

		ArrayList<DICOMImageStudy> studyList=new ArrayList<DICOMImageStudy>();
		DICOMImage[] allImages=new DICOMImage[unsortedImages.size()];
		unsortedImages.toArray(allImages);

		int seriesCount=0;

		ArrayList<DICOMImage[]> studiesByPatient=separateImages(PATIENT_ID,allImages);                      // Separate by patient ID
		for (int h=0, numPatients=studiesByPatient.size(); h<numPatients; h++) {
			DICOMImage[] studyByPatient=studiesByPatient.get(h);
			ArrayList<DICOMImage[]> studiesByDate=separateImages(STUDY_DATE,studyByPatient);                  // Separate by study date
			for (int i=0, numStudies1=studiesByDate.size(); i<numStudies1; i++) {                   
				DICOMImage[] studyByDate=studiesByDate.get(i);
				ArrayList<DICOMImage[]> studiesByDesc=separateImages(STUDY_DESCRIPTION,studyByDate);            // Separate by study description
				for (int j=0, numStudies2=studiesByDesc.size(); j<numStudies2; j++) {
					DICOMImage[] studyByDesc=studiesByDesc.get(j);
					ArrayList<DICOMImageSeries> seriesList=new ArrayList<DICOMImageSeries>();
					ArrayList<DICOMImage[]> seriesByUID=separateImages(SERIES_INSTANCE_UID,studyByDesc);          // Separate by series instance UID
					for (int k=0, numSeries1=seriesByUID.size(); k<numSeries1; k++) {
						DICOMImage[] UIDSeries=seriesByUID.get(k);
						ArrayList<DICOMImage[]> seriesByDesc=separateImages(SERIES_DESCRIPTION,UIDSeries);          // Separate by series description
						for (int l=0, numSeries2=seriesByDesc.size(); l<numSeries2; l++) {
							DICOMImage[] descSeries=(DICOMImage[])seriesByDesc.get(l);
							ArrayList<DICOMImage[]> seriesByNumber=separateImages(SERIES_NUMBER,descSeries);          // Separate by series number
							for (int m=0, numSeries3=seriesByNumber.size(); m<numSeries3; m++) {
								DICOMImage[] echoNumber=seriesByNumber.get(m);
								ArrayList<DICOMImage[]> seriesBySequenceType=separateImages(IMAGE_ECHO_NUMBER,echoNumber);   // Separate by series echo type (for MR)
								for (int n=0, numSeries4=seriesBySequenceType.size(); n<numSeries4; n++) {
									DICOMImage[] series=seriesBySequenceType.get(n);
									sortImages(series,DEFAULT_IMAGE_COMPARATOR);
									DICOMImageSeries dis=new DICOMImageSeries(series);
									seriesList.add(dis);
									seriesCount++;
								}
							}
						}
					}
					
					// Sort the series temporally, from oldest to newest.  Also,
					// compare the image numbers if it's a funny series that's
					// split.  Haha!
					
					Collections.sort(seriesList,DEFAULT_SERIES_COMPARATOR);
					studyList.add(new DICOMImageStudy(seriesList));
				}
			}
		}
			
		// Sort the studies temporally, from oldest to newest. Because
		// some stupid DICOM PACS don't give study descriptions
		// consistently, we try and collapse sequential studies by a given
		// patient ID if the immediate prior is on the same date and has
		// no study description.  The description is then driven by the
		// first study that *does* have information associated with it.
			
		Collections.sort(studyList,DEFAULT_STUDY_COMPARATOR);
		ArrayList<DICOMImageStudy> finalList=new ArrayList<DICOMImageStudy>();
		for (int loop=studyList.size()-1; loop>=0; loop--) {
			DICOMImageStudy dis1=studyList.get(loop);
			if (loop!=0) {
				DICOMImageStudy dis2=studyList.get(loop-1);
				if (dis2.getStudyDescription()==null) {
					dis1.combine(dis2);
					loop--;
				}
			}
			finalList.add(dis1);
		}
		Collections.reverse(finalList);
		LOG.debug("Sorted DICOM images into "+finalList.size()+" studies from "+seriesCount+" series.");
		return finalList;
	}

	public ArrayList<DICOMImageStudy> organizeByStudy(ImageSequence unsortedImageSequence) {return organizeByStudy(unsortedImageSequence.getSequence());}

	// =======================================================================
	/**
	 * Provide a String representation of this list of studies.
	 * 
	 * @param studyList
	 * @return
	 */
	public String toString(ArrayList<DICOMImageStudy> studyList) {

		StringBuffer sb=new StringBuffer();
		for (int i=0, numStudy=studyList.size(); i<numStudy; i++) {
			DICOMImageStudy study=studyList.get(i);
			String studyDescription=study.getStudyDescription();
			if (studyDescription==null) studyDescription=new String("[no study description]");
			for (int k=0, wordLen=(26-studyDescription.length()); k<wordLen; k++) studyDescription+=" ";
			sb.append("|-Study "+(i+1)+": "+study.getTimestamp()+"\n");
			sb.append("| "+study.size()+" series, "+studyDescription+"\n");
			for (int j=0, numSeries=study.size(); j<numSeries; j++) {
				DICOMImageSeries dis=(DICOMImageSeries)study.get(j);
				String description=dis.getSeriesDescription();
				if (description==null) description=new String("[no series description]");
				for (int k=0, wordLen=(26-description.length()); k<wordLen; k++) description+=" ";
				String size=new String(""+dis.size());
				for (int k=0, wordLen=(3-size.length()); k<wordLen; k++) size=" "+size;
				sb.append("| |-"+dis.getFilePath()+"\n");
				sb.append("| | "+dis.getSeriesImageModality()+" "+description+" "+size+" image(s) ("+dis.getTimestamp()+")\n");
			}
			sb.append("|------------------------------------------------------------------------------\n");
		}
		return sb.toString();
	}

	// =======================================================================

	/**
	 * Convenience method for sorting a set of images with a given comparator.
	 * 
	 * @param images
	 * @param comp
	 */
	public void sortImages(DICOMImage[] images, Comparator<DICOMImage> comp) {

		List<DICOMImage> imageList=Arrays.asList(images);
		Collections.sort(imageList,comp);
		imageList.toArray(images);
	}

	// =======================================================================
	/**
	 * Categorizes the images by the given property.
	 *
	 * @param propertyName  provides name of property to categorize images by
	 * @param images  an array of <code>DICOMImage</code> which contains images to categorize
	 * @return an arraylist containing sorted images
	 */
	public ArrayList<DICOMImage[]> separateImages(String propertyName, DICOMImage[] images) {

		Hashtable categories=new Hashtable();

		for (int loop=0; loop<images.length; loop++) {
			DICOMHeader dh=images[loop].getDICOMHeader();
			Object propValue=dh.doLookup(propertyName);
			if (propValue==null) propValue=new String("__"+propertyName);
			if (categories.containsKey(propValue)) {
				((ArrayList)categories.get(propValue)).add(images[loop]);
			} else {
				ArrayList al=new ArrayList();
				al.add(images[loop]);
				categories.put(propValue,al);
			}
		}

		ArrayList<DICOMImage[]> alist=new ArrayList<DICOMImage[]>();
		Set valueSet=categories.keySet();
		Iterator iter=valueSet.iterator();

		while (iter.hasNext()) {
			Object key=iter.next();
			ArrayList iAL=(ArrayList)categories.get(key);
			if (iAL!=null) {
				DICOMImage[] itmp=new DICOMImage[iAL.size()];
				itmp=(DICOMImage[])iAL.toArray(itmp); 
				alist.add(itmp);
			}
		}
		return (alist);
	}

	// =======================================================================
	/**
	 * 	Return one or more images; because some files have weirdness and
	 * 	have multiple frames embedded within a single file, need to pass
	 * 	back an array.
	 * 
	 * @see imageviewer.model.ImageReader#readFile(java.lang.String)
	 */
	public ArrayList<DICOMImage> readFile(String filename) {

		try {

			FileChannel fc=new FileInputStream(filename).getChannel();
			if (fc.size()==0) return null;
			FileChannelImageInputStream fciis=new FileChannelImageInputStream(fc);
			fciis.setByteOrder(ByteOrder.LITTLE_ENDIAN);
			DICOMHeader dh=new DICOMHeader();
			dh.setProperty(ImageProperties.FILE_LOCATION,filename);
			boolean validImage=dh.readDICOMHeader(fciis);

			if (validImage) {

				int imageWidth=dh.getImageColumns();
				int imageHeight=dh.getImageRows();
				int bitsAllocated=dh.getImageBitsAllocated();
				int bitsStored=dh.getImageBitsStored();
				int minBitsAllocated=Math.min(bitsStored,16);
				int maxPixelValue=(int)Math.pow(2,minBitsAllocated)-1;
				int numFrames=dh.getNumberFrames();
				int numBands=dh.getImageSamplePerPixel();

				// A bit of a sanity check, as not all CT images seem to be
				// properly set with the number of bits...

				if (dh.getSeriesImageModality().compareToIgnoreCase("CT")==0) maxPixelValue=4096;

				ArrayList<DICOMImage> images=new ArrayList<DICOMImage>();
				long filePosition=fciis.getStreamPosition();

				for (int loop=0; loop<numFrames; loop++) {
					int tileWidth=(imageWidth<=512) ? imageWidth : ((((imageWidth % 512)==0) ? 512 : ((imageWidth % 2)==0) ? imageWidth/2 : imageWidth));
					int tileHeight=(imageHeight<=512) ? imageHeight : ((((imageHeight % 512)==0) ? 512 : ((imageHeight % 2)==0) ? imageHeight/2 : imageHeight));
					DICOMRenderedImage dri=new DICOMRenderedImage(filename,filePosition,bitsAllocated,bitsStored,imageWidth,imageHeight,tileWidth,tileHeight,numBands);
					TiledImage ti=new TiledImage(dri,tileWidth,tileHeight);
					DICOMImage di=new DICOMImage(ti,filename,filePosition);
					di.setDICOMHeader(dh);
					di.setBitDepth(minBitsAllocated);
					di.setMaxPixelValue(maxPixelValue);
					di.addDataLayer(new DICOMHeaderDataLayer(dh));                  // Add header information as a data layer
					images.add(di);
					di=null;
					ti=null;
					dri=null;
					filePosition+=(imageWidth*imageHeight*numBands*bitsStored/8);
				}
				fciis.close();
				return images;
			} else {
				LOG.debug("Unable to process file as DICOM image:");
				LOG.debug(filename);
				fciis.close();
				return null;
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================
	// 
	/**
	 * Convenience method to handle the issue of loading/unloading a given DICOM image.
	 * 
	 * @param dh
	 * @param filename
	 * @param filePosition
	 * @return
	 */
	public static TiledImage readFile(DICOMHeader dh, String filename, long filePosition) {

		int imageWidth=dh.getImageColumns();
		int imageHeight=dh.getImageRows();
		int bitsAllocated=dh.getImageBitsAllocated();
		int bitsStored=dh.getImageBitsStored();
		int tileWidth=(imageWidth<=512) ? imageWidth : ((((imageWidth % 512)==0) ? 512 : ((imageWidth % 2)==0) ? imageWidth/2 : imageWidth));
		int tileHeight=(imageHeight<=512) ? imageHeight : ((((imageHeight % 512)==0) ? 512 : ((imageHeight % 2)==0) ? imageHeight/2 : imageHeight));
		int numBands=dh.getImageSamplePerPixel();
		DICOMRenderedImage dri=new DICOMRenderedImage(filename,filePosition,bitsAllocated,bitsStored,imageWidth,imageHeight,tileWidth,tileHeight,numBands);
		TiledImage ti=new TiledImage(dri,tileWidth,tileHeight);
		dri=null;
		return ti;
	}

	// =======================================================================
	/**
	 * Implements comparison between two DICOM images and compares their
	 * series and instance numbers. If series numbers are different,
	 * returns their difference. If they are the same, the comparator
	 * looks at the instance numbers and returns their difference. If
	 * instance numbers are the same, returns 1 if image has multiple
	 * frames. Otherwise, returns 0 meaning images are identical.
	 *
	 * @param obj1  first DICOM object to be compared
	 * @param obj2  second DICOM object to be compared
	 * @return integer values specified in the description
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	public static class DICOMSeriesNumInstanceNumComparator implements Comparator<DICOMImage> {

		public int compare(DICOMImage obj1, DICOMImage obj2) {

			DICOMImage img1=(DICOMImage)obj1;
			DICOMImage img2=(DICOMImage)obj2;
			int seriesNum1=img1.getDICOMHeader().getSeriesNumber();
			int seriesNum2=img2.getDICOMHeader().getSeriesNumber();
			int diff=seriesNum1-seriesNum2;
			if (diff!=0) return diff;

			String imageInstance1=img1.getDICOMHeader().getImageInstance();
			String imageInstance2=img2.getDICOMHeader().getImageInstance();
			if ((imageInstance1!=null)&&(imageInstance2!=null)) {
				int imgNum1=Integer.parseInt(imageInstance1.trim());
				int imgNum2=Integer.parseInt(imageInstance2.trim());
				diff=imgNum1-imgNum2;
				if (diff!=0) return diff;
			}

			diff=img1.getDICOMHeader().getSOPInstanceUID().compareTo(img2.getDICOMHeader().getSOPInstanceUID());
			if ((diff==0)&&(img1.getDICOMHeader().getNumberFrames()>1)) return 1;
			return diff;
		}
	}

	// =======================================================================

	/**
	 * Compares by the timestamp of the DICOMImageSeries.
	 */
	public static class DICOMSeriesDateComparator implements Comparator<DICOMImageSeries> {

		/**
		 * Compares by the timestamp of the DICOMImageSeries.
		 * 
		 * @param obj1
		 * @param obj2
		 * @return
		 */
		public int compare(DICOMImageSeries obj1, DICOMImageSeries obj2) {

			DICOMImageSeries dis1=(DICOMImageSeries)obj1;
			DICOMImageSeries dis2=(DICOMImageSeries)obj2;
			Date timestamp1=dis1.getTimestamp();
			Date timestamp2=dis2.getTimestamp();
			if ((timestamp1==null) || (timestamp2==null)) return -1;
			int delta=timestamp1.compareTo(timestamp2);
			if (delta!=0) return delta;
			
			DICOMImage di1=(DICOMImage)dis1.getImage(0);
			DICOMImage di2=(DICOMImage)dis2.getImage(0);
			if ((di1!=null)&&(di2!=null)) {
				int imageNumber1=Integer.parseInt(di1.getDICOMHeader().getImageInstance());
				int imageNumber2=Integer.parseInt(di2.getDICOMHeader().getImageInstance());
				return (imageNumber1-imageNumber2);
			}
			return 0;
		}
	}

	// =======================================================================
	/**
	 * Compare the timestamps in each DICOMImageStudy.
	 */
	public static class DICOMStudyDateComparator implements Comparator<DICOMImageStudy> {

		/**
		 * Compare the timestamps in each DICOMImageStudy.
		 * In the case that two studies have the same timestamp, compare 
		 * the first series of each study.
		 * 
		 * @param obj1
		 * @param obj2
		 * @return
		 */
		public int compare(DICOMImageStudy obj1, DICOMImageStudy obj2) {

			DICOMImageStudy dis1=(DICOMImageStudy)obj1;
			DICOMImageStudy dis2=(DICOMImageStudy)obj2;
			Date timestamp1=dis1.getTimestamp();
			Date timestamp2=dis2.getTimestamp();
			if ((timestamp1==null) || (timestamp2==null)) return -1;
			int diff=timestamp1.compareTo(timestamp2);
			if (diff!=0) return diff;

			// In the case that two studies have the same timestamp, compare
			// the first series of each study.
			
			DICOMImageSeries series1=(DICOMImageSeries)dis1.getSeries().get(0);
			DICOMImageSeries series2=(DICOMImageSeries)dis2.getSeries().get(0);
			if ((series1!=null)&&(series2!=null))	return DEFAULT_SERIES_COMPARATOR.compare(series1,series2);
			return -1;
		}
	}

	// =======================================================================

	public static class DICOMImageComparator implements Comparator<DICOMImage> {

		/**
		 * Compare two DICOMImage objects.  Sort first by seriesDate, then seriesTime,
		 * then series description, then series number, modality, instance number, 
		 * then instance uid. 
		 * 
		 * @param obj1
		 * @param obj2
		 * @return
		 */
		public int compare(DICOMImage obj1, DICOMImage obj2) {

			DICOMImage img1=(DICOMImage)obj1;
			DICOMImage img2=(DICOMImage)obj2;
			DICOMHeader dh1=img1.getDICOMHeader();
			DICOMHeader dh2=img2.getDICOMHeader();
			
			String seriesDate1=dh1.getSeriesDate();
			String seriesDate2=dh2.getSeriesDate();
			if ((seriesDate1!=null)&&(seriesDate2!=null)) {
				int diff=seriesDate1.compareTo(seriesDate2);
				if (diff!=0) return diff;
			}

			String seriesTime1=dh1.getSeriesTime();
			String seriesTime2=dh2.getSeriesTime();
			if ((seriesTime1!=null)&&(seriesTime2!=null)) {
				int diff=seriesTime1.compareTo(seriesTime2);
				if (diff!=0) return diff;
			}
			
			String seriesDesc1=dh1.getSeriesDescription();
			String seriesDesc2=dh2.getSeriesDescription();
			if ((seriesDesc1!=null)&&(seriesDesc2!=null)) {
				int diff=seriesDesc1.compareTo(seriesDesc2);
				if (diff!=0) return diff;
			}

			int seriesNumber1=dh1.getSeriesNumber();
			int seriesNumber2=dh2.getSeriesNumber();
			int diff=(seriesNumber1-seriesNumber2);
			if (diff!=0) return diff;

			String modality1=dh1.getSeriesImageModality();
			String imageInstance1=dh1.getImageInstance();
			String imageInstance2=dh2.getImageInstance();
			if ((imageInstance1!=null)&&(imageInstance2!=null)) {
				int imgNum1=Integer.parseInt(imageInstance1.trim());
				int imgNum2=Integer.parseInt(imageInstance2.trim());
				diff=imgNum1-imgNum2;
				if (diff!=0) return diff;
			}

			diff=dh1.getSOPInstanceUID().compareTo(dh2.getSOPInstanceUID());
			if ((diff==0)&&(dh1.getNumberFrames()>1)) return 1;
			return diff;
		}
	}

	// =======================================================================

	public static void main(String args[]) {

		if (args.length==1) {
			DICOMReader dr=new DICOMReader();
			ImageSequence is=dr.readDirectory(args[0],true);
			ArrayList<DICOMImageStudy> al=dr.organizeByStudy(is);
			System.err.println(dr.toString(al));
		}
	}
}
