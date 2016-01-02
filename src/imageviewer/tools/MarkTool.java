/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Cursor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.EventObject;

import imageviewer.model.Image;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.WindowLevel;
import imageviewer.rendering.wl.DefaultWindowLevelManager;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.MarkImageEdit;

// =======================================================================

public class MarkTool extends ImagingTool implements Tool {

	public MarkTool() {}
	
	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Mark image");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {execute(e);}

	// =======================================================================
	// Mark the image by setting a specific variable within the image
	// construct.  This will trigger a display within the header layer
	// and can be used by other tools (like presentation states, etc.).

	public void execute(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			Image image=ip.getSource();
			String isMarked=(String)image.getProperties().get(Image.MARKED);
			MarkImageEdit mie=null;
			if (isMarked==null) {
				image.getProperties().put(Image.MARKED,"true");
				mie=new MarkImageEdit(ip,true);
			} else {
				image.getProperties().remove(Image.MARKED);
				mie=new MarkImageEdit(ip,false);
			}
			ApplicationContext.postEdit(mie);
			ip.repaint();
		}
	}
}
