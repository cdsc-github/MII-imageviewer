/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Image;
import java.awt.Toolkit;

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

// =======================================================================

public class GrayFilter extends RGBImageFilter {

	public GrayFilter() {canFilterIndexColorModel=true;}

	// =======================================================================

	public static Image createDisabledImage(Image i) {

		GrayFilter filter=new GrayFilter();
		ImageProducer prod=new FilteredImageSource(i.getSource(),filter);
		Image grayImage=Toolkit.getDefaultToolkit().createImage(prod);
		return grayImage;
	}

	// =======================================================================
    
	public int filterRGB(int x, int y, int rgb) {

    int red=(rgb & 0x00FF0000) >>> 16;
    int green=(rgb & 0x0000FF00) >>> 8;
    int blue=rgb & 0x0000FF;
    int grey=(int)((red+green+blue)/4);
		if (grey<40) grey=40;
		if (grey>255) grey=255;
		return (rgb & 0xff000000) | (grey << 16) | (grey << 8) | (grey << 0);
	}
}

