/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.RoundRectangle2D;

import javax.swing.CellRendererPane;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.ShapePainter;
import org.jdesktop.swingx.util.Resize;

// =======================================================================

public class ComboBoxButton extends JButton {

	protected static final int LEFT_INSET=2;
	protected static final int RIGHT_INSET=2;
	protected static final int BUTTON_WIDTH=UIManager.getInt("ScrollBar.width");

	// =======================================================================

	protected JList listBox;
	protected CellRendererPane rendererPane;

	protected boolean iconOnly=false, borderPaintsFocus;
	protected JComboBox comboBox=null;
	protected Icon comboIcon=null;

	public ComboBoxButton(JComboBox comboBox, Icon comboIcon, boolean iconOnly, CellRendererPane rendererPane, JList listBox) {

		super("");
		setModel(new DefaultButtonModel() {public void setArmed(boolean armed) {super.setArmed(isPressed() || armed);}});
		this.comboBox=comboBox;
		this.comboIcon=comboIcon;
		this.iconOnly =iconOnly;
		this.rendererPane=rendererPane;
		this.listBox=listBox;
		setEnabled(comboBox.isEnabled());
		setFocusable(false);
		setRequestFocusEnabled(comboBox.isEnabled());
		setRolloverEnabled(false);
		setMargin(new Insets(0,LEFT_INSET,0,RIGHT_INSET));
		borderPaintsFocus=UIManager.getBoolean("ComboBox.borderPaintsFocus");
	}

	// =======================================================================

	public JComboBox getComboBox() {return comboBox;}
	public Icon getComboIcon() {return comboIcon;}
	public boolean isIconOnly() {return iconOnly;}
    
	public void setComboBox(JComboBox cb) {comboBox=cb;}
	public void setComboIcon(Icon i) {comboIcon=i;}
	public void setIconOnly(boolean b) {iconOnly=b;}

	public boolean isFocusTraversable() {return false;}

	// =======================================================================

	public void setEnabled(boolean enabled) {

		super.setEnabled(enabled);
		if (enabled) {
			setBackground(comboBox.getBackground());
			setForeground(comboBox.getForeground());
		} else {
			setBackground(UIManager.getColor("ComboBox.disabledBackground"));
			setForeground(UIManager.getColor("ComboBox.disabledForeground"));
		}
	}

	// =======================================================================
	// A point of frustration...grr! Compensate for a pixel offset of 1
	// that occurs somehwere in the pipeline...

	public int getWidth() {return super.getWidth()-1;} 

	// =======================================================================

	protected void paintComboBoxBackground(Graphics g, Insets insets, int width, int height) {

		int buttonX=getWidth()-BUTTON_WIDTH;
		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.setColor(UIManager.getColor("ComboBox.background"));
		RoundRectangle2D rr=new RoundRectangle2D.Double(0,insets.top,getWidth()-2,height,6,6); 
		g2.fill(rr);

		RoundRectangle2D.Double r2=new RoundRectangle2D.Double(0,insets.top,getWidth(),height,6,6);
		ShapePainter sp=new ShapePainter(rr,getBackground());
		sp.setAntialiasing(true);
		sp.setStyle(ShapePainter.Style.FILLED);
		GlossPainter gp1=new GlossPainter();
		gp1.setPaint(new Color(1.0f,1.0f,1.0f,0.2f));
		gp1.setPosition(GlossPainter.GlossPosition.TOP);
		gp1.setAntialiasing(true);
		CompoundPainter cp=new CompoundPainter(sp,gp1); 
		cp.setAntialiasing(true);
		Shape oldClip=g2.getClip();
		g2.setClip(r2);
		cp.paint(g2,this,getWidth(),height+1);
		g2.setClip(oldClip);
		g.setColor(UIManager.getColor("ComboBox.background"));
		g.drawLine(buttonX-2,insets.top,buttonX-1,height+1);
		g.setColor(Color.gray);
		g.drawLine(buttonX-1,insets.top,buttonX,height+1);
	}

