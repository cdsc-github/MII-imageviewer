/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 *   * Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   * Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package imageviewer.ui.animation;

import java.awt.image.BufferedImage;

/** 
 * Defines how elements in the ListModel associated with the
 * ImageBrowser are converted into images that can be rendered
 * on-screen. <P>
 * 
 * The clientIdentifier is a token, specific to a particular client,
 * for a particular image. It is supplied back to the client in
 * progress callbacks to make it easier for the client to understand
 * for what request the progress update is for. For example, this
 * might be the index into the ListModel of the image we're talking
 * about. <P>
 *
 * @author Kenneth Russell
 * @author Jasper Potts
 * @author Richard Bair
 */

public interface Fetcher {

	/** Constant for normal priority tasks */
	public static final int NORMAL_PRIORITY=1;

	/** Constant for high priority tasks */
	public static final int HIGH_PRIORITY=2;

	/** Requests the image associated with the given image descriptor,
			scaled to match approximately the requested width and height.
			This call does not block; if an image smaller than the requested
			width and height is immediately available in the cache, returns
			that and starts a background download process to fetch a larger
			version of the image. May return an image larger than the
			requested width and height, but will not return an image more
			than a factor of two larger in either dimension; note that this
			may result in one dimension being smaller than requested
			depending on the aspect ratio of the image. If a
			ProgressListener is supplied, it is called as the download
			progresses. The client can then re-fetch the larger image when
			the {@link ProgressListener#progressEnd progressEnd} callback is
			called. */

	public BufferedImage getImage(Object imageDescriptor, int requestedImageWidth, int requestedImageHeight, int priority, ProgressListener listener, Object clientIdentifier);

	/** Cancels a previously-registered download request from this
			Fetcher -- one which is producing progress events to a
			previously-specified ProgressListener. */

	public void cancelDownload(Object imageDescriptor, int requestedImageWidth, int requestedImageHeight, int priority, ProgressListener listener, Object clientIdentifier);
}
