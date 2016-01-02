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

import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class RotationEdit extends ComponentUndoableEdit {

	double oldRotation=0, newRotation=0;
	ImagePanel source=null;

	public RotationEdit(ImagePanel source, double oldRotation, double newRotation) {

		this.source=source;
		this.oldRotation=oldRotation;
		this.newRotation=newRotation;
	}

	// =======================================================================

	public void undo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.ROTATION},new Object[] {new Float(-oldRotation)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.ROTATION},new Object[] {new Float(-oldRotation)});
	}

	public void redo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.ROTATION},new Object[] {new Float(-newRotation)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.ROTATION},new Object[] {new Float(-newRotation)});
	}

	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Rotation adjustment");}

	public Component getComponent() {return source;}
}
