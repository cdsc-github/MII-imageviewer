/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.jogl;

import java.awt.Rectangle;
import java.awt.Point;

import java.awt.geom.AffineTransform;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import java.io.File;
import java.io.IOException;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import java.util.ArrayList;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.LookupTableJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.BufferUtil;

import imageviewer.model.Image;
import imageviewer.model.ImageReader;
import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.rendering.wl.WindowLevel;
import imageviewer.system.ImageReaderManager;

// =======================================================================

public class TextureVolume {

	public static boolean CONSTRAIN_TEXTURE_SIZE=true;
	public static int TEXTURE_MAX_SIZE=256;
	public static double DEG2RAD=3.14/180;

	private static final int X0=0;
	private static final int X1=1;
	private static final int Y0=2;
	private static final int Y1=3;
	private static final int Z0=4;
	private static final int Z1=5;

	// =======================================================================

	double scaledXSpace=0, scaledYSpace=0, scaledZSpace=0;
	double xSpacing=0, ySpacing=0, zSpacing=0;
	double zAxisScale=1;

	int baseWidth=0, baseHeight=0, baseDepth=0;
	int textureWidth=0, textureHeight=0, textureDepth=0, threshold=0;
	int texName[]=new int[1];

	int internalFormat=GL.GL_LUMINANCE_ALPHA;
	int pixelFormat=GL.GL_LUMINANCE_ALPHA;
	int pixelType=GL.GL_UNSIGNED_BYTE;
	int pixelOffset=2;

	double[][] clipPlanes={{-1,0,0,1.0001},{1,0,0,1.0001},{0,-1,0,1.0001},{0,1,0,1.0001},{0,0,-1,1.0001},{0,0,1,1.0001}};
	Point selectionPoint=new Point();
	int selectedObject=-1;

	float alpha=1.0f, size=1.0f;
	boolean textureInitialized=false, viewAligned=false, drawAxes=true, clipped=true, picking=false;
	ByteBuffer bb=null;
	ImageSequence is=null;
	LookupTableJAI table=null;

	public TextureVolume(String dir, String imageType, int threshold) {this(dir,imageType,threshold,0);}

	public TextureVolume(String dir, String imageType, int threshold, int seriesNumber) {

		ImageReader ir=ImageReaderManager.getInstance().getImageReader(imageType);
		ImageSequence files=ir.readDirectory(dir,true);
		ArrayList imageStudies=ir.organizeByStudy(files);
		if ((imageStudies!=null)&&(!imageStudies.isEmpty())) {
			ImageSequenceGroup isg=(ImageSequenceGroup)imageStudies.get(0);
			initialize((ImageSequence)isg.getSequence(seriesNumber),threshold);
		}
	}

	public TextureVolume(ImageSequence is, int threshold) {initialize(is,threshold);}

	// =======================================================================

