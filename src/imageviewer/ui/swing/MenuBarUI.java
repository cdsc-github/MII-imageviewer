/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicMenuBarUI;

import org.jdesktop.swingx.painter.Painter;

// =======================================================================

public class MenuBarUI extends BasicMenuBarUI {

  public static ComponentUI createUI(JComponent c) {c.setBorder(new EmptyBorder(1,5,1,5)); return new MenuBarUI();}

  public void paint(Graphics g, JComponent c) {

	  int width=c.getWidth();
	  int height=c.getHeight();
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		Painter p=(Painter)UIManager.get("MenuBar.backgroundPainter");
		p.paint(g2,c,width,height);
		g2.setColor(Color.black);
		g2.drawLine(0,height-1,width,height-1);
		g2.dispose();
	}
}
  
  
