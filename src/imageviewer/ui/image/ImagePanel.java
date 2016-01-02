/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;

import java.util.ArrayList;

import javax.swing.TransferHandler;

import imageviewer.model.DataLayer;
import imageviewer.model.Image;
import imageviewer.model.ImageSequence;

import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.event.GroupPropertyChangeEvent;
import imageviewer.rendering.event.GroupPropertyChangeListener;
import imageviewer.rendering.event.ImagePropertyChangeEvent;
import imageviewer.rendering.event.ImagePropertyChangeListener;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.DataPanel;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.UIPanel;
import imageviewer.ui.VisualLayerRenderer;

import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.dialog.ps.PSDialogPanel;
import imageviewer.ui.image.vl.GridVisualLayer;
import imageviewer.ui.image.vl.ShapeVisualLayer;
import imageviewer.ui.layout.PanelDescription;
import imageviewer.ui.swing.undo.AdvanceEdit;

// =======================================================================

public class ImagePanel extends BasicImagePanel implements DragSourceListener, DragGestureListener {

	protected static final ImageInfoPanel INFO_PANEL=new ImageInfoPanel();
	protected static final PSDialogPanel PS_PANEL=PSDialogPanel.getInstance();

	protected static final FloatingPanel FLOATING_INFO_PANEL=new FloatingPanel(INFO_PANEL,"Image properties",true,true);
	protected static final FloatingPanel FLOATING_PS_PANEL=new FloatingPanel(PS_PANEL,"Presentation states",true,true);

	static {toggleImagePropertyWindow(); togglePSWindow();}

	// =======================================================================

	PanelDescription pd=null;
	boolean dragDropMode=false, dragToolMode=false;
	DragSource ds=null;

	public ImagePanel() {super(); initialize();}
	public ImagePanel(PanelDescription pd) {super(); this.pd=pd; initialize();}
	public ImagePanel(Image source, ImagePipelineRenderer ipr) {super(); this.source=source; this.ipr=ipr; initialize();}
	public ImagePanel(PanelDescription pd, Image source, ImagePipelineRenderer ipr) {super(); this.pd=pd; this.source=source; this.ipr=ipr; initialize();}

	private void initialize() {

		if (pd!=null) {
			setSize(pd.getWidth(),pd.getHeight());
			setLocation(pd.getX(),pd.getY());
		} else {
			setSize(512,512);
			setLocation(0,0);
		}
		setOpaque(true);
		setFocusable(true); 
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);

		// Setup drag-and-drop support...

