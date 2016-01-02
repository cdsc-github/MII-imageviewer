/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Container;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.ArrayList;
import java.util.EventObject;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.annotation.Selectable;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.image.ImagePanelGroup;

import imageviewer.ui.swing.undo.AdvanceEdit;

// =======================================================================

public class CineTool extends ImagingTool implements Tool {

	Point start=new Point();
	ImagePanelGroup ipg=null;
	float heightRatio=0;
	
	public CineTool() {}

	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Cine");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {execute(e);}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {prepare(e);}
	public void mouseReleased(MouseEvent e) {finish(e);}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	// =======================================================================

	public void prepare(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			if (ip.getParent() instanceof ImagePanelGroup) {
				ipg=(ImagePanelGroup)ip.getParent();
				start=e.getPoint();
				float panelHeight=(float)ip.getHeight();
				int numberOfImages=ipg.getImageSequence().size();
				heightRatio=panelHeight/(float)numberOfImages;
			}
		}
	}

	// =======================================================================

	public void execute(MouseEvent e) {

		int deltaY=-(e.getY()-start.y);
		int advanceAmount=Math.round(deltaY/heightRatio);
		int delta=ipg.advance(advanceAmount);
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
			AdvanceEdit ae=new AdvanceEdit(ipg,advanceAmount);
			ApplicationContext.postEdit(ae);
		}
	}

	// =======================================================================

	public void finish(MouseEvent e) {}

}
