/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import imageviewer.model.ImageSequence;
import imageviewer.util.StringUtilities;

// =======================================================================

public class GroupedLayout {

	LayoutDescription ld=null;
	ImageSequence[] series=null;
	String[] sequenceTags=null;

	public GroupedLayout(ImageSequence[] series, String[] sequenceTags, LayoutDescription ld) {this.ld=ld; this.series=series; this.sequenceTags=sequenceTags;}

	// =======================================================================

	public ImageSequence[] getImageSequences() {return series;}
	public LayoutDescription getLayoutDescription() {return ld;}
	public String[] getSequenceTags() {return sequenceTags;}

	public void setImageSequences(ImageSequence[] x) {series=x;}
	public void setLayoutDescription(LayoutDescription x) {ld=x;}
	public void setSequenceTags(String[] x) {sequenceTags=x;}

	// =======================================================================

	public ImageSequence getSequenceByTag(String tag) {

		if (tag==null) return null;
		String[] targetTags=tag.split(",");
		for (int i=0; i<targetTags.length; i++) {
			for (int j=0; j<sequenceTags.length; j++) {
				if (sequenceTags[j].compareTo(targetTags[i])==0) return series[j];
			}
		}
		return getSequenceByTag("PRIMARY");
	}

	// =======================================================================

	public String getShortTabDescription() {return getSequenceByTag("PRIMARY").getShortDescription().replaceAll("\\^\\d*|_|\\s+"," ");}

	public String getLongTabDescription() {

		StringBuffer sb=new StringBuffer("<html><body>");
		String[] lines=getSequenceByTag("PRIMARY").getLongDescription();
		if (lines!=null) {
			sb.append(StringUtilities.replaceAndCapitalize(lines[0],true));
			for (int loop=1; loop<lines.length; loop++) {
				if ((lines[loop]!=null)&&(lines[loop].length()!=0)) sb.append("<br>"+StringUtilities.replaceAndCapitalize(lines[loop],true));
			}
		}
		sb.append("</body></html>");
		return sb.toString();
	}
}
