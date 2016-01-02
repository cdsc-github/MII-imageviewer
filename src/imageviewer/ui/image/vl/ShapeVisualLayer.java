/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.vl;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.GeneralPath;

import java.util.ArrayList;

import imageviewer.model.dl.ShapeDataLayer;
import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.DataPanel;
import imageviewer.ui.VisualLayerRenderer;
import imageviewer.ui.annotation.StylizedShape;
import imageviewer.ui.image.BasicImagePanel;

// =======================================================================

public class ShapeVisualLayer implements VisualLayerRenderer {

	private static final boolean GP_BUG_FLAG; 

	// Handle a nasty bug in JDK 1.5 where generalShape cannot be
	// drawn/filled using an anti-aliased graphics2D object. This
	// problem is fixed in JDK 1.6 beta.

	static {
		String javaVersion=System.getProperty("java.version");
		GP_BUG_FLAG=(javaVersion.startsWith("1.6")) ? false : true;
	}

	// =======================================================================

	private void render(Graphics2D g2, StylizedShape ss) {

		Shape baseShape=ss.getBaseShape();
		Object antiAlias=null;
		if ((GP_BUG_FLAG)&&(baseShape instanceof GeneralPath)) {
			antiAlias=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);
		}
		if (ss.isOutlined()) {
			g2.setComposite(ss.getOutlineAlphaComposite());
			g2.setColor(ss.getOutlineColor());
			BasicStroke shapeStroke=ss.getStroke();
			float penWidth=shapeStroke.getLineWidth();
			BasicStroke bs=new BasicStroke(penWidth+0.5f,shapeStroke.getEndCap(),shapeStroke.getLineJoin(),
																		 shapeStroke.getMiterLimit(),shapeStroke.getDashArray(),shapeStroke.getDashPhase());
			g2.setStroke(bs);
			g2.setColor(ss.getOutlineColor());
			if (ss.paints()) ss.draw(g2); else g2.draw(baseShape);
		}
		if (ss.isFilled()) {
			g2.setComposite(ss.getFillAlphaComposite());
			g2.setColor(ss.getFillColor());
			if (ss.paints()) ss.fill(g2); else g2.fill(baseShape);
		}
		if (ss.isStroked()) {
			g2.setComposite(ss.getStrokeAlphaComposite());
			g2.setStroke(ss.getStroke());
			g2.setColor(ss.getStrokeColor());
			if (ss.paints()) ss.draw(g2); else g2.draw(baseShape);
		}
		if ((GP_BUG_FLAG)&&(baseShape instanceof GeneralPath)) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,antiAlias);
		}
	}

	// =======================================================================

	private void paintLayer(Object dataLayer, DataPanel dp, Graphics g, boolean doTransform) {

		if ((dataLayer!=null)&&(dp instanceof BasicImagePanel)) {

			if (dataLayer instanceof ShapeDataLayer) {if (((ShapeDataLayer)dataLayer).getShapes().isEmpty()) return;}
			if (dataLayer instanceof ArrayList) {if (((ArrayList)dataLayer).isEmpty()) return;}

			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

			BasicImagePanel bip=(BasicImagePanel)dp;
			if (doTransform) {
				RenderingProperties rp=bip.getPipelineRenderer().getRenderingProperties();
				double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
				double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
				double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();
				int sourceWidth=((Integer)rp.getProperty(RenderingProperties.SOURCE_WIDTH)).intValue();
				int sourceHeight=((Integer)rp.getProperty(RenderingProperties.SOURCE_HEIGHT)).intValue();
				float rotationAngle=((Float)rp.getProperty(RenderingProperties.ROTATION)).floatValue();	
				boolean isVFlip=((Boolean)rp.getProperty(RenderingProperties.VERTICAL_FLIP)).booleanValue();	
				boolean isHFlip=((Boolean)rp.getProperty(RenderingProperties.HORIZONTAL_FLIP)).booleanValue();	
				double vFlip=(isVFlip) ? -1 : 1;
				double hFlip=(isHFlip) ? -1 : 1;
				double yTranslate=(isVFlip) ? -sourceHeight : 0;
				double xTranslate=(isHFlip) ? -sourceWidth : 0;
				g2.translate(translateX,translateY);
				g2.scale(scale,scale);
				g2.rotate((double)rotationAngle,sourceWidth/2,sourceHeight/2);
				g2.scale(hFlip,vFlip);
				g2.translate(xTranslate,yTranslate);
			}

			if (dataLayer instanceof ShapeDataLayer) {
				ArrayList shapes=((ShapeDataLayer)dataLayer).getShapes();
				for (int loop=0; loop<shapes.size(); loop++) {
					StylizedShape ss=(StylizedShape)shapes.get(loop);
					render(g2,ss);
				}
			} else if (dataLayer instanceof ArrayList) {
				ArrayList shapes=(ArrayList)dataLayer;
				for (int loop=0; loop<shapes.size(); loop++) {
					StylizedShape ss=(StylizedShape)shapes.get(loop);
					render(g2,ss);
				}
			} else if (dataLayer instanceof StylizedShape) {
				StylizedShape ss=(StylizedShape)dataLayer;
				render(g2,ss);
			}
			g2.dispose();
		}
	}

	public void paintLayer(Object dataLayer, DataPanel dp, Graphics g) {paintLayer(dataLayer,dp,g,true);}
	public void paintOverlayLayer(Object dataLayer, DataPanel dp, Graphics g) {paintLayer(dataLayer,dp,g,false);}
}
