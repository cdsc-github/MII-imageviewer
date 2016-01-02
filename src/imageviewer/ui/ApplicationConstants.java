/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui;

public interface ApplicationConstants {

	// Properties

	public static final String ACTIVE_LAYOUT=new String("__ACTIVE_LAYOUT");
	public static final String ASK_CHANGE_LAYOUT=new String("__ASK_CHANGE_LAYOUT");
	public static final String ASK_CHANGE_LAYOUT_COMMAND=new String("Warn on layout change");
	public static final String ASK_CLOSE_TAB=new String("__ASK_CLOSE_TAB");
	public static final String ASK_CLOSE_TAB_COMMAND=new String("Warn on closing tab");
	public static final String ASK_ON_SAVE=new String("__ASK_ON_SAVE");
	public static final String CONFIG_LAYOUTS=new String("CONFIG_LAYOUTS");
	public static final String CONFIG_MENUS=new String("CONFIG_MENUS");
	public static final String CONFIG_PIPELINE=new String("CONFIG_PIPELINE");
	public static final String CONFIG_PLUGINS=new String("CONFIG_PLUGINS");
	public static final String CONFIG_RIBBON=new String("CONFIG_RIBBON");
	public static final String CONFIG_TOOLBAR=new String("CONFIG_TOOLBAR");
	public static final String CONFIG_WINDOW_LEVEL=new String("CONFIG_WINDOW_LEVEL");
	public static final String CURRENT_DIRECTORY=new String("__CURRENT_DIRECTORY");
	public static final String DEFAULT_IMAGE_CACHE_SIZE=new String("DEFAULT_IMAGE_CACHE_SIZE");
	public static final String DISPLAY_GRID=new String("DISPLAY_GRID");
	public static final String DISPLAY_IMAGE_INFORMATION=new String("DISPLAY_IMAGE_INFORMATION");
	public static final String DISPLAY_IMAGE_PROP_WINDOW=new String("DISPLAY_IMAGE_PROP_WINDOW");
	public static final String DISPLAY_PARTIAL_PANELS=new String("DISPLAY_PARTIAL_PANELS");
	public static final String DISPLAY_PRESENTATION_STATES=new String("DISPLAY_PRESENTATION_STATES");
	public static final String IMAGESERVER_CLIENT_NODE=new String("IMAGESERVER_CLIENT_NODE");
	public static final String IMAGESERVER_GATEWAY_CONFIG=new String("IMAGESERVER_GATEWAY_CONFIG");
	public static final String JAVA_VERSION=new String("__JAVA_VERSION");
	public static final String JOGL_DETECTED=new String("__JOGL_NATIVE");
	public static final String LAYOUT_IN_PROGRESS=new String("__LAYOUT_IN_PROGRESS");
	public static final String MAXIMUM_MEMORY_THRESHOLD=new String("MAXIMUM_MEMORY_THRESHOLD");
	public static final String MEMORY_THRESHOLD=new String("MEMORY_THRESHOLD");
	public static final String MOVE_IN_PROGRESS=new String("__MOVE_IN_PROGRESS");
	public static final String OS_NAME=new String("__OS_NAME");
	public static final String PLUGIN_CONFIG_ACTIVE_CONTOUR=new String("PLUGIN_CONFIG_ACTIVE_CONTOUR");
	public static final String PLUGIN_CONFIG_MAGIC_WAND=new String("PLUGIN_CONFIG_MAGIC_WAND");
	public static final String SCREEN_RESOLUTION=new String("__SCREEN_RESOLUTION");
	public static final String UNDO_LEVEL=new String("UNDO_LEVEL");
	public static final String USE_IMAGE_COMPRESSION=new String("USE_IMAGE_COMPRESSION");
	public static final String USE_XML_COMPRESSION=new String("USE_XML_COMPRESSION");
	public static final String WARN_NO_LOGIN=new String("__WARN_NO_LOGIN");
	public static final String WARN_PS_DELETE=new String("__WARN_PS_DELETE");

	public static final String LAST_ACTION_STATUS=new String("__LAST_ACTION_STATUS");

	public static final String IGNORE_VISIBLE_PANEL_CHECK=new String("__IGNORE_VISIBLE_CHECKS");

	// Button commands

	public static final String DISPLAY_GRID_COMMAND=new String("Display grid");
	public static final String DISPLAY_IMAGE_INFORMATION_COMMAND=new String("Display image information");
	public static final String DISPLAY_IMAGE_PROP_WINDOW_COMMAND=new String("Display image property window");
	public static final String DISPLAY_PARTIAL_PANELS_COMMAND=new String("Display partial panels");
	public static final String DISPLAY_PRESENTATION_STATES_COMMAND=new String("Display presentation states");
}
