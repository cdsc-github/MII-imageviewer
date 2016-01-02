/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.undo;

import java.awt.Component;

import java.util.ArrayList;

import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoableEdit;

import imageviewer.model.dl.ShapeDataLayer;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class AddAnnotationEdit extends ComponentUndoableEdit {

	ImagePanel ip=null;
	ShapeDataLayer sdl=null;
	Annotation a=null;

	public AddAnnotationEdit(ImagePanel ip, ShapeDataLayer sdl, Annotation a) {this.ip=ip; this.sdl=sdl; this.a=a;}

	// =======================================================================

	public void undo() throws CannotUndoException {

		sdl.remove(a); 
		if (a.isSelected()) {
			ArrayList<ControlPoint> controlPoints=a.getControlPoints();
			if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
				for (ControlPoint cp : controlPoints) cp.getParent().remove(cp);
			}
			ApplicationContext.getContext().removeSelection(a);
		}
		ip.repaint();
	}

	public void redo() throws CannotRedoException {

		sdl.add(a); 
		if (a.isSelected()) {
			ArrayList<ControlPoint> controlPoints=a.getControlPoints();
			if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
				for (ControlPoint cp : controlPoints) ip.add(cp);
			}
			ApplicationContext.getContext().addSelection(a);
		}
		ip.repaint();
	}
	
	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Add annotation");}

	public Component getComponent() {return ip;}
}