	protected void paintComboBoxArrowIcon(Graphics g, int x, int y) {comboIcon.paintIcon(this,g,x+2,y+1);}

	// =======================================================================
	// Override the default paint behavior for a JButton.  Note that the
	// *entire* JComboBox region is really a button, so it needs special
	// painting.

	public void paintComponent(Graphics g) {

		Insets insets=getInsets();
		int width=getWidth()-(insets.left+insets.right);
		int height=getHeight()-(insets.top+insets.bottom);
		if ((height<=0)||(width<=0)) return;
		
		boolean leftToRight=comboBox.getComponentOrientation().isLeftToRight();

		int left=insets.left;
		int top=insets.top;
		int right=left+(width-1);
		int iconWidth=0;
		int iconLeft=(leftToRight) ? right : left;

		// Paint the icon with a specific button background.  Add
		// separator lines.

		if (comboIcon!=null) {

			iconWidth=comboIcon.getIconWidth();
			int iconHeight=comboIcon.getIconHeight();
			int iconTop=0;
			if (iconOnly) {
				iconLeft=(getWidth()-iconWidth)/2;
				iconTop=(getHeight()-iconHeight)/2;
			} else {
				iconLeft=(leftToRight) ? ((left+(width-1))-iconWidth) : left;
				iconTop=(getHeight()-iconHeight)/2;
			}
			
			if (!getModel().isPressed()) paintComboBoxBackground(g,insets,width,height);
			paintComboBoxArrowIcon(g,iconLeft,iconTop);
		}

		// Let the renderer paint

		if ((!iconOnly)&&(comboBox!=null)) {

			ListCellRenderer renderer=comboBox.getRenderer();
			boolean renderPressed=getModel().isPressed();
			Component c=renderer.getListCellRendererComponent(listBox,comboBox.getSelectedItem(),-1,renderPressed,false);
			c.setFont(rendererPane.getFont());

			if (model.isArmed() && model.isPressed()) {
				if (isOpaque()) c.setBackground(UIManager.getColor("Button.select"));
				c.setForeground(comboBox.getForeground());
			} else if (!comboBox.isEnabled()) {
				if (isOpaque()) c.setBackground(UIManager.getColor("ComboBox.disabledBackground"));
				c.setForeground(UIManager.getColor("ComboBox.disabledForeground"));
			} else {
				c.setForeground(comboBox.getForeground());
				c.setBackground(comboBox.getBackground());
			}

			int cWidth=width-(insets.right+iconWidth);

			// Fix for 4238829: should lay out the JPanel.

			boolean shouldValidate=(c instanceof JPanel);
			int x=(leftToRight) ? left : (width-(left+iconWidth));
			int myHeight=getHeight()-LEFT_INSET-RIGHT_INSET-1;

			if (!(c instanceof JComponent)) {
				rendererPane.paintComponent(g,c,this,x,top+2,cWidth,myHeight,shouldValidate);
			} else if (!c.isOpaque()) {
				rendererPane.paintComponent(g,c,this,x,top+2,cWidth,myHeight,shouldValidate);
			} else {
				JComponent component=(JComponent) c;
				boolean hasBeenOpaque=component.isOpaque();
				component.setOpaque(false);
				rendererPane.paintComponent(g,c,this,x,top+2,cWidth,myHeight,shouldValidate);
				component.setOpaque(hasBeenOpaque);
			}
		}

		if (comboIcon!=null) {
			boolean hasFocus=comboBox.hasFocus();
			if (!borderPaintsFocus && hasFocus) {
				g.setColor(Color.gray);
				int x=LEFT_INSET;
				int y=LEFT_INSET;
				int w=getWidth()-LEFT_INSET-RIGHT_INSET;
				int h=getHeight()-LEFT_INSET-RIGHT_INSET;
				g.drawRect(x,y,w-1,h-1);
			}
		}
	}
}
