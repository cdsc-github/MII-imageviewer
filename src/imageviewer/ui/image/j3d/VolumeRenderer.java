/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Group;

public interface VolumeRenderer {

	public BranchGroup getVolume();
	public void setVolume(BranchGroup x);

	public Group getStaticAttachGroup();
	public Group getDynamicAttachGroup();

	public void update();

}
