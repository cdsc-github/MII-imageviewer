/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JPanel;

// =======================================================================

public abstract class UIPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	public UIPanel() {setLayout(null); setBackground(Color.black);}

	// =======================================================================

	public abstract void mouseClicked(MouseEvent e); 
	public abstract void mousePressed(MouseEvent e); 
	public abstract void mouseReleased(MouseEvent e);
	public abstract void mouseEntered(MouseEvent e);
	public abstract void mouseExited(MouseEvent e);
	public abstract void mouseMoved(MouseEvent e); 
	public abstract void mouseDragged(MouseEvent e);
	public abstract void mouseWheelMoved(MouseWheelEvent e);

	public abstract void keyPressed(KeyEvent e);
	public abstract void keyReleased(KeyEvent e);
	public abstract void keyTyped(KeyEvent e);

	// =======================================================================

	public Dimension getPreferredSize() {return new Dimension(getWidth(),getHeight());}
	public Dimension getMinimumSize() {return new Dimension(getWidth(),getHeight());}
	public Dimension getMaximumSize() {return new Dimension(getWidth(),getHeight());}

	// =======================================================================
}
