/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/* ==========================================================================

  Copyright (C) 2005 UCLA Medical Imaging Informatics

  This library is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2.1 of the
  License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA

  Email: opensource@mii.ucla.edu

  Mail:  UCLA Medical Imaging Informatics
         924 Westwood Bl., Suite 420
         Westwood, CA 90024

   Web:  http://www.mii.ucla.edu/imageviewer/ 

==========================================================================*/
// Attempt to find decent (if not optimal) window/level settings for
// magnetic resoance images.  Loosely based on Wendt's paper,
// "Automatic Adjustment of Contrast and Brightness of MRI" (J.
// Digital Imaging, 1994), with adaptation of the algorithm for
// certain cases.  To accomomdate abnormalities in the image (e.g.,
// devices), the high-value is not used to calculate the median, but
// instead the threshold value for 99% of the pixels is used.

package imageviewer.rendering.wl;

import javax.media.jai.Histogram;

import imageviewer.model.Image;
import imageviewer.model.dicom.DICOMImage;
import imageviewer.model.dicom.DICOMHeader;

public class SimpleMRWindowLevel implements WindowLevelAlgorithm {

	public static final double THRESHOLD_VALUE=0.99;
	public static final String SPIN_ECHO=new String("SE");
	public static final String INVERSION_RECOVERY=new String("IR");
	public static final String GRADIENT_RECALLED=new String("GR");
	public static final String ECHO_PLANAR=new String("EP");
	public static final String RESARCH_MODE=new String("RM");
	public static final String SEGMENTED_K_SPACE=new String("SK");
	public static final String MAGNETIZATION_TRANSFER_CONTRAST=new String("MTC");
	public static final String STEADY_STATE=new String("SS");
	public static final String TIME_REVERSED=new String("TRSS");
	public static final String SPOILED=new String("SP");
	public static final String MAG_PREPARED=new String("MP");
	public static final String OVERSAMPLING=new String("OSP");
	public static final String NONE=new String("NONE");

	public static final int T1=0;
	public static final int T2=1;
	public static final int PROTON_DENSITY=2;
	public static final int SCOUT=3;

	// =======================================================================

	public WindowLevel computeWindowLevel(Image i) {

		// Typically the low value should be 0, but look it up anyway...

		if (!(i instanceof DICOMImage)) return null;

		DICOMImage di=(DICOMImage)i;
		Histogram h=di.getHistogram();
		DICOMHeader dh=di.getDICOMHeader();
		int seriesType=classifySeries(dh);
		double median=(((h.getPTileThreshold(THRESHOLD_VALUE))[0])+((h.getLowValue())[0]))/2;
		double min=0, max=0;

		switch (seriesType) {

			          case T1: min=0;
				                 int plane=dh.computePlane();
												 if (plane==DICOMHeader.AXIAL) {max=(median*1.540); break;}
												 if (plane==DICOMHeader.SAGITTAL) {max=(median*1.517);	break;}
												 max=(median*1.473);
												 break;
			          case T2: min=0;
				                 max=(median*2.3);
												 break;
	           case SCOUT: min=0;
					               max=(median*2.0);
										     break;
    case PROTON_DENSITY: min=0;
				                 max=(median*1.905);
												 break;
		}

		int level=(int)((max-min)/2);
		int window=(int)(max-min);
		return new WindowLevel(window,level);
	}

	// =======================================================================
	/* Attempts to classify the MR image sequence so that an appropriate
	 * window/level setting may be chosen. Based on heuristic rules
	 * that will probably need to be updated if not configurable from a
	 * XML file.
	 */

	private int classifySeries(DICOMHeader dh) {

		String scanSequence=dh.getSeriesScanningSequence();
		double flipAngle=dh.getImageFlipAngle();
		double TR=dh.getImageRepetitionTime();
		double TE=dh.getImageEchoTime();

		// double delta90=Math.abs(90-flipAngle);
		// double delta30=Math.abs(30-flipAngle);

		if ((scanSequence!=null)&&((scanSequence.compareTo(SPIN_ECHO)==0)||(scanSequence.compareTo(INVERSION_RECOVERY)==0))) {
			if ((TE<=30)&&(TR<=800)) return T1;
			if ((TR<40)&&(TE<100)) return SCOUT;
			if ((TE<=30)&&(TR>1200)) return PROTON_DENSITY;
			if ((TE>=70)&&(TR>2000)) return T2;
			return T2;
		}

		if ((scanSequence!=null)&&(scanSequence.compareTo(GRADIENT_RECALLED)==0)) {
			if ((TR<40)&&(TE<100)) return SCOUT;
			if (flipAngle>=30) return T1;
			if (flipAngle<=15) return T2;
			return T2;
		}

		return T2;
	}
}
