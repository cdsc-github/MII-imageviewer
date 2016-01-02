/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/* ==========================================================================

  Copyright (C) 2005 UCLA Medical Imaging Informatics

  This library is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation; either version 2.1 of the
  License, or (at your option) any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  USA

  Email: opensource@mii.ucla.edu

  Mail:  UCLA Medical Imaging Informatics
         924 Westwood Bl., Suite 420
         Westwood, CA 90024

   Web:  http://www.mii.ucla.edu/imageviewer/ 

==========================================================================*/

package imageviewer.rendering.wl;

import imageviewer.model.Image;

public interface WindowLevelAlgorithm {

	public WindowLevel computeWindowLevel(Image i);
}
