/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import javax.swing.plaf.ComponentUI;

import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

import javax.swing.plaf.metal.MetalComboBoxUI;
import javax.swing.plaf.metal.MetalScrollBarUI;

// =======================================================================
// Adapated from JGoodies plastic look & feel. Override several methods
// to handle better dimensions for imageviewer, etc.

public class ComboBoxUI extends MetalComboBoxUI {

	private static final Insets EMPTY_INSETS=new Insets(0,0,2,0);
	private static final Border RENDERER_BORDER=new EmptyBorder(1,2,4,2);

	// =======================================================================
	
	public static ComponentUI createUI(JComponent b) {return new ComboBoxUI();}

	// =======================================================================

  protected JButton createArrowButton() {return new ComboBoxButton(comboBox,IconFactory.getComboBoxButtonIcon(),comboBox.isEditable(),currentValuePane,listBox);}
	protected LayoutManager createLayoutManager() {return new ComboBoxLayoutManager();}
	protected ComboBoxPopup createPopup() {return new ComboBoxPopup(comboBox);}
	protected ComboBoxEditor createEditor() {return new ComboBoxEditor.UIResource();}

	protected ListCellRenderer createRenderer() {
		BasicComboBoxRenderer renderer=new BasicComboBoxRenderer.UIResource();
		renderer.setBorder(RENDERER_BORDER);
	  return renderer;
  }

	public PropertyChangeListener createPropertyChangeListener() {return new ComboBoxPropertyChangeListener();}

	private int getEditableButtonWidth() {return UIManager.getInt("ScrollBar.width");}

	// =======================================================================

	public Dimension getMinimumSize(JComponent c) {
		
		if (!isMinimumSizeDirty) return new Dimension(cachedMinimumSize);

		Dimension size=null;

		if ((!comboBox.isEditable())&&(arrowButton!=null)&&(arrowButton instanceof ComboBoxButton)) {

			ComboBoxButton button=(ComboBoxButton)arrowButton;
			Insets buttonInsets=button.getInsets();
			Insets buttonMargin=button.getMargin();
			Insets insets=comboBox.getInsets();
			size=getDisplaySize();            
			size.width+=insets.left+insets.right;
			size.width+=buttonInsets.left+buttonInsets.right;
			size.width+=buttonMargin.left+buttonMargin.right;
			size.width+=button.getComboIcon().getIconWidth();
			size.height+=3+insets.top+insets.bottom;
			size.height+=buttonInsets.top+buttonInsets.bottom;

		} else if (comboBox.isEditable() && arrowButton!=null && editor!=null) {

			size=getDisplaySize();
			Insets insets=comboBox.getInsets();
			Insets editorInsets=(editor instanceof JComponent) ? ((JComponent)editor).getInsets() : EMPTY_INSETS;;
			int buttonWidth=getEditableButtonWidth();
			size.width+=insets.left+insets.right;
			size.width+=editorInsets.left+editorInsets.right-1;
			size.width+=buttonWidth;
			size.height+=insets.top+insets.bottom;
		} else {
			size=super.getMinimumSize(c);
		}

		cachedMinimumSize.setSize(size.width,size.height);
		isMinimumSizeDirty=false;
		return new Dimension(cachedMinimumSize);
	}

	// =======================================================================

	public void update(Graphics g, JComponent c) {

		if (c.isOpaque()) {
			g.setColor(c.getBackground());
			g.fillRect(0,0,c.getWidth(),c.getHeight());
			Container parent=c.getParent();
			boolean isToolBarComboBox=((parent!=null)&& (parent instanceof JToolBar || parent.getParent() instanceof JToolBar));
			if (isToolBarComboBox) c.setOpaque(false);
		}
		paint(g,c);
	}

	// =======================================================================
	// Stretch the arrowButton by 2 in the width to compensate for the
	// imageViewer button insets.

	private final class ComboBoxLayoutManager	extends MetalComboBoxUI.MetalComboBoxLayoutManager {

		public void layoutContainer(Container parent) {

			JComboBox cb=(JComboBox)parent;
			if (!cb.isEditable()) {
				super.layoutContainer(parent);
				Rectangle r=arrowButton.getBounds();
				arrowButton.setBounds(r.x,r.y,r.width,r.height);
				return;
			}

			Insets insets=getInsets();
			int width=cb.getWidth();
			int height=cb.getHeight();
			int buttonWidth=getEditableButtonWidth();
			int buttonHeight=height-(insets.top+insets.bottom);

			if (arrowButton!=null) arrowButton.setBounds((cb.getComponentOrientation().isLeftToRight()) ? (width-(insets.right+buttonWidth)) : insets.left,
																									 insets.top,buttonWidth,buttonHeight);
			if (editor!=null) editor.setBounds(rectangleForCurrentValue());
		}
	}

	// =======================================================================

	private final class ComboBoxPropertyChangeListener extends BasicComboBoxUI.PropertyChangeHandler {

		public void propertyChange(PropertyChangeEvent e) {

			super.propertyChange(e);
			String propertyName=e.getPropertyName();
			if (propertyName.equals("editable")) {
				ComboBoxButton button=(ComboBoxButton)arrowButton;
				button.setIconOnly(comboBox.isEditable());
				comboBox.repaint();
			} else if (propertyName.equals("background")) {
				Color color=(Color)e.getNewValue();
				arrowButton.setBackground(color);
				listBox.setBackground(color);
			} else if (propertyName.equals("foreground")) {
				Color color=(Color)e.getNewValue();
				arrowButton.setForeground(color);
				listBox.setForeground(color);
			}
		}
	}

	// =======================================================================

	private static final class ComboBoxPopup extends BasicComboPopup {

		private ComboBoxPopup(JComboBox combo) {super(combo);}

		protected void configureList() {
			super.configureList();
			list.setForeground(UIManager.getColor("MenuItem.foreground"));
			list.setBackground(UIManager.getColor("MenuItem.background"));
		}

		protected void configureScroller() {

			super.configureScroller();
			scroller.getVerticalScrollBar().putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP,Boolean.FALSE);
		}
	}
}
