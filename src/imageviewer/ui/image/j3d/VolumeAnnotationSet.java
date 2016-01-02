/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.LineStripArray;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.SharedGroup;
import javax.media.j3d.Switch;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;

import javax.vecmath.Matrix3d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;

// =======================================================================

public class VolumeAnnotationSet {

	private static Appearance DEFAULT_APP=new Appearance();
	private static NormalGenerator ng=new NormalGenerator();

	static {

		TextureAttributes textureAttributes=new TextureAttributes();
		RenderingAttributes renderingAttributes=new RenderingAttributes();
		TransparencyAttributes transparencyAttributes=new TransparencyAttributes();
		PolygonAttributes polygonAttributes=new PolygonAttributes();
		LineAttributes la=new LineAttributes(1.0f,LineAttributes.PATTERN_SOLID,true);

		textureAttributes.setTextureMode(TextureAttributes.MODULATE);
		renderingAttributes.setAlphaTestFunction(RenderingAttributes.ALWAYS);
		transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);
		transparencyAttributes.setDstBlendFunction(TransparencyAttributes.BLEND_ONE_MINUS_SRC_ALPHA);
		polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
		ColoringAttributes ca=new ColoringAttributes(1.0f,1.0f,1.0f,ColoringAttributes.SHADE_FLAT);

