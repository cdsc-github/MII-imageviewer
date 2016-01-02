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
import javax.swing.undo.UndoableEdit;

import imageviewer.model.ImageSequence;
import imageviewer.ui.image.ImagePanelGroup;

// =======================================================================

public class AdvanceEdit extends ComponentUndoableEdit {

	ImagePanelGroup source=null;
	int advanceAmount=0;

	public AdvanceEdit(ImagePanelGroup source, int advanceAmount) {

		this.source=source;
		this.advanceAmount=advanceAmount;
	}

	// =======================================================================

	public void undo() throws CannotUndoException {source.advance(-advanceAmount);}
	public void redo() throws CannotRedoException {source.advance(advanceAmount);}

	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Cine advance");}

	public ImagePanelGroup getSource() {return source;}
	public int getAdvanceAmount() {return advanceAmount;}

	public Component getComponent() {return source;}

	// =======================================================================
	// Collapse the specified event with this undoableEdit if the type
	// is the same (i.e., a cine event) and the source is the same panel
	// group.

	public boolean addEdit(UndoableEdit anEdit) {

		if (anEdit instanceof AdvanceEdit) {
			AdvanceEdit ae=(AdvanceEdit)anEdit;
			if (ae.getSource()==source) {advanceAmount+=ae.getAdvanceAmount(); return true;}
		}
		return false;
	}
}
