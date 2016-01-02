/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.rendering;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import java.util.ArrayList;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.apache.log4j.Logger;

import imageviewer.model.Image;

import imageviewer.rendering.event.ImagePropertyChangeEvent;
import imageviewer.rendering.event.ImagePropertyChangeListener;

// =======================================================================

public class ImagePipelineRenderer implements ImagePropertyChangeListener {

	private static Logger LOG=Logger.getLogger("imageviewer.rendering");

	// =======================================================================

	RenderingOpPipeline pipelineOps=null;
	RenderingProperties rp=null;
	ArrayList<RenderedImage> pipelineImages=new ArrayList<RenderedImage>();
	RenderedImage source=null;

	public ImagePipelineRenderer(RenderingOpPipeline pipelineOps, RenderingProperties rp) {initialize(pipelineOps,rp,null);}
	public ImagePipelineRenderer(RenderingOpPipeline pipelineOps, RenderingProperties rp, Image imageSource) {initialize(pipelineOps,rp,imageSource);}

	private void initialize(RenderingOpPipeline pipelineOps, RenderingProperties rp, Image imageSource) {

		this.pipelineOps=pipelineOps;
		this.rp=rp;
		if (imageSource!=null) {
			this.source=imageSource.getRenderedImage();
			render();
		}
		rp.addListener(this);
	}

	// =======================================================================

	public RenderingOpPipeline getPipelineOps() {return pipelineOps;}
	public RenderingProperties getRenderingProperties() {return rp;}

	public RenderedImage getSource() {return source;}
	public RenderedImage getRenderedImage() {return (!pipelineImages.isEmpty()) ? pipelineImages.get(pipelineImages.size()-1) : null;}

	public void setPipelineOps(RenderingOpPipeline x) {pipelineOps=x;}
	public void setRenderingProperties(RenderingProperties x) {rp=x;}

	public void setSource(RenderedImage x) {

		if (source!=null) {
			if (source instanceof PlanarImage) {((PlanarImage)source).dispose();}
			if (source instanceof BufferedImage) {((BufferedImage)source).flush();}
		}
		source=null;
		source=x; 
		render();
	}

	// =======================================================================
	// Empty the current pipeline of images to free up resources.

	public void flush() {

		if (!(pipelineImages.isEmpty())) {
			for (int loop=0, n=pipelineImages.size(); loop<n; loop++) {
				RenderedImage ri=pipelineImages.get(loop);
				if (ri instanceof RenderedOp) {((RenderedOp)ri).dispose();}
				if (ri instanceof BufferedImage) {((BufferedImage)ri).flush();}
				ri=null;
			}
			pipelineImages.clear();
		}
	}

	// =======================================================================

	public void propertyChangeEvent(ImagePropertyChangeEvent ipce) {

		// Get the property name associated with the fired events, and
		// tell the rendering pipeline to update the corresponding
		// rendered image. Need to find the first occurring propertyName
		// to trigger the rerendering accordingly.

		String[] propertyName=ipce.getPropertyName();
		String targetOp=pipelineOps.findFirstOp(propertyName);
		if (pipelineImages.isEmpty()) pipelineOps.render(source,pipelineImages,rp); else pipelineOps.renderOperationByName(source,targetOp,pipelineImages,rp);
	}

	// =======================================================================

	public void render() {flush(); pipelineOps.render(source,pipelineImages,rp);}

	// =======================================================================

	protected void finalize() throws Throwable {super.finalize(); doCleanup();}

	// =======================================================================
	// Explicit cleanup of the pipeline.

	public void doCleanup() {

		if (rp!=null) rp.removeListener(this);
		if (source!=null) {
			if (source instanceof PlanarImage) {((PlanarImage)source).dispose();} else if (source instanceof BufferedImage) {((BufferedImage)source).flush();} source=null;
		}
		if (pipelineImages!=null) {
			for (int loop=0, n=pipelineImages.size(); loop<n; loop++) {
				RenderedImage ri=pipelineImages.get(loop);
				if (ri instanceof PlanarImage) {((PlanarImage)ri).dispose();} else if (ri instanceof BufferedImage) {((BufferedImage)ri).flush();}
			}
			pipelineImages.clear();
			pipelineImages=null;
		}
	}
}
