/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import java.util.ArrayList;
import java.util.Hashtable;

import imageviewer.model.ImageSequence;

public class LayoutPlan {

	ArrayList<GroupedLayout> groupedLayouts=new ArrayList<GroupedLayout>();
	ArrayList<ImageSequence> sequences=new ArrayList<ImageSequence>();
	Hashtable<ImageSequence,GroupedLayout> sequenceMap=new Hashtable<ImageSequence,GroupedLayout>();
	boolean[] processedList=null;

	public LayoutPlan() {}

	// =======================================================================

	public int size() {return groupedLayouts.size();}
	public ArrayList getGroupedLayouts() {return groupedLayouts;}
	public GroupedLayout getGroupedLayout(int x) {return groupedLayouts.get(x);}
	public Hashtable getSequenceMap() {return sequenceMap;}

	public boolean isProcessed(int x) {return (processedList!=null) ? processedList[x] : false;}
	public boolean isProcessed(ImageSequence x) {return isProcessed(sequences.indexOf(x));}

	public void setGroupedLayouts(ArrayList<GroupedLayout> x) {groupedLayouts=x;}
	public void setImageSequenceMap(Hashtable<ImageSequence,GroupedLayout> x) {sequenceMap=x;}
	public void addInitialLayout(ImageSequence is, GroupedLayout gl) {sequenceMap.put(is,gl);	groupedLayouts.add(gl); sequences.add(is);}

	// =======================================================================

	public void setProcessed(ImageSequence is) {processedList[sequences.indexOf(is)]=true;}

	// =======================================================================

	public LayoutDescription getLayout(ImageSequence is) {

		GroupedLayout gl=sequenceMap.get(is);
		return gl.getLayoutDescription();
	}

	// =======================================================================

	public void initializeLayouts() {

		int length=sequences.size();
		processedList=new boolean[length];
		for (int loop=0; loop<length; loop++) processedList[loop]=false;
	}

	// =======================================================================

	public void addLayout(int index, GroupedLayout gl) {

		ImageSequence[] series=gl.getImageSequences();
		for (int loop=0; loop<series.length; loop++) sequenceMap.put(series[loop],gl);	
		groupedLayouts.add(index,gl);
	}

	public int removeLayout(ImageSequence is) {

		GroupedLayout gl=sequenceMap.get(is);
		int index=groupedLayouts.indexOf(gl);
		groupedLayouts.remove(gl);
		sequenceMap.remove(is);
		return index;
	}

	// =======================================================================

	public void cleanUp() {

		groupedLayouts.clear(); groupedLayouts=null;
		sequences.clear(); sequences=null;
		sequenceMap.clear(); sequenceMap=null;
	}
}
