/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.RenderingHints;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.MenuSelectionManager;
import javax.swing.UIManager;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jdesktop.swingx.painter.Painter;

// =======================================================================

public class MultiButton extends JPanel implements ActionListener, ChangeListener, MouseListener, MouseMotionListener {

	private static final Painter BP=(Painter)UIManager.get("Button.backgroundPainter");

	private static final Polygon ARROW=new Polygon(new int[] {0,5,2},new int[] {0,0,3},3);   
	private static final int ARROW_WIDTH=13;

	private static Color FOCUS_COLOR=UIManager.getColor("Button.focus");
	private static Color SELECT_COLOR=UIManager.getColor("Button.select");
	private static Color ARROW_COLOR=UIManager.getColor("MultiButton.arrowColor");
	private static Color SEPARATOR_COLOR=UIManager.getColor("MultiButton.separator");

	// =======================================================================

	ArrayList<AbstractButton> buttonList=new ArrayList<AbstractButton>();
	Hashtable<String,AbstractButton> menuActionMap=new Hashtable<String,AbstractButton>();
	boolean mouseInPopupArea=false, mouseOverArea=false, isRollover=false, showText=false;
	AbstractButton currentButton=null;
	JPopupMenu popupMenu=null;

	public MultiButton(boolean showText) {

		super(new BorderLayout(),true); 
		this.showText=showText;
		addMouseMotionListener(this);
    addMouseListener(this);
		setOpaque(false);
		popupMenu=new JPopupMenu();
		popupMenu.add(Box.createRigidArea(new Dimension(0,2)));
	}

	// =======================================================================

	public AbstractButton getCurrentButton() {return currentButton;}

	public void addButton(AbstractButton b) {addButton(b,null);}

	public void addButton(AbstractButton b, String text) {

		if (buttonList.isEmpty()) setCurrentButton(b);	
		buttonList.add(b);
		JMenuItem jmi=new JMenuItem(b.getIcon());
		String actionCommand=b.getToolTipText();
		jmi.setActionCommand(actionCommand);
		jmi.addActionListener(this);
		jmi.setText((showText) ? text : null);
		jmi.setToolTipText(b.getToolTipText());
		jmi.setOpaque(false);
		menuActionMap.put(actionCommand,b);
		popupMenu.add(jmi);
	}

	public void removeButton(AbstractButton b) {buttonList.remove(b);}

	// =======================================================================

	public void setCurrentButton(AbstractButton b) {

		if (currentButton!=null) {currentButton.removeChangeListener(this); remove(currentButton);}
		add(b,BorderLayout.WEST);
		currentButton=b;
		currentButton.addChangeListener(this);
		revalidate();
		repaint();
	}

	// =======================================================================

	public void stateChanged(ChangeEvent ce) {isRollover=((AbstractButton)(ce.getSource())).getModel().isRollover(); repaint();}

	// =======================================================================
	
	public void actionPerformed(ActionEvent ae) {

		String actionCommand=ae.getActionCommand();
		if (actionCommand!=null) {
			AbstractButton ab=menuActionMap.get(actionCommand);
			if (ab==null) return;
			ab.doClick();
			setCurrentButton(ab);
		}
	}

	// =======================================================================

	public void mouseDragged(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}

  public void mouseMoved(MouseEvent e) {int x=e.getX(); int width=getWidth(); mouseInPopupArea=((x>=(width-ARROW_WIDTH))&&(x<=width));}
	public void mouseExited(MouseEvent e) {mouseOverArea=false; mouseInPopupArea=false; repaint();}
	public void mouseEntered(MouseEvent e) {mouseOverArea=true; repaint();}
	public void mouseClicked(MouseEvent e) {if (isEnabled()&&(mouseInPopupArea)) popupMenu.show(e.getComponent(),0,getHeight());}

	// =======================================================================

	public void paint(Graphics g) {

		Graphics2D g2=(Graphics2D)g.create();
		if ((isRollover)||(mouseOverArea)) {
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY); 
			BP.paint(g2,this,getWidth(),getHeight());
		}
		super.paint(g2);
		int width=getWidth();
		int height=getHeight();

		if ((isRollover)||(mouseOverArea)) {
			g2.setColor(SELECT_COLOR);
			Insets i=currentButton.getInsets();
			g2.setColor(SEPARATOR_COLOR);
			g2.drawLine(width-14,1,width-14,height-2);
		}
		g2.setColor(ARROW_COLOR);
		g2.translate(width-10,height/2-1);
		g2.fill(ARROW);
		g2.dispose();
	}

	// =======================================================================

	public Dimension getMinimumSize() {Dimension d=currentButton.getMinimumSize(); return new Dimension((int)d.getWidth()+ARROW_WIDTH,(int)d.getHeight());}
	public Dimension getMaximumSize() {Dimension d=currentButton.getMaximumSize(); return new Dimension((int)d.getWidth()+ARROW_WIDTH,(int)d.getHeight());}
	public Dimension getPreferredDimension() {Dimension d=currentButton.getPreferredSize(); return new Dimension((int)d.getWidth()+ARROW_WIDTH,(int)d.getHeight());}
	
}
