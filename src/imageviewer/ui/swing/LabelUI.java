/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.JLabel;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLabelUI;

// =======================================================================

public class LabelUI extends MetalLabelUI {

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);
	
	public static ComponentUI createUI(JComponent b) {return new LabelUI();}

	// =======================================================================

  protected void paintEnabledText(JLabel l, Graphics g, String s, int textX, int textY) {

		Graphics2D g2=(Graphics2D)g;
		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);	
		super.paintEnabledText(l,g2,s,textX,textY);
		g2.setComposite(oldComposite);
	}

	protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY) {
		
		Graphics2D g2=(Graphics2D)g;
		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);	
		super.paintDisabledText(l,g2,s,textX,textY);
		g2.setComposite(oldComposite);
	}
}
