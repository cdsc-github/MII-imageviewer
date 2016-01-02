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

package imageviewer.ui.animation;

import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.sun.opengl.util.j2d.TextureRenderer;

import net.java.joglutils.msg.actions.GLRenderAction;
import net.java.joglutils.msg.actions.GLResetAction;
import net.java.joglutils.msg.actions.RayPickAction;
import net.java.joglutils.msg.collections.Vec2fCollection;
import net.java.joglutils.msg.collections.Vec3fCollection;
import net.java.joglutils.msg.collections.Vec4fCollection;

import net.java.joglutils.msg.math.Rotf;
import net.java.joglutils.msg.math.Vec2f;
import net.java.joglutils.msg.math.Vec3f;
import net.java.joglutils.msg.math.Vec4f;
import net.java.joglutils.msg.misc.Path;
import net.java.joglutils.msg.misc.PickedPoint;
import net.java.joglutils.msg.misc.SystemTime;

import net.java.joglutils.msg.nodes.Blend;
import net.java.joglutils.msg.nodes.Color4;
import net.java.joglutils.msg.nodes.Coordinate3;
import net.java.joglutils.msg.nodes.PerspectiveCamera;
import net.java.joglutils.msg.nodes.Separator;
import net.java.joglutils.msg.nodes.Texture2;
import net.java.joglutils.msg.nodes.TextureCoordinate2;
import net.java.joglutils.msg.nodes.Transform;
import net.java.joglutils.msg.nodes.TriangleSet;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;

// =======================================================================
/* A 3D display shelf component which can be plugged in to any JOGL
 * GLAutoDrawable implementation (heavyweight or lightweight,
 * on-screen or off-screen). Uses Swing's ListModel for the image
 * descriptors as per the {@link org.jdesktop.iris.model.ImageBrowser
 * ImageBrowser} interface. Performs 3D selection internally and fires
 * events affecting the SelectionModel as necessary. <P><p/> In order
 * for the SlideShowRenderer to operate properly, a persistent OpenGL
 * context must be shared with the containing OpenGL component so that
 * repeated calls to init() do not require repeated re-loads of
 * textures.
 *
 * @author Kenneth Russell
 */

public class DisplayShelfRenderer implements GLEventListener {

	private static float DEFAULT_ASPECT_RATIO=0.665f;
	private static float DEFAULT_HEIGHT=1.5f;
	private static float DEFAULT_ON_SCREEN_FRAC=0.5f;
	private static float EDITING_ON_SCREEN_FRAC=0.95f;
	private static float FADE_FRAC=0.3f;      	                           // The fraction of the height used to fade out the reflections
	private static float FADE_INTENSITY=0.5f; 	                           // The intensity of the reflection at the start (0=no reflection, 1=intense)
	private static float STACKED_SPACING_FRAC=0.5f;
	private static float SELECTED_SPACING_FRAC=1.0f;
	private static float DEFAULT_EDITED_SPACING_FRAC=1.5f;
	private static float SINGLE_IMAGE_MODE_RAISE_FRAC=0.0f;

	private static final float EPSILON=1.0e-4f;
	private static final float ANIM_SCALE_FACTOR=9.0f;
	private static final float ROT_ANGLE=(float)Math.toRadians(75);
	private static final float ONE_OVER_ROOT_2_PI=(float)(1.0f/Math.sqrt(2*Math.PI));

	// =======================================================================

	float editedSpacingFrac=DEFAULT_EDITED_SPACING_FRAC; 	                 // This is adjusted dynamically based on the aspect ratio of the window
	float offsetFrac;

	PerspectiveCamera camera=null;

	volatile ArrayList<AnimationListener> animationListeners=new ArrayList<AnimationListener>();
	Separator root=null, imageRoot=null, mirrorRoot=null;
	boolean firstInit=true, texturesArePersistent=false, floorCoordsDirty=false;
	boolean animating=false, animationScheduled=false, singleImageMode=false;
	GLAutoDrawable drawable=null;
	ListModel model=null;
	ListSelectionModel selectionModel=null;
	List<ImageGraph> graphs=new ArrayList<ImageGraph>();
	List<Texture2> texturesToDispose=new ArrayList<Texture2>();
	TriangleSet tris=null;

