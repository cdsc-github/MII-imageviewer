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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicGraphicsUtils;

import org.jvnet.flamingo.common.AbstractCommandButton;
import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JCommandButtonPanel;
import org.jvnet.flamingo.common.JCommandToggleButton;
import org.jvnet.flamingo.common.ui.BasicCommandButtonUI;
import org.jvnet.flamingo.ribbon.ui.JRibbonGallery;

import imageviewer.ui.graphics.EllipticalGradientPaint;

// =======================================================================

public class ImageViewerRibbonButtonUI extends BasicCommandButtonUI {

	private static final Color BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.topHighlightLight");
	private static final Color BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.topHighlightDark");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.bottomHighlightLight");
	private static final Color BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.bottomHighlightDark");
	private static final Color BUTTON_BORDER=UIManager.getColor("RibbonBandButton.borderColor");

	private static final Color CL_BUTTON_TOP_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.collapsedTopHighlightLight");
	private static final Color CL_BUTTON_TOP_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.collapsedTopHighlightDark");
	private static final Color CL_BUTTON_BOTTOM_HIGHLIGHT_LIGHT=UIManager.getColor("RibbonBandButton.collapsedBottomHighlightLight");
	private static final Color CL_BUTTON_BOTTOM_HIGHLIGHT_DARK=UIManager.getColor("RibbonBandButton.collapsedBottomHighlightDark");
	private static final Color CL_BUTTON_BORDER=UIManager.getColor("RibbonBandButton.collapsedBorderColor");

	private static final Color BOTTOM_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonHighlightBottomLight");
	private static final Color BOTTOM_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonHighlightBottomDark");
	private static final Color TOP_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonHighlightTopLight");
	private static final Color TOP_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonHighlightTopDark");
	private static final Color ROLLOVER_BOTTOM_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonRolloverBottomLight");
	private static final Color ROLLOVER_BOTTOM_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonRolloverBottomDark");
	private static final Color ROLLOVER_TOP_TOGGLE_LIGHT=UIManager.getColor("JRibbon.toggleButtonRolloverTopLight");
	private static final Color ROLLOVER_TOP_TOGGLE_DARK=UIManager.getColor("JRibbon.toggleButtonRolloverTopDark");

	private static final Color BUTTON_HIGHLIGHT1=new Color(255,255,255,100);
	private static final Color BUTTON_HIGHLIGHT2=new Color(255,255,255,50);

	private static final Font BUTTON_FONT=UIManager.getFont("Button.font");
	private static final Color BUTTON_TEXT=UIManager.getColor("RibbonBandButton.textColor");

	public static ComponentUI createUI(JComponent c) {return new ImageViewerRibbonButtonUI();}

	protected Rectangle iconRect=new Rectangle();
	protected Rectangle bigLabel1Rect=new Rectangle();
	protected Rectangle bigLabel2Rect=new Rectangle();
	protected Rectangle midLabelRect=new Rectangle();
	protected Rectangle midExtraLabelRect=new Rectangle();
	protected Rectangle actionArrowLabelRect=new Rectangle();

	MouseAdapter mml=null;
	boolean isOverTopGallery=false, isOverBottomGallery=false, isOver=false;

	// =======================================================================

	public void installUI(JComponent c) {

		commandButton=(AbstractCommandButton)c;
		c.setOpaque(false);
		installDefaults();
		installComponents();
		installListeners();
		commandButton.setLayout(createLayoutManager());
		commandButton.setBorder(new EmptyBorder(1,4,1,4));
		if (commandButton.getState()!=null) updateState(commandButton.getState(),false);
		updateCustomDimension();
	}

	public void uninstallUI(JComponent c) {

		c.setLayout(null);
		uninstallListeners();
		uninstallComponents();
		uninstallDefaults();
		commandButton=null;
	}

	protected void installListeners() {

		super.installListeners();
		mml=new MouseAdapter() {
			public void mouseEntered(MouseEvent e) {isOver=true; commandButton.repaint();}
			public void mouseExited(MouseEvent e) {isOver=false; commandButton.repaint();}

			public void mouseClicked(MouseEvent e) {
				final AbstractButton ab=commandButton;
				final ActionListener[] al=ab.getActionListeners();
				if (al!=null) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							for (int loop=0; loop<al.length; loop++) al[loop].actionPerformed(new ActionEvent(ab,ActionEvent.ACTION_PERFORMED,"pressed"));
						}
					});
				}
			}

			public void mouseMoved(MouseEvent e) {
				Point p=e.getPoint();
				boolean b=(actionClickArea.contains(p)) ? true : false;
				if (isOverTopGallery!=b) {isOverTopGallery=b; commandButton.repaint();}
			}
		};
		commandButton.addMouseListener(mml);
		commandButton.addMouseMotionListener(mml);
	}

	protected void uninstallListeners() {

		super.uninstallListeners();
		commandButton.removeMouseListener(mml);
		commandButton.removeMouseMotionListener(mml);
	}

	protected void installComponents() {

		super.installComponents();
		titlePart1=titlePart1.trim();
		titlePart2=titlePart2.trim();
	}

	// =======================================================================

	protected Icon getIconToPaint() {

		if (commandButton.isEnabled()) return commandButton.getIcon();
		BasicResizableIcon bsi=(BasicResizableIcon)commandButton.getDisabledIcon();
		if (bsi==null) return commandButton.getIcon();
		bsi.setDimension(new Dimension(commandButton.getIcon().getIconWidth(),commandButton.getIcon().getIconHeight()));
		return bsi;
	}

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g.create();
		g2.setFont(BUTTON_FONT);

		if (isOver) {if (c.getMousePosition()==null) isOver=false;} // Just do one more check...

		actionClickArea=new Rectangle(0,0,0,0);
		popupClickArea=new Rectangle(0,0,0,0);

		iconRect.setBounds(0,0,0,0);
		bigLabel1Rect.setBounds(0,0,0,0);
		bigLabel2Rect.setBounds(0,0,0,0);
		midLabelRect.setBounds(0,0,0,0);
		midExtraLabelRect.setBounds(0,0,0,0);
		actionArrowLabelRect.setBounds(0,0,0,0);
	
		JCommandButton.CommandButtonKind buttonKind=(commandButton instanceof JCommandButton) ? 
			((JCommandButton)commandButton).getCommandButtonKind() : JCommandButton.CommandButtonKind.ACTION_ONLY;
		layoutButtonComponents(g2,c,buttonKind,iconRect,bigLabel1Rect,bigLabel2Rect,midLabelRect,midExtraLabelRect,actionArrowLabelRect);

		int targetWidth=((commandButton.getState()==ElementState.CUSTOM)||(commandButton.getState()==ElementState.BIG)||(commandButton.getState()==ElementState.BIG_FIXED_LANDSCAPE)||
										 (commandButton.getState()==ElementState.TILE)||(commandButton.getState()==ElementState.ORIG)) ? c.getWidth() : (int)c.getPreferredSize().getWidth();

		if (commandButton.getClientProperty("isRibbonToggleButton")!=null) {
			paintToggleButtonBackground(g2,new Rectangle(0,0,targetWidth,c.getHeight()));
		} else {
			paintButtonBackground(g2,new Rectangle(0,0,targetWidth,c.getHeight()));
		}
		if (iconRect!=null) paintButtonIcon(g,iconRect);
		if (actionArrowLabelRect.getWidth()>0) actionIcon.paintIcon(commandButton,g2,actionArrowLabelRect.x,actionArrowLabelRect.y);

		FontMetrics fm=g2.getFontMetrics();
		g2.setColor(BUTTON_TEXT);

		if (bigLabel1Rect.getWidth()>0) {
			int mnemonicChar=commandButton.getMnemonic();
			BasicGraphicsUtils.drawString(g2,titlePart1,mnemonicChar,bigLabel1Rect.x,bigLabel1Rect.y+fm.getAscent());
		}
		if (bigLabel2Rect.getWidth()>0) {
			int mnemonicChar=commandButton.getMnemonic();
			BasicGraphicsUtils.drawString(g2,titlePart2,mnemonicChar,bigLabel2Rect.x,bigLabel2Rect.y+fm.getAscent());
		}
		if (midLabelRect.getWidth()>0) {
			int mnemonicChar=this.commandButton.getMnemonic();
			BasicGraphicsUtils.drawString(g2,commandButton.getTitle(),mnemonicChar,midLabelRect.x,midLabelRect.y+fm.getAscent());
		}
		if (midExtraLabelRect.getWidth()>0) {
			BasicGraphicsUtils.drawString(g2,commandButton.getExtraText(),-1,midExtraLabelRect.x,midExtraLabelRect.y+fm.getAscent());
		}
		g2.dispose();
	}

	// =======================================================================

	protected void layoutButtonComponents(Graphics g, JComponent c, JCommandButton.CommandButtonKind buttonKind, Rectangle iconRect,
																				Rectangle bigLabel1Rect, Rectangle bigLabel2Rect, Rectangle midLabelRect, Rectangle midExtraLabelRect,
																				Rectangle actionArrowLabelRect) {

		FontMetrics fm=g.getFontMetrics();
		int width=c.getWidth();
		int height=c.getHeight();

		if (buttonKind==JCommandButton.CommandButtonKind.ACTION_ONLY) {
			actionClickArea.x=0;
			actionClickArea.y=0;
			actionClickArea.width=width;
			actionClickArea.height=height;
		}

		if (buttonKind==JCommandButton.CommandButtonKind.POPUP_ONLY) {
			popupClickArea.x=0;
			popupClickArea.y=0;
			popupClickArea.width=width;
			popupClickArea.height=height;
		}

		int x=commandButton.getBorder().getBorderInsets(commandButton).left;
		int y=3+commandButton.getBorder().getBorderInsets(commandButton).top;

		switch (commandButton.getState()) {

		    case CUSTOM: case BIG: case BIG_FIXED_LANDSCAPE: case TILE:
			    case ORIG: if (commandButton.getState()==ElementState.ORIG) commandButton.getIcon().revertToOriginalDimension(); else commandButton.getIcon().setHeight(32);
						         iconRect.setBounds((width-commandButton.getIcon().getIconWidth())/2,y,commandButton.getIcon().getIconWidth(),commandButton.getIcon().getIconHeight());
										 if ((commandButton.getParent() instanceof JRibbonGallery)||(commandButton.getParent() instanceof JCommandButtonPanel)) y-=3;
										 y+=commandButton.getIcon().getIconHeight()+3;
										 Rectangle2D rect1=fm.getStringBounds(titlePart1,g);
										 int labelWidth=(int)rect1.getWidth();
										 int labelHeight=(int)rect1.getHeight();
										 bigLabel1Rect.setBounds((width-labelWidth)/2,y,labelWidth,labelHeight);
										 y+=labelHeight+2;
										 if (titlePart2.length()==0) {
											 if (actionIcon!=null) {
												 x=((width-actionIcon.getIconWidth())/2)+2;
												 actionArrowLabelRect.setBounds(x,y+(labelHeight-actionIcon.getIconHeight())/2-3,actionIcon.getIconWidth(),actionIcon.getIconHeight());
											 }
										 } else {
											 Rectangle2D rect2=fm.getStringBounds(titlePart2,g);
											 labelWidth=(int)rect2.getWidth();
											 x=(width-(labelWidth+(((actionIcon!=null)&&(y+labelHeight>height-10)) ? 4+actionIcon.getIconWidth() : 0)))/2;
											 bigLabel2Rect.setBounds(x,y-5,labelWidth,labelHeight);
											 if (actionIcon!=null) {
												 if (y+labelHeight<=height-10) {
													 y+=labelHeight-4; 
													 x=((width-actionIcon.getIconWidth())/2)+2;
													 actionArrowLabelRect.setBounds(x,y+(labelHeight-actionIcon.getIconHeight())/2-4,actionIcon.getIconWidth(),actionIcon.getIconHeight());
												 } else {
													 x+=labelWidth+4;
													 actionArrowLabelRect.setBounds(x,y+(labelHeight-actionIcon.getIconHeight())/2-3,actionIcon.getIconWidth(),actionIcon.getIconHeight());
												 }
											 }
										 }
										 if (actionIcon!=null) {
											 int labelY=commandButton.getIcon().getIconHeight()+7;
											 x=commandButton.getBorder().getBorderInsets(commandButton).left;
											 y=3+commandButton.getBorder().getBorderInsets(commandButton).top;
											 actionClickArea.setBounds(x,y,width,labelY);
											 popupClickArea.setBounds(x,y+labelY,width,height-labelY);
										 }
										 break;

			  case MEDIUM: iconRect.setBounds(x,(height-commandButton.getIcon().getIconHeight())/2,commandButton.getIcon().getIconWidth(),commandButton.getIcon().getIconHeight());
					           Rectangle2D midRect=fm.getStringBounds(commandButton.getTitle(),g);
										 int midLabelWidth=(int)midRect.getWidth();
										 int midLabelHeight=(int)midRect.getHeight();
										 x+=2+commandButton.getIcon().getIconWidth();
										 midLabelRect.setBounds(x,(height-midLabelHeight)/2,midLabelWidth,midLabelHeight);
										 String extraText=this.commandButton.getExtraText();
										 midExtraLabelRect.x=x;
										 midExtraLabelRect.y=midLabelRect.y+midLabelHeight;
										 midExtraLabelRect.width=fm.stringWidth((extraText==null) ? "" : extraText);
										 midExtraLabelRect.height=fm.getAscent();
										 x+=midLabelWidth;
										 if (actionIcon!=null) {
											 actionArrowLabelRect.setBounds(x+5,(height-actionIcon.getIconHeight())/2+2,actionIcon.getIconWidth(),actionIcon.getIconHeight());
											 int x0=commandButton.getBorder().getBorderInsets(commandButton).left;
											 actionClickArea.setBounds(x0,y,x+4-x0,height);
											 popupClickArea.setBounds(x+4,y,actionIcon.getIconWidth()+1,height); //width-x-4,height);
										 } 
										 break;

			   case SMALL: iconRect.setBounds(x,(height-commandButton.getIcon().getIconHeight())/2,commandButton.getIcon().getIconWidth(),commandButton.getIcon().getIconHeight());
										 x+=commandButton.getIcon().getIconWidth();
										 if (actionIcon!=null) {
											 actionArrowLabelRect.setBounds(x+8,(height-actionIcon.getIconHeight())/2+2,actionIcon.getIconWidth(),actionIcon.getIconHeight());
											 int x0=commandButton.getBorder().getBorderInsets(commandButton).left;
											 actionClickArea.setBounds(x0,y,x+6-x0,height);
											 // popupClickArea.setBounds(x+7,y,width-x-7,height);
											 popupClickArea.setBounds(x+7,y,actionIcon.getIconWidth(),height);
										 } 
										 break;
		}
		commandButton.putClientProperty("icon.bounds",iconRect);
	}

	// =======================================================================

	protected void paintToggleButtonBackground(Graphics graphics, Rectangle toFill) {

		if (commandButton.isSelected()) {

			Graphics2D g2=(Graphics2D)graphics;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 		
			boolean buttonState=(((((commandButton instanceof JCommandButton)&&(((JCommandButton)commandButton).getPopupPanel()==null))))||(commandButton instanceof JCommandToggleButton)||
													 ((commandButton.getState()==ElementState.MEDIUM)||(commandButton.getState()==ElementState.SMALL)));

			int buttonHalf=(int)(toFill.height*0.4);
			Shape s1=new RoundRectangle2D.Double(toFill.x,toFill.y,toFill.width,buttonHalf,6,6);
			Area a1=new Area(s1);
			GradientPaint gp1=new GradientPaint(toFill.x,toFill.y,isOver ? (((!buttonState && isOverTopGallery)||(buttonState)) ? ROLLOVER_TOP_TOGGLE_LIGHT : TOP_TOGGLE_LIGHT) : TOP_TOGGLE_LIGHT,
																					toFill.x,toFill.y+buttonHalf,isOver ? (((!buttonState && isOverTopGallery)||(buttonState)) ? ROLLOVER_TOP_TOGGLE_DARK : TOP_TOGGLE_DARK) : TOP_TOGGLE_DARK);
			a1.add(new Area(new Rectangle2D.Double(toFill.x,toFill.y+(buttonHalf/2),toFill.width,1+buttonHalf/2)));
			g2.setPaint(gp1);
			g2.fill(a1);
			Shape s2=new RoundRectangle2D.Double(toFill.x,toFill.y+buttonHalf,toFill.width,toFill.height-buttonHalf,6,6);
			Area a2=new Area(s2);
			EllipticalGradientPaint egp=new EllipticalGradientPaint(toFill.x+toFill.width/2,toFill.y+toFill.height,
																															isOver ? (((!buttonState && !isOverTopGallery)||(buttonState)) ? ROLLOVER_BOTTOM_TOGGLE_LIGHT : BOTTOM_TOGGLE_LIGHT) : BOTTOM_TOGGLE_LIGHT,
																															new Point2D.Double(toFill.width,toFill.height-buttonHalf),
																															isOver ? (((!buttonState && !isOverTopGallery)||(buttonState)) ? ROLLOVER_BOTTOM_TOGGLE_DARK : BOTTOM_TOGGLE_DARK) : BOTTOM_TOGGLE_DARK);
			a2.add(new Area(new Rectangle2D.Double(toFill.x,toFill.y+buttonHalf,toFill.width,toFill.height/4)));
			g2.setPaint(egp);
			g2.fill(a2);

			if (!buttonState) {
				int labelY=commandButton.getIcon().getIconHeight()+7;
				g2.setColor(BUTTON_BORDER.darker());
				g2.drawLine(toFill.x,labelY+1,toFill.width,labelY+1);
				g2.setColor(BUTTON_BORDER);
				g2.drawLine(toFill.x,labelY+2,toFill.width,labelY+2);
			}

			g2.setColor(BUTTON_BORDER.darker());
			g2.drawRoundRect(toFill.x,toFill.y,toFill.width-1,toFill.height-1,6,6);
			g2.setColor(isOver ? BUTTON_HIGHLIGHT2 : BUTTON_HIGHLIGHT1);
			g2.drawLine(toFill.x+1,toFill.y+6,1,toFill.y+toFill.height-6);
			g2.drawLine(toFill.x+6,1,toFill.x+toFill.width-6,1);
			g2.drawLine(toFill.x+toFill.width-2,toFill.y+6,toFill.x+toFill.width-2,toFill.y+toFill.height-6);
			
		} else {
			paintButtonBackground(graphics,toFill);
		}
	}

	// =======================================================================

	protected void paintButtonBackground(Graphics graphics, Rectangle toFill) {

		if ((isOver)&&(commandButton.isEnabled())) {
			Graphics2D g2=(Graphics2D)graphics;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 

			boolean isCollapsed=(commandButton.getClientProperty("isCollapsedButton")==null) ? false : true;
			boolean buttonState=((actionIcon==null)||isCollapsed);

			if (buttonState) {
				int buttonHalf=(isCollapsed) ? (toFill.height/4) : toFill.height/2;
				GradientPaint gp1=(isCollapsed) ? new GradientPaint(toFill.x,toFill.y,CL_BUTTON_TOP_HIGHLIGHT_LIGHT,toFill.x,toFill.y+buttonHalf,CL_BUTTON_TOP_HIGHLIGHT_DARK) : 
					new GradientPaint(toFill.x,toFill.y,BUTTON_TOP_HIGHLIGHT_LIGHT,toFill.x,toFill.y+buttonHalf,BUTTON_TOP_HIGHLIGHT_DARK);
				Shape s1=new RoundRectangle2D.Double(toFill.x,toFill.y,toFill.width,buttonHalf,6,6);
				Area a1=new Area(s1);
				a1.add(new Area(new Rectangle2D.Double(toFill.x,toFill.y+(buttonHalf/2),toFill.width,1+buttonHalf/2)));
				g2.setPaint(gp1);
				g2.fill(a1);
				GradientPaint gp2=(isCollapsed) ? new GradientPaint(toFill.x,toFill.y+buttonHalf,CL_BUTTON_BOTTOM_HIGHLIGHT_LIGHT,toFill.x,toFill.y+toFill.height,CL_BUTTON_BOTTOM_HIGHLIGHT_DARK) :
					new GradientPaint(toFill.x,toFill.y+buttonHalf,BUTTON_BOTTOM_HIGHLIGHT_LIGHT,toFill.x,toFill.y+toFill.height,BUTTON_BOTTOM_HIGHLIGHT_DARK);
				Shape s2=new RoundRectangle2D.Double(toFill.x,toFill.y+buttonHalf,toFill.width,toFill.height-buttonHalf,6,6);
				Area a2=new Area(s2);
				a2.add(new Area(new Rectangle2D.Double(toFill.x,toFill.y+buttonHalf,toFill.width,toFill.height/4)));
				g2.setPaint(gp2);
				g2.fill(a2);			
				g2.setColor((isCollapsed) ? CL_BUTTON_BORDER : BUTTON_BORDER);
				g2.drawRoundRect(toFill.x,toFill.y,toFill.width-1,toFill.height-1,6,6);
			} else {
				switch (commandButton.getState()) {

				  case CUSTOM: case BIG: case BIG_FIXED_LANDSCAPE:
				              case ORIG: {int labelY=commandButton.getIcon().getIconHeight()+7;
				                          int buttonHalf=(int)((labelY-toFill.y)*0.5);
																	if (isOverTopGallery) {
																		Paint p1=new GradientPaint(toFill.x,toFill.y,BUTTON_TOP_HIGHLIGHT_LIGHT,toFill.x,toFill.y+buttonHalf,BUTTON_TOP_HIGHLIGHT_DARK);
																		Shape s1=new RoundRectangle2D.Double(toFill.x,toFill.y,toFill.width,buttonHalf,6,6);
																		Area a1=new Area(s1);
																		a1.add(new Area(new Rectangle2D.Double(toFill.x,toFill.y+(buttonHalf/2),toFill.width,1+buttonHalf/2)));
																		g2.setPaint(p1);
																		g2.fill(a1);
																		g2.setPaint(new GradientPaint(toFill.x,toFill.y+buttonHalf,BUTTON_BOTTOM_HIGHLIGHT_LIGHT,toFill.x,labelY,BUTTON_BOTTOM_HIGHLIGHT_DARK));
																		g2.fillRect(toFill.x,toFill.y+buttonHalf,toFill.width,labelY-(toFill.y+buttonHalf)+1);
																	} else {
																		Paint p1=new GradientPaint(toFill.x,toFill.y,BUTTON_TOP_HIGHLIGHT_LIGHT,toFill.x,toFill.y+labelY,BUTTON_TOP_HIGHLIGHT_DARK); 
																		Shape s1=new RoundRectangle2D.Double(toFill.x,toFill.y,toFill.width,labelY,6,6);
																		Area a1=new Area(s1);
																		a1.add(new Area(new Rectangle2D.Double(toFill.x,toFill.y+labelY-6,toFill.width,7)));
																		g2.setPaint(p1);
																		g2.fill(a1);
																	}
																	Paint p2=(isOverTopGallery) ? BUTTON_TOP_HIGHLIGHT_LIGHT : new GradientPaint(toFill.x,labelY,BUTTON_BOTTOM_HIGHLIGHT_LIGHT,
																																																							 toFill.x,toFill.y+toFill.height,BUTTON_TOP_HIGHLIGHT_DARK);
																	g2.setPaint(p2);
																	Shape s2=new RoundRectangle2D.Double(toFill.x,labelY+1,toFill.width,toFill.height-labelY-2,6,6);
																	Area a2=new Area(s2);
																	a2.add(new Area(new Rectangle2D.Double(toFill.x,labelY+1,toFill.width,10)));
																	g2.fill(a2);
																	g2.setColor(BUTTON_BORDER);
																	g2.drawLine(toFill.x,labelY+1,toFill.width,labelY+1);
																	if (!isOverTopGallery) {
																		g2.setColor(BUTTON_TOP_HIGHLIGHT_DARK); 
																		g2.drawLine(toFill.x+1,labelY+2,toFill.width-2,labelY+2);
																	} else {
																		g2.setColor(Color.white);
																		g2.drawLine(toFill.x+1,labelY+2,toFill.width-2,labelY+2);
																	}
																	g2.setColor((isCollapsed) ? CL_BUTTON_BORDER : BUTTON_BORDER);
																	g2.drawRoundRect(toFill.x,toFill.y,toFill.width-1,toFill.height-1,6,6);
																	break;}			

				                default: {int buttonHalf=toFill.height/2;
						                      Area a1=new Area(new RoundRectangle2D.Double(toFill.x,toFill.y,popupClickArea.x-toFill.x-2,buttonHalf,4,4));
																	a1.add(new Area(new Rectangle(popupClickArea.x-8,toFill.y,6,buttonHalf)));
																	a1.add(new Area(new Rectangle(toFill.x,toFill.y+buttonHalf-4,popupClickArea.x-toFill.x-2,4)));
						                      Area a2=new Area(new RoundRectangle2D.Double(toFill.x,toFill.y+buttonHalf,popupClickArea.x-toFill.x-2,buttonHalf,4,4));
																	a2.add(new Area(new Rectangle(popupClickArea.x-8,toFill.y+buttonHalf,6,buttonHalf)));
																	a2.add(new Area(new Rectangle(toFill.x,toFill.y+buttonHalf,popupClickArea.x-toFill.x-2,4)));
						                      Area a3=new Area(new RoundRectangle2D.Double(popupClickArea.x-2,toFill.y,popupClickArea.width+2,buttonHalf,4,4));
																	a3.add(new Area(new Rectangle(popupClickArea.x-2,toFill.y,6,buttonHalf)));
																	a3.add(new Area(new Rectangle(popupClickArea.x-2,toFill.y+buttonHalf-4,popupClickArea.width+2,4)));
						                      Area a4=new Area(new RoundRectangle2D.Double(popupClickArea.x-2,toFill.y+buttonHalf,popupClickArea.width+2,buttonHalf,4,4));
																	a4.add(new Area(new Rectangle(popupClickArea.x-2,toFill.y+buttonHalf,6,buttonHalf)));
																	a4.add(new Area(new Rectangle(popupClickArea.x-2,toFill.y+buttonHalf,popupClickArea.width+2,4)));
																	GradientPaint gp1=new GradientPaint(toFill.x,toFill.y,(isOverTopGallery) ? BUTTON_TOP_HIGHLIGHT_LIGHT : BUTTON_TOP_HIGHLIGHT_LIGHT.brighter(),
																																			toFill.x,toFill.y+buttonHalf,(isOverTopGallery) ? BUTTON_TOP_HIGHLIGHT_DARK : BUTTON_TOP_HIGHLIGHT_DARK.brighter());
																	g2.setPaint(gp1);
																	g2.fill(a1);
																	GradientPaint gp2=new GradientPaint(toFill.x,toFill.y+buttonHalf,(isOverTopGallery) ? BUTTON_BOTTOM_HIGHLIGHT_DARK : BUTTON_BOTTOM_HIGHLIGHT_DARK.brighter(),
																																			toFill.x,toFill.y+toFill.height,(isOverTopGallery) ? BUTTON_BOTTOM_HIGHLIGHT_LIGHT : BUTTON_BOTTOM_HIGHLIGHT_LIGHT.brighter());
																	g2.setPaint(gp2);
																	g2.fill(a2);
																	GradientPaint gp3=new GradientPaint(toFill.x,toFill.y,(!isOverTopGallery) ? BUTTON_TOP_HIGHLIGHT_LIGHT : BUTTON_TOP_HIGHLIGHT_LIGHT.brighter(),
																																			toFill.x,toFill.y+buttonHalf,(!isOverTopGallery) ? BUTTON_TOP_HIGHLIGHT_DARK : BUTTON_TOP_HIGHLIGHT_DARK.brighter());

																	g2.setPaint(gp3);
																	g2.fill(a3);
																	GradientPaint gp4=new GradientPaint(toFill.x,toFill.y+buttonHalf,(!isOverTopGallery) ? BUTTON_BOTTOM_HIGHLIGHT_DARK : BUTTON_BOTTOM_HIGHLIGHT_DARK.brighter(),
																																			toFill.x,toFill.y+toFill.height,(!isOverTopGallery) ? BUTTON_BOTTOM_HIGHLIGHT_LIGHT : BUTTON_BOTTOM_HIGHLIGHT_LIGHT.brighter());
																	g2.setPaint(gp4);
																	g2.fill(a4);
																	g2.setColor(BUTTON_HIGHLIGHT2);
																	g2.drawLine(popupClickArea.x-1,toFill.y,popupClickArea.x-1,toFill.y+toFill.height);
																	g2.setColor(BUTTON_BORDER);
																	g2.drawLine(popupClickArea.x-2,toFill.y,popupClickArea.x-2,toFill.y+toFill.height);
																	g2.setColor((isCollapsed) ? CL_BUTTON_BORDER : BUTTON_BORDER);
																	g2.drawRoundRect(toFill.x,toFill.y,toFill.width-1,toFill.height-1,4,4);
																	break;}
				}
			}
		}
	}

	// =======================================================================

	protected LayoutManager createLayoutManager() {return new ImageViewerRibbonButtonLayout();}

	// =======================================================================

	private class ImageViewerRibbonButtonLayout implements LayoutManager {

		FontMetrics fm=null;
		Graphics2D g2=null;
	
		public ImageViewerRibbonButtonLayout() {
			g2=(Graphics2D)new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB).getGraphics();
			g2.setFont(UIManager.getFont("JRibbon.font"));
			fm=g2.getFontMetrics();
		}

		public void addLayoutComponent(String name, Component c) {}
		public void removeLayoutComponent(Component c) {}

		public Dimension preferredLayoutSize(Container c) {

			Insets borderInsets=commandButton.getBorder().getBorderInsets(commandButton);
			int bx=borderInsets.left+borderInsets.right;
			int by=borderInsets.top+borderInsets.bottom;

			Dimension iconDim=toTakeSavedDimension ? savedDimension : new Dimension(commandButton.getIcon().getIconWidth(),commandButton.getIcon().getIconHeight());
			switch (commandButton.getState()) {
			    case SMALL: return new Dimension(bx+commandButton.getIcon().getIconWidth()+((actionIcon!=null) ? actionIcon.getIconWidth()+4 : -1),by+iconDim.height);
			   case MEDIUM: return new Dimension(bx+iconDim.width+(int)fm.getStringBounds(commandButton.getTitle(),g2).getWidth()+
																					 ((actionIcon!=null) ? 4+actionIcon.getIconWidth() : 4),by+iconDim.height);
			   case CUSTOM: case BIG: case BIG_FIXED_LANDSCAPE: case TILE:
			     case ORIG: Rectangle2D rect1=fm.getStringBounds(titlePart1,g2);
						          Rectangle2D rect2=fm.getStringBounds(titlePart2,g2);
											int rect2Width=(int)rect2.getWidth()+((actionIcon!=null) ?  4+actionIcon.getIconWidth() : 0);
											int width=(int)(Math.max(iconDim.width,Math.max(rect1.getWidth(),rect2Width)));
											int offset=(commandButton.getParent() instanceof JRibbonGallery) ? 6 : 0;
											return new Dimension(bx+width,by+offset+iconDim.height+(2*fm.getHeight()));
			}
			return null;
		}

		public Dimension minimumLayoutSize(Container c) {return preferredLayoutSize(c);}

		public void layoutContainer(Container c) {

			switch (commandButton.getState()) {

			  case CUSTOM: case BIG: case BIG_FIXED_LANDSCAPE: case TILE:
			    case ORIG: if (commandButton.getState()==ElementState.BIG) commandButton.getIcon().setHeight(32);
				             else if (commandButton.getState()==ElementState.ORIG) commandButton.getIcon().revertToOriginalDimension();
 					           commandButton.setToolTipText(null);
										 break;
			  case MEDIUM: commandButton.getIcon().setHeight(16);
 					           commandButton.setToolTipText(null);
										 break;
			   case SMALL: commandButton.getIcon().setHeight(16);
					           commandButton.setToolTipText(commandButton.getTitle());
										 break;
			}
		}
	}
}
