/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import java.util.Map;
import java.util.HashMap;

import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.painter.Painter;

import org.jvnet.flamingo.ribbon.JRibbonBand;
import org.jvnet.flamingo.ribbon.JToggleTabButton;
import org.jvnet.flamingo.ribbon.ui.BasicRibbonUI;
import org.jvnet.flamingo.ribbon.ui.RibbonBandCollapseKind;

// =======================================================================

public class ImageViewerRibbonUI extends BasicRibbonUI {

	private static final Painter BACKGROUND_PAINTER=(Painter)UIManager.get("JRibbon.taskBackgroundPainter");
	private static final Color RIBBON_TOP_LIGHT=UIManager.getColor("JRibbon.ribbonBackgroundTopLight");
	private static final Color RIBBON_TOP_DARK=UIManager.getColor("JRibbon.ribbonBackgroundTopDark");
	private static final Color RIBBON_BOTTOM_LIGHT=UIManager.getColor("JRibbon.ribbonBackgroundBottomLight");
	private static final Color RIBBON_BOTTOM_DARK=UIManager.getColor("JRibbon.ribbonBackgroundBottomDark");

	private static final Border BAND_BORDER=UIManager.getBorder("RibbonBand.border");

	// =======================================================================
	
	public static ComponentUI createUI(JComponent c) {return new ImageViewerRibbonUI();}