	float[] gradientTop=null, gradientBottom=null;
	Color4 mirrorColors, floorColors;
	Coordinate3 floorCoords;
	GLRenderAction ra=new GLRenderAction();
	Vec2f currentYZ=new Vec2f(), targetYZ=new Vec2f();
	int targetIndex=0;
	float currentIndex=0, viewingZ=0f, editingZ=0f;
	SystemTime time=null;
	Fetcher f=null;

	Texture2 clockTexture;
	volatile boolean imagesDirty=false, doneLoading=false;
	volatile Animator clockAnimator=null;
  volatile float clockFraction=0f;

	DownloadListener downloadListener=new DownloadListener();
	DataListener dataListener=new DataListener();
	SelectionListener selectionListener=new SelectionListener();

	// =======================================================================
	// Creates a DisplayShelfRenderer with the specified Fetcher and
	// with a DefaultListModel as its data model. 

	public DisplayShelfRenderer(Fetcher f) {
		this(f,new DefaultListModel(),new DefaultListSelectionModel(),false);
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().setSelectionInterval(0,0);
	}

	public DisplayShelfRenderer(Fetcher f, ListModel model, ListSelectionModel selectionModel, boolean texturesArePersistent) {

		this.f=f;
		this.texturesArePersistent=texturesArePersistent;
		root=new Separator();
		clockTexture=new Texture2();
		time=new SystemTime();
		time.rebase();
		imageRoot=new Separator();
		mirrorRoot=new Separator();
		tris=new TriangleSet();
		tris.setNumTriangles(4);
		mirrorColors=new Color4();
		Vec4fCollection colors=new Vec4fCollection();
		for (int i=0; i<12; i++) colors.add(new Vec4f(0,0,0,0));
		mirrorColors.setData(colors);
		floorCoords=new Coordinate3();
		floorCoords.setData(new Vec3fCollection());
		for (int i=0; i<6; i++) floorCoords.getData().add(new Vec3f(0,0,0));

		floorColors=new Color4();
		floorColors.setData(new Vec4fCollection());
		for (int i=0; i<6; i++) floorColors.getData().add(new Vec4f(0,0,0,0));

		camera=new PerspectiveCamera();
		camera.setNearDistance(1.0f);
		camera.setFarDistance(100.0f);
		camera.setHeightAngle((float) Math.PI/8);

		viewingZ=0.5f*DEFAULT_HEIGHT/(DEFAULT_ON_SCREEN_FRAC*(float)Math.tan(camera.getHeightAngle()));
		offsetFrac=(float)(((3*Math.PI/40)/camera.getHeightAngle()) + 0.1f);

		Separator mirrorParent=new Separator();
		Transform mirrorXform=new Transform();
		mirrorXform.getTransform().set(1,1,-1);
		mirrorParent.addChild(mirrorXform);
		mirrorParent.addChild(mirrorColors);

		Blend blend=new Blend();
		blend.setEnabled(true);
		blend.setSourceFunc(Blend.ONE);
		blend.setDestFunc(Blend.ONE_MINUS_SRC_ALPHA);
		mirrorParent.addChild(blend);
		mirrorParent.addChild(mirrorRoot);

		Separator floorRoot=new Separator();
		floorRoot.addChild(blend);
		floorRoot.addChild(floorCoords);
		floorCoordsDirty=true;
		floorRoot.addChild(floorColors);
		TriangleSet floorTris=new TriangleSet();
		floorTris.setNumTriangles(2);
		floorRoot.addChild(floorTris);

		root.addChild(camera);
		root.addChild(imageRoot);
		root.addChild(mirrorParent);
		root.addChild(floorRoot);

		setModel(model);
		setSelectionModel(selectionModel);
	}

	// =======================================================================
	/** Switches to single image mode, viewing the current selected image. */

	public void setSingleImageMode(boolean singleImageMode, boolean animateTransition) {

		this.singleImageMode=singleImageMode;
		resetAnimation(animateTransition);
		if (getModel()!=null &&	getSelectedIndex()<getModel().getSize()) {
			ImageGraph graph=graphs.get(getSelectedIndex()); 			                     // Boost the priority of the image being viewed
			if (!graph.downloaded && graph.downloading) {
				graph.downloading=false;
				imagesDirty=true;
			}
		}
		if (drawable!=null) drawable.repaint();
	}

