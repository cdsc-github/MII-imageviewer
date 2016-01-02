/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import org.jdesktop.swingx.graphics.ReflectionRenderer;

import imageviewer.model.Image;

import imageviewer.rendering.ImagePipelineRenderer;
import imageviewer.rendering.RenderingOpPipeline;
import imageviewer.rendering.RenderingOpPipelineFactory;
import imageviewer.rendering.RenderingProperties;
import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.rendering.wl.WindowLevel;

// =======================================================================

public class ImageMagDetailPanel extends JPanel {

	AffineTransform at=new AffineTransform();
	BufferedImage source=null, reflection=null;
	ReflectionRenderer rr=new ReflectionRenderer(0.3f,0.15f,true);
	Image i=null;

	public ImageMagDetailPanel() {
		super();
		setLayout(null);
		setBackground(Color.black);
		setSize(512,768);
		setPreferredSize(new Dimension(512,768));
	}

	public void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (i==null) return;
		if (source==null) {
			RenderingProperties rp=new RenderingProperties();
			ImagePipelineRenderer ipr=new ImagePipelineRenderer(RenderingOpPipelineFactory.create(),rp);
			int imageWidth=i.getWidth();
			double scaleFactor=(double)512/(double)imageWidth;
			WindowLevel wl=DefaultWindowLevelManager.getDefaultWindowLevel(i);				
			rp.setProperties(new String[] {RenderingProperties.SCALE,RenderingProperties.WINDOW_LEVEL,RenderingProperties.MAX_PIXEL,
																		 RenderingProperties.SOURCE_WIDTH,RenderingProperties.SOURCE_HEIGHT}, 
					                           new Object[] {new Double(scaleFactor),wl,new Integer(i.getMaxPixelValue()),
																									 new Integer(i.getWidth()),new Integer(i.getHeight())});
			ipr.setSource(i.getRenderedImage());
			RenderedImage ri=ipr.getRenderedImage();
			source=new BufferedImage(512,512,BufferedImage.TYPE_BYTE_GRAY);		
			Graphics2D big=source.createGraphics();
			big.setColor(Color.black);
			big.drawRenderedImage(ri,at);
			ipr.flush();
			ipr.doCleanup();
			ri=null;
			ipr=null;
		}
		if (reflection==null) reflection=rr.createReflection(source);
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		
		g2.drawImage(source,0,0,null);
		g2.drawImage(reflection,0,515,null);
	}

	public void setImage(Image i) {this.i=i; if (source!=null) {source.flush(); source=null;} if (reflection!=null) {reflection.flush(); reflection=null;}}
}
