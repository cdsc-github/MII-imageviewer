/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import java.io.Serializable;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import javax.swing.plaf.UIResource;

// =======================================================================

public final class IconFactory {

	protected static final AlphaComposite ALPHA_MENU_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.8f);
	protected static final Stroke FOCUS_STROKE=new BasicStroke(2);

	private static Icon checkBoxIcon=null;
	private static Icon checkBoxMenuItemIcon=null;
	private static Icon comboBoxButtonIcon=null;
	private static Icon comboBoxButtonIconDark=null;
	private static Icon radioButtonIcon=null;
	private static Icon radioButtonMenuItemIcon=null;
		
	private IconFactory() {}

	// =======================================================================

	private static void drawCheckBoxMark(Graphics2D g2, boolean enabled, int x, int y, int width, int height) {

		g2.setColor(enabled	? UIManager.getColor("CheckBox.check") : ImageViewerLookAndFeel.getControlDisabled());
		int right=x+width;
		int bottom=y+height;
		int startY=y+height/3;
		int turnX =x+width/2-2;
		g2.drawLine(x,startY,turnX,bottom-3);
		g2.drawLine(x,startY+1,turnX,bottom-2);
		g2.drawLine(x,startY+2,turnX,bottom-1);
		g2.drawLine(turnX+1,bottom-2,right,y);
		g2.drawLine(turnX+1,bottom-1,right,y+1);
		g2.drawLine(turnX+1,bottom,right,y+2);
	}
	
	private static void drawCheckBoxFocus(Graphics2D g2, int x, int y, int width, int height) {
		
		g2.setPaint(new GradientPaint(x,y,ImageViewerLookAndFeel.getFocusColor().brighter(),width,height,ImageViewerLookAndFeel.getFocusColor()));
		g2.drawRect(x,y,width,height);
		g2.drawRect(x+1,y+1,width-2,height-2);
	}

	private static void drawRadioButtonMark(Graphics2D g2, Component c, boolean enabled, int x, int y, int w, int h) {

		g2.translate(x,y);
		if (enabled) {
			g2.setColor(UIManager.getColor("RadioButton.check"));
			g2.fillOval(0,0,w,h);
		} else {
			g2.setColor(ImageViewerLookAndFeel.getControlDisabled());
			g2.fillOval(0,0,w,h);
		}
		g2.translate(-x,-y);
	}

	private static void drawRadioButtonFocus(Graphics2D g2, int x, int y, int w, int h) {

		g2.setPaint(new GradientPaint(x,y,ImageViewerLookAndFeel.getFocusColor().brighter(),w,h,ImageViewerLookAndFeel.getFocusColor()));
		Stroke stroke=g2.getStroke();
		g2.setStroke(FOCUS_STROKE);
		g2.drawOval(x,y,w,h);
		g2.setStroke(stroke);
	}

	// =======================================================================

	private static class CheckBoxIcon implements Icon, UIResource, Serializable {

		private static final int SIZE=11;

		public int getIconWidth()	{return SIZE;}
		public int getIconHeight() {return SIZE;}

			public void paintIcon(Component c, Graphics g, int x, int y) {

			JCheckBox cb=(JCheckBox)c;
			ButtonModel model=cb.getModel();

			Graphics2D g2=(Graphics2D)g;
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(model.isEnabled() ? ImageViewerLookAndFeel.getControlDarkShadow()	: ImageViewerLookAndFeel.getControlDisabled());
			g2.drawRect(x,y,SIZE-1,SIZE-1);
			Color upperLeft=(model.isPressed()) ? ImageViewerLookAndFeel.getControlShadow() : ImageViewerLookAndFeel.getControl();
			Color lowerRight=(model.isPressed()) ? ImageViewerLookAndFeel.getControlHighlight() : ImageViewerLookAndFeel.getControlHighlight().brighter();
			g2.setPaint(new GradientPaint(x+1,y+1,upperLeft,x+SIZE-1,y+SIZE-1,lowerRight));
			g2.fillRect(x+1,y+1,SIZE-2,SIZE-2);
			if (model.isEnabled() && (model.isArmed() && !(model.isPressed()))) drawCheckBoxFocus(g2,x+1,y+1,SIZE-3,SIZE-3);
			if (model.isSelected()) drawCheckBoxMark(g2,model.isEnabled(),x+3,y+3,SIZE-7,SIZE-7);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
		}
	}

	// =======================================================================
	
	private static class CheckBoxMenuItemIcon implements Icon, UIResource, Serializable {

		// Adapted from the JGoodies Plastic L&F for XP, but forces the
		// use of an alpha composite value for menu items in our look and
		// feel.
    	
		private static final int SIZE=11;
		
		public int getIconWidth()	{return SIZE;}
		public int getIconHeight() {return SIZE;}
		
		public void paintIcon(Component c, Graphics g, int x, int y) {

			JMenuItem b=(JMenuItem)c;
			ButtonModel model=b.getModel();
			
			Graphics2D g2=(Graphics2D)g;
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			Composite currentAC=g2.getComposite();
			g2.setComposite(ALPHA_MENU_AC);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(model.isEnabled() ? ImageViewerLookAndFeel.getControlDarkShadow()	: ImageViewerLookAndFeel.getControlDisabled());
			g2.drawRect(x,y,SIZE-1,SIZE-1);
			Color upperLeft=(model.isPressed()) ? ImageViewerLookAndFeel.getControlShadow() : ImageViewerLookAndFeel.getControl();
			Color lowerRight=(model.isPressed()) ? ImageViewerLookAndFeel.getControlHighlight() : ImageViewerLookAndFeel.getControlHighlight().brighter();
			g2.setPaint(new GradientPaint(x+1,y+1,upperLeft,x+SIZE-1,y+SIZE-1,lowerRight));
			g2.fillRect(x+1,y+1,SIZE-2,SIZE-2);
			if (model.isEnabled() && (model.isArmed() && !(model.isPressed()))) drawCheckBoxFocus(g2,x+1,y+1,SIZE-3,SIZE-3);
			if (model.isSelected()) drawCheckBoxMark(g2,model.isEnabled(),x+3,y+2,SIZE-6,SIZE-6);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
			g2.setComposite(currentAC);
		}
	}

	// =======================================================================

  private static class RadioButtonIcon implements Icon, UIResource, Serializable {

		private static final int SIZE=11;

		public int getIconWidth()  {return SIZE;}
		public int getIconHeight() {return SIZE;}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			Graphics2D g2=(Graphics2D)g;
			AbstractButton b=(AbstractButton)c;
			ButtonModel model=b.getModel();
			
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			Color upperLeft=(model.isPressed()) ? ImageViewerLookAndFeel.getControlShadow() : ImageViewerLookAndFeel.getControl();
			Color lowerRight=(model.isPressed()) ? ImageViewerLookAndFeel.getControlHighlight() : ImageViewerLookAndFeel.getControlHighlight().brighter();
			g2.setPaint(new GradientPaint(x,y,upperLeft,x+SIZE-1,y+SIZE-1,lowerRight));
			g2.fillOval(x,y,SIZE-1,SIZE-1);
			if (model.isArmed() && !(model.isPressed())) drawRadioButtonFocus(g2,x+1,y+1,SIZE-3,SIZE-3);
			if (model.isSelected()) drawRadioButtonMark(g2,c,model.isEnabled(),x+4,y+4,SIZE-8,SIZE-8);
			g2.setColor(model.isEnabled() ? ImageViewerLookAndFeel.getControlDarkShadow() : ImageViewerLookAndFeel.getControlDisabled());
			g2.drawOval(x,y,SIZE-1,SIZE-1);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
		}
	}

	// =======================================================================

  private static class RadioButtonMenuItemIcon implements Icon, UIResource, Serializable {

		private static final int SIZE=11;

		public int getIconWidth()  {return SIZE;}
		public int getIconHeight() {return SIZE;}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			Graphics2D g2=(Graphics2D)g;
			AbstractButton b=(AbstractButton)c;
			ButtonModel model=b.getModel();
			
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			Composite currentAC=g2.getComposite();
			g2.setComposite(ALPHA_MENU_AC);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			Color upperLeft=(model.isPressed()) ? ImageViewerLookAndFeel.getControlShadow() : ImageViewerLookAndFeel.getControl();
			Color lowerRight=(model.isPressed()) ? ImageViewerLookAndFeel.getControlHighlight() : ImageViewerLookAndFeel.getControlHighlight().brighter();
			g2.setPaint(new GradientPaint(x,y,upperLeft,x+SIZE-1,y+SIZE-1,lowerRight));
			g2.fillOval(x,y,SIZE-1,SIZE-1);
			if (model.isArmed() && !(model.isPressed())) drawRadioButtonFocus(g2,x+1,y+1,SIZE-3,SIZE-3);
			if (model.isSelected()) drawRadioButtonMark(g2,c,model.isEnabled(),x+3,y+3,SIZE-6,SIZE-6);
			g2.setColor(model.isEnabled() ? ImageViewerLookAndFeel.getControlDarkShadow() : ImageViewerLookAndFeel.getControlDisabled());
			g2.drawOval(x,y,SIZE-1,SIZE-1);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
			g2.setComposite(currentAC);
		}
	}

	// =======================================================================

	public static Icon getCheckBoxIcon() {if (checkBoxIcon==null) checkBoxIcon=new CheckBoxIcon(); return checkBoxIcon;}
	public static Icon getCheckBoxMenuItemIcon() {if (checkBoxMenuItemIcon==null) checkBoxMenuItemIcon=new CheckBoxMenuItemIcon(); return checkBoxMenuItemIcon;}
	public static Icon getRadioButtonIcon() {if (radioButtonIcon==null) radioButtonIcon=new RadioButtonIcon(); return radioButtonIcon;}
	public static Icon getRadioButtonMenuItemIcon() {if (radioButtonMenuItemIcon==null) radioButtonMenuItemIcon=new RadioButtonMenuItemIcon(); return radioButtonMenuItemIcon;}

	public static Icon getComboBoxButtonIcon() {if (comboBoxButtonIcon==null) comboBoxButtonIcon=new ComboBoxButtonIcon(Color.white); return comboBoxButtonIcon;}
	public static Icon getComboBoxButtonIconDark() {if (comboBoxButtonIconDark==null) comboBoxButtonIconDark=new ComboBoxButtonIcon(Color.gray); return comboBoxButtonIconDark;}

	// =======================================================================

	private static class ComboBoxButtonIcon implements Icon, Serializable {

		Color iconColor=null;

		public ComboBoxButtonIcon(Color iconColor) {this.iconColor=iconColor;}

		public void paintIcon(Component c, Graphics g, int x, int y) {

			JComponent component=(JComponent)c;
			int iconWidth=getIconWidth();
		
			Graphics2D g2=(Graphics2D)g;
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.translate(x,y);
			g2.setColor(component.isEnabled() ? iconColor : ImageViewerLookAndFeel.getControlShadow());
			g2.drawLine(0,0,iconWidth-1,0);
			g2.drawLine(1,1,1+(iconWidth-3),1);
			g2.drawLine(2,2,2+(iconWidth-5),2);
			g2.drawLine(3,3,3+(iconWidth-7),3);
			g2.translate(-x,-y);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
		}
    
		public int getIconWidth()  {return 7;}
		public int getIconHeight() {return 4;}
	}
}
