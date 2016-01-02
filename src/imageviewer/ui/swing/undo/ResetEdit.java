/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.undo;

import java.awt.Component;

import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CannotRedoException;

import imageviewer.model.Image;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.WindowLevel;
import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.ui.image.ImagePanel;

// =======================================================================

public class ResetEdit extends ComponentUndoableEdit {

	double scale=0, translateX=0, translateY=0;
	boolean isVFlip=false, isHFlip=false;
	WindowLevel wl=null;
	ImagePanel source=null;
	float rotation=0;

	public ResetEdit(ImagePanel source, WindowLevel wl, double scale, double translateX, double translateY, float rotation, boolean isVFlip, boolean isHFlip) {

		this.source=source; 
		this.wl=wl;
		this.scale=scale;
		this.translateX=translateX;
		this.translateY=translateY;
		this.rotation=rotation;
		this.isVFlip=isVFlip;
		this.isHFlip=isHFlip;
	}

	// =======================================================================

	public void undo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();
		rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL,RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,
																	 RenderingProperties.ROTATION,RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP},
										 new Object[] {wl,new Double(scale),new Double(translateX),new Double(translateY),new Float(rotation),new Boolean(isVFlip),new Boolean(isHFlip)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL,RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,
																								 RenderingProperties.ROTATION,RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP},
																	 new Object[] {wl,new Double(scale),new Double(translateX),new Double(translateY),new Float(rotation),new Boolean(isVFlip),new Boolean(isHFlip)});
	}
	
	public void redo() throws CannotUndoException {

		RenderingProperties rp=source.getPipelineRenderer().getRenderingProperties();

		Image image=source.getSource();
		WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(image);
		double newScale=(double)source.getWidth()/(double)image.getWidth();
		newScale=(scale<0.01) ? 0.01 : newScale;

		rp.setProperties(new String[] {RenderingProperties.WINDOW_LEVEL,RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,
																	 RenderingProperties.ROTATION,RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP},
										 new Object[] {wl,new Double(newScale),new Double(0),new Double(0),new Float(0),new Boolean(false),new Boolean(false)});
		source.repaint();
		source.fireGroupPropertyChange(new String[] {RenderingProperties.WINDOW_LEVEL,RenderingProperties.SCALE,RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,
																								 RenderingProperties.ROTATION,RenderingProperties.VERTICAL_FLIP,RenderingProperties.HORIZONTAL_FLIP},
																	 new Object[] {wl,new Double(newScale),new Double(0),new Double(0),new Float(0),new Boolean(false),new Boolean(false)});
	}
	
	public boolean canUndo() {return true;}
	public boolean canRedo() {return true;}

	public String getPresentationName() {return new String("Reset");}

	public Component getComponent() {return source;}
}
