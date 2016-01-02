/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout.rules;

import java.util.ArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceProperties;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.model.ImageSequenceGroupProperties;

import imageviewer.model.dicom.DICOMHeader;
import imageviewer.model.dicom.DICOMImage;
import imageviewer.model.dicom.DICOMImageSeries;
import imageviewer.model.dicom.DICOMImageStudy;

import imageviewer.model.processing.ColorMap;
import imageviewer.model.processing.ColorMapImageProcessor;
import imageviewer.model.processing.ProcessedImageSequence;
import imageviewer.model.processing.TransformedImageProcessor;
import imageviewer.model.processing.WindowLevelImageProcessor;

import imageviewer.model.composite.CompositeImageSequence;
import imageviewer.model.composite.SequenceMap;

import imageviewer.rendering.wl.DefaultWindowLevelManager;

import imageviewer.ui.layout.CompositionRule;

// ===========================================================

public class CTPETOverlayRule implements CompositionRule {

	private static final Pattern CT_PET_PATTERN=Pattern.compile("ct.pet|pet.ct|petct");
	
	// ===========================================================

	String name=new String("CT/PET overlays");
	boolean ignored=false;

	public CTPETOverlayRule() {}

	// ===========================================================

	public String getName() {return name;}
	public boolean isIgnored() {return ignored;}

	public void setName(String x) {name=x;}
	public void setIgnored(boolean x) {ignored=x;}

	// ===========================================================

	private DICOMImageSeries findCT(DICOMImageStudy study) {

		for (int loop=0, n=study.size(); loop<n; loop++) {
			DICOMImageSeries dis=(DICOMImageSeries)study.get(loop);
			String description=(String)dis.getProperty(ImageSequenceProperties.DESCRIPTION);
			if (description!=null) {
				if (description.contains("CT")) return dis;
			}
		}
		return null;
	}

	private DICOMImageSeries findPET(DICOMImageStudy study) {

		DICOMImageSeries tmpSeries=null;

		for (int loop=0, n=study.size(); loop<n; loop++) {
			DICOMImageSeries dis=(DICOMImageSeries)study.get(loop);
			String modality=(String)dis.getProperty(ImageSequenceProperties.MODALITY);
			if (modality!=null) {
				if (modality.compareTo("PT")==0) {
					DICOMImage image=(DICOMImage)dis.get(0);
					DICOMHeader dh=image.getDICOMHeader();				
					String correctedImage=dh.getCorrectedImage();
					if (correctedImage!=null) {
						correctedImage=correctedImage.toLowerCase();
						if ((correctedImage.contains("attn"))&&(correctedImage.contains("scat"))) return dis;
						if (tmpSeries==null) tmpSeries=dis;
					}
				}
			}
		}
		return tmpSeries;
	}

	// ===========================================================

	private double[] scaleAndTranslateFactor(DICOMImageSeries ct, DICOMImageSeries pet) {

		double[] transform=new double[4];
		DICOMImage image1=(DICOMImage)ct.get(0);
		DICOMHeader ctHeader=image1.getDICOMHeader();
		DICOMImage image2=(DICOMImage)pet.get(0);
		DICOMHeader petHeader=image2.getDICOMHeader();
		double[] ctSpacing=ctHeader.getImagePixelSpacingArray();
		double[] petSpacing=petHeader.getImagePixelSpacingArray();
		transform[0]=petSpacing[0]/ctSpacing[0];
		transform[1]=petSpacing[1]/ctSpacing[1];
		transform[2]=-((transform[0]*image2.getWidth())-image1.getWidth())/2;
		transform[3]=-((transform[1]*image2.getHeight())-image1.getHeight())/2;
		return transform;
	}

	// ===========================================================