	// =======================================================================
	/** Indicates whether this component is in single image mode. */

	public boolean getSingleImageMode() {return singleImageMode;}

	// =======================================================================
	/** Adds an animation listener to this display shelf, which will be
	 * called when any animation is complete. */

	public synchronized void addAnimationListener(AnimationListener listener) {

		ArrayList<AnimationListener> newListeners=new ArrayList<AnimationListener>();
		newListeners.addAll(animationListeners);
		newListeners.add(listener);
		animationListeners=newListeners;
	}

	// =======================================================================
	/** Removes an animation listener from this display shelf. */

	public synchronized void removeAnimationListener(AnimationListener listener) {

		ArrayList<AnimationListener> newListeners=new ArrayList<AnimationListener>();
		newListeners.addAll(animationListeners);
		newListeners.remove(listener);
		animationListeners=newListeners;
	}

	// =======================================================================

	public void setModel(ListModel model) {

		ListModel oldModel=this.model;
		if (oldModel!=null) {
			oldModel.removeListDataListener(dataListener);
			for (int i=0; i<oldModel.getSize(); i++) removeGraph(0);
		}

		for (int i=0; i<model.getSize(); i++) {
			ImageGraph graph=createImageGraph(model.getElementAt(i));
			insertGraph(i,graph);
		}

		imagesDirty=true;
		this.model=model;
		model.addListDataListener(dataListener);
		resetAnimation(animating);
		if (drawable!=null)	drawable.repaint();
	}

	// =======================================================================

	public ListModel getModel() {return model;}
	public void addListSelectionListener(ListSelectionListener listener) {getSelectionModel().addListSelectionListener(listener);}
	public void removeListSelectionListener(ListSelectionListener listener) {getSelectionModel().removeListSelectionListener(listener);}

	// =======================================================================

	public void setSelectedIndex(int index) {

		if (targetIndex==index)	return;
		this.targetIndex=index;
		selectionModel.setSelectionInterval(index,index);
		resetAnimation(true);
		ImageGraph graph=graphs.get(index);
		if (!graph.downloaded && graph.downloading) {
			graph.downloading=false;
			imagesDirty=true;
		}
		if (drawable!=null) drawable.repaint();
	}

	public int getSelectedIndex() {return targetIndex;}

	public ListSelectionModel getSelectionModel() {return selectionModel;}

	public void setSelectionModel(ListSelectionModel selectionModel) {
		ListSelectionModel oldModel=this.selectionModel;
		if (oldModel!=null) oldModel.removeListSelectionListener(selectionListener);
		this.selectionModel=selectionModel;
		selectionModel.addListSelectionListener(selectionListener);
	}

	// =======================================================================
	// Implementation of the GLEventListener interface

