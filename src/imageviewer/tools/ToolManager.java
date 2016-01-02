/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;

import java.util.EventObject;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import imageviewer.rendering.RenderingProperties;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;

// =======================================================================

public class ToolManager implements MenuActionListener {

	protected static Hashtable<String,Tool> TOOLS=new Hashtable<String,Tool>();
	protected static Hashtable<Class,Tool> CACHE=new Hashtable<Class,Tool>();

	public ToolManager() {}

	// =======================================================================

	private static final Logger LOG=Logger.getLogger("imageViewer.tools");

	public static Tool getTool(Class c) {

		Tool t=CACHE.get(c);
		if (t==null) {
			final Class[] constructorParameterTypes={};
			final Object[] constructorParameters={};
			try {
				Constructor newConstructor=c.getConstructor(constructorParameterTypes);
				t=(Tool)newConstructor.newInstance(constructorParameters);
				CACHE.put(c,t);
			} catch (Exception exc) {
				LOG.error("Unable to create tool: "+c);
			}
		}
		return t;
	}

	// =======================================================================
	// Handle switching of basic tools

	public void actionPerformed(MenuActionEvent mae) {

		ActionEvent ae=mae.getActionEvent();
		String actionCommand=ae.getActionCommand();
		if (actionCommand==null) {
			MenuAction ma=mae.getMenuAction();
			if (ma!=null) actionCommand=ma.getCommandName(); else return;
		}
		if (actionCommand==null) return;

		Tool t=(Tool)TOOLS.get(actionCommand);
		if (t==null) {
			if (actionCommand.compareTo("Window/level")==0) {
				t=getTool(WindowLevelTool.class);
			} else if (actionCommand.compareTo("Zoom")==0) {
				t=getTool(ScaleTool.class);
			} else if (actionCommand.compareTo("Pan")==0) {
				t=getTool(PanTool.class);
			} else if (actionCommand.compareTo("Rotate")==0) {
				t=getTool(RotationTool.class);
			} else if (actionCommand.compareTo("Magnifier")==0) {
				t=getTool(MagicLensTool.class);
			} else if (actionCommand.compareTo("Cine")==0) {
				t=getTool(CineTool.class);
			} else if (actionCommand.compareTo("Auto window/level")==0) {
				t=getTool(AutoBrightnessContrastTool.class);
			} else if (actionCommand.compareTo("Lung Setting")==0) {
				t=getTool(AutoLungContrastTool.class);
			} else if (actionCommand.compareTo("Bone Setting")==0) {
				t=getTool(AutoBoneContrastTool.class);
			} else if (actionCommand.compareTo("Soft Tissue Setting")==0) {
				t=getTool(AutoSoftContrastTool.class);
			} else if (actionCommand.compareTo("Flip vertical")==0) {
				t=getTool(FlipVerticalTool.class);
			} else if (actionCommand.compareTo("Flip horizontal")==0) {
				t=getTool(FlipHorizontalTool.class);
			} else if (actionCommand.compareTo("Reset image")==0) {
				t=getTool(ResetTool.class);
			} else if (actionCommand.compareTo("Select")==0) {
				t=getTool(SelectTool.class);
			} else if (actionCommand.compareTo("Region histogram")==0) {
				t=getTool(HistogramTool.class);
			} else if (actionCommand.compareTo("Line")==0) {
				t=getTool(LineTool.class);
			} else if (actionCommand.compareTo("Arrow")==0) {
				t=getTool(ArrowTool.class);
			} else if (actionCommand.compareTo("Box")==0) {
				t=getTool(BoxTool.class);
			} else if (actionCommand.compareTo("Ellipse")==0) {
				t=getTool(EllipseTool.class);
			} else if (actionCommand.compareTo("Polyline")==0) {
				t=getTool(PolyLineTool.class);
			} else if (actionCommand.compareTo("Freehand curve")==0) {
				t=getTool(FreehandCurveTool.class);
			} else if (actionCommand.compareTo("Text")==0) {
				t=getTool(TextAnnotationTool.class);
			} else if (actionCommand.compareTo("Mark image")==0) {
				t=getTool(MarkTool.class);
			}
			if (t!=null) TOOLS.put(actionCommand,t);
		}

		// If no tool found, do nothing.  If there's a current tool set,
		// then call its endTool method to do a cleanup.  Also, if we do
		// have a new tool, then call its startTool method to get ready.

		if (t!=null) {
			Tool currentTool=ApplicationContext.getContext().getTool();
			EventObject eo=new EventObject(this);
			if (currentTool!=null) currentTool.endTool(eo);
			ApplicationContext.getContext().setTool(t);
			t.startTool(eo);
		}
	}
}
