/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Cursor;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import java.util.EventObject;

public interface Tool extends MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

	public void startTool(EventObject e);
	public void endTool(EventObject e);

	public Cursor getCursor();
	public String getToolName();
}
