/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package imageviewer.ui.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;

/**
 * ImageScaler - Scales a image down by scaling by 2 repeatedly till
 * it is less than twice the required size. Then it scales the last
 * bit by using Graphics.drawImage() scaled with bilinear. The scale
 * by two is done by averaging each 4 pixels into one with the data
 * array for the image without creating a new image. If the image is
 * not a supported data type then the first scale by 50% is done with
 * Graphics.drawImage() to into a new image of compatable type.
 *
 * @author Jasper Potts
 */

public class ImageScaler {

	/**
	 * Scale down a image to fit in the required size, using java
	 * biliner to fill in what power 2 algorithum can't do
	 *
	 * @param image     The source image to scale
	 * @param reqWidth  The required destination reqWidth
	 * @param reqHeight The required destination height
	 * @return The scaled image
	 */

	public static final BufferedImage scale(BufferedImage image, int reqWidth, int reqHeight) {

		if (reqWidth<=0 || reqHeight<=0) throw new IllegalArgumentException("Destination size to scale to must be > 0 not ("+reqWidth+","+reqHeight+")");
		if (image.getColorModel() instanceof IndexColorModel) {
			double dScale=calculateScaleFactor(image.getWidth(),image.getHeight(),reqWidth,reqHeight);
			if (dScale<0.5) {
				int newWidth=image.getWidth()/2,newHeight=image.getHeight()/2;
				BufferedImage img=new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
				Graphics2D g2=img.createGraphics();
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(image,0,0,newWidth,newHeight,0,0,image.getWidth(),image.getHeight(),null);
				g2.dispose();
				image=img;
			}
		}

		BufferedImage oImage=scalePower2(image,reqWidth,reqHeight);
		double dScale=calculateScaleFactor(oImage.getWidth(),oImage.getHeight(),reqWidth,reqHeight);

		int iNewWidth=(int)(oImage.getWidth()*dScale);
		int iNewHeight=(int)(oImage.getHeight()*dScale);
		BufferedImage oDest=new BufferedImage(iNewWidth,iNewHeight,BufferedImage.TYPE_INT_RGB);
		Graphics2D oGraphics=oDest.createGraphics();
		oGraphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		oGraphics.drawImage(oImage,0,0,iNewWidth,iNewHeight,null);
		oGraphics.dispose();
		return oDest;
	}

	/**
	 * Scale an image to fit in the rectangle spcified by reqWidth and
	 * reqHeight. Uses java bilinear for the scaling.
	 *
	 * @param src       The source image to scale
	 * @param reqWidth  Max width of distination image
	 * @param reqHeight Max height of distination image
	 * @return The scaled image
	 */

