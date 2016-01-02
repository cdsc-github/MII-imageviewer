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
import java.awt.Component;
import java.awt.Container;
import java.awt.DisplayMode;
import java.awt.EventQueue;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import java.net.URL;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimerTask;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLCapabilitiesChooser;
import javax.media.opengl.GLContext;
import javax.media.opengl.GLJPanel;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;

import javax.swing.JFrame;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

// =======================================================================
/* A 3-D display shelf component. This is a thin wrapper around the
 * DisplayShelfRenderer, which is pluggable into either a heavyweight
 * or lightweight OpenGL component. */

public class DisplayShelf extends Container {

	private static final ExecutorService THREAD_POOL=Executors.newFixedThreadPool(5);
	private static final LinkedHashMap<Object,BufferedImage> IMAGE_CACHE=new LinkedHashMap<Object,BufferedImage>() {
		protected boolean removeEldestEntry(Map.Entry<Object,BufferedImage> entry) {return (size()>50);}
  };

	// =======================================================================
	
	DisplayShelfRenderer renderer=null;
	GLAutoDrawable canvas=null;

	public DisplayShelf(Fetcher f) {
		this(f,new DefaultListModel(),new DefaultListSelectionModel());
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().setSelectionInterval(0,0);
	}

	public DisplayShelf(Fetcher f, ListModel model, ListSelectionModel selectionModel) {this(f,model,selectionModel,false);}

