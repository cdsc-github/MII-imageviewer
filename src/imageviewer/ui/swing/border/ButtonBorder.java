/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.UIManager;

import javax.swing.border.AbstractBorder;

import imageviewer.ui.swing.ImageViewerLookAndFeel;

// =======================================================================
// Loosely adapted from JGoodies. These buttons render true rounded
// rectangles, as opposed to the "faked" rounded rectangles that the
// original code used (based on determining, for example, the
// background color of the component, instead of the background of the
// containing object, opaqueness, etc.). Also, overrode the
// buttonPressed painting, and now use Graphics2D anti-aliased
// rendering when possible.

public class ButtonBorder extends AbstractBorder {

	protected static final Insets INSETS=new Insets(2,2,2,2);

	// =======================================================================

	public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {

		AbstractButton button=(AbstractButton)c;
		ButtonModel model=button.getModel();

		if (model.isEnabled()) {
			boolean isPressed=(model.isPressed() && model.isArmed());
			boolean isDefault=(button instanceof JButton && ((JButton)button).isDefaultButton());
			boolean isFocused=(button.isFocusPainted() && button.hasFocus());
			if (isPressed) {
				drawPressedButtonBorder(g,x,y,w,h);
			} else if (isFocused) {
				drawFocusedButtonBorder(g,x,y,w,h);
			}	else if (isDefault) {
				drawDefaultButtonBorder(g,x,y,w,h);
			}	else {
				drawPlainButtonBorder(g,x,y,w,h);
			}
		} else { 
			drawDisabledButtonBorder(g,x,y,w,h);
		}
	}

	// =======================================================================

	public Insets getBorderInsets(Component c) {return INSETS;}

	public Insets getBorderInsets(Component c, Insets newInsets) {

		newInsets.top=INSETS.top;
		newInsets.left=INSETS.left;
		newInsets.bottom=INSETS.bottom;
		newInsets.right=INSETS.right;
		return newInsets;
	}

	// =======================================================================

	protected void drawPlainButtonBorder(Graphics g, int x, int y, int w, int h) {drawButtonBorder(g,x,y,w,h,ImageViewerLookAndFeel.getControl(),ImageViewerLookAndFeel.getControlDarkShadow());}

	protected void drawPressedButtonBorder(Graphics g, int x, int y, int w, int h) {
		
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.translate(x,y);
		g2.setColor(UIManager.getColor("Button.pressed"));
		g2.drawRoundRect(0,0,w-1,h-1,6,6);
		g2.translate(-x,-y);
	}

	protected void drawDefaultButtonBorder(Graphics g, int x, int y, int w, int h) {drawFocusedButtonBorder(g,x,y,w,h);}

	protected void drawFocusedButtonBorder(Graphics g, int x, int y, int w, int h) {

		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.translate(x,y);
		g2.setColor(Color.lightGray);
		g2.drawRoundRect(0,0,w-1,h-1,6,6);
		g2.translate(-x,-y);
	}

	protected void drawDisabledButtonBorder(Graphics g, int x, int y, int w, int h) {

		drawButtonBorder(g,x,y,w,h,ImageViewerLookAndFeel.getControl(),ImageViewerLookAndFeel.getControlDarkShadow());
	}

	protected void drawButtonBorder(Graphics g, int x, int y, int w, int h, Color backgroundColor, Color edgeColor) {

		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.translate(x,y);
		g2.setColor(edgeColor);
		g2.drawRoundRect(0,0,w-1,h-1,6,6);
		g2.translate(-x,-y);
	}
}
