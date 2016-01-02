/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTextArea;
import imageviewer.ui.swing.TextRenderer;

// =======================================================================

public class DialogUtil {

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC,1.0f);
	
	public static JTextArea createTextArea(String text) {

		JTextArea ta=new JTextArea(text) {
			public void paint(Graphics g) {
				Graphics2D g2=(Graphics2D)g; 
				g2.setComposite(DEFAULT_AC); 
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,TextRenderer.AA_VALUE);
				// if (TextRenderer.AA_TEXT_LCD_CONTRAST!=null) g2.setRenderingHint((RenderingHints.Key)TextRenderer.AA_TEXT_LCD_CONTRAST,new Integer(100));
				super.paint(g2);
			}
		};
		ta.setColumns(30);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		ta.setRows(ta.getLineCount());
		ta.setEditable(false);
		ta.setMaximumSize(ta.getPreferredSize());
		return ta;
	}

	// =======================================================================

	private DialogUtil() {}
}
