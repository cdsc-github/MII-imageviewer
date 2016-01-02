/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Image;
import java.awt.Insets;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.UIDefaults;

import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import javax.swing.plaf.IconUIResource;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.plaf.metal.MetalLookAndFeel;

import imageviewer.ui.swing.border.AlphaFilledCurvedBorder;
import imageviewer.ui.swing.border.ButtonBorder;
import imageviewer.ui.swing.border.CurvedBorder;
import imageviewer.ui.swing.border.DropShadowRectangularBorder;

import imageviewer.ui.swing.flamingo.DropDownArrowIcon;
import imageviewer.ui.swing.flamingo.ExpandArrowIcon;

import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.GlossPainter;
import org.jdesktop.swingx.painter.MattePainter;
import org.jdesktop.swingx.painter.RectanglePainter;

// =======================================================================
/**
 * Our customized ImageViewer Swing Look and Feel.
 * <p>
 * This features nice rounded corners, translucent popups, and drop-shadows.
 */

public class ImageViewerLookAndFeel extends MetalLookAndFeel {

	private static final long serialVersionUID=-1706251893676238427L;

	protected ImageViewerTheme theme=null;

	/**
	 * @see javax.swing.LookAndFeel#getName()
	 */
	public String getName() {return new String("imageViewer L&F");}

	// =======================================================================
	/**
	 * @see javax.swing.plaf.metal.MetalLookAndFeel#createDefaultTheme()
	 */
	protected void createDefaultTheme() {theme=new ImageViewerTheme(); MetalLookAndFeel.setCurrentTheme(theme);}

	// =======================================================================

	/**
	 * @see javax.swing.LookAndFeel#initialize()
	 */
	public void initialize() {super.initialize(); BasicPopupFactory.install();}
	
	/**
	 * @see javax.swing.LookAndFeel#uninitialize()
	 */
	public void uninitialize() {super.uninitialize(); BasicPopupFactory.uninstall();}
    
	// =======================================================================
	/**
	 * @see javax.swing.LookAndFeel#getDisabledIcon(javax.swing.JComponent, javax.swing.Icon)
	 */
	public Icon getDisabledIcon(JComponent component, Icon icon) {

		if (icon instanceof MinimumSizedIcon) {
			MinimumSizedIcon msi=(MinimumSizedIcon)icon;
			Icon imageIcon=msi.getIcon();
			if (imageIcon instanceof ImageIcon) {
				Image i=GrayFilter.createDisabledImage(((ImageIcon)imageIcon).getImage());  // Use custom gray filter to get better grayed icons
				return new IconUIResource(new MinimumSizedIcon(new ImageIcon(i)));
			}
		}
		return super.getDisabledIcon(component,icon);
	}

	// =======================================================================
	/**
	 * @see javax.swing.plaf.basic.BasicLookAndFeel#initClassDefaults(javax.swing.UIDefaults)
	 */

