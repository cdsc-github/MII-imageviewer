/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.plaf.metal.MetalTabbedPaneUI;

import javax.swing.text.View;

import imageviewer.ui.TabbedDataPanel;
import imageviewer.ui.graphics.EllipticalGradientPaint;

// =======================================================================
// Adapated from JGoodies. Using a custom renderer to paint a
// different graphical type of tab, and simplified code base to remove
// embedded tabs. Edges are all now straight, as opposed to slanted.
// Also added a different gradient paint mechanism.

public class TabbedPaneUI extends MetalTabbedPaneUI {

	private static final Color TAB_HIGHLIGHT_LIGHT_TOP=UIManager.getColor("TabbedPane.tabHighlightLightTop");
	private static final Color TAB_HIGHLIGHT_DARK_TOP=UIManager.getColor("TabbedPane.tabHighlightDarkTop");
	private static final Color TAB_HIGHLIGHT_LIGHT_BOTTOM=UIManager.getColor("TabbedPane.tabHighlightLightBottom");
	private static final Color TAB_HIGHLIGHT_DARK_BOTTOM=UIManager.getColor("TabbedPane.tabHighlightDarkBottom");
	private static final Color TAB_SELECT_HIGHLIGHT_LIGHT=UIManager.getColor("TabbedPane.selectedTabHighlightLight");
	private static final Color TAB_SELECT_HIGHLIGHT_DARK=UIManager.getColor("TabbedPane.selectedTabHighlightDark");
	private static final Color TAB_ROLLOVER_HIGHLIGHT_LIGHT=UIManager.getColor("TabbedPane.rolloverTabHighlightLight");
	private static final Color TAB_ROLLOVER_HIGHLIGHT_DARK=UIManager.getColor("TabbedPane.rolloverTabHighlightDark");

