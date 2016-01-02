/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import java.util.EventObject;

import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.WindowLevel;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.graphics.IndicatorBar;
import imageviewer.ui.swing.undo.PanEdit;

// =======================================================================

public class PanTool extends ImagingTool implements Tool {

	double startTranslateX=0, startTranslateY=0;
	double newX=0, newY=0;
	RenderingProperties rp=null;
	Point start=null;

	public PanTool() {}
	
	// =======================================================================

	public void startTool(EventObject e) {}
	public void endTool(EventObject e) {}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Pan image");}

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
			rp=ip.getPipelineRenderer().getRenderingProperties();
			start=e.getPoint();
			startTranslateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
			startTranslateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
			addIndicatorBar(ip,new PanIndicatorBar());
			ip.repaint();
		}
	}

	// =======================================================================

	public void execute(MouseEvent e) {

		newX=startTranslateX-(start.x-e.getX());
		newY=startTranslateY-(start.y-e.getY());
		rp.setProperties(new String[] {RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
										 new Object[] {new Double(newX),new Double(newY)});
		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			ip.repaint();
		}
	}

	// =======================================================================

	public void finish(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			ImagePanel ip=(ImagePanel)e.getSource();
			PanEdit pe=new PanEdit(ip,startTranslateX,startTranslateY,newX,newY);
			ApplicationContext.postEdit(pe);
			ip.fireGroupPropertyChange(new String[] {RenderingProperties.TRANSLATE_X,RenderingProperties.TRANSLATE_Y},
																 new Object[] {new Double(newX),new Double(newY)});
			removeIndicatorBar(ip);
			ip.repaint();
		}
	}

	// =======================================================================

	private class PanIndicatorBar extends IndicatorBar {

		public PanIndicatorBar() {super();}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			Graphics2D g2=(Graphics2D)g.create();
			String str=new String("("+(int)newX+","+(int)newY+")");
			paintText(g2,str,0,1);
			g2.dispose();
		}
	}
}