	protected void initClassDefaults(UIDefaults table) {

		super.initClassDefaults(table);
		table.putDefaults(new Object[] {

			"ButtonUI","imageviewer.ui.swing.ButtonUI",	
			"CheckBoxMenuItemUI","imageviewer.ui.swing.CheckBoxMenuItemUI",
			"ComboBoxUI","imageviewer.ui.swing.ComboBoxUI",
			"FileChooserUI","imageviewer.ui.swing.FileChooserUI",
			"FormattedTextFieldUI","imageviewer.ui.swing.TextFieldUI",
			"LabelUI","imageviewer.ui.swing.LabelUI",
			"MenuBarUI","imageviewer.ui.swing.MenuBarUI",
			"MenuItemUI","imageviewer.ui.swing.MenuItemUI",
			"MenuUI","imageviewer.ui.swing.MenuUI",
			"PopupMenuUI","imageviewer.ui.swing.PopupMenuUI",
			"PopupMenuSeparatorUI","imageviewer.ui.swing.PopupMenuSeparatorUI",
			"RadioButtonMenuItemUI","imageviewer.ui.swing.RadioButtonMenuItemUI",
			"SliderUI","imageviewer.ui.swing.SliderUI",
			"TabbedPaneUI","imageviewer.ui.swing.TabbedPaneUI",
			"ToggleButtonUI","imageviewer.ui.swing.ToggleButtonUI",
			"ToolBarUI","imageviewer.ui.swing.ToolbarUI",
			"OptionPaneUI","imageviewer.ui.swing.OptionPaneUI",
			"PasswordFieldUI","imageviewer.ui.swing.PasswordFieldUI",
			"ScrollBarUI","imageviewer.ui.swing.ScrollBarUI",
			"SpinnerUI","imageviewer.ui.swing.SpinnerUI",
			"TextFieldUI","imageviewer.ui.swing.TextFieldUI",
			"TextAreaUI","imageviewer.ui.swing.TextAreaUI",

			// Flamingo/JRibbon overrides

			"BandControlPanelUI","imageviewer.ui.swing.flamingo.ImageViewerBandControlPanelUI",
			"ButtonStripUI","imageviewer.ui.swing.flamingo.ImageViewerButtonStripUI",
			"CommandButtonUI","imageviewer.ui.swing.flamingo.ImageViewerRibbonButtonUI",
			"CommandButtonPanelUI","imageviewer.ui.swing.flamingo.ImageViewerCommandButtonPanelUI",
			"CommandToggleButtonUI","imageviewer.ui.swing.flamingo.CommandToggleButtonUI",
			"RibbonBandUI","imageviewer.ui.swing.flamingo.ImageViewerRibbonBandUI",
			"RibbonGalleryUI","imageviewer.ui.swing.flamingo.ImageViewerRibbonGalleryUI",
			"RibbonUI","imageviewer.ui.swing.flamingo.ImageViewerRibbonUI",
			"PopupPanelUI","imageviewer.ui.swing.flamingo.ImageViewerPopupPanelUI",
			"ToggleTabButtonUI","imageviewer.ui.swing.flamingo.ImageViewerToggleTabButtonUI",
			"RibbonMultiButtonUI","imageviewer.ui.swing.flamingo.RibbonMultiButtonUI",

			// "CheckBoxUI","com.stefankrause.xplookandfeel.XPCheckBoxUI",
			// "ScrollPaneUI","com.stefankrause.xplookandfeel.XPScrollPaneUI",
			// "ProgressBarUI","com.stefankrause.xplookandfeel.XPProgressBarUI",
			// "InternalFrameUI","com.stefankrause.xplookandfeel.XPInternalFrameUI",
			// "RadioButtonUI","com.stefankrause.xplookandfeel.XPRadioButtonUI",
		});
	}

