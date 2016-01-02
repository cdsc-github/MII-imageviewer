/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import java.awt.Rectangle;

import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.util.ArrayList;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;

import javax.vecmath.Point3d;

import imageviewer.model.Image;
import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.model.processing.ColorMap;

import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.rendering.wl.WindowLevel;
import imageviewer.system.ImageReaderManager;

// =======================================================================

public class TextureVolumeData implements VolumeData {

	public static boolean CONSTRAIN_TEXTURE_SIZE=true;

	int baseWidth=0, baseHeight=0, textureWidth=0, textureHeight=0, threshold=0, depth=0;
	double xSpacing=0, ySpacing=0, zSpacing=0;
	double scaledXSpace=0, scaledYSpace=0, scaledZSpace=0;
	Point3d maxCoordinate=new Point3d(), minCoordinate=new Point3d();
	ImageSequence is=null;

	Point3d[][] facePoints = new Point3d[6][];
	Point3d[]	volumePts=new Point3d[8];
	Point3d referencePoint=new Point3d();

	LookupTableJAI table=null;

	// =======================================================================

	public TextureVolumeData(String dir, String imageType, int threshold) {

		ImageReader ir=ImageReaderManager.getInstance().getImageReader(imageType);
		ImageSequence files=ir.readDirectory(dir,true);
		ArrayList imageStudies=ir.organizeByStudy(files);
		if ((imageStudies!=null)&&(!imageStudies.isEmpty())) {
			ImageSequenceGroup isg=(ImageSequenceGroup)imageStudies.get(0);
			initialize((ImageSequence)isg.getSequence(0),threshold);
		}
	}

	public TextureVolumeData(ImageSequence is, int threshold) {initialize(is,threshold);}

	// =======================================================================

	private void initialize(ImageSequence is, int threshold) {

		this.is=is; 
		this.threshold=threshold;

		if (is!=null) {
			Image i=is.get(0);
			WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i);
			table=WindowLevel.createLinearGrayscaleLookupTable(wl,i.getMaxPixelValue(),255);
			double[] dim=i.getPixelDimensions();
			xSpacing=(dim!=null) ? dim[0] : 1;                 
			ySpacing=(dim!=null) ? dim[1] : 1;
			zSpacing=i.getImageSliceThickness();
			baseWidth=i.getWidth();                            
			baseHeight=i.getHeight();
			textureWidth=textureSize(baseWidth);
			textureHeight=textureSize(baseHeight);
			if (CONSTRAIN_TEXTURE_SIZE) {
				if (textureWidth>256) textureWidth=256;
				if (textureHeight>256) textureHeight=256;
			}
			depth=is.size();
		}

		for (int i=0; i<8; i++) volumePts[i]=new Point3d();
		for (int i=0; i<6; i++) facePoints[i]=new Point3d[4];

		facePoints[PLUS_X][0]=volumePts[5];
		facePoints[PLUS_X][1]=volumePts[4];
		facePoints[PLUS_X][2]=volumePts[7];
		facePoints[PLUS_X][3]=volumePts[6];
		facePoints[PLUS_Y][0]=volumePts[2];
		facePoints[PLUS_Y][1]=volumePts[3];
		facePoints[PLUS_Y][2]=volumePts[7];
		facePoints[PLUS_Y][3]=volumePts[6];
		facePoints[PLUS_Z][0]=volumePts[1];
		facePoints[PLUS_Z][1]=volumePts[2];
		facePoints[PLUS_Z][2]=volumePts[6];
		facePoints[PLUS_Z][3]=volumePts[5];

