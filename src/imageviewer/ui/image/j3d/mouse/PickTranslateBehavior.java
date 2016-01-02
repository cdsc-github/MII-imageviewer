/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d.mouse;

import java.awt.AWTEvent;
import java.awt.event.MouseEvent;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.WakeupCondition;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOr;

import javax.vecmath.Point3d;
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickingCallback;

import imageviewer.ui.image.j3d.BoundaryCollection;
import imageviewer.ui.image.j3d.BoundaryConstraint;

// =======================================================================

public class PickTranslateBehavior extends PickMouseBehavior implements BoundaryCollection {

	public static final int X_MIN=0;
	public static final int Y_MIN=1;
	public static final int Z_MIN=2;
	public static final int X_MAX=3;
	public static final int Y_MAX=4;
	public static final int Z_MAX=5;

	// =======================================================================

	Transform3D currentTransform=new Transform3D(), viewpointTransform=new Transform3D();
	Transform3D translationMatrix=new Transform3D(), t3d=new Transform3D();
	Vector3d translation=new Vector3d(), boundVector=new Vector3d();

	TransformGroup rootTG=null, currentTG=null;
  WakeupCondition wc=null;
	PickingCallback callback=null;
	PickResult pr=null;

	Hashtable translationBounds=new Hashtable();
	
	double xFactor=0.0015, yFactor=0.0015, deltaX=0, deltaY=0;
	int x=0, y=0, xLast=0, yLast=0;

	// =======================================================================

  public PickTranslateBehavior(Canvas3D canvas3D, BranchGroup bg, TransformGroup rootTG, Bounds b) {

		super(canvas3D,bg,b);
		initialize(canvas3D,bg,rootTG,b,PickTool.GEOMETRY);
	}

  public PickTranslateBehavior(Canvas3D canvas3D, BranchGroup bg, TransformGroup rootTG, Bounds b, int pickMode) {

		super(canvas3D,bg,b);
		initialize(canvas3D,bg,rootTG,b,pickMode);
	}

	// =======================================================================

	private void initialize(Canvas3D canvas3D, BranchGroup bg, TransformGroup rootTG, Bounds b, int pickMode) {

		this.setSchedulingBounds(b);
		this.pickMode=pickMode;
		this.rootTG=rootTG;

		WakeupCriterion criterionArray[]=new WakeupCriterion[2];
    criterionArray[0]=new WakeupOnAWTEvent(MouseEvent.MOUSE_PRESSED);
    criterionArray[1]=new WakeupOnAWTEvent(MouseEvent.MOUSE_DRAGGED);
    wc=new WakeupOr(criterionArray);
	}

	// =======================================================================

	public void addTranslationBounds(TransformGroup tg, BoundaryConstraint b) {translationBounds.put(tg,b);}
	public BoundaryConstraint getTranslationBounds(TransformGroup tg) {return (BoundaryConstraint)translationBounds.get(tg);}

	// =======================================================================

  public double getXFactor() {return xFactor;}
  public double getYFactor() {return yFactor;}

	public float getTolerance() {return pc.getTolerance();}
	public int getPickMode() {return pickMode;}

	public void setFactor(double factor) {xFactor=yFactor=factor;}
  public void setFactor(double xFactor, double yFactor) {xFactor=xFactor; yFactor=yFactor;}
	public void setPickMode(int x) {pickMode=x;}
	public void setupCallback(PickingCallback x) {callback=x;}
	public void setTolerance(float x) {pc.setTolerance(x);}

	// =======================================================================
	// Override the superclass processStimulus so that we have access to
	// the processAWTevent calls and therefore capture the required drag
	// information.  Java3D just has issues when dealing with mouse
	// interaction...

  public void processStimulus(Enumeration criteria) {

    while (criteria.hasMoreElements()) {
      WakeupCriterion wakeUp=(WakeupCriterion)criteria.nextElement();
      if (wakeUp instanceof WakeupOnAWTEvent) {
        AWTEvent[] evt=((WakeupOnAWTEvent)wakeUp).getAWTEvent();
        try {
					processAWTEvent(evt);
        } catch (Exception exc) {
					exc.printStackTrace();
        }
      }
    }
    wakeupOn(wc);
  }	

	// =======================================================================
	// Take care of computing the inverse transform on the root so that
	// the movements in the translation make sense relative to the user,
	// and not to any rotated axes.

