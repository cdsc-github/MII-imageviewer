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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;

import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JPopupPanel;
import org.jvnet.flamingo.ribbon.JBandControlPanel;
import org.jvnet.flamingo.ribbon.JRibbonBand;

import org.jvnet.flamingo.ribbon.ui.BasicRibbonBandUI;
import org.jvnet.flamingo.ribbon.ui.RibbonBandCollapseKind;

// =======================================================================

public class ImageViewerRibbonBandUI extends BasicRibbonBandUI {

	public static final String RIBBON_TITLE_TEXT=new String("__RIBBON_TEXT");

	private static final Color BORDER_HIGHLIGHT=UIManager.getColor("RibbonBand.borderHighlight");
	private static final Border BAND_BORDER=UIManager.getBorder("RibbonBand.border");

	// =======================================================================

	public static ComponentUI createUI(JComponent c) {return new ImageViewerRibbonBandUI();}

	public void installUI(JComponent c) {
		ribbonBand=(JRibbonBand)c;
		installDefaults();
		installComponents();
		installListeners();
		c.setLayout(createLayoutManager());
	}

	public void uninstallUI(JComponent c) {
		c.setLayout(null);
		uninstallListeners();
		uninstallDefaults();
		uninstallComponents();
	}

	protected void installComponents() {

		collapsedButton=new JCommandButton(ribbonBand.getTitle(),ribbonBand.getIcon());
		collapsedButton.setState(ElementState.BIG,true);
		collapsedButton.putClientProperty("isCollapsedButton",Boolean.TRUE);
		ribbonBand.add(collapsedButton);

		if (ribbonBand.getExpandActionListener()!=null) {
			expandButton=createExpandButton();
			ribbonBand.add(expandButton);
			expandButton.setOpaque(false);
			expandButton.setRequestFocusEnabled(false);  
			expandButton.setRolloverEnabled(false); 
			expandButton.setBorderPainted(false);
			expandButton.setContentAreaFilled(false);
		}
	}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g.create();
		int topOffset=0, bottomOffset=0, leftOffset=0, rightOffset=0;
		if (BAND_BORDER!=null) {
			Insets i=BAND_BORDER.getBorderInsets(c);
			topOffset=i.top;
			bottomOffset=i.bottom;
			leftOffset=i.left;
			rightOffset=i.right;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.setColor(BORDER_HIGHLIGHT);
		g2.drawLine(3,1,c.getWidth()-3,1);
		g2.drawLine(1,3,1,c.getHeight()-3);
	
		if (currentFitCollapse!=RibbonBandCollapseKind.ICON) {
			String title=ribbonBand.getTitle();
			JBandControlPanel controlPanel=ribbonBand.getControlPanel();
			RibbonUtil.paintBandTitle(g2,new Rectangle(0,(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET)-bottomOffset-topOffset+2,c.getWidth(),
																								 getBandTitleHeight()-2),title,isUnderMouse,(ribbonBand.getExpandActionListener()!=null));
		}
		g2.dispose();
	}

	// =======================================================================

	protected AbstractButton createExpandButton() {
		JButton result=new JButton(UIManager.getIcon("RibbonBand.expandIcon"));
		result.setPreferredSize(new Dimension(result.getIcon().getIconWidth()+4,result.getIcon().getIconHeight()+4));
		return result;
	}

	// =======================================================================

	protected void paintBandBackground(Graphics g, Rectangle r) {} 

	// =======================================================================

	protected LayoutManager createLayoutManager() {return new ImageViewerRibbonBandLayout();}

	// =======================================================================
	// Additional check; the width fit must also accomodate the title band
	
	public int getPreferredWidth(RibbonBandCollapseKind collapseKind, int availableHeight) {

		int titleWidth=RibbonUtil.getTitleWidth(ribbonBand.getTitle());
		if (ribbonBand.getExpandActionListener()!=null) titleWidth+=20;
		if (collapseKind==RibbonBandCollapseKind.ICON) return (int)Math.max(titleWidth,collapsedButton.getPreferredSize().width);
		JBandControlPanel controlPanel=ribbonBand.getControlPanel();
		if (controlPanel==null) {
			JPopupPanel jpp=collapsedButton.getPopupPanel();
			JRibbonBand popupBand=(JRibbonBand)jpp.getComponent();
			ImageViewerRibbonBandUI popupBandUI=(ImageViewerRibbonBandUI)popupBand.getUI();
			return popupBandUI.getPreferredWidth(collapseKind,availableHeight);
		}
		ImageViewerBandControlPanelUI baseUI=(ImageViewerBandControlPanelUI)controlPanel.getUI();
		return (int)Math.max(titleWidth,6+baseUI.getPreferredWidth(collapseKind,availableHeight));
	}

