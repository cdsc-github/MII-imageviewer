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
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class WindowLevelEdit extends ComponentUndoableEdit {

	WindowLevel oldValue=null, newValue=null;
	ImagePanel source=null;

	public WindowLevelEdit(ImagePanel source, WindowLevel oldValue, WindowLevel newValue) {this.source=source; this.oldValue=oldValue; this.newValue=newValue;}

	// =======================================================================

	public void undo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {oldValue});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {oldValue});
		
	}

	public void redo() throws CannotRedoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {newValue});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {newValue});
	}

	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Window/level adjustment");}

	public Component getComponent() {return source;}
}
