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

import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;

// =======================================================================

public class MouseRotator extends MouseBehavior {

	double xAngle=0, yAngle=0, xFactor=0.03, yFactor=0.03;
	MouseCondition mc=null;
  MouseBehaviorCallback mbc=null;

  public MouseRotator() {super(0);}
  public MouseRotator(TransformGroup transformGroup) {super(transformGroup);}
  public MouseRotator(int flags) {super(flags);}

	// =======================================================================

  public void initialize() {
    super.initialize();
    if ((flags & INVERT_INPUT)==INVERT_INPUT) {
			invert=true;
			xFactor*=-1;
			yFactor*=-1;
    }
  }

	// =======================================================================

  public double getXFactor() {return xFactor;}
  public double getYFactor() {return yFactor;}

	public void setFactor(double factor) {xFactor=yFactor=factor;}
  public void setFactor(double xFactor, double yFactor) {xFactor=xFactor; yFactor=yFactor;}
	public void setMouseCondition(MouseCondition x) {mc=x;}

	// =======================================================================

  public void processStimulus (Enumeration criteria) {

		WakeupCriterion wakeup=null;
		AWTEvent[] event=null;
		int id=0, dx=0, dy=0;

		while (criteria.hasMoreElements()) {
			wakeup=(WakeupCriterion)criteria.nextElement();
			if (wakeup instanceof WakeupOnAWTEvent) {
				event=((WakeupOnAWTEvent)wakeup).getAWTEvent();
				MouseEvent lastDragEvent=null;
				for (int i=0; i<event.length; i++) { 
					processMouseEvent((MouseEvent)event[i]);
					if (((buttonPress)&&((flags & MANUAL_WAKEUP)==0)) || ((wakeUp)&&((flags & MANUAL_WAKEUP)!=0)) && (mc!=null)) {
						id=event[i].getID();
						if (mc.evaluateMouseEvent((MouseEvent)event[i])) {
							lastDragEvent=(MouseEvent)event[i];
						} else if (id==MouseEvent.MOUSE_PRESSED) {
							x_last=((MouseEvent)event[i]).getX();
							y_last=((MouseEvent)event[i]).getY();
						}
					}
				}
				if (lastDragEvent!=null) {
					x=lastDragEvent.getX();
					y=lastDragEvent.getY();
					dx=x-x_last;
					dy=y-y_last;
					if (!reset){	    
						xAngle=dy*yFactor;
						yAngle=dx*xFactor;
						transformX.rotX(xAngle);
						transformY.rotY(yAngle);
						transformGroup.getTransform(currXform);
						Matrix4d mat=new Matrix4d();
						currXform.get(mat);
						currXform.setTranslation(new Vector3d(0.0,0.0,0.0));
						if (invert) {
							currXform.mul(currXform,transformX);
							currXform.mul(currXform,transformY);
						} else {
							currXform.mul(transformX,currXform);
							currXform.mul(transformY,currXform);
						}
						Vector3d translation=new Vector3d(mat.m03,mat.m13,mat.m23);
						currXform.setTranslation(translation);
						transformGroup.setTransform(currXform);
						transformChanged(currXform);
						if (mbc!=null) mbc.transformChanged(MouseBehaviorCallback.TRANSLATE,currXform);
					} else {
						reset=false;
					}
					x_last=x;
					y_last=y;
	      }
			}
		}
		wakeupOn(mouseCriterion);
	}

	// =======================================================================

  public void transformChanged(Transform3D transform) {}
  public void setupCallback(MouseBehaviorCallback mbc) {this.mbc=mbc;}
}

