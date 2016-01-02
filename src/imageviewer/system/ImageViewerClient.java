/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.system;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.io.File;
import java.lang.reflect.Field;
import java.net.InetAddress;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;

import javax.imageio.ImageIO;

import javax.media.jai.JAI;
import javax.media.jai.TileCache;
import javax.media.jai.TileScheduler;

import javax.media.j3d.VirtualUniverse;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;

import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import org.jvnet.flamingo.ribbon.JRibbon;

import imageviewer.rendering.wl.DefaultWindowLevelManager;
import imageviewer.tools.plugins.PluginLoader;

import imageviewer.ui.ApplicationPanel;
import imageviewer.ui.ApplicationContext;
import imageviewer.ui.layout.LayoutFactory;

import imageviewer.ui.swing.ImageViewerLookAndFeel;
import imageviewer.ui.swing.ImageViewerLookAndFeelAddons;
import imageviewer.ui.swing.MenuReader;
import imageviewer.ui.swing.flamingo.RibbonReader;

// =======================================================================
/* Main client program for ImageViewer. 
 * 
 */

public class ImageViewerClient {

	private static Logger LOG=Logger.getLogger("imageviewer.client");

	// =======================================================================

	JFrame mainFrame=null;
	boolean fullScreen=true;
	Timer timer=null;

	/**
	 * ImageViewerClient constructor.  Sets up JAI, the frame, menu, look and feel, 
	 * window level manager, memory management, etc.  
	 * 
	 * @param dir - directory or file path of images to open on startup 
	 * @param fullScreen - open in full screen mode?
	 */

	public ImageViewerClient(CommandLine args) {initialize(args);}

	// =======================================================================

