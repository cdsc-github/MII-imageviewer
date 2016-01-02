/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jvnet.flamingo.common.JCommandButton;

// =======================================================================

public class RibbonFlipButton extends AbstractMultiButton implements PropertyChangeListener {

	public RibbonFlipButton() {super();}

	// =======================================================================

	public void addButton(JCommandButton jcb) {super.addButton(jcb); jcb.addPropertyChangeListener(this);}

	public void propertyChange(PropertyChangeEvent pce) {

		if (pce.getSource()==currentButton) {
			if ("enabled".equals(pce.getPropertyName())) {
				Boolean b=(Boolean)pce.getNewValue();
				if (b==Boolean.FALSE) {
					int nextButtonIndex=buttonList.indexOf(pce.getSource());
					if (nextButtonIndex==(buttonList.size()-1)) nextButtonIndex=0; else nextButtonIndex++;
					setCurrentButton(buttonList.get(nextButtonIndex));
				}
			}
		}
	}
}
