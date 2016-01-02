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
import java.util.HashMap;

import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import org.apache.log4j.Logger;

// =======================================================================

public class RenderingOpPipeline {

	private static Logger LOG=Logger.getLogger("imageviewer.rendering");

	// =======================================================================

	ArrayList<RenderedOperation> pipelineOps=new ArrayList<RenderedOperation>();
	HashMap<String,ArrayList> operationPropertyMap=new HashMap<String,ArrayList>();
	String name=new String();

	public RenderingOpPipeline() {}

	// =======================================================================

	public ArrayList<RenderedOperation> getPipeline() {return pipelineOps;}
	public String getName() {return name;}

	public void setPipeline(ArrayList<RenderedOperation> x) {pipelineOps=x;}
	public void setName(String x) {name=x;}

	public boolean isEmpty() {return pipelineOps.isEmpty();}

	public void add(RenderedOperation x) {pipelineOps.add(x); addOperationProperties(x);}
	public void remove(RenderedOperation x) {pipelineOps.remove(x); removeOperationProperties(x);}
	public void indexOf(RenderedOperation x) {pipelineOps.indexOf(x);}

	// =======================================================================

	private void addOperationProperties(RenderedOperation ro) {

		String[] properties=ro.getListenerProperties();
		if (properties!=null) {
			for (int loop=0; loop<properties.length; loop++) {
				ArrayList ops=operationPropertyMap.get(properties[loop]);
				if (ops!=null) {
					ops.add(ro);
				} else {
					ops=new ArrayList();
					ops.add(ro);
					operationPropertyMap.put(properties[loop],ops);
				}
			}
		}
	}

	// =======================================================================

	private void removeOperationProperties(RenderedOperation ro) {

		String[] properties=ro.getListenerProperties();
		if (properties!=null) {
			for (int loop=0; loop<properties.length; loop++) {
				ArrayList ops=operationPropertyMap.get(properties[loop]);
				if (ops!=null) ops.remove(ro);
			}
		}
	}

	// =======================================================================

	public void render(RenderedImage source, ArrayList pipelineImages, RenderingProperties rp) {

		for (int loop=0, n=pipelineOps.size(); loop<n; loop++) {
			RenderedImage ri=(loop==0) ? source : (RenderedImage)pipelineImages.get(loop-1);
			RenderedImage newImage=pipelineOps.get(loop).performOperation(ri,rp);
			if (pipelineImages.size()>loop) {
				RenderedImage oldImage=(RenderedImage)pipelineImages.set(loop,newImage);
				if (oldImage instanceof PlanarImage) ((PlanarImage)oldImage).dispose();
				if (oldImage instanceof BufferedImage) ((BufferedImage)oldImage).flush();
				oldImage=null;
			} else {
				pipelineImages.add(newImage);
			} 
			ri=null;
			newImage=null;
		}
	}

	// =======================================================================

	public void renderOperationByName(RenderedImage source, String opName, ArrayList pipelineImages, RenderingProperties rp) {

		int opIndex=findOpIndex(opName);
		if (opIndex!=-1) {
			for (int loop=opIndex, n=pipelineOps.size(); loop<n; loop++) {
				RenderedImage ri=(loop==0) ? source : (RenderedImage)pipelineImages.get(loop-1);
				RenderedImage newImage=pipelineOps.get(loop).performOperation(ri,rp);
				if (pipelineImages.size()>loop) {
					RenderedImage oldImage=(RenderedImage)pipelineImages.set(loop,newImage);
					if (oldImage instanceof PlanarImage) ((PlanarImage)oldImage).dispose();
					if (oldImage instanceof BufferedImage) ((BufferedImage)oldImage).flush();
					oldImage=null;
				} else {
					pipelineImages.add(newImage);
				} 
				ri=null;
				newImage=null;
			}
		}
	}

	// =======================================================================
	// Do the actual rendering of the images.  The rendering of the
	// pipeline starts at a given index and progresses to the end of the
	// chain.  Each image is based on the previously computed image in
	// the pipeline.  A string is passed in as an argument: do a lookup
	// of the operation name to determine what index it is in the
	// pipeline.  
	
	public void renderOperationByProperty(RenderedImage source, String propertyName, ArrayList pipelineImages, RenderingProperties rp) {
		
		// Only executes the pipeline from a given point by starting at
		// the first operation for a given rendering property.

		ArrayList ops=operationPropertyMap.get(propertyName);
		if (ops==null) {
			render(source,pipelineImages,rp);
		} else {
			if (ops.isEmpty()) {
				render(source,pipelineImages,rp);
			} else {
				RenderedOperation ro=(RenderedOperation)ops.get(0);
				int pipelineStart=findOpIndex(ro.getRenderedOperationName());
				if (pipelineStart!=-1) {
					for (int loop=pipelineStart, n=pipelineOps.size(); loop<n; loop++) {
						RenderedImage ri=(loop==0) ? source : (RenderedImage)pipelineImages.get(loop-1);
						RenderedImage oldImage=(RenderedImage)(pipelineImages.set(loop,pipelineOps.get(loop).performOperation(ri,rp)));
						if (oldImage instanceof PlanarImage) ((PlanarImage)oldImage).dispose();
						if (oldImage instanceof BufferedImage) ((BufferedImage)oldImage).flush();
						oldImage=null;
					}
				}
			}
		}
	}

	// =======================================================================
	// Given a sequence of properties, find out which one occurs first in
	// the pipeline.

	public String findFirstOp(String[] propertyNames) {

		for (int loop=0, n=pipelineOps.size(); loop<n; loop++) {
			RenderedOperation ro=(RenderedOperation)pipelineOps.get(loop);
			String[] opProperties=ro.getListenerProperties();
			for (int i=0; i<opProperties.length; i++) {
				for (int j=0; j<propertyNames.length; j++) {
					if (propertyNames[j].compareTo(opProperties[i])==0) return ro.getRenderedOperationName(); //propertyNames[j];
				}
			}
		}
		return null;
	}

	// =======================================================================

	private int findOpIndex(String opName) {

		for (int loop=0, n=pipelineOps.size(); loop<n; loop++) {
			RenderedOperation ro=pipelineOps.get(loop);
			if (opName.compareTo(ro.getRenderedOperationName())==0) return loop;
		}
		return -1;
	}
}
