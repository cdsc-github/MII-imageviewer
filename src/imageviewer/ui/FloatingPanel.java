/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;

import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.painter.Painter;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imageviewer.ui.swing.TranslucentPanel;
import imageviewer.ui.swing.border.CurvedBorder;

// =======================================================================
// Make a translucent panel, and place the title panel at the top, and
// the argument panel in the center.  Set the location as a default
// (for now), and put a curved border around it.  Next, throw one's
// hands up in despair.  Add mouse listeners to this panel so it can
// be moved (i.e., dragged) by the user to change its location.

public class FloatingPanel extends TranslucentPanel implements ActionListener {

	protected static final Cursor RESIZE_CURSOR=new Cursor(Cursor.SE_RESIZE_CURSOR);
	protected static final Cursor DEFAULT_CURSOR=new Cursor(Cursor.DEFAULT_CURSOR);
	protected static final Color PANEL_COLOR=UIManager.getColor("FloatingPanel.background");
	protected static final Painter TITLE_PAINTER=(Painter)UIManager.get("FloatingPanel.titlePainter");

	protected static final Icon CLOSE_ICON=UIManager.getIcon("FloatingPanel.closeIcon");
	protected static final Icon CLOSE_ROLLOVER_ICON=UIManager.getIcon("FloatingPanel.closeRolloverIcon");

	// =======================================================================

	boolean canResize=false;
	JComponent floatingComponent=null;

	public FloatingPanel(JComponent component, String title) {this(component,title,true,false);}
	public FloatingPanel(JComponent component, String title, boolean hasCloseButton) {this(component,title,true,false);}

	public FloatingPanel(JComponent component, String title, boolean hasCloseButton, boolean canResize) {

		// Set up a title panel that has the title in the upper-left hand
		// corner, and a close button on the right. A separator is placed
		// below it.

		super(new BorderLayout(0,5));
		setBackground(PANEL_COLOR);
		FormLayout fl=new FormLayout("5px,left:pref,pref:grow,right:pref,2px","15px");
		JXPanel titlePanel=new JXPanel(fl);
		CellConstraints cc=new CellConstraints();

		if (TITLE_PAINTER!=null) titlePanel.setBackgroundPainter(TITLE_PAINTER);
		floatingComponent=component;
		this.canResize=canResize;

		JLabel label=new JLabel(title,JLabel.LEFT);
		label.setBackground(PANEL_COLOR);
		label.setOpaque(false);
		titlePanel.add(label,cc.xy(2,1));

		if (hasCloseButton) {
			JButton closeButton=new JButton(CLOSE_ICON) {                  // Something funny with the rolloverEnabled...
					protected void processMouseEvent(MouseEvent me) {
						if (me.getID()==MouseEvent.MOUSE_ENTERED) {
							setIcon(CLOSE_ROLLOVER_ICON);
							repaint();
						} else if (me.getID()==MouseEvent.MOUSE_EXITED) {
							setIcon(CLOSE_ICON);
							getParent().repaint();
						}
						super.processMouseEvent(me);
					}
				};
			closeButton.setBorder(null);
			closeButton.setFocusPainted(false);
			closeButton.setActionCommand("close");
			closeButton.addActionListener(this);
			titlePanel.add(closeButton,cc.xy(4,1));
		}
		add(titlePanel,BorderLayout.NORTH);
		add(component,BorderLayout.CENTER);
		setAlpha(0.9f);
		setComponentOpacity();
		setBorder(new CurvedBorder(7,4));
		ToolDialogMouseListener tdml=new ToolDialogMouseListener(this);
		addMouseListener(tdml);
		addMouseMotionListener(tdml);
		setLocation(100,100);
		setSize(getPreferredSize());
	}

	// =======================================================================

	public void actionPerformed(ActionEvent ae) {

		if ("close".equals(ae.getActionCommand())) {
			if (floatingComponent instanceof FloatingPanelActionListener) {
				((FloatingPanelActionListener)floatingComponent).closeAction();
			} else {
				ApplicationPanel.getInstance().removeFloatingPanel(this);
			}
		}
	}
	
	// =======================================================================

	public void paintComponent(Graphics g) {

		g.setColor(PANEL_COLOR);
		g.fillRoundRect(0,0,getWidth(),getHeight(),7,7);
		super.paintComponent(g);
	}

	// =======================================================================
	/*
	public void paintChildren(Graphics g) {

		super.paintChildren(g);
		if (canResize) {
			Graphics2D g2=(Graphics2D)g;
			Object hint=g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			Dimension d=getSize();
			g2.setColor(Color.white);
			g2.drawLine(d.width-15,d.height-2,d.width-2,d.height-15);
			g2.drawLine(d.width-12,d.height-2,d.width-2,d.height-12);
			g2.drawLine(d.width-9,d.height-2,d.width-2,d.height-9);
			g2.setColor(Color.darkGray);
			g2.drawLine(d.width-14,d.height-2,d.width-2,d.height-14);
			g2.drawLine(d.width-11,d.height-2,d.width-2,d.height-11);
			g2.drawLine(d.width-8,d.height-2,d.width-2,d.height-8);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,hint);
		}
	}
	*/
	// =======================================================================
	// Allow for dragging and resizing of the dialog panel

	private class ToolDialogMouseListener implements MouseListener, MouseMotionListener {

		JPanel panel=null;
		Dimension currentSize=null;
		Point startPoint=null, origin=null;
		boolean isResizing=false;

		public ToolDialogMouseListener(JPanel panel) {this.panel=panel;}

		// =======================================================================

		private Point getScreenLocation(MouseEvent e) {
			Point cursor=e.getPoint();
			Point targetLocation=panel.getLocationOnScreen();
			return new Point((int)(targetLocation.getX()+cursor.getX()),(int)(targetLocation.getY()+cursor.getY()));
    }

		// =======================================================================

		public void mouseClicked(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mouseMoved(MouseEvent e) {}

		public void mouseReleased(MouseEvent e) {if ((isResizing)&&(canResize)) {getParent().setCursor(DEFAULT_CURSOR);}}

		public void mousePressed(MouseEvent e) {

			Container panelParent=getParent();
			if (panelParent instanceof JLayeredPane) {
				((JLayeredPane)panelParent).moveToFront(FloatingPanel.this);
			}
			origin=getLocation(); 
			currentSize=getSize();
			int x=e.getX(), y=e.getY();
			isResizing=(((x<currentSize.width)&&(x>(currentSize.width-15)))&&((y<currentSize.height)&&(y>currentSize.height-15)));
			if ((isResizing)&&(canResize)) {getParent().setCursor(RESIZE_CURSOR);}
			startPoint=getScreenLocation(e);
		}

		public void mouseDragged(MouseEvent e) {

			Point currentPoint=getScreenLocation(e);
			if ((isResizing)&&(canResize)) {
				int deltaX=currentPoint.x-startPoint.x;
				int deltaY=currentPoint.y-startPoint.y;
				// int newXSize=currentSize.width+deltaX;
				// int newYSize=currentSize.height+deltaY;
				setSize(currentSize.width+deltaX,currentSize.height+deltaY);
				revalidate();
			} else {
				Point offset=new Point((int)currentPoint.x-(int)startPoint.x,(int)currentPoint.y-(int)startPoint.y);
				Point newLocation=new Point((int)(origin.x+offset.x),(int)(origin.y+offset.y));
				setLocation(newLocation);
			}
		}
	}
}
