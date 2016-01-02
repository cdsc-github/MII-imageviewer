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

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;

import java.util.ArrayList;

import imageviewer.model.DataLayer;
import imageviewer.model.Image;

import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.DataPanel;
import imageviewer.ui.VisualLayerRenderer;

import imageviewer.ui.annotation.ControlPoint;
import imageviewer.ui.image.vl.GridVisualLayer;
import imageviewer.ui.image.vl.ShapeVisualLayer;

// =======================================================================
// Non-interactive panel that can draw an image.  Must be extended by
// other classes because it's abstract.

public abstract class BasicImagePanel extends DataPanel {

	protected static final AffineTransform IDENTITY=new AffineTransform();
	protected static final GridVisualLayer GVL=new GridVisualLayer();
	protected static final ShapeVisualLayer SVL=new ShapeVisualLayer();

	protected static Image CURRENT_IMAGE=null;

	// =======================================================================

	protected ImagePipelineRenderer ipr=null;
	protected Image source=null;
	protected boolean highlight=false;

	public BasicImagePanel() {super();}
	public BasicImagePanel(Image source, ImagePipelineRenderer ipr) {super(); this.source=source; this.ipr=ipr;}

	// =======================================================================

	public boolean hasHighlight() {return highlight;}
	public void setHighlight(boolean x) {highlight=x; repaint();}

	public Image getSource() {return source;}
	public ImagePipelineRenderer getPipelineRenderer() {return ipr;}

	// =======================================================================

	public void paintComponent(Graphics g) {paintComponent(g,true);}

	public void paintComponent(Graphics g, boolean borders) {

		super.paintComponent(g);
		Graphics2D g2=(Graphics2D)g;	
		if (ipr!=null) {
			RenderedImage ri=ipr.getRenderedImage();
			if (ri!=null) g2.drawRenderedImage(ri,IDENTITY);
			ri=null;
		}
		int w=getWidth();
		int h=getHeight();

		// Draw the corner borders around the edge of the image, if the
		// object is not highlighted.

		if (borders) {
			if (!highlight) {
				g2.setColor(Color.darkGray);
				g2.drawLine(0,0,10,0);
				g2.drawLine(0,0,0,10);
				g2.drawLine(w-10,0,w,0);
				g2.drawLine(w,0,w,10);
				g2.drawLine(0,h-10,0,h);
				g2.drawLine(0,h,10,h);
				g2.drawLine(w,h-10,w,h);
				g2.drawLine(w-10,h,w,h);
			} else {
				g2.setColor(Color.lightGray);
				g2.drawRect(0,0,w-1,h-1);
			}
		}
	}

	// =======================================================================

	public void paintLayers(Graphics g) {

		if (source==null) return;
		ArrayList dataLayers=source.getDataLayers();
		if (dataLayers==null) return;
		for (int loop=0, n=dataLayers.size(); loop<n; loop++) {
			DataLayer dl=(DataLayer)dataLayers.get(loop);
			VisualLayerRenderer vlr=dl.getRenderer();
			vlr.paintLayer(dl,this,g);
		}
	}

	//=======================================================================

	protected void paintChildren(Graphics g) {

		Component[] children=getComponents();
		for (int loop=0; loop<children.length; loop++) {
			Component child=children[loop];
			if (child instanceof ControlPoint) {
				((ControlPoint)child).updateLocation(ipr.getRenderingProperties());
			}
		}
		super.paintChildren(g);
	}

	// =======================================================================

	public void setSource(Image x) {

		if (source==x) return;
		source=null;
		source=x;
		if (source!=null) {
			ipr.setSource(source.getRenderedImage());
			ipr.render();
		} else {
			ipr.setSource(null);
		}
		repaint();
	}
}
