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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Frame;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLDrawableFactory;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLException;
import javax.media.opengl.GLPbuffer;

// =======================================================================
/**
 * Helps query OpenGL-related information and provides a shared OpenGL
 * context which helps keep textures loaded persistently in the
 * DisplayShelf and SlideShow components. <P>
 *
 * Most JOGL-based applications do not require this degree of
 * sophistication to provide fall-back paths, but for this
 * demonstration it is essential that it work on the largest possible
 * class of end user machines. Since we also want to use the GLJPanel
 * where possible for smooth transitions between the editor and
 * display shelf views, we require a certain amount of fall-back
 * logic.
 */

public class OpenGLInfo {

	private static boolean initializing;
	private static boolean initialized;
	private static boolean havePbufferSupport;
	private static boolean isWindows;
	private static boolean isVista;
	private static boolean isNVOrATI;
	private static boolean haveGL14;

	private static int maxTextureSize;
	private static GLPbuffer pbuffer;

	// =======================================================================

	public static synchronized void init() {

		if (!initialized && !initializing) {

			initializing=true;
			String osName=AccessController.doPrivileged(new PrivilegedAction<String>() {public String run() {return System.getProperty("os.name").toLowerCase();}});
			if (osName.startsWith("windows")) isWindows=true;
			if (osName.contains("vista")) isVista=true;

			// See whether we have pbuffer support
			GLDrawableFactory factory=GLDrawableFactory.getFactory();
			if (!isWindows && factory.canCreateGLPbuffer()) {
				try {
					pbuffer=factory.createGLPbuffer(new GLCapabilities(),null,1,1,null);
					pbuffer.addGLEventListener(new InfoListener());
					pbuffer.display();
					havePbufferSupport=true;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (!havePbufferSupport) {
				if (pbuffer!=null) {
					try {pbuffer.destroy();} catch (GLException e) {e.printStackTrace();}
					pbuffer=null;
				}
				initialized=false;
			}
		}
	}

	// =======================================================================

	public static GLCapabilities getCapabilities(boolean opaque) {GLCapabilities caps=new GLCapabilities(); if (!opaque) caps.setAlphaBits(8); return caps;}

	// =======================================================================

	/** Indicates whether we think we can rely on pbuffer (and,
			therefore, GLJPanel) support on this machine. */

	public static boolean havePbufferSupport() {return havePbufferSupport;}

	/** Returns an OpenGL context other components can share textures
			and display lists with to avoid repeated texture re-loading. */

	public static GLContext getSharedContext() {return (pbuffer!=null) ? pbuffer.getContext() : null;}

	/** Returns the (approximate) maximum size of an OpenGL texture on
			the current hardware. This limits the size of the images we can
			view in the 3-D display shelf and slide show views. */

	public static int getMaxTextureSize() {return maxTextureSize;}

	// =======================================================================

	/** Clean up resources associated with this class. */

	public static void destroy() {

		Runnable r=new Runnable() {
			public void run() {
				if (pbuffer!=null) {
					try {pbuffer.destroy();} catch (GLException e) {e.printStackTrace();}
					pbuffer=null;
				}
			}
		};
		if (EventQueue.isDispatchThread()) {
			r.run();
		} else {
			try {EventQueue.invokeAndWait(r);} catch (Exception e) {}
		}
	}

	// =======================================================================

	private static class InfoListener implements GLEventListener {

		public void init(GLAutoDrawable drawable) {

			GL gl=drawable.getGL();
			String glVendor=gl.glGetString(GL.GL_VENDOR).toLowerCase();
			if (glVendor.startsWith("nvidia") || glVendor.startsWith("ati")) isNVOrATI=true;
			if (gl.isExtensionAvailable("GL_VERSION_1_4")) haveGL14=true;
			int[] tmp=new int[1];
			gl.glGetIntegerv(GL.GL_MAX_TEXTURE_SIZE,tmp,0);
			maxTextureSize=tmp[0];
			initialized=true;
		}

		public void display(GLAutoDrawable drawable) {GL gl=drawable.getGL(); gl.glClear(GL.GL_COLOR_BUFFER_BIT);}
		public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}
		public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}
	}
}
