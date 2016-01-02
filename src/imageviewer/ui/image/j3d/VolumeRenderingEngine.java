/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.Canvas3D;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// =======================================================================

public abstract class VolumeRenderingEngine {

  public static final int X_AXIS=0;
	public static final int Y_AXIS=1;
	public static final int Z_AXIS=2;
	public static final int FRONT=0;
	public static final int BACK=1;

	// =======================================================================

	TextureAttributes textureAttributes=new TextureAttributes();
	RenderingAttributes renderingAttributes=new RenderingAttributes();
	TransparencyAttributes transparencyAttributes=new TransparencyAttributes();
	PolygonAttributes polygonAttributes=new PolygonAttributes();

	View view=null;
	VolumeData vd=null;

	public VolumeRenderingEngine(View view, VolumeData vd) {this.view=view; this.vd=vd;}

	// =======================================================================

	public void attach(Group dynamicGroup, Group staticGroup) {}
	public void transformChanged(int type, Transform3D transform) {} 

	public abstract void update();
	public abstract void viewpointChange();

	// =======================================================================

	protected Point3d getLocalViewPosition(Node node) {

		Point3d viewPosition=new Point3d();
		Vector3d translate=new Vector3d();
		double angle=0.0, mag=0.0, sign=0.0;
		double tx=0, ty=0, tz=0;

		if (node==null) return null;
		if (!node.isLive()) return null;

		Canvas3D canvas=(Canvas3D)view.getCanvas3D(0); 		// Get viewplatforms's location in virutal world
		canvas.getCenterEyeInImagePlate(viewPosition);
		Transform3D t=new Transform3D();
		canvas.getImagePlateToVworld(t);
		t.transform(viewPosition);
		Transform3D parentTransform=new Transform3D(); 		// Get parent transform and invert
		node.getLocalToVworld(parentTransform);
		parentTransform.invert();
		parentTransform.transform(viewPosition); 		      // Transform the eye position into the parent's coordinate system
		return viewPosition;
	}
}
