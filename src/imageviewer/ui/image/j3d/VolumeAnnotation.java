/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Switch;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

// =======================================================================

public class VolumeAnnotation {

	SharedGroup annotation=null;
	TransformGroup tg=null;
	Shape3D shape=null;
	Vector3d faceNormal=new Vector3d();

	public VolumeAnnotation(Shape3D shape) {

		annotation=new SharedGroup();
		BranchGroup bg=new BranchGroup();
		tg=new TransformGroup();
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		tg.setCapability(TransformGroup.ENABLE_PICK_REPORTING);
		tg.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
		
		Switch s=new Switch();
		s.setCapability(Switch.ALLOW_SWITCH_WRITE);
		s.addChild(shape);
		s.setWhichChild(Switch.CHILD_ALL);
		tg.addChild(s);
		bg.addChild(tg);
		annotation.addChild(bg);
		this.shape=shape;

		shape.setCapability(Shape3D.ALLOW_GEOMETRY_READ);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
	}

	// =======================================================================

	public TransformGroup getAnnotationTransform() {return tg;}	
	public SharedGroup getAnnotation() {return annotation;}
	public Vector3d getFaceNormal() {return faceNormal;}
	public Shape3D getShape() {return shape;}

	public void setFaceNormal(float x, float y, float z) {faceNormal.x=x; faceNormal.y=y; faceNormal.z=z;}
	public void setFaceNormal(double x, double y, double z) {faceNormal.x=x; faceNormal.y=y; faceNormal.z=z;}
	public void setFaceNormal(Vector3d x) {faceNormal=x;}

}
