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
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.metal.MetalScrollButton;

import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.painter.ShapePainter;

// =======================================================================
// Loosely adapted from JGoodies L&F

public class SpinnerUI extends BasicSpinnerUI {

  private static final BasicArrowButtonHandler nextButtonHandler=new BasicArrowButtonHandler("increment",true);
  private static final BasicArrowButtonHandler previousButtonHandler=new BasicArrowButtonHandler("decrement",false);

	private static final int spinnerArrowWidth=UIManager.getInt("Spinner.arrowWidth");
	private static final Color spinnerHighlight=UIManager.getColor("Spinner.highlightColor");
	private static final Color spinnerShadow=UIManager.getColor("Spinner.shadowColor");

	// =======================================================================

  public static ComponentUI createUI(JComponent b) {return new SpinnerUI();}

	protected void installDefaults() {
		super.installDefaults();
		ImageViewerLookAndFeel.installProperty(spinner,"opaque",Boolean.FALSE);
	}

  protected Component createNextButton() {return new SpinnerArrowButton(SwingConstants.NORTH,nextButtonHandler);}
  protected Component createPreviousButton() {return new SpinnerArrowButton(SwingConstants.SOUTH,previousButtonHandler);}

	// =======================================================================

  protected LayoutManager createLayout() {return new BasicSpinnerLayout();}

  protected JComponent createEditor() {

    JComponent editor=spinner.getEditor();
    configureEditorBorder(editor);
    return editor;
  }

	// =======================================================================

  protected void replaceEditor(JComponent oldEditor, JComponent newEditor) {

    spinner.remove(oldEditor);
    configureEditorBorder(newEditor);
    spinner.add(newEditor,"Editor");
  }

	// =======================================================================

  private void configureEditorBorder(JComponent editor) {

    if ((editor instanceof JSpinner.DefaultEditor)) {
      JSpinner.DefaultEditor defaultEditor=(JSpinner.DefaultEditor)editor;
      JTextField editorField=defaultEditor.getTextField();
      Insets insets=UIManager.getInsets("Spinner.defaultEditorInsets");
      editorField.setBorder(new EmptyBorder(insets));
    } else if ((editor instanceof JPanel)&&(editor.getBorder()==null)&&(editor.getComponentCount()>0)) {
      JComponent editorField=(JComponent)editor.getComponent(0);
      Insets insets=UIManager.getInsets("Spinner.defaultEditorInsets");
      editorField.setBorder(new EmptyBorder(insets));
    }
  }

