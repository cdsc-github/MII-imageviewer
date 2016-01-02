/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.ModelClip;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector4d;

import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickingCallback;

import imageviewer.ui.image.j3d.mouse.PickTranslateBehavior;

// =======================================================================

public class ClipBox implements PickingCallback {

	TransformGroup[] clippingPlanes=null;
	Transform3D transform=new Transform3D();
	Vector3d translation=new Vector3d();
	double halfPlaneFactor=0, epsilon=0.005, origin=0.05;
	ModelClip mc=null;

	TextureModel3D tm3d=null;
	VolumeAnnotation[] vaArray=null;

	public ClipBox(TextureModel3D tm3d) {this.tm3d=tm3d; initialize();}

	// =======================================================================

	private void initialize() {

		BranchGroup bg=tm3d.getRoot();
		TransformGroup tg=tm3d.getRootTransformGroup();
		BoundingSphere bs=new BoundingSphere();

		// Set picking to use a PickRay for better speed by putting the
		// tolerance to 0.

		PickTranslateBehavior ptb=new PickTranslateBehavior(tm3d.getCanvas(),bg,tg,bs,PickTool.GEOMETRY);
		ptb.setupCallback(this);
		ptb.setTolerance(0); 
		ptb.setSchedulingBounds(bs);
		bg.addChild(ptb);

		VolumeAnnotationSet vas=tm3d.getAnnotationSet();
		vas.setBoundaryCollection(ptb);
		clippingPlanes=vas.createDefaultClipPlanes();

		// Associate clipping planes with the volume.

		mc=new ModelClip();
    mc.setInfluencingBounds(bs);
		halfPlaneFactor=(-0.5*tm3d.getVolumeRenderer().getScaleFactor());
		mc.setPlane(0,new Vector4d(0,0,-1,halfPlaneFactor-epsilon));
		mc.setPlane(1,new Vector4d(0,0,1,halfPlaneFactor-epsilon));
		mc.setPlane(2,new Vector4d(-1,0,0,halfPlaneFactor-epsilon));
		mc.setPlane(3,new Vector4d(1,0,0,halfPlaneFactor-epsilon));
		mc.setPlane(4,new Vector4d(0,-1,0,halfPlaneFactor-epsilon));
		mc.setPlane(5,new Vector4d(0,1,0,halfPlaneFactor-epsilon));
		mc.setCapability(ModelClip.ALLOW_PLANE_READ);
		mc.setCapability(ModelClip.ALLOW_PLANE_WRITE);
		tg.addChild(mc);	

		// Create a wire frame box around the region.

		// vaArray=vas.createWireFrameAnnotation(origin,origin,origin,1-(origin*2));
	}

	// =======================================================================
	// Implement the PickBehaviorCallback interface method. Handle the
	// clipping planes...Note that the only difference with the method
	// above is that a transfromGroup is passed in, and not a transform.

	public void transformChanged(int type, TransformGroup planeGroup) {

		// Pay attention only to the transform groups that correspond
		// to a clipping plane.  Grab the corresponding transform.
		// Also, need to handle any scaling...
	
		planeGroup.getTransform(transform);
		transform.get(translation);
		translation.scale(tm3d.getVolumeRenderer().getScaleFactor());
		
		if (clippingPlanes[0]==planeGroup) {
			mc.setPlane(0,new Vector4d(0,0,-1,translation.z+halfPlaneFactor-epsilon)); 
			// VolumeAnnotation va=vaArray[1];
			// Shape3D shape=va.getShape();
			// LineStripArray lsa=(LineStripArray)shape.getGeometry();
			return;
		}

		if (clippingPlanes[1]==planeGroup) {mc.setPlane(1,new Vector4d(0,0,1,-translation.z+halfPlaneFactor-epsilon)); return;}
		if (clippingPlanes[2]==planeGroup) {mc.setPlane(2,new Vector4d(-1,0,0,translation.x+halfPlaneFactor-epsilon)); return;}
		if (clippingPlanes[3]==planeGroup) {mc.setPlane(3,new Vector4d(1,0,0,-translation.x+halfPlaneFactor-epsilon)); return;}
		if (clippingPlanes[4]==planeGroup) {mc.setPlane(4,new Vector4d(0,-1,0,translation.y+halfPlaneFactor-epsilon)); return;}
		if (clippingPlanes[5]==planeGroup) {mc.setPlane(5,new Vector4d(0,1,0,-translation.y+halfPlaneFactor-epsilon)); return;}
	}
}
