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
import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.annotation.Selectable;
import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class DeleteAnnotationEdit extends ComponentUndoableEdit {

	ImagePanel ip=null;
	ShapeDataLayer sdl=null;
	ArrayList<Selectable> al=null;

	public DeleteAnnotationEdit(ImagePanel ip, ShapeDataLayer sdl, ArrayList<Selectable> al) {this.ip=ip; this.sdl=sdl; this.al=al;}

	// =======================================================================

	public void undo() throws CannotUndoException {

		for (Selectable s : al) {
			s.select(); 
			sdl.add(s);
			ArrayList<ControlPoint> controlPoints=s.getControlPoints();
			if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
				for (ControlPoint cp : controlPoints) ip.add(cp);
			}
			ApplicationContext.getContext().addSelection(s);
		}
		ip.repaint();
	}

	public void redo() throws CannotRedoException {

		for (Selectable s : al) {
			if (s.isSelected()) {
				ArrayList<ControlPoint> controlPoints=s.getControlPoints();
				if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
					for (ControlPoint cp : controlPoints) cp.getParent().remove(cp);
				}
			}
			ApplicationContext.getContext().removeSelection(s);
			s.deselect(); 
			sdl.remove(s);
		} 
		ip.repaint();
	}
	
	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Delete annotation");}

	public Component getComponent() {return ip;}

}