	private void initialize(ImageSequence is, int threshold) {

		this.is=is; 
		this.threshold=threshold;

		if (is!=null) {
			Image i=is.get(0);
			WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i);
			table=WindowLevel.createLinearLuminanceAlphaLookupTable(wl,25,i.getMaxPixelValue(),255);
			double[] dim=i.getPixelDimensions();
			xSpacing=(dim!=null) ? dim[0] : 1;                 
			ySpacing=(dim!=null) ? dim[1] : 1;
			zSpacing=i.getImageSliceThickness();

			baseWidth=i.getWidth();                            
			baseHeight=i.getHeight();
			baseDepth=is.size();
			textureWidth=textureSize(baseWidth);
			textureHeight=textureSize(baseHeight);
			textureDepth=textureSize(baseDepth);

			double zScaleFactor=1f;
			if (CONSTRAIN_TEXTURE_SIZE) {
				if (textureWidth>TEXTURE_MAX_SIZE) {textureWidth=TEXTURE_MAX_SIZE;}
				if (textureHeight>TEXTURE_MAX_SIZE) {textureHeight=TEXTURE_MAX_SIZE;}
			}

			double min=Math.min(xSpacing,ySpacing);
			min=Math.min(min,zSpacing);
			scaledXSpace=xSpacing/min;
			scaledYSpace=ySpacing/min;
			scaledZSpace=(min==zSpacing) ? 1 : (zScaleFactor*zSpacing)/min;

			// System.err.println(xSpacing+" "+ySpacing+" "+zSpacing);
			// System.err.println(textureWidth+" "+textureHeight+" "+textureDepth);
			// System.err.println(scaledXSpace+" "+scaledYSpace+" "+scaledZSpace);
			// System.err.println(baseWidth+" "+baseHeight+" "+baseDepth);

			// Compute how far the stretching on the z-axis goes; this
			// should depend on whether x/y axis of the image scaling
			// factors. We may end up exceeding a 1x1x1 texture in the
			// mapping, but that's okay in the z-direction.

			double xLength=baseWidth*scaledXSpace;
			double yLength=baseHeight*scaledYSpace;
			double zLength=baseDepth*scaledZSpace;
			zAxisScale=(Math.max(xLength,yLength)>zLength) ? ((double)baseDepth/(double)textureDepth) : (zLength/Math.max(xLength,yLength)); 
		}
		loadTexture();
	}

	// =======================================================================

	private int textureSize(int value) {int retval=16; while (retval<value) retval*=2; return retval;}

	// =======================================================================

	private void loadTexture() {

		double scaleX=(double)textureWidth/(double)baseWidth;
		double scaleY=(double)textureHeight/(double)baseHeight;
		Rectangle rect=new Rectangle(0,0,textureWidth,textureHeight);
		bb=BufferUtil.newByteBuffer(pixelOffset*textureWidth*textureHeight*textureDepth);

		for (int loop=baseDepth-1; loop>=0; loop--) {
			Image i=is.get(loop);
			if ((scaleX==1)&&(scaleY==1)) {
				RenderedImage ri=JAI.create("lookup",i.getRenderedImage(),table);
				Raster r=ri.getData();
				RenderedImage[] src={ri};
				RasterAccessor ra=new RasterAccessor(r,rect,(RasterAccessor.findCompatibleTags(src,ri))[0],ri.getColorModel());
				bb.put(ra.getByteDataArray(0));
				if (ri instanceof PlanarImage) ((PlanarImage)ri).dispose();
				ri=null;
			} else if ((scaleX<1)||(scaleY<1)) {
				ParameterBlock pb=new ParameterBlock();
				pb.addSource(i.getRenderedImage());
				pb.add(AffineTransform.getScaleInstance(scaleX,scaleY));
				PlanarImage pi=JAI.create("affine",pb);
				RenderedImage ri=JAI.create("lookup",pi,table);
				Raster r=ri.getData();
				RenderedImage[] src={ri};
				RasterAccessor ra=new RasterAccessor(r,rect,(RasterAccessor.findCompatibleTags(src,ri))[0],ri.getColorModel());
				bb.put(ra.getByteDataArray(0));
				if (ri instanceof PlanarImage) ((PlanarImage)ri).dispose();
				pi.dispose();
				pi=null;
				ri=null;
			} else {
				int right=textureWidth-baseWidth;
				int bottom=textureHeight-baseHeight;
				ParameterBlock pb=new ParameterBlock();
				pb.addSource(i.getRenderedImage());
				pb.add(new Integer(0));
				pb.add(new Integer(right));
				pb.add(new Integer(0));
				pb.add(new Integer(bottom));
				pb.add(BorderExtender.createInstance(BorderExtender.BORDER_ZERO));
				PlanarImage pi=JAI.create("border",pb);
				RenderedImage ri=JAI.create("lookup",pi,table);
				Raster r=ri.getData();
				RenderedImage[] src={ri};
				RasterAccessor ra=new RasterAccessor(r,rect,(RasterAccessor.findCompatibleTags(src,ri))[0],ri.getColorModel());
				bb.put(ra.getByteDataArray(0));
				if (ri instanceof PlanarImage) ((PlanarImage)ri).dispose();
				pi.dispose();
				pi=null;
				r=null;
			}
		}
		bb.rewind();
	}
		
	// =======================================================================

	private void renderViewAligned(GL gl, float x, float y, float z) {

		// Set up the texture matrix for rotation in 3D accordingly; this
		// implements the view-aligned 3D texture process.  We should also
		// need to scale the texture based on the real-world dimensions
		// given in the image, and stretch it out so that the original
		// texture region covers the target region.
		
		double zStretch=(double)baseDepth/(double)textureDepth;
		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glScaled(1/scaledXSpace,1/scaledYSpace,1/(zStretch*scaledZSpace));
		gl.glTranslatef(0.5f,0.5f,0.5f);
		gl.glRotatef(z,0.0f,0.0f,1.0f);
		gl.glRotatef(-y,0.0f,1.0f,0.0f);
		gl.glRotatef(-x,1.0f,0.0f,0.0f);
		gl.glScaled(-1,-1,1);
		gl.glTranslatef(-0.5f,-0.5f,-0.5f);
		
		// Render the number of slices on the z-axis that we're using for
		// view alignment.  Because the depth has to be a power of 2, some
		// of the slices may be empty; skip these and render the volume
		// such that it fits appropriately.
		
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL.GL_FILL);		
		gl.glBegin(GL.GL_QUADS);
		for (float loop=1.5f; loop>=-0.5f; loop-=(1f/(float)(baseDepth))) {
			float t=-(loop+1.75f);
			gl.glTexCoord3f(-0.5f,-0.5f,loop); gl.glVertex3f(-size,-size,t); 
			gl.glTexCoord3f(1.5f,-0.5f,loop); gl.glVertex3f(size,-size,t);
			gl.glTexCoord3f(1.5f,1.5f,loop); gl.glVertex3f(size,size,t);
			gl.glTexCoord3f(-0.5f,1.5f,loop); gl.glVertex3f(-size,size,t);
		}
		gl.glEnd();
		gl.glPopMatrix();

		gl.glMatrixMode(GL.GL_TEXTURE);
		gl.glLoadIdentity();
		gl.glMatrixMode(GL.GL_MODELVIEW);
	}

	// =======================================================================

	private void renderSlice(GL gl, float t, float c, int axis) {

		float zScale=(float)baseDepth/(float)textureDepth;
		float[][][] texCoords={{{t,0,0},{t,1,0},{t,1,zScale},{t,0,zScale}},
													 {{0,t,0},{1,t,0},{1,t,zScale},{0,t,zScale}},
													 {{0,0,t},{1,0,t},{1,1,t},{0,1,t}}};

		float zStretch=(float)zAxisScale;
		// float zStretch=-2.75f+(2f*(float)zAxisScale);
		
		float[][][] coordinates={{{-c,-1f,zStretch},{-c,1f,zStretch},{-c,1f,-zStretch},{-c,-1f,-zStretch}},
														 {{-1f,-c,zStretch},{1f,-c,zStretch},{1f,-c,-zStretch},{-1f,-c,-zStretch}},
														 {{-1f,-1f,c},{1f,-1f,c},{1f,1f,c},{-1f,1f,c}}};

		// float[][][] coordinates={{{-c,-1f,-0.75f},{-c,1f,-0.75f},{-c,1f,-2.75f},{-c,-1f,-2.75f}},
		//												 {{-1f,-c,-0.75f},{1f,-c,-0.75f},{1f,-c,-2.75f},{-1f,-c,-2.75f}},
		//												 {{-1f,-1f,c},{1f,-1f,c},{1f,1f,c},{-1f,1f,c}}};

		gl.glTexCoord3f(texCoords[axis][0][0],texCoords[axis][0][1],texCoords[axis][0][2]);
		gl.glVertex3f(coordinates[axis][0][0],coordinates[axis][0][1],coordinates[axis][0][2]);
		gl.glTexCoord3f(texCoords[axis][1][0],texCoords[axis][1][1],texCoords[axis][1][2]);
		gl.glVertex3f(coordinates[axis][1][0],coordinates[axis][1][1],coordinates[axis][1][2]);
		gl.glTexCoord3f(texCoords[axis][2][0],texCoords[axis][2][1],texCoords[axis][2][2]);
		gl.glVertex3f(coordinates[axis][2][0],coordinates[axis][2][1],coordinates[axis][2][2]);
		gl.glTexCoord3f(texCoords[axis][3][0],texCoords[axis][3][1],texCoords[axis][3][2]);
		gl.glVertex3f(coordinates[axis][3][0],coordinates[axis][3][1],coordinates[axis][3][2]);
	}

	// =======================================================================

	private void renderObjectAligned(GL gl, float x, float y, float z) {

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glScaled(1/scaledXSpace,1/scaledYSpace,1/scaledZSpace);
		gl.glTranslatef(0.0f,0.0f,-3f);
		gl.glRotatef(x,1.0f,0.0f,0.0f);
		gl.glRotatef(y,0.0f,1.0f,0.0f);
		gl.glRotatef(z,0.0f,0.0f,1.0f);
		gl.glScalef(-size,-size,size);

		gl.glBegin(GL.GL_QUADS);
		double angleX=Math.cos(x*DEG2RAD)*Math.sin(y*DEG2RAD);
		double angleY=-Math.sin(x*DEG2RAD);
    double angleZ=-Math.cos(x*DEG2RAD)*Math.cos(y*DEG2RAD);
		float reslice=(float)Math.max(baseWidth,baseHeight);         // Avoid artifacting as much as possible.
		reslice=Math.max(reslice,baseDepth);

		if ((Math.abs(angleX)>=Math.abs(angleY))&&(Math.abs(angleX)>=Math.abs(angleZ))) {
			if (angleX>0) {
				for (float loop=0f; loop<1f; loop+=1f/reslice) renderSlice(gl,loop,-((2f*loop)-1f),0);
			} else {
				for (float loop=1f; loop>=0f; loop-=1f/reslice) renderSlice(gl,loop,-((2f*loop)-1f),0);
			}
		} else if (Math.abs(angleY)>=Math.abs(angleZ)) {
			if (angleY>0) {
				for (float loop=0f; loop<1f; loop+=1f/reslice) renderSlice(gl,loop,-((2f*loop)-1f),1);
			 } else {
				for (float loop=1f; loop>=0f; loop-=1f/reslice) renderSlice(gl,loop,-((2f*loop)-1f),1);
			} 
		} else {
			float scale=(float)textureDepth/(float)baseDepth;
			if (angleZ>0) {
				for (float loop=0f; loop<1f; loop+=1f/reslice) renderSlice(gl,loop,(float)zAxisScale-(float)(2*loop*scale*zAxisScale),2);
			} else {
				for (float loop=1f; loop>=0f; loop-=1f/reslice) renderSlice(gl,loop,(float)zAxisScale-(float)(2*loop*scale*zAxisScale),2);
			}
		}
		gl.glEnd();
	}

	// =======================================================================

	private void renderAxes(GL gl, float x, float y, float z, float d1, float d2) {

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glTranslatef(0f,0f,-3f);
		gl.glRotatef(x,1.0f,0.0f,0.0f);
		gl.glRotatef(y,0.0f,1.0f,0.0f);
		gl.glRotatef(z,0.0f,0.0f,1.0f);
		
		gl.glLineWidth(1);
		gl.glBegin(GL.GL_LINES);
		gl.glColor4f(1f,0,0,1f); gl.glVertex3f(d1,0,0);	gl.glVertex3f(d2,0,0);
		gl.glColor4f(0,1f,0f,1f);	gl.glVertex3f(0,d1,0); gl.glVertex3f(0,d2,0);
		gl.glColor4f(0,0,1f,1f); gl.glVertex3f(0,0,d1);	gl.glVertex3f(0,0,d2);
		gl.glEnd();
		gl.glColor4f(1.0f,1.0f,1.0f,1.0f);
	}

	// =======================================================================

	private void renderClipPlanes(GL gl, float x, float y, float z, int matrixMode) {

		/*		gl.glEnable(GL.GL_CLIP_PLANE0);
		gl.glEnable(GL.GL_CLIP_PLANE1);
		gl.glEnable(GL.GL_CLIP_PLANE2);
		gl.glEnable(GL.GL_CLIP_PLANE3);
		gl.glEnable(GL.GL_CLIP_PLANE4);
		gl.glEnable(GL.GL_CLIP_PLANE5);*/

		double zStretch=(double)baseDepth/(double)textureDepth;
		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();
		gl.glScaled(1/scaledXSpace,1/scaledYSpace,1/(zStretch*scaledZSpace));
		// gl.glTranslatef(0f,0f,-2.5f);
		gl.glRotatef(x,1.0f,0.0f,0.0f);
		gl.glRotatef(y,0.0f,1.0f,0.0f);
		gl.glRotatef(z,0.0f,0.0f,1.0f);
		gl.glScalef(-size,-size,size);

		double x0=clipPlanes[0][3];
		double x1=-clipPlanes[1][3];
		double y0=clipPlanes[2][3];
		double y1=-clipPlanes[3][3];
		double z0=clipPlanes[4][3];
		double z1=-clipPlanes[5][3];

		if (matrixMode==GL.GL_SELECT) gl.glLoadName(X0); // Draw the X planes
		gl.glBegin(GL.GL_QUADS);
		gl.glColor4f(1.0f,0,0,0.2f);
		gl.glVertex3d(x0,y0,z0); 	      
		gl.glVertex3d(x0,y1,z0);
		gl.glVertex3d(x0,y1,z1);
		gl.glVertex3d(x0,y0,z1);
		gl.glEnd();

		if (matrixMode==GL.GL_SELECT) gl.glLoadName(X1);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(x1,y0,z0);
		gl.glVertex3d(x1,y1,z0);
		gl.glVertex3d(x1,y1,z1);
		gl.glVertex3d(x1,y0,z1);
		gl.glEnd();
		
		if (matrixMode==GL.GL_SELECT) gl.glLoadName(Y0); // Draw the Y planes
		gl.glBegin(GL.GL_QUADS);
		gl.glColor4f(0,1,0,0.2f);	    
		gl.glVertex3d(x0,y0,z0);
		gl.glVertex3d(x1,y0,z0);
		gl.glVertex3d(x1,y0,z1);
		gl.glVertex3d(x0,y0,z1);
		gl.glEnd();

		if (matrixMode==GL.GL_SELECT) gl.glLoadName(Y1);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(x0,y1,z0);
		gl.glVertex3d(x1,y1,z0);
		gl.glVertex3d(x1,y1,z1);
		gl.glVertex3d(x0,y1,z1);
		gl.glEnd();

		if (matrixMode==GL.GL_SELECT) gl.glLoadName(Z0); // Draw the Z planes
		gl.glBegin(GL.GL_QUADS);
		gl.glColor4f(0,0,1,0.2f);		   
		gl.glVertex3d(x0,y0,z0);
		gl.glVertex3d(x1,y0,z0);
		gl.glVertex3d(x1,y1,z0);
		gl.glVertex3d(x0,y1,z0);
		gl.glEnd();

		if (matrixMode==GL.GL_SELECT) gl.glLoadName(Z1);
		gl.glBegin(GL.GL_QUADS);
		gl.glVertex3d(x0,y0,z1);
		gl.glVertex3d(x1,y0,z1);
		gl.glVertex3d(x1,y1,z1);
		gl.glVertex3d(x0,y1,z1);
		gl.glEnd();

		gl.glColor4f(1.0f,1.0f,1.0f,1.0f);

		/*gl.glClipPlane(GL.GL_CLIP_PLANE0,clipPlanes[0],0);
		gl.glClipPlane(GL.GL_CLIP_PLANE1,clipPlanes[1],0);
		gl.glClipPlane(GL.GL_CLIP_PLANE2,clipPlanes[2],0);
		gl.glClipPlane(GL.GL_CLIP_PLANE3,clipPlanes[3],0);
		gl.glClipPlane(GL.GL_CLIP_PLANE4,clipPlanes[4],0);
		gl.glClipPlane(GL.GL_CLIP_PLANE5,clipPlanes[5],0);*/
	}

	// =======================================================================

	private void pick(GL gl, GLU glu, float x, float y, float z) {
		
		// Set up the selection mode, matrix/stack, and draw the clip
		// planes accordingly in this pick mode. Then switch back to
		// normal rendering mode and render them again.
		
		int[] selectBuf=new int[512];
		IntBuffer selectBuffer=BufferUtil.newIntBuffer(512);
		int viewport[]=new int[4];
		
		gl.glGetIntegerv(GL.GL_VIEWPORT,viewport,0);
		gl.glSelectBuffer(512,selectBuffer);
		gl.glRenderMode(GL.GL_SELECT);
		gl.glInitNames();
		gl.glPushName(-1);
		
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
		gl.glLoadIdentity();
		glu.gluPickMatrix((double)selectionPoint.x,(double)(viewport[3]-selectionPoint.y),5.0,5.0,viewport,0);
		glu.gluPerspective(45.0,1.0,0.001,20.0);
				
		// renderClipPlanes(gl,x,y,z,GL.GL_SELECT);
		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();
				
		int hits=gl.glRenderMode(GL.GL_RENDER);
		if (hits==0) return;

		selectBuffer.get(selectBuf);
		int minDistance=Integer.MAX_VALUE;
		int minName=-1;
    for (int loop=0, ptr=0; loop<hits; loop++) {
			int names=selectBuf[ptr++];
			int minZ=selectBuf[ptr++];
			int maxZ=selectBuf[ptr++];
			if (minZ<minDistance) {minDistance=minZ; minName=selectBuf[ptr];}
			ptr+=names;
		}
		selectedObject=minName;
	}

	// =======================================================================

  public void render(GL gl, GLU glu, float x, float y, float z) {

		if (!textureInitialized) {
			gl.glGenTextures(1,texName,0);
			gl.glBindTexture(GL.GL_TEXTURE_3D,texName[0]);
			gl.glTexImage3D(GL.GL_TEXTURE_3D,0,GL.GL_LUMINANCE_ALPHA,textureWidth,textureHeight,textureDepth,0,GL.GL_LUMINANCE_ALPHA,GL.GL_UNSIGNED_BYTE,bb);
			gl.glTexParameteri(GL.GL_TEXTURE_3D,GL.GL_TEXTURE_WRAP_S,GL.GL_CLAMP_TO_BORDER);
			gl.glTexParameteri(GL.GL_TEXTURE_3D,GL.GL_TEXTURE_WRAP_T,GL.GL_CLAMP_TO_BORDER);
			gl.glTexParameteri(GL.GL_TEXTURE_3D,GL.GL_TEXTURE_WRAP_R,GL.GL_CLAMP_TO_BORDER);
			gl.glTexParameteri(GL.GL_TEXTURE_3D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR);
			gl.glTexParameteri(GL.GL_TEXTURE_3D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR);
			textureInitialized=true;
		} 

		if (alpha==0) return;

		// Use the GL_MODULATE texture function to effectively multiply
		// each pixel in the texture by the current alpha value (this
		// controls the opacity)
			
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		gl.glTexEnvi(GL.GL_TEXTURE_ENV,GL.GL_TEXTURE_ENV_MODE,GL.GL_MODULATE);
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK,GL.GL_FILL);		
		gl.glEnable(GL.GL_BLEND);
		gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glPixelStorei(GL.GL_UNPACK_ALIGNMENT,pixelOffset);
		if (drawAxes) renderAxes(gl,x,y,z,-10,10);
		gl.glEnable(GL.GL_TEXTURE_3D);
		if (viewAligned) renderViewAligned(gl,x,y,z); else renderObjectAligned(gl,x,y,z);
 		gl.glDisable(GL.GL_TEXTURE_3D);
		if (clipped) {
			if (picking) {pick(gl,glu,x,y,z);	picking=false;}
			gl.glRenderMode(GL.GL_RENDER);
			gl.glDepthMask(false);
			gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glMatrixMode(GL.GL_MODELVIEW);
			// renderClipPlanes(gl,x,y,z,GL.GL_RENDER);
			gl.glDepthMask(true);
		}
		gl.glDisable(GL.GL_BLEND);
		gl.glFlush();
	}

	// =======================================================================

	public float getAlpha() {return alpha;}
	public float getScale() {return size;}

	public boolean isTextureInitialized() {return textureInitialized;}
	public boolean isViewAligned() {return viewAligned;}
	public boolean isClipped() {return clipped;}
	public boolean isPicking() {return picking;}

	public Point getSelectionPoint() {return selectionPoint;}

	public void setAlpha(float alpha) {this.alpha=alpha;}
	public void setClipped(boolean x) {clipped=x;}
	public void setPicking(boolean x) {picking=x;}
	public void setScale(float size) {this.size=size;}
	public void setViewAligned(boolean x) {viewAligned=x;}
	public void setTextureInitialized(boolean x) {textureInitialized=x;}
	public void setSelectionPoint(Point x) {selectionPoint=x;}
	public void setSelectionPoint(int x, int y) {selectionPoint.x=x; selectionPoint.y=y;}

}
