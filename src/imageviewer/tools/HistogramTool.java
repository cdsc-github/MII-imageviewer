/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import java.util.EventObject;

import javax.media.jai.Histogram;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.FontUIResource;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;

import org.jfree.chart.renderer.xy.XYBarRenderer; 

import org.jfree.data.statistics.SimpleHistogramBin;
import org.jfree.data.statistics.SimpleHistogramDataset;
import org.jfree.ui.Layer;

import imageviewer.model.Image;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.FloatingPanel;
import imageviewer.ui.image.ImagePanel;
import imageviewer.ui.swing.TranslucentPanel;

// =======================================================================

public class HistogramTool extends ImagingTool implements Tool {

	protected static final FontUIResource axisFont=new FontUIResource("Tahoma",Font.PLAIN,11);
	protected static final FontUIResource tickFont=new FontUIResource("Arial",Font.PLAIN,9);

	// =======================================================================

	JFreeChart chart=null;
	ChartPanel cp=null;

	public HistogramTool() {

		cp=new ChartPanel(null);
		cp.setOpaque(false);
		cp.setPreferredSize(new Dimension(500,350));
		cp.setBorder(new EmptyBorder(5,0,0,0));
	}

	// =======================================================================

	public void startTool(EventObject e) {if (toolDialog==null) toolDialog=new FloatingPanel(cp,getToolName(),true,true); showToolDialog();}
	public void endTool(EventObject e) {hideToolDialog();}

	public Cursor getCursor() {return null;}
	public String getToolName() {return new String("Histogram");}

	// =======================================================================

	public void mouseClicked(MouseEvent e) {plot(e);}
	public void mouseDragged(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	// =======================================================================

	public void plot(MouseEvent e) {

		if (e.getSource() instanceof ImagePanel) {
			Image image=((ImagePanel)e.getSource()).getSource();
			Histogram h=image.getHistogram();
			SimpleHistogramDataset shd=new SimpleHistogramDataset("Voxel distribution"); 
			for (int i=0, numBands=h.getNumBands(); i<numBands; i++) {
				int[] binData=h.getBins(i);
				for (int j=0; j<binData.length; j++) {
					SimpleHistogramBin shb=new SimpleHistogramBin(j-0.5,j+0.5,true,false);
					shb.setItemCount(binData[j]);
					shd.addBin(shb);
				}
			}
			double[] domainBounds=null, rangeBounds=null;
			if (chart!=null) {
				ValueAxis va=chart.getXYPlot().getDomainAxis();
				domainBounds=new double[] {va.getLowerBound(),va.getUpperBound()};
				va=chart.getXYPlot().getRangeAxis();
				rangeBounds=new double[] {va.getLowerBound(),va.getUpperBound()};
			}
			chart=ChartFactory.createHistogram(null,"Voxel value","Count",shd,PlotOrientation.VERTICAL,false,true,false);
			chart.setBackgroundPaint(new Color(20,30,45));
			chart.setAntiAlias(true);
			ValueAxis domainAxis=chart.getXYPlot().getDomainAxis();
			domainAxis.setLabelFont(axisFont);
			domainAxis.setTickLabelFont(tickFont);
			domainAxis.setAxisLinePaint(Color.white);
			domainAxis.setTickLabelPaint(Color.white);
			domainAxis.setLabelPaint(Color.white);
			if (domainBounds!=null) {
				domainAxis.setAutoRange(false);
				domainAxis.setLowerBound(domainBounds[0]);
				domainAxis.setUpperBound(domainBounds[1]);
			} else {
				domainAxis.setLowerBound(0);
			}
			ValueAxis rangeAxis=chart.getXYPlot().getRangeAxis();
			rangeAxis.setLabelFont(axisFont);
			rangeAxis.setTickLabelFont(tickFont);
			rangeAxis.setAxisLinePaint(Color.white);
			rangeAxis.setTickLabelPaint(Color.white);
			rangeAxis.setLabelPaint(Color.white);
			if (rangeBounds!=null) {
				rangeAxis.setAutoRange(false);
				rangeAxis.setLowerBound(rangeBounds[0]);
				rangeAxis.setUpperBound(rangeBounds[1]);
			}
			chart.getXYPlot().getRenderer().setSeriesPaint(0,new Color(0,51,113));
			((XYBarRenderer)chart.getXYPlot().getRenderer()).setDrawBarOutline(false);
			chart.getXYPlot().setBackgroundAlpha(0.05f);

			double[] mean=h.getMean();
			double[] sd=h.getStandardDeviation();
			IntervalMarker im=new IntervalMarker(mean[0]-sd[0]/2,mean[0]+sd[0]/2);
			im.setPaint(new Color(200,200,200,128));
			chart.getXYPlot().addDomainMarker(0,im,Layer.BACKGROUND);			
			cp.setChart(chart);
		}
	}
}
