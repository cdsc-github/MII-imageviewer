/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.layout;

import imageviewer.model.ImageSequenceGroup;

public interface CompositionRule {

	public String getName();
	public boolean isIgnored();

	public void process(ImageSequenceGroup isg);
	public void setName(String x);
	public void setIgnored(boolean x);
}

