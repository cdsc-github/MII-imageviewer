/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

import java.util.ArrayList;
import java.io.Serializable;

import imageviewer.ui.annotation.Annotation;

// =======================================================================

public interface PresentationState extends Serializable {

	public String[] getGroupingKeys();
	public String[] getPresentationPixelAspectRatio();
	public String[] getPresentationPixelSpacing();
	public String[] getSortingKeys();
	public String[] getPSStudyDescription();
	public String[] getPSSeriesDescription();

	public String getStudyTimestamp();
	public String getSeriesTimestamp();

	public String getImageHorizontalFlip();
	public String getImageType();
	public String getInstitution();
	public String getModality();
	public String getPresentationCreationDate();
	public String getPresentationCreationTime();
	public String getPresentationCreatorName();
	public String getPresentationDescription();
	public String getPresentationLUTShape();
	public String getPresentationLabel();
	public String getPresentationSizeMode();
	public String getReferencedImageKey();
	public String getReferencedSeriesKey();
	public String getReferencedStudyKey();
	public String getRescaleType();

	public double getRescaleIntercept();
	public double getRescaleSlope();
	public double getWindowCenter();
	public double getWindowLevel();

	public short[] getDisplayedAreaBottomRightHandCorner();
	public short[] getDisplayedAreaTopLeftHandCorner();

	public short getImageRotation();

	public float getPresentationPixelMagnificationRatio();

	public ArrayList<Annotation> getAnnotations();

	public void setPresentationCreationDate(String x);
	public void setPresentationCreationTime(String x);
	public void setPresentationCreatorName(String x);
	public void setPresentationDescription(String x);
}
