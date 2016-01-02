/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.image.j3d;

import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;

import java.util.Timer;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.TileScheduler;

import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.GraphicsConfigTemplate3D;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.View;
import javax.media.j3d.VirtualUniverse;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.sun.j3d.utils.behaviors.mouse.MouseBehaviorCallback;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;

import com.sun.j3d.exp.swing.JCanvas3D;

import com.sun.j3d.utils.universe.SimpleUniverse;

import imageviewer.model.ImageSequence;
import imageviewer.system.GarbageCollectionTimer;

import imageviewer.ui.image.j3d.mouse.MouseDragEvaluator;
import imageviewer.ui.image.j3d.mouse.MouseRotator;

// =======================================================================

public class TextureModel3D extends JPanel implements MouseBehaviorCallback {

	Canvas3D canvas3D=null;
	JCanvas3D jc3D=null;
	BranchGroup bg=null;
	SimpleUniverse su=null;
	TransformGroup tg=null;

	TextureVolumeData tvd=null;
	TextureVolumeRenderer tvr=null;
	TextureVolumeRenderingEngine tvre=null;
	VolumeAnnotationSet vas=null;
	AnnotationRenderingEngine are=null;

	public TextureModel3D(String directory, String imageType, int threshold) {initialize(new TextureVolumeData(directory,imageType,threshold));}
	public TextureModel3D(ImageSequence is, int threshold) {initialize(new TextureVolumeData(is,threshold));}

	// =======================================================================

	private void initialize(TextureVolumeData tvd) {

		setLayout(new BorderLayout());
		GraphicsConfiguration config=SimpleUniverse.getPreferredConfiguration();
		canvas3D=new Canvas3D(config);
		canvas3D.setDoubleBufferEnable(true);
		add(canvas3D,BorderLayout.CENTER);
		// GraphicsConfigTemplate3D config=new GraphicsConfigTemplate3D();
		// jc3D=new JCanvas3D();
		// jc3D.setSize(512,512);
		// add(jc3D,BorderLayout.CENTER);
		su=new SimpleUniverse(canvas3D);
		su.getViewingPlatform().setNominalViewingTransform();
		View v=su.getViewer().getView();
		v.setProjectionPolicy(View.PARALLEL_PROJECTION);
		v.setSceneAntialiasingEnable(true);
		v.setMinimumFrameCycleTime(20);
		createBaseSceneGraph(tvd);
		setup();
		bg.compile();
    su.addBranchGraph(bg);
		are.update();
	}

	// =======================================================================

	private BranchGroup createBaseSceneGraph(TextureVolumeData tvd) {

		bg=new BranchGroup();
		BoundingSphere bs=new BoundingSphere();
		View view=su.getViewer().getView();

		Background bground=new Background(0.0f,0.0f,0.0f);		
		bground.setApplicationBounds(bs);
		bg.addChild(bground);

		tg=new TransformGroup();
    tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
		bg.addChild(tg);

		// Add basic mouse behavior for rotating

		MouseRotator mr=new MouseRotator();
		mr.setMouseCondition(new MouseDragEvaluator());
		mr.setupCallback(this);
		mr.setFactor(0.025);
		mr.setTransformGroup(tg);
		mr.setSchedulingBounds(bs);
		bg.addChild(mr);

		// Set up the corresponding image annotation sets and the actual
		// rendering engine.

		this.tvd=tvd;
		tvr=new TextureVolumeRenderer(tvd);
		tvre=new TextureVolumeRenderingEngine(view,tvd,tvr,false);
		vas=new VolumeAnnotationSet();

		// Attach the annotation sets to the rendered imaging volume, and
		// call the update to set things in motion.  Add the final volume
		// to the top transform group.
	
		are=new AnnotationRenderingEngine(view,tvd,vas);
		are.attachFront(tvr.getDynamicFrontAnnotationSwitch(),tvr.getStaticFrontAnnotationSwitch());
		are.attachBack(tvr.getDynamicBackAnnotationSwitch(),tvr.getStaticBackAnnotationSwitch());
		tvre.attach(tvr.getDynamicAttachGroup(),tvr.getStaticAttachGroup());
		tvre.update();
		tg.addChild(tvr.getVolume());

		return bg;
	}

	// =======================================================================

	public BranchGroup getRoot() {return bg;}
	public TransformGroup getRootTransformGroup() {return tg;}
	public VolumeAnnotationSet getAnnotationSet() {return vas;}
	public TextureVolumeRenderer getVolumeRenderer() {return tvr;}
	public Canvas3D getCanvas() {return canvas3D;}

	// =======================================================================
	// Implement the MouseBehaviorCallback interface method.  Inform the
	// underlying rendering engine that a transform change has occurred

	public void transformChanged(int type, Transform3D transform) {

		tvre.transformChanged(type,transform);
		tvre.viewpointChange();
		are.update();
	}

	// =======================================================================
	// Customize the model view with additional behaviors and the such
	// by changing the setup() method below.  Once the base model is
	// constructed, the constructed will call the setup() to add any
	// additional items.

	protected void setup() {

		ClipBox cb=new ClipBox(this);
		// RotatingClipPlane rpc=new RotatingClipPlane(tvr.getCenter(),tvr.getScaleFactor());
		// tg.addChild(rpc.getTransformGroup());
	}

	// =======================================================================

	public static void main(String[] args) {

		if (args.length>=1) {
			JFrame frame=new JFrame("DICOM 3D Volume Renderer");  
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			TileCache tc=JAI.getDefaultInstance().getTileCache();
			tc.setMemoryCapacity(32*1024*1024);
			TileScheduler ts=JAI.createTileScheduler();
			ts.setPriority(Thread.MAX_PRIORITY);
			JAI.getDefaultInstance().setTileScheduler(ts);
			JAI.getDefaultInstance().setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED,Boolean.TRUE);
			Timer timer=new Timer();
			timer.schedule(new GarbageCollectionTimer(),5000,2500);
			int threshold=(args.length==2) ? Integer.parseInt(args[1]) : 0;
			VirtualUniverse.setJ3DThreadPriority(Thread.NORM_PRIORITY+1);
			TextureModel3D panel=new TextureModel3D(args[0],"DICOM",threshold);
			frame.setContentPane(panel);
			frame.setSize(512,512);
			frame.setVisible(true);
		}
	}
}
