/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

/**
 * Properties common to an ImageSequenceGroup.
 */
public interface ImageSequenceGroupProperties {

	/**
	 * Study Description. 
	 */
	public static final String DESCRIPTION=new String("__STUDY_DESCRIPTION");
	
	/**
	 * Study Type. 
	 */
	public static final String TYPE=new String("__STUDY_TYPE");
	
	/**
	 * Timestamp
	 */
	public static final String TIMESTAMP=new String("__TIMESTAMP");
	
	/**
	 * The number of series in the study. 
	 */
	public static final String NUMBER_SERIES=new String("__NUM_SERIES");
}
