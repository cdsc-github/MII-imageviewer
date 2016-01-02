/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
// =======================================================================
/* Taken from JGoodies Project 
/*
 * Copyright (c) 2001-2005 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */

package imageviewer.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.plaf.basic.BasicOptionPaneUI;

// =======================================================================
/**
 * Unlike its superclass, this layout uses a minimum button width 
 * that complies with Mac and Windows UI style guides.
 * 
 * @author  Karsten Lentzsch
 * @version $Revision: 1.1 $
 */

public final class OptionPaneButtonAreaLayout extends BasicOptionPaneUI.ButtonAreaLayout {

	/**
	 * Constructs an <code>ExtButtonAreaLayout</code>.
	 * 
	 * @param syncAllWidths  true indicates that all buttons get the same size 
	 * @param padding        the padding between buttons
	 */

	public OptionPaneButtonAreaLayout(boolean syncAllWidths, int padding) {super(syncAllWidths,padding);}

	// =======================================================================

	public void layoutContainer(Container container) {

		Component[] children=container.getComponents();

		if (children!=null && children.length>0) {

			int numChildren=children.length;
			Dimension[] sizes=new Dimension[numChildren];
			int yLocation=container.getInsets().top;

			if (syncAllWidths) {
				int maxWidth=getMinimumButtonWidth();
				for (int counter=0; counter<numChildren; counter++) {
					sizes[counter]=children[counter].getPreferredSize();
					maxWidth=Math.max(maxWidth, sizes[counter].width);
				}

				int xLocation, xOffset;

				if (getCentersChildren()) {
					xLocation=(container.getSize().width-(maxWidth*numChildren+(numChildren-1)* padding))/2;
					xOffset=padding+maxWidth;
				} else {
					if (numChildren>1) {
						xLocation=0;
						xOffset=(container.getSize().width-(maxWidth*numChildren))/(numChildren-1)+maxWidth;
					} else {
						xLocation=(container.getSize().width-maxWidth)/2;
						xOffset=0;
					}
				}
				for (int counter=0; counter<numChildren; counter++) {
					children[counter].setBounds(xLocation,yLocation,maxWidth,sizes[counter].height);
					xLocation+=xOffset;
				}
			} else {

				int totalWidth=0;
				for (int counter=0; counter<numChildren; counter++) {
					sizes[counter]=children[counter].getPreferredSize();
					totalWidth+=sizes[counter].width;
				}
				totalWidth+=((numChildren-1)*padding);
				boolean cc=getCentersChildren();

				int xOffset, xLocation;

				if (cc) {
					xLocation=(container.getSize().width-totalWidth)/2;
					xOffset=padding;
				} else {
					if (numChildren>1) {
						xOffset=(container.getSize().width-totalWidth)/(numChildren-1);
						xLocation=0;
					} else {
						xLocation=(container.getSize().width-totalWidth)/2;
						xOffset=0;
					}
				}

				for (int counter=0; counter<numChildren; counter++) {
					children[counter].setBounds(xLocation,yLocation,sizes[counter].width,sizes[counter].height);
					xLocation+=xOffset+sizes[counter].width;
				}
			}
		}
	}

	// =======================================================================

	public Dimension minimumLayoutSize(Container c) {

		if (c!=null) {
			Component[] children=c.getComponents();
			if (children!=null && children.length>0) {
				Dimension aSize=null;
				int numChildren=children.length, height=0;
				Insets cInsets=c.getInsets();
				int extraHeight=cInsets.top+cInsets.bottom;
				if (syncAllWidths) {
					int maxWidth=getMinimumButtonWidth();
					for (int counter=0; counter<numChildren; counter++) {
						aSize=children[counter].getPreferredSize();
						height=Math.max(height,aSize.height);
						maxWidth=Math.max(maxWidth,aSize.width);
					}
					return new Dimension(maxWidth*numChildren+(numChildren-1)*padding,extraHeight+height);
				} else {
					int totalWidth=0;
					for (int counter=0; counter<numChildren; counter++) {
						aSize=children[counter].getPreferredSize();
						height=Math.max(height,aSize.height);
						totalWidth+=aSize.width;
					}
					totalWidth+=((numChildren-1)*padding);
					return new Dimension(totalWidth,extraHeight+height);
				}
			}
		}
		return new Dimension(0,0);
	}

	// =======================================================================
    
	private int getMinimumButtonWidth() {return 75;}

}
