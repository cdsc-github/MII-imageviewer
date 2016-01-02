/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.SwingConstants;
import javax.swing.plaf.metal.MetalScrollButton;

// =======================================================================

public class ScrollArrowButton extends MetalScrollButton implements SwingConstants {

	int btnWidth, btnHeight;

	public ScrollArrowButton(int direction, int width, boolean freeStanding) {

		super(direction,width,freeStanding);
		setOpaque(false);
		if (direction==NORTH || direction==SOUTH) {btnWidth=11;	btnHeight=14;} else {btnWidth=14;	btnHeight=11;}
	}

	public Dimension getMaximumSize() {return this.getPreferredSize();}
	public Dimension getMinimumSize() {return this.getPreferredSize();}
  public Dimension getPreferredSize() {return new Dimension(btnWidth,btnHeight);}

	public void repaint(long tm, int x, int y, int width, int height) {if (getParent()!=null) getParent().repaint();}

	public void paint(Graphics g) {}
}