	public static BufferedImage javaScale(BufferedImage src, int reqWidth, int reqHeight) {

		double dScale=calculateScaleFactor(src.getWidth(),src.getHeight(),reqWidth,reqHeight);
		BufferedImage dest=src;
		while (dScale<0.5) {
			int newWidth=dest.getWidth()/2, newHeight=dest.getHeight()/2;
			BufferedImage img=new BufferedImage(newWidth,newHeight,BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2=img.createGraphics();
			g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g2.drawImage(dest,0,0,newWidth,newHeight,0,0,dest.getWidth(),dest.getHeight(),null);
			g2.dispose();
			dest=img;
			dScale=calculateScaleFactor(dest.getWidth(),dest.getHeight(),reqWidth,reqHeight);
		}
		BufferedImage img=new BufferedImage(reqWidth,reqHeight,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2=img.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(dest,0,0,reqWidth,reqHeight,0,0,dest.getWidth(),dest.getHeight(),null);
		g2.dispose();
		return img;
	}

	/**
	 * Scale down a img to the nearest power to bigger than the required
	 * size.
	 *
	 * @param img       The source img to scale
	 * @param reqWidth  The required destination width
	 * @param reqHeight The required destination height
	 * @return The scaled img or source img if scale factor is not<0.5
	 */

	public static final BufferedImage scalePower2(BufferedImage img, int reqWidth, int reqHeight) {

		if (reqWidth<=0 || reqHeight<=0) throw new IllegalArgumentException("Destination size to scale to must be > 0 not ("+reqWidth+","+reqHeight+")");
		int iSrcWidth=img.getWidth();
		int iSrcHeight=img.getHeight();
		double dScale=calculateScaleFactor(iSrcWidth, iSrcHeight, reqWidth, reqHeight);
		if (dScale<0.5) {
			Raster oRaster=img.getRaster();
			DataBuffer oDataBuffer=oRaster.getDataBuffer();
			BufferedImage oSmallImage=null;
			switch (oDataBuffer.getDataType()) {

			  case DataBuffer.TYPE_BYTE: switch (oRaster.getNumBands()) {
				                             case 1: oSmallImage=scale1BandByte(img,oDataBuffer,reqWidth,reqHeight); break;
				                             case 3: oSmallImage=scale3BandByte(img,oDataBuffer,reqWidth,reqHeight);	break;
				                             case 4: oSmallImage=scale4BandByte(img,oDataBuffer,reqWidth,reqHeight);	break;
				                            default: throw new IllegalArgumentException("Requires a buffered img with 3 or 4 bands");
				                           }
				                           break;
			   case DataBuffer.TYPE_INT: switch (oRaster.getNumBands()) {
				                             case 3: oSmallImage=scale3BandInt(img,oDataBuffer,reqWidth,reqHeight); break;
				                             case 4: oSmallImage=scale4BandInt(img,oDataBuffer,reqWidth,reqHeight);	break;
				                            default: throw new IllegalArgumentException("Requires a buffered img with 3 or 4 bands");
																	 }
					                         break;
			                    default: throw new IllegalArgumentException("Unsupported image type ["+oDataBuffer.getDataType()+"]");
			}
			return oSmallImage;
		} else {
			return img;
		}
	}

	// =========================================================
	// private util methods

	private static final BufferedImage scale3BandInt(BufferedImage oSource, DataBuffer oDataBuffer, int reqWidth, int reqHeight) {

		int[] oBuffer=((DataBufferInt)oDataBuffer).getData();
		int iImgWidth=oSource.getWidth();
		int iImgHeight=oSource.getHeight();
		final int iBufferWidth=iImgWidth;

		double dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		while (dScale<0.5) {
			int iWorkingWidth=iImgWidth/2;
			int iWorkingHeight=iImgHeight/2;
			for (int y=0; y<iWorkingHeight; y++) {
				for (int x=0; x<iWorkingWidth; x++) {
					int iBIndex=(x+(y*iBufferWidth));
					int iAX=(x*2);
					int iAY=(y*2)*iBufferWidth;
					int iAY1=((y*2)+1)*iBufferWidth;
					int iAIndex1=iAX+iAY;
					int iAIndex2=iAIndex1+1;
					int iAIndex3=iAX+iAY1;
					int iAIndex4=iAIndex3+1;
					int iPixR1=(oBuffer[iAIndex1] & 0x00FF0000) >>> 16;
					int iPixR2=(oBuffer[iAIndex2] & 0x00FF0000) >>> 16;
					int iPixR3=(oBuffer[iAIndex3] & 0x00FF0000) >>> 16;
					int iPixR4=(oBuffer[iAIndex4] & 0x00FF0000) >>> 16;
					int iRed=((iPixR1+iPixR2+iPixR3+iPixR4)/4);
					int iPixB1=(oBuffer[iAIndex1] & 0x0000FF00) >>> 8;
					int iPixB2=(oBuffer[iAIndex2] & 0x0000FF00) >>> 8;
					int iPixB3=(oBuffer[iAIndex3] & 0x0000FF00) >>> 8;
					int iPixB4=(oBuffer[iAIndex4] & 0x0000FF00) >>> 8;
					int iBlue=((iPixB1+iPixB2+iPixB3+iPixB4)/4);
					int iPixG1=oBuffer[iAIndex1] & 0x000000FF;
					int iPixG2=oBuffer[iAIndex2] & 0x000000FF;
					int iPixG3=oBuffer[iAIndex3] & 0x000000FF;
					int iPixG4=oBuffer[iAIndex4] & 0x000000FF;
					int iGreen=((iPixG1+iPixG2+iPixG3+iPixG4)/4);
					oBuffer[iBIndex]=(iRed << 16) | (iBlue << 8) | iGreen;
				}
			}
			iImgWidth=iWorkingWidth;
			iImgHeight=iWorkingHeight;
			dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		}
		return oSource.getSubimage(0, 0, iImgWidth, iImgHeight);
	}

	private static final BufferedImage scale4BandInt(BufferedImage oSource, DataBuffer oDataBuffer, int reqWidth, int reqHeight) {

		int[] oBuffer=((DataBufferInt) oDataBuffer).getData();
		int iImgWidth=oSource.getWidth();
		int iImgHeight=oSource.getHeight();
		final int iBufferWidth=iImgWidth;

		double dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		while (dScale<0.5) {
			int iWorkingWidth=iImgWidth/2;
			int iWorkingHeight=iImgHeight/2;
			for (int y=0; y<iWorkingHeight; y++) {
				for (int x=0; x<iWorkingWidth; x++) {
					int iBIndex=(x+(y*iBufferWidth));
					int iAX=(x*2);
					int iAY=(y*2)*iBufferWidth;
					int iAY1=((y*2)+1)*iBufferWidth;
					int iAIndex1=iAX+iAY;
					int iAIndex2=iAIndex1+1;
					int iAIndex3=iAX+iAY1;
					int iAIndex4=iAIndex3+1;
					int iPixA1=(oBuffer[iAIndex1] & 0xFF000000) >>> 24;
					int iPixA2=(oBuffer[iAIndex2] & 0xFF000000) >>> 24;
					int iPixA3=(oBuffer[iAIndex3] & 0xFF000000) >>> 24;
					int iPixA4=(oBuffer[iAIndex4] & 0xFF000000) >>> 24;
					int iAlpha=((iPixA1+iPixA2+iPixA3+iPixA4)/4);
					int iPixR1=(oBuffer[iAIndex1] & 0x00FF0000) >>> 16;
					int iPixR2=(oBuffer[iAIndex2] & 0x00FF0000) >>> 16;
					int iPixR3=(oBuffer[iAIndex3] & 0x00FF0000) >>> 16;
					int iPixR4=(oBuffer[iAIndex4] & 0x00FF0000) >>> 16;
					int iRed=((iPixR1+iPixR2+iPixR3+iPixR4)/4);
					int iPixB1=(oBuffer[iAIndex1] & 0x0000FF00) >>> 8;
					int iPixB2=(oBuffer[iAIndex2] & 0x0000FF00) >>> 8;
					int iPixB3=(oBuffer[iAIndex3] & 0x0000FF00) >>> 8;
					int iPixB4=(oBuffer[iAIndex4] & 0x0000FF00) >>> 8;
					int iBlue=((iPixB1+iPixB2+iPixB3+iPixB4)/4);
					int iPixG1=oBuffer[iAIndex1] & 0x000000FF;
					int iPixG2=oBuffer[iAIndex2] & 0x000000FF;
					int iPixG3=oBuffer[iAIndex3] & 0x000000FF;
					int iPixG4=oBuffer[iAIndex4] & 0x000000FF;
					int iGreen=((iPixG1+iPixG2+iPixG3+iPixG4)/4);
					oBuffer[iBIndex]=(iAlpha << 24) | (iRed << 16) | (iBlue << 8) | iGreen;
				}
			}
			iImgWidth=iWorkingWidth;
			iImgHeight=iWorkingHeight;
			dScale=dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		}
		return oSource.getSubimage(0,0,iImgWidth,iImgHeight);
	}

	private static final BufferedImage scale1BandByte(BufferedImage oSource, DataBuffer oDataBuffer, int reqWidth, int reqHeight) {

		byte[] oBuffer=((DataBufferByte) oDataBuffer).getData();
		int iImgWidth=oSource.getWidth();
		int iImgHeight=oSource.getHeight();
		final int iBufferWidth=iImgWidth;

		double dScale=calculateScaleFactor(iImgWidth, iImgHeight, reqWidth, reqHeight);
		while (dScale<0.5) {
			int iWorkingWidth=iImgWidth/2;
			int iWorkingHeight=iImgHeight/2;
			for (int y=0; y<iWorkingHeight; y++) {
				for (int x=0; x<iWorkingWidth; x++) {
					int iBIndex=(x+(y*iBufferWidth));
					int iAX=(x*2);
					int iAY=(y*2)*(iBufferWidth);
					int iAY1=((y*2)+1)*(iBufferWidth);
					int iAIndex1=iAX+iAY;
					int iAIndex2=iAIndex1+1;
					int iAIndex3=iAX+iAY1;
					int iAIndex4=iAIndex3+1;
					int iPix1=(int) oBuffer[iAIndex1] & 0xFF;
					int iPix2=(int) oBuffer[iAIndex2] & 0xFF;
					int iPix3=(int) oBuffer[iAIndex3] & 0xFF;
					int iPix4=(int) oBuffer[iAIndex4] & 0xFF;
					oBuffer[iBIndex]=(byte) ((iPix1+iPix2+iPix3+iPix4)/4);
				}
			}
			iImgWidth=iWorkingWidth;
			iImgHeight=iWorkingHeight;
			dScale=dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		}
		return oSource.getSubimage(0,0,iImgWidth,iImgHeight);
	}

	private static final BufferedImage scale3BandByte(BufferedImage oSource, DataBuffer oDataBuffer, int reqWidth, int reqHeight) {

		byte[] oBuffer=((DataBufferByte) oDataBuffer).getData();
		int iImgWidth=oSource.getWidth();
		int iImgHeight=oSource.getHeight();
		final int iBufferWidth=iImgWidth;

		double dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		while (dScale<0.5) {
			int iWorkingWidth=iImgWidth/2;
			int iWorkingHeight=iImgHeight/2;
			for (int y=0; y<iWorkingHeight; y++) {
				for (int x=0; x<iWorkingWidth; x++) {
					int iBIndex=(x+(y*iBufferWidth))*3;
					int iAX=(x*2)*3;
					int iAY=(y*2)*(iBufferWidth*3);
					int iAY1=((y*2)+1)*(iBufferWidth*3);
					int iAIndex1=iAX+iAY;
					int iAIndex2=iAIndex1+3;
					int iAIndex3=iAX+iAY1;
					int iAIndex4=iAIndex3+3;
					int iPixB1=(int) oBuffer[iAIndex1] & 0xFF;
					int iPixB2=(int) oBuffer[iAIndex2] & 0xFF;
					int iPixB3=(int) oBuffer[iAIndex3] & 0xFF;
					int iPixB4=(int) oBuffer[iAIndex4] & 0xFF;
					oBuffer[iBIndex]=(byte) ((iPixB1+iPixB2+iPixB3+iPixB4)/4);
					int iPixG1=(int) oBuffer[iAIndex1+1] & 0xFF;
					int iPixG2=(int) oBuffer[iAIndex2+1] & 0xFF;
					int iPixG3=(int) oBuffer[iAIndex3+1] & 0xFF;
					int iPixG4=(int) oBuffer[iAIndex4+1] & 0xFF;
					oBuffer[iBIndex+1]=(byte) ((iPixG1+iPixG2+iPixG3+iPixG4)/4);
					int iPixR1=(int) oBuffer[iAIndex1+2] & 0xFF;
					int iPixR2=(int) oBuffer[iAIndex2+2] & 0xFF;
					int iPixR3=(int) oBuffer[iAIndex3+2] & 0xFF;
					int iPixR4=(int) oBuffer[iAIndex4+2] & 0xFF;
					oBuffer[iBIndex+2]=(byte) ((iPixR1+iPixR2+iPixR3+iPixR4)/4);
				}
			}
			iImgWidth=iWorkingWidth;
			iImgHeight=iWorkingHeight;
			dScale=dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		}
		return oSource.getSubimage(0,0,iImgWidth,iImgHeight);
	}

	private static final BufferedImage scale4BandByte(BufferedImage oSource, DataBuffer oDataBuffer, int reqWidth, int reqHeight) {

		byte[] oBuffer=((DataBufferByte) oDataBuffer).getData();
		int iImgWidth=oSource.getWidth();
		int iImgHeight=oSource.getHeight();
		final int iBufferWidth=iImgWidth;

		double dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		while (dScale<0.5) {
			int iWorkingWidth=iImgWidth/2;
			int iWorkingHeight=iImgHeight/2;
			for (int y=0; y<iWorkingHeight; y++) {
				for (int x=0; x<iWorkingWidth; x++) {
					int iBIndex=(x+(y*iBufferWidth))*4;
					int iAX=(x*2)*4;
					int iAY=(y*2)*(iBufferWidth*4);
					int iAY1=((y*2)+1)*(iBufferWidth*4);
					int iAIndex1=iAX+iAY;
					int iAIndex2=iAIndex1+4;
					int iAIndex3=iAX+iAY1;
					int iAIndex4=iAIndex3+4;
					int iPixB1=(int) oBuffer[iAIndex1] & 0xFF;
					int iPixB2=(int) oBuffer[iAIndex2] & 0xFF;
					int iPixB3=(int) oBuffer[iAIndex3] & 0xFF;
					int iPixB4=(int) oBuffer[iAIndex4] & 0xFF;
					oBuffer[iBIndex]=(byte) ((iPixB1+iPixB2+iPixB3+iPixB4)/4);
					int iPixG1=(int) oBuffer[iAIndex1+1] & 0xFF;
					int iPixG2=(int) oBuffer[iAIndex2+1] & 0xFF;
					int iPixG3=(int) oBuffer[iAIndex3+1] & 0xFF;
					int iPixG4=(int) oBuffer[iAIndex4+1] & 0xFF;
					oBuffer[iBIndex+1]=(byte) ((iPixG1+iPixG2+iPixG3+iPixG4)/4);
					int iPixR1=(int) oBuffer[iAIndex1+2] & 0xFF;
					int iPixR2=(int) oBuffer[iAIndex2+2] & 0xFF;
					int iPixR3=(int) oBuffer[iAIndex3+2] & 0xFF;
					int iPixR4=(int) oBuffer[iAIndex4+2] & 0xFF;
					oBuffer[iBIndex+2]=(byte) ((iPixR1+iPixR2+iPixR3+iPixR4)/4);
				}
			}
			iImgWidth=iWorkingWidth;
			iImgHeight=iWorkingHeight;
			dScale=dScale=calculateScaleFactor(iImgWidth,iImgHeight,reqWidth,reqHeight);
		}
		return oSource.getSubimage(0,0,iImgWidth,iImgHeight);
	}

	/**
	 * Calculate the scale factor to scale the src image to fit withing
	 * the required width and height.
	 *
	 * @param srcWidth  Source image width
	 * @param srcHeight Source image height
	 * @param reqWidth  Required image width
	 * @param reqHeight Required image height
	 * @return the scale factor, eg 0.5 to scale to a to half size
	 */

	public static final double calculateScaleFactor(int srcWidth, int srcHeight, int reqWidth, int reqHeight) {

		double dXscale=(double)reqWidth/(double)srcWidth;
		double dYscale=(double)reqHeight/(double)srcHeight;
		return Math.min(dXscale,dYscale);
	}
}
