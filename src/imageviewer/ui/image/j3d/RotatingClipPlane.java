/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Link;
import javax.media.j3d.ModelClip;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;

import imageviewer.ui.image.j3d.mouse.MouseCtrlDragEvaluator;
import imageviewer.ui.image.j3d.mouse.MouseRotator;

// =======================================================================

public class RotatingClipPlane implements MouseBehaviorCallback {

	MouseRotator mr=new MouseRotator();
	ModelClip mc=new ModelClip();
	TransformGroup tg=new TransformGroup();

	public RotatingClipPlane(TransformGroup origin, double scaleFactor) {initialize(origin,scaleFactor);}

	private void initialize(TransformGroup origin, double scaleFactor) {

		// Set up the bounding sphere and the transform group responsible
		// for this clip plane.

		BoundingSphere bs=new BoundingSphere();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tg.setCapability(TransformGroup.ENABLE_PICK_REPORTING);

		Transform3D t3d=new Transform3D();
		tg.getTransform(t3d);
		t3d.setScale(scaleFactor);
		tg.setTransform(t3d);
				
		TransformGroup temp=new TransformGroup();		
		origin.getTransform(t3d);
		temp.setTransform(t3d);
		tg.addChild(temp);		

		// Instantiate a mouse rotation device, but give a different type
		// of mouse button evaluator so that it does not conflict with the
		// normal mouse rotation mechanism.

		mr.setMouseCondition(new MouseCtrlDragEvaluator());
		mr.setTransformGroup(tg);
		mr.setupCallback(this);
		mr.setFactor(0.007);
		mr.setSchedulingBounds(bs);
		tg.addChild(mr);

		// Set up the clipping bounds on this transform group.  The
		// clipping plane will rotate automatically when the parent
		// transformGroup is rotated by the mouse.

    mc.setInfluencingBounds(bs);
		mc.setPlane(0,new Vector4d(0,0,1.5,-0.01));
		tg.addChild(mc);

		Point3d a=new Point3d(0,0,0.5);
		Point3d b=new Point3d(0,1,0.5);
		Point3d c=new Point3d(1,1,0.5);
		Point3d d=new Point3d(1,0,0.5);

		VolumeAnnotation va=VolumeAnnotationSet.createRectangleAnnotation(a,b,c,d);
		temp.addChild(new Link(va.getAnnotation()));
	}

	// =======================================================================

	public TransformGroup getTransformGroup() {return tg;}
	public ModelClip getModelClip() {return mc;}

	// =======================================================================
	// Implement the MouseBehaviorCallback interface method.  

	public void transformChanged(int type, Transform3D transform) {}
}

