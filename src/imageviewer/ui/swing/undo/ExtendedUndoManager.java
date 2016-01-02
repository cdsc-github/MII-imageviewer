/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.undo;

import java.awt.Component;
import javax.swing.SwingUtilities;

import javax.swing.undo.UndoableEdit;
import javax.swing.undo.UndoManager;

// =======================================================================
// Really simple extension to the undo manager that lets us scan the
// contents of the undo stack and remove any specific edits that may
// involve a given component. Useful so that we don't hang on to any
// image panels, etc. that might be left over after closing a
// tab/panel. Note that a really dumb mechanism is used to safely
// remove the edits via trimEdits(x,x) because the UndoManager doesn't
// let us remove it otherwise without bombing (private variables
// suck).

public class ExtendedUndoManager extends UndoManager {

	public void removeComponentEdits(Component c) {

		for (int loop=edits.size()-1; loop>=0; loop--) {
			UndoableEdit ue=(UndoableEdit)edits.elementAt(loop);
			if (ue instanceof ComponentUndoableEdit) {
				ComponentUndoableEdit cue=(ComponentUndoableEdit)ue;
				if (SwingUtilities.isDescendingFrom(cue.getComponent(),c)) trimEdits(loop,loop);
			}
		}
	}
}