	public void paint(Graphics g, JComponent c) {

		Graphics2D g2=(Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
		g2.setColor(Color.gray);
		g2.fillRoundRect(0,0,c.getWidth(),c.getHeight(),6,6);
		super.paint(g2,c);
	}

	// =======================================================================

  private static final class SpinnerArrowButton extends MetalScrollButton {

    private SpinnerArrowButton(int direction, BasicArrowButtonHandler handler) {

      super(direction,spinnerArrowWidth,true);
      addActionListener(handler);
      addMouseListener(handler);
			setOpaque(false);
			setBorderPainted(false);
    }

		// =======================================================================

    protected int calculateArrowHeight(int height, int width) {int arrowHeight=Math.min((height-4)/3,(width-4)/3); return Math.max(arrowHeight,3);}
    protected int calculateArrowOffset() {return 1;}

    protected boolean isPaintingNorthBottom() {return true;}

		// =======================================================================
		
		public void paint(Graphics g) {
			
			boolean leftToRight=getComponentOrientation().isLeftToRight();
			boolean isEnabled=getParent().isEnabled();
			boolean isPressed=getModel().isPressed();
		
			Color arrowColor=(isEnabled) ? UIManager.getColor("Spinner.arrowEnabled") : UIManager.getColor("Spinner.arrowDisabled");
			
			int width=getWidth();
			int height=getHeight();
			int w=width;
			int h=height;
			int arrowHeight=calculateArrowHeight(height,width);
			int arrowOffset=calculateArrowOffset();
			boolean paintNorthBottom=isPaintingNorthBottom();
			
			if (getDirection()==NORTH) {
				paintNorth(g,leftToRight,isEnabled,arrowColor,isPressed,width,height,w,h,arrowHeight,arrowOffset,paintNorthBottom);
			} else if (getDirection()==SOUTH) {
				paintSouth(g,leftToRight,isEnabled,arrowColor,isPressed,width,height,w,h,arrowHeight,arrowOffset);
			} 
			
			ButtonModel buttonModel=getModel();
			if (buttonModel.isArmed() && buttonModel.isPressed() || buttonModel.isSelected())	return;
			if (getDirection()==EAST) width-=4; else if (getDirection()==SOUTH) height-=4;
			Rectangle r=new Rectangle(2,2,width,height);
			boolean isHorizontal=(getDirection()==EAST || getDirection()==WEST);
		}

		// =======================================================================

		protected void paintSouth(Graphics g, boolean leftToRight, boolean isEnabled, Color arrowColor, boolean isPressed, int width,
															int height, int w, int h, int arrowHeight, int arrowOffset) {

			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			RoundRectangle2D.Double rr=new RoundRectangle2D.Double(1,1,width-2,height-2,2,2);
			Area a=new Area(rr);
			a.add(new Area(new Rectangle2D.Double(0,0,10,height-2)));
			a.add(new Area(new Rectangle2D.Double(width-11,0,11,4)));
			
			g2.setPaint(new GradientPaint(0,0,spinnerShadow,0,height,spinnerHighlight));
			g2.fill(a);
			g2.setColor(Color.darkGray);
			g2.drawLine(1,1,1,height-2);
			g2.drawLine(1,1,width-2,1);
			g2.setColor(Color.black);
			g2.drawLine(0,0,0,height-1);

			// Draw the border on the button; it gets wiped out for some reason...

			Area border=new Area(new RoundRectangle2D.Double(0,0,width-1,height-1,6,6));
			border.add(new Area(new Rectangle2D.Double(0,0,6,height-1)));
			border.add(new Area(new Rectangle2D.Double(0,0,width-1,6)));
			g2.draw(border);

			// Draw the arrow

			g2.setColor(arrowColor);
			int startY=(((h+0)-arrowHeight)/2)+arrowHeight-1; 
			int startX=w/2;
			for (int line=0; line<arrowHeight; line++) g.fillRect(startX-line-arrowOffset,startY-line,2*(line+1),1);
		}

		// =======================================================================

		protected void paintNorth(Graphics g, boolean leftToRight, boolean isEnabled, Color arrowColor, boolean isPressed,
															int width,int height,int w,int h,int arrowHeight,int arrowOffset, boolean paintBottom) {

			Graphics2D g2=(Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			RoundRectangle2D.Double rr=new RoundRectangle2D.Double(1,1,width-2,height-2,2,2);
			Area a=new Area(rr);
			a.add(new Area(new Rectangle2D.Double(0,0,10,height-2)));
			a.add(new Area(new Rectangle2D.Double(width-12,height-6,10,4)));
			
			g2.setPaint(new GradientPaint(0,0,spinnerHighlight,0,height,spinnerShadow));
			g2.fill(a);
			g2.setColor(Color.black);
			g2.drawLine(0,0,width-4,0);
			g2.setColor(Color.darkGray);
			g2.drawLine(1,1,1,height-1);
			g2.setColor(Color.black);
			g2.drawLine(0,height-1,width,height-1);
			g2.drawLine(0,0,0,height-1);
			Area border=new Area(new RoundRectangle2D.Double(0,0,width-1,height-1,6,6));
			border.add(new Area(new Rectangle2D.Double(0,0,6,height-1)));
			border.add(new Area(new Rectangle2D.Double(0,height-6,width-1,5)));
			g2.draw(border);

			// Draw the arrow

			g2.setColor(arrowColor);
			int startY=((h+1)-arrowHeight)/2;
			int startX=w/2;
			for (int line=0; line<arrowHeight; line++) g2.fillRect(startX-line-arrowOffset,startY+line,2*(line+1),1);
		}
	}
}
