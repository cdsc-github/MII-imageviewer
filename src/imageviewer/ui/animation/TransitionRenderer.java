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

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;

import com.sun.opengl.util.j2d.TextureRenderer;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;

import net.java.joglutils.msg.actions.GLRenderAction;
import net.java.joglutils.msg.actions.GLResetAction;
import net.java.joglutils.msg.collections.Vec2fCollection;
import net.java.joglutils.msg.collections.Vec3fCollection;
import net.java.joglutils.msg.collections.Vec4fCollection;

import net.java.joglutils.msg.math.Rotf;
import net.java.joglutils.msg.math.Vec2f;
import net.java.joglutils.msg.math.Vec3f;
import net.java.joglutils.msg.math.Vec4f;
import net.java.joglutils.msg.misc.Path;

import net.java.joglutils.msg.nodes.Blend;
import net.java.joglutils.msg.nodes.Color4;
import net.java.joglutils.msg.nodes.Coordinate3;
import net.java.joglutils.msg.nodes.DepthTest;
import net.java.joglutils.msg.nodes.Node;
import net.java.joglutils.msg.nodes.PerspectiveCamera;
import net.java.joglutils.msg.nodes.Separator;
import net.java.joglutils.msg.nodes.Texture2;
import net.java.joglutils.msg.nodes.TextureCoordinate2;
import net.java.joglutils.msg.nodes.Transform;
import net.java.joglutils.msg.nodes.TriangleSet;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.triggers.Trigger;
import org.jdesktop.animation.timing.triggers.TriggerEvent;

// =======================================================================
// Adapted from Sun's Iris SlideShowRenderer

public class TransitionRenderer implements GLEventListener {

	public static enum TransitionType {CUBE_TRANSITION_LEFT, CUBE_TRANSITION_RIGHT, FLIP_FORWARD, FLIP_BACKWARD, FADE}

	boolean firstInit=true, texturesArePersistent=false, transitionPending=false, dirty=false;
	TexturedRectangle firstImage=null, secondImage=null;
	Transform transitionTranslation=null;
	Transition curTransition=null;
	Separator root=null, transitionRoot=null;
	PerspectiveCamera camera=null;

	GLAutoDrawable drawable=null;
	GLRenderAction ra=new GLRenderAction();

	Vec2f imagePlaneXYScale=new Vec2f();
	Thread openGLThread=null;
	float imagePlaneZ=0f;
	int maxTextureSize=0, transitionLength=2000;
	TransitionType transitionType=TransitionType.CUBE_TRANSITION_RIGHT;

	ArrayList<AnimationListener> listeners=new ArrayList<AnimationListener>();

	// =======================================================================

	public TransitionRenderer(boolean texturesArePersistent) {

		TextureIO.setTexRectEnabled(false);
		this.texturesArePersistent=texturesArePersistent;
		root=new Separator();
		camera=new PerspectiveCamera();
		camera.setNearDistance(2.0f);
		camera.setFarDistance(20.0f);
		camera.setHeightAngle((float)Math.PI/8);
		camera.setPosition(new Vec3f(0,0,0));
		Separator slideRoot=new Separator();
		slideRoot.addChild(camera);
		transitionTranslation=new Transform();
		slideRoot.addChild(transitionTranslation);
		transitionRoot=new Separator();
		slideRoot.addChild(transitionRoot);
		firstImage=newTexturedRectangle();
		secondImage=newTexturedRectangle();
		root.addChild(slideRoot);
	}
	
	// =======================================================================
	// Implementation of the GLEventListener interface

	private boolean checkOpenGLThread() {if (openGLThread==null) {openGLThread=Thread.currentThread(); return true;} else {Thread t=Thread.currentThread(); return (t!=openGLThread) ? false : true;}}

