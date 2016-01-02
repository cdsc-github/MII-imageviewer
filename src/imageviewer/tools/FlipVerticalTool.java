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

import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.FlipVerticalEdit;

// =======================================================================

public class FlipVerticalTool extends ImagingTool implements Tool {

	public FlipVerticalTool() {}
	
	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Flip vertical");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {execute(e);}

	// =======================================================================

	public void execute(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			RenderingProperties rp=ip.getPipelineRenderer().getRenderingProperties();
			boolean flipState=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();
			rp.setProperties(new String[] {RenderingProperties.VERTICAL_FLIP},new Object[] {new Boolean(!flipState)});
			FlipVerticalEdit fve=new FlipVerticalEdit(ip,flipState);
			ApplicationContext.postEdit(fve);
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.VERTICAL_FLIP},new Object[] {new Boolean(!flipState)});
			ip.repaint();
		}
	}
}
