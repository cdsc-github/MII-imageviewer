/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import java.awt.Transparency;
import java.awt.color.ColorSpace;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

import javax.media.j3d.ImageComponent;
import javax.media.j3d.ImageComponent3D;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture3D;

import javax.vecmath.Vector4f;

import imageviewer.model.Image;
import imageviewer.model.processing.ColorMap;

// =======================================================================

public class TextureVolume extends Volume {

	TextureVolumeData tvd=null;
	boolean useColormap=true;

	public TextureVolume(TextureVolumeData tvd, boolean useColormap) {

		super(tvd); 
		this.tvd=tvd;
		this.useColormap=useColormap;
	}

	// =======================================================================

	public void generateTexture() {
		
		xSize=textureSize(tvd.getMaxX()-tvd.getMinX());
		ySize=textureSize(tvd.getMaxY()-tvd.getMinY());
		zSize=textureSize(tvd.getMaxZ()-tvd.getMinZ());

		xTexGenScale=(float)(1.0/(tvd.getScaledXSpace()*xSize));
		yTexGenScale=(float)(1.0/(tvd.getScaledYSpace()*ySize));
		zTexGenScale=(float)(1.0/(tvd.getScaledZSpace()*zSize));

		// Set up the texture coordinate generation 

		tcg=new TexCoordGeneration();
		tcg.setFormat(TexCoordGeneration.TEXTURE_COORDINATE_3);
		tcg.setPlaneS(new Vector4f(xTexGenScale,0.0f,0.0f,0.0f));
		tcg.setPlaneT(new Vector4f(0.0f,yTexGenScale,0.0f,0.0f));
		tcg.setPlaneR(new Vector4f(0.0f,0.0f,zTexGenScale,0.0f));
			
		// Set up the texture based on whether we use a colormap or just
		// grayscale intensities to do the mapping.

		texture=new Texture3D(Texture.BASE_LEVEL,(useColormap) ? Texture.RGBA : Texture.INTENSITY,xSize,ySize,zSize);
		ImageComponent3D pArray=new ImageComponent3D(ImageComponent.FORMAT_CHANNEL8,xSize,ySize,zSize);
		for (int loop=0, n=tvd.getDepth(); loop<n; loop++) pArray.set(loop,tvd.getRenderedImage(loop));
		texture.setImage(0,pArray);
		texture.setEnable(true);
		texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
		texture.setMagFilter(Texture.NICEST);
		texture.setBoundaryModeS(Texture.CLAMP);
		texture.setBoundaryModeT(Texture.CLAMP);
		texture.setBoundaryModeR(Texture.CLAMP);

		/* ColorModel cm=ColorModel.getRGBdefault();
			 ColorMap linear=ColorMap.createLinearLuminanceTable();
			 WritableRaster wr=cm.createCompatibleWritableRaster(xSize,ySize); 
			 BufferedImage bi=new BufferedImage(cm,wr,false,null); 
			 int[] intData=((DataBufferInt)wr.getDataBuffer()).getData(); 
			 texture=new Texture3D(Texture.BASE_LEVEL,Texture.RGBA,xSize,ySize,zSize);
			 ImageComponent3D pArray=new ImageComponent3D(ImageComponent.FORMAT_RGBA,xSize,ySize,zSize);
			 for (int i=0, n=tvd.getDepth(); i<n; i++) { 
			 tvd.loadZRGBA(i,xSize,ySize,intData,linear);
			 pArray.set(i,bi);
			 }
			 texture.setImage(0,pArray);
			 bi.flush(); */
		/* int[] nBits={8};
			 ColorSpace cs=ColorSpace.getInstance(ColorSpace.CS_GRAY);
			 ColorModel cm=new ComponentColorModel(cs,nBits,false,false,Transparency.TRANSLUCENT,DataBuffer.TYPE_BYTE);
			 WritableRaster wr=cm.createCompatibleWritableRaster(xSize,ySize); 
			 BufferedImage bi=new BufferedImage(cm,wr,false,null); 
			 byte[] byteData=((DataBufferByte)wr.getDataBuffer()).getData(); 		
			 texture=new Texture3D(Texture.BASE_LEVEL,Texture.INTENSITY,xSize,ySize,zSize);
			 ImageComponent3D pArray=new ImageComponent3D(ImageComponent.FORMAT_CHANNEL8,xSize,ySize,zSize,true,false);
			 for (int i=0, n=vd.getDepth(); i<n; i++) { 
			 vd.loadZIntensity(i,xSize,ySize,byteData);
			 pArray.set(i,bi);
			 }
			 texture.setImage(0,pArray);
			 bi.flush(); */
	}
}
