/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d.mouse;

import java.awt.event.MouseEvent;

public class MouseDragEvaluator implements MouseCondition {

	public MouseDragEvaluator() {}

	public boolean evaluateMouseEvent(MouseEvent me) {

		int id=me.getID();
		return ((id==MouseEvent.MOUSE_DRAGGED) && !(me.isMetaDown()) && !(me.isControlDown()) &&  !(me.isAltDown()));
	}
}
