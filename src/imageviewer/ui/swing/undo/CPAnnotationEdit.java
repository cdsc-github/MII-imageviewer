/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.undo;

import java.awt.Component;
import java.awt.Point;
import java.awt.geom.Point2D;

import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class CPAnnotationEdit extends ComponentUndoableEdit {

	Point2D.Double startPoint=null, endPoint=null, offset1=null, offset2=null;
	ControlPoint cp=null;
	ImagePanel ip=null;

	public CPAnnotationEdit(ImagePanel ip, ControlPoint cp, Point2D.Double startPoint, Point2D.Double endPoint, Point2D.Double offset1, Point2D.Double offset2) {

		this.ip=ip;
		this.cp=cp;
		this.startPoint=startPoint;
		this.endPoint=endPoint;
		this.offset1=offset1;
		this.offset2=offset2;
	}

	// =======================================================================

	public void undo() throws CannotUndoException {cp.doMove(new Point((int)(startPoint.x-endPoint.x+offset1.x),(int)(startPoint.y-endPoint.y+offset1.y))); ip.repaint();}
	public void redo() throws CannotRedoException {cp.doMove(new Point((int)(endPoint.x-startPoint.x+offset1.x),(int)(endPoint.y-startPoint.y+offset1.y))); ip.repaint();}
	
	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Edit annotation");}
	public Component getComponent() {return ip;}
}
