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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;

import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.beans.PropertyChangeEvent;

import java.util.ArrayList;
import java.util.EventObject;

import imageviewer.model.Image;
import imageviewer.model.ImageSequence;
import imageviewer.model.ModelEvent;
import imageviewer.model.ModelEvent.EventType;
import imageviewer.model.ModelListener;

import imageviewer.rendering.RenderingOpPipeline;
import imageviewer.rendering.RenderingOpPipelineFactory;
import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.DataPanelGroup;
import imageviewer.ui.GroupedComponent;
import imageviewer.ui.TabbedDataPanel;
import imageviewer.ui.UIPanel;
import imageviewer.ui.VisualProperties;

import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.annotation.Selectable;
import imageviewer.ui.layout.Layout;
import imageviewer.ui.layout.ControlGroupDescription;
import imageviewer.ui.layout.PanelDescription;
import imageviewer.ui.swing.undo.AdvanceEdit;

// =======================================================================

public class ImagePanelGroup extends DataPanelGroup implements ModelListener {

	private static final Rectangle BOUNDS_RECT=new Rectangle();                           // Reused objects so new ones aren't re-allocated
	private static final Rectangle VISIBLE_RECT=new Rectangle();

	// =======================================================================

	RenderingOpPipeline rop=null;
	ImageSequence is=null;

	public ImagePanelGroup(GroupedComponent gc) {super(gc); setOpaque(false); setBackground(Color.black);}
	public ImagePanelGroup(GroupedComponent gc, ImageSequence is, ControlGroupDescription cgd, RenderingOpPipeline rop, VisualProperties vp) {super(gc); initialize(gc,is,cgd,rop,vp);}

	//public ImagePanelGroup(GroupedComponent gc, ImageSequence is, ControlGroupDescription cgd, RenderingOpPipeline rop) {super(gc); initialize(gc,is,cgd,rop,null);}

	// =======================================================================

	private void initialize(GroupedComponent gc, ImageSequence is, ControlGroupDescription cgd, RenderingOpPipeline rop, VisualProperties sourceVP) {
		
		this.rop=rop;
		this.is=is;
		setOpaque(false);
		setBackground(Color.black);
		is.addListener(this);

		int boundWidth=0, boundHeight=0, sequenceLength=is.size();

		ArrayList<PanelDescription> panels=cgd.getPanels();
		for (int loop=0, n=panels.size(); ((loop<n)&&(loop<sequenceLength)); loop++) {
			PanelDescription pd=panels.get(loop);
			Image image=is.get(loop);
			WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(image);
			double scale=(double)pd.getWidth()/(double)image.getWidth();
			scale=(scale<0.01) ? 0.01 : scale;
			// KYLE - getMaxPixelValue now finds the Max for the Series instead of each individual image
			RenderingProperties rp=((sourceVP!=null)&&(sourceVP instanceof RenderingProperties)) ? ((RenderingProperties)sourceVP).copy() : 
				new RenderingProperties(wl,is.getMaxPixelValue(),image.getWidth(),image.getHeight());
			rp.setProperties(new String[] {RenderingProperties.SCALE},new Object[] {new Double(scale)});
			ImagePipelineRenderer ipr=new ImagePipelineRenderer(rop,rp,image);
			ImagePanel ip=new ImagePanel(pd,image,ipr);
			ip.setGroupParent(this);
			addChild(ip);
			add(ip);
			
			// Compute the bounds of the group panel based on the bounds of
			// the underlying panels...
			
			int ipBoundWidth=pd.getX()+pd.getWidth();
			int ipBoundHeight=pd.getY()+pd.getHeight();
			boundWidth=(boundWidth<ipBoundWidth) ? ipBoundWidth : boundWidth;
			boundHeight=(boundHeight<ipBoundHeight) ? ipBoundHeight : boundHeight;
		}
		setSize(boundWidth,boundHeight);
		setPreferredSize(new Dimension(boundWidth,boundHeight));
	}

