/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

/**
 * Properties of an ImageSequence.
 */
public interface ImageSequenceProperties {

	public static final String MODALITY=new String("__MODALITY");
	public static final String DESCRIPTION=new String("__SERIES_DESCRIPTION");
	public static final String TIMESTAMP=new String("__TIMESTAMP");
	public static final String NUMBER_IMAGES=new String("__NUM_IMAGES");
	public static final String BODY_PART=new String("__BODY_PART");

	public static final String MANUFACTURER=new String("__MANUFACTURER");
	public static final String MANUFACTURER_MODEL=new String("__MANUFACTURER_MODEL");
	public static final String STATION=new String("__STATION");

	public static final String SEQUENCE_NAME=new String("__SEQ_NAME");
	public static final String SCAN_SEQUENCE=new String("__SCAN_SEQUENCE");
	public static final String SEQUENCE_VARIANT=new String("__SEQUENCE_VARIANT");
	public static final String CONTRAST=new String("__CONTRAST");

	public static final String FILENAME=new String("__FILENAME");

	public static final String MAX_PIXEL_VALUE=new String("__MAX_PIXEL_VALUE");
}
