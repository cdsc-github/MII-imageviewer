/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model;

public interface ImageProperties {

	public static final String IMAGE_MODALITY=new String("__MODALITY");
	public static final String IMAGE_DESCRIPTION=new String("__DESCRIPTION");
	public static final String IMAGE_TIMESTAMP=new String("__TIMESTAMP");

	public static final String WIDTH=new String("__WIDTH");
	public static final String HEIGHT=new String("__HEIGHT");
	public static final String BITS_PER_PIXEL=new String("__BITS_PER_PIXEL");

	public static final String FORMAT=new String("__IMAGE_FORMAT");
	public static final String PROGRESSIVE=new String("__PROGRESSIVE");
	public static final String MARKED=new String("__MARKED");

	public static final String FILE_LOCATION=new String("FileLocation");
}