	// =======================================================================

	public ImageSequence getImageSequence() {return is;}

	public void setImageSequence(ImageSequence x) {if (is!=null) is.removeListener(this); is=x;}
	public void setRenderingOpPipeline(RenderingOpPipeline x) {rop=x;}

	// =======================================================================

	public void mouseWheelMoved(MouseWheelEvent e) {

		int advanceAmount=e.getWheelRotation();
		if (advanceAmount!=0) ApplicationContext.getCurrentTool().endTool(new EventObject(this));
		int delta=advance(advanceAmount);
		if (delta!=0) {
			ArrayList<Selectable> al=(ArrayList<Selectable>)ApplicationContext.getContext().getSelections().clone();
			for (Selectable s : al) {
				if (s.isSelected()) {
					ArrayList<ControlPoint> controlPoints=s.getControlPoints();
					if ((controlPoints!=null)&&(!controlPoints.isEmpty())) {
						for (ControlPoint cp : controlPoints) {Container c=cp.getParent(); c.remove(cp); c.repaint();}
					}
				}
				s.deselect();
			}
			ApplicationContext.getContext().clearSelections();
			AdvanceEdit ae=new AdvanceEdit(this,delta);
			ApplicationContext.postEdit(ae);
		}
		e.consume();
	}

	// =======================================================================

	public int advance(int advanceAmount) {

		int delta=0;
		for (int loop=0, indexCounter=0, nop=children.size(); loop<nop; loop++) {
			ImagePanel ip=(ImagePanel)children.get(loop);
			if (ip.isVisible()) delta=ip.advance(is,advanceAmount,indexCounter++,nop);                             // Only advance if the panel is visible...
		}
		return delta;
	}

	// =======================================================================

	public void groupPropertyChange(Object source, String[] propertyNames, Object[] values) {

		for (int loop=0, n=children.size(); loop<n; loop++) {
			ImagePanel ip=(ImagePanel)children.get(loop);
			if (source!=ip)	{
				ip.propertyChange(propertyNames,values);
				ip.repaint();
			}
		}
	}

	// =======================================================================

	public ImagePanelGroup copy(GroupedComponent gc) {

		ImagePanelGroup ipg=new ImagePanelGroup(gc);
		is.addListener(ipg);
		ipg.setImageSequence(is);
		String rpType=rop.getName();
		RenderingOpPipeline rop=((rpType==null)||(rpType.compareTo("default")==0)) ? RenderingOpPipelineFactory.create() : RenderingOpPipelineFactory.create(rpType);
		if (rop==null) rop=RenderingOpPipelineFactory.create();
		ipg.setRenderingOpPipeline(rop);
		ipg.setLocation(getX(),getY());
		ipg.setSize(getWidth(),getHeight());
		Component[] children=getComponents();
		for (int loop=0; loop<children.length; loop++) {
			Component c=children[loop];
			if (c instanceof ImagePanel) {
				ImagePanel original=(ImagePanel)c;
				RenderingProperties rp=original.getPipelineRenderer().getRenderingProperties().copy();
				ImagePipelineRenderer ipr=new ImagePipelineRenderer(rop,rp,original.getSource());
				ImagePanel ip=new ImagePanel(original.getSource(),ipr);
				ip.setLocation(original.getX(),original.getY());
				ip.setSize(original.getWidth(),original.getHeight());
				ip.setGroupParent(ipg);
				ipg.addChild(ip);
				ipg.add(ip);
			}
		}
		return ipg;
	}

	// =======================================================================
	// Grab the rendering properties of the given child image
	// panel. Note that a copy is made, rather than returning the direct
	// pointer, based on the boolean flag.

	public VisualProperties getVisualProperties(int index, boolean copy) {

		Component[] children=getComponents();
		if ((index>=0)&&(index<children.length)) {
			Component c=children[index];
			if (c instanceof ImagePanel) {
				ImagePanel ip=(ImagePanel)c;
				RenderingProperties rp=ip.getPipelineRenderer().getRenderingProperties();
				return (copy) ? rp.copy() : rp;
			}
		}
		return null;
	}

