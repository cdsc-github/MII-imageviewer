/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import java.util.Enumeration;

import java.awt.image.BufferedImage;

import javax.media.j3d.Appearance;
import javax.media.j3d.Behavior;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.Group;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.Switch;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnBehaviorPost;
import javax.media.j3d.WakeupOr;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// =======================================================================

public class TextureVolumeRenderer implements VolumeRenderer { 

	BranchGroup volume=null;
	TransformGroup origin=null, center=null;
	Transform3D centerTransform=new Transform3D();
	Group staticAttachGroup=null, dynamicAttachGroup=null;
	Switch staticBackAnnotationSwitch=null, dynamicBackAnnotationSwitch=null;
	Switch staticFrontAnnotationSwitch=null, dynamicFrontAnnotationSwitch=null;
	Vector3d centerOffset=new Vector3d(-0.5,-0.5,-0.5);
	UpdateBehavior ub=null;

	TextureVolumeData tvd=null;
	VolumeRenderingEngine vre=null;
	double scaleFactor=1.5;

	public TextureVolumeRenderer(TextureVolumeData tvd) {

		this.tvd=tvd; 
		volume=createGroup();
	}

	// =======================================================================

	private BranchGroup createGroup() {

		BranchGroup bg=new BranchGroup();

		// Create a transform group to scale the whole scene overall;
		// directly under this, place an ordered group that is used to
		// handle the underlying volume rendering.

		TransformGroup tg=new TransformGroup();
		Transform3D scale=new Transform3D();
		scale.setScale(scaleFactor);
		tg.setTransform(scale);
		bg.addChild(tg);

		OrderedGroup scaleGroup=new OrderedGroup();
		tg.addChild(scaleGroup);

		// Create the static annotation and attachment groups
	
		staticBackAnnotationSwitch=new Switch();
		staticBackAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		staticBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
		staticBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
		staticBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		scaleGroup.addChild(staticBackAnnotationSwitch);

		staticAttachGroup=new Group();
		staticAttachGroup.setCapability(Group.ALLOW_CHILDREN_READ);
		staticAttachGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		staticAttachGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		scaleGroup.addChild(staticAttachGroup);

		// Create a transform group at the origin.  Enable the
		// TRANSFORM_WRITE capability so that our behavior code can modify
		// it at runtime.  Add it to the root of the subgraph.

		origin=new TransformGroup();
		origin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		origin.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		scaleGroup.addChild(origin);

		// Create the static annotation group.  Added after origin so
		// it shows up in front.

		staticFrontAnnotationSwitch=new Switch();
		staticFrontAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		staticFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
		staticFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
		staticFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		scaleGroup.addChild(staticFrontAnnotationSwitch);

		// Create the transform group node and initialize it center the
		// object around the origin

		center=new TransformGroup();
		updateCenter(new Point3d(0.0,0.0,0.0),new Point3d(1.0,1.0,1.0));
		center.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		origin.addChild(center);

		// Set up the annotation/volume/annotation sandwich

		OrderedGroup cog=new OrderedGroup();
		center.addChild(cog);

		// Create the back dynamic annotation and attachment points

		dynamicBackAnnotationSwitch=new Switch(Switch.CHILD_ALL);
		dynamicBackAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		dynamicBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
		dynamicBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
		dynamicBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		cog.addChild(dynamicBackAnnotationSwitch);
		dynamicAttachGroup=new Group();
		dynamicAttachGroup.setCapability(Group.ALLOW_CHILDREN_READ);
		dynamicAttachGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
		dynamicAttachGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		cog.addChild(dynamicAttachGroup);

		// Create the front dynamic annotation point

		dynamicFrontAnnotationSwitch=new Switch(Switch.CHILD_ALL);
		dynamicFrontAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		dynamicFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
		dynamicFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
		dynamicFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		cog.addChild(dynamicFrontAnnotationSwitch);

		ub=new UpdateBehavior();
		ub.setSchedulingBounds(new BoundingSphere());
		bg.addChild(ub);

		return bg;
	}

	// =======================================================================

	private void updateCenter(Point3d minCoord, Point3d maxCoord) {

		centerOffset.x=-(maxCoord.x-minCoord.x)/2.0;
		centerOffset.y=-(maxCoord.y-minCoord.y)/2.0;
		centerOffset.z=-(maxCoord.z-minCoord.z)/2.0;
		centerTransform.setTranslation(centerOffset);
		center.setTransform(centerTransform);
	}

	// =======================================================================

	public void update() {

		staticBackAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
		dynamicBackAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
		staticFrontAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
		dynamicFrontAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
	}

	// =======================================================================

	public BranchGroup getVolume() {return volume;}
	public double getScaleFactor() {return scaleFactor;}
	public void setVolume(BranchGroup x) {volume=x;}

	public Group getStaticAttachGroup() {return staticAttachGroup;}
	public Group getDynamicAttachGroup() {return dynamicAttachGroup;}

	public Group getDynamicFrontAnnotationSwitch() {return dynamicFrontAnnotationSwitch;}
	public Group getDynamicBackAnnotationSwitch() {return dynamicBackAnnotationSwitch;}
	public Group getStaticFrontAnnotationSwitch() {return staticFrontAnnotationSwitch;}
	public Group getStaticBackAnnotationSwitch() {return staticBackAnnotationSwitch;}

	public TransformGroup getOrigin() {return origin;}
	public TransformGroup getCenter() {return center;}

	// =======================================================================

	private class UpdateBehavior extends Behavior {

		WakeupCriterion criterion[]={new WakeupOnBehaviorPost(null,1)};
		WakeupCondition conditions=new WakeupOr(criterion);

		public void initialize() {wakeupOn(conditions);}
		public void processStimulus(Enumeration criteria) {update(); wakeupOn(conditions);}
	}
}