		ds=new DragSource();
		ds.createDefaultDragGestureRecognizer(this,DnDConstants.ACTION_COPY,this);
		setTransferHandler(ImagePanelTransferHandler.getInstance());
	}

	// =======================================================================

	public static void toggleImagePropertyWindow() {

		Boolean b=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_IMAGE_PROP_WINDOW);
		if (b==null) return;
		if (b.booleanValue()) {
			INFO_PANEL.updatePanel(CURRENT_IMAGE);
			ApplicationPanel.getInstance().addFloatingPanel(FLOATING_INFO_PANEL);
		} else {
			ApplicationPanel.getInstance().removeFloatingPanel(FLOATING_INFO_PANEL);
		}
	}

	// =======================================================================

	public static void togglePSWindow() {

		Boolean b=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_PRESENTATION_STATES);
		if (b==null) return;
		if (b.booleanValue()) {
			ApplicationPanel.getInstance().centerFloatingPanel(FLOATING_PS_PANEL);
			ApplicationPanel.getInstance().addFloatingPanel(FLOATING_PS_PANEL);
			PS_PANEL.update(ApplicationPanel.getInstance().getActiveAssocKey());
		} else {
			ApplicationPanel.getInstance().removeFloatingPanel(FLOATING_PS_PANEL);
		}
	}

	// =======================================================================

	public void updateImagePropertyWindow() {

		CURRENT_IMAGE=source;
		Boolean b=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_IMAGE_PROP_WINDOW);
		if (b==null) return;
		if (b.booleanValue()) INFO_PANEL.updatePanel(source);
	}

	// =======================================================================

	public void paintComponent(Graphics g, boolean borders) {

		super.paintComponent(g,borders);
		paintLayers(g);
		SVL.paintLayer(temporaryShapes,this,g);
		SVL.paintOverlayLayer(panelOverlayShapes,this,g);
		Boolean showGrid=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_GRID);
		if ((showGrid!=null)&&(showGrid.booleanValue())&&(source!=null)) GVL.paintLayer(source.getPixelDimensions(),this,g);
	}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {ApplicationContext.getCurrentTool().mouseClicked(e);}
	public void mousePressed(MouseEvent e) {if (!e.isControlDown()) ApplicationContext.getCurrentTool().mousePressed(e);}
	public void mouseReleased(MouseEvent e) {ApplicationContext.getCurrentTool().mouseReleased(e); dragDropMode=false; dragToolMode=false;}
	public void mouseMoved(MouseEvent e) {ApplicationContext.getCurrentTool().mouseMoved(e);}

	public void mouseEntered(MouseEvent e) {setHighlight(true); requestFocusInWindow(); updateImagePropertyWindow(); super.mouseEntered(e);}
	public void mouseExited(MouseEvent e) {setHighlight(false); super.mouseExited(e);}

	public void mouseDragged(MouseEvent e) {

		if (e.isControlDown()) {
			if (dragToolMode) {mouseReleased(e); dragToolMode=false;}
			getTransferHandler().exportAsDrag(this,e,TransferHandler.COPY);
			dragDropMode=true;
		} else {
			dragDropMode=false;
			dragToolMode=true;
			ApplicationContext.getCurrentTool().mouseDragged(e);
		}
	}

	public void mouseWheelMoved(MouseWheelEvent e) {

		if (parent instanceof UIPanel) {
			if (e.getSource()==this) getParent().setCursor(HOURGLASS_CURSOR); 
			((UIPanel)parent).mouseWheelMoved(e); 
			if (e.getSource()==this) getParent().setCursor(DEFAULT_CURSOR); 
		}
	}

	// =======================================================================

	public void keyPressed(KeyEvent e) {

		ApplicationContext.getCurrentTool().keyPressed(e);
		if (!e.isConsumed()) {
			int delta=0;
			switch (e.getKeyCode()) {

			       case KeyEvent.VK_DOWN: delta=-1; e.consume(); break;
			         case KeyEvent.VK_UP: delta=1; e.consume(); break;
			  case KeyEvent.VK_PAGE_DOWN: delta=-((ImagePanelGroup)parent).getComponentCount(); e.consume(); break;
			    case KeyEvent.VK_PAGE_UP: delta=((ImagePanelGroup)parent).getComponentCount(); e.consume(); break;
			                     default: delta=0;
			}
			if (e.isConsumed()) {
				if ((delta!=0)&&(parent instanceof ImagePanelGroup)) {
					int change=((ImagePanelGroup)parent).advance(delta);
					if (change!=0) {
						AdvanceEdit ae=new AdvanceEdit((ImagePanelGroup)parent,delta);
						ApplicationContext.postEdit(ae);
					}
				}
			} 
		}
	}

	public void keyReleased(KeyEvent e) {ApplicationContext.getCurrentTool().keyReleased(e);}
	public void keyTyped(KeyEvent e) {ApplicationContext.getCurrentTool().keyTyped(e);}

	// =======================================================================

	public void propertyChange(String[] propertyNames, Object[] values) {ipr.getRenderingProperties().setProperties(propertyNames,values);}
	public void fireGroupPropertyChange(String[] propertyNames, Object[] values) {parent.groupPropertyChange(this,propertyNames,values);}
	public void groupPropertyChange(Object source, String[] propertyNames, Object[] values) {}

	// =======================================================================

	public int advance(ImageSequence is, int advanceAmount, int frameNumber, int nop) {

		int nos=is.size();
		int maxFrame=(nos-nop)+frameNumber;
		int minFrame=frameNumber;
		int currentIndex=is.indexOf(source);
		int newIndex=currentIndex+advanceAmount;
		if (newIndex<minFrame) newIndex=minFrame;
		if (newIndex>=(maxFrame+1)) newIndex=maxFrame;
		if (currentIndex==newIndex) return 0;
		setSource(is.getImage(newIndex));
		return (currentIndex-newIndex);
	}

	// =======================================================================

	public void setVisible(boolean b) {

		if (isVisible()!=b) {if (b==false) ipr.flush(); else ipr.render();}
		super.setVisible(b);
	}

	// =======================================================================

	public ImagePanel copy() {ImagePanel ip=new ImagePanel();	return ip;}

	// =======================================================================
	// Explicit cleanup mechanism to control for any long-term memory
	// leaks and garbage collection issues. Only do a cleanup if we're
	// not doing a move.

	public void removeNotify() {

		super.removeNotify();
		Boolean b=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.MOVE_IN_PROGRESS);
		if ((b!=null)&&(!b.booleanValue())) {
			if (CURRENT_IMAGE==source) {INFO_PANEL.updatePanel(null); CURRENT_IMAGE=null;}
			doCleanup();
			if (INFO_PANEL.getImage()==this) INFO_PANEL.updatePanel(null);
			pd=null; ipr.doCleanup(); ipr=null; source=null;
		}
	}

	// =======================================================================
	// Drag and drop support for the image panel...Absorb some weird exceptions
	// that can be thrown from the startDrag() method (drag-in-progress issues).

	public void dragGestureRecognized(DragGestureEvent dge) {

		if (!dge.getTriggerEvent().isControlDown()) return;
		if (!source.canGeneratePresentationState()) return;
		try {
			ds.startDrag(dge,DragSource.DefaultLinkDrop,(ImagePanelTransferHandler)getTransferHandler(),this);
		} catch (Exception exc) {}
	}

	public void dragEnter(DragSourceDragEvent dsde) {}
	public void dragOver(DragSourceDragEvent dsde) {}
  public void dropActionChanged(DragSourceDragEvent dsde) {}
  public void dragExit(DragSourceEvent dse) {}
	public void dragDropEnd(DragSourceDropEvent dsde) {}
}
