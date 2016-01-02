/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.model.composite;

import java.awt.AlphaComposite;

import imageviewer.model.Image;
import imageviewer.model.ImageSequence;

public class CompositeImageSequence extends ImageSequence {

	private static final AlphaComposite DEFAULT_AC=AlphaComposite.getInstance(AlphaComposite.SRC);
	private static final AlphaComposite DEFAULT_OVERLAY_AC=AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.5f);

	ImageSequence[] sequences=null;

	public CompositeImageSequence(ImageSequence[] sequences) {super(); this.sequences=sequences;}

	// =======================================================================
	// Convenience method to take several image series and combine them
	// together into a single composite. The first sequence is used as
	// the base source. The smallest number of image slices will be used
	// based in the smallest length of the sequence given.

	public static CompositeImageSequence generate(ImageSequence[] sequences) {

		AlphaComposite[] acs=new AlphaComposite[sequences.length];
		acs[0]=DEFAULT_AC;
		for (int loop=1; loop<sequences.length; loop++) acs[loop]=DEFAULT_OVERLAY_AC;
		return generate(sequences,acs);
	}

	public static CompositeImageSequence generate(ImageSequence[] sequences, AlphaComposite sourceAC, AlphaComposite otherAC) {

		AlphaComposite[] acs=new AlphaComposite[sequences.length];
		acs[0]=sourceAC;
		for (int loop=1; loop<sequences.length; loop++) acs[loop]=otherAC;
		return generate(sequences,acs);
	}

	public static CompositeImageSequence generate(ImageSequence[] sequences, AlphaComposite[] acs) {

		if (acs.length!=sequences.length) return null;
		CompositeImageSequence cis=new CompositeImageSequence(sequences);
		int compositeSequenceLength=Integer.MAX_VALUE;
		for (int loop=0; loop<sequences.length; loop++) {
			int sequenceLength=sequences[loop].size();
			compositeSequenceLength=(compositeSequenceLength>sequenceLength)? sequenceLength : compositeSequenceLength;
		}
		for (int i=0, n=compositeSequenceLength; i<n; i++) {
			CompositeImage ci=new CompositeImage();
			CompositeImageDescriptor[] sources=new CompositeImageDescriptor[sequences.length];
			sources[0]=new CompositeImageDescriptor((Image)sequences[0].get(i),acs[0]);
			for (int j=1; j<sequences.length; j++) sources[j]=new CompositeImageDescriptor((Image)sequences[j].get(i),acs[j]);
			ci.setSources(sources);
			cis.add(ci);
		}
		return cis;
	}
	
	// =======================================================================

	public void setProperty(String x, Object o) {sequences[0].setProperty(x,o);}
	public Object getProperty(String x) {return sequences[0].getProperty(x);}

	public String getShortDescription() {return sequences[0].getShortDescription();}
	public String[] getLongDescription() {return sequences[0].getLongDescription();}

	// =======================================================================

	public static CompositeImageSequence generate(ImageSequence[] sequences, SequenceMap sm) {
	
		AlphaComposite[] acs=new AlphaComposite[sequences.length];
		acs[0]=DEFAULT_AC;
		for (int loop=1; loop<sequences.length; loop++) acs[loop]=DEFAULT_OVERLAY_AC;
		return generate(sequences,acs,sm);
	}

	public static CompositeImageSequence generate(ImageSequence[] sequences, AlphaComposite[] acs, SequenceMap sm) {

		CompositeImageSequence cis=new CompositeImageSequence(sequences);
		int compositeSequenceLength=sm.size();
		for (int i=0, n=compositeSequenceLength; i<n; i++) {
			CompositeImage ci=new CompositeImage();
			int[] targetSlices=sm.computeSlices(i);
			CompositeImageDescriptor[] sources=new CompositeImageDescriptor[1+targetSlices.length];
			sources[0]=new CompositeImageDescriptor((Image)sequences[0].get(i),acs[0]);
			for (int j=0; j<targetSlices.length; j++) {
				sources[j+1]=new CompositeImageDescriptor((Image)sequences[j+1].get(targetSlices[j]),acs[j+1]);
			}
			ci.setSources(sources);
			cis.add(ci);
		}
		return cis;
	}

	// =======================================================================

	public Object getDependentKey() {return this;}
	public Object getClosingKey() {return null;}
}
