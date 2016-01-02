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
import imageviewer.ui.swing.undo.ResetEdit;

// =======================================================================

public class ResetTool extends ImagingTool implements Tool {

	public ResetTool() {}
	
	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Reset image");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {execute(e);}

	// =======================================================================
	// Reset the image scale to the appropriate scale for the image
	// panel, the default window level, translation to (0,0), and the
	// rotation to 0. Remove any vertical/horizontal flips.

	public void execute(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			RenderingProperties rp=ip.getPipelineRenderer().getRenderingProperties();

			WindowLevel wl=(WindowLevel)rp.getProperty(RenderingProperties.WINDOW_LEVEL);
			double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
			double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
			double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
			float rotationAngle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();	
			boolean isVFlip=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();	
			boolean isHFlip=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();	

			Image image=ip.getSource();
			WindowLevel newWL=DefaultWindowLevelManager.getDefaultWindowLevel(image);
			double newScale=(double)ip.getWidth()/(double)image.getWidth();
			newScale=(scale<0.01) ? 0.01 : newScale;

			rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL,RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,
																		 RenderingProperties.ROTATION,RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP},
											 new Object[] {newWL,new Double(newScale),new Double(0),new Double(0),new Float(0),new Boolean(false),new Boolean(false)});
			
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL,RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,
																							 RenderingProperties.ROTATION,RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP},
																 new Object[] {newWL,new Double(newScale),new Double(0),new Double(0),new Float(0),new Boolean(false),new Boolean(false)});

			ResetEdit re=new ResetEdit(ip,wl,scale,translateX,translateY,rotationAngle,isVFlip,isHFlip);
			ApplicationContext.postEdit(re);
			ip.repaint();
		}
	}
}