	protected void processAWTEvent(AWTEvent[] events) {

		for (int j=0; j<events.length; j++) {

      if (events[j] instanceof MouseEvent) {
        MouseEvent eventPress=(MouseEvent)events[j];
				if (eventPress.getModifiers()==MouseEvent.BUTTON3_MASK) {

          if (eventPress.getID()==MouseEvent.MOUSE_PRESSED) {
            pc.setShapeLocation(eventPress);
						currentTG=null;
						PickResult[] prArray=pc.pickAllSorted();
						if (prArray!=null) {
							boolean flag=true;
							for (int i=0, n=prArray.length; (i<n && flag) ; i++) {
								pr=prArray[i];
								TransformGroup tg=(TransformGroup)pr.getNode(PickResult.TRANSFORM_GROUP);
								if (tg!=null) {
									if ((tg.getCapability(TransformGroup.ALLOW_TRANSFORM_READ)) && (tg.getCapability(TransformGroup.ALLOW_TRANSFORM_WRITE))) {
										currentTG=tg;
										x=xLast=eventPress.getX();
										y=yLast=eventPress.getY();
										flag=false;
									}
								}
							}
						}

          } else if ((eventPress.getID()==MouseEvent.MOUSE_DRAGGED)&&(currentTG!=null)) {
						
						// At this point, we need to take into consideration the
						// viewpoint and alter the values such that the underlying
						// transform corresponds to the viewer's perspective, and
						// not the rotated axes.  Also, change in x/y axes, taking
						// into account the factor amount.

            x=eventPress.getX();
            y=eventPress.getY();
						deltaX=(x-xLast)*xFactor;  
						deltaY=(y-yLast)*yFactor;

						rootTG.getTransform(viewpointTransform);
						viewpointTransform.setTranslation(new Vector3d());
						viewpointTransform.invert();
						currentTG.getTransform(currentTransform);
						currentTransform.mul(viewpointTransform,currentTransform);

						// Compute the translation portion; note that Y is
						// reversed because of the direction of the graphics plane
						// relative to how the user views things.  I have *no*
						// idea why this work as it does; I would have thought
						// that you need to augment the translation, rather than
						// set it -- but this appears to work correctly.

						currentTransform.get(translation);
						Vector3d angles=getRotationAngles(viewpointTransform);

						double xTrans=deltaX*Math.cos(-angles.y)*Math.cos(-angles.z)+deltaY*(-Math.sin(-angles.z));
						double yTrans=-deltaX*Math.sin(-angles.z)-deltaY*Math.cos(-angles.x)*Math.cos(-angles.z);
						double zTrans=deltaX*Math.sin(-angles.y)+deltaY*Math.sin(-angles.x);

						// Now revert the viewpoint transform back...

						viewpointTransform.invert();

						// Check the bounds after computing the new matrix in a
						// temporary matrix.  Basically, temporarily compute what
						// the new translation bounds would be; if they exceed the
						// discovered bounds, then set the change to 0.

						BoundaryConstraint bc=(BoundaryConstraint)translationBounds.get(currentTG);

						if (bc!=null) {
							translation.x=xTrans;
							translation.y=yTrans;
							translation.z=zTrans;
							translationMatrix.set(translation);
							t3d.mul(currentTransform,translationMatrix);
							t3d.mul(viewpointTransform,t3d);
							t3d.get(boundVector);
							double[] bounds=bc.getBounds();
							xTrans=(((boundVector.x<bounds[X_MIN])||(boundVector.x>bounds[X_MAX])) ? 0 : xTrans);
							yTrans=(((boundVector.y<bounds[Y_MIN])||(boundVector.y>bounds[Y_MAX])) ? 0 : yTrans);
							zTrans=(((boundVector.z<bounds[Z_MIN])||(boundVector.z>bounds[Z_MAX])) ? 0 : zTrans);
						}

						translation.x=xTrans;
						translation.y=yTrans;
						translation.z=zTrans;
						translationMatrix.set(translation);
						currentTransform.mul(currentTransform,translationMatrix);

						// Add the viewpoint back into the overall transform.  Set
						// the transform and execute any updates...
									
						currentTransform.mul(viewpointTransform,currentTransform);
						currentTG.setTransform(currentTransform);
						updateScene(x,y);
						xLast=x;
						yLast=y;

          }
				}
			}
    }
	}

	// =======================================================================
	
	public void updateScene(int xpos, int ypos)	{if (callback!=null)	callback.transformChanged(PickingCallback.TRANSLATE,currentTG);}

	// =======================================================================

	public void transformChanged(int type, Transform3D transform) {if (callback!=null) callback.transformChanged(PickingCallback.TRANSLATE,currentTG);}

	// =======================================================================

	public Vector3d getRotationAngles(Transform3D t3D) {

		Matrix3d m1=new Matrix3d();
		Vector3d angles=new Vector3d();
		double tRx=0, tRy=0;
	
		t3D.get(m1);
		angles.y=Math.asin(m1.getElement(0,2));
		double c=Math.cos(angles.y);

		if (Math.abs(c)>0.00001) {
			tRx=m1.getElement(2,2)/c;
			tRy=-m1.getElement(1,2)/c;
			angles.x=Math.atan2(tRy,tRx);
			tRx=m1.getElement(0,0)/c;
			tRy=-m1.getElement(0,1)/c;
			angles.z=Math.atan2(tRy,tRx);
		}	else {
			angles.x=0.0;
			tRx=m1.getElement(1,1)/c;
			tRy=m1.getElement(1,0)/c;
			angles.z=Math.atan2(tRy,tRx);
		}

		if (angles.x<0.0)	{
			angles.x+=2*Math.PI;
		}	else if (angles.x>(2*Math.PI)) {
			angles.x-=2*Math.PI;
		}
		if (angles.y<0.0) 	{
			angles.y+=2*Math.PI;
		}	else if (angles.y>(2*Math.PI)) {
			angles.y-=2*Math.PI;
		}
		if (angles.z < 0.0)	{
			angles.z+=2*Math.PI;
		}	else if (angles.z>(2*Math.PI)) {
			angles.z-=2*Math.PI;
		}

		if ((angles.x<0.001)&&(angles.x>-0.001)) angles.x=0.0;
		if ((angles.y<0.001)&&(angles.y>-0.001)) angles.y=0.0;
		if ((angles.z<0.001)&&(angles.z>-0.001)) angles.z=0.0;

		if (angles.x==0.0) angles.x=Math.abs(angles.x);
		if (angles.y==0.0) angles.y=Math.abs(angles.y);
		if (angles.z==0.0) angles.z=Math.abs(angles.z);

		if (angles.x==2*Math.PI) angles.x=0.0;
		if (angles.y==2*Math.PI) angles.y=0.0;
		if (angles.z==2*Math.PI) angles.z=0.0;

		return angles;
	}
}