		DEFAULT_APP.setTransparencyAttributes(transparencyAttributes);
		DEFAULT_APP.setTextureAttributes(textureAttributes);
		DEFAULT_APP.setRenderingAttributes(renderingAttributes);
		DEFAULT_APP.setPolygonAttributes(polygonAttributes);
		DEFAULT_APP.setColoringAttributes(ca);
		DEFAULT_APP.setLineAttributes(la);
	}

	// =======================================================================

	ArrayList annotations=new ArrayList();
	BoundaryCollection bc=null;
	AnnotationRenderingEngine are=null;

	public VolumeAnnotationSet() {initialize(null);}
	public VolumeAnnotationSet(BoundaryCollection bc) {initialize(bc);}

	// =======================================================================

	private void initialize(BoundaryCollection bc) {this.bc=bc;}

	// =======================================================================

	public ArrayList getAnnotations() {return annotations;}

	public void setAnnotations(ArrayList x) {annotations=x;}
	public void setBoundaryCollection(BoundaryCollection x) {bc=x;}
	public void setAnnotationRenderingEngine(AnnotationRenderingEngine x) {are=x;}

	public static void setDefaultAppearance(Appearance x) {DEFAULT_APP=x;}

	// =======================================================================

	public void addAnnotation(VolumeAnnotation x) {annotations.add(x); if (are!=null) are.addAnnotation(x);}

	public void addAnnotation(VolumeAnnotation x, BoundaryConstraint bounds) {

		annotations.add(x); 
		if (are!=null) are.addAnnotation(x);
		if ((bounds!=null)&&(bc!=null)) bc.addTranslationBounds(x.getAnnotationTransform(),bounds);
	}

	// =======================================================================

	public static Shape3D createAnnotation(Geometry points, Appearance a) {

		points.setCapability(Geometry.ALLOW_INTERSECT);
		return new Shape3D(points,a);
	}

	// =======================================================================

	protected static Vector3f computeNormal(Point3d[] pArray, int[] stripLength) {

		GeometryInfo gi=new GeometryInfo(GeometryInfo.POLYGON_ARRAY);
		gi.setCoordinates(pArray);
		gi.setStripCounts(stripLength);
		ng.generateNormals(gi);
		Vector3f[] normals=gi.getNormals();
		int index=normals.length-1;
		index=(index<0) ? 0 : index;
		return normals[index];
	}

	// =======================================================================

	public static VolumeAnnotation createLineAnnotation(Point3d start, Point3d end) {return createLineAnnotation(start,end,DEFAULT_APP);}

	public static VolumeAnnotation createLineAnnotation(Point3d start, Point3d end, Appearance app) {

		Point3d[] pArray=new Point3d[2];
		pArray[0]=start; 
		pArray[1]=end; 

		int stripLength[]=new int[] {2};
		LineStripArray line=new LineStripArray(2,GeometryArray.COORDINATES,stripLength);
		line.setCoordinates(0,pArray,0,2);
		line.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		VolumeAnnotation va=new VolumeAnnotation(createAnnotation(line,app));
		va.setFaceNormal(0,-1,1);             // Seems to work, but I don't know why...
		return va;
	}

	// =======================================================================

	public static VolumeAnnotation createRectangleAnnotation(Point3d a, Point3d b, Point3d c, Point3d d) {return createRectangleAnnotation(a,b,c,d,DEFAULT_APP);}

	public static VolumeAnnotation createRectangleAnnotation(Point3d a, Point3d b, Point3d c, Point3d d, Appearance app) {

		Point3d[] pArray=new Point3d[5];
		pArray[0]=a; 
		pArray[1]=b; 
		pArray[2]=c; 
		pArray[3]=d; 
		pArray[4]=a; 

		int stripLength[]=new int[] {5};
		LineStripArray box=new LineStripArray(5,GeometryArray.COORDINATES,stripLength);
		box.setCoordinates(0,pArray,0,4);
		box.setCoordinate(4,pArray[0]);
		box.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		VolumeAnnotation va=new VolumeAnnotation(createAnnotation(box,app));

		// Try and guess the normal...use the last computed normal in the
		// array;

		Vector3f normal=computeNormal(pArray,stripLength);
		va.setFaceNormal(normal.x,normal.y,normal.z);
		return va;
	}

	// =======================================================================

	public static VolumeAnnotation createEllipseAnnotation(Point3d center, double rotX, double rotY, double rotZ, double majorAxis, double minorAxis) {

		return createEllipseAnnotation(center,rotX,rotY,rotZ,majorAxis,minorAxis,DEFAULT_APP);
	}

	public static VolumeAnnotation createEllipseAnnotation(Point3d center, double rotX, double rotY, double rotZ, double majorAxis, double minorAxis, Appearance a) {

		// Approximate an ellipse at a given point.

		int intervals=32;
		int segments=intervals*2;
		int length=segments+1;
		double arcLength=Math.PI/intervals;

		Transform3D t=new Transform3D();
		Vector3d scaleVector=new Vector3d(majorAxis,minorAxis,1);
		t.setScale(scaleVector);
		Transform3D xRotation=new Transform3D(); xRotation.rotX(rotX); t.mul(xRotation);
		Transform3D yRotation=new Transform3D(); yRotation.rotY(rotY); t.mul(yRotation);
		Transform3D zRotation=new Transform3D(); zRotation.rotZ(rotZ); t.mul(zRotation);
		t.setTranslation(new Vector3d(center.x,center.y,center.z));

		int[] stripLength=new int[] {length};
		LineStripArray ellipse=new LineStripArray(length,GeometryArray.COORDINATES,stripLength);
		Point3d[] pArray=new Point3d[length];

		Point3d p0=new Point3d(0.0,1.0,0.0);
		t.transform(p0);
		ellipse.setCoordinate(0,p0);
		pArray[0]=p0;

		// Make sure that the ellipse is drawn in counter-clockwise
		// direction (CCW) so that the normal is derived correctly.

		for (int loop=1; loop<segments; loop++) {
			Point3d p=new Point3d();
			pArray[loop]=p;
			p.x=Math.sin(loop*arcLength);
			p.y=Math.cos(loop*arcLength);
			p.z=0.0;
			t.transform(p);
			ellipse.setCoordinate(loop,p);	
		}
		pArray[segments]=p0;
		ellipse.setCoordinate(segments,p0); 
		VolumeAnnotation va=new VolumeAnnotation(createAnnotation(ellipse,a));

		Vector3f normal=computeNormal(pArray,stripLength);
		va.setFaceNormal(normal.x,normal.y,normal.z);
		return va;
  }

	// =======================================================================

	public VolumeAnnotation[] createWireFrameAnnotation(double x, double y, double z, double size) {

		// Generate a set of six rectangular annotations using the x,y,z
		// coordinates and given size to construct a wire frame cube.

		Point3d a=new Point3d(x,y,z);
		Point3d b=new Point3d(x,y+size,z);
		Point3d c=new Point3d(x+size,y+size,z);
		Point3d g=new Point3d(x+size,y,z);

		Point3d d=new Point3d(x+size,y+size,z+size);
		Point3d e=new Point3d(x,y+size,z+size);
		Point3d f=new Point3d(x,y,z+size);
		Point3d h=new Point3d(x+size,y,z+size);

		VolumeAnnotation[] vaArray=new VolumeAnnotation[6];
		vaArray[0]=VolumeAnnotationSet.createRectangleAnnotation(a,b,c,g);
		addAnnotation(vaArray[0]);
		vaArray[1]=VolumeAnnotationSet.createRectangleAnnotation(h,d,e,f);
		addAnnotation(vaArray[1]);
		vaArray[2]=VolumeAnnotationSet.createRectangleAnnotation(f,e,b,a);
		addAnnotation(vaArray[2]);
		vaArray[3]=VolumeAnnotationSet.createRectangleAnnotation(c,d,h,g);
		addAnnotation(vaArray[3]);
		vaArray[4]=VolumeAnnotationSet.createRectangleAnnotation(g,h,f,a);
		addAnnotation(vaArray[4]);
		vaArray[5]=VolumeAnnotationSet.createRectangleAnnotation(b,e,d,c);
		addAnnotation(vaArray[5]);
		return vaArray;
	}

	// =======================================================================

	public TransformGroup[] createDefaultClipPlanes() {

		// Generate a set of six rectangular annotations for a default
		// volume of (1.0,1.0,1.0) in size; set the corresponding bounds
		// to restrict the movement.

		Appearance app=(Appearance)DEFAULT_APP.cloneNodeComponent(true);
		ColoringAttributes ca=new ColoringAttributes(0.1f,0.8f,0.8f,ColoringAttributes.SHADE_FLAT);
		app.setColoringAttributes(ca);

		Point3d a=new Point3d(0,0,0);
		Point3d b=new Point3d(0,1,0);
		Point3d c=new Point3d(1,1,0);
		Point3d g=new Point3d(1,0,0);

		Point3d d=new Point3d(1,1,1);
		Point3d e=new Point3d(0,1,1);
		Point3d f=new Point3d(0,0,1);
		Point3d h=new Point3d(1,0,1);

		BoundaryConstraint xBounds=new BoundaryConstraint(true,false,false,new double[] {0,0,0,1,0,0});
		BoundaryConstraint yBounds=new BoundaryConstraint(false,true,false,new double[] {0,0,0,0,1,0});
		BoundaryConstraint zBounds=new BoundaryConstraint(false,false,true,new double[] {0,0,0,0,0,1});

		BoundaryConstraint xBoundsNeg=new BoundaryConstraint(true,false,false,new double[] {-1,0,0,0,0,0});
		BoundaryConstraint yBoundsNeg=new BoundaryConstraint(false,true,false,new double[] {0,-1,0,0,0,0});
		BoundaryConstraint zBoundsNeg=new BoundaryConstraint(false,false,true,new double[] {0,0,-1,0,0,0});

		TransformGroup[] tgArray=new TransformGroup[6];
		VolumeAnnotation va=VolumeAnnotationSet.createRectangleAnnotation(a,b,c,g,app);
		addAnnotation(va,zBounds);
		tgArray[0]=va.getAnnotationTransform();
		va=VolumeAnnotationSet.createRectangleAnnotation(h,d,e,f,app);
		addAnnotation(va,zBoundsNeg);
		tgArray[1]=va.getAnnotationTransform();
		va=VolumeAnnotationSet.createRectangleAnnotation(f,e,b,a,app);
		addAnnotation(va,xBounds);
		tgArray[2]=va.getAnnotationTransform();
		va=VolumeAnnotationSet.createRectangleAnnotation(c,d,h,g,app);
		addAnnotation(va,xBoundsNeg);
		tgArray[3]=va.getAnnotationTransform();
		va=VolumeAnnotationSet.createRectangleAnnotation(g,h,f,a,app);
		addAnnotation(va,yBounds);
		tgArray[4]=va.getAnnotationTransform();		
		va=VolumeAnnotationSet.createRectangleAnnotation(b,e,d,c,app);
		addAnnotation(va,yBoundsNeg);
		tgArray[5]=va.getAnnotationTransform();
		return tgArray;
	}
}
