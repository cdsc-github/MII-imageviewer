/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalToolBarUI;

import org.jdesktop.swingx.painter.Painter;

// =======================================================================

public class ToolbarUI extends MetalToolBarUI {

	private Border border=new EmptyBorder(1,1,1,1);

	private final static ToolbarUI UI_INSTANCE=new ToolbarUI();

	private ToolbarUI() {}

	public static ComponentUI createUI(JComponent b) {return UI_INSTANCE;}

	// =======================================================================

	public void installUI(JComponent c) {super.installUI(c); c.putClientProperty("JToolBar.isRollover",Boolean.TRUE);}

	// =======================================================================

	protected void setBorderToRollover(Component c) {

		if (c instanceof AbstractButton) {
			super.setBorderToRollover(c);
		} else if (c instanceof Container) {
			Container cont=(Container)c;
			for (int i=0, n=cont.getComponentCount(); i<n; i++) super.setBorderToRollover(cont.getComponent(i));
		}
	}

	// =======================================================================

	protected void setBorderToNormal(Component c) {

		if (c instanceof AbstractButton) {
			AbstractButton b=(AbstractButton) c;
			b.setBorder(border);
			b.putClientProperty("JToolBar.isToolbarButton", Boolean.TRUE);
		}
	}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

	  int width=c.getWidth();
	  int height=c.getHeight();
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		Painter p=(Painter)UIManager.get("ToolBar.backgroundPainter");
		if (p!=null) p.paint(g2,c,width,height);
		g2.dispose();
	}
}
