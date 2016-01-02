/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

// =======================================================================

public class TextureVolumeRenderingEngine extends VolumeRenderingEngine {

	Material material=new Material();

	BranchGroup root=null;
	Switch axisSwitch=new Switch();
	int[][] axisIndex=new int[3][2];
	double[] quadCoords=new double[12];
	boolean autoAxisEnable=true;
	int autoAxis=0, autoDirection=0, currentAxis=Z_AXIS, currentDirection=FRONT;

	TextureVolume textureVolume=null;
	VolumeRenderer vr=null;

	// =======================================================================

	public TextureVolumeRenderingEngine(View v, TextureVolumeData tvd, VolumeRenderer vr, boolean colorMap) {

		super(v,tvd);
		this.vr=vr;

		// Setup the basic attributes and materials associated with the
		// rendering engine.  Turn off lighting.
	
		textureAttributes.setTextureMode(TextureAttributes.REPLACE);
		textureAttributes.setCapability(TextureAttributes.ALLOW_COLOR_TABLE_WRITE);
		renderingAttributes.setAlphaTestFunction(RenderingAttributes.ALWAYS);
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setDstBlendFunction(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		material.setLightingEnable(false);

		axisIndex[X_AXIS][FRONT]=0;
		axisIndex[X_AXIS][BACK]=1;
		axisIndex[Y_AXIS][FRONT]=2;
		axisIndex[Y_AXIS][BACK]=3;
		axisIndex[Z_AXIS][FRONT]=4;
		axisIndex[Z_AXIS][BACK]=5;

		axisSwitch.setCapability(Switch.ALLOW_SWITCH_READ);
		axisSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
		axisSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
		axisSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
		axisSwitch.addChild(createOrderedGroup());
		axisSwitch.addChild(createOrderedGroup());
		axisSwitch.addChild(createOrderedGroup());
		axisSwitch.addChild(createOrderedGroup());
		axisSwitch.addChild(createOrderedGroup());
		axisSwitch.addChild(createOrderedGroup());

		root=new BranchGroup();
		root.addChild(axisSwitch);
		root.setCapability(BranchGroup.ALLOW_DETACH);
		root.setCapability(BranchGroup.ALLOW_LOCAL_TO_VWORLD_READ);

		textureVolume=new TextureVolume(tvd,colorMap);
	}

	// =======================================================================

	private OrderedGroup createOrderedGroup() {

		OrderedGroup og=new OrderedGroup();
		og.setCapability(Group.ALLOW_CHILDREN_READ);
		og.setCapability(Group.ALLOW_CHILDREN_WRITE);
		og.setCapability(Group.ALLOW_CHILDREN_EXTEND);
		return og;
	}

	// =======================================================================

	public void attach(Group dynamicGroup, Group staticGroup) {dynamicGroup.addChild(root);}

	// =======================================================================

	public BranchGroup getRoot() {return root;}

	// =======================================================================

	public void update() {

		vr.update();
		vd.update();
		textureVolume.generateTexture();
		loadAxis(Z_AXIS);
		loadAxis(Y_AXIS);
		loadAxis(X_AXIS);
		axisSwitch.setWhichChild(axisIndex[currentAxis][currentDirection]);
	}

	// =======================================================================

	protected void setCurCoordX(int i) {

		double curX=(i*vd.getScaledXSpace());
		quadCoords[0]=curX;	quadCoords[3]=curX; quadCoords[6]=curX;	quadCoords[9]=curX;
	}

	protected void setCurCoordY(int i) {

		double curY=(i*vd.getScaledYSpace());
		quadCoords[1]=curY;	quadCoords[4]=curY;	quadCoords[7]=curY;	quadCoords[10]=curY;
	}

	protected void setCurCoordZ(int i) {

		double curZ=(i*vd.getScaledZSpace());
		quadCoords[2]=curZ; quadCoords[5]=curZ; quadCoords[8]=curZ;	quadCoords[11]=curZ;
	}

	protected void setCoordsX() {

		quadCoords[1]=vd.getMinCoord().y; 	// lower left
		quadCoords[2]=vd.getMinCoord().z;
		quadCoords[4]=vd.getMaxCoord().y; 	// lower right
		quadCoords[5]=vd.getMinCoord().z;
		quadCoords[7]=vd.getMaxCoord().y; 	// upper right
		quadCoords[8]=vd.getMaxCoord().z;
		quadCoords[10]=vd.getMinCoord().y; 	// upper left
		quadCoords[11]=vd.getMaxCoord().z;
	}

	protected void setCoordsY() {

		quadCoords[0]=vd.getMinCoord().x;		// lower left
		quadCoords[2]=vd.getMinCoord().z;
		quadCoords[3]=vd.getMinCoord().x;		// lower right
		quadCoords[5]=vd.getMaxCoord().z;
		quadCoords[6]=vd.getMaxCoord().x;		// upper right
		quadCoords[8]=vd.getMaxCoord().z;
		quadCoords[9]=vd.getMaxCoord().x;		// upper left
		quadCoords[11]=vd.getMinCoord().z;
	}

	protected void setCoordsZ() {

		quadCoords[0]=vd.getMinCoord().x;		// lower left
		quadCoords[1]=vd.getMinCoord().y;
		quadCoords[3]=vd.getMaxCoord().x;		// lower right
		quadCoords[4]=vd.getMinCoord().y;
		quadCoords[6]=vd.getMaxCoord().x;		// upper right
		quadCoords[7]=vd.getMaxCoord().y;
		quadCoords[9]=vd.getMinCoord().x;		// upper left
		quadCoords[10]=vd.getMaxCoord().y;
	}

	// =======================================================================

	private void loadAxis(int axis) {

		OrderedGroup frontGroup=null, backGroup=null;
		int rSize=0;

		switch (axis) {

	    case Z_AXIS: frontGroup=(OrderedGroup)axisSwitch.getChild(axisIndex[Z_AXIS][FRONT]);
				           backGroup=(OrderedGroup)axisSwitch.getChild(axisIndex[Z_AXIS][BACK]);
									 rSize=vd.getDepth();
									 setCoordsZ();
									 break;
	    case Y_AXIS: frontGroup=(OrderedGroup)axisSwitch.getChild(axisIndex[Y_AXIS][FRONT]);
	                 backGroup=(OrderedGroup)axisSwitch.getChild(axisIndex[Y_AXIS][BACK]);
									 rSize=vd.getHeight();
									 setCoordsY();
									 break;
	    case X_AXIS: frontGroup=(OrderedGroup)axisSwitch.getChild(axisIndex[X_AXIS][FRONT]);
	                 backGroup=(OrderedGroup)axisSwitch.getChild(axisIndex[X_AXIS][BACK]);
									 rSize=vd.getWidth();
									 setCoordsX();
									 break;
		}

		for (int i=0; i<rSize; i++) { 

			switch (axis) {
			  case Z_AXIS: setCurCoordZ(i);	break;
			  case Y_AXIS: setCurCoordY(i);	break;
			  case X_AXIS: setCurCoordX(i);	break;
			}

			Appearance a=new Appearance();
			a.setMaterial(material);
			a.setTransparencyAttributes(transparencyAttributes);
			a.setTextureAttributes(textureAttributes);
			a.setTexture(textureVolume.getTexture());
			a.setTexCoordGeneration(textureVolume.getTextureCoordinates());
			a.setRenderingAttributes(renderingAttributes);
			a.setPolygonAttributes(polygonAttributes);

			QuadArray quadArray=new QuadArray(4,GeometryArray.COORDINATES);
			quadArray.setCoordinates(0,quadCoords);

			Shape3D frontShape=new Shape3D(quadArray,a);
			frontShape.getGeometry().setCapability(Geometry.ALLOW_INTERSECT);
			BranchGroup frontShapeGroup=new BranchGroup();
			frontShapeGroup.setCapability(BranchGroup.ALLOW_DETACH);
			frontShapeGroup.addChild(frontShape);
			frontGroup.addChild(frontShapeGroup);
		
			Shape3D backShape=new Shape3D(quadArray,a);
			backShape.getGeometry().setCapability(Geometry.ALLOW_INTERSECT);
			BranchGroup backShapeGroup=new BranchGroup();
			backShapeGroup.setCapability(BranchGroup.ALLOW_DETACH);
	    backShapeGroup.addChild(backShape);
	    backGroup.insertChild(backShapeGroup,0);
		} 
	} 

	// =======================================================================

	public void viewpointChange() {

		Point3d viewpoint=getLocalViewPosition(root);
		if (viewpoint!=null) {

	    Point3d volRefPt=vd.getReferencePoint();
	    Vector3d viewpointVector=new Vector3d();
	    viewpointVector.sub(viewpoint,volRefPt);

	    // Compensate for different xyz resolution/scale, and then
			// select the axis with the greatest magnitude

	    viewpointVector.x/=vd.getXSpacing();
	    viewpointVector.y/=vd.getYSpacing();
	    viewpointVector.z/=vd.getZSpacing();

	    int axis=X_AXIS;
	    double value=viewpointVector.x;
	    double max=Math.abs(viewpointVector.x);

	    if (Math.abs(viewpointVector.y)>max) {
				axis=Y_AXIS;
				value=viewpointVector.y;
				max=Math.abs(viewpointVector.y);
	    }

	    if (Math.abs(viewpointVector.z)>max) {
				axis=Z_AXIS;
				value=viewpointVector.z;
				max=Math.abs(viewpointVector.z);
	    }

	    int direction=(value>0.0) ? FRONT : BACK;
	    if ((axis!=autoAxis)||(direction!=autoDirection)) {
				autoAxis=axis;
		 		autoDirection=direction;
				if (autoAxisEnable) autoSetAxis();
			}
		}
	}

	// =======================================================================

	private void setAutoAxisEnable(boolean flag) {

		if (autoAxisEnable!=flag) {
	    autoAxisEnable=flag;
	    if (autoAxisEnable) autoSetAxis();
		}
	}

	private void autoSetAxis() {setAxis(autoAxis,autoDirection);}
	private void setAxis(int axis, int dir) {currentAxis=axis; currentDirection=dir; axisSwitch.setWhichChild(axisIndex[currentAxis][currentDirection]);}

}
