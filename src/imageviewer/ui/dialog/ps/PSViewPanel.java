/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog.ps;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable; 

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import java.util.ArrayList;

import javax.swing.JPanel;

import imageviewer.model.Image;
import imageviewer.model.PresentationState;

import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingOpPipeline;
import imageviewer.rendering.RenderingOpPipelineFactory;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.annotation.Annotation;
import imageviewer.ui.image.BasicImagePanel;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.image.ImagePanelTransferHandler;

// =======================================================================

public class PSViewPanel extends BasicImagePanel implements DropTargetListener {

	DropTarget dt=null;
	PresentationState ps=null;
	RenderingProperties rp=new RenderingProperties();
	float scaleFactor=1.0f;

	public PSViewPanel() {

		super(); 
		setBackground(Color.black); 
		setOpaque(true); 
		setSize(512,512);
		RenderingOpPipeline rop=RenderingOpPipelineFactory.create();
		ipr=new ImagePipelineRenderer(rop,rp);
		dt=new DropTarget(this,this);
	}

	// =======================================================================

	public DropTarget getDropTarget() {return dt;}

	// =======================================================================

	public PresentationState getPresentationState() {return ps;}

	public void setPresentationState(PresentationState ps) {

		this.ps=ps;
		if (source==null) return;
		if (ps==null) return;
		WindowLevel wl=new WindowLevel((int)ps.getWindowCenter(),(int)ps.getWindowLevel(),ps.getRescaleSlope(),ps.getRescaleIntercept());
		Boolean horizontalFlip=new Boolean((ps.getImageHorizontalFlip().equals("Y")) ? true : false);
		short[] translateCoords=ps.getDisplayedAreaTopLeftHandCorner();
		float scale=ps.getPresentationPixelMagnificationRatio();
		
		// Rescale the scaling factor to take into consideration the new
		// view panel size vs. what it was before.  We may also need to
		// clip the region?

		short[] psViewSize=ps.getDisplayedAreaBottomRightHandCorner();
		scaleFactor=(float)getWidth()/(float)psViewSize[0];
				
		rp.setProperties(new String[] {RenderingProperties.HORIZONTAL_FLIP,RenderingProperties.ROTATION,RenderingProperties.SCALE,
																	 RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y,RenderingProperties.WINDOW_LEVEL,
																	 RenderingProperties.MAX_PIXEL,RenderingProperties.SOURCE_WIDTH,RenderingProperties.SOURCE_HEIGHT},
			                             new Object[] {horizontalFlip,new Float(Math.toRadians(ps.getImageRotation())),new Double(scale*scaleFactor),
																								 new Double(translateCoords[0]*scaleFactor),new Double(translateCoords[1]*scaleFactor),wl,
																								 new Integer(source.getMaxPixelValue()),new Integer(source.getWidth()),new Integer(source.getHeight())});
		repaint();
	}

	public void groupPropertyChange(Object source, String[] propertyNames, Object[] values) {}

	// =======================================================================

	public void paintComponent(Graphics g) {

		if ((ps!=null)&&(scaleFactor>1)) {
			short[] psViewSize=ps.getDisplayedAreaBottomRightHandCorner();
			g.clipRect(0,0,(int)scaleFactor*psViewSize[0],(int)scaleFactor*psViewSize[1]);
		}
		Graphics2D g2=(Graphics2D)g;
		paintComponent(g2,false);
		if (ps!=null) {
			ArrayList<Annotation> annotations=ps.getAnnotations();
			if (annotations!=null) SVL.paintLayer(annotations,this,g2);
		}			
	}

	// =======================================================================

	public void flush() {doCleanup(); ipr.doCleanup(); ipr=null; source=null; ps=null;}

	// =======================================================================
	// DropTargetListener interface methods

	public void dragExit(DropTargetEvent dte) {}
  public void dragEnter(DropTargetDragEvent dtde) {}
  public void dragOver(DropTargetDragEvent dtde) {}
  public void dropActionChanged(DropTargetDragEvent dtde) {}

  public void drop(DropTargetDropEvent dtde) {

		try {
			Transferable t=dtde.getTransferable();
			if (t.isDataFlavorSupported(ImagePanelTransferHandler.IMAGE_PANEL_MIME_FLAVOR)) {
				dtde.acceptDrop(DnDConstants.ACTION_COPY);
				ImagePanel ip=(ImagePanel)t.getTransferData(ImagePanelTransferHandler.IMAGE_PANEL_MIME_FLAVOR);
				dtde.getDropTargetContext().dropComplete(true);
				PSDialogPanel.getInstance().createNewPresentationState(ip);
			} else {
				dtde.rejectDrop();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
