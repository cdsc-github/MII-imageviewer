/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d.mouse;

import java.awt.Event;
import java.awt.AWTEvent;
import java.awt.event.MouseEvent;

import java.util.Enumeration;

import javax.media.j3d.Behavior;
import javax.media.j3d.Bounds;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.WakeupCriterion;
import javax.media.j3d.WakeupOnAWTEvent;
import javax.media.j3d.WakeupOr;

import com.sun.j3d.utils.picking.PickCanvas;
import com.sun.j3d.utils.picking.PickTool;

// =======================================================================

public abstract class PickMouseBehavior extends Behavior {

	protected PickCanvas pc=null;
	protected WakeupCriterion[] conditions=null;
	protected WakeupOr wakeupCondition=null;
	protected boolean buttonPress=false;
	protected Bounds bounds=null;
	protected int pickMode=PickTool.GEOMETRY;

	protected TransformGroup currentTransformGroup=new TransformGroup();
	protected MouseEvent mevent=null;
	protected Canvas3D canvas=null;
	
	public PickMouseBehavior(Canvas3D canvas, BranchGroup bg, Bounds bounds) {

		super();
		currentTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		currentTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		bg.addChild(currentTransformGroup);
		pc=new PickCanvas(canvas,bg);
		this.bounds=bounds;
		this.canvas=canvas;
	}

	// =======================================================================

	public void initialize() {

		conditions=new WakeupCriterion[2];
		conditions[0]=new WakeupOnAWTEvent(Event.MOUSE_MOVE);
		conditions[1]=new WakeupOnAWTEvent(Event.MOUSE_DOWN);
		wakeupCondition=new WakeupOr(conditions);
		wakeupOn(wakeupCondition);
	}

	// =======================================================================

	private void processMouseEvent(MouseEvent me) {buttonPress=(me.getID()==MouseEvent.MOUSE_PRESSED | me.getID()==MouseEvent.MOUSE_CLICKED) ? true : false;}

	// =======================================================================

	public void processStimulus(Enumeration criteria) {

		WakeupCriterion wakeup=null;
		AWTEvent[] me=null;
		int xPos=0, yPos=0;

		while(criteria.hasMoreElements())	{
			wakeup=(WakeupCriterion)criteria.nextElement();
			if (wakeup instanceof WakeupOnAWTEvent) me=((WakeupOnAWTEvent)wakeup).getAWTEvent();
		}

		if (me[0] instanceof MouseEvent) {
			mevent=(MouseEvent)me[0];
			processMouseEvent((MouseEvent)me[0]);
			xPos=mevent.getPoint().x;
			yPos=mevent.getPoint().y;
		}

		if (buttonPress) updateScene(xPos,yPos);
		wakeupOn(wakeupCondition);
	}

	// =======================================================================

	public abstract void updateScene(int xPos, int yPos);

}
