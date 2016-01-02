/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTextAreaUI;

// =======================================================================

public class TextAreaUI extends BasicTextAreaUI {

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

	public static ComponentUI createUI(JComponent c) {return new TextAreaUI();}

	// =======================================================================

	public void paintSafely(Graphics g) {

		Graphics2D g2=(Graphics2D)g;
		g2.setComposite(DEFAULT_AC);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,TextRenderer.AA_VALUE);	
		super.paintSafely(g2);
	}
}
