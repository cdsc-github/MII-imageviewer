/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalCheckBoxUI;

// =======================================================================

public class RibbonCheckBoxUI extends MetalCheckBoxUI {

	private final static RibbonCheckBoxUI RIBBON_CHECKBOX_UI=new RibbonCheckBoxUI();

	private final static ImageIcon CHECKBOX=new ImageIcon("resources/icons/swing/checkbox.png");
	private final static ImageIcon CHECKBOX_DISABLED=new ImageIcon("resources/icons/swing/checkboxDisabled.png");
	private final static ImageIcon CHECKBOX_SELECTED=new ImageIcon("resources/icons/swing/checkboxSelected.png");

	private static final Color HIGHLIGHT_COLOR=UIManager.getColor("RibbonCheckBox.highlight");
	private static final Color BORDER_COLOR=UIManager.getColor("RibbonCheckBox.borderColor");

	// =======================================================================

	public static ComponentUI createUI(JComponent b) {return RIBBON_CHECKBOX_UI;}

// =======================================================================

  public void installDefaults(AbstractButton b) {

		super.installDefaults(b); 
		icon=new RibbonCheckboxIcon(); 
		b.setOpaque(false); 
		b.setFocusPainted(false); 
		b.setRolloverEnabled(true);
		b.setForeground(UIManager.getColor("RibbonCheckBox.foreground"));
	}

	// =======================================================================

	private class RibbonCheckboxIcon implements Icon {

		public int getIconWidth()	{return 12;}
		public int getIconHeight() {return 12;}

		public void paintIcon(Component c, Graphics g, int x, int y) {
			
			JCheckBox cb=(JCheckBox)c;
			ButtonModel model=cb.getModel();
			Graphics2D g2=(Graphics2D)g;
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			if (model.isSelected()) {
				CHECKBOX_SELECTED.paintIcon(c,g,x+1,y+1);
			} else if (!model.isEnabled()) {
				CHECKBOX_DISABLED.paintIcon(c,g,x+1,y+1);
			} else {
				CHECKBOX.paintIcon(c,g,x+1,y+1);
				if (model.isRollover()) {
					g2.setColor(HIGHLIGHT_COLOR);
					g2.fillRect(x+2,y+2,9,9);
				}
			}
			g2.setColor(BORDER_COLOR);
			g2.drawRect(x,y,12,12);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
		}
	}
}