	// =======================================================================

	public int getCurrentImageIndex(int index) {

		Component[] children=getComponents();
		if ((index>=0)&&(index<children.length)) {
			Component c=children[index];
			if (c instanceof ImagePanel) {
				ImagePanel ip=(ImagePanel)c;
				return is.indexOf(ip.getSource());
			}
		}
		return -1;
	} 

	// =======================================================================
	// Determine the visibility of a panel and whether it needs to
	// release its resources (done by an override of the setVisible on
	// an imagePanel). Have to check and see if there are any children
	// first of all, otherwise a NPE can be thrown...

	private void determineVisiblePanels() {
	
		Boolean b=(Boolean)(ApplicationContext.getContext().getProperty(ApplicationContext.IGNORE_VISIBLE_PANEL_CHECK));
		if ((b!=null)&&(b.booleanValue())) return;
		ImagePanel firstPanel=(ImagePanel)children.get(0);
		if (firstPanel.getSource()==null) return;
		for (int loop=0, nop=children.size(), imageIndex=is.indexOf(firstPanel.getSource()); loop<nop; loop++) {
			ImagePanel ip=(ImagePanel)children.get(loop);
			ip.computeVisibleRect(VISIBLE_RECT);
			if ((VISIBLE_RECT.width==0)&&(VISIBLE_RECT.height==0)) {  // Always hide if the panel is completely invisible...
				ip.setVisible(false);
			} else {
				ip.getBounds(BOUNDS_RECT);
				if ((((Boolean)(ApplicationContext.getContext().getProperty(ApplicationContext.DISPLAY_PARTIAL_PANELS))).booleanValue()) ||
						((VISIBLE_RECT.width==BOUNDS_RECT.width)&&(VISIBLE_RECT.height==BOUNDS_RECT.height))) {
					ip.setVisible(true);
					ip.setSource(is.getImage(imageIndex++));
				} else {
					ip.setVisible(false);
				}
			}
		}
	}

	// =======================================================================

	public void propertyChange(PropertyChangeEvent pce) {

		String propertyName=pce.getPropertyName();
		if (propertyName.compareTo("showPartial")==0) {
			determineVisiblePanels();
			return;
		} 
	}

	// =======================================================================

	public void ancestorResized(HierarchyEvent he) {determineVisiblePanels();}
	
	// =======================================================================
	// Handle cleanup of this imagePanelGroup if it's garbage collected
	// or removed. The removeNotify will call the child panels also, and
	// they will need to to cleanup accordingly. Only do a cleanup if
	// we're not in the middle of a move (which would trigger the
	// removeNotify, duh!).

	public void removeNotify() {

		super.removeNotify(); 
		Boolean b1=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.MOVE_IN_PROGRESS);
		Boolean b2=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.LAYOUT_IN_PROGRESS);
		if (((b1!=null)&&(!b1.booleanValue()))&&((b2!=null)&&(!b2.booleanValue()))) doCleanup();
	}

	protected void finalize() throws Throwable {super.finalize(); doCleanup();}
	protected void doCleanup() {super.doCleanup(); rop=null; is.removeListener(this); is=null; ApplicationContext.getContext().removeComponentEdits(this);}

	// =======================================================================
	// An event is being sent from the model itself, such as a close request.

	public void processModelEvent(ModelEvent me) {

		if (me.getEvent()==EventType.CLOSE) {
			GroupedComponent gc=getGroupParent();
			if (gc instanceof Layout) {
				Layout l=(Layout)gc;
				Component targetPanel=l.getParent();
				Component c=targetPanel.getParent();
				if (c instanceof TabbedDataPanel) {
					TabbedDataPanel tdp=(TabbedDataPanel)c;
					tdp.closeTab(targetPanel);
				}
			}
		}
	}
}