	protected void installComponents() {ribbon.setLayout(createLayoutManager());}
	protected void uninstallComponents() {ribbon.setLayout(null);}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		if (BACKGROUND_PAINTER!=null) BACKGROUND_PAINTER.paint(g2,c,c.getWidth(),c.getHeight());
		GradientPaint gp1=new GradientPaint(0,getTaskbarHeight(),RIBBON_TOP_LIGHT,0,getTaskbarHeight()+20,RIBBON_TOP_DARK);
		Shape s1=new RoundRectangle2D.Double(1,getTaskbarHeight(),c.getWidth()-2,getTaskbarHeight()+20,6,6);
		Area a1=new Area(s1);
		a1.add(new Area(new Rectangle2D.Double(1,getTaskbarHeight()+20,c.getWidth()-2,10)));
		g2.setPaint(gp1);
		g2.fill(a1);
		GradientPaint gp2=new GradientPaint(0,getTaskbarHeight()+20,RIBBON_BOTTOM_DARK,0,c.getHeight()-20,RIBBON_BOTTOM_LIGHT);
		Shape s2=new RoundRectangle2D.Double(1,getTaskbarHeight()+20,c.getWidth()-2,c.getHeight()-getTaskbarHeight()-16,6,6);
		Area a2=new Area(s2);
		a2.add(new Area(new Rectangle2D.Double(1,getTaskbarHeight()+20,c.getWidth()-2,10)));
		g2.setPaint(gp2);
		g2.fill(a2);
		super.paint(g,c);
		g2.dispose();
	}

	protected void paintTaskBorder(Graphics g, int x, int y, int width, int height) {}

  protected LayoutManager createLayoutManager() {return new ImageViewerRibbonLayout();}

	// =======================================================================

	private class ImageViewerRibbonLayout implements LayoutManager {

		public final int TOTAL_HEIGHT=getTaskbarHeight()+RibbonConstants.BAND_HEADER_HEIGHT+(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET);

		public void addLayoutComponent(String name, Component c) {}
		public void removeLayoutComponent(Component c) {}

		public Dimension preferredLayoutSize(Container c) {return new Dimension(c.getWidth(),TOTAL_HEIGHT);}

		public Dimension minimumLayoutSize(Container c) {
		
			int width=0;
			Insets i=c.getInsets();
			for (JRibbonBand ribbonBand : ribbon.getBands()) {
				ImageViewerRibbonBandUI bandUI=(ImageViewerRibbonBandUI)ribbonBand.getUI();
				Insets bandInsets=ribbonBand.getInsets();
				int availableBandHeight=c.getHeight()-i.top-getTaskbarHeight();
				width+=bandUI.getPreferredWidth(RibbonBandCollapseKind.ICON,availableBandHeight);
				width+=4;
			}
			width-=4;
			return new Dimension(width,TOTAL_HEIGHT);
		}

		public void layoutContainer(Container c) {

			Insets ribbonInsets=c.getInsets();

			int x=2;
			for (Component regComp : ribbon.getRegularComponents()) {
				int pw=regComp.getPreferredSize().width;
				regComp.setBounds(x,0,pw,getTaskbarHeight());
				x+=(pw+2);
			}

			int totalTaskButtonsWidth=0;
			for (JToggleButton taskToggleButton : ribbon.getTaskToggleButtons()) {
				int pw=taskToggleButton.getPreferredSize().width;
				totalTaskButtonsWidth+=(pw+10);
			}

			int helpTaskButtonWidth=0;
			int leftTaskButtonOffset=20;
			JToggleTabButton helpTaskButton=ribbon.getHelpTaskButton();
			if (helpTaskButton!=null) helpTaskButtonWidth=helpTaskButton.getPreferredSize().width;
			totalTaskButtonsWidth-=helpTaskButtonWidth;
			totalTaskButtonsWidth-=leftTaskButtonOffset;

			switch (ribbon.getAlignment()) {
			    case SwingConstants.LEFT:	break;
			   case SwingConstants.RIGHT:	x=Math.max(x,c.getWidth()-totalTaskButtonsWidth-helpTaskButtonWidth);	break;
			  case SwingConstants.CENTER:	x+=Math.max(x,(c.getWidth()-totalTaskButtonsWidth)/2-helpTaskButtonWidth);
			}

			for (JToggleButton taskToggleButton : ribbon.getTaskToggleButtons()) {
				int pw=taskToggleButton.getPreferredSize().width;
				if (taskToggleButton!=helpTaskButton) {
					taskToggleButton.setBounds(x+leftTaskButtonOffset,0,pw,getTaskbarHeight());
					x+=(pw+10);
				} else {
					taskToggleButton.setBounds(c.getWidth()-helpTaskButtonWidth-10+leftTaskButtonOffset,0,pw,getTaskbarHeight());
				}
			}

			int topOffset=0, bottomOffset=0, leftOffset=0, rightOffset=0;
			if (BAND_BORDER!=null) {
				Insets i=BAND_BORDER.getBorderInsets(c);
				topOffset=i.top;
				bottomOffset=i.bottom;
				leftOffset=i.left;
				rightOffset=1;
			}

			Map<RibbonBandCollapseKind,Integer> widths=new HashMap<RibbonBandCollapseKind,Integer>();
			for (RibbonBandCollapseKind collapseKind : RibbonBandCollapseKind.values()) {
				int totalWidth=0;
				for (JRibbonBand panel : ribbon.getBands()) {
					int availableBandHeight=c.getHeight()-ribbonInsets.top-getTaskbarHeight()-ribbonInsets.bottom;
					ImageViewerRibbonBandUI ui=(ImageViewerRibbonBandUI)panel.getUI();
					totalWidth+=ui.getPreferredWidth(collapseKind,availableBandHeight)+leftOffset;
				}
				widths.put(collapseKind,totalWidth);
			}

			RibbonBandCollapseKind bestFitCollapse=RibbonBandCollapseKind.getSortedKinds().getLast();
			for (RibbonBandCollapseKind collapseKind : RibbonBandCollapseKind.getSortedKinds()) {
				if (widths.get(collapseKind)<c.getWidth()) {
					bestFitCollapse=collapseKind;
					break;
				}
			}

			int bestFitWidth=widths.get(bestFitCollapse);
			double coef=((double)c.getWidth()-8)/bestFitWidth;
			int i=RibbonBandCollapseKind.getSortedKinds().indexOf(bestFitCollapse);

			x=3;
			int widthOffset=0, index=0, n=ribbon.getBands().size();

			Insets insets=c.getInsets();
			int availableWidth=c.getWidth()-insets.left-insets.right;
			int availableHeight=c.getHeight()-insets.top-insets.bottom;
			int widthToDistribute=availableWidth-bestFitWidth;

			for (JRibbonBand band : ribbon.getBands()) {
				ImageViewerRibbonBandUI ui=(ImageViewerRibbonBandUI)band.getUI();
				int maxPrefWidth=ui.getPreferredWidth(RibbonBandCollapseKind.NONE,availableHeight);
				int pw=Math.max(BAND_MIN_WIDTH,ui.getPreferredWidth(bestFitCollapse,availableHeight));

				// Try allocating the available pixels to the current band and
				// see if the collapse kind for this specific band will
				// change.

				int availableForBand=pw+widthToDistribute;
				RibbonBandCollapseKind bestFitForBand=RibbonBandCollapseKind.getSortedKinds().getLast();
				for (RibbonBandCollapseKind collapseKind : RibbonBandCollapseKind.getSortedKinds()) {
					if (ui.getPreferredWidth(collapseKind,availableHeight)<=availableForBand) {
						bestFitForBand=collapseKind;
						break;
					}
				}

				// Now that the collapse kind of the band is computed
				// according to the actual available width, compute the
				// preferred band width under that collapse kind

				int fw=ui.getPreferredWidth(bestFitForBand,availableHeight);
				widthToDistribute-=(fw-pw);
				Insets bandInsets=band.getInsets();
				band.setBounds(x,insets.top+getTaskbarHeight()+bandInsets.top,fw,c.getHeight()-insets.top-getTaskbarHeight()-bandInsets.top-bandInsets.bottom-insets.bottom);
				band.doLayout();
				RibbonBandCollapseKind actualCollapseKind=ui.getCurrentFitCollapse();
				ImageViewerRibbonBandUI ribbonBandUI=(ImageViewerRibbonBandUI)band.getUI();
				if (ribbonBandUI.getExpandButton()!=null) {
					int ebpw=ribbonBandUI.getExpandButton().getPreferredSize().width;
					int ebph=ribbonBandUI.getExpandButton().getPreferredSize().height;
					ribbonBandUI.getExpandButton().setBounds(fw-4-ebpw,(RibbonConstants.BAND_HEADER_HEIGHT-ebph)/2,ebpw,ebph);
				}

				x+=(fw+2);

				/*
				int fw=(int)Math.min((coef*pw),maxPrefWidth);
				if (bestFitCollapse==RibbonBandCollapseKind.ICON) {
					fw=pw;
				} else if (widthOffset!=0) fw=Math.max(BAND_MIN_WIDTH,Math.max(ui.getPreferredWidth(bestFitCollapse)+leftOffset+rightOffset,fw-widthOffset));

				// Can we potentially expand it to the next size up and leave
				// the next one alone or shrink it? We leave it alone if it's
				// the last item, but do a final check.

				if (bestFitCollapse!=RibbonBandCollapseKind.NONE) {
					if (index<n-1) {
						int nextPW=0;
						for (int loop=index+1; loop<n; loop++) {
							JRibbonBand nextPanel=ribbon.getBands().get(index+1);
							ImageViewerRibbonBandUI nextUI=(ImageViewerRibbonBandUI)nextPanel.getUI();
							nextPW+=Math.max(BAND_MIN_WIDTH,nextUI.getPreferredWidth(bestFitCollapse)+leftOffset);
						}
						int currentPWExpanded=Math.max(BAND_MIN_WIDTH,ui.getPreferredWidth(RibbonBandCollapseKind.getSortedKinds().get(i-1))+leftOffset);
						if (x+currentPWExpanded+nextPW<c.getWidth()) {
							widthOffset=currentPWExpanded-fw;
							fw=currentPWExpanded+leftOffset+rightOffset;
						} else {
							ui.getPreferredWidth(bestFitCollapse);
							widthOffset=0;
						}
					} else {
						widthOffset=0;
						if (index==n-1) {
							int currentPWExpanded=Math.max(BAND_MIN_WIDTH,ui.getPreferredWidth(RibbonBandCollapseKind.getSortedKinds().get(i-1))+leftOffset);
							if (x+currentPWExpanded<c.getWidth())  {
								fw=currentPWExpanded+leftOffset+rightOffset;
							} else {
								ui.getPreferredWidth(bestFitCollapse);
							}		
							if (x+fw+2>=c.getWidth()-4) fw=c.getWidth()-x-6;
						}
					}
				}
			

				panel.setBounds(x,getTaskbarHeight()+topOffset,fw+2, //-leftOffset, //-rightOffset,
												RibbonConstants.BAND_HEADER_HEIGHT+(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET)-topOffset-bottomOffset);
				panel.doLayout();
				ImageViewerRibbonBandUI ribbonBandUI=(ImageViewerRibbonBandUI)panel.getUI();
				if (ribbonBandUI.getExpandButton()!=null) {
					int ebpw=ribbonBandUI.getExpandButton().getPreferredSize().width;
					int ebph=ribbonBandUI.getExpandButton().getPreferredSize().height;
					ribbonBandUI.getExpandButton().setBounds(fw-4-ebpw,(RibbonConstants.BAND_HEADER_HEIGHT-ebph)/2,ebpw,ebph);
				}
				x+=fw+4;
				index++;
				*/
			}
		}
	}
}