	public void init(GLAutoDrawable drawable) {

		this.drawable=drawable;
		GL gl=drawable.getGL();
		if (firstInit || !texturesArePersistent) {
			GLResetAction reset=new GLResetAction();
			reset.apply(root);
			clockTexture.initTextureRenderer((int)(300*DEFAULT_HEIGHT*DEFAULT_ASPECT_RATIO),(int)(300*DEFAULT_HEIGHT),false);
			for (ImageGraph graph : graphs) graph.downloaded=false;
		}

		if (firstInit) {
			drawable.addMouseListener(new MListener());
			drawable.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					switch (e.getKeyCode()) {
						case KeyEvent.VK_SPACE:	setSingleImageMode(!getSingleImageMode(),true); break;
						case KeyEvent.VK_ENTER:	setSingleImageMode(!getSingleImageMode(),false); break;
						 case KeyEvent.VK_LEFT:	setSelectedIndex(Math.max(0,targetIndex-1)); break;
						case KeyEvent.VK_RIGHT:	setSelectedIndex(Math.min(graphs.size()-1,targetIndex+1)); break;
					}
				}});

			clockAnimator=new Animator(4000,Animator.INFINITE,Animator.RepeatBehavior.LOOP,new TimingTarget() {
				public void begin() {}
				public void end() {}
				public void repeat() {}
				public void timingEvent(float fraction) {
					clockFraction=fraction;
					if (DisplayShelfRenderer.this.drawable!=null) DisplayShelfRenderer.this.drawable.repaint();
				}
			});

			clockAnimator.setResolution(4000/60);
			clockAnimator.start();
		}
		firstInit=false;
	}

	// =======================================================================

	public void display(GLAutoDrawable drawable) {

		while (texturesToDispose.size()>0) {
			Texture2 texture=texturesToDispose.remove(texturesToDispose.size()-1);
			texture.dispose();
		}
		if (imagesDirty) {
			imagesDirty=false;
			for (int i=graphs.size()-1; i>=0; --i) {
				ImageGraph graph=graphs.get(i);
				if (!graph.downloaded) updateImage(graph);
			}
		}
		boolean repaintAgain=false;
		boolean updateNow=false;
		synchronized (this) {
			if (animationScheduled || animating) {
				animationScheduled=false;
				updateNow=true;
			}
		}
		if (updateNow) repaintAgain=updateAnimation(false, false);
		if (!doneLoading) {
			TextureRenderer rend=clockTexture.getTextureRenderer();
			Graphics2D g=rend.createGraphics();
			Clock.draw(g,Color.DARK_GRAY,Color.GRAY,(int)(clockFraction*60),0,0,rend.getWidth(),rend.getHeight());
			g.dispose();
			rend.markDirty(0,0,rend.getWidth(),rend.getHeight());
		}
		GL gl=drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		ra.apply(root);

		if (repaintAgain) {
			animating=true;
			if (drawable!=null) drawable.repaint();
		} else {
			if (animating) {
				animating=false;
				fireAnimationComplete();
			}
		}
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {recomputeEditedSpacingFrac();}
	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

	// =======================================================================

	private void computeCoords(ImageGraph graph) {

		Coordinate3 coordNode=graph.coords;
		float aspectRatio=graph.aspectRatio;
		Vec3fCollection coords=coordNode.getData();
		if (coords==null) {
			coords=new Vec3fCollection();
			Vec3f zero=new Vec3f();
			for (int i=0; i<12; i++) coords.add(zero);
			coordNode.setData(coords);
		}

		// Now compute the actual values

		Vec3f lowerLeft=new Vec3f(-0.5f*DEFAULT_HEIGHT*aspectRatio,0,0);
		Vec3f lowerRight=new Vec3f(0.5f*DEFAULT_HEIGHT*aspectRatio,0,0);
		Vec3f upperLeft=new Vec3f(-0.5f*DEFAULT_HEIGHT*aspectRatio,DEFAULT_HEIGHT,0);
		Vec3f upperRight=new Vec3f(0.5f*DEFAULT_HEIGHT*aspectRatio,DEFAULT_HEIGHT,0);

		// These additional vertices are only used in the mirrored
		// geometry to affect how quickly the reflection fades out

		Vec3f midLeft=lowerLeft.times(1.0f-FADE_FRAC).plus(upperLeft.times(FADE_FRAC));
		Vec3f midRight=lowerRight.times(1.0f-FADE_FRAC).plus(upperRight.times(FADE_FRAC));

		coords.set(0,upperRight);
		coords.set(1,upperLeft);
		coords.set(2,midLeft);
		coords.set(3,upperRight);
		coords.set(4,midLeft);
		coords.set(5,midRight);
		coords.set(6,midRight);
		coords.set(7,midLeft);
		coords.set(8,lowerLeft);
		coords.set(9,midRight);
		coords.set(10,lowerLeft);
		coords.set(11,lowerRight);
	}

	// =======================================================================
	// Creates the ImageGraph to be associated with the given
	// ImageDescriptor. Initially sets up the Texture of the sub-graph
	// to point to the animating clock texture. This is replaced later
	// when the Fetcher calls us back.

	private ImageGraph createImageGraph(Object imageDescriptor) {

		ImageGraph graph=new ImageGraph(imageDescriptor);
		graph.aspectRatio=DEFAULT_ASPECT_RATIO;
		computeCoords(graph);
		Separator sep=graph.sep;
		sep.addChild(graph.xform);
		sep.addChild(graph.coords);
		sep.addChild(clockTexture);
		TextureCoordinate2 texCoordNode=new TextureCoordinate2();
		Vec2fCollection texCoords=new Vec2fCollection();
		texCoords.add(new Vec2f(1,1));
		texCoords.add(new Vec2f(0,1));
		texCoords.add(new Vec2f(0,FADE_FRAC));
		texCoords.add(new Vec2f(1,1));
		texCoords.add(new Vec2f(0,FADE_FRAC));
		texCoords.add(new Vec2f(1,FADE_FRAC));
		texCoords.add(new Vec2f(1,FADE_FRAC));
		texCoords.add(new Vec2f(0,FADE_FRAC));
		texCoords.add(new Vec2f(0,0));
		texCoords.add(new Vec2f(1,FADE_FRAC));
		texCoords.add(new Vec2f(0,0));
		texCoords.add(new Vec2f(1,0));
		texCoordNode.setData(texCoords);
		sep.addChild(texCoordNode);
		sep.addChild(tris);
		return graph;
	}

	// =======================================================================
	// Causes an ImageGraph to be inserted in the graph list and the
	// scene graph at the specified index, shifting any existing
	// graphs to the right. This is used to reflect the insertion of
	// elements in the model.

	private void insertGraph(int index, ImageGraph graph) {

		graphs.add(index,graph);
		imageRoot.insertChild(index,graph.sep);
		mirrorRoot.insertChild(index,graph.sep);
		if (index<targetIndex) targetIndex+=1;
		if (index<currentIndex) currentIndex+=1;
	}

	// =======================================================================
	// Causes the ImageGraph at the specified index to be removed from
	// the graph list and its associated Texture2 node properly
	// disposed.

	private void removeGraph(int index) {

		ImageGraph graph=graphs.remove(index);
		imageRoot.removeChild(graph.sep);
		mirrorRoot.removeChild(graph.sep);
		texturesToDispose.add(graph.texture);
		if (index<targetIndex) targetIndex-=1;
		if (index<currentIndex) currentIndex-=1;
	}

	// =======================================================================

	private void updateImage(ImageGraph graph) {

		if (graph.downloaded) return;
		if (graph.downloading) return;
		graph.downloading=true;
		int width=Math.min(500,OpenGLInfo.getMaxTextureSize());
		int height=Math.min(300,OpenGLInfo.getMaxTextureSize());
		BufferedImage img=f.getImage(graph.imageDescriptor,width,height,Fetcher.NORMAL_PRIORITY,downloadListener,graph);
		if (img!=null) {
			graph.downloaded=true;
			graph.sep.replaceChild(clockTexture,graph.texture);
			graph.texture.setTexture(img,false);
			graph.aspectRatio=(float)img.getWidth()/(float)img.getHeight();
			computeCoords(graph);
			recomputeEditedSpacingFrac();
			if (!animating) updateAnimation(false, true);
			if (drawable!=null) drawable.repaint();
		}

		boolean done=true;
		for (ImageGraph cur : graphs) {if (!cur.downloaded) {done=false; break;}}
		if (done) {doneLoading=true; synchronized (this) {clockAnimator.cancel();}}
	}

	// =======================================================================
	// This method resets the animation state of the display shelf,
	// cancelling any old Animator, if necessary, setting up the start
	// and target parameters, and preparing (but not starting) a new
	// Animator. The animator is started once we start rendering; this
	// handles the case of the DisplayShelf being added the first
	// time, when the first paint takes a long time due to OpenGL
	// texture uploading.

	private synchronized void resetAnimation(boolean animate) {

		if (singleImageMode) {

			// Compute a target Y and Z depth based on the image we want to view
			// FIXME: right now the Y and Z targets are always the same, but
			// once we adjust the images to fit within a bounding square,
			// they won't be

			editingZ=0.5f*DEFAULT_HEIGHT/(EDITING_ON_SCREEN_FRAC*(float)Math.tan(camera.getHeightAngle()));
			targetYZ.set((0.5f+SINGLE_IMAGE_MODE_RAISE_FRAC)*DEFAULT_HEIGHT,editingZ);
		} else {
			targetYZ.set(0.5f*DEFAULT_HEIGHT,viewingZ);
		}

		if (!animate) {
			currentYZ.set(targetYZ);
			currentIndex=targetIndex;
			updateAnimation(true, false);
			fireAnimationComplete();
			animating=false;
		} else {
			animationScheduled=true;
		}
	}

	// =======================================================================

	private void recomputeEditedSpacingFrac() {

		// Need to recompute the spacing fraction when in single image
		// editing mode so that we don't see other images no matter
		// what their aspect ratios

		float maxAspectRatio=DEFAULT_ASPECT_RATIO;
		for (ImageGraph graph : graphs) maxAspectRatio=Math.max(maxAspectRatio,graph.aspectRatio);
		float viewportAspectRatio=DEFAULT_ASPECT_RATIO;
		if (drawable!=null) viewportAspectRatio=(float)drawable.getWidth()/(float)drawable.getHeight();
		editedSpacingFrac=1.2f*Math.max(maxAspectRatio,viewportAspectRatio);
		if (!animating) updateAnimation(false,false);
	}

	// =======================================================================
	// This is the main routine which recomputes the positions of all
	// of the geometry on the shelf

	private boolean updateAnimation(boolean jumpToEnd, boolean forceLayout) {

		boolean needsMoreUpdates=false;
		if (!animating) time.rebase();
		if (jumpToEnd) {
			currentIndex=targetIndex;
			currentYZ.set(targetYZ.x(),targetYZ.y());
		} else {
			if (!forceLayout && Math.abs(targetIndex-currentIndex)<EPSILON &&	Math.abs(targetYZ.x()-currentYZ.x())<EPSILON &&
					Math.abs(targetYZ.y()-currentYZ.y())<EPSILON) {
				if (animating) updateAnimation(true,false);
				return false;
			}
			needsMoreUpdates=true;
			time.update();
			float deltaT=(float) time.deltaT();
			if (deltaT*ANIM_SCALE_FACTOR>1.0f) deltaT=0.9f/ANIM_SCALE_FACTOR;
			currentIndex=currentIndex+(targetIndex-currentIndex)*deltaT*ANIM_SCALE_FACTOR;
			currentYZ.set(currentYZ.x()+(targetYZ.x()-currentYZ.x())*deltaT*ANIM_SCALE_FACTOR,
                    currentYZ.y()+(targetYZ.y()-currentYZ.y())*deltaT*ANIM_SCALE_FACTOR);
		}

		// Now recompute the positions and orientations of all of the geometry

		int firstIndex=(int)Math.floor(currentIndex);
		int secondIndex=(int)Math.ceil(currentIndex);
		if (secondIndex==firstIndex) secondIndex=firstIndex+1;

		// This is the interpolation fraction between the two adjacent
		// images to the current index

		float localAlpha=currentIndex-firstIndex;
		float zAlpha=(currentYZ.y()-viewingZ)/(editingZ-viewingZ);
		float angle=(1.0f-zAlpha)*ROT_ANGLE;
		float y=zAlpha*DEFAULT_HEIGHT*SINGLE_IMAGE_MODE_RAISE_FRAC;
		Rotf posAngle=new Rotf(Vec3f.Y_AXIS,angle);
		Rotf negAngle=new Rotf(Vec3f.Y_AXIS,-angle);
		float offset=0;

		// Only bump the selected title out of the list if we're in viewing mode and close to it

		if (Math.abs(targetIndex-currentIndex)<3.0) offset=(1.0f-zAlpha)*offsetFrac*DEFAULT_HEIGHT;
		float firstImageAspectRatio=(localAlpha*averageAspectRatio(firstIndex,currentIndex,0)+(1.0f-localAlpha)*averageAspectRatio(firstIndex,currentIndex,1));
		float secondImageAspectRatio=((1.0f-localAlpha)*averageAspectRatio(secondIndex,currentIndex,0)+localAlpha*averageAspectRatio(secondIndex,currentIndex,1));

		// Assuming we have two images we're interpolating between,
		// figure out where they go first; note that this is the only
		// situation where we have to worry about an image to the
		// "other side" of the one we're considering
		// localAlpha=0 means looking at the leftmost image
		// localAlpha=1 means looking at the rightmost image

		float firstImagePos=(-1*localAlpha*DEFAULT_HEIGHT*SELECTED_SPACING_FRAC*(zAlpha*editedSpacingFrac+(1.0f-zAlpha)*secondImageAspectRatio));
		float secondImagePos=0.0f;
		if (secondIndex<graphs.size()) {
			secondImagePos=((1.0f-localAlpha)*DEFAULT_HEIGHT*SELECTED_SPACING_FRAC*(zAlpha*editedSpacingFrac+(1.0f-zAlpha)*firstImageAspectRatio));
		}

		float curPos=0.0f;
		if (graphs!=null && graphs.size()>0) {
			curPos=firstImagePos;
			for (int idx=firstIndex; idx>=0; idx--) {
				ImageGraph graph=graphs.get(idx);
				float aspectRatio;
				if (idx==firstIndex) {
					graph.xform.getTransform().setRotation(new Rotf(Vec3f.Y_AXIS,localAlpha*angle));
					graph.xform.getTransform().setTranslation(new Vec3f(curPos, y,(1.0f-localAlpha)*offset));
					curPos-=((1.0f-localAlpha)*DEFAULT_HEIGHT*(SELECTED_SPACING_FRAC-STACKED_SPACING_FRAC)*
									 (zAlpha*editedSpacingFrac+(1.0f-zAlpha)*firstImageAspectRatio));
					aspectRatio=firstImageAspectRatio;
				} else {
					graph.xform.getTransform().setRotation(posAngle);
					graph.xform.getTransform().setTranslation(new Vec3f(curPos,y,0));
					aspectRatio=graph.aspectRatio;
				}
				curPos-=(DEFAULT_HEIGHT*STACKED_SPACING_FRAC*(zAlpha*editedSpacingFrac+(1.0f-zAlpha)*aspectRatio));
			}

			curPos=secondImagePos;
			for (int idx=secondIndex; idx<graphs.size(); idx++) {
				ImageGraph graph=graphs.get(idx);
				float aspectRatio;
				if (idx==secondIndex) {
					graph.xform.getTransform().setRotation(new Rotf(Vec3f.Y_AXIS,(1.0f-localAlpha)*-angle));
					graph.xform.getTransform().setTranslation(new Vec3f(curPos, y,localAlpha*offset));
					curPos+=(localAlpha*DEFAULT_HEIGHT*(SELECTED_SPACING_FRAC-STACKED_SPACING_FRAC)*
									 (zAlpha*editedSpacingFrac+(1.0f-zAlpha)*secondImageAspectRatio));
					aspectRatio=secondImageAspectRatio;
				} else {
					graph.xform.getTransform().setRotation(negAngle);
					graph.xform.getTransform().setTranslation(new Vec3f(curPos,y,0));
					aspectRatio=graph.aspectRatio;
				}
				curPos+=(DEFAULT_HEIGHT*STACKED_SPACING_FRAC*(zAlpha*editedSpacingFrac+(1.0f-zAlpha)*aspectRatio));
			}
		}

		camera.setPosition(new Vec3f(0,currentYZ.x(),currentYZ.y()));
		Vec4f fadeTop=new Vec4f(0,0,0,0);
		float intensity=FADE_INTENSITY*(1.0f-zAlpha);
		Vec4f fadeBot=new Vec4f(intensity,intensity,intensity,intensity);
		Vec4fCollection colors=mirrorColors.getData();
		colors.set(0,fadeTop);
		colors.set(1,fadeTop);
		colors.set(2,fadeTop);
		colors.set(3,fadeTop);
		colors.set(4,fadeTop);
		colors.set(5,fadeTop);
		colors.set(6,fadeTop);
		colors.set(7,fadeTop);
		colors.set(8,fadeBot);
		colors.set(9,fadeTop);
		colors.set(10,fadeBot);
		colors.set(11,fadeBot);

		float maxSpacing=DEFAULT_HEIGHT*Math.max(STACKED_SPACING_FRAC,Math.max(SELECTED_SPACING_FRAC,editedSpacingFrac));
		float minx=-5*maxSpacing;
		float maxx=(float)curPos+5*maxSpacing;
		float minz=-2*DEFAULT_HEIGHT;
		float maxz=2*DEFAULT_HEIGHT;
		floorCoords.getData().set(0,new Vec3f(maxx,0,minz));
		floorCoords.getData().set(1,new Vec3f(minx,0,minz));
		floorCoords.getData().set(2,new Vec3f(minx,0,maxz));
		floorCoords.getData().set(3,new Vec3f(maxx,0,minz));
		floorCoords.getData().set(4,new Vec3f(minx,0,maxz));
		floorCoords.getData().set(5,new Vec3f(maxx,0,maxz));
		return needsMoreUpdates;
	}

	// =======================================================================
	// Helper method to compute a weighted average of the aspect ratios
	// of images around the given index. The spread indicates how many
	// values to consider around the given one; a value of 0 indicates
	// to consider only the given index. Returns 0.0f if the given index
	// is out of bounds, so this is safe to call for arbitrary indices.
	
	private float averageAspectRatio(int index, float mean, int spread) {

		if (index<0 || index >= graphs.size()) return 0.0f;
		float totalWeight=0.0f;
		float weightedAspectRatio=0.0f;
		for (int idx=index-spread; idx<=index+spread; idx++) {
			if (idx>=0 && idx<graphs.size()) {
				ImageGraph graph=graphs.get(idx);
				float weight=gaussian(idx,2.0f,mean);
				weightedAspectRatio+=graph.aspectRatio*weight;
				totalWeight+=weight;
			}
		}
		return weightedAspectRatio/totalWeight;
	}

	// =======================================================================
	// Assists in computing weighted averages. Computes the probability
	// density function of a gaussian probability density function of
	// given sigma centered about the given mean.

	private static float gaussian(float x, float sigma, float mean) {float xMinusM=x-mean; return (float)(ONE_OVER_ROOT_2_PI/sigma*Math.exp(-1.0f*(xMinusM*xMinusM)/(2.0f*sigma*sigma)));}

	// =======================================================================

	private void fireAnimationComplete() {List<AnimationListener> listeners=animationListeners;	for (AnimationListener listener : listeners) listener.animationCompleted();}

	// =======================================================================

	private class MListener extends MouseAdapter {

		RayPickAction ra=new RayPickAction();
		public void mousePressed(MouseEvent e) {
			ra.setPoint(e.getX(),e.getY(),e.getComponent());
			ra.apply(root);
			List<PickedPoint> pickedPoints=ra.getPickedPoints();
			Path p=null;
			if (!pickedPoints.isEmpty()) p=pickedPoints.get(0).getPath();
			if (p!=null && p.size()>1) {
				int idx=imageRoot.findChild(p.get(p.size()-2));
				if (idx>=0) setSelectedIndex(idx);
			}
		}
	}

	// =======================================================================

	private static class ImageGraph {

		boolean downloading=false, downloaded=false;
		Object imageDescriptor=null;

		Separator sep=new Separator();
		Transform xform=new Transform();
		Texture2 texture=new Texture2();
		Coordinate3 coords=new Coordinate3();
		float aspectRatio;

		ImageGraph(Object imageDescriptor) {this.imageDescriptor=imageDescriptor;}
	}

	// =======================================================================

	private class DownloadListener implements ProgressListener {

		public void progressStart(ProgressEvent evt) {}
		public void progressUpdate(ProgressEvent evt) {}
		public void progressEnd(ProgressEvent evt) {
			ImageGraph graph=(ImageGraph)evt.getClientIdentifier();
			graph.downloading=false;
			imagesDirty=true;
			if (drawable!=null) drawable.repaint();
		}
	}

	// =======================================================================
	// We need to listen to changes to the data model in order to keep
	// the scene graph in sync.

	private class DataListener implements ListDataListener {

		public void intervalAdded(ListDataEvent e) {
			for (int i=e.getIndex0(); i<=e.getIndex1(); i++) {
				ImageGraph graph=createImageGraph(model.getElementAt(i));
				insertGraph(i, graph);
			}
			imagesDirty=true;
			resetAnimation(animating);
			if (drawable!=null) drawable.repaint();
		}

		public void intervalRemoved(ListDataEvent e) {

			for (int i=e.getIndex0(); i<=e.getIndex1(); i++) removeGraph(e.getIndex0());
			resetAnimation(animating);
			if (drawable!=null)	drawable.repaint();
		}

		public void contentsChanged(ListDataEvent e) {

			for (int i=e.getIndex1(); i>=e.getIndex0(); --i) {
				ImageGraph graph=graphs.get(i);
				graph.imageDescriptor=model.getElementAt(i);
				graph.downloaded=false;
			}
			imagesDirty=true;
			resetAnimation(animating);
			if (drawable!=null)	drawable.repaint();
		}
	}

	// =======================================================================

	private class SelectionListener implements ListSelectionListener {

		public void valueChanged(ListSelectionEvent e) {
			ListSelectionModel model=(ListSelectionModel) e.getSource();
			setSelectedIndex(model.getMinSelectionIndex());
		}
	}
}
