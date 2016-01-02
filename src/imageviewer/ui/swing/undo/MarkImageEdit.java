/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.undo;

import java.awt.Component;

import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

import imageviewer.model.Image;
import imageviewer.rendering.RenderingProperties;
import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class MarkImageEdit extends ComponentUndoableEdit {

	boolean isMarked=false;
	ImagePanel source=null;

	public MarkImageEdit(ImagePanel source, boolean isMarked) {this.source=source; this.isMarked=isMarked;}

	// =======================================================================

	public void undo() throws CannotUndoException {

		Image image=source.getSource();
		if (isMarked) {
			image.getProperties().remove(Image.MARKED);
		} else {
			image.getProperties().put(Image.MARKED,"true");
		}
		source.repaint();
	}
	
	public void redo() throws CannotUndoException {

		Image image=source.getSource();
		if (isMarked) {
			image.getProperties().put(Image.MARKED,"true");
		} else {
			image.getProperties().remove(Image.MARKED);
		}
		source.repaint();
	}
	
	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Mark image");}

	public Component getComponent() {return source;}
}
