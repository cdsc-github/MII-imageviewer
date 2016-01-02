/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import java.util.ArrayList;
import java.util.BitSet;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Group;
import javax.media.j3d.Link;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Switch;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// =======================================================================

public class AnnotationRenderingEngine extends VolumeRenderingEngine {

	BranchGroup frontRoot=new BranchGroup(), backRoot=new BranchGroup();
	Switch frontAnnotations=new Switch(Switch.CHILD_MASK), backAnnotations=new Switch(Switch.CHILD_MASK);
	VolumeAnnotationSet vas=null;
	BitSet frontFaceBits=new BitSet(), backFaceBits=new BitSet();
	Point3d volumeCenter=new Point3d();
	Vector3d viewpointVector=new Vector3d();
	
	public AnnotationRenderingEngine(View v, VolumeData vd, VolumeAnnotationSet vas) {

		super(v,vd);
		this.vas=vas;

		textureAttributes.setTextureMode(TextureAttributes.MODULATE);
		renderingAttributes.setAlphaTestFunction(RenderingAttributes.ALWAYS);
		renderingAttributes.setDepthBufferEnable(true);
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setDstBlendFunction(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		ColoringAttributes ca=new ColoringAttributes(1.0f,1.0f,1.0f,ColoringAttributes.SHADE_FLAT);

		frontRoot.setCapability(BranchGroup.ALLOW_DETACH);
		frontRoot.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);
		backRoot.setCapability(BranchGroup.ALLOW_DETACH);
		backRoot.setCapability(Node.ALLOW_LOCAL_TO_VWORLD_READ);

		if (vas!=null) {

			Appearance a=new Appearance();
			a.setTransparencyAttributes(transparencyAttributes);
			a.setTextureAttributes(textureAttributes);
			a.setRenderingAttributes(renderingAttributes);
			a.setPolygonAttributes(polygonAttributes);
			a.setColoringAttributes(ca);
			vas.setDefaultAppearance(a);

			ArrayList annotations=vas.getAnnotations();
			for (int loop=0, n=annotations.size(); loop<n; loop++) {
				VolumeAnnotation va=(VolumeAnnotation)annotations.get(loop);
				frontAnnotations.addChild(new Link(va.getAnnotation()));
				backAnnotations.addChild(new Link(va.getAnnotation()));
			}
		}

		frontAnnotations.setCapability(Switch.ALLOW_SWITCH_WRITE);
		backAnnotations.setCapability(Switch.ALLOW_SWITCH_WRITE);
		frontRoot.addChild(frontAnnotations);
		backRoot.addChild(backAnnotations);

		volumeCenter.x=(vd.getMaxCoord().x+vd.getMinCoord().x)/2;
		volumeCenter.y=(vd.getMaxCoord().y+vd.getMinCoord().y)/2;
		volumeCenter.z=(vd.getMaxCoord().z+vd.getMinCoord().z)/2;

		vas.setAnnotationRenderingEngine(this);
	}

	// =======================================================================

	public void attachFront(Group dynamicGroup, Group staticGroup) {dynamicGroup.addChild(frontRoot);}
	public void attachBack(Group dynamicGroup, Group staticGroup) {dynamicGroup.addChild(backRoot);}

	public void update() {

		volumeCenter.x=(vd.getMaxCoord().x+vd.getMinCoord().x)/2;
		volumeCenter.y=(vd.getMaxCoord().y+vd.getMinCoord().y)/2;
		volumeCenter.z=(vd.getMaxCoord().z+vd.getMinCoord().z)/2;
		viewpointChange();
	}

	// =======================================================================

	public void addAnnotation(VolumeAnnotation va) {

		frontAnnotations.addChild(new Link(va.getAnnotation()));
		backAnnotations.addChild(new Link(va.getAnnotation()));
	}

	// =======================================================================

	public BranchGroup getFrontRoot() {return frontRoot;}
	public BranchGroup getBackRoot() {return backRoot;}

	// =======================================================================

	public void viewpointChange() {

		Point3d viewpoint=getLocalViewPosition(frontRoot);
		if (viewpoint!=null) {
			viewpointVector.sub(viewpoint,volumeCenter);
			if (vas!=null) {
				ArrayList annotations=vas.getAnnotations();
				for (int loop=0, n=annotations.size(); loop<n; loop++) {
					VolumeAnnotation va=(VolumeAnnotation)annotations.get(loop);
					if (viewpointVector.dot(va.getFaceNormal())<0) {
						backFaceBits.set(loop);
						frontFaceBits.clear(loop);
					} else {
						frontFaceBits.set(loop);
						backFaceBits.clear(loop);
					}
				}
			}
			frontAnnotations.setChildMask(frontFaceBits);
			backAnnotations.setChildMask(backFaceBits);
		}
	}
}