	// =======================================================================
	
	public AbstractButton getExpandButton() {return expandButton;}

	// =======================================================================
	// Replace the original layoutManager so that we can put the band
	// title at the bottom.

	private class ImageViewerRibbonBandLayout implements LayoutManager {

		public void addLayoutComponent(String name, Component c) {}
		public void removeLayoutComponent(Component c) {}

		public Dimension preferredLayoutSize(Container c) {

			int width=ribbonBand.getControlPanel().isVisible() ? collapsedButton.getPreferredSize().width	: ribbonBand.getControlPanel().getPreferredSize().width;
			JBandControlPanel controlPanel=ribbonBand.getControlPanel();
			return new Dimension(width,getBandTitleHeight()+RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET);
		}

		public Dimension minimumLayoutSize(Container c) {return preferredLayoutSize(c);}

		public void layoutContainer(Container c) {

			if (!c.isVisible())	return;

			Insets i=c.getInsets();
			int availableHeight=c.getHeight()-i.top-i.bottom;

			JBandControlPanel controlPanel=ribbonBand.getControlPanel();

			RibbonBandCollapseKind bestFitCollapse=RibbonBandCollapseKind.getSortedKinds().getLast();
			for (RibbonBandCollapseKind collapseKind : RibbonBandCollapseKind.getSortedKinds()) {
				int collapsedWidth=getPreferredWidth(collapseKind,availableHeight);
				if (collapsedWidth<=c.getWidth()+4) {
					bestFitCollapse=collapseKind;
					break;
				}
			}

			currentFitCollapse=bestFitCollapse;
			if (bestFitCollapse==RibbonBandCollapseKind.ICON) {
				collapsedButton.setVisible(true);
				int w=c.getWidth()-4;
				collapsedButton.setBounds((c.getWidth()-w)/2,2,w,(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET)+getBandTitleHeight()-9);
				if (collapsedButton.getPopupPanel()==null) {
					JRibbonBand popupBand=new JRibbonBand(ribbonBand.getTitle(),ribbonBand.getIcon(),ribbonBand.getExpandActionListener());
					popupBand.setControlPanel(ribbonBand.getControlPanel());
					Dimension size=new Dimension(getPreferredWidth(RibbonBandCollapseKind.NONE,availableHeight),
																			 (RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET)+getBandTitleHeight());
					JPopupPanel jpp=new JPopupPanel(popupBand,size);
					jpp.putClientProperty(RIBBON_TITLE_TEXT,ribbonBand.getTitle());
					collapsedButton.setPopupPanel(jpp);
					collapsedButton.setCommandButtonKind(JCommandButton.CommandButtonKind.POPUP_ONLY);
					ribbonBand.setControlPanel(null);
				}
				if (expandButton!=null) expandButton.setVisible(false);
				return;
			}

			if (collapsedButton.isVisible()) {
				JPopupPanel popupGallery=collapsedButton.getPopupPanel();
				if (popupGallery!=null) {
					JRibbonBand bandFromPopup=(JRibbonBand)collapsedButton.getPopupPanel().removeComponent();
					ribbonBand.setControlPanel(bandFromPopup.getControlPanel());
					collapsedButton.setPopupPanel(null);
				}
			}
			collapsedButton.setVisible(false);
			if (expandButton!=null) {
				expandButton.setVisible(true);
				expandButton.setLocation(c.getWidth()-3-expandButton.getWidth(),(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET)+((getBandTitleHeight()-expandButton.getHeight())/2)-6);
			}
			if (controlPanel!=null) {
				controlPanel.setVisible(true);
				controlPanel.setBounds(0,0,c.getWidth(),(RibbonConstants.BAND_CONTROL_PANEL_HEIGHT+RibbonConstants.BAND_OFFSET));
				controlPanel.doLayout();
			}
		}
	}
}