	public void init(GLAutoDrawable drawable) {

		if (!checkOpenGLThread()) drawable.repaint();
		this.drawable=drawable;
		GL gl=drawable.getGL();
		if (firstInit || !texturesArePersistent) {
			GLResetAction reset=new GLResetAction(); 
			reset.apply(root);
			int[] tmp=new int[1];
			gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE,tmp,0);
			maxTextureSize=tmp[0];
			firstInit=false;
		}
		gl.setSwapInterval(1);
		gl.glEnable(GL.GL_CULL_FACE);
	}

	public void display(GLAutoDrawable drawable) {
	
		if (!checkOpenGLThread()) drawable.repaint();
		recompute();
		if (transitionPending) runTransition(getTransition(),transitionLength);
		GL gl=drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		ra.apply(root);
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {

		if (!checkOpenGLThread()) drawable.repaint();
		dirty=true;
		recompute();
		GL gl=drawable.getGL();
		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		ra.apply(root);
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

	// =======================================================================

	public void addAnimationListener(AnimationListener al) {if (!listeners.contains(al)) listeners.add(al);}
	public void removeAnimationListener(AnimationListener al) {listeners.remove(al);}
	public void fireAnimationStart() {for (AnimationListener al : listeners) al.animationStarted();}
	public void fireAnimationCompleted() {for (AnimationListener al : listeners) al.animationCompleted();}

	// =======================================================================

	private void getImage(BufferedImage img, TexturedRectangle rect, boolean showRectWhenAvailable) {

		if ((rect!=null)&&(img!=null)) {
			rect.texture.setTexture(img,false);
			if (showRectWhenAvailable) transitionRoot.addChild(rect.sep);
			dirty=true;
			if (drawable!=null) drawable.repaint();
		}
	}

	// =======================================================================

	private void recomputeTransitionTransform() {transitionTranslation.getTransform().setTranslation(new Vec3f(0,0,imagePlaneZ));}
	public void setTransition(TransitionType transitionType) {this.transitionType=transitionType;}
	public boolean isTransitionPending() {return transitionPending;}

	private Transition getTransition() {

		switch (transitionType) {

		           case FLIP_FORWARD: return new FlipTransition(FlipTransition.FLIP_FORWARD); 
		          case FLIP_BACKWARD: return new FlipTransition(FlipTransition.FLIP_BACK); 
		                   case FADE: return new FadeTransition();
		   case CUBE_TRANSITION_LEFT: return new CubeTransition(CubeTransition.ROTATE_LEFT);
		                     default:
		  case CUBE_TRANSITION_RIGHT: return new CubeTransition(CubeTransition.ROTATE_RIGHT); 
		}
	}

	// =======================================================================

	private void recomputeImagePlane() {

		// Recompute the image plane based on the camera parameters and
		// the current aspect ratio. The assumption we make is the
		// distance of the geometry from the camera's near plane -- we
		// solve for this to try to make sure it will fit in front of the
		// near plane no matter which point it is rotated about.

		float aspectRatio=1.0f;
		if (drawable!=null) aspectRatio=(float)drawable.getWidth()/(float)drawable.getHeight();
		float tanHeightAngle=(float) Math.tan(camera.getHeightAngle());
		float tanWidthAngle=(float) Math.tan(camera.getWidthAngle());
		float z=camera.getNearDistance()/(1-2*tanHeightAngle);
		float y=z*tanHeightAngle;
		float x=z*tanWidthAngle;
		imagePlaneXYScale.set(x, y);
		imagePlaneZ=-z;
	}

	// =======================================================================

	private TexturedRectangle newTexturedRectangle() {

		Coordinate3 coords=new Coordinate3();
		coords.setData(new Vec3fCollection());
		Vec3f origin=new Vec3f();
		for (int i=0; i<6; i++) coords.getData().add(origin);
		Separator sep=new Separator();
		sep.addChild(coords);
		TextureCoordinate2 texCoords=new TextureCoordinate2();
		texCoords.setData(new Vec2fCollection());
		texCoords.getData().add(new Vec2f(1,1));
		texCoords.getData().add(new Vec2f(0,1));
		texCoords.getData().add(new Vec2f(0,0));
		texCoords.getData().add(new Vec2f(1,1));
		texCoords.getData().add(new Vec2f(0,0));
		texCoords.getData().add(new Vec2f(1,0));
		sep.addChild(texCoords);
		Texture2 texture=new Texture2();
		sep.addChild(texture);
		TriangleSet tris=new TriangleSet();
		tris.setNumTriangles(2);
		sep.addChild(tris);
		TexturedRectangle rect=new TexturedRectangle();
		rect.sep=sep;
		rect.coords=coords;
		rect.texCoords=texCoords;
		rect.texture=texture;
		return rect;
	}

	// =======================================================================

	private void recomputeCoords(TexturedRectangle rect) {

		Vec2f xy=imagePlaneXYScale;
		float x=xy.x();
		float y=xy.y();
		Texture tex=rect.texture.getTexture();
		if (tex==null) return;
		float desiredAspectRatio=tex.getAspectRatio();
		float aspectRatio=1.0f;
		if (drawable!=null) aspectRatio=(float)drawable.getWidth()/(float)drawable.getHeight();
		if (aspectRatio>desiredAspectRatio) {
			x=y*desiredAspectRatio;
		} else if (aspectRatio<desiredAspectRatio) {
			y=x/desiredAspectRatio;
		}

		rect.coords.getData().set(0,new Vec3f(x,y,0));
		rect.coords.getData().set(1,new Vec3f(-x,y,0));
		rect.coords.getData().set(2,new Vec3f(-x,-y,0));
		rect.coords.getData().set(3,new Vec3f(x,y,0));
		rect.coords.getData().set(4,new Vec3f(-x,-y,0));
		rect.coords.getData().set(5,new Vec3f(x,-y,0));

		float texOffsetX=0.5f/tex.getWidth();
		float texOffsetY=0.5f/tex.getHeight();
		rect.texCoords.getData().set(0,new Vec2f(1-texOffsetX,1-texOffsetY));
		rect.texCoords.getData().set(1,new Vec2f(texOffsetX,1-texOffsetY));
		rect.texCoords.getData().set(2,new Vec2f(texOffsetX, texOffsetY));
		rect.texCoords.getData().set(3,new Vec2f(1-texOffsetX,1-texOffsetY));
		rect.texCoords.getData().set(4,new Vec2f(texOffsetX,texOffsetY));
		rect.texCoords.getData().set(5,new Vec2f(1-texOffsetX,texOffsetY));
		rect.maxX=x;
		rect.maxY=y;
	}

	// =======================================================================

	private void recompute() {

		if (dirty) {
			dirty=false;
			recomputeImagePlane();
			recomputeTransitionTransform();
			recomputeCoords(firstImage);
			recomputeCoords(secondImage);
			if (curTransition!=null) curTransition.updateSizes();
		}
	}

	// =======================================================================

	private void runTransition(final Transition transition, final int dur) {

		if (curTransition!=null) return;
		transitionPending=false;
		curTransition=transition;
		curTransition.setAlpha(0);
		Animator animator=new Animator(dur,new TimingTarget() {

			public void begin() {}
			public void end() {
				curTransition=null;
				TexturedRectangle t=firstImage;
				firstImage=secondImage;
				secondImage=t;
				fireAnimationCompleted();
			}

			public void repeat() {}
			public void timingEvent(float fraction) {transition.setAlpha(fraction); drawable.display();}});

		animator.setAcceleration(0.2f);
		animator.setDeceleration(0.2f);
		animator.start();
	}

	// =======================================================================

	public void execute(BufferedImage startImage, BufferedImage endImage) {transitionPending=true; getImage(startImage,firstImage,false); getImage(endImage,secondImage,false);}

	// =======================================================================
	// Implementation of the transitions

	private abstract class Transition {

		public abstract void updateSizes();
		public abstract void setAlpha(float alpha);
	}

	private abstract class TwoTransformTransition extends Transition {

		protected Transform leftXform1=new Transform();  // translation
		protected Transform leftXform2=new Transform();  // rotation
		protected Transform leftXform3=new Transform();  // translation
		protected Transform rightXform1=new Transform(); // translation
		protected Transform rightXform2=new Transform(); // rotation
		protected Transform rightXform3=new Transform(); // translation

		protected TwoTransformTransition() {

			Separator left=new Separator();
			left.addChild(leftXform1);
			left.addChild(leftXform2);
			left.addChild(leftXform3);
			left.addChild(firstImage.sep);

			Separator right=new Separator();
			right.addChild(rightXform1);
			right.addChild(rightXform2);
			right.addChild(rightXform3);
			right.addChild(secondImage.sep);

			transitionRoot.removeAllChildren();
			transitionRoot.addChild(left);
			transitionRoot.addChild(right);
		}

		protected void setLeftOffset(Vec3f offset) {leftXform1.getTransform().setTranslation(offset); leftXform3.getTransform().setTranslation(offset.times(-1));}
		protected void setRightOffset(Vec3f offset) {rightXform1.getTransform().setTranslation(offset); rightXform3.getTransform().setTranslation(offset.times(-1));}
		protected void setLeftRotation(Rotf rotation) {leftXform2.getTransform().setRotation(rotation);}
		protected void setRightRotation(Rotf rotation) {rightXform2.getTransform().setRotation(rotation);}
	}

	// =======================================================================

	private class CubeTransition extends TwoTransformTransition {

		public static final int ROTATE_LEFT=0;
		public static final int ROTATE_RIGHT=1;

		private int direction;
		protected Transform rotationXform=new Transform();

		public CubeTransition(int rotationDirection) {
			super();
			transitionRoot.insertChild(0, rotationXform);
			this.direction=rotationDirection;
			dirty=true;
			updateSizes();
		}

		public void updateSizes() {
			leftXform3.getTransform().setTranslation(new Vec3f(0,0,secondImage.maxX));
			rightXform3.getTransform().setTranslation(new Vec3f(0,0,firstImage.maxX));
			float scale=((direction==ROTATE_LEFT) ? 1 : -1);
			setRightRotation(new Rotf(Vec3f.Y_AXIS,(float)(scale*Math.PI/2)));
		}

		public void setAlpha(float alpha) {

			float scale=((direction==ROTATE_LEFT) ? -1 : 1);
			float initialOffset=-secondImage.maxX;
			float destOffset=-firstImage.maxX;
			rotationXform.getTransform().setRotation(new Rotf(Vec3f.Y_AXIS,(float)(scale*alpha*Math.PI/2)));
			rotationXform.getTransform().setTranslation(new Vec3f(0,0,destOffset*alpha+initialOffset*(1.0f-alpha)));
		}
	}

	// =======================================================================

	private class FlipTransition extends TwoTransformTransition {

		public static final int FLIP_FORWARD=0;
		public static final int FLIP_BACK=1;

		private int direction;
		protected Transform rotationXform=new Transform();

		public FlipTransition(int direction) {
			super();
			transitionRoot.insertChild(0, rotationXform);
			this.direction=direction;
			dirty=true;
			updateSizes();
		}

		public void updateSizes() {
			leftXform3.getTransform().setTranslation(new Vec3f(0,0,secondImage.maxY));
			rightXform3.getTransform().setTranslation(new Vec3f(0,0,firstImage.maxY));
			float scale=((direction==FLIP_FORWARD) ? 1 : -1);
			setRightRotation(new Rotf(Vec3f.X_AXIS,(float)(scale*Math.PI/2)));
		}

		public void setAlpha(float alpha) {

			float scale=((direction==FLIP_FORWARD) ? -1 : 1);
			float initialOffset=-secondImage.maxY;
			float destOffset=-firstImage.maxY;
			rotationXform.getTransform().setRotation(new Rotf(Vec3f.X_AXIS,(float)(scale*alpha*Math.PI/2)));
			rotationXform.getTransform().setTranslation(new Vec3f(0,0,destOffset*alpha+initialOffset*(1.0f-alpha)));
		}
	}

	// =======================================================================

	private class FadeTransition extends Transition {

		Vec4f leftColor=new Vec4f();
		Vec4f rightColor=new Vec4f();
		Color4 leftColors=new Color4();
		Color4 rightColors=new Color4();
		Transform leftXform=new Transform();
		DepthTest depthTest=new DepthTest();
		protected Separator right=null;

		public FadeTransition() {

			depthTest.setEnabled(false);
			Separator left=new Separator();
			leftXform.getTransform().setTranslation(new Vec3f(0f,0f,0.001f));
			left.addChild(leftXform);
			Blend leftBlend=new Blend();
			leftBlend.setEnabled(true);
			leftBlend.setSourceFunc(Blend.ONE);
			leftBlend.setDestFunc(Blend.ONE_MINUS_SRC_ALPHA);
			left.addChild(leftBlend);
			leftColors=new Color4();
			leftColors.setData(new Vec4fCollection(6));
			for (int i=0; i<6; i++) leftColors.getData().add(leftColor);
			left.addChild(leftColors);
			left.addChild(firstImage.sep);

			right=new Separator();
			Blend rightBlend=new Blend();
			rightBlend.setEnabled(true);
			rightBlend.setSourceFunc(Blend.ONE);
			rightBlend.setDestFunc(Blend.ONE_MINUS_SRC_ALPHA);
			right.addChild(rightBlend);
			rightColors=new Color4();
			rightColors.setData(new Vec4fCollection(6));
			for (int i=0; i<6; i++) rightColors.getData().add(rightColor);
			right.addChild(rightColors);
			right.addChild(secondImage.sep);

			transitionRoot.removeAllChildren();
			transitionRoot.addChild(depthTest);
			transitionRoot.addChild(left);
			transitionRoot.addChild(right);
		}

		public void updateSizes() {}

		public void setAlpha(float alpha) {

			float a1=1f-alpha;
			float a2=alpha;

			leftColor.set(a1,a1,a1,a1);
			rightColor.set(a2,a2,a2,a2);
			for (int i=0; i<6; i++) {
				leftColors.getData().set(i,leftColor);
				rightColors.getData().set(i,rightColor);
			}
		}
	}

	// =======================================================================

	private static class TexturedRectangle {

		TextureCoordinate2 texCoords=null;
		Coordinate3 coords=null;
		Texture2 texture=null;
		Separator sep=null;
		float maxX=0f, maxY=0f;
	}
}
