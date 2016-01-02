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
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;

import javax.swing.plaf.ComponentUI;

import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.ribbon.RibbonElementPriority;

import org.jvnet.flamingo.ribbon.ui.BasicBandControlPanelUI;
import org.jvnet.flamingo.ribbon.ui.RibbonBandCollapseKind;
import org.jvnet.flamingo.ribbon.ui.JRibbonGallery;

// =======================================================================

public class ImageViewerBandControlPanelUI extends BasicBandControlPanelUI {

	private static final Color BACKGROUND_PAINT1=UIManager.getColor("BandControlPanel.backgroundPaintLight");
	private static final Color BACKGROUND_PAINT2=UIManager.getColor("BandControlPanel.backgroundPaintDark");

	public static final int X_BORDER=2;

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new ImageViewerBandControlPanelUI();}

	// =======================================================================

	public void installUI(JComponent c) {super.installUI(c); c.setOpaque(false); c.setLayout(createLayoutManager());}
	public void uninstallUI(JComponent c) {super.uninstallUI(c); c.setLayout(null);}

	public void paint(Graphics g, JComponent c) {}

	protected void paintBandBackground(Graphics graphics, Rectangle toFill) {

		Graphics2D g2=(Graphics2D)graphics.create();
		if ((BACKGROUND_PAINT1!=null)&&(BACKGROUND_PAINT2!=null)) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.setPaint(new GradientPaint(toFill.x,toFill.y,BACKGROUND_PAINT1,toFill.x,toFill.y+20,BACKGROUND_PAINT2));
		} else {
			g2.setColor(controlPanel.getBackground());
		} 
		g2.fillRect(toFill.x,toFill.y,toFill.width,toFill.height);
		g2.dispose();
	}

	// =======================================================================

	protected LayoutManager createLayoutManager() {return new ImageViewerControlPanelLayout();}

	// public int getPreferredWidth(RibbonBandCollapseKind collapseKind) {
	//
	//	int w=super.getPreferredWidth(collapseKind);
	//	return (collapseKind==RibbonBandCollapseKind.HIGH_TO_LOW) ? w+20 : w;
	// }

	// =======================================================================
	// Replicated inner class to handle specific layout issues...sigh...

	private class ImageViewerControlPanelLayout implements LayoutManager {

		public void addLayoutComponent(String name, Component c) {}
		public void removeLayoutComponent(Component c) {}

		public Dimension preferredLayoutSize(Container c) {return c.getSize();}
		public Dimension minimumLayoutSize(Container c) {return preferredLayoutSize(c);}

		public void layoutContainer(Container c) {

			Insets i=c.getInsets();
			int x=X_BORDER;
			int availableHeight=c.getHeight()-i.top-i.bottom-RibbonConstants.BAND_OFFSET; //-ins.top-ins.bottom;

			RibbonBandCollapseKind bestFitCollapse=RibbonBandCollapseKind.getSortedKinds().getLast();
			int collapsedWidth=0;
			for (RibbonBandCollapseKind collapseKind : RibbonBandCollapseKind.getSortedKinds()) {
				collapsedWidth=getPreferredWidth(collapseKind,availableHeight);
				if (collapsedWidth<=c.getWidth()) {
					bestFitCollapse=collapseKind;
					break;
				}
			}

			currentFitCollapse=bestFitCollapse;
			if (bestFitCollapse==RibbonBandCollapseKind.ICON) return;
			
			List<JRibbonGallery> topGalleries=controlPanel.getRibbonGalleries(RibbonElementPriority.TOP);
			List<JRibbonGallery> mediumGalleries=controlPanel.getRibbonGalleries(RibbonElementPriority.MEDIUM);
			List<JRibbonGallery> lowGalleries=controlPanel.getRibbonGalleries(RibbonElementPriority.LOW);

			switch (bestFitCollapse) {
			      case NONE: for (JRibbonGallery top : topGalleries) top.setState(ElementState.BIG);
							         for (JRibbonGallery med : mediumGalleries)	med.setState(ElementState.BIG);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.BIG);
											 break;
			case LOW_TO_MID: for (JRibbonGallery top : topGalleries) top.setState(ElementState.BIG);
				               for (JRibbonGallery med : mediumGalleries) med.setState(ElementState.BIG);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.MEDIUM);
											 break;
			case MID_TO_MID: for (JRibbonGallery top : topGalleries) top.setState(ElementState.BIG);
				               for (JRibbonGallery med : mediumGalleries) med.setState(ElementState.MEDIUM);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.MEDIUM);
											 break;
			case LOW_TO_LOW: for (JRibbonGallery top : topGalleries) top.setState(ElementState.BIG);
				               for (JRibbonGallery med : mediumGalleries) med.setState(ElementState.MEDIUM);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.SMALL);
											 break;
			case MID_TO_LOW: for (JRibbonGallery top : topGalleries) top.setState(ElementState.BIG);
				               for (JRibbonGallery med : mediumGalleries) med.setState(ElementState.SMALL);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.SMALL);
											 break;
 		 case HIGH_TO_MID: for (JRibbonGallery top : topGalleries) top.setState(ElementState.MEDIUM);
				               for (JRibbonGallery med : mediumGalleries) med.setState(ElementState.SMALL);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.SMALL);
											 break;
		 case HIGH_TO_LOW: for (JRibbonGallery top : topGalleries) top.setState(ElementState.SMALL);
				               for (JRibbonGallery med : mediumGalleries) med.setState(ElementState.SMALL);
											 for (JRibbonGallery low : lowGalleries) low.setState(ElementState.SMALL);
											 break;
			}

			Map<ElementState,List<JRibbonGallery>>galleryStateMap=new HashMap<ElementState,List<JRibbonGallery>>();
			galleryStateMap.put(ElementState.BIG,new LinkedList<JRibbonGallery>());
			galleryStateMap.put(ElementState.MEDIUM,new LinkedList<JRibbonGallery>());
			galleryStateMap.put(ElementState.SMALL,new LinkedList<JRibbonGallery>());

			for (JRibbonGallery top : topGalleries) galleryStateMap.get(top.getState()).add(top);
			for (JRibbonGallery med : mediumGalleries) galleryStateMap.get(med.getState()).add(med);
			for (JRibbonGallery low : lowGalleries) galleryStateMap.get(low.getState()).add(low);

			for (JRibbonGallery big : galleryStateMap.get(ElementState.BIG)) {
				int pw=big.getPreferredWidth(ElementState.BIG,availableHeight);
				big.setBounds(x,4,pw,availableHeight);
				x+=pw;
				x+=X_BORDER;
			}
			for (JRibbonGallery med : galleryStateMap.get(ElementState.MEDIUM)) {
				int pw=med.getPreferredWidth(ElementState.MEDIUM,availableHeight);
				med.setBounds(x,4,pw,availableHeight);
				x+=pw;
				x+=X_BORDER;
			}
			for (JRibbonGallery small : galleryStateMap.get(ElementState.SMALL)) {
				int pw=small.getPreferredWidth(ElementState.SMALL,availableHeight);
				small.setBounds(x,4,pw,availableHeight);
				x+=pw;
				x+=X_BORDER;
			}
			for (JPanel panel : controlPanel.getPanels()) {
				int pw=panel.getPreferredSize().width;
				panel.setBounds(x,2,pw,availableHeight+RibbonConstants.BAND_OFFSET);
				x+=pw;
				x+=X_BORDER;
			}

			List<JCommandButton> topButtons=controlPanel.getRibbonButtons(RibbonElementPriority.TOP);
			List<JCommandButton> mediumButtons=controlPanel.getRibbonButtons(RibbonElementPriority.MEDIUM);
			List<JCommandButton> lowButtons=controlPanel.getRibbonButtons(RibbonElementPriority.LOW);
			
			switch (bestFitCollapse) {

			      case NONE: for (JCommandButton top : topButtons) top.setState(ElementState.BIG,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.BIG,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.BIG,true);
											 break;
			case LOW_TO_MID: for (JCommandButton top : topButtons) top.setState(ElementState.BIG,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.BIG,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.MEDIUM,true);
											 break;
			case MID_TO_MID: for (JCommandButton top : topButtons) top.setState(ElementState.BIG,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.MEDIUM,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.MEDIUM,true);
											 break;
			case LOW_TO_LOW: for (JCommandButton top : topButtons) top.setState(ElementState.BIG,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.MEDIUM,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.SMALL,true);
											 break;
			case MID_TO_LOW: for (JCommandButton top : topButtons) top.setState(ElementState.BIG,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.SMALL,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.SMALL,true);
											 break;
		 case HIGH_TO_MID: for (JCommandButton top : topButtons) top.setState(ElementState.MEDIUM,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.SMALL,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.SMALL,true);
											 break;
		 case HIGH_TO_LOW: for (JCommandButton top : topButtons) top.setState(ElementState.SMALL,true);
				               for (JCommandButton med : mediumButtons) med.setState(ElementState.SMALL,true);
											 for (JCommandButton low : lowButtons) low.setState(ElementState.SMALL,true);
											 break;
			}

			Map<ElementState,List<JCommandButton>> buttonStateMap=new HashMap<ElementState,List<JCommandButton>>();
			buttonStateMap.put(ElementState.BIG,new LinkedList<JCommandButton>());
			buttonStateMap.put(ElementState.MEDIUM,new LinkedList<JCommandButton>());
			buttonStateMap.put(ElementState.SMALL,new LinkedList<JCommandButton>());

			for (JCommandButton top : topButtons) buttonStateMap.get(top.getState()).add(top);
			for (JCommandButton med : mediumButtons) buttonStateMap.get(med.getState()).add(med);
			for (JCommandButton low : lowButtons) buttonStateMap.get(low.getState()).add(low);

			for (JCommandButton big : buttonStateMap.get(ElementState.BIG)) {
				big.setBounds(x,4,big.getPreferredSize().width,(availableHeight+RibbonConstants.BAND_OFFSET)-10);
				x+=big.getPreferredSize().width;
				x+=X_BORDER;
			}

			int medSize=buttonStateMap.get(ElementState.MEDIUM).size();
			if (medSize>0) {
				while (((buttonStateMap.get(ElementState.MEDIUM).size() % 3)!=0)	&& (buttonStateMap.get(ElementState.SMALL).size()>0)) {
					JCommandButton low=buttonStateMap.get(ElementState.SMALL).get(0);
					buttonStateMap.get(ElementState.SMALL).remove(low);
					low.setState(ElementState.MEDIUM,true);
					buttonStateMap.get(ElementState.MEDIUM).add(low);
				}
			}

			int index3=0;
			int maxWidth3=0;
			int yOffset=21; //(int)((availableHeight+RibbonConstants.BAND_OFFSET)/3);

			Set<JCommandButton> threesome=new HashSet<JCommandButton>();
			for (JCommandButton medium : buttonStateMap.get(ElementState.MEDIUM)) {
				int medWidth=medium.getPreferredSize().width;
				maxWidth3=Math.max(maxWidth3,medWidth);
				medium.setBounds(x,4+index3*(2+yOffset),medWidth,yOffset);
				threesome.add(medium);
				index3++;
				if (index3==3) {
					index3=0;
					x+=maxWidth3;
					x+=X_BORDER;
					maxWidth3=0;
					threesome.clear();
				}
			}

			x+=maxWidth3;
			if (maxWidth3>0) x+=X_BORDER;

			index3=0;
			maxWidth3=0;
			for (JCommandButton small : buttonStateMap.get(ElementState.SMALL)) {
				int lowWidth=small.getPreferredSize().width;
				maxWidth3=Math.max(maxWidth3,lowWidth);
				small.setBounds(x,4+index3*(2+yOffset),lowWidth,yOffset);
				threesome.add(small);
				index3++;
				if (index3==3) {
					for (JCommandButton button : threesome) {
						Rectangle bounds=button.getBounds();
						button.setBounds(bounds.x,bounds.y,maxWidth3,bounds.height);
					}
					index3=0;
					x+=maxWidth3;
					x+=X_BORDER;
					maxWidth3=0;
					threesome.clear();
				}
			}
			x+=maxWidth3;
			if (maxWidth3>0) x+=X_BORDER;

			int shiftX=(c.getWidth()-x)/2;
			if (controlPanel.hasRibbonGalleries()) {
				int galleryCount=controlPanel.getRibbonGalleriesCount();
				int delta=0;
				delta=(shiftX<0) ? -2*(int)Math.ceil((double)(-shiftX)/(double)galleryCount) : 2*(int)Math.ceil((double)(shiftX)/(double)galleryCount);
				int totalDelta=0;
				for (JRibbonGallery top : topGalleries) {
					Rectangle bounds=top.getBounds();
					top.setBounds(bounds.x+totalDelta,bounds.y,bounds.width+delta,bounds.height);
					totalDelta+=delta;
				}
				for (JRibbonGallery med : mediumGalleries) {
					Rectangle bounds=med.getBounds();
					med.setBounds(bounds.x+totalDelta,bounds.y,bounds.width+delta,bounds.height);
					totalDelta+=delta;
				}
				for (JRibbonGallery low : lowGalleries) {
					Rectangle bounds=low.getBounds();
					low.setBounds(bounds.x+totalDelta,bounds.y,bounds.width+delta,bounds.height);
					totalDelta+=delta;
				}
				for (JPanel panel : controlPanel.getPanels()) {
					Rectangle bounds=panel.getBounds();
					panel.setBounds(bounds.x+2*shiftX,bounds.y,bounds.width,bounds.height);
				}
				for (JCommandButton top : topButtons) {
					Rectangle bounds=top.getBounds();
					top.setBounds(bounds.x+2*shiftX,bounds.y,bounds.width,bounds.height);
				}
				for (JCommandButton med : mediumButtons) {
					Rectangle bounds=med.getBounds();
					med.setBounds(bounds.x+2*shiftX,bounds.y,bounds.width,bounds.height);
				}
				for (JCommandButton low : lowButtons) {
					Rectangle bounds=low.getBounds();
					low.setBounds(bounds.x+2*shiftX,bounds.y,bounds.width,bounds.height);
				}
			} else {
				if (shiftX>0) {
					for (JPanel panel : controlPanel.getPanels()) {
						Rectangle bounds=panel.getBounds();
						panel.setBounds(bounds.x+shiftX,bounds.y,bounds.width,bounds.height);
					}
					for (JCommandButton top : topButtons) {
						Rectangle bounds=top.getBounds();
						top.setBounds(bounds.x+shiftX,bounds.y,bounds.width,bounds.height);
					}
					for (JCommandButton med : mediumButtons) {
						Rectangle bounds=med.getBounds();
						med.setBounds(bounds.x+shiftX,bounds.y,bounds.width,bounds.height);
					}
					for (JCommandButton low : lowButtons) {
						Rectangle bounds=low.getBounds();
						low.setBounds(bounds.x+shiftX,bounds.y,bounds.width,bounds.height);
					}
				}
			}
		}
	}
}
