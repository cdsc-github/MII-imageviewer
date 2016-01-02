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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.event.KeyEvent;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

// =======================================================================

public final class MenuItemRenderer {

	private static final AlphaComposite ALPHA_MENU_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.85f);

	private static final String MAX_TEXT_WIDTH=new String("maxTextWidth");
	private static final String MAX_ACC_WIDTH=new String("maxAccWidth");
	private static final String ACCELERATOR_DELIM=UIManager.getString("MenuItem.acceleratorDelimiter");

	private static final Icon FILLER_ICON=new MinimumSizedIcon();
	private static final Icon NULL_ICON=new NullIcon();

	private static final Color DEFAULT_FOREGROUND=UIManager.getColor("PopupMenu.foreground");

	// =======================================================================
	// Reused globals that are reset as needed

	private static Rectangle zeroRect=new Rectangle(0,0,0,0);
	private static Rectangle iconRect=new Rectangle(0,0,0,0);
	private static Rectangle textRect=new Rectangle(0,0,0,0);
	private static Rectangle acceleratorRect=new Rectangle(0,0,0,0);
	private static Rectangle checkIconRect=new Rectangle(0,0,0,0);
	private static Rectangle arrowIconRect=new Rectangle(0,0,0,0);
	private static Rectangle viewRect=new Rectangle(Short.MAX_VALUE,Short.MAX_VALUE);
	private static Rectangle r=new Rectangle(0,0,0,0);

	// =======================================================================

	JMenuItem menuItem=null;
	boolean	iconBorderEnabled=false;  
	Font acceleratorFont=null;
	Color	selectionForeground=null, disabledForeground=null, acceleratorForeground=null, acceleratorSelectionForeground=null;

	public MenuItemRenderer(JMenuItem menuItem, boolean iconBorderEnabled, Font acceleratorFont, Color selectionForeground,
													Color	disabledForeground,	Color	acceleratorForeground, Color acceleratorSelectionForeground) {

		this.menuItem=menuItem;
		this.iconBorderEnabled=iconBorderEnabled;
		this.acceleratorFont=acceleratorFont;
		this.selectionForeground=selectionForeground;
		this.disabledForeground=disabledForeground;
		this.acceleratorForeground=acceleratorForeground;
		this.acceleratorSelectionForeground=acceleratorSelectionForeground;
	}

	// =======================================================================
	
	private void resetRects() {

		iconRect.setBounds(zeroRect);
		textRect.setBounds(zeroRect);
		acceleratorRect.setBounds(zeroRect);
		checkIconRect.setBounds(zeroRect);
		arrowIconRect.setBounds(zeroRect);
		viewRect.setBounds(0,0,Short.MAX_VALUE,Short.MAX_VALUE);
		r.setBounds(zeroRect);
	}

	// =======================================================================
	// Adapated from JGoodies to handle the alignment of menu items with
	// icons because the default Swing mechanism does not handle the
	// text alignment nicely. Multiple fixes, though...

	public Dimension getPreferredMenuItemSize(JComponent c, Icon checkIcon, Icon arrowIcon, int defaultTextIconGap) {
			
		JMenuItem b=(JMenuItem)c;
		KeyStroke accelerator=b.getAccelerator();
		String text=b.getText();
		String acceleratorText=new String();

		if (accelerator!=null) {
			int modifiers=accelerator.getModifiers();
			if (modifiers>0) {
				acceleratorText=KeyEvent.getKeyModifiersText(modifiers);
				acceleratorText+=ACCELERATOR_DELIM;
			}
			int keyCode=accelerator.getKeyCode();
			acceleratorText+=(keyCode!=0) ?	KeyEvent.getKeyText(keyCode) : new Character(accelerator.getKeyChar()).toString();
		}

		Font font=b.getFont();
		FontMetrics fm=b.getFontMetrics(font);
		FontMetrics fmAccel=b.getFontMetrics(acceleratorFont);
		resetRects();

		// Fixed to handle the case where maybe the menu is only a set of
		// icons.  Also made sure that the condition for the fillerIcon is
		// GE, not GT. 

		Icon wrappedIcon=getWrappedIcon(getIcon(menuItem,checkIcon));
		Icon wrappedArrowIcon=getWrappedIcon(arrowIcon);
		Icon icon=(b.getIcon()==null) ? null : (wrappedIcon.getIconHeight()>=FILLER_ICON.getIconHeight()) ? wrappedIcon : null; 
		layoutMenuItem(fm,text,fmAccel,acceleratorText,icon,wrappedIcon,wrappedArrowIcon,
									 b.getVerticalAlignment(),b.getHorizontalAlignment(),
									 b.getVerticalTextPosition(),b.getHorizontalTextPosition(),
									 viewRect,iconRect,textRect,acceleratorRect,checkIconRect,arrowIconRect,
									 (text==null) ? 0 : defaultTextIconGap,defaultTextIconGap);

		r.setBounds(textRect);
		r=SwingUtilities.computeUnion(iconRect.x,iconRect.y,iconRect.width,iconRect.height,r);

		// Adapated to handle the case where maybe there's no text...

		if (b.getText()!=null) {

			// To make the accelerator texts appear in a column, find the
			// widest MenuItem text and the widest accelerator text.  Get the
			// parent, which stores the information. Check the parent, and see
			// that it is not a top-level menu.

			Container parent=menuItem.getParent();
			if (parent!=null && (parent instanceof JComponent) && !(menuItem instanceof JMenu && ((JMenu)menuItem).isTopLevelMenu())) {

				// Get widest text so far from parent, if no one exists null is
				// returned.  Compare the text widths, and adjust the r.width to
				// the widest.
				
				JComponent p=(JComponent)parent;
				Integer maxTextWidth=(Integer)p.getClientProperty(MAX_TEXT_WIDTH);
				Integer maxAccWidth=(Integer)p.getClientProperty(MAX_ACC_WIDTH);
				int maxTextValue=(maxTextWidth!=null) ? maxTextWidth.intValue() : 0;
				int maxAccValue=(maxAccWidth!=null) ? maxAccWidth.intValue() : 0;
				
				if (r.width<maxTextValue) {
					r.width=maxTextValue;
				} else {
					p.putClientProperty(MAX_TEXT_WIDTH,new Integer(r.width));
				}
				
				// Compare the accelarator widths. Add on the widest
				// accelerator.
				
				if (acceleratorRect.width>maxAccValue) {
					maxAccValue=acceleratorRect.width;
					p.putClientProperty(MAX_ACC_WIDTH,new Integer(acceleratorRect.width));
				}
				r.width+=(10+maxAccValue);
			}
			
			if ((!((menuItem instanceof JMenu) && (((JMenu)menuItem).isTopLevelMenu())))) r.width+=checkIconRect.width+defaultTextIconGap+defaultTextIconGap+arrowIconRect.width;
			r.width+=(2*defaultTextIconGap);
		}

		Insets insets=b.getInsets();
		if (insets!=null) {
			r.width+=insets.left+insets.right;
			r.height+=insets.top+insets.bottom;
		}
		if ((r.height % 2)==1) r.height++;
		return r.getSize();
	}

	// =======================================================================
	// Adapated from JGoodies. Looks up and answers the appropriate menu
	// item icon.

	private Icon getIcon(JMenuItem aMenuItem, Icon defaultIcon) {

		Icon icon=aMenuItem.getIcon();
		if (icon==null) return defaultIcon;
		ButtonModel model=aMenuItem.getModel();
		if (!model.isEnabled()) {
			return (model.isSelected()) ? aMenuItem.getDisabledSelectedIcon() : aMenuItem.getDisabledIcon();
		} else if (model.isPressed() && model.isArmed()) {
			Icon pressedIcon=aMenuItem.getPressedIcon();
			return (pressedIcon!=null) ? pressedIcon : icon;
		} else if (model.isSelected()) {
			Icon selectedIcon=aMenuItem.getSelectedIcon();
			return (selectedIcon!=null) ? selectedIcon : icon;
		} else {
			return icon;
		}
	}

	// =======================================================================

	private Icon getWrappedIcon(Icon icon) {

		if (!menuHasIcons()) return NULL_ICON;
		if (icon==null) return FILLER_ICON;
		return ((iconBorderEnabled)&&(getIcon(menuItem,null)!=null)) ? new MinimumSizedCheckIcon(icon,menuItem) : new MinimumSizedIcon(icon);
	}

	// =======================================================================

	private String layoutMenuItem(FontMetrics fm, String text, FontMetrics fmAccel, String acceleratorText,
																Icon icon, Icon checkIcon, Icon arrowIcon, int verticalAlignment, int horizontalAlignment,
																int verticalTextPosition, int horizontalTextPosition,	Rectangle viewRectangle,
																Rectangle iconRectangle, Rectangle textRectangle,	Rectangle acceleratorRectangle,
																Rectangle checkIconRectangle,	Rectangle arrowIconRectangle,	int textIconGap, int menuItemGap) {

		SwingUtilities.layoutCompoundLabel(menuItem,fm,text,icon,verticalAlignment,horizontalAlignment,verticalTextPosition,
																			 horizontalTextPosition,viewRectangle,iconRectangle,textRectangle,textIconGap);

		// Initialize the acceleratorText bounds rectangle textRect. If a
		// null or and empty String was specified we substitute "" here
		// and use 0,0,0,0 for acceleratorTextRect.

		if ((acceleratorText==null) || acceleratorText.equals("")) {
			acceleratorRectangle.width=acceleratorRectangle.height=0;
			acceleratorText=new String();
		} else {
			acceleratorRectangle.width=SwingUtilities.computeStringWidth(fmAccel,acceleratorText);
			acceleratorRectangle.height=fmAccel.getHeight();
		}
		
		boolean useCheckAndArrow=(!((menuItem instanceof JMenu) && (((JMenu)menuItem).isTopLevelMenu())));

		// Initialize the check and arrow icon bounds rectangle's width &
		// height.

		if (useCheckAndArrow) {
			if (checkIcon!=null) {
				checkIconRectangle.width=checkIcon.getIconWidth();
				checkIconRectangle.height=checkIcon.getIconHeight();
			} else {
				checkIconRectangle.width=checkIconRectangle.height=0;
			}
			if (arrowIcon!=null) {
				arrowIconRectangle.width=arrowIcon.getIconWidth();
				arrowIconRectangle.height=arrowIcon.getIconHeight();
			} else {
				arrowIconRectangle.width=arrowIconRectangle.height=0;
			}
		} 

		if ((menuItem instanceof JMenu)&&(!((JMenu)menuItem).isTopLevelMenu())) textRectangle.x-=2;

		// Fixed the computations in JGoodies; for some reason, if there
		// was no icon then the acceleratorRectangle.x and arrowIconRectangle.x 
		// would be computed incorrectly and shifted left...

		Rectangle labelRect=iconRectangle.union(textRectangle);
		if (menuItem.getComponentOrientation().isLeftToRight()) {
			textRectangle.x+=menuItemGap;
			iconRectangle.x+=menuItemGap;
			acceleratorRectangle.x=20+(viewRectangle.x+viewRectangle.width-arrowIconRectangle.width-menuItemGap-acceleratorRectangle.width);
			if (useCheckAndArrow) {
				checkIconRectangle.x=viewRectangle.x; 
				textRectangle.x+=menuItemGap+checkIconRectangle.width;
				iconRectangle.x+=menuItemGap+checkIconRectangle.width;
				arrowIconRectangle.x=8+(viewRectangle.x+viewRectangle.width-menuItemGap-arrowIconRectangle.width);
			} 
		} else {
			textRectangle.x-=menuItemGap;
			iconRectangle.x-=menuItemGap;
			acceleratorRectangle.x=viewRectangle.x+arrowIconRectangle.width+menuItemGap;
			if (useCheckAndArrow) {
				checkIconRectangle.x=viewRectangle.x+viewRectangle.width-checkIconRectangle.width;
				textRectangle.x-=menuItemGap+checkIconRectangle.width;
				iconRectangle.x-=menuItemGap+checkIconRectangle.width;
				arrowIconRectangle.x=viewRectangle.x+menuItemGap;
			}
		}

		// Align the accelerator text and the check and arrow icons
		// vertically with the center of the label rect.
		
		acceleratorRectangle.y=labelRect.y+(labelRect.height/2)-(acceleratorRectangle.height/2);
		if (useCheckAndArrow) {
			arrowIconRectangle.y=labelRect.y+(labelRect.height/2)-(arrowIconRectangle.height/2);
			checkIconRectangle.y=labelRect.y+(labelRect.height/2)-(checkIconRectangle.height/2);
		}
		return text;
	}

	// =======================================================================

	public void paintMenuItem(Graphics g, JComponent c,	Icon checkIcon, Icon arrowIcon,	Color background, Color foreground, int defaultTextIconGap) {

		JMenuItem b=(JMenuItem)c;
		ButtonModel model=b.getModel();
		int menuWidth=b.getWidth();
		int menuHeight=b.getHeight();
		Insets i=c.getInsets();

		resetRects();
		viewRect.setBounds(0,0,menuWidth,menuHeight);
		viewRect.x+=i.left;
		viewRect.y+=i.top;
		viewRect.width-=(i.right+viewRect.x);
		viewRect.height-=(i.bottom+viewRect.y);

		Font holdf=g.getFont();
		Font f=c.getFont();
		g.setFont(f);
		FontMetrics fm=g.getFontMetrics(f);
		FontMetrics fmAccel=g.getFontMetrics(acceleratorFont);

		// Get accelerator text

		KeyStroke accelerator=b.getAccelerator();
		String acceleratorText=new String();
		if (accelerator!=null) {
			int modifiers=accelerator.getModifiers();
			if (modifiers>0) {
				acceleratorText=KeyEvent.getKeyModifiersText(modifiers);
				acceleratorText+=ACCELERATOR_DELIM;
			}
			int keyCode=accelerator.getKeyCode();
			acceleratorText+=(keyCode!=0) ? KeyEvent.getKeyText(keyCode) : new Character(accelerator.getKeyChar()).toString();
		}

		Icon wrappedIcon=getWrappedIcon(getIcon(menuItem,checkIcon));
		Icon wrappedArrowIcon=new MinimumSizedIcon(arrowIcon);
		
		// Layout the text and icon

		String text=layoutMenuItem(fm,b.getText(),fmAccel,acceleratorText,null,wrappedIcon,wrappedArrowIcon,b.getVerticalAlignment(),
															 b.getHorizontalAlignment(),b.getVerticalTextPosition(),b.getHorizontalTextPosition(), 
															 viewRect,iconRect,textRect,acceleratorRect,checkIconRect,arrowIconRect,(b.getText()==null) ? 0 : defaultTextIconGap,
															 defaultTextIconGap);
		
		paintBackground(g,b,background); 
		Color holdc=g.getColor();		
		Graphics2D g2=(Graphics2D)g.create();
		g2.setComposite(ALPHA_MENU_AC);
		if (model.isArmed() || (c instanceof JMenu && model.isSelected())) g2.setColor(foreground);
		wrappedIcon.paintIcon(c,g2,checkIconRect.x,checkIconRect.y);
		g2.dispose();
				
		// Draw the text and accelerator
		
		if (text!=null) {
			View v=(View)c.getClientProperty(BasicHTML.propertyKey);
			if (v!=null) v.paint(g,textRect); else paintText(g,b,textRect,text); 
		}

		if ((acceleratorText!=null)&&(!acceleratorText.equals(""))) {
			int accOffset=0;
			Container parent=menuItem.getParent();
			if ((parent!=null)&&(parent instanceof JComponent)) {
				JComponent p=(JComponent)parent;
				Integer maxValueInt=(Integer)p.getClientProperty(MAX_ACC_WIDTH);
				int maxValue=(maxValueInt!=null) ? maxValueInt.intValue() : acceleratorRect.width;
				accOffset=maxValue-acceleratorRect.width; 				                                   
			}
			g.setFont(acceleratorFont);
			if (!model.isEnabled()) {
				if (disabledForeground!=null) {
					g.setColor(disabledForeground);
					TextRenderer.drawString(c,g,acceleratorText,acceleratorRect.x-accOffset,acceleratorRect.y+fmAccel.getAscent());
				} else {
					g.setColor(b.getBackground().brighter());
					TextRenderer.drawString(c,g,acceleratorText,acceleratorRect.x-accOffset,acceleratorRect.y+fmAccel.getAscent());
					g.setColor(b.getBackground().darker());
					TextRenderer.drawString(c,g,acceleratorText,acceleratorRect.x-accOffset-1,acceleratorRect.y+fmAccel.getAscent()-1);
				}
			} else {
				if (model.isArmed() || (c instanceof JMenu && model.isSelected())) {
					g.setColor(acceleratorSelectionForeground);
				} else {
					g.setColor(Color.white);
					// g.setColor(acceleratorForeground);
				}
				TextRenderer.drawString(c,g,acceleratorText,acceleratorRect.x-accOffset,acceleratorRect.y+fmAccel.getAscent());
			}
		}

		// Paint the Arrow

		if (arrowIcon!=null) {
			if (model.isArmed() || (c instanceof JMenu && model.isSelected())) g.setColor(foreground);
			if (!((menuItem instanceof JMenu) && (((JMenu)menuItem).isTopLevelMenu()))) wrappedArrowIcon.paintIcon(c,g,arrowIconRect.x,arrowIconRect.y);
		}

		g.setColor(holdc);
		g.setFont(holdf);
	}

	// =======================================================================

	private void paintText(Graphics g, JMenuItem aMenuItem, Rectangle textRectangle, String text) {

		ButtonModel model=aMenuItem.getModel();
		FontMetrics fm=g.getFontMetrics();
		int mnemIndex=aMenuItem.getDisplayedMnemonicIndex();

		if (!model.isEnabled()) {
			if (UIManager.get("MenuItem.disabledForeground") instanceof Color) {
				g.setColor(UIManager.getColor("MenuItem.disabledForeground"));
				TextRenderer.drawStringUnderlineCharAt(aMenuItem,g,text,mnemIndex,textRectangle.x,textRectangle.y+fm.getAscent());
			} else {
				g.setColor(aMenuItem.getBackground().brighter());
				TextRenderer.drawStringUnderlineCharAt(aMenuItem,(Graphics2D)g,text,mnemIndex,textRectangle.x,textRectangle.y+fm.getAscent());
				g.setColor(aMenuItem.getBackground().darker());
				TextRenderer.drawStringUnderlineCharAt(aMenuItem,(Graphics2D)g,text,mnemIndex,textRectangle.x-1,textRectangle.y+fm.getAscent()-1);
			}
		} else {
			if (model.isArmed()||(aMenuItem instanceof JMenu && model.isSelected())) g.setColor(selectionForeground); else g.setColor(DEFAULT_FOREGROUND);
			TextRenderer.drawStringUnderlineCharAt(aMenuItem,(Graphics2D)g,text,mnemIndex,textRectangle.x,textRectangle.y+fm.getAscent());
		}
	}

	// =======================================================================

	protected boolean menuHasIcons() {

		Container parent=menuItem.getParent();
		if (parent!=null && (parent instanceof JPopupMenu)) {
			JPopupMenu pm=(JPopupMenu)parent;
			MenuElement[] elements=pm.getSubElements();
			for (int loop=0; loop<elements.length; loop++) {
				MenuElement me=elements[loop];
				if (me instanceof JMenuItem) {
					JMenuItem mi=(JMenuItem)me;
					if ((mi.getIcon()!=null)||(mi instanceof JCheckBoxMenuItem)||(mi instanceof JRadioButtonMenuItem)) return true;
				}
			}
		}
		return false;
	}

	// =======================================================================

	protected void paintBackground(Graphics g, JMenuItem jmi, Color bgColor) {
		
		ButtonModel model=jmi.getModel();
		int menuWidth=jmi.getWidth();

		int menuHeight=jmi.getHeight();
		Graphics2D g2=(Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);			
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);			
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);			
		g2.setComposite(ALPHA_MENU_AC);

		if (jmi.isOpaque()) {
			g2.setColor((model.isArmed()|| (jmi instanceof JMenu && model.isSelected())) ? bgColor : jmi.getBackground());
			g2.fillRect(0,0,menuWidth,menuHeight);
		}	else if (model.isArmed() || (jmi instanceof JMenu &&  model.isSelected())) {
			g2.setColor(Color.darkGray);
			g2.fillRoundRect(0,0,menuWidth,menuHeight,5,5);
			if ((jmi instanceof JMenu) && (((JMenu)jmi).isTopLevelMenu())) {
				g2.setColor(bgColor.darker());
				g2.drawLine(0,0,0,menuHeight);
				g2.setColor(Color.white);
				g2.drawLine(menuWidth-1,0,menuWidth-1,menuHeight);
			}
		} 
		g2.dispose();
	}

	// =======================================================================

  private static class NullIcon implements Icon {

		public int getIconWidth()	{return 0;}
		public int getIconHeight() {return 0;}
		public void paintIcon(Component c, Graphics g, int x, int y) {}
	}
}