	private static final Icon CLOSE_TAB_ICON=UIManager.getIcon("TabbedPane.closeTabIcon");
	private static final Icon CLOSE_TAB_ICON_GRAY=UIManager.getIcon("TabbedPane.closeTabIconDisabled");
	private static final Icon ARROW_EAST_ICON=UIManager.getIcon("TabbedPane.forwardButtonRolloverIcon");
	private static final Icon ARROW_WEST_ICON=UIManager.getIcon("TabbedPane.backButtonRolloverIcon");
	private static final Icon ARROW_EAST_ICON_DARK=UIManager.getIcon("TabbedPane.forwardButtonIcon");
	private static final Icon ARROW_WEST_ICON_DARK=UIManager.getIcon("TabbedPane.backButtonIcon");

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f);

	protected static boolean isTabIconsEnabled=true;
	protected static final int CLOSE_BUTTON_SIZE=13;

	// =======================================================================
	
	Boolean noContentBorder=new Boolean(false);
	TabRenderer renderer=null;
	ScrollableTabSupport tabScroller=null;
	MouseAdapter ma=null;
    
	public static ComponentUI createUI(JComponent tabPane) {return new TabbedPaneUI();}

	public void installUI(JComponent c) {super.installUI(c); renderer=createRenderer(tabPane);}
	public void uninstallUI(JComponent c) {renderer=null; super.uninstallUI(c);}

	protected void installComponents() {

		if (scrollableTabLayoutEnabled()) {
			if (tabScroller==null) {
				tabScroller=new ScrollableTabSupport(tabPane.getTabPlacement());
				tabPane.add(tabScroller.viewport);
			}
		}
	}
    
	protected void uninstallComponents() {

		if (scrollableTabLayoutEnabled()) {
			tabPane.remove(tabScroller.viewport);
			tabPane.remove(tabScroller.scrollForwardButton);
			tabPane.remove(tabScroller.scrollBackwardButton);
			tabScroller=null;
		}
	}

	protected void installListeners() {

		super.installListeners();
		ma=new MouseAdapter() {
			public void mouseMoved(MouseEvent e) {tabPane.repaint();}
			public void mouseExited(MouseEvent e) {tabPane.repaint();}};
		tabPane.addMouseListener(ma);
		tabPane.addMouseMotionListener(ma);
	}

	protected void uninstallListeners() {tabPane.removeMouseListener(ma); tabPane.removeMouseMotionListener(ma); ma=null;}
    
	protected void installKeyboardActions() {
	
		super.installKeyboardActions();
		if (scrollableTabLayoutEnabled()) {
			Action forwardAction=new ScrollTabsForwardAction();
			Action backwardAction=new ScrollTabsBackwardAction();
			ActionMap am=SwingUtilities.getUIActionMap(tabPane);
			am.put("scrollTabsForwardAction",forwardAction);
			am.put("scrollTabsBackwardAction",backwardAction);
			tabScroller.scrollForwardButton.setAction(forwardAction);
			tabScroller.scrollBackwardButton.setAction(backwardAction);
		}
	}

	// =======================================================================
	
	private boolean hasNoContentBorder() {return Boolean.TRUE.equals(noContentBorder);}
	private boolean scrollableTabLayoutEnabled() {return tabPane.getLayout() instanceof TabbedPaneScrollLayout;}

	private TabRenderer createRenderer(JTabbedPane tabbedPane) {return TabRenderer.createRenderer(tabPane);}

	protected int calculateTabWidth(int tabPlacement, int tabIndex,	FontMetrics metrics) {

		return (((tabPane.getClientProperty("TABBED_CLOSE_BUTTONS")!=null) ? CLOSE_BUTTON_SIZE : 0)+super.calculateTabWidth(tabPlacement,tabIndex,metrics));
	}

	// =======================================================================

	protected PropertyChangeListener createPropertyChangeListener() {return new TabPropertyChangeHandler();}
	protected ChangeListener createChangeListener() {return new TabSelectionHandler();}
    
	private void tabPlacementChanged() {

		renderer=createRenderer(tabPane);
		if (scrollableTabLayoutEnabled()) tabScroller.createButtons();
		tabPane.revalidate();
		tabPane.repaint();
	}
	
	private void noContentBorderPropertyChanged(Boolean newValue) {
		noContentBorder=newValue;
		tabPane.repaint();
	}

	// =======================================================================

	public void paint(Graphics g, JComponent c) {

		int selectedIndex=tabPane.getSelectedIndex();
		int tabPlacement=tabPane.getTabPlacement();
		ensureCurrentLayout();
		if (!scrollableTabLayoutEnabled()) paintTabArea(g,tabPlacement,selectedIndex);
		paintContentBorder(g,tabPlacement,selectedIndex);
	}

	// =======================================================================
     
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {

		Rectangle tabRect=rects[tabIndex];
		int selectedIndex=tabPane.getSelectedIndex();
		boolean isSelected=selectedIndex==tabIndex;
		Shape cropShape=null, save=null;
		int cropLine=0, cropX=0, cropY=0;
		Graphics2D g2=(Graphics2D)g;

		if (scrollableTabLayoutEnabled()) {
			Rectangle viewRect=tabScroller.viewport.getViewRect();
			switch (tabPlacement) {

			  case LEFT:
			 case RIGHT: cropLine=viewRect.y+viewRect.height;
				           if ((tabRect.y<cropLine)&&(tabRect.y+tabRect.height>cropLine)) {
										 cropShape=createCroppedTabClip(tabPlacement,tabRect,cropLine);
										 cropX=tabRect.x;
										 cropY=cropLine-1;
									 }
									 break;
			   case TOP:
			case BOTTOM:
			    default: cropLine=viewRect.x+viewRect.width;
						       if ((tabRect.x<cropLine)&&(tabRect.x+tabRect.width>cropLine)) {
										 cropShape=createCroppedTabClip(tabPlacement,tabRect,cropLine);
										 cropX=cropLine-1;
										 cropY=tabRect.y;
									 }
			}
			if (cropShape!=null) {
				save=g2.getClip();
				g2.clip(cropShape);
			}
		}

		String title=tabPane.getTitleAt(tabIndex);
		paintTabBackground(g2,tabPlacement,tabIndex,tabRect.x,tabRect.y,tabRect.width,tabRect.height,isSelected);
		Font font=tabPane.getFont();
		FontMetrics metrics=g.getFontMetrics(font);
		Icon icon=getIconForTab(tabIndex);

		layoutLabel(tabPlacement,metrics,tabIndex,title,icon,tabRect,iconRect,textRect,isSelected);
		title=sun.swing.SwingUtilities2.clipStringIfNecessary(null,metrics,title,(cropX!=0) ? (cropX-tabRect.x-((tabPane.getClientProperty("TABBED_CLOSE_BUTTONS")!=null) ? 17 : 0)-10) : 
																													(tabRect.width-((tabPane.getClientProperty("TABBED_CLOSE_BUTTONS")!=null) ? 17 : 0)-10));
		textRect.x=tabRect.x+7;
		textRect.y-=1; // (isSelected) ? -1 : 0;
		Composite oldComposite=g2.getComposite();
		g2.setComposite(DEFAULT_AC);
		paintText(g2,tabPlacement,font,metrics,tabIndex,title,textRect,isSelected);
		g2.setComposite(oldComposite);
		paintIcon(g,tabPlacement,tabIndex,icon,iconRect,isSelected);
		if (tabPane.getClientProperty("TABBED_CLOSE_BUTTONS")!=null) paintCloseButton(g,tabPlacement,tabIndex,tabRect,cropX,isSelected);

		if (cropShape!=null) {
			paintCroppedTabEdge(g,tabPlacement,tabIndex,isSelected,cropX,cropY);
			g2.setClip(save);
		}
	}

	// =======================================================================

	private int[] xCropLen={0,0}; //1,1,0,0,1,1,2,2};
	private int[] yCropLen={0,12}; //3,3,6,6,9,9,12};

	private static final int CROP_SEGMENT=12;

	private Polygon createCroppedTabClip(int tabPlacement, Rectangle tabRect, int cropLine) {

		int rlen=0, start=0, end=0, ostart=0;

		switch (tabPlacement) {
		    case LEFT:
		   case RIGHT: rlen=tabRect.width;
				           start=tabRect.x;
									 end=tabRect.x+tabRect.width;
									 ostart=tabRect.y;
									 break;
		     case TOP:
		  case BOTTOM:
		      default: rlen=tabRect.height;
						       start=tabRect.y;
									 end=tabRect.y+tabRect.height;
									 ostart=tabRect.x;
		}
		int rcnt=rlen/CROP_SEGMENT;
		if ((rlen % CROP_SEGMENT)>0) rcnt++;
		int npts=2+(rcnt*8);
		int[] xp=new int[npts];
		int[] yp=new int[npts];
		int pcnt=0;

		xp[pcnt]=ostart;
		yp[pcnt++]=end;
		xp[pcnt]=ostart;
		yp[pcnt++]=start;
		for (int i=0; i<rcnt; i++) {
			for (int j=0; j<xCropLen.length; j++) {
				xp[pcnt]=cropLine-xCropLen[j];
				yp[pcnt]=start+(i*CROP_SEGMENT)+yCropLen[j];
				if (yp[pcnt]>=end) {
					yp[pcnt]=end;
					pcnt++;
					break;
				}
				pcnt++;
			}
		}
		if (tabPlacement==SwingConstants.TOP || tabPlacement==SwingConstants.BOTTOM) return new Polygon(xp,yp,pcnt);
		return new Polygon(yp,xp,pcnt);
	}

	private void paintCroppedTabEdge(Graphics g, int tabPlacement, int tabIndex, boolean isSelected, int x, int y) {

		switch (tabPlacement) {
		    case LEFT:
		   case RIGHT: int xx=x;
				           g.setColor(shadow.darker());
									 while (xx <= x+rects[tabIndex].width) {
										 for (int i=0; i<xCropLen.length; i+=2) g.drawLine(xx+yCropLen[i],y-xCropLen[i],xx+yCropLen[i+1]-1,y-xCropLen[i+1]);
										 xx+=CROP_SEGMENT;
									 }
									 break;
		     case TOP:
		  case BOTTOM:
		      default: int yy=y;
						       g.setColor(Color.darkGray); //shadow.darker());
									 while (yy <= y+rects[tabIndex].height) {
										 for (int i=0; i<xCropLen.length; i+=2) g.drawLine(x-xCropLen[i],yy+yCropLen[i],x-xCropLen[i+1],yy+yCropLen[i+1]-1);
										 yy+=CROP_SEGMENT;
									 }
		}
	}

	// =======================================================================

	private void paintCloseButton(Graphics g, int tabPlacement, int tabIndex, Rectangle r, int cropX, boolean isSelected) {

		if (tabPane instanceof TabbedDataPanel) {
			int x=(cropX==0) ? (r.x+r.width-17) : (cropX-17);
			if ((((TabbedDataPanel)tabPane).isMouseOverCloseButton()) && isSelected) 
				CLOSE_TAB_ICON.paintIcon(tabPane,g,x,r.y+4); 
			else 
				CLOSE_TAB_ICON_GRAY.paintIcon(tabPane,g,x,r.y+4);
		}
	}

	// =======================================================================

	private void ensureCurrentLayout() {

		// If tabPane doesn't have a peer yet, the validate() call will
		// silently fail. We handle that by forcing a layout if tabPane
		// is still invalid. See bug 4237677.

		if (!tabPane.isValid()) tabPane.validate();
		if (!tabPane.isValid()) {
			TabbedPaneLayout layout=(TabbedPaneLayout)tabPane.getLayout();
			layout.calculateLayoutInfo();          
		}
	}

	// =======================================================================
     
	public int tabForCoordinate(JTabbedPane pane, int x, int y) {

		ensureCurrentLayout();
		Point p=new Point(x,y);
		if (scrollableTabLayoutEnabled()) {
			translatePointToTabPanel(x,y,p);
			Rectangle viewRect=tabScroller.viewport.getViewRect();
			if (!viewRect.contains(p)) return -1;
		}
		int tabCount=tabPane.getTabCount();
		for (int i=0; i<tabCount; i++) {
			if (rects[i].contains(p.x,p.y)) return i;
		}
		return -1;
	}

	// =======================================================================

	protected Rectangle getTabBounds(int tabIndex, Rectangle dest) {

		dest.width=rects[tabIndex].width;
		dest.height=rects[tabIndex].height;
		if (scrollableTabLayoutEnabled()) { 
			Point vpp=tabScroller.viewport.getLocation();
			Point viewp=tabScroller.viewport.getViewPosition();
			dest.x=rects[tabIndex].x+vpp.x-viewp.x;
			dest.y=rects[tabIndex].y+vpp.y-viewp.y;
		} else {
			dest.x=rects[tabIndex].x;
			dest.y=rects[tabIndex].y;
		}
		return dest;
	}

	// =======================================================================
     
	private int getClosestTab(int x, int y) {

		int min=0;
		int tabCount=Math.min(rects.length,tabPane.getTabCount());
		int max=tabCount;
		int tabPlacement=tabPane.getTabPlacement();
		boolean useX=((tabPlacement==TOP)||(tabPlacement==BOTTOM));
		int want=(useX) ? x : y;

		while (min!=max) {
			int current=(max+min)/2;
			int minLoc=0, maxLoc=0;

			if (useX) {
				minLoc=rects[current].x;
				maxLoc=minLoc+rects[current].width;
			} else {
				minLoc=rects[current].y;
				maxLoc=minLoc+rects[current].height;
			}
			if (want<minLoc) {
				max=current;
				if (min==max) return Math.max(0,current-1);
			} else if (want>=maxLoc) {
				min=current;
				if (max-min<=1) return Math.max(current+1,tabCount-1);
			} else {
				return current;
			}
		}
		return min;
	}

	// =======================================================================
	
	private Point translatePointToTabPanel(int srcx, int srcy, Point dest) {

		Point vpp=tabScroller.viewport.getLocation();
		Point viewp=tabScroller.viewport.getViewPosition();
		dest.x=srcx-vpp.x+viewp.x;
		dest.y=srcy-vpp.y+viewp.y;
		return dest;
	}

	// =======================================================================
     
	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {

		int tabCount=tabPane.getTabCount();
		Rectangle iconRect=new Rectangle();
		Rectangle textRect=new Rectangle();
		Rectangle clipRect=g.getClipBounds();  

		for (int i=runCount-1; i>=0; i--) {
			int start=tabRuns[i];
			int next=tabRuns[(i==runCount-1) ? 0 : i+1];
			int end=(next!=0) ? next-1 : tabCount-1;

			for (int j=start; j<=end; j++) {
				if (j!=selectedIndex && rects[j].intersects(clipRect)) {
					paintTab(g,tabPlacement,rects,j,iconRect,textRect);
				}
			}
		}
		if (selectedIndex>=0 && rects[selectedIndex].intersects(clipRect)) paintTab(g,tabPlacement,rects,selectedIndex,iconRect,textRect);
	}

	// =======================================================================
    
	protected void layoutLabel(int tabPlacement, FontMetrics metrics, int tabIndex, String title, Icon icon, Rectangle tabRect,
														 Rectangle iconRect, Rectangle textRect, boolean isSelected) {

		textRect.x=textRect.y=iconRect.x=iconRect.y=0;
		View v=getTextViewForTab(tabIndex);
		if (v!=null) tabPane.putClientProperty("html",v);

		Rectangle calcRectangle=new Rectangle(tabRect);
		if (isSelected) {
			Insets calcInsets=getSelectedTabPadInsets(tabPlacement);
			calcRectangle.x+=calcInsets.left;
			calcRectangle.y+=calcInsets.top;
			calcRectangle.width-=calcInsets.left+calcInsets.right;
			calcRectangle.height-=calcInsets.bottom+calcInsets.top;
		}
		int xNudge=getTabLabelShiftX(tabPlacement,tabIndex,isSelected);
		int yNudge=getTabLabelShiftY(tabPlacement,tabIndex,isSelected);
		if ((tabPlacement==RIGHT || tabPlacement==LEFT) && icon!=null && title!=null && !title.equals("")) {
			SwingUtilities.layoutCompoundLabel(tabPane,metrics,title,icon,SwingConstants.CENTER,SwingConstants.LEFT,SwingConstants.CENTER,
																				 SwingConstants.TRAILING,calcRectangle,iconRect,textRect,textIconGap);
			xNudge+=4;
		} else {
			SwingUtilities.layoutCompoundLabel(tabPane,metrics,title,icon,SwingConstants.CENTER,SwingConstants.CENTER,SwingConstants.CENTER,
																				 SwingConstants.TRAILING,calcRectangle,iconRect,textRect,textIconGap);
			iconRect.y+=(calcRectangle.height % 2);
		}

		tabPane.putClientProperty("html",null);
		iconRect.x+=xNudge;
		iconRect.y+=yNudge;
		textRect.x+=xNudge;
		textRect.y+=yNudge;
	}

	// =======================================================================

	protected Icon getIconForTab(int tabIndex) {

		String title=tabPane.getTitleAt(tabIndex);
		boolean hasTitle=(title!=null) && (title.length()>0);
		return !isTabIconsEnabled  && hasTitle ? null	: super.getIconForTab(tabIndex);
	}

	// =======================================================================

	protected LayoutManager createLayoutManager() {

		if (tabPane.getTabLayoutPolicy()==JTabbedPane.SCROLL_TAB_LAYOUT) return new TabbedPaneScrollLayout();
		return new TabbedPaneLayout();
	}

	// =======================================================================

	protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {

		int width =tabPane.getWidth();
		int height=tabPane.getHeight();
		Insets insets=tabPane.getInsets();
		int x=insets.left;
		int y=insets.top;
		int w=width-insets.right-insets.left;
		int h=height-insets.top-insets.bottom;

		switch (tabPlacement) {
		  case LEFT: x+=calculateTabAreaWidth(tabPlacement,runCount,maxTabWidth);
				         w-=(x-insets.left);
								 break;
		 case RIGHT: w-=calculateTabAreaWidth(tabPlacement,runCount,maxTabWidth);
			           break;
		case BOTTOM: h-=calculateTabAreaHeight(tabPlacement,runCount,maxTabHeight);
			           break;
		   case TOP:
		    default: y+=calculateTabAreaHeight(tabPlacement,runCount,maxTabHeight);
					       h-=(y-insets.top);
		}

		g.setColor(selectColor==null ? tabPane.getBackground() : selectColor);
		g.fillRect(x,y,w,h);
		Rectangle selRect=(selectedIndex<0) ? null : getTabBounds(selectedIndex,calcRect);
		boolean drawBroken=selectedIndex>=0 && isTabInFirstRun(selectedIndex);
		boolean isContentBorderPainted=!hasNoContentBorder();
		renderer.paintContentBorderTopEdge(g,x,y,w,h,drawBroken,selRect,isContentBorderPainted);
		renderer.paintContentBorderLeftEdge(g,x,y,w,h,drawBroken,selRect,isContentBorderPainted);
		renderer.paintContentBorderBottomEdge(g,x,y,w,h,drawBroken,selRect,isContentBorderPainted);
		renderer.paintContentBorderRightEdge(g,x,y,w,h,drawBroken,selRect,isContentBorderPainted);
	}

	// =======================================================================

	protected Insets getContentBorderInsets(int tabPlacement) {return renderer.getContentBorderInsets(super.getContentBorderInsets(tabPlacement));}
	protected Insets getTabAreaInsets(int tabPlacement) {return renderer.getTabAreaInsets(super.getTabAreaInsets(tabPlacement));}
	protected Insets getTabInsets(int tabPlacement,int tabIndex) {return renderer.getTabInsets(tabIndex,tabInsets);}
	protected Insets getSelectedTabPadInsets(int tabPlacement) {return renderer.getSelectedTabPadInsets();}

	protected int getTabLabelShiftX(int tabPlacement,int tabIndex,boolean isSelected) {return renderer.getTabLabelShiftX(tabIndex,isSelected);}
	protected int getTabLabelShiftY(int tabPlacement,int tabIndex,boolean isSelected) {return renderer.getTabLabelShiftY(tabIndex,isSelected);}
	protected int getTabRunOverlay(int tabPlacement) {return renderer.getTabRunOverlay(tabRunOverlay);}
	protected int getTabRunIndent(int tabPlacement,int run) {return renderer.getTabRunIndent(run);}

	protected boolean shouldPadTabRun(int tabPlacement,int run) {return renderer.shouldPadTabRun(run,super.shouldPadTabRun(tabPlacement,run));}
	protected boolean shouldRotateTabRuns(int tabPlacement) {return false;}
	protected boolean isTabInFirstRun(int tabIndex) {return getRunForTab(tabPane.getTabCount(),tabIndex)==0;}

	protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
		renderer.paintTabBackground(g,tabIndex,x,y,w,h,isSelected);
	}

	// =======================================================================

	private class TabSelectionHandler implements ChangeListener {
        
		private Rectangle rect=new Rectangle();
        
		public void stateChanged(ChangeEvent e) {

			JTabbedPane tabPane=(JTabbedPane)e.getSource();
			tabPane.revalidate();
			tabPane.repaint();
			if (tabPane.getTabLayoutPolicy()==JTabbedPane.SCROLL_TAB_LAYOUT) {
				int index=tabPane.getSelectedIndex();
				if (index<rects.length && index!=-1) {
					rect.setBounds(rects[index]);
					Point viewPosition=tabScroller.viewport.getViewPosition();
					if (rect.x<viewPosition.x) {
						rect.x-=renderer.getTabsOverlay();
					} else {
						rect.x+=renderer.getTabsOverlay();
					}
					tabScroller.tabPanel.scrollRectToVisible(rect);
				}
			}
		}
	}

	// =======================================================================
    
	private class TabPropertyChangeHandler extends BasicTabbedPaneUI.PropertyChangeHandler {

		public void propertyChange(PropertyChangeEvent e) {

			String pName=e.getPropertyName();
			if (null==pName) return;            
			super.propertyChange(e);
			if (pName.equals("tabPlacement")) {tabPlacementChanged();	return;}
		}
	}

	// =======================================================================
    
	private class TabbedPaneLayout extends BasicTabbedPaneUI.TabbedPaneLayout implements LayoutManager {

		protected void calculateTabRects(int tabPlacement, int tabCount) {

			FontMetrics metrics=getFontMetrics();
			Dimension size=tabPane.getSize();
			Insets insets=tabPane.getInsets();
			Insets theTabAreaInsets=getTabAreaInsets(tabPlacement);
			int fontHeight=metrics.getHeight();
			int selectedIndex=tabPane.getSelectedIndex();
			int theTabRunOverlay;
			int i=0, j=0, x=0, y=0, returnAt=0;
			boolean verticalTabRuns=(tabPlacement==LEFT || tabPlacement==RIGHT);
			boolean leftToRight=tabPane.getComponentOrientation().isLeftToRight();

			// Calculate bounds within which a tab run must fit

			switch (tabPlacement) {

			  case LEFT: maxTabWidth=calculateMaxTabWidth(tabPlacement);
					         x=insets.left+theTabAreaInsets.left;
									 y=insets.top+theTabAreaInsets.top;
									 returnAt=size.height-(insets.bottom+theTabAreaInsets.bottom);
									 break;
			 case RIGHT: maxTabWidth=calculateMaxTabWidth(tabPlacement);
				           x=size.width-insets.right-theTabAreaInsets.right-maxTabWidth;
									 y=insets.top+theTabAreaInsets.top;
									 returnAt=size.height-(insets.bottom+theTabAreaInsets.bottom);
									 break;
			case BOTTOM: maxTabHeight=calculateMaxTabHeight(tabPlacement);
				           x=insets.left+theTabAreaInsets.left;
									 y=size.height-insets.bottom-theTabAreaInsets.bottom-maxTabHeight;
									 returnAt=size.width-(insets.right+theTabAreaInsets.right);
									 break;
			   case TOP:
			    default: maxTabHeight=calculateMaxTabHeight(tabPlacement);
						       x=insets.left+theTabAreaInsets.left;
									 y=insets.top +theTabAreaInsets.top;
									 returnAt=size.width-(insets.right+theTabAreaInsets.right);
									 break;
			}

			theTabRunOverlay=getTabRunOverlay(tabPlacement);
			runCount=0;
			selectedRun=-1;

			// Keeps track of where we are in the current run. This helps
			// not to rely on fragile positioning informaion to find out
			// wheter the active Tab is the first in run.  Make a copy of
			// returnAt for the current run and modify that so returnAt may
			// still be used later on

			int tabInRun=-1;
			int runReturnAt=returnAt;
			if (tabCount==0) return;

			// Run through tabs and partition them into runs

			Rectangle rect;
			for (i=0; i<tabCount; i++) {
				rect=rects[i];
				tabInRun++;
				if (!verticalTabRuns) {
					if (i>0) {
						rect.x=rects[i-1].x+rects[i-1].width;
					} else {
						tabRuns[0]=0;
						runCount=1;
						maxTabWidth=0;
						rect.x=x;
					}
					rect.width=calculateTabWidth(tabPlacement,i,metrics);
					maxTabWidth=Math.max(maxTabWidth,rect.width);
					if (tabInRun!=0 && rect.x+rect.width>runReturnAt) {
						if (runCount>tabRuns.length-1) expandTabRunsArray();
						tabInRun=0;
						tabRuns[runCount]=i;
						runCount++;
						rect.x=x;
						runReturnAt=runReturnAt-2*getTabRunIndent(tabPlacement,runCount);
					}
					rect.y=y;
					rect.height=maxTabHeight;
				} else {
					if (i>0) {
						rect.y=rects[i-1].y+rects[i-1].height;
					} else {
						tabRuns[0]=0;
						runCount=1;
						maxTabHeight=0;
						rect.y=y;
					}
					rect.height=calculateTabHeight(tabPlacement,i,fontHeight);
					maxTabHeight=Math.max(maxTabHeight,rect.height);
					if (tabInRun!=0 && rect.y+rect.height>runReturnAt) {
						if (runCount > tabRuns.length-1) expandTabRunsArray();
						tabRuns[runCount]=i;
						runCount++;
						rect.y=y;
						tabInRun=0;
						runReturnAt-=2*getTabRunIndent(tabPlacement,runCount);
					}
					rect.x=x;
					rect.width=maxTabWidth;
				}
				if (i==selectedIndex) selectedRun=runCount-1;
			}

			if (runCount>1) {
				if (shouldRotateTabRuns(tabPlacement)) rotateTabRuns(tabPlacement,selectedRun);
			}

			// Step through runs from back to front to calculate
			// tab y locations and to pad runs appropriately

			for (i=runCount-1; i>=0; i--) {

				int start=tabRuns[i];
				int next=tabRuns[i==(runCount-1) ? 0 : i+1];
				int end=(next!=0 ? next-1 : tabCount-1);
				int indent=getTabRunIndent(tabPlacement,i);
				if (!verticalTabRuns) {
					for (j=start; j<=end; j++) {
						rect=rects[j];
						rect.y=y;
						rect.x += indent;
					}
					if (shouldPadTabRun(tabPlacement,i)) padTabRun(tabPlacement,start,end,returnAt-2*indent);
					if (tabPlacement==BOTTOM) {
						y-=(maxTabHeight-theTabRunOverlay);
					} else {
						y+=(maxTabHeight-theTabRunOverlay);
					}
				} else {
					for (j=start; j<=end; j++) {
						rect=rects[j];
						rect.x=x;
						rect.y+=indent;
					}
					if (shouldPadTabRun(tabPlacement,i)) padTabRun(tabPlacement,start,end,returnAt-2*indent);
					if (tabPlacement==RIGHT) {
						x-=(maxTabWidth-theTabRunOverlay);
					} else {
						x+=(maxTabWidth-theTabRunOverlay);
					}
				}
			}

			// Pad the selected tab so that it appears raised in front. If
			// right to left and tab placement on the top or the bottom,flip
			// x positions and adjust by widths

			// padSelectedTab(tabPlacement,selectedIndex);
			if (!leftToRight && !verticalTabRuns) {
				int rightMargin=size.width-(insets.right+theTabAreaInsets.right);
				for (i=0; i<tabCount; i++) rects[i].x=rightMargin-rects[i].x-rects[i].width+renderer.getTabsOverlay();
			}
		}
	}

	// =======================================================================

	private boolean requestFocusForVisibleComponent() {

		Component visibleComponent=getVisibleComponent();
		if (visibleComponent.isFocusable()) {
			visibleComponent.requestFocus();
			return true;
		} else if (visibleComponent instanceof JComponent) {
			if (((JComponent) visibleComponent).requestDefaultFocus()) return true;
		}
		return false;
	}

	// =======================================================================
	
	private static class ScrollTabsForwardAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

			JTabbedPane pane=null;
			Object src=e.getSource();
			if (src instanceof JTabbedPane) {
				pane=(JTabbedPane)src;
			} else if (src instanceof ArrowButton) {
				pane=(JTabbedPane)((ArrowButton)src).getParent();
			} else {
				return;
			}
			TabbedPaneUI ui=(TabbedPaneUI)pane.getUI();
			if (ui.scrollableTabLayoutEnabled()) ui.tabScroller.scrollForward(pane.getTabPlacement());
		}
	}

	// =======================================================================

	private static class ScrollTabsBackwardAction extends AbstractAction {

		public void actionPerformed(ActionEvent e) {

			JTabbedPane pane=null;
			Object src=e.getSource();
			if (src instanceof JTabbedPane) {
				pane=(JTabbedPane)src;
			} else if (src instanceof ArrowButton) {
				pane=(JTabbedPane)((ArrowButton)src).getParent();
			} else {
				return; 
			}
			TabbedPaneUI ui=(TabbedPaneUI) pane.getUI();
			if (ui.scrollableTabLayoutEnabled()) ui.tabScroller.scrollBackward(pane.getTabPlacement());
		}
	}

	// =======================================================================

	private class TabbedPaneScrollLayout extends TabbedPaneLayout {

		protected int preferredTabAreaHeight(int tabPlacement, int width) {return calculateMaxTabHeight(tabPlacement);}
		protected int preferredTabAreaWidth(int tabPlacement, int height) {return calculateMaxTabWidth(tabPlacement);}

		public void layoutContainer(Container parent) {

			int tabPlacement=tabPane.getTabPlacement();
			int tabCount=tabPane.getTabCount();
			Insets insets=tabPane.getInsets();
			int selectedIndex=tabPane.getSelectedIndex();
			Component visibleComponent=getVisibleComponent();

			calculateLayoutInfo();
			if (selectedIndex<0) {
				if (visibleComponent!=null) setVisibleComponent(null);
			} else {
				Component selectedComponent=tabPane.getComponentAt(selectedIndex);
				boolean shouldChangeFocus=false;

				// In order to allow programs to use a single component as the
				// display for multiple tabs, we will not change the visible
				// compnent if the currently selected tab has a null
				// component.  This is a bit dicey, as we don't explicitly
				// state we support this in the spec, but since programs are
				// now depending on this, we're making it work.

				if (selectedComponent!=null) {
					if (selectedComponent!=visibleComponent && visibleComponent!=null) {
						if (SwingUtilities.findFocusOwner(visibleComponent)!=null) shouldChangeFocus=true;
					} 
					setVisibleComponent(selectedComponent);
				}

				int tx=0, ty=0, tw=0, th=0;
				int cx=0, cy=0, cw=0, ch=0;
				Insets contentInsets=getContentBorderInsets(tabPlacement);
				Rectangle bounds=tabPane.getBounds();
				int numChildren=tabPane.getComponentCount();

				if (numChildren>0) {
					switch (tabPlacement) {

					  case LEFT: tw=calculateTabAreaWidth(tabPlacement,runCount,maxTabWidth);
							         th=bounds.height-insets.top-insets.bottom;
											 tx=insets.left;
											 ty=insets.top;
											 cx=tx+tw+contentInsets.left;
											 cy=ty+contentInsets.top;
											 cw=bounds.width-insets.left-insets.right-tw-contentInsets.left-contentInsets.right;
											 ch=bounds.height-insets.top-insets.bottom-contentInsets.top-contentInsets.bottom;
											 break;
					 case RIGHT: tw=calculateTabAreaWidth(tabPlacement,runCount,maxTabWidth);
						           th=bounds.height-insets.top-insets.bottom;
											 tx=bounds.width-insets.right-tw;
											 ty=insets.top;
											 cx=insets.left+contentInsets.left;
											 cy=insets.top+contentInsets.top;
											 cw=bounds.width-insets.left-insets.right-tw-contentInsets.left-contentInsets.right;
											 ch=bounds.height-insets.top-insets.bottom-contentInsets.top-contentInsets.bottom;
											 break;
					case BOTTOM: tw=bounds.width-insets.left-insets.right;
						           th=calculateTabAreaHeight(tabPlacement,runCount,maxTabHeight);
											 tx=insets.left;
											 ty=bounds.height-insets.bottom-th;
											 cx=insets.left+contentInsets.left;
											 cy=insets.top+contentInsets.top;
											 cw=bounds.width-insets.left-insets.right-contentInsets.left-contentInsets.right;
											 ch=bounds.height-insets.top-insets.bottom-th-contentInsets.top-contentInsets.bottom;
											 break;
					   case TOP:
					    default: tw=bounds.width-insets.left-insets.right;
								       th=calculateTabAreaHeight(tabPlacement,runCount,maxTabHeight);
											 tx=insets.left;
											 ty=insets.top;
											 cx=tx+contentInsets.left;
											 cy=ty+th+contentInsets.top;
											 cw=bounds.width-insets.left-insets.right-contentInsets.left-contentInsets.right;
											 ch=bounds.height-insets.top-insets.bottom-th-contentInsets.top-contentInsets.bottom;
					}

					for (int i=0; i<numChildren; i++) {
						Component child=tabPane.getComponent(i);
						if (tabScroller!=null && child==tabScroller.viewport) {
							JViewport viewport=(JViewport)child;
							Rectangle viewRect=viewport.getViewRect();
							int vw=tw;
							int vh=th;
							Dimension butSize=tabScroller.scrollForwardButton.getPreferredSize();

							switch (tabPlacement) {

							  case LEFT:
							 case RIGHT: int totalTabHeight=rects[tabCount-1].y+rects[tabCount-1].height;
								           if (totalTabHeight>th) {
														 vh=(th>2*butSize.height) ? th-2*butSize.height : 0;
														 if (totalTabHeight-viewRect.y<=vh) {
															 vh=totalTabHeight-viewRect.y;
														 }
													 }
													 break;
							case BOTTOM:
							   case TOP:
							    default: int totalTabWidth=rects[tabCount-1].x+rects[tabCount-1].width+renderer.getTabsOverlay();
								           if (totalTabWidth>tw) {
														 vw=(tw>2*butSize.width) ? tw-2*butSize.width : 0;
														 if (totalTabWidth-viewRect.x<=vw) {
															 vw=totalTabWidth-viewRect.x;
														 }
													 }
							} 				
							child.setBounds(tx,ty,vw,vh);                      
                   
						} else if (tabScroller!=null && (child==tabScroller.scrollForwardButton || child==tabScroller.scrollBackwardButton)) {

							Component scrollbutton=child;
							Dimension bsize=scrollbutton.getPreferredSize();
							int bx=0, by=0;
							int bw=bsize.width;
							int bh=bsize.height;
							boolean visible=false;
                
							switch (tabPlacement) {
							  case LEFT:
							 case RIGHT: int totalTabHeight=rects[tabCount-1].y+rects[tabCount-1].height;
								           if (totalTabHeight>th) {
														 visible=true;
														 bx=(tabPlacement==LEFT ? tx+tw-bsize.width : tx);
														 by=(child==tabScroller.scrollForwardButton) ? bounds.height-insets.bottom-bsize.height	: bounds.height-insets.bottom-2*bsize.height;
													 }
													 break;
							case BOTTOM:
							   case TOP:
							    default: int totalTabWidth=rects[tabCount-1].x+rects[tabCount-1].width;
										       if (totalTabWidth>tw) {
														 visible=true;
														 bx=(child==tabScroller.scrollForwardButton) ? bounds.width-insets.left-bsize.width : bounds.width-insets.left-2*bsize.width;
														 by=(tabPlacement==TOP ? ty+th-bsize.height : ty);
													 }
							}
							child.setVisible(visible);
							if (visible) child.setBounds(bx,by,bw,bh);

						} else {
							child.setBounds(cx,cy,cw,ch);
						}
					}
					if (shouldChangeFocus) {
						if (!requestFocusForVisibleComponent()) {
							tabPane.requestFocus();
						}
					}
				}
			}
		}

		protected void calculateTabRects(int tabPlacement, int tabCount) {

			FontMetrics metrics=getFontMetrics();
			Dimension size=tabPane.getSize();
			Insets insets=tabPane.getInsets(); 
			Insets tabAreaInsets=getTabAreaInsets(tabPlacement);
			int fontHeight=metrics.getHeight();
			int selectedIndex=tabPane.getSelectedIndex();
			boolean verticalTabRuns=(tabPlacement==LEFT || tabPlacement==RIGHT);
			boolean leftToRight=tabPane.getComponentOrientation().isLeftToRight();
			int x=tabAreaInsets.left;
			int y=tabAreaInsets.top;
			int totalWidth=0, totalHeight=0;
			int i=0, j=0;

			switch (tabPlacement) {
			  case LEFT: case RIGHT: maxTabWidth=calculateMaxTabWidth(tabPlacement); break;
			  case BOTTOM: case TOP: default: maxTabHeight=calculateMaxTabHeight(tabPlacement);
			}

			runCount=0;
			selectedRun=-1;
			if (tabCount==0) return;
			selectedRun=0;
			runCount=1;

			// Run through tabs and lay them out in a single run

			for (i=0; i<tabCount; i++) {
				Rectangle rect=rects[i];
				if (!verticalTabRuns) {
					if (i>0) {
						rect.x=rects[i-1].x+rects[i-1].width;
					} else {
						tabRuns[0]=0;
						maxTabWidth=0;
						totalHeight+=maxTabHeight;
						rect.x=x;
					}
					rect.width=calculateTabWidth(tabPlacement,i,metrics);
					totalWidth=rect.x+rect.width+renderer.getTabsOverlay();
					maxTabWidth=Math.max(maxTabWidth,rect.width);
					rect.y=y;
					rect.height=maxTabHeight;
				} else {
					if (i>0) {
						rect.y=rects[i-1].y+rects[i-1].height;
					} else {
						tabRuns[0]=0;
						maxTabHeight=0;
						totalWidth=maxTabWidth;
						rect.y=y;
					}
					rect.height=calculateTabHeight(tabPlacement,i,fontHeight);
					totalHeight=rect.y+rect.height;
					maxTabHeight=Math.max(maxTabHeight,rect.height);
					rect.x=x;
					rect.width=maxTabWidth;
				}            
			}

			// Handle padding for the selected tab...

			if (selectedIndex>=0) {
				Rectangle selRect=rects[selectedIndex];
				Insets padInsets=getSelectedTabPadInsets(tabPlacement);
				selRect.x-=padInsets.left;            
				selRect.width+=(padInsets.left+padInsets.right);
				selRect.y-=padInsets.top;
				selRect.height+=(padInsets.top+padInsets.bottom);
			}

			if (!leftToRight && !verticalTabRuns) {
				int rightMargin=size.width-(insets.right+tabAreaInsets.right);
				for (i=0; i<tabCount; i++) rects[i].x=rightMargin-rects[i].x-rects[i].width;
			}
			tabScroller.tabPanel.setPreferredSize(new Dimension(totalWidth,totalHeight));
		}
	}

	// =======================================================================

	private class ScrollableTabSupport implements ActionListener, ChangeListener {

		public ScrollableTabViewport viewport=null;
		public ScrollableTabPanel tabPanel=null;
		public JButton scrollForwardButton=null, scrollBackwardButton=null;
		public int leadingTabIndex=0;

		Point tabViewPosition=new Point(0,0);

		public ScrollableTabSupport(int tabPlacement) {

			viewport=new ScrollableTabViewport();
			tabPanel=new ScrollableTabPanel();
			viewport.setView(tabPanel);
			viewport.addChangeListener(this);
			createButtons();
		}

		private void createButtons() {

			if (scrollForwardButton!=null) {
				tabPane.remove(scrollForwardButton);
				scrollForwardButton.removeActionListener(this);
				tabPane.remove(scrollBackwardButton);
				scrollBackwardButton.removeActionListener(this);
			}
			int tabPlacement=tabPane.getTabPlacement();
			int width=23; 
			int height=20;
			if (tabPlacement==TOP || tabPlacement==BOTTOM) {
				scrollForwardButton=new ArrowButton(EAST,width,height);
				scrollBackwardButton=new ArrowButton(WEST,width,height);
			} else {
				scrollForwardButton=new ArrowButton(SOUTH,width,height);
				scrollBackwardButton=new ArrowButton(NORTH,width,height);
			}
			scrollForwardButton.addActionListener(this);
			scrollBackwardButton.addActionListener(this);
			tabPane.add(scrollForwardButton);
			tabPane.add(scrollBackwardButton);
		}

		public void scrollForward(int tabPlacement) {

			Dimension viewSize=viewport.getViewSize();
			Rectangle viewRect=viewport.getViewRect();
			if (tabPlacement==TOP || tabPlacement==BOTTOM) {
				if (viewRect.width>=viewSize.width-viewRect.x) return;
			} else { 
				if (viewRect.height>=viewSize.height-viewRect.y) return;
			}
			setLeadingTabIndex(tabPlacement,leadingTabIndex+1);
		}

		public void scrollBackward(int tabPlacement) {

			if (leadingTabIndex==0) return;
			setLeadingTabIndex(tabPlacement,leadingTabIndex-1);
		}

		public void setLeadingTabIndex(int tabPlacement, int index) {

			leadingTabIndex=index;
			Dimension viewSize=viewport.getViewSize();
			Rectangle viewRect=viewport.getViewRect();
			
			switch (tabPlacement) {

			   case TOP:
			case BOTTOM: tabViewPosition.x=leadingTabIndex==0 ? 0	: rects[leadingTabIndex].x-renderer.getTabsOverlay();  
				           if ((viewSize.width-tabViewPosition.x)<viewRect.width) {
										 Dimension extentSize=new Dimension(viewSize.width-tabViewPosition.x,viewRect.height);
										 viewport.setExtentSize(extentSize);
									 }
									 break;
			  case LEFT:
			 case RIGHT: tabViewPosition.y=leadingTabIndex==0 ? 0 : rects[leadingTabIndex].y;
				           if ((viewSize.height-tabViewPosition.y)<viewRect.height) {
										 Dimension extentSize=new Dimension(viewRect.width,viewSize.height-tabViewPosition.y); 
										 viewport.setExtentSize(extentSize);
									 }
			}
			viewport.setViewPosition(tabViewPosition);
		}

		public void stateChanged(ChangeEvent e) {

			JViewport viewport=(JViewport)e.getSource();
			int tabPlacement=tabPane.getTabPlacement();
			int tabCount=tabPane.getTabCount();
			Rectangle vpRect=viewport.getBounds();
			Dimension viewSize=viewport.getViewSize();
			Rectangle viewRect=viewport.getViewRect();

			leadingTabIndex=getClosestTab(viewRect.x,viewRect.y);
			if (leadingTabIndex+1<tabCount) {
				switch (tabPlacement) {
				  case TOP: case BOTTOM: if (rects[leadingTabIndex].x<viewRect.x) leadingTabIndex++; break;
				  case LEFT: case RIGHT: if (rects[leadingTabIndex].y<viewRect.y) leadingTabIndex++; break;
				}
			}

			Insets contentInsets=getContentBorderInsets(tabPlacement);
			switch (tabPlacement) {
			  case LEFT: tabPane.repaint(vpRect.x+vpRect.width,vpRect.y,contentInsets.left,vpRect.height);
					         scrollBackwardButton.setEnabled(viewRect.y>0 && leadingTabIndex>0);
									 scrollForwardButton.setEnabled(leadingTabIndex<tabCount-1 && viewSize.height-viewRect.y>viewRect.height);
									 break;
			 case RIGHT: tabPane.repaint(vpRect.x-contentInsets.right,vpRect.y,contentInsets.right,vpRect.height);
				           scrollBackwardButton.setEnabled(viewRect.y>0 && leadingTabIndex>0);
									 scrollForwardButton.setEnabled(leadingTabIndex<tabCount-1 && viewSize.height-viewRect.y>viewRect.height);
									 break;
			case BOTTOM: tabPane.repaint(vpRect.x,vpRect.y-contentInsets.bottom,vpRect.width,contentInsets.bottom);
				           scrollBackwardButton.setEnabled(viewRect.x>0 && leadingTabIndex>0);
									 scrollForwardButton.setEnabled(leadingTabIndex<tabCount-1 && viewSize.width-viewRect.x>viewRect.width);
									 break;
			   case TOP:
			    default: tabPane.repaint(vpRect.x,vpRect.y+vpRect.height,vpRect.width,contentInsets.top);
						       scrollBackwardButton.setEnabled(viewRect.x>0 && leadingTabIndex>0);
									 scrollForwardButton.setEnabled(leadingTabIndex<tabCount-1 && viewSize.width-viewRect.x>viewRect.width);
			}
		}

		public void actionPerformed(ActionEvent e) {

			ActionMap map=tabPane.getActionMap();
			if (map!=null) {
				String actionKey;
				actionKey=(e.getSource()==scrollForwardButton) ? "scrollTabsForwardAction" : "scrollTabsBackwardAction";
				Action action=map.get(actionKey);
				if (action!=null && action.isEnabled()) {
					action.actionPerformed(new ActionEvent(tabPane,ActionEvent.ACTION_PERFORMED,null,e.getWhen(),e.getModifiers()));
				}
			}
		}
	}

	// =======================================================================

	private class ScrollableTabViewport extends JViewport implements UIResource {

		public ScrollableTabViewport() {

			super();
			setName("TabbedPane.scrollableViewport");
			setScrollMode(SIMPLE_SCROLL_MODE);
			setOpaque(tabPane.isOpaque());
			Color bgColor=UIManager.getColor("TabbedPane.tabAreaBackground");
			if (bgColor==null) bgColor=tabPane.getBackground();
			setBackground(bgColor);
		}
	}

	// =======================================================================

	private class ScrollableTabPanel extends JPanel implements UIResource {

		public ScrollableTabPanel() {

			super(null);
			setOpaque(tabPane.isOpaque());
			Color bgColor=UIManager.getColor("TabbedPane.tabAreaBackground");
			if (bgColor==null) bgColor=tabPane.getBackground();
			setBackground(bgColor);
		}

		public void paintComponent(Graphics g) {

			super.paintComponent(g);
			TabbedPaneUI.this.paintTabArea(g,tabPane.getTabPlacement(),tabPane.getSelectedIndex());
		}
	}

	// =======================================================================

	private static class ArrowButton extends JButton implements UIResource {

		private final int buttonWidth, buttonHeight, direction;
		boolean mouseIsOver=false;

		public ArrowButton(int direction, int buttonWidth, int buttonHeight) {

			super();
			this.direction=direction; 
			this.buttonWidth=buttonWidth; 
			this.buttonHeight=buttonHeight;
			setOpaque(false);
			setRequestFocusEnabled(false);  
			setRolloverEnabled(true); 
			setBorderPainted(false);
			setSelected(false);
		}
		
		protected void paintComponent(Graphics g) {

			if (getModel().isRollover()) {
				Icon ii=(direction==WEST) ? ARROW_WEST_ICON : ARROW_EAST_ICON;	
				ii.paintIcon(ArrowButton.this,g,0,0);
			} else {
				Icon ii=(direction==WEST) ? ARROW_WEST_ICON_DARK : ARROW_EAST_ICON_DARK;	
				ii.paintIcon(ArrowButton.this,g,0,0);
			}
		}

		public Dimension getPreferredSize() {return new Dimension(buttonWidth,buttonHeight);}
		public Dimension getMinimumSize() {return getPreferredSize();}
		public Dimension getMaximumSize() {return new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE);}
	}

	// =======================================================================

	private abstract static class TabRenderer {

		protected static final Insets EMPTY_INSETS=new Insets(0,0,0,0);
		protected static final Insets NORTH_INSETS=new Insets(1,0,0,0);
		protected static final Insets WEST_INSETS=new Insets(0,1,0,0);
		protected static final Insets SOUTH_INSETS=new Insets(0,0,1,0);
		protected static final Insets EAST_INSETS=new Insets(0,0,0,1);

		protected final JTabbedPane tabPane;
		protected final int tabPlacement;

		protected Color shadowColor=null, darkShadow=null, selectColor=null, selectLight=null, selectHighlight=null;
		protected Color lightHighlight=null, focus=null;

		private TabRenderer(JTabbedPane tabPane) {initColors();	this.tabPane=tabPane;	this.tabPlacement=tabPane.getTabPlacement();}

		private static TabRenderer createRenderer(JTabbedPane tabPane) {

			switch (tabPane.getTabPlacement()) {
			     case SwingConstants.TOP: return new TopRenderer(tabPane);
			  case SwingConstants.BOTTOM: return new BottomRenderer(tabPane);
			    case SwingConstants.LEFT:	return new LeftRenderer(tabPane);
			   case SwingConstants.RIGHT:	return new RightRenderer(tabPane);
			                     default: return new TopRenderer(tabPane);
			}
		}

		private void initColors() {

			shadowColor=UIManager.getColor("TabbedPane.shadow");
			darkShadow=UIManager.getColor("TabbedPane.darkShadow");
			selectColor=UIManager.getColor("TabbedPane.selected");
			focus=UIManager.getColor("TabbedPane.focus");
			selectHighlight=UIManager.getColor("TabbedPane.selectHighlight");
			lightHighlight=UIManager.getColor("TabbedPane.highlight");
			selectLight=new Color((2*selectColor.getRed()+selectHighlight.getRed())/3,
														(2*selectColor.getGreen()+selectHighlight.getGreen())/3,
														(2*selectColor.getBlue()+selectHighlight.getBlue())/3);
		}

		protected boolean isFirstDisplayedTab(int tabIndex, int position, int paneBorder) {return tabIndex==0;}
		protected boolean shouldPadTabRun(int run, boolean aPriori) {return aPriori;}

		protected Insets getTabAreaInsets(Insets defaultInsets) {return defaultInsets;}
		protected Insets getContentBorderInsets(Insets defaultInsets) {return defaultInsets;}
		protected Insets getSelectedTabPadInsets() {return EMPTY_INSETS;}

		protected int getTabLabelShiftX(int tabIndex,boolean isSelected) {return 0;}
		protected int getTabLabelShiftY(int tabIndex,boolean isSelected) {return 0;}
		protected int getTabRunOverlay(int tabRunOverlay) {return tabRunOverlay;}
		protected int getTabRunIndent(int run) {return 0;}
		protected int getTabsOverlay() {return 0;}

		protected abstract Insets getTabInsets(int tabIndex, Insets tabInsets);

		protected abstract void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected);

		protected void paintContentBorderTopEdge(Graphics g, int x, int y, int w, int h, boolean drawBroken, Rectangle selRect, boolean isContentBorderPainted) {

			if (isContentBorderPainted) {
				g.setColor(selectHighlight);
				g.fillRect(x,y,w-1,1);
			}
		}

		protected void paintContentBorderBottomEdge(Graphics g, int x, int y, int w, int h, boolean drawBroken, Rectangle selRect, boolean isContentBorderPainted) {

			if (isContentBorderPainted) {
				g.setColor(darkShadow);
				g.fillRect(x,y+h-1,w-1,1);
			}
		}

		protected void paintContentBorderLeftEdge(Graphics g, int x, int y, int w, int h, boolean drawBroken, Rectangle selRect, boolean isContentBorderPainted) {

			if (isContentBorderPainted) {
				g.setColor(selectHighlight);
				g.fillRect(x,y,1,h-1);
			}
		}

		protected void paintContentBorderRightEdge(Graphics g, int x, int y, int w, int h, boolean drawBroken, Rectangle selRect, boolean isContentBorderPainted) {

			if (isContentBorderPainted) {
				g.setColor(darkShadow);
				g.fillRect(x+w-1,y,1,h);
			}
		}
	}

	// =======================================================================

	private static final class LeftRenderer extends TabRenderer {

		private LeftRenderer(JTabbedPane tabPane) {super(tabPane);}

		protected Insets getTabAreaInsets(Insets defaultInsets) {return new Insets(defaultInsets.top+4,defaultInsets.left,defaultInsets.bottom,defaultInsets.right);}
		protected Insets getTabInsets(int tabIndex,Insets tabInsets) {return new Insets(tabInsets.top,tabInsets.left-5,tabInsets.bottom+1,tabInsets.right-5);}
		protected Insets getSelectedTabPadInsets() {return WEST_INSETS;}

		protected int getTabLabelShiftX(int tabIndex, boolean isSelected) {return 1;}
		protected int getTabRunOverlay(int tabRunOverlay) {return 1;}

		protected boolean shouldPadTabRun(int run, boolean aPriori) {return false;}

		protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

			if (!isSelected) {
				g.setColor(selectLight);
				g.fillRect(x+1,y+1,w-1,h-2);
			} else {
				g.setColor(selectColor);
				g.fillRect(x+1,y+1,w-3,h-2);
			}
		}

		protected void paintContentBorderLeftEdge(Graphics g, int x, int y, int w, int h, boolean drawBroken, Rectangle selRect, boolean isContentBorderPainted) {

			g.setColor(selectHighlight);
			if (drawBroken && selRect.y>=y && selRect.y<=y+h) {
				g.fillRect(x,y,1,selRect.y+1-y);
				if (selRect.y+selRect.height < y+h-2) g.fillRect(x,selRect.y+selRect.height-1,1,y+h-selRect.y-selRect.height);
			} else {
				g.fillRect(x,y,1,h-1);
			}
		}
	}

	// =======================================================================

	private static final class RightRenderer extends TabRenderer {

		private RightRenderer(JTabbedPane tabPane) {super(tabPane);}

		protected int getTabLabelShiftX(int tabIndex, boolean isSelected) {return 1;}
		protected int getTabRunOverlay(int tabRunOverlay) {return 1;}

		protected boolean shouldPadTabRun(int run, boolean aPriori) {return false;}

		protected Insets getTabInsets(int tabIndex,Insets tabInsets) {return new Insets(tabInsets.top,tabInsets.left-5,tabInsets.bottom+1,tabInsets.right-5);}
		protected Insets getSelectedTabPadInsets() {return EAST_INSETS;}

		protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

			if (!isSelected) {
				g.setColor(selectLight);
				g.fillRect(x,y,w,h);
			} else {
				g.setColor(selectColor);
				g.fillRect(x+2,y,w-2,h);
			}
		}

		protected void paintContentBorderRightEdge(Graphics g, int x, int y, int w, int h, boolean drawBroken, Rectangle selRect, boolean isContentBorderPainted) {

			g.setColor(darkShadow);
			if (drawBroken && selRect.y>=y && selRect.y<=y+h) {
				g.fillRect(x+w-1,y,1,selRect.y-y);
				if (selRect.y+selRect.height<y+h-2) g.fillRect(x+w-1,selRect.y+selRect.height,1,y+h-selRect.y-selRect.height);
			} else {
				g.fillRect(x+w-1,y,1,h-1);
			}
		}
	}

	// =======================================================================

	private static final class TopRenderer extends TabRenderer {

		Color edgeColor=new Color(128,128,128,64);

		private TopRenderer(JTabbedPane tabPane) {super(tabPane);}

		protected Insets getTabAreaInsets(Insets defaultInsets) {return new Insets(defaultInsets.top,defaultInsets.left+2,defaultInsets.bottom,defaultInsets.right);}
		protected Insets getSelectedTabPadInsets() {return NORTH_INSETS;}
		protected Insets getTabInsets(int tabIndex,Insets tabInsets) {return new Insets(tabInsets.top+1,tabInsets.left,tabInsets.bottom+1,tabInsets.right-4);}

		protected int getTabLabelShiftY(int tabIndex,boolean isSelected) {return isSelected ? -1 : 0;}
		protected int getTabRunOverlay(int tabRunOverlay) {return tabRunOverlay-2;}
		protected int getTabRunIndent(int run) {return 0;}

		protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

			Shape currentClip=g.getClip();
			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
			g2.setBackground(Color.black);

			// Choose how the gradient will be painted on the tab.
		
			Point p=MouseInfo.getPointerInfo().getLocation();
			SwingUtilities.convertPointFromScreen(p,tabPane);
			int mouseOverIndex=tabPane.getUI().tabForCoordinate(tabPane,p.x,p.y);

			GradientPaint gp1=new GradientPaint(0,0,TAB_HIGHLIGHT_LIGHT_TOP,0,h/2+4,TAB_HIGHLIGHT_DARK_TOP);
			GradientPaint gp2=new GradientPaint(0,h/2,TAB_HIGHLIGHT_DARK_BOTTOM,0,h,TAB_HIGHLIGHT_LIGHT_BOTTOM);

			TabbedPaneUI ui=(TabbedPaneUI)tabPane.getUI();
			int previousTabRun=ui.getRunForTab(tabPane.getTabCount(),tabIndex-1);
			int currentTabRun=ui.getRunForTab(tabPane.getTabCount(),tabIndex);
			int nextTabRun=ui.getRunForTab(tabPane.getTabCount(),tabIndex+1);
			int tabCount=tabPane.getTabCount();

			if (tabCount==1) {
				Shape s=new RoundRectangle2D.Double(0,3,w+3,h+8,8,6);                       // Filled background
				g2.setPaint(gp1);
				g2.fill(s);
				g2.setPaint(gp2);
				g2.fill(new Rectangle2D.Double(0,h/2+4,w+3,h/2+4));
				if (isSelected) {
					EllipticalGradientPaint egp=new EllipticalGradientPaint((w+3)/2,h,TAB_SELECT_HIGHLIGHT_LIGHT,new Point2D.Double(w-1,h),TAB_SELECT_HIGHLIGHT_DARK);
					g2.setPaint(egp);
					g2.fillRect(0,h-12,w+3,15);
				}
			} else if ((tabIndex==0)||(currentTabRun>previousTabRun)) {                   // Left tabs
				Shape s=new RoundRectangle2D.Double(0,0,w,h+8,8,6);                         // Filled background
				Area a=new Area(s);
				a.subtract(new Area(new Rectangle2D.Double(0,h,w,8)));
				a.add(new Area(new Rectangle2D.Double(w-7,0,7,h-2)));
				g2.translate(x,y);
				g2.setPaint(gp1);
				g2.fill(a);
				g2.setPaint(gp2);
				g2.fill(new Rectangle2D.Double(0,h/2,w,h/2));
				if (isSelected) {
					EllipticalGradientPaint egp=new EllipticalGradientPaint((w)/2,h,TAB_SELECT_HIGHLIGHT_LIGHT,new Point2D.Double(w,h),TAB_SELECT_HIGHLIGHT_DARK);
					g2.setPaint(egp);
					g2.fillRect(0,h-12,w,15);
				} else if (tabIndex==mouseOverIndex) {
					EllipticalGradientPaint egp=new EllipticalGradientPaint((w)/2,h,TAB_ROLLOVER_HIGHLIGHT_LIGHT,new Point2D.Double(w,h),TAB_ROLLOVER_HIGHLIGHT_DARK);
					g2.setPaint(egp);
					g2.fillRect(0,h-12,w,15);
				}
			} else if ((tabIndex==tabPane.getTabCount()-1)||(currentTabRun<nextTabRun)) { // Right tabs
				Shape s=new RoundRectangle2D.Double(0,0,w,h+8,8,6);                         // Filled background
				Area a=new Area(s);
				a.subtract(new Area(new Rectangle2D.Double(0,h,w,8)));
				a.add(new Area(new Rectangle2D.Double(0,0,7,h-2)));
				g2.translate(x,y);
				g2.setPaint(gp1);
				g2.fill(a);
				g2.setPaint(gp2);
				g2.fill(new Rectangle2D.Double(0,h/2,w,h/2));
				if (!isSelected) {
					g2.setColor(edgeColor);
					g2.drawLine(0,0,0,h);
					if (tabIndex==mouseOverIndex) {
						EllipticalGradientPaint egp=new EllipticalGradientPaint((w)/2,h,TAB_ROLLOVER_HIGHLIGHT_LIGHT,new Point2D.Double(w,h),TAB_ROLLOVER_HIGHLIGHT_DARK);
						g2.setPaint(egp);
						g2.fillRect(0,h-12,w,15);
					}
				} else {
					EllipticalGradientPaint egp=new EllipticalGradientPaint((w)/2,h,TAB_SELECT_HIGHLIGHT_LIGHT,new Point2D.Double(w,h),TAB_SELECT_HIGHLIGHT_DARK);
					g2.setPaint(egp);
					g2.fillRect(0,h-12,w,15);
				}
			} else {                                                                      // Tabs in the middle
				Shape s=new Rectangle2D.Double(0,0,w,h);
				g2.translate(x,y);
				g2.setPaint(gp1);
				g2.fill(s);
				g2.setPaint(gp2);
				if (!isSelected) {
					g2.fill(new Rectangle2D.Double(0,h/2,w+3,h/2+4));
					g2.setColor(edgeColor);
					g2.drawLine(0,0,0,h);
					if (tabIndex==mouseOverIndex) {
						EllipticalGradientPaint egp=new EllipticalGradientPaint((w)/2,h,TAB_ROLLOVER_HIGHLIGHT_LIGHT,new Point2D.Double(w,h),TAB_ROLLOVER_HIGHLIGHT_DARK);
						g2.setPaint(egp);
						g2.fillRect(0,h-12,w,15);
					}
				} else {
					EllipticalGradientPaint egp=new EllipticalGradientPaint((w)/2,h,TAB_SELECT_HIGHLIGHT_LIGHT,new Point2D.Double(w,h),TAB_SELECT_HIGHLIGHT_DARK);
					g2.setPaint(egp);
					g2.fillRect(0,h-12,w,15);
				}
			}
			g2.dispose();
		}
	}

	// =======================================================================

	private static final class BottomRenderer extends TabRenderer {
        
		private BottomRenderer(JTabbedPane tabPane) {super(tabPane);}

		protected Insets getTabAreaInsets(Insets defaultInsets) {return new Insets(defaultInsets.top,defaultInsets.left+5,defaultInsets.bottom,defaultInsets.right);}
		protected Insets getSelectedTabPadInsets() {return SOUTH_INSETS;}
		protected Insets getTabInsets(int tabIndex, Insets tabInsets) {return new Insets(tabInsets.top,tabInsets.left-2,tabInsets.bottom,tabInsets.right-2);}

		protected int getTabLabelShiftY(int tabIndex, boolean isSelected) {return isSelected ? 0 : -1;}
		protected int getTabRunOverlay(int tabRunOverlay) {return tabRunOverlay-2;}
		protected int getTabRunIndent(int run) {return 6*run;}
		protected int getTabsOverlay() {return 4;}

		protected void paintTabBackground(Graphics g, int tabIndex, int x, int y, int w, int h, boolean isSelected) {

			Graphics2D g2=(Graphics2D)g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			if (isSelected) {
				GradientPaint gp=new GradientPaint(x,y,new Color(240,240,255),x,y+h,new Color(210,210,255));
				g2.setPaint(gp);
			} else {
				GradientPaint gp=new GradientPaint(x,y+2,new Color(220,220,220),x,y+2+(h/2),new Color(190,190,190),true);
				g2.setPaint(gp);
			}

			TabbedPaneUI ui=(TabbedPaneUI)tabPane.getUI();
			int previousTabRun=ui.getRunForTab(tabPane.getTabCount(),tabIndex-1);
			int currentTabRun=ui.getRunForTab(tabPane.getTabCount(),tabIndex);
			int nextTabRun=ui.getRunForTab(tabPane.getTabCount(),tabIndex+1);
			int sel=(isSelected) ? 1 : 0;

			// Note: Need to do the case where there's only one tab...

			if ((tabIndex==0)||(currentTabRun>previousTabRun)) {                          // Left tabs

				Shape s=new RoundRectangle2D.Double(x+1,y+1-8,w-1,h+7+sel,8,8);             // Filled background
				Area a=new Area(s);
				a.subtract(new Area(new Rectangle2D.Double(x+1,y+1-8,w-1,8)));
				a.add(new Area(new Rectangle2D.Double(x+w-7,y+1,7,h-2+sel)));
				g2.fill(a);
				s=new RoundRectangle2D.Double(x,y-8,w,h+8+sel,8,8);                         // Shape outline
				a=new Area(s);
				a.subtract(new Area(new Rectangle2D.Double(x,y-8,w,8)));
				a.add(new Area(new Rectangle2D.Double(x+w-8,y,8,h+sel)));
				g2.setColor(Color.darkGray);
				g2.draw(a);
				g2.setColor((isSelected) ? Color.white : new Color(240,240,240));           // Highlight lines
				g2.drawLine(x+3,y+h+sel-2,x+w-1,y+h+sel-2);
				g2.drawLine(x+4,y+h+sel-1,x+w-1,y+h+sel-1);
				if (isSelected) {                                                           // Darker shadow on left if selected
					g2.setColor(Color.lightGray);
					g2.drawLine(x+w-1,y+1,x+w-1,y+h+sel-1);
				}
				
			} else if ((tabIndex==tabPane.getTabCount()-1)||(currentTabRun<nextTabRun)) { // Right tabs

				Shape s=new RoundRectangle2D.Double(x+1,y+1-8,w-1,h+7+sel,8,8);             // Filled background
				Area a=new Area(s);
				a.subtract(new Area(new Rectangle2D.Double(x+1,y+1-8,w-1,8)));
				a.add(new Area(new Rectangle2D.Double(x,y+1,7,h-1+sel)));
				g2.fill(a);
				s=new RoundRectangle2D.Double(x,y-8,w,h+8+sel,8,8);                         // Shape outline
				a=new Area(s);
				a.subtract(new Area(new Rectangle2D.Double(x,y-8,w,8)));
				a.add(new Area(new Rectangle2D.Double(x,y,8,h+sel)));
				g2.setColor(Color.darkGray);
				g2.draw(a);
				g2.setColor((isSelected) ? Color.white : new Color(240,240,240));           // Highlight lines
				g2.drawLine(x+1,y+h+sel-2,x+w-3,y+h+sel-2);
				g2.drawLine(x+1,y+h+sel-1,x+w-4,y+h+sel-1);
				g2.drawLine(x+1,y+1,x+1,y+sel+h-2);
				if (isSelected) {                                                           // Darker shadow on left if selected
					g2.setColor(Color.lightGray);
					g2.drawLine(x+w-1,y+1,x+w-1,y+h+sel-5);
				}
						
			} else {                                                                      // Tabs in the middle

				g2.fillRect(x,y+1,w,h-2+sel);
				g2.setColor(Color.darkGray);
				g2.drawRect(x,y,w,h+sel);
				g2.setColor((isSelected) ? Color.white : new Color(240,240,240));
				g2.drawLine(x+1,y+h-1+sel,x+w-1,y+h-1+sel);
				g2.drawLine(x+1,y+h-2+sel,x+w-1,y+h-2+sel);
				g2.drawLine(x+1,y+1,x+1,y+sel+h-2);
				if (isSelected) {
					g2.setColor(Color.lightGray);
					g2.drawLine(x+w-1,y+1,x+w-1,y+h+sel-1);
				}
			}
			g2.dispose();
		}
	}
}
