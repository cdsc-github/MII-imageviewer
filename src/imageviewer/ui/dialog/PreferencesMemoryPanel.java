/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import java.awt.Dimension;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.media.jai.JAI;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.FontUIResource;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import imageviewer.ui.ApplicationContext;
import imageviewer.util.ImageCache;

// =======================================================================

public class PreferencesMemoryPanel extends JPanel implements ActionListener, ChangeListener {

	private FontUIResource SLIDER_FONT=new FontUIResource("Tahoma",Font.PLAIN,9);

	// =======================================================================

	JSlider memoryThresholdSlider=new JSlider(JSlider.HORIZONTAL,0,100,50);
	JSlider maxMemoryThresholdSlider=new JSlider(JSlider.HORIZONTAL,0,100,50);
	JSpinner cacheSpinner=new JSpinner(new SpinnerNumberModel(256,64,1024,1));
	PreferencesDialog pd=null;
	Hashtable changedProperties=new Hashtable();

	public PreferencesMemoryPanel(PreferencesDialog pd) {

		super();
		setPreferredSize(new Dimension(385,360));

		JTextArea memoryDescription=DialogUtil.createTextArea("Changes to these memory settings will not take place until the application is restarted.");
		JTextArea iCacheDescription=DialogUtil.createTextArea("New images are loaded into memory on an as-needed basis, and old images unloaded from the local cache to save memory.  "+
																													"The size of the image cache determines the number of images that may be maintained in memory.  "+
																													"Increasing the size of the cache may improve performance, but will also require more "+
																													"heap space.");
		JTextArea purgerDescription=DialogUtil.createTextArea("The program attempts to clear memory by removing old images from its "+
																													"cache and performing garbage collection.  You can perform "+
																													"these tasks immediately to create extra space.  The entire image cache will be eliminated; "+
																													"image performance may suffer in the short term as images are re-read from disk into memory.");

		JButton garbageCollectionButton=new JButton("Clear memory now");
		garbageCollectionButton.setActionCommand("garbageCollect");
		garbageCollectionButton.addActionListener(this);

		JLabel memoryLabel=new JLabel("Initial heap threshold (%)",JLabel.TRAILING);
		JLabel maxMemoryLabel=new JLabel("Maximum heap threshold (%)",JLabel.TRAILING);
		JLabel cacheSizeLabel=new JLabel("Image cache size",JLabel.TRAILING);
		JSeparator separator1=new JSeparator();
		JSeparator separator2=new JSeparator();

		memoryThresholdSlider.setPaintTicks(true);
		memoryThresholdSlider.setPaintLabels(true);
		memoryThresholdSlider.setMajorTickSpacing(20);
		memoryThresholdSlider.setMinorTickSpacing(5);
		memoryThresholdSlider.addChangeListener(this);
		memoryThresholdSlider.setFont(SLIDER_FONT);
		setSliderFont(memoryThresholdSlider);

		Float mt=(Float)ApplicationContext.getContext().getProperty(ApplicationContext.MEMORY_THRESHOLD);
		if (mt!=null) memoryThresholdSlider.setValue(new Integer((int)(mt.floatValue()*100))); else memoryThresholdSlider.setEnabled(false);
		memoryThresholdSlider.addChangeListener(this);

		maxMemoryThresholdSlider.setPaintTicks(true);
		maxMemoryThresholdSlider.setPaintLabels(true);
		maxMemoryThresholdSlider.setMajorTickSpacing(20);
		maxMemoryThresholdSlider.setMinorTickSpacing(5);
		maxMemoryThresholdSlider.addChangeListener(this);
		maxMemoryThresholdSlider.setFont(SLIDER_FONT);
		setSliderFont(maxMemoryThresholdSlider);

		Float mmt=(Float)ApplicationContext.getContext().getProperty(ApplicationContext.MAXIMUM_MEMORY_THRESHOLD);
		if (mmt!=null) maxMemoryThresholdSlider.setValue(new Integer((int)(mmt.floatValue()*100))); else maxMemoryThresholdSlider.setEnabled(false);
		maxMemoryThresholdSlider.addChangeListener(this);

		Integer cs=(Integer)ApplicationContext.getContext().getProperty(ApplicationContext.DEFAULT_IMAGE_CACHE_SIZE);
		if (cs!=null) cacheSpinner.setValue(cs); else cacheSpinner.setEnabled(false);
		cacheSpinner.addChangeListener(this);

		FormLayout fl=new FormLayout("10px,right:pref:grow,10px,pref,2dlu,pref,10px",
																 "10px,pref,5px,center:pref,5px,center:pref,5px,center:pref,5px,pref,5px,pref,5px,pref,5px,pref,5px,pref,10px");
		setLayout(fl);

		CellConstraints cc=new CellConstraints();
		add(memoryDescription,cc.xywh(2,2,5,1));
		add(memoryLabel,cc.xy(2,4));
		add(memoryThresholdSlider,cc.xywh(4,4,3,1));
		add(maxMemoryLabel,cc.xy(2,6));
		add(maxMemoryThresholdSlider,cc.xywh(4,6,3,1));
		add(separator1,cc.xywh(2,8,5,1));
		add(iCacheDescription,cc.xywh(2,10,5,1));
		add(cacheSizeLabel,cc.xy(2,12));
		add(cacheSpinner,cc.xywh(4,12,1,1));
		add(separator2,cc.xywh(2,14,5,1));
		add(purgerDescription,cc.xywh(2,16,5,1));
		add(garbageCollectionButton,cc.xy(6,18));
		this.pd=pd;
	}

	// =======================================================================
	// Need to independently set the JLabel font for each slider; this problem
	// is fixed in JDK 1.6, but not in earlier versions.

	private void setSliderFont(JSlider slider) {

		Dictionary d=slider.getLabelTable();
    Enumeration e=d.elements();
    while (e.hasMoreElements()) {
      Object o=e.nextElement();
      if (o instanceof JComponent) ((JComponent)o).setFont(SLIDER_FONT);
    }
	}

	// =======================================================================

	public void actionPerformed(ActionEvent e) {

		String actionCommand=e.getActionCommand();
		if ("garbageCollect".equals(actionCommand)) {
			ImageCache.getDefaultImageCache().removeAll();
			JAI.getDefaultInstance().getTileCache().flush();
			System.gc();
		}
	}

	public void stateChanged(ChangeEvent e) {

		if (pd!=null) {
			Object o=e.getSource();
			if (o==memoryThresholdSlider) {
				int value=memoryThresholdSlider.getValue();
				changedProperties.put(ApplicationContext.MEMORY_THRESHOLD,new Float((float)value/100));
				} else if (o==maxMemoryThresholdSlider) {
				int value=maxMemoryThresholdSlider.getValue();
				changedProperties.put(ApplicationContext.MAXIMUM_MEMORY_THRESHOLD,new Float((float)value/100));
			} else if (o==cacheSpinner) {
				Integer value=(Integer)cacheSpinner.getValue();
				changedProperties.put(ApplicationContext.DEFAULT_IMAGE_CACHE_SIZE,value);
			}
			pd.enableApplyButton(true);
		}
	}

	public void applyChanges() {

		for (Enumeration e=changedProperties.keys(); e.hasMoreElements();) {
			String s=(String)e.nextElement();
			ApplicationContext.getContext().setProperty(s,changedProperties.get(s));
		}
		changedProperties.clear();
		pd.enableApplyButton(false);
	}
}
