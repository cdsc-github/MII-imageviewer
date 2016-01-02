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

import java.awt.geom.AffineTransform;

import imageviewer.rendering.RenderingProperties;

import imageviewer.ui.DataPanel;
import imageviewer.ui.VisualLayerRenderer;
import imageviewer.ui.image.BasicImagePanel;

// =======================================================================

public class GridVisualLayer implements VisualLayerRenderer {

	private static final AlphaComposite GRID_AC1=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5F);
	private static final AlphaComposite GRID_AC2=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.375F);

	public void paintLayer(Object dataLayer, DataPanel dp, Graphics g) {

		if (dp instanceof BasicImagePanel) {

			BasicImagePanel bip=(BasicImagePanel)dp;
			RenderingProperties rp=bip.getPipelineRenderer().getRenderingProperties();
			double scale=((Double)rp.getProperty(RenderingProperties.SCALE)).doubleValue();
			double translateX=((Double)rp.getProperty(RenderingProperties.TRANSLATE_X)).doubleValue();
			double translateY=((Double)rp.getProperty(RenderingProperties.TRANSLATE_Y)).doubleValue();

			int panelWidth=dp.getWidth();
			int panelHeight=dp.getHeight();
			Color gridColor=new Color(128,196,255);                                          // Get from application context!
			double xSpacing=0, ySpacing=0;

			if (dataLayer==null) {
				xSpacing=(int)(panelWidth/4);
				ySpacing=(int)(panelHeight/4);
				gridColor=Color.red;
			} else {
				double[] pixelSpacing=(double[])dataLayer;
				xSpacing=(((double)10)/pixelSpacing[0]);
				ySpacing=(((double)10)/pixelSpacing[1]);
			}

			Graphics2D g2=(Graphics2D)g;
			Composite oldAC=g2.getComposite();
			Color oldColor=g2.getColor();
					
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.translate(translateX,translateY);
			g2.scale(scale,scale);
			g2.setStroke(new BasicStroke((float)(1.0/scale)));
			
			int minX=(translateX>=0) ? (int)Math.floor(-translateX/(scale*xSpacing)) : 0;
			for (int loop=minX, n=(int)((panelWidth-translateX)/(scale*xSpacing)); loop<=n; loop++) {
				if ((loop % 5)==0) {
					g2.setComposite(GRID_AC1);
					g2.setColor(gridColor);
				} else {
					g2.setColor(gridColor.darker());
				}
				g2.drawLine((int)(loop*xSpacing),(int)(-translateY/scale),(int)(loop*xSpacing),(int)((panelHeight-translateY)/scale));
			}

			int minY=(translateY>=0) ? (int)Math.floor(-translateY/(scale*ySpacing)) : 0;
			for (int loop=minY, n=(int)((panelHeight-translateY)/(scale*ySpacing)); loop<=n; loop++) {
				if ((loop % 5)==0) {
					g2.setComposite(GRID_AC1);
					g2.setColor(gridColor);
				} else {
					g2.setColor(gridColor.darker());
				}
				g2.drawLine((int)(-translateX/scale),(int)(loop*ySpacing),(int)((panelWidth-translateX)/scale),(int)(loop*ySpacing));
			}

			g2.setComposite(oldAC);
			g2.setColor(oldColor);
			g2.scale(1/scale,1/scale);
			g2.translate(-translateX,-translateY);
		}
	}
}
