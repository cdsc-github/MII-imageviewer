/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import imageviewer.model.Image;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.AutoBrightnessOperation;
import imageviewer.rendering.wl.WindowLevel;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.undo.WindowLevelEdit;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.EventObject;

import javax.media.jai.Histogram;

public class AutoSoftContrastTool extends ImagingTool implements Tool {

	// =======================================================================

	public AutoSoftContrastTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Soft Tissue Setting");}

	// =======================================================================
	// Simple algorithm that examines the histogram for the image and
	// attempts to select an appropriate auto brightness/contrast based
	// on the pixel data. To handle the potential problem of black
	// washout and noise, a pThreshold is set to find that value at
	// which 40% of the pixels are below (should be safe?). Loosely
	// taken from the base algorithm in ImageJ.

	public void mouseClicked(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			Image i=ip.getSource();
			Histogram h=i.getHistogram();
			changeWindowLevel(ip);
		}
	}

	public static void changeWindowLevel(ImagePanel ip) {
		int[] values = {400,40};
		//		int[] values=AutoBrightnessOperation.compute(h);
		if (values!=null) {
			RenderingProperties rp=ip.getPipelineRenderer().getRenderingProperties();
			WindowLevel originalWL=(WindowLevel)rp.getProperty(RenderingProperties.WINDOW_LEVEL);
			int newWindow=values[0];
			int newLevel=values[1];
			WindowLevel wl=new WindowLevel(newWindow,newLevel,originalWL.getRescaleSlope(),originalWL.getRescaleIntercept());
			wl.setRescaled(originalWL.isRescaled());
			rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {wl});
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL},new Object[] {wl});
			WindowLevelEdit wle=new WindowLevelEdit(ip,wl,originalWL);
			ApplicationContext.postEdit(wle);
			ip.repaint();
		}
		
	}

	// =======================================================================

	public void mouseDragged(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}

	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}

}

