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

public class FlipHorizontalEdit extends ComponentUndoableEdit {

	boolean flipState=false;
	ImagePanel source=null;

	public FlipHorizontalEdit(ImagePanel source, boolean flipState) {this.source=source; this.flipState=flipState;}

	// =======================================================================

	public void undo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.HORIZONTAL_FLIP},new Object[] {new Boolean(flipState)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.HORIZONTAL_FLIP},new Object[] {new Boolean(flipState)});
	}
	
	public void redo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.HORIZONTAL_FLIP},new Object[] {new Boolean(!flipState)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.HORIZONTAL_FLIP},new Object[] {new Boolean(!flipState)});
	}
	
	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Flip vertical");}

	public Component getComponent() {return source;}
}