	// =======================================================================
	/**
	 * @see javax.swing.plaf.basic.BasicLookAndFeel#initComponentDefaults(javax.swing.UIDefaults)
	 */
	protected void initComponentDefaults(UIDefaults table) {

		super.initComponentDefaults(table);
		table.put("controlHighlight",Color.darkGray);

		Border border=new EmptyBorder(2,2,2,2);

		// Color backgroundPanelColor=new Color(25,35,50);
		Color backgroundPanelColor=new Color(16,16,16);
		Color checkColor=new Color(0,128,0);
		Color glossDarkColor=new Color(25,35,50);
		Color glossHighlightColor=new Color(59,67,79);
		Color glossSelectedDarkColor=glossDarkColor.brighter();
		Color glossSelectedHighlightColor=new Color(110,110,120);
		Color selectionColor=Color.darkGray;
		Color textColor=Color.white;
		Color menubarColor=new Color(68,72,80);
		/*
		GlossPainter gp1=new GlossPainter();
		gp1.setPaint(new Color(1.0f,1.0f,1.0f,0.2f));
		gp1.setPosition(GlossPainter.GlossPosition.TOP);
		GradientPaint gpaint1=new GradientPaint(0,0,glossHighlightColor.darker(),0,10,new Color(98,110,120));
		MattePainter mp1=new MattePainter(gpaint1);
		mp1.setPaintStretched(true);
		mp1.setAntialiasing(true);
		CompoundPainter glossBlackPainter=new CompoundPainter(mp1,gp1);
		glossBlackPainter.setAntialiasing(true);
		*/
		Point2D start=new Point2D.Float(0,0);
		Point2D end=new Point2D.Float(0,100);
		float[] dist={0.0f,0.49f,0.5f,1.0f};
		Color[] colors2={glossHighlightColor.brighter(),glossHighlightColor,glossDarkColor,glossDarkColor.brighter()};
		LinearGradientPaint gpaint1=new LinearGradientPaint(start,end,dist,colors2);
		MattePainter glossBlackPainter=new MattePainter(gpaint1);
		glossBlackPainter.setPaintStretched(true);
		glossBlackPainter.setAntialiasing(true);
		/*
		GlossPainter gp2=new GlossPainter();
		gp2.setPaint(new Color(1.0f,1.0f,1.0f,0.2f));
		gp2.setPosition(GlossPainter.GlossPosition.TOP);
		GradientPaint gpaint2=new GradientPaint(0,0,glossHighlightColor.darker(),0,10,glossDarkColor.darker());
		MattePainter mp2=new MattePainter(gpaint2);
		mp2.setPaintStretched(true);
		mp2.setAntialiasing(true);
		CompoundPainter darkGlossBlackPainter=new CompoundPainter(mp2,gp2);
		darkGlossBlackPainter.setAntialiasing(true);
		*/

		// Color[] colors={new Color(146,146,146),new Color(96,96,96),Color.BLACK,new Color(24,24,24)};
		Color[] colors={new Color(97,105,135),new Color(50,50,50),new Color(17,17,17),new Color(63,71,86)};
		LinearGradientPaint lgp1=new LinearGradientPaint(start,end,dist,colors);
		MattePainter darkGlossBlackPainter=new MattePainter(lgp1);
		darkGlossBlackPainter.setPaintStretched(true);
		darkGlossBlackPainter.setAntialiasing(true);
		glossBlackPainter=darkGlossBlackPainter;

 		RectanglePainter buttonBackgroundPainter=new RectanglePainter(1,1,1,1,4,4,true,new Color(0,0,0,0.4f),0,null);
		buttonBackgroundPainter.setAntialiasing(true);
		MattePainter mp3=new MattePainter(glossHighlightColor);
		MattePainter mp4=new MattePainter(menubarColor);
		MattePainter highlightMP=new MattePainter(new Color(141,182,205,128));
		
		table.put("Button.backgroundPainter",buttonBackgroundPainter);
		table.put("Button.border",new CompoundBorder(new ButtonBorder(),new BasicBorders.MarginBorder()));
		table.put("Button.disabled",Color.darkGray);
		table.put("Button.disabledText",Color.gray); 
		table.put("Button.focus",new Color(0f,0f,0f,0f));
		table.put("Button.foreground",Color.white);
		table.put("Button.gradientBackground",lgp1); 
		// table.put("Button.gradientBackground",new GradientPaint(2,2,glossDarkColor,2,10,glossHighlightColor));
		table.put("Button.pressed",Color.orange);
		table.put("CheckBox.check",checkColor);
		table.put("CheckBox.background",backgroundPanelColor);
		table.put("CheckBox.foreground",textColor);
		table.put("CheckBox.icon",IconFactory.getCheckBoxIcon());
		table.put("CheckBoxMenuItem.acceleratorSelectionForeground",Color.white);
		table.put("CheckBoxMenuItem.border",border);
		table.put("CheckBoxMenuItem.checkIcon",IconFactory.getCheckBoxMenuItemIcon());
		table.put("CheckBoxMenuItem.disabledForeground",Color.darkGray);
		table.put("CheckBoxMenuItem.selectionBackground",selectionColor);
		table.put("CheckBoxMenuItem.selectionForeground",Color.white);
		table.put("ComboBox.background",Color.darkGray); 
		table.put("ComboBox.borderPaintsFocus",Boolean.TRUE);
		table.put("ComboBox.foreground",textColor); 
		table.put("ComboBox.selectionBackground",selectionColor);
		table.put("ComboBox.selectionForeground",Color.white); 
		table.put("Dialog.shutdownIcon",new ImageIcon("resources/icons/swing/logout.png"));
		table.put("Dialog.loginIcon",new ImageIcon("resources/icons/swing/login.png"));
		table.put("EditorPane.font",theme.getControlTextFont());
		table.put("FileChooser.detailsViewIcon",new ImageIcon("resources/icons/swing/files/detailView.png"));
		table.put("FileChooser.fileNameLabelText","File Name");
		table.put("FileChooser.filesOfTypeLabelText","Files of Type");
		table.put("FileChooser.homeFolderIcon",new ImageIcon("resources/icons/swing/files/home.png"));
		table.put("FileChooser.listViewIcon",new ImageIcon("resources/icons/swing/files/listView.png"));
		table.put("FileChooser.lookInLabelText","Look In");
		table.put("FileChooser.newFolderIcon",new ImageIcon("resources/icons/swing/swing/files/newFolder.png"));
		table.put("FileChooser.readOnly",Boolean.TRUE);
		table.put("FileChooser.saveInLabelText","Save In");
		table.put("FileChooser.upFolderIcon",new ImageIcon("resources/icons/swing/files/upDirectory.png")); 
		table.put("FileView.computerIcon",new ImageIcon("resources/icons/swing/tree/computer.png"));
		table.put("FileView.directoryIcon",new ImageIcon("resources/icons/swing/tree/closed.png"));
		table.put("FileView.fileIcon",new ImageIcon("resources/icons/tree/file.png"));
		table.put("FileView.floppyDriveIcon",new ImageIcon("resources/icons/swing/files/floppy.png"));
		table.put("FileView.hardDriveIcon",new ImageIcon("resources/icons/swing/files/hardDrive.png"));
		table.put("FloatingPanel.background",backgroundPanelColor); 
		table.put("FloatingPanel.backgroundPainter",new MattePainter(new Color(20,30,45,192))); 
		table.put("FloatingPanel.titlePainter",glossBlackPainter); 
		table.put("FloatingPanel.closeIcon",new ImageIcon("resources/icons/swing/close.png"));
		table.put("FloatingPanel.closeRolloverIcon",new ImageIcon("resources/icons/swing/closeRollover.png"));
		table.put("JXDatePicker.border",new CompoundBorder(new CurvedBorder(Color.black,6),border));
		table.put("JXMonthView.background",backgroundPanelColor);
		table.put("JXMonthView.daysOfTheWeekForeground",Color.lightGray);
		table.put("JXMonthView.monthStringBackground",new Color(0,0,0,0));
		table.put("JXMonthView.monthStringForeground",Color.white);
		table.put("JXMonthView.selectedBackground",Color.darkGray);
		table.put("Label.foreground",Color.white);
		table.put("Label.background",backgroundPanelColor);
		table.put("List.background",glossDarkColor);
		table.put("List.foreground",Color.white);
		table.put("List.selectionBackground",selectionColor);
		table.put("List.selectionForeground",Color.white);
		table.put("LoginLabel.highlightColor",Color.orange);
		table.put("Menu.border",new EmptyBorder(2,4,2,4));
		table.put("Menu.foreground",Color.white);
		table.put("Menu.opaque",Boolean.FALSE);
		table.put("Menu.selectionBackground",selectionColor);
		table.put("Menu.selectionForeground",Color.white);
		table.put("MenuBar.backgroundPainter",mp4);
		table.put("MenuBar.foreground",Color.white);
		table.put("MenuItem.acceleratorForeground",Color.white);
		table.put("MenuItem.acceleratorSelectionForeground",Color.white);
		table.put("MenuItem.border",border);
		table.put("MenuItem.disabledForeground",Color.darkGray);
		table.put("MenuItem.selectionBackground",Color.gray);
		table.put("MenuItem.selectionForeground",Color.white);
		table.put("MultiButton.arrowColor",Color.lightGray);
		table.put("MultiButton.separator",Color.darkGray);
		table.put("OptionPane.background",backgroundPanelColor);
		table.put("OptionPane.border",new CompoundBorder(new CurvedBorder(backgroundPanelColor,6,2,true),border));
		table.put("OptionPane.errorIcon",new ImageIcon("resources/icons/swing/error.png"));
		table.put("OptionPane.font",theme.getControlTextFont());
		table.put("OptionPane.informationIcon",new ImageIcon("resources/icons/swing/information.png"));
		table.put("OptionPane.questionIcon",new ImageIcon("resources/icons/swing/questionmark.png"));
		table.put("OptionPane.warningIcon",new ImageIcon("resources/icons/swing/warn.png"));
		table.put("OutlookBar.border",new EmptyBorder(0,0,0,0));
		table.put("PSThumbnail.dragHighlightColor",Color.white);
		table.put("PSThumbnail.selectColor",Color.red);
		table.put("PasswordField.background",Color.gray);
		table.put("PasswordField.border",new CompoundBorder(new CurvedBorder(Color.black,6),border));
		table.put("PasswordField.font",theme.getControlTextFont());
		table.put("PasswordField.foreground",Color.white);
		table.put("PopupMenu.background",Color.gray);
		table.put("PopupMenu.border",new AlphaFilledCurvedBorder(5));
		table.put("PopupMenu.foreground",Color.white);
		table.put("PopupMenu.selectionBackground",selectionColor);
		table.put("PopupMenu.selectionForeground",Color.white);
		table.put("PopupMenuSeparator.background",new Color(100,100,100));
		table.put("PopupMenuSeparator.foreground",Color.gray);
		table.put("RadioButton.check",checkColor);
		table.put("RadioButton.icon",IconFactory.getRadioButtonIcon());
		table.put("RadioButtonMenuItem.acceleratorSelectionForeground",Color.white);
		table.put("RadioButtonMenuItem.border",border);
		table.put("RadioButtonMenuItem.checkIcon",IconFactory.getRadioButtonMenuItemIcon());
		table.put("RadioButtonMenuItem.disabledForeground",Color.darkGray);
		table.put("RadioButtonMenuItem.selectionBackground",selectionColor);
		table.put("RadioButtonMenuItem.selectionForeground",Color.white);
		table.put("ScrollBar.darkShadow",new Color(0,0,10));
		table.put("ScrollBar.highlight",Color.gray);
		table.put("ScrollBar.shadow",new Color(0,10,15));
		table.put("ScrollBar.thumb",glossHighlightColor); 
		table.put("ScrollBar.thumbHighlight",new Color(255,255,255,180));
		table.put("ScrollBar.thumbShadow",new Color(140,140,140,200));
		table.put("ScrollBar.width",12);
		table.put("ScrollBar.backgroundEnabled","resources/icons/swing/scrollbar/scrollEnabled.png");
		table.put("ScrollBar.backgroundDisabled","resources/icons/swing/scrollbar/scrollEnabled.png");
		table.put("ScrollBar.decreaseNormal","resources/icons/swing/scrollbar/scrollDecrease.png");
		table.put("ScrollBar.decreaseOver","resources/icons/swing/scrollbar/scrollDecrease.png");
		table.put("ScrollBar.decreasePressed","resources/icons/swing/scrollbar/scrollDecrease.png");
		table.put("ScrollBar.increaseNormal","resources/icons/swing/scrollbar/scrollIncrease.png");
		table.put("ScrollBar.increaseOver","resources/icons/swing/scrollbar/scrollIncrease.png");
		table.put("ScrollBar.increasePressed","resources/icons/swing/scrollbar/scrollIncrease.png");
		table.put("ScrollBar.thumbNormal","resources/icons/swing/scrollbar/scrollThumb.png");
		table.put("ScrollBar.thumbOver","resources/icons/swing/scrollbar/scrollThumbOver.png");
		table.put("ScrollBar.thumbPressed","resources/icons/swing/scrollbar/scrollThumbOver.png");
		table.put("ScrollBar.lightBackgroundEnabled","resources/icons/swing/scrollbar/scrollEnabled.png");
		table.put("ScrollBar.lightBackgroundDisabled","resources/icons/swing/scrollbar/scrollEnabled.png");
		table.put("ScrollBar.lightDecreaseNormal","resources/icons/swing/scrollbar/scrollDecreaseGray.png");
		table.put("ScrollBar.lightDecreaseOver","resources/icons/swing/scrollbar/scrollDecreaseGray.png");
		table.put("ScrollBar.lightDecreasePressed","resources/icons/swing/scrollbar/scrollDecreaseGray.png");
		table.put("ScrollBar.lightIncreaseNormal","resources/icons/swing/scrollbar/scrollIncreaseGray.png");
		table.put("ScrollBar.lightIncreaseOver","resources/icons/swing/scrollbar/scrollIncreaseGray.png");
		table.put("ScrollBar.lightIncreasePressed","resources/icons/swing/scrollbar/scrollIncreaseGray.png");
		table.put("ScrollBar.lightThumbNormal","resources/icons/swing/scrollbar/scrollThumbGray.png");
		table.put("ScrollBar.lightThumbOver","resources/icons/swing/scrollbar/scrollThumbOver.png");
		table.put("ScrollBar.lightThumbPressed","resources/icons/swing/scrollbar/scrollThumbOver.png");
		table.put("ScrollPane.border",new LineBorder(new Color(20,20,20),0));
		table.put("Slider.tickColor",Color.lightGray);
		table.put("Spinner.arrowDisabled",Color.darkGray);
		table.put("Spinner.arrowEnabled",Color.lightGray);
		table.put("Spinner.arrowWidth",14);
		table.put("Spinner.border",new CurvedBorder(Color.black,6));
		table.put("Spinner.defaultEditorInsets",new Insets(2,2,2,2));
		table.put("Spinner.highlightColor",glossHighlightColor);
		table.put("Spinner.shadowColor",backgroundPanelColor.darker());
		table.put("SplitPane.highlight",menubarColor);
		table.put("SplitPane.darkShadow",glossDarkColor.darker());
		table.put("StatusBar.backgroundPainter",glossBlackPainter);
		table.put("StatusBar.foreground",Color.white);
		table.put("TabbedPane.closeButton",new Color(200,0,0,196));
		table.put("TabbedPane.contentBorderInsets",new Insets(1,1,1,1));
		table.put("TabbedPane.darkShadow",Color.black);
		table.put("TabbedPane.foreground",Color.white);
		table.put("TabbedPane.selectHighlight",Color.darkGray);
		table.put("TabbedPane.selected",glossHighlightColor);
		table.put("TabbedPane.selectedTabHighlightDark",new Color(0,0,64,32));    
		table.put("TabbedPane.selectedTabHighlightLight",new Color(64,177,230,200)); 
		table.put("TabbedPane.rolloverTabHighlightDark",new Color(64,0,0,32));    
		table.put("TabbedPane.rolloverTabHighlightLight",Color.orange);
		table.put("TabbedPane.selectedTabPadInsets",new Insets(2,2,2,2));
		table.put("TabbedPane.tabAreaInsets",new Insets(4,2,0,4)); 
		table.put("TabbedPane.tabHighlightLightTop",new Color(97,105,135));
		table.put("TabbedPane.tabHighlightDarkTop",new Color(63,71,86));
		table.put("TabbedPane.tabHighlightLightBottom",new Color(50,50,50));
		table.put("TabbedPane.tabHighlightDarkBottom",new Color(17,17,17));
		table.put("TabbedPane.closeTabIcon",new ImageIcon("resources/icons/swing/closeRollover.png"));
		table.put("TabbedPane.closeTabIconDisabled",new ImageIcon("resources/icons/swing/closeDisabled.png"));
		table.put("TabbedPane.forwardButtonIcon",new ImageIcon("resources/icons/swing/forwardButtonDark.png"));
		table.put("TabbedPane.forwardButtonRolloverIcon",new ImageIcon("resources/icons/swing/forwardButton.png"));
		table.put("TabbedPane.backButtonIcon",new ImageIcon("resources/icons/swing/backButtonDark.png"));
		table.put("TabbedPane.backButtonRolloverIcon",new ImageIcon("resources/icons/swing/backButton.png"));
		table.put("Table.font",theme.getControlTextFont());
		table.put("Table.gridColor",Color.gray);
		table.put("Table.rowHeaderPainter",mp3);
		table.put("Table.selectedRowHeaderPainter",highlightMP);
		table.put("Table.selectionBackground",selectionColor);
		table.put("Table.selectionForeground",Color.white);
		table.put("TableHeader.backgroundPainter",darkGlossBlackPainter);
		table.put("TableHeader.font",theme.getControlTextFont());
		table.put("TextArea.font",theme.getControlTextFont());
		table.put("TextArea.foreground",textColor);
		table.put("TextArea.background",backgroundPanelColor);
		table.put("TextField.background",Color.gray);
		table.put("TextField.border",new CompoundBorder(new CurvedBorder(Color.black,6),border));
		table.put("TextField.disabledBackground",Color.darkGray);
		table.put("TextField.font",theme.getControlTextFont());
		table.put("TextField.foreground",Color.white);
		table.put("TextPane.font",theme.getControlTextFont());
		table.put("ToggleButton.borderPaintsFocus",Boolean.FALSE);
		table.put("ToggleButton.focus",new Color(0,0,0,0.35f));
		table.put("ToolBar.backgroundPainter",darkGlossBlackPainter); 
		table.put("ToolBar.border",new EmptyBorder(1,1,1,1)); 
		table.put("ToolTip.background",Color.white); 
		table.put("ToolTip.border",new CompoundBorder(new DropShadowRectangularBorder(),new CompoundBorder(new LineBorder(Color.black,1),border)));
		table.put("ToolTip.font",theme.getControlTextFont());
		table.put("ToolTip.foreground",Color.black);
		table.put("ToolTip.hideAccelerator",Boolean.TRUE);
		table.put("Tree.closedIcon",new ImageIcon("resources/icons/swing/tree/closed.png"));
		table.put("Tree.collapsedIcon",new ImageIcon("resources/icons/swing/tree/collapsed.png"));
		table.put("Tree.expandedIcon",new ImageIcon("resources/icons/swing/tree/expanded.png"));
		table.put("Tree.font",theme.getControlTextFont());
		table.put("Tree.leafIcon",new ImageIcon("resources/icons/swing/tree/leaf.png"));
		table.put("Tree.openIcon",new ImageIcon("resources/icons/swing/tree/closed.png"));
		table.put("Tree.textBackground",new Color(0,0,0,0));
		table.put("Tree.textForeground",Color.white);
		table.put("Viewport.background",new Color(0,0,0,0));

		// Ribbon stuff

		table.put("BandControlPanel.backgroundPaintDark",new Color(190,190,190,0));
		table.put("BandControlPanel.backgroundPaintLight",new Color(215,215,215,0));
		table.put("ButtonStrip.border",new CurvedBorder(new Color(64,64,64,128),4));
		table.put("ButtonStrip.highlight",new Color(240,240,240,64));
		table.put("ButtonStrip.darkHighlight",new Color(128,128,128,64));
		table.put("ControlPanel.border",null);
		table.put("GalleryButton.expandIcon",new IconUIResource(new DropDownArrowIcon()));
		table.put("IconPopupGallery.font",theme.getControlTextFont());
		table.put("JRibbon.font",theme.getControlTextFont());
		table.put("JRibbon.ribbonBackgroundTopDark",new Color(150,155,176));    //new Color(194,199,207)); 
		table.put("JRibbon.ribbonBackgroundTopLight",new Color(160,165,175));   //new Color(205,210,217)); 
		table.put("JRibbon.ribbonBackgroundBottomDark",new Color(135,145,155)); //new Color(181,188,198)); 
		table.put("JRibbon.ribbonBackgroundBottomLight",new Color(180,180,180));//new Color(228,234,235)); 
		table.put("JRibbon.buttonStripBottomDark",new Color(176,187,189));      //new Color(206,217,219));
		table.put("JRibbon.buttonStripBottomLight",new Color(230,235,235));
		table.put("JRibbon.buttonStripTopDark",new Color(189,196,198));         //new Color(219,226,228));
		table.put("JRibbon.buttonStripTopLight",new Color(184,192,193));        //new Color(214,222,223));
		table.put("JRibbon.buttonStripButtonHighlightBottomDark",new Color(254,180,86));
		table.put("JRibbon.buttonStripButtonHighlightBottomLight",new Color(255,228,127));
		table.put("JRibbon.buttonStripButtonHighlightTopDark",new Color(254,199,120));
		table.put("JRibbon.buttonStripButtonHighlightTopLight",new Color(251,219,181));
		table.put("JRibbon.toggleButtonHighlightBottomDark",new Color(250,148,47));
		table.put("JRibbon.toggleButtonHighlightBottomLight",new Color(254,241,176));
		table.put("JRibbon.toggleButtonHighlightTopDark",new Color(251,172,95));
		table.put("JRibbon.toggleButtonHighlightTopLight",new Color(253,212,169));
		table.put("JRibbon.toggleButtonRolloverBottomDark",new Color(226,125,49));
		table.put("JRibbon.toggleButtonRolloverBottomLight",new Color(254,209,101));
		table.put("JRibbon.toggleButtonRolloverTopDark",new Color(234,151,85));
		table.put("JRibbon.toggleButtonRolloverTopLight",new Color(243,175,109));
		table.put("JRibbon.taskBackgroundPainter",mp4);
		table.put("JRibbonGallery.background",new Color(185,195,195));          // new Color(218,226,226));
		table.put("PopupGallery.labelBackground",new Color(64,64,64,140));
		table.put("PopupGallery.labelForeground",Color.white);
		table.put("PopupPanel.background",null);
		table.put("PopupPanel.border",new AlphaFilledCurvedBorder(new Color(140,140,140,180),4,true,new Insets(2,0,3,3)));
		table.put("RibbonBand.backgroundPaintDark",new Color(150,150,150));
		table.put("RibbonBand.backgroundPaintLight",new Color(170,170,170));
		table.put("RibbonBand.border",new CompoundBorder(new CurvedBorder(new Color(100,100,100,180),6),new EmptyBorder(1,0,1,0)));
		table.put("RibbonBand.borderHighlight",new Color(255,255,255,75));
		table.put("RibbonBand.expandIcon",new IconUIResource(new ExpandArrowIcon()));
		table.put("RibbonBand.font",theme.getControlTextFont());
		table.put("RibbonBand.foreground",Color.white);
		table.put("RibbonBandButton.borderColor",Color.orange);
		table.put("RibbonBandButton.bottomHighlightDark",new Color(254,231,149));
		table.put("RibbonBandButton.bottomHighlightLight",new Color(254,213,70));
		table.put("RibbonBandButton.collapsedBorderColor",new Color(89,126,175));
		table.put("RibbonBandButton.collapsedBottomHighlightDark",new Color(227,237,251));
		table.put("RibbonBandButton.collapsedBottomHighlightLight",new Color(188,208,233));
		table.put("RibbonBandButton.collapsedTopHighlightDark",new Color(201,221,246));
		table.put("RibbonBandButton.collapsedTopHighlightLight",new Color(200,219,238));
		table.put("RibbonBandButton.textColor",Color.darkGray);
		table.put("RibbonBandButton.topHighlightDark",new Color(254,232,153));
		table.put("RibbonBandButton.topHighlightLight",new Color(254,250,215));
		table.put("RibbonCheckBox.highlight",new Color(255,213,79,64));
		table.put("RibbonCheckBox.borderColor",new Color(172,172,172));
		table.put("RibbonCheckBox.foreground",Color.darkGray);
		table.put("ToggleTabButton.border",new EmptyBorder(0,10,0,10));
		table.put("ToggleTabButton.foreground",Color.white);
		table.put("ToggleTabButton.foregroundSelected",Color.darkGray);
		table.put("ToggleTabButton.rolloverTabHighlightDark",new Color(64,64,64,0));
		table.put("ToggleTabButton.rolloverTabHighlightLight",new Color(255,255,255,128));
		table.put("ToggleTabButton.rolloverTabGlow",Color.orange);
		table.put("ToggleTabButton.rolloverBorder",new Color(200,200,200,64));
		table.put("ToggleTabButton.selectedTabHighlightDark",new Color(160,165,175));
		table.put("ToggleTabButton.selectedTabHighlightLight",Color.white);

		// table.put("FileChooser.listViewBackground",Color.white);
		// table.put("FileChooser.listViewBorder",new LineBorder());
		// table.put("FileChooser.listViewWindowsStyle",Boolean.TRUE);
		// table.put("FileChooser.useShellFolder",Boolean.TRUE);
		// table.put("JXMonthView.monthDownFileName","resources/icons/rarowsvg.png");
		// table.put("JXMonthView.monthUpFileName","resources/icons/larowsvg.png");
		// table.put("Tree.selectionForeground",Color.white );
		// table.put("Tree.selectionBackground",new Color(49,106,197) );
		// table.put("PopupMenuSeparator.margin",new InsetsUIResource(3,4,3,4));	

		/*
			table.put("ToggleButton.margin",new InsetsUIResource(4,16,4,16));
			table.put("ToggleButton.border",new BasicBorders.MarginBorder());
			table.put("InternalFrame.paletteTitleHeight",new Integer(10));
			table.put("InternalFrame.frameTitleHeight",new Integer(21));
			table.put("InternalFrame.normalTitleFont",new Font("Trebuchet MS",Font.BOLD,13));
			table.put("Checkbox.select",xpTheme.getPressedBackground());
			table.put("InternalFrame.border",border); 
			table.put("InternalFrame.paletteBorder",border); 
			table.put("InternalFrame.optionDialogBorder",border); 
			table.put("InternalFrame.frameTitleHeight",new Integer(25));
			table.put("InternalFrame.paletteTitleHeight",new Integer(16));
			table.put("InternalFrame.icon",loadIcon("XPInternalFrameIcon.res",this));
			table.put("Tree.selectionForeground",Color.white );
			table.put("Tree.selectionBackground",new Color(49,106,197) );
		*/    
	}
}
