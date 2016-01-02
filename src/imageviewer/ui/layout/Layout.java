/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseEvent;

import java.lang.reflect.Constructor;

import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import imageviewer.model.ImageSequence;

import imageviewer.rendering.RenderingOpPipeline;
import imageviewer.rendering.RenderingOpPipelineFactory;
import imageviewer.rendering.ImagePipelineRenderer;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.DataPanelGroup;
import imageviewer.ui.GroupedComponent;
import imageviewer.ui.UIPanel;
import imageviewer.ui.VisualProperties;

// =======================================================================

public class Layout extends DataPanelGroup {

	private static final Class[] IMAGE_PANEL_ARGS=new Class[] {GroupedComponent.class,ImageSequence.class,ControlGroupDescription.class,
																														 RenderingOpPipeline.class,VisualProperties.class};
	private static final Class[] OTHER_PANEL_ARGS=new Class[] {GroupedComponent.class,ImageSequence.class,ControlGroupDescription.class};

	private static final JCheckBox ASK_CB=new JCheckBox("Do not show this message again.",false);

	// =======================================================================

	LayoutDescription ld=null;
	ImageSequence[] is=null;
	String[] sequenceTags=null;

	public Layout() {super();}
	public Layout(GroupedLayout gl) {super(); initialize(gl);}

	private void initialize(GroupedLayout gl) {

		// Set up the basic panel
	
		is=gl.getImageSequences();
		sequenceTags=gl.getSequenceTags();
		setOpaque(true);
		setBackground(Color.black);
		applyLayout(gl,null);
	}

	// =======================================================================

