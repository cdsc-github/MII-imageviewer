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

public class PanEdit extends ComponentUndoableEdit {

	double oldX=0, oldY=0, newX=0, newY=0;
	ImagePanel source=null;

	public PanEdit(ImagePanel source, double oldX, double oldY, double newX, double newY) {

		this.source=source;
		this.oldX=oldX;
		this.oldY=oldY;
		this.newX=newX;
		this.newY=newY;
	}

	// =======================================================================

	public void undo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
										 new Object[] {new Double(oldX),new Double(oldY)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
																	 new Object[] {new Double(oldX),new Double(oldY)});
	}

	public void redo() throws CannotRedoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
										 new Object[] {new Double(newX),new Double(newY)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
																	 new Object[] {new Double(newX),new Double(newY)});		
	}

	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Pan adjustment");}

	public Component getComponent() {return source;}
}
