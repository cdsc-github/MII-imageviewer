/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout.rules;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.model.ImageSequenceProperties;

import imageviewer.ui.ApplicationContext;

import imageviewer.ui.layout.LayoutDescription;
import imageviewer.ui.layout.LayoutFactory;
import imageviewer.ui.layout.LayoutPlan;
import imageviewer.ui.layout.GroupingRule;
import imageviewer.ui.layout.GroupedLayout;

// =======================================================================

public class ScoutRule implements GroupingRule {

	private static final String[] SCOUT_KEYWORDS=new String[] {"SCOUT","TOPO"};

	// =======================================================================

	String ruleName=new String("SCOUT");
	boolean ignore=false;

	public ScoutRule() {}

	// =======================================================================

	private boolean isScout(String seriesDescription) {

		if (seriesDescription==null) return false;
		seriesDescription=seriesDescription.toUpperCase();
		for (int i=0; i<SCOUT_KEYWORDS.length; i++) if (seriesDescription.contains(SCOUT_KEYWORDS[i])) return true;
		return false;
	}

	// =======================================================================
	// Group together any image series that are considered scouts (based
	// on a description with the substring of 'scout' or 'topo') with
	// the image series that has the closest timestamp and that is also not
	// a scout.

	public void selectLayout(ImageSequenceGroup isg, LayoutPlan lp) {

		ArrayList series=isg.getGroups();
		for (int loop=0, n=series.size(); loop<n; loop++) {
			ImageSequence is=(ImageSequence)series.get(loop);
			if (!lp.isProcessed(is)) {
				String seriesDescription=(String)is.getProperty(ImageSequenceProperties.DESCRIPTION);
				if (isScout(seriesDescription)) {
					Date scoutDate=(Date)is.getProperty(ImageSequenceProperties.TIMESTAMP);
					long scoutTime=scoutDate.getTime();
					long currentMin=Long.MAX_VALUE;
					ImageSequence selectedSequence=null;
					for (int j=0; j<n; j++) {
						if (j!=loop) {
							ImageSequence primarySequence=(ImageSequence)series.get(j);
							String s=(String)primarySequence.getProperty(ImageSequenceProperties.DESCRIPTION);
							if (!isScout(s)) {
								Date primaryDate=(Date)primarySequence.getProperty(ImageSequenceProperties.TIMESTAMP);
								long primaryTime=primaryDate.getTime();
								long delta=primaryTime-scoutTime;
								if ((delta>=0)&&(delta<currentMin)) {
									selectedSequence=primarySequence;
									currentMin=delta;
								}
							}
						}
					}

					// Remove from the layout plan the previous layouts
					// associated with both sequences.

					if (selectedSequence==null) return;
					LayoutDescription ld=lp.getLayout(selectedSequence);
					if (!ld.hasKeyFor("SCOUT")) {
						GraphicsConfiguration gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
						Rectangle r=gc.getBounds();						
						String resolutionTag=(int)r.getWidth()+"x"+(int)r.getHeight();								
						ld=LayoutFactory.doLookup("Small overview"+"_"+resolutionTag);
					}
					GroupedLayout gl=new GroupedLayout(new ImageSequence[] {is,selectedSequence},new String[] {"SCOUT","PRIMARY"},ld);
					lp.removeLayout(is);
					int insertIndex=lp.removeLayout(selectedSequence);
					lp.addLayout(insertIndex,gl);
					lp.setProcessed(is);
					lp.setProcessed(selectedSequence);
				}
			}
		}
	}

	public void selectLayout(ArrayList studies, LayoutPlan lp) {}

	// =======================================================================

	public String getName() {return ruleName;}
	public boolean isIgnored() {return ignore;}

	public void setIgnored(boolean x) {ignore=x;}
	public void setName(String x) {ruleName=x;}
}