	private void applyLayout(GroupedLayout gl, VisualProperties vp) {

		// Create the rendering pipeline associated with this layout

		ld=gl.getLayoutDescription();
		if (ld==null) return;
		String rpType=ld.getRenderingPipelineName();
		RenderingOpPipeline rop=((rpType==null)||(rpType.compareTo("default")==0)) ? RenderingOpPipelineFactory.create() : RenderingOpPipelineFactory.create(rpType);
		if (rop==null) rop=RenderingOpPipelineFactory.create();
		
		// Traverse through each of the control groups in this layout.
		// Have to select the appropriate image sequence and group to
		// create based on the specified tags.
		
		int panelWidth=0, panelHeight=0;
		ArrayList<ControlGroupDescription> groups=ld.getControlGroups();
		for (int loop=0, n=groups.size(); loop<n; loop++) {
			ControlGroupDescription cgd=groups.get(loop);
			String panelType=cgd.getType();
			ImageSequence is=gl.getSequenceByTag(cgd.getTarget());
			if (is!=null) {
				try {
					Class c=Class.forName(panelType);
					Object[] param=null;
					Constructor constructor=null;
					try {
						constructor=c.getConstructor(IMAGE_PANEL_ARGS);
						param=new Object[] {this,is,cgd,rop,vp};
					} catch (NoSuchMethodException nsme) {
						constructor=c.getConstructor(OTHER_PANEL_ARGS);
						param=new Object[] {this,is,cgd};
					}
					DataPanelGroup dpg=(DataPanelGroup)constructor.newInstance(param);
					dpg.setLocation(cgd.getX(),cgd.getY());
					dpg.setTag(cgd.getTarget());
					add(dpg);
					addHierarchyBoundsListener(dpg);
					int layoutWidth=cgd.getX()+dpg.getWidth();
					int layoutHeight=cgd.getY()+dpg.getHeight();
					if (panelWidth<layoutWidth) panelWidth=layoutWidth;
					if (panelHeight<layoutHeight) panelHeight=layoutHeight;

				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}
		setSize(panelWidth,panelHeight);

		// Trigger an update to the bounds of the underlying data panels.

		HierarchyBoundsListener[] hbls=getHierarchyBoundsListeners();
		if (hbls!=null) {
			for (int loop=0; loop<hbls.length; loop++) hbls[loop].ancestorResized(new HierarchyEvent(this,HierarchyEvent.ANCESTOR_RESIZED,this,this));
		}		
	}

	// =======================================================================

	public LayoutDescription getLayoutDescription() {return ld;}
	public ImageSequence[] getImageSequences() {return is;}
	public String[] getSequenceTags() {return sequenceTags;}

	public void setLayoutDescription(LayoutDescription x) {ld=x;}
	public void setImageSequences(ImageSequence[] x) {is=x;}
	public void setSequenceTags(String[] x) {sequenceTags=x;}

	// =======================================================================

	public void groupPropertyChange(Object source, String[] propertyNames, Object[] values) {}

	// =======================================================================

	public Layout copy() {

		Layout l=new Layout();
		Component[] children=getComponents();
		for (int loop=0; loop<children.length; loop++) {
			Component c=children[loop];
			if (c instanceof DataPanelGroup) {
				DataPanelGroup dpgCopy=((DataPanelGroup)c).copy(l);
				l.add(dpgCopy);
				l.addHierarchyBoundsListener(dpgCopy);
			}
		}
		l.setSize(getWidth(),getHeight());
		l.setSequenceTags(sequenceTags);
		l.setImageSequences(is);
		l.setLayoutDescription(ld);
		return l;
	}

	// =======================================================================

	public void mouseEntered(MouseEvent e) {super.mouseEntered(e); ApplicationPanel.getInstance().setActiveLayout(this);}

	// =======================================================================
	// A new grouped layout has been selected by the user, and must be
	// applied to the current contents of this layout panel.  We can try
	// and reuse the underlying image components, or totally create a
	// new layout based on the new information. Of course, don't do
	// anything if the layouts are the same.

	public void changeLayout(LayoutDescription newLayout, boolean discardCurrent) {

		if (ld==newLayout) return;

		// Confirm the layout change with the user...
		
		Boolean askLayoutChange=(Boolean)ApplicationContext.getContext().getProperty(ApplicationContext.ASK_CHANGE_LAYOUT);
		if ((askLayoutChange==null)||(askLayoutChange.booleanValue())) {
			int response=ApplicationPanel.getInstance().showDialog("Changing the layout will remove any undoable operations associated with this tab.  Do you want to proceed?",
																														 new JComponent[] {ASK_CB},JOptionPane.WARNING_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
			if (ASK_CB.isSelected()) ApplicationContext.getContext().setProperty(ApplicationContext.ASK_CHANGE_LAYOUT,new Boolean(false));
			if (response!=JOptionPane.OK_OPTION) return;
		}
		
		ApplicationContext.getContext().setProperty(ApplicationContext.LAYOUT_IN_PROGRESS,new Boolean(true));
		GroupedLayout gl=new GroupedLayout(is,sequenceTags,newLayout);
		DataPanelGroup primaryGroup=null;

		Component[] children=getComponents();
		for (int loop=0; loop<children.length; loop++) {
			Component c=children[loop];
			if (c instanceof DataPanelGroup) {
				String tag=((DataPanelGroup)c).getTag();
				if ((tag!=null)&&(tag.compareToIgnoreCase("PRIMARY")==0)) {primaryGroup=(DataPanelGroup)c; break;}
			}
		}
		VisualProperties vp=(primaryGroup!=null) ? primaryGroup.getVisualProperties(0,true) : null;
		if (discardCurrent) {
			HierarchyBoundsListener[] hbls=getHierarchyBoundsListeners();
			if (hbls!=null) {
				for (int loop=0; loop<hbls.length; loop++) removeHierarchyBoundsListener(hbls[loop]);
			}
			removeAll();
			applyLayout(gl,vp);
			repaint();
		}
		ApplicationContext.getContext().setProperty(ApplicationContext.LAYOUT_IN_PROGRESS,new Boolean(false));
	}

	// =======================================================================

	public void removeNotify() {super.removeNotify(); doCleanup();}
}
