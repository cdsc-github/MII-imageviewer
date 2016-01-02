/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

import java.awt.ActiveEvent;
import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.MenuComponent;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import imageviewer.ui.swing.TranslucentPanel;

// =======================================================================

public class GlassPaneManager implements ActionListener, MouseListener, MouseMotionListener, MouseWheelListener, PropertyChangeListener {

	int fileChooserStatus=JFileChooser.CANCEL_OPTION;
 	JPanel pane=null;
	
	public GlassPaneManager(JPanel pane) {

		this.pane=pane;
		pane.addMouseListener(this); 
		pane.addMouseMotionListener(this); 
		pane.addMouseWheelListener(this); 
		pane.setLayout(new GridBagLayout());
		pane.setVisible(false);
	}

	// =======================================================================

	public int showModalDialog(JOptionPane op) {

		op.setOpaque(false);
		op.addPropertyChangeListener(this);
		TranslucentPanel tp=new TranslucentPanel();
		tp.setOpaque(false);
		tp.add(op);
		tp.setAlpha(0.9f);
		tp.setMaximumSize(op.getPreferredSize());
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.anchor=GridBagConstraints.CENTER;
		pane.removeAll();
		pane.add(tp,gbc);
		pane.setVisible(true);
		startModal();

		// Block until there's an answer...

		pane.removeAll();
    Object selectedValue=op.getValue();
		Object[] options=op.getOptions();
		if (selectedValue==null) return JOptionPane.CLOSED_OPTION;
		if (options==null) {
			if (selectedValue instanceof Integer) return ((Integer)selectedValue).intValue();
			return JOptionPane.CLOSED_OPTION;
		}
		for (int counter=0, maxCounter=options.length; counter<maxCounter; counter++) {
			if (options[counter].equals(selectedValue)) return counter;
		}
		return JOptionPane.CLOSED_OPTION;
	}
	
	// =======================================================================

	public int showModalDialog(JFileChooser fc, String dialogTitle) {

		fc.addActionListener(this);
		FloatingPanel fp=new FloatingPanel(fc,dialogTitle,false);
		fp.setAlpha(0.9f);
		GridBagConstraints gbc=new GridBagConstraints();
		gbc.anchor=GridBagConstraints.CENTER;
		pane.removeAll();
		pane.add(fp,gbc);
		pane.setVisible(true);
		startModal();
		pane.removeAll();
		fc.removePropertyChangeListener(this);
		return fileChooserStatus;
	}

	// =======================================================================
	
	public void mouseClicked(MouseEvent e) {} 
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}

	// =======================================================================

	public void propertyChange(PropertyChangeEvent pce) {if (pce.getPropertyName().equals(JOptionPane.VALUE_PROPERTY)) pane.setVisible(false);}

	// =======================================================================

	public void actionPerformed(ActionEvent ae) {

		if (ae.getSource() instanceof JFileChooser) {
			JFileChooser chooser=(JFileChooser)ae.getSource();
			if (JFileChooser.APPROVE_SELECTION.equals(ae.getActionCommand())) {
				fileChooserStatus=JFileChooser.APPROVE_OPTION;
				pane.setVisible(false);
			} else if (JFileChooser.CANCEL_SELECTION.equals(ae.getActionCommand())) {
				fileChooserStatus=JFileChooser.CANCEL_OPTION;
				pane.setVisible(false);
			}
		}
	}

	// =======================================================================

	private synchronized void startModal() {
		
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				EventQueue theQueue=pane.getToolkit().getSystemEventQueue();
				while (pane.isVisible()) {
					AWTEvent event=theQueue.getNextEvent();
					Object source=event.getSource();
					if (event instanceof ActiveEvent) {
						((ActiveEvent)event).dispatch();
					} else if (source instanceof Component) {
						((Component)source).dispatchEvent(event);
					} else if (source instanceof MenuComponent) {
						((MenuComponent)source).dispatchEvent(event);
					} else {
						System.err.println("Unable to dispatch: "+event);
					}
				}
			} else {
				while (pane.isVisible()) wait();
			}
		} catch (InterruptedException ignored) {}
	}
}


	