	private void initialize(CommandLine args) {

		// First, process all the different command line arguments that we
		// need to override and/or send onwards for initialization.

		boolean fullScreen=(args.hasOption("fullscreen")) ? true : false;
		String dir=(args.hasOption("dir")) ? args.getOptionValue("dir") : null;
		String type=(args.hasOption("type")) ? args.getOptionValue("type") : "DICOM";
		String prefix=(args.hasOption("client")) ? args.getOptionValue("client") : null;

		LOG.info("Java home environment: "+System.getProperty("java.home"));

		// Logging system taken care of through properties file.  Check
		// for JAI.  Set up JAI accordingly with the size of the cache
		// tile and to recycle cached tiles as needed.

		verifyJAI();
		TileCache tc=JAI.getDefaultInstance().getTileCache();
		tc.setMemoryCapacity(32*1024*1024);
		TileScheduler ts=JAI.createTileScheduler();
		ts.setPriority(Thread.MAX_PRIORITY);
		JAI.getDefaultInstance().setTileScheduler(ts);
		JAI.getDefaultInstance().setRenderingHint(JAI.KEY_CACHED_TILE_RECYCLING_ENABLED,Boolean.TRUE);

		// Set up the frame and everything else.  First, try and set the
		// UI manager to use our look and feel because it's groovy and
		// lets us control the GUI components much better.

		try {
			UIManager.setLookAndFeel(new ImageViewerLookAndFeel());
			LookAndFeelAddons.setAddon(ImageViewerLookAndFeelAddons.class);
		} catch (Exception exc) {
			LOG.error("Could not set imageViewer L&F.");
		}

		// Load up the ApplicationContext information...

		ApplicationContext ac=ApplicationContext.getContext();
		if (ac==null) {LOG.error("Could not load configuration, exiting."); System.exit(1);}

		// Override the client node information based on command line
		// arguments...Make sure that the files are there, otherwise just
		// default to a local configuration.

		String hostname=new String("localhost");
		try {hostname=InetAddress.getLocalHost().getHostName();} catch (Exception exc) {}

		String[] gatewayConfigs=(prefix!=null) ? (new String[] {"resources/server/"+prefix+"GatewayConfig.xml",(String)ac.getProperty(ApplicationContext.IMAGESERVER_GATEWAY_CONFIG),
																														"resources/server/"+hostname+"GatewayConfig.xml","resources/server/localGatewayConfig.xml"}) :
			                                       (new String[] {(String)ac.getProperty(ApplicationContext.IMAGESERVER_GATEWAY_CONFIG),"resources/server/"+hostname+"GatewayConfig.xml",
																														"resources/server/localGatewayConfig.xml"});
		String[] nodeConfigs=(prefix!=null) ? (new String[] {"resources/server/"+prefix+"NodeConfig.xml",(String)ac.getProperty(ApplicationContext.IMAGESERVER_CLIENT_NODE),
																												 "resources/server/"+hostname+"NodeConfig.xml","resources/server/localNodeConfig.xml"}) :
			                                    (new String[] {(String)ac.getProperty(ApplicationContext.IMAGESERVER_CLIENT_NODE),"resources/server/"+hostname+"NodeConfig.xml",
																												 "resources/server/localNodeConfig.xml"});	

		for (int loop=0; loop<gatewayConfigs.length; loop++) {
			String s=gatewayConfigs[loop];
			if ((s!=null)&&(s.length()!=0)&&(!"null".equals(s))) {
				File f=new File(s);
				if (f.exists()) {
					ac.setProperty(ApplicationContext.IMAGESERVER_GATEWAY_CONFIG,s);
					break;
				}
			}
		}

		LOG.info("Using gateway config: "+ac.getProperty(ApplicationContext.IMAGESERVER_GATEWAY_CONFIG));

		for (int loop=0; loop<nodeConfigs.length; loop++) {
			String s=nodeConfigs[loop];
			if ((s!=null)&&(s.length()!=0)&&(!"null".equals(s))) {
				File f=new File(s);
				if (f.exists()) {
					ac.setProperty(ApplicationContext.IMAGESERVER_CLIENT_NODE,s);
					break;
				}
			}
		}
		LOG.info("Using client config: "+ac.getProperty(ApplicationContext.IMAGESERVER_CLIENT_NODE));
		
		// Load the layouts and set the default window/level manager...
		
		LayoutFactory.initialize();
		DefaultWindowLevelManager dwlm=new DefaultWindowLevelManager();

		// Create the main JFrame, set its behavior, and let the
		// ApplicationPanel know the glassPane and layeredPane.  Set the
		// menubar based on reading in the configuration menus.

		mainFrame=new JFrame("imageviewer");
		try {
			ArrayList<Image> iconList=new ArrayList<Image>();
			iconList.add(ImageIO.read(new File("resources/icons/mii.png")));
			iconList.add(ImageIO.read(new File("resources/icons/mii32.png")));
			mainFrame.setIconImages(iconList);
		} catch (Exception exc) {}

		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				int confirm=ApplicationPanel.getInstance().showDialog("Are you sure you want to quit imageViewer?",null,JOptionPane.QUESTION_MESSAGE,
																															JOptionPane.OK_CANCEL_OPTION,UIManager.getIcon("Dialog.shutdownIcon"));
				if (confirm==JOptionPane.OK_OPTION) {
					boolean hasUnsaved=SaveStack.getInstance().hasUnsavedItems();
					if (hasUnsaved) {
						int saveResult=ApplicationPanel.getInstance().showDialog("There is still unsaved data.  Do you want to save this data in the local archive?",null,
																																		 JOptionPane.QUESTION_MESSAGE,JOptionPane.YES_NO_CANCEL_OPTION);
						if (saveResult==JOptionPane.CANCEL_OPTION) return;
						if (saveResult==JOptionPane.YES_OPTION) SaveStack.getInstance().saveAll();
					}
					LOG.info("Shutting down imageServer local archive...");
					try {
						ImageViewerClientNode.getInstance().shutdown();
					} catch (Exception exc) {
						LOG.error("Problem shutting down imageServer local archive...");
					} finally {
						System.exit(0);
					}
				}
			}
		});

		String menuFile=(String)ApplicationContext.getContext().getProperty(ac.CONFIG_MENUS);
		String ribbonFile=(String)ApplicationContext.getContext().getProperty(ac.CONFIG_RIBBON);
		if (menuFile!=null) {
			JMenuBar mb=new JMenuBar();
			mb.setBackground(Color.black);
			MenuReader.parseFile(menuFile,mb);
			mainFrame.setJMenuBar(mb);
			ApplicationContext.getContext().setApplicationMenuBar(mb);
		} else if (ribbonFile!=null) {
			RibbonReader rr=new RibbonReader();
			JRibbon jr=rr.parseFile(ribbonFile);
			mainFrame.getContentPane().add(jr,BorderLayout.NORTH);
			ApplicationContext.getContext().setApplicationRibbon(jr);
		}
		mainFrame.getContentPane().add(ApplicationPanel.getInstance(),BorderLayout.CENTER);
		ApplicationPanel.getInstance().setGlassPane((JPanel)(mainFrame.getGlassPane()));
		ApplicationPanel.getInstance().setLayeredPane(mainFrame.getLayeredPane());

		// Load specified plugins...has to occur after the menus are
		// created, btw.

		PluginLoader.initialize("config/plugins.xml");

		// Detect operating system...

		String osName=System.getProperty("os.name");
		ApplicationContext.getContext().setProperty(ApplicationContext.OS_NAME,osName);
		LOG.info("Detected operating system: "+osName);

		// Try and hack the searched library paths if it's windows so we
		// can add a local dll path...

		try {
			if (osName.contains("Windows")) {
				Field f=ClassLoader.class.getDeclaredField("usr_paths");
				f.setAccessible(true);
				String[] paths=(String[])f.get(null);
				String[] tmp=new String[paths.length+1];
				System.arraycopy(paths,0,tmp,0,paths.length);
				File currentPath=new File(".");
				tmp[paths.length]=currentPath.getCanonicalPath()+"/lib/dll/";
				f.set(null,tmp);
				f.setAccessible(false);
			}
		} catch (Exception exc) {
			LOG.error("Error attempting to dynamically set library paths.");
		}
		
		// Get screen resolution...

		GraphicsConfiguration gc=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
		Rectangle r=gc.getBounds();
		LOG.info("Detected screen resolution: "+(int)r.getWidth()+"x"+(int)r.getHeight());

		// Try and see if Java3D is installed, and if so, what version...

		try {
			VirtualUniverse vu=new VirtualUniverse();
			Map m=vu.getProperties();
			String s=(String)m.get("j3d.version");
			LOG.info("Detected Java3D version: "+s);
		} catch (Throwable t) {
			LOG.info("Unable to detect Java3D installation");
		}

		// Try and see if native JOGL is installed...

		try {
			System.loadLibrary("jogl");
			ac.setProperty(ApplicationContext.JOGL_DETECTED,Boolean.TRUE);
			LOG.info("Detected native libraries for JOGL");
		} catch (Throwable t) {
			LOG.info("Unable to detect native JOGL installation");
			ac.setProperty(ApplicationContext.JOGL_DETECTED,Boolean.FALSE);
		}

		// Start the local client node to connect to the network and the
		// local archive running on this machine...Thread the connection
		// process so it doesn't block the imageViewerClient creating this
		// instance.  We may not be connected to the given gateway, so the
		// socketServer may go blah...

		final boolean useNetwork=(args.hasOption("nonet")) ? false : true;
		ApplicationPanel.getInstance().addStatusMessage("ImageViewer is starting up, please wait...");
		if (useNetwork) LOG.info("Starting imageServer client to join network - please wait...");
		Thread t=new Thread(new Runnable() {
			public void run() {
				try {
					ImageViewerClientNode.getInstance((String)ApplicationContext.getContext().getProperty(ApplicationContext.IMAGESERVER_GATEWAY_CONFIG),
																						(String)ApplicationContext.getContext().getProperty(ApplicationContext.IMAGESERVER_CLIENT_NODE),useNetwork);
					ApplicationPanel.getInstance().addStatusMessage("Ready");
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		});
		t.setPriority(9);
		t.start();

		this.fullScreen=fullScreen;
		mainFrame.setUndecorated(fullScreen);

		// Set the view to encompass the default screen.

		if (fullScreen) {
			Insets i=Toolkit.getDefaultToolkit().getScreenInsets(gc);
			mainFrame.setLocation(r.x+i.left,r.y+i.top);
			mainFrame.setSize(r.width-i.left-i.right,r.height-i.top-i.bottom);
		} else {
			mainFrame.setSize(1100,800);
			mainFrame.setLocation(r.x+200,r.y+100);
		}

		if (dir!=null) ApplicationPanel.getInstance().load(dir,type);

		timer=new Timer();
		timer.schedule(new GarbageCollectionTimer(),5000,2500);
		mainFrame.setVisible(true);
	}

	// =======================================================================

	public boolean isFullScreen() {return fullScreen;}
	public void setFullScreen(boolean x) {fullScreen=x;}

	public JFrame getMainFrame() {return mainFrame;}

	// =======================================================================

	private void verifyJAI() {

		// JAI Detection

  	boolean dllPassFlag=false;
		try {
			Class.forName("javax.media.jai.JAI");
		} catch (ClassNotFoundException exc) {
			JFrame frame=new JFrame("mii imageviewer");
			JOptionPane.showMessageDialog(frame, "JAI is not installed on this machine. Please install.");
			exc.printStackTrace();
			System.exit(1);
		}
		
		// JAI DLL Detection

		try {
			System.load(System.getProperty("java.home")+"/bin/mlib_jai.dll");
			dllPassFlag=true;
		}	catch (Exception exc) {
			dllPassFlag=false;
		}	catch (UnsatisfiedLinkError exc22) {
			dllPassFlag=false;
		}

		// Don't ever check for *.so objects!  Breaks things on Linux! -gsw
		
		if (dllPassFlag==false) {
			LOG.error("Missing JAI DLL or system libraries.");
		} else {
			LOG.info("Java Advanced Imaging (JAI) version: "+JAI.getBuildVersion());
		}
	}

	// =======================================================================

	public static void main(String[] args) {

		// Use Apache command-line interface (CLI) to set up the argument
		// handling, etc. 

		Option help=new Option("help","Print this message");
		Option fullScreen=new Option("fullscreen","Run imageViewer in full screen mode");
		Option noNetwork=new Option("nonet","No network connectivity");
		Option client=OptionBuilder.withArgName("prefix").hasArg().withDescription("Specify client node (prefix) for imageServer").create("client");
		Option imageType=OptionBuilder.withArgName("image type").hasArg().withDescription("Open as image type").create("type");
		Option imageDir=OptionBuilder.withArgName("image directory").hasArg().withDescription("Open images in directory").create("dir");
		Option property=OptionBuilder.withArgName("property=value").hasArg().withValueSeparator().withDescription("use value for given property").create("D");

		Options o=new Options();
		o.addOption(help);
		o.addOption(client);
		o.addOption(imageDir);
		o.addOption(fullScreen);
		o.addOption(noNetwork);
		o.addOption(property);

		try {
			CommandLineParser parser=new GnuParser();
			CommandLine line=parser.parse(o,args);
			String jaasConfig=System.getProperty("java.security.auth.login.config");
			if (jaasConfig==null) System.setProperty("java.security.auth.login.config","config/jaas.config");
			String antialising=new String("swing.aatext");
			if (System.getProperty(antialising)==null) System.setProperty(antialising,"true");
			@SuppressWarnings("unused") 
			ImageViewerClient ivc=new ImageViewerClient(line);
		} catch (UnrecognizedOptionException uoe) {
			System.err.println("Unknown argument: "+uoe.getMessage());
			HelpFormatter hf=new HelpFormatter();
			hf.printHelp("ImageViewerClient",o);
			System.exit(1);
		} catch (Exception exc) {
			exc.printStackTrace();
		}
	}
}
