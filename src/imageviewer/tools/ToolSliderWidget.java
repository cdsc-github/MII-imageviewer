/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.text.NumberFormat;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JFormattedTextField;
import javax.swing.KeyStroke;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

// =======================================================================

public class ToolSliderWidget extends JPanel implements ChangeListener, PropertyChangeListener {

	String title=null;
	int minValue=0, maxValue=0;
	JFormattedTextField textField=null;
	JSlider slider=null;
	JLabel label=null;
	JButton leftArrow=null, rightArrow=null;

	public ToolSliderWidget(String title, int minValue, int maxValue, int initialValue, NumberFormat format) {

		super();
		this.title=title;
		this.minValue=minValue;
		this.maxValue=maxValue;

		label=new JLabel(title,JLabel.LEFT);
		slider=new JSlider(JSlider.HORIZONTAL,minValue,maxValue,initialValue);
		slider.addChangeListener(this);
		slider.setPaintTicks(false);
		slider.setPaintLabels(false);

		NumberFormatter formatter=new NumberFormatter(format);
		formatter.setMinimum(new Integer(minValue));
		formatter.setMaximum(new Integer(maxValue));
		textField=new JFormattedTextField(formatter);
		textField.setValue(new Integer(initialValue));
		textField.setColumns(3);
		textField.addPropertyChangeListener(this);
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"check");
		textField.getActionMap().put("check", new AbstractAction() {
		  public void actionPerformed(ActionEvent e) {
				if (!textField.isEditValid()) {                                 
					Toolkit.getDefaultToolkit().beep();
					textField.selectAll();
				} else try {                   
					textField.commitEdit();    
				} catch (Exception exc) { 
					exc.printStackTrace();
				}
			}
		});

		// Glob them all together into this panel.

		GridBagLayout gb=new GridBagLayout();
		GridBagConstraints gbc=new GridBagConstraints();
		setLayout(gb);

		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.weightx=1;
		gbc.weighty=1;
		gbc.ipadx=0;
		gbc.ipady=0;
		gbc.gridx=0;
		gbc.gridy=0;
		gb.setConstraints(label,gbc);
		add(label);

		gbc.fill=GridBagConstraints.NONE;
		gbc.gridx=1;
		gbc.gridy=0;
		gbc.weightx=0;
		gbc.weighty=0;
		gb.setConstraints(textField,gbc);
		add(textField);

		gbc.fill=GridBagConstraints.HORIZONTAL;
		gbc.weighty=0;
		gbc.gridx=0;
		gbc.gridy=1;
		gbc.gridwidth=2;
		gbc.insets=new Insets(2,0,0,0);
		gb.setConstraints(slider,gbc);
		add(slider);
	}

	// =======================================================================

	public JFormattedTextField getTextField() {return textField;}
	public JSlider getSlider() {return slider;}
	public JLabel getLabel() {return label;}

	public int getValue() {return slider.getValue();}

	// =======================================================================
	// Handles listening for changes in the text field.  Updates the
	// slider accordingly.

  public void propertyChange(PropertyChangeEvent e) {

		if ("value".equals(e.getPropertyName())) {
			Number value=(Number)e.getNewValue();
			if ((slider!=null)&&(value!=null)) slider.setValue(value.intValue());
		}
	}

	// =======================================================================
	// Handles listening for changes in the slider.  Updates the text
	// field accordingly.

	public void stateChanged(ChangeEvent e) {

		JSlider source=(JSlider)e.getSource();
		int value=(int)source.getValue();
		if (!source.getValueIsAdjusting()) { 
			textField.setValue(new Integer(value));
		} else {
			textField.setText(String.valueOf(value));
		}
	}
}