	public DisplayShelf(final Fetcher f, final ListModel model, final ListSelectionModel selectionModel, final boolean heavyweight) {

		// Force the lightweight version to be opaque for better rendering performance on Windows
		// as well as better reproducibility of rendering results across platforms (OS X wants to
		// make everything translucent by default)

		setLayout(new BorderLayout());
		renderer=new DisplayShelfRenderer(f,model,selectionModel,(OpenGLInfo.getSharedContext()!=null));
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				OpenGLInfo.init();
				boolean hw=heavyweight;
				if (!OpenGLInfo.havePbufferSupport()) hw=true;
				if (hw) {
					canvas=new GLCanvas(new GLCapabilities(),null,OpenGLInfo.getSharedContext(),null);
				} else {
					canvas=new DisplayShelfGLJPanel(new GLCapabilities(),null,OpenGLInfo.getSharedContext());
					((GLJPanel)canvas).setOpaque(true);
				}
				canvas.addGLEventListener(renderer);
				((Component)canvas).setFocusable(true);
				add((Component)canvas,BorderLayout.CENTER);
		}});
	}

	// =======================================================================

	public ListModel getModel() {return renderer.getModel();}
	public ListSelectionModel getSelectionModel() {return renderer.getSelectionModel();}

	public int getSelectedIndex() {return renderer.getSelectedIndex();}

	public void addListSelectionListener(ListSelectionListener listener) {renderer.addListSelectionListener(listener);}
	public void removeListSelectionListener(ListSelectionListener listener) {renderer.removeListSelectionListener(listener);}
	public void setModel(ListModel model) {renderer.setModel(model);}
	public void setSelectedIndex(int index) {renderer.setSelectedIndex(index);}
	public void setSelectionModel(ListSelectionModel selectionModel) {renderer.setSelectionModel(selectionModel);}

	/** Switches to single image mode, viewing the current selected image. */

	public void setSingleImageMode(boolean singleImageMode, boolean animateTransition) {renderer.setSingleImageMode(singleImageMode,animateTransition);}

	/** Indicates whether this component is in single image mode. */

	public boolean getSingleImageMode() {return renderer.getSingleImageMode();}

	/** Adds an animation listener to this display shelf, which will be called when any animation is complete. */

	public void addAnimationListener(AnimationListener listener) {renderer.addAnimationListener(listener);}

	/** Removes an animation listener from this display shelf. */

	public void removeAnimationListener(AnimationListener listener) {renderer.removeAnimationListener(listener);}

	// =======================================================================
	// A GLJPanel subclass with special behavior because we know the
	// addition/removal behavior of the DisplayShelf and want to avoid
	// repeatedly destroying and re-creating its internal pbuffer

	private static class DisplayShelfGLJPanel extends GLJPanel {

		boolean created=false, destroyed=false;

		public DisplayShelfGLJPanel(GLCapabilities capabilities, GLCapabilitiesChooser chooser, GLContext shareWith) {super(capabilities,chooser,shareWith);}

		public void addNotify() {if (!created) {created=true; super.addNotify();}}
		public void removeNotify() {if (destroyed) super.removeNotify();}
		public void destroy() {destroyed=true; removeNotify();}
	}

	// =======================================================================

	private static class ImageIOFetcher implements Fetcher {

		public ImageIOFetcher() {}

		public BufferedImage getImage(final Object imageDescriptor, final int requestedImageWidth, final int requestedImageHeight, 
																	final int priority, final ProgressListener listener, final Object clientIdentifier) {

			BufferedImage bi=IMAGE_CACHE.get(imageDescriptor);
			if (bi!=null) return bi;
			TimerTask tt=new TimerTask() {

				public void run() {
					try {
						InputStream input=null;
						if (imageDescriptor instanceof File) {
							input=new BufferedInputStream(new FileInputStream((File) imageDescriptor));
						} else if (imageDescriptor instanceof URL) {
							input=((URL)imageDescriptor).openStream();
						} else {
							RuntimeException re=new RuntimeException("Unsupported imageDescriptor type "+imageDescriptor.getClass().getName());
							re.printStackTrace();
						}
						ImageInputStream imgIn=ImageIO.createImageInputStream(input);
						Iterator<ImageReader> readerIter=ImageIO.getImageReaders(imgIn);
						if (readerIter.hasNext()) {
							ImageReader reader=readerIter.next();
							reader.setInput(imgIn);
							IIOProgress iioListener=new IIOProgress(imageDescriptor,listener,clientIdentifier);
							reader.addIIOReadProgressListener(iioListener);
							try {
								ImageReadParam params=reader.getDefaultReadParam();
								BufferedImage img=reader.read(0,params);
								IMAGE_CACHE.put(imageDescriptor,img);
							} finally {
								reader.removeIIOReadProgressListener(iioListener);
								reader.dispose();
							}
						}
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}};
			THREAD_POOL.execute(tt);
			return null;
		}
		
    public void cancelDownload(Object imageDescriptor, int requestedImageWidth, int requestedImageHeight, int priority, ProgressListener listener, Object clientIdentifier) {}

		protected void fireProgressStart(final ProgressListener listener, final ProgressEvent e) {if (listener!=null) {invokeLaterOnEDT(new Runnable() {public void run() {listener.progressStart(e);}});}}
    protected void fireProgressUpdate(final ProgressListener listener, final ProgressEvent e) {if (listener!=null) {invokeLaterOnEDT(new Runnable() {public void run() {listener.progressUpdate(e);}});}}
    protected void fireProgressEnd(final ProgressListener listener, final ProgressEvent e) {if (listener!=null) {invokeLaterOnEDT(new Runnable() {public void run() {listener.progressEnd(e);}});}}

		private void invokeLaterOnEDT(Runnable runnable) {if (EventQueue.isDispatchThread()) {runnable.run();} else {EventQueue.invokeLater(runnable);}}
		
		private class IIOProgress implements IIOReadProgressListener {

			Object imageDescriptor=null, clientIdentifier=null;
			ProgressListener listener=null;;

			private IIOProgress(Object imageDescriptor, ProgressListener listener, Object clientIdentifier) {this.imageDescriptor=imageDescriptor; this.listener=listener; this.clientIdentifier=clientIdentifier;}

			public void sequenceStarted(ImageReader source, int minIndex) {}
			public void sequenceComplete(ImageReader source) {}
			public void thumbnailStarted(ImageReader source,int imageIndex, int thumbnailIndex) {}
			public void thumbnailProgress(ImageReader source, float percentageDone) {}
			public void thumbnailComplete(ImageReader source) {}
			public void readAborted(ImageReader source) {}

			public void imageStarted(ImageReader source, int imageIndex) {fireProgressStart(listener,new ProgressEvent(ImageIOFetcher.this,imageDescriptor,clientIdentifier,0.0f,true));}
			public void imageProgress(ImageReader source, float percentageDone) {fireProgressUpdate(listener,new ProgressEvent(ImageIOFetcher.this,imageDescriptor,clientIdentifier,percentageDone/100.0f,true));}
			public void imageComplete(ImageReader source) {fireProgressEnd(listener,new ProgressEvent(ImageIOFetcher.this,imageDescriptor,clientIdentifier,1.0f,true));}
		}
	}

	// =======================================================================

	public static void main(String[] args) {

		JFrame f=new JFrame("Display Shelf test");
		f.setLayout(new BorderLayout());
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {new Thread(new Runnable() {public void run() {System.exit(0);}}).start();}
		});

		String[] images={
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.jsepedzf.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.wvbmknhn.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.oorrjicu.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.woofnkar.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.tapbaxpy.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.awlngumx.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.bpuzrjch.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.nqarjlzt.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.hgadlawz.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.sdfnrwzj.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.vtbicehh.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.lhgtckcs.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.tbwyqyqm.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.eimndamh.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.nxvdfcwt.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.njoydoqk.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.ikfbfqzh.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.niqwioqm.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.tqqldmqe.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.ynokefwv.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.jodjmgxs.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.yhdaeino.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.xmgrrxef.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.pahnmknr.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.sbkwhrik.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.hwbcjnfx.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.umbuvrfe.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.krksguze.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.jionwnuf.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.dgnjindw.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.wpfmtfzp.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.gcajwhco.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.glzycglj.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.pajmxsmk.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.lamcsbwx.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.nqvsikaq.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.elyzoipc.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.oidpsvzg.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.moyzjiht.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.qizpbris.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.uadqyjbr.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.pqzeferc.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.jhotijvb.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.asztraij.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.dricykdh.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.tpysowpf.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.cawuddxy.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.vmajyyha.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.tuyoxwib.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.sanzeosx.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/dj.zfqfgoas.200x200-75.jpg",
			"http://download.java.net/media/jogl/builds/ds_tmp/mzi.uswlslxx.200x200-75.jpg"
		};

		DefaultListModel model=new DefaultListModel();
		for (String str : images) {try {model.addElement(new URL(str));} catch (Exception exc) {exc.printStackTrace();}}
		DefaultListSelectionModel selectionModel=new DefaultListSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		selectionModel.setSelectionInterval(0,0);
		final DisplayShelf shelf=new DisplayShelf(new ImageIOFetcher(),model,selectionModel,true);
		f.add(shelf,BorderLayout.CENTER);
		f.setSize(800,250);
		f.setLocation(100,100);
		f.setVisible(true);
	}
}
