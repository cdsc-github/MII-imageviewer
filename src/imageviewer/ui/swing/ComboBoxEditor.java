/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import javax.swing.JTextField;
import javax.swing.UIManager;

import javax.swing.plaf.basic.BasicComboBoxEditor;

public class ComboBoxEditor extends BasicComboBoxEditor {

	public ComboBoxEditor() {
		editor=new JTextField("",UIManager.getInt("ComboBox.editorColumns"));
		editor.setBorder(UIManager.getBorder("ComboBox.editorBorder"));
	}
	
	static final class UIResource extends ComboBoxEditor implements javax.swing.plaf.UIResource {}
}