	public void process(ImageSequenceGroup isg) {

		if (!(isg instanceof DICOMImageStudy)) return;
		DICOMImageStudy study=(DICOMImageStudy)isg;
		String studyDescription=(String)study.getProperty(ImageSequenceGroupProperties.DESCRIPTION);
		if (studyDescription!=null) {
			studyDescription=studyDescription.toLowerCase();
			studyDescription=studyDescription.replaceAll("\\^\\d*|_|\\s+"," ");
			Matcher m=CT_PET_PATTERN.matcher(studyDescription);
			if (!m.find()) return;
			
			// Determine which series is the CT, and which is the target PET
			// sequence. There must be a better way of doing this...

			DICOMImageSeries ct=findCT(study); if (ct==null) return;
			DICOMImageSeries pet=findPET(study); if (pet==null) return;
	
			// Compute a transform of the pet data into the scale and
			// translation factor of the ct data. Use the pixel spacing
			// information to correlate the two data sets visually.

			SequenceMap sm=new CTPETSequenceMap(ct,pet);
			double[] transform=scaleAndTranslateFactor(ct,pet);
			TransformedImageProcessor tip=new TransformedImageProcessor();
			tip.setScaleX(transform[0]);
			tip.setScaleY(transform[1]);
			tip.setTranslateX(transform[2]);
			tip.setTranslateY(transform[3]);
		
			ProcessedImageSequence pis1=new ProcessedImageSequence(tip,pet);

			DICOMHeader petHeader=((DICOMImage)pet.get(0)).getDICOMHeader();
			double[] petWindowCenter=petHeader.getWindowCenter();
			double[] petWindowWidth=petHeader.getWindowWidth();
			// if ((petWindowCenter!=null)&&(petWindowWidth!=null)) {
			int colorMapStart=(int)(petWindowCenter[0]-(petWindowWidth[0]/2));
			int colorMapEnd=(int)(petWindowCenter[0]+(petWindowWidth[0]/2));
			int colorMapLength=colorMapEnd+1;
			// ColorMapImageProcessor cmimp=new ColorMapImageProcessor(ColorMap.createLinearSpectrumTable(colorMapStart,colorMapEnd,colorMapLength),colorMapStart,colorMapEnd);
			// ColorMapImageProcessor cmimp=new ColorMapImageProcessor(ColorMap.createRedBlueSpectrumTable(colorMapStart,colorMapEnd,colorMapLength),colorMapStart,colorMapEnd);
			// ColorMapImageProcessor cmimp=new ColorMapImageProcessor(ColorMap.createRedBlueSpectrumTable(100,4000,4000),100,4000); 
			ColorMapImageProcessor cmimp=new ColorMapImageProcessor(ColorMap.createLinearSpectrumTable(200,12000,10000),200,12000); 

			ProcessedImageSequence pis2=new ProcessedImageSequence(cmimp,pis1);
			WindowLevelImageProcessor wlip=new WindowLevelImageProcessor(DefaultWindowLevelManager.getDefaultWindowLevel(ct.get(0)));
			ProcessedImageSequence pis3=new ProcessedImageSequence(wlip,ct);
			CompositeImageSequence cis=CompositeImageSequence.generate(new ImageSequence[]{pis3,pis2},sm);
			study.removeSeries(ct);
			study.removeSeries(pet);
			study.add(cis);
		}
	}

	// ===========================================================
	// Look for the slices that are closest in position

	private class CTPETSequenceMap implements SequenceMap {

		int seqSize=0;
		int[][] mapping=null;

		public CTPETSequenceMap(DICOMImageSeries ct, DICOMImageSeries pet) {

			seqSize=ct.size();
			mapping=new int[seqSize][1];
			for (int i=0; i<seqSize; i++) {
				DICOMImage di=(DICOMImage)ct.get(i);
				DICOMHeader dh=di.getDICOMHeader();
				double targetPosition=dh.getImageSlicePosition();
				double delta=Double.MAX_VALUE;
				for (int j=0, m=pet.size(); j<m; j++) {
					DICOMImage petImage=(DICOMImage)pet.get(j);
					DICOMHeader petHeader=petImage.getDICOMHeader();
					double petImagePosition=petHeader.getImageSlicePosition();
					if (petImagePosition==targetPosition) {
						mapping[i][0]=j;
						j=m;
					} else if (Math.abs(petImagePosition-targetPosition)<delta) {
						delta=Math.abs(petImagePosition-targetPosition);
						mapping[i][0]=j;
					}
				}
			}
		}
	
		public int size() {return seqSize;}
		public int[] computeSlices(int imageNumber) {return mapping[imageNumber];}
	}
}