		facePoints[MINUS_X][0]=volumePts[0];
		facePoints[MINUS_X][1]=volumePts[1];
		facePoints[MINUS_X][2]=volumePts[2];
		facePoints[MINUS_X][3]=volumePts[3];
		facePoints[MINUS_Y][0]=volumePts[0];
		facePoints[MINUS_Y][1]=volumePts[4];
		facePoints[MINUS_Y][2]=volumePts[5];
		facePoints[MINUS_Y][3]=volumePts[1];
		facePoints[MINUS_Z][0]=volumePts[0];
		facePoints[MINUS_Z][1]=volumePts[3];
		facePoints[MINUS_Z][2]=volumePts[7];
		facePoints[MINUS_Z][3]=volumePts[4];
	}

	// =======================================================================

	public void update() {

		double maxX=getMaxX()*getXSpacing();
		double maxY=getMaxY()*getYSpacing();
		double maxZ=getMaxZ()*getZSpacing();
		double max=(maxX > maxY) ? maxX : maxY; 	// max=(max < maxZ) ? maxZ : max;
		double scale=(1.0/max);

		// Z-level scaling is independent of the x/y for now, otherwise it
		// won't fit in a 1/1/1 cube.

		scaledXSpace=getXSpacing()*scale;
		scaledYSpace=getYSpacing()*scale;
		scaledZSpace=getZSpacing()*(scale<(1/maxZ) ? scale : (1/maxZ)); 

		// The min and max coords are for the usable area of the texture,
		// which is has a half-texel boundary.  Otherwise the boundary
		// gets sampled, leading to artifacts with a texture color table.

		float offset=0.01f;
		minCoordinate.x=(getMinX()+offset)*scaledXSpace;
		minCoordinate.y=(getMinY()+offset)*scaledYSpace;
		minCoordinate.z=(getMinZ()+offset)*scaledZSpace;
		maxCoordinate.x=(getMaxX()-offset)*scaledXSpace;
		maxCoordinate.y=(getMaxY()-offset)*scaledYSpace;
		maxCoordinate.z=(getMaxZ()-offset)*scaledZSpace;

		volumePts[0].x=volumePts[1].x=volumePts[2].x=volumePts[3].x=minCoordinate.x;
		volumePts[4].x=volumePts[5].x=volumePts[6].x=volumePts[7].x=maxCoordinate.x;
		volumePts[0].y=volumePts[1].y=volumePts[4].y=volumePts[5].y=minCoordinate.y;
		volumePts[2].y=volumePts[3].y=volumePts[6].y=volumePts[7].y=maxCoordinate.y;
		volumePts[0].z=volumePts[3].z=volumePts[4].z=volumePts[7].z=minCoordinate.z;
		volumePts[1].z=volumePts[2].z=volumePts[5].z=volumePts[6].z=maxCoordinate.z;

		referencePoint.x=(maxCoordinate.x+minCoordinate.x)/2;
		referencePoint.y=(maxCoordinate.y+minCoordinate.y)/2;
		referencePoint.z=(maxCoordinate.z+minCoordinate.z)/2;
	}

	// =======================================================================

	public int getHeight() {return textureHeight;}                   // Texture size, power of 2
	public int getWidth() {return textureWidth;}
	public int getDepth() {return depth;}

	public int getMinX() {return 0;}                                 // Min and max in texture space
	public int getMinY() {return 0;}
	public int getMinZ() {return 0;}
	public int getMaxX() {return textureWidth;}
	public int getMaxY() {return textureHeight;}
	public int getMaxZ() {return depth;}

	public double getXSpacing() {return xSpacing;}                   // texture -> geometry scaling for the volume
	public double getYSpacing() {return ySpacing;}
	public double getZSpacing() {return zSpacing;}
	public double getScaledXSpace() {return scaledXSpace;}
	public double getScaledYSpace() {return scaledYSpace;}
	public double getScaledZSpace() {return scaledZSpace;}

	public Point3d getMinCoord() {return minCoordinate;}
	public Point3d getMaxCoord() {return maxCoordinate;}
	public Point3d getReferencePoint() {return referencePoint;}

	public Point3d[][] getFacePoints() {return facePoints;}
	public Point3d[] getVolumePoints() {return volumePts;}

	// =======================================================================

	private int textureSize(int value) {

		int retval=16;
		while (retval<value) retval*=2;
		return retval;
	}

	// =======================================================================
	// Old method for getting actual byte data out of the image sequence...

	public byte[][] getRawByteData() {
		
		byte[][] fileData=new byte[is.size()][];
		Rectangle rect=new Rectangle(0,0,textureWidth,textureHeight);
		for (int loop=0, n=is.size(); loop<n; loop++) {
			Image i=is.get(loop);
			RenderedImage ri=JAI.create("lookup",i.getRenderedImage(),table);
			Raster r=ri.getData();
			RenderedImage[] src={ri};
			RasterAccessor ra=new RasterAccessor(r,rect,(RasterAccessor.findCompatibleTags(src,ri))[0],ri.getColorModel());
			fileData[loop]=ra.getByteDataArray(0);
		}
		return fileData;
	}

	// =======================================================================
	// Old method for getting actual int data out of the image sequence...

	public int[][] getRawIntData() {

		int[][] fileData=new int[is.size()][];
		Rectangle rect=new Rectangle(0,0,textureWidth,textureHeight);
		for (int loop=0, n=is.size(); loop<n; loop++) {
			Image i=is.get(loop);
			RenderedImage ri=JAI.create("lookup",i.getRenderedImage(),table);
			Raster r=ri.getData();
			RenderedImage[] src={ri};
			RasterAccessor ra=new RasterAccessor(r,rect,(RasterAccessor.findCompatibleTags(src,ri))[0],ri.getColorModel());
			fileData[loop]=ra.getIntDataArray(0);
		}
		return fileData;
	}

	// =======================================================================

	public RenderedImage getRenderedImage(int index) {

		Image i=is.get(index);
		double scaleX=(double)textureWidth/(double)baseWidth;
		double scaleY=(double)textureHeight/(double)baseHeight;
		if ((scaleX<1)||(scaleY<1)) {
			ParameterBlock pb=new ParameterBlock();
			pb.addSource(i.getRenderedImage());
			pb.add(AffineTransform.getScaleInstance(scaleX,scaleY));
			PlanarImage pi=JAI.create("affine",pb);
			RenderedImage ri=JAI.create("lookup",pi,table);
			pi.dispose();
			pi=null;
			return ri;
		} else {
			int right=textureWidth-baseWidth;
			int bottom=textureHeight-baseHeight;
			if ((right!=0)||(bottom!=0)) {
				ParameterBlock pb=new ParameterBlock();
				pb.addSource(i.getRenderedImage());
				pb.add(new Integer(0));
				pb.add(new Integer(right));
				pb.add(new Integer(0));
				pb.add(new Integer(bottom));
				pb.add(BorderExtender.createInstance(BorderExtender.BORDER_ZERO));
				PlanarImage pi=JAI.create("border",pb);
				RenderedImage ri=JAI.create("lookup",pi,table);
				pi.dispose();
				pi=null;
				return ri;
			} else {
				return JAI.create("lookup",i.getRenderedImage(),table);
			}
		}
	}
}
