/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout.rules;

import java.util.ArrayList;
import java.util.Date;

import imageviewer.model.ImageSequence;
import imageviewer.model.ImageSequenceGroup;
import imageviewer.model.ImageSequenceProperties;

import imageviewer.ui.layout.LayoutDescription;
import imageviewer.ui.layout.LayoutFactory;
import imageviewer.ui.layout.LayoutPlan;
import imageviewer.ui.layout.GroupingRule;
import imageviewer.ui.layout.GroupedLayout;

// =======================================================================

public class ImmediatePriorRule implements GroupingRule {

	String ruleName=new String("IMMEDIATE PRIOR");
	boolean ignore=false;

	public ImmediatePriorRule() {}

	// =======================================================================
	// Group together any image series that have similar series descriptions
	// that occur from previous dates.

	public void selectLayout(ArrayList studies, LayoutPlan lp) {

		/*
		ArrayList series=isg.getGroups();
		ArrayList groupings=new ArrayList();
		for (int loop=0, n=series.size(); loop<n; loop++) {
			ImageSequence is=(ImageSequence)series.get(loop);
			String seriesDescription=(String)is.getProperty(ImageSequenceProperties.DESCRIPTION);
			if (seriesDescription!=null) {
				Date currentDate=(Date)is.getProperty(ImageSequenceProperties.TIMESTAMP);
				ImageSequence selectedSequence=null;
				int currentMin=Integer.MAX_VALUE;
				for (int j=0; j<n; j++) {
					if (j!=loop) {
						ImageSequence pastSequence=(ImageSequence)series.get(j);
						String s=(String)pastSequence.getProperty(ImageSequenceProperties.DESCRIPTION);
						if (seriesDescription.compareTo(s)==0) {
							Date pastDate=(Date)pastSequence.getProperty(ImageSequenceProperties.TIMESTAMP);
							int delta=pastDate.compareTo(currentDate);            
							if ((delta<0)&&(delta<currentMin)) selectedSequence=pastSequence;
						}
					}
				}
				if (selectedSequence!=null) {
					LayoutDescription ld=LayoutFactory.selectLayout(is,true);
					GroupedLayout gl=new GroupedLayout(new ImageSequence[] {is,selectedSequence},new String[] {"CURRENT","PRIOR"},ld);
					groupings.add(gl);
				}
			}
		}
		return groupings;
		*/
	}

	public void selectLayout(ImageSequenceGroup isg, LayoutPlan lp) {}

	// =======================================================================

	public String getName() {return ruleName;}
	public boolean isIgnored() {return ignore;}

	public void setIgnored(boolean x) {ignore=x;}
	public void setName(String x) {ruleName=x;}
}
