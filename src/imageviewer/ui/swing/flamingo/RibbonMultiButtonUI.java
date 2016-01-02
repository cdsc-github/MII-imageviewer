/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.ui.BasicCommandButtonUI;

// =======================================================================
// The multibutton should do no painting or mouse stuff, really; all
// the drawing is done by the contained button elements.

public class RibbonMultiButtonUI extends BasicCommandButtonUI {

	public static ComponentUI createUI(JComponent c) {return new RibbonMultiButtonUI();}

	// =======================================================================

	public void installUI(JComponent c) {

		commandButton=(JCommandButton)c; 
		c.setOpaque(false); 
		c.setLayout(new BorderLayout(0,0)); 
		c.setBorder(new EmptyBorder(0,0,0,0));
		if (commandButton.getState()!=null) updateState(commandButton.getState(),false);
	}

	protected void installListeners() {}
	protected void uninstallListeners() {}
	protected void installComponents() {}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {}

}
