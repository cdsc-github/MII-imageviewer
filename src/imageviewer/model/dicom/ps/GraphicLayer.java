/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.dicom.ps;

import java.io.Serializable;

// =======================================================================

public class GraphicLayer implements Serializable {

	private static final long serialVersionUID = -812635446966976018L;

	String graphicLayer=null;
	String graphicLayerDescription=null;
	short graphicLayerOrder=0;
	short graphicLayerRecommendedGS=0;
	short[] graphicLayerRecommendedRGB=null;

	public GraphicLayer() {}

	// =======================================================================

	public String getGraphicLayer() {return graphicLayer;}
	public String getGraphicLayerDescription() {return graphicLayerDescription;}
	public short getGraphicLayerOrder() {return graphicLayerOrder;}
	public short getGraphicLayerRecommendedGS() {return graphicLayerRecommendedGS;}
	public short[] getGraphicLayerRecommendedRGB() {return graphicLayerRecommendedRGB;}

	public void setGraphicLayer(String x) {graphicLayer=x;}
	public void setGraphicLayerDescription(String x) {graphicLayerDescription=x;}
	public void setGraphicLayerOrder(short x) {graphicLayerOrder=x;}
	public void setGraphicLayerRecommendedGS(short x) {graphicLayerRecommendedGS=x;}
	public void setGraphicLayerRecommendedRGB(short[] x) {graphicLayerRecommendedRGB=x;}

}
