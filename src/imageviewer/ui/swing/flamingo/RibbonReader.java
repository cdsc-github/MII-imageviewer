/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.swing.flamingo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComponentInputMap;
import javax.swing.DefaultButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ActionMapUIResource;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import org.jdesktop.swingx.plaf.LookAndFeelAddons;

import org.jvnet.flamingo.common.ElementState;
import org.jvnet.flamingo.common.JButtonStrip;
import org.jvnet.flamingo.common.AbstractCommandButton;
import org.jvnet.flamingo.common.JCommandButton;
import org.jvnet.flamingo.common.JCommandButtonPanel;
import org.jvnet.flamingo.common.JCommandToggleButton;
import org.jvnet.flamingo.common.JIconPopupPanel;
import org.jvnet.flamingo.common.JPopupPanel;
import org.jvnet.flamingo.common.PopupPanelManager;
import org.jvnet.flamingo.common.StringValuePair;

import org.jvnet.flamingo.ribbon.JRibbon;
import org.jvnet.flamingo.ribbon.JRibbonBand;
import org.jvnet.flamingo.ribbon.RibbonElementPriority;
import org.jvnet.flamingo.ribbon.RibbonTask;
import org.jvnet.flamingo.ribbon.ui.JRibbonGallery;

import imageviewer.tools.Tool;
import imageviewer.tools.ToolManager;
import imageviewer.tools.plugins.Plugin;
import imageviewer.tools.plugins.PluginManager;

import imageviewer.ui.ApplicationContext;
import imageviewer.ui.swing.ColorPanel;
import imageviewer.ui.swing.ImageViewerLookAndFeel;
import imageviewer.ui.swing.ImageViewerLookAndFeelAddons;
import imageviewer.ui.swing.MenuAction;
import imageviewer.ui.swing.MinimumSizedIcon;
import imageviewer.ui.swing.border.RibbonSeparatorBorder;

import imageviewer.ui.swing.event.BasicMenuActionListener;
import imageviewer.ui.swing.event.MenuActionEvent;
import imageviewer.ui.swing.event.MenuActionListener;
import imageviewer.ui.swing.event.MenuActionListenerFactory;

import imageviewer.util.XMLUtil;

// =======================================================================

public class RibbonReader {

	private static Logger LOG=Logger.getLogger("imageviewer.ui");

	// =======================================================================

	public JRibbon parseFile(String filename) {

		LOG.info("Loading ribbon configuration file: "+filename);
		try {
			RibbonHandler handler=new RibbonHandler();
			SAXParser parser=XMLUtil.getSAXParser();
			InputStream is=new FileInputStream(filename);
			InputSource aSource=new InputSource(is);
			parser.parse(aSource,handler);
			is.close();
			XMLUtil.releaseSAXParser(parser);
			return handler.getRibbon();
		} catch (FileNotFoundException exc) {
			LOG.error("Specified filename could not be accessed: "+filename);
		} catch (Exception exc) {
			LOG.error("Error attempting to parse file for configuration: "+filename);
			exc.printStackTrace();
		}
		return null;
	}

	// =======================================================================

	private class RibbonHandler extends DefaultHandler {

		JRibbon jr=new JRibbon();
		JRibbonBand jrb=null;
		JRibbonGallery jrg=null;
		JButtonStrip jbs=null;
		JPanel currentPanel=null, currentGroup=null;
		JComboBox jcb=null;
		RibbonTask rt=null;
		RibbonMultiButton rmb=null;
		RibbonFlipButton rfb=null;
		String currentTaskName=null;
		RibbonMenuPanel popupPanel=null;

		LinkedHashMap<ElementState,Integer> galleryVisibleButtonCount=null;
		LinkedHashMap<String,ArrayList<JCommandToggleButton>> galleryGroups=null;

		Stack<MenuActionListener> listenerStack=null;
		ArrayList<JComponent> gp=null;
		
		public RibbonHandler() {super();}

		// =======================================================================

		public JRibbon getRibbon() {return jr;}

		// =======================================================================

		private AbstractAction getActionListener(final String commandName, final AbstractButton ab) {

			final MenuActionListener mal=listenerStack.peek();
			return new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					RibbonActionEventWrapper raew=new RibbonActionEventWrapper(new ActionEvent(ae.getSource(),ae.getID(),commandName,ae.getModifiers()),ab);
					mal.actionPerformed(raew);
				}
			};
		}

		private AbstractAction getActionListener(final String commandName) {
			
			final MenuActionListener mal=listenerStack.peek();
			return new AbstractAction() {
				public void actionPerformed(ActionEvent ae) {
					RibbonActionEventWrapper raew=new RibbonActionEventWrapper(new ActionEvent(ae.getSource(),ae.getID(),commandName,ae.getModifiers()),commandName);
					mal.actionPerformed(raew);
				}
			};
		}

		private void setActionListener(String actionCommand, AbstractButton ab) {ab.setActionCommand(actionCommand); ab.addActionListener(getActionListener(actionCommand,ab));}

		private void setAccelerator(AbstractButton ab, Attributes attr) {

			if (attr.getIndex("shortcut")>=0) {
				String accelerator=attr.getValue("shortcut");
				try {
					KeyStroke ks=KeyStroke.getKeyStroke(accelerator);
					InputMap keyMap=new ComponentInputMap(ab);
					keyMap.put(ks,"action");
					ActionMap amap=new ActionMapUIResource();
					Action a=ab.getAction();
					if (a==null) {
						if ((ab instanceof JCommandButton)||(ab instanceof RibbonToggleButton)||
								(ab instanceof RibbonStripButton)||(ab instanceof JCheckBox)) a=(Action)ab.getClientProperty("ToggleButtonAction");
						if (a==null) {
							ActionListener[] alArray=ab.getActionListeners();
							for (int loop=0; loop<alArray.length; loop++) if (alArray[loop] instanceof Action) {a=(Action)alArray[loop]; break;}
						}
					}
					if (a==null) return;
					amap.put("action",a);
					SwingUtilities.replaceUIActionMap(ab,amap);
					SwingUtilities.replaceUIInputMap(ab,JComponent.WHEN_IN_FOCUSED_WINDOW,keyMap);
				} catch (Exception exc) {
					exc.printStackTrace();
				}
			}
		}

		// =======================================================================

		private void setContext(String commandName) {

			String propName=commandName.trim().toUpperCase();
			propName=propName.replaceAll(" ","_");
			Object o=ApplicationContext.getContext().getProperty(propName);
			if (o!=null) {
				if (o instanceof Boolean) {
					boolean b=((Boolean)o).booleanValue();
					if (b) System.err.println("WHAAA?"); //ma.getMenuItem().doClick();
				}
			}
		}

		// =======================================================================

		private ArrayList<JCommandToggleButton> getGalleryGroup(String groupName) {

			ArrayList<JCommandToggleButton> al=galleryGroups.get(groupName);
			if (al==null) {al=new ArrayList<JCommandToggleButton>(); galleryGroups.put(groupName,al);}
			return al;
		}

		// =======================================================================

		public void startDocument() {
		
			listenerStack=new Stack<MenuActionListener>();
			listenerStack.push(MenuActionListenerFactory.getListener(BasicMenuActionListener.class));
		}

		// =======================================================================

		public void startElement(String uri, String localname, String qname, Attributes attr) {
			
			if ("task".equals(localname)) {
				String taskName=attr.getValue("name");
				boolean isInitiallyEnabled=(attr.getIndex("isInitiallyEnabled")>0) ? Boolean.parseBoolean(attr.getValue("isInitiallyEnabled")) : false;
				if (taskName!=null) {
					rt=new RibbonTask();
					currentTaskName=new String(taskName);
				}
				return;
			} else if (("band".equals(localname))&&(rt!=null)) {
				String bandName=attr.getValue("name");
				String iconName=attr.getValue("icon");
				String alternateIconName=attr.getValue("alternateIcon");
				String expandAction=(attr.getIndex("expandAction")>0) ? attr.getValue("expandAction") : null;
				jrb=new JRibbonBand(bandName,new BasicResizableIcon(iconName,alternateIconName),(expandAction==null) ? null :	getActionListener(expandAction));
				return;
			} else if (("ribbonFlipButton".equals(localname))&&(jrb!=null)) {
				String priorityStr=attr.getValue("priority");
				RibbonElementPriority rep=RibbonElementPriority.TOP;
				if ("MEDIUM".equals(priorityStr)) rep=RibbonElementPriority.TOP; else if ("LOW".equals(priorityStr)) rep=RibbonElementPriority.LOW;
				rfb=new RibbonFlipButton();
				jrb.addGalleryButton(rfb,rep);
				rfb.setEnabled(true);
				return;
			} else if (("galleryMultiButton".equals(localname))&&(jrb!=null)) {
				String priorityStr=attr.getValue("priority");
				RibbonElementPriority rep=RibbonElementPriority.TOP;
				if ("MEDIUM".equals(priorityStr)) rep=RibbonElementPriority.TOP; else if ("LOW".equals(priorityStr)) rep=RibbonElementPriority.LOW;
				String popupType=attr.getValue("popupType");
				boolean popupIsLarge=(popupType==null) ? true : (("LARGE".equals(popupType)) ? true : false);
				int popupWidth=(attr.getIndex("popupMaximumWidth")>=0) ? Integer.parseInt(attr.getValue("popupMaximumWidth")) : -1;
				boolean useExtraText=(attr.getIndex("useExtraText")>=0) ? Boolean.parseBoolean(attr.getValue("useExtraText")) : true;
				rmb=new RibbonMultiButton(popupIsLarge,popupWidth,useExtraText);
				jrb.addGalleryButton(rmb,rep);
				rmb.setEnabled(true);
				return;
			} else if (("galleryButton".equals(localname))&&(jrb!=null)) {
				String buttonName=attr.getValue("name");
				String iconName=attr.getValue("icon");
				String alternateIconName=attr.getValue("alternateIcon");
				boolean hasGallery=(attr.getIndex("hasGallery")>=0) ? Boolean.parseBoolean(attr.getValue("hasGallery")) : false;
				boolean isInitiallyEnabled=(attr.getIndex("isInitiallyEnabled")>=0) ? Boolean.parseBoolean(attr.getValue("isInitiallyEnabled")) : true;
				boolean isToggle=(attr.getIndex("isToggle")>=0) ? Boolean.parseBoolean(attr.getValue("isToggle")) : false;
				boolean checkStatus=(attr.getIndex("checkStatus")>=0) ? Boolean.parseBoolean(attr.getValue("checkStatus")) : false;
				String functionName=attr.getValue("functionName");
				String priorityStr=attr.getValue("priority");
				RibbonElementPriority rep=RibbonElementPriority.TOP;
				if ("MEDIUM".equals(priorityStr)) rep=RibbonElementPriority.TOP; else if ("LOW".equals(priorityStr)) rep=RibbonElementPriority.LOW;
				AbstractCommandButton button=(jrg!=null) ? new JCommandToggleButton(buttonName,new BasicResizableIcon(iconName,alternateIconName)) :
					new JCommandButton(buttonName,new BasicResizableIcon(iconName,alternateIconName));
				button.setDisabledIcon(new BasicResizableIcon(iconName,alternateIconName,true));
				if ((hasGallery)&&(jrg==null)&&(rmb==null)) {
					if (popupPanel!=null) {
						JPopupPanel jpp=new JPopupPanel(popupPanel,new Dimension(popupPanel.getComputedWidth(),popupPanel.getComputedHeight()));
						jpp.putClientProperty("paintBandTitle",Boolean.FALSE);
						((JCommandButton)button).setPopupPanel(jpp);
						popupPanel=null;
					} else {
						((JCommandButton)button).setPopupPanel(new JPopupPanel());
					}
				}
				button.setActionCommand((functionName!=null) ? functionName : buttonName);
				if ((isToggle)||(jrg!=null)) button.setRolloverEnabled(true);
				ActionListener al=new RibbonButtonActionListener(getActionListener((functionName!=null) ? functionName : buttonName,button),checkStatus);
				button.putClientProperty("ToggleButtonAction",al);
				button.putClientProperty("isRibbonToggleButton",((isToggle)||(jrg!=null)) ? Boolean.TRUE : null);
				button.addActionListener(al);
				button.setActionCommand((functionName!=null) ? functionName : buttonName);
			  setAccelerator(button,attr);
				setContext((functionName!=null) ? functionName : buttonName);
				if (rmb!=null) {
					rmb.addButton((JCommandButton)button);
				} else if (rfb!=null) {
					rfb.addButton((JCommandButton)button);
				} else if (jrg!=null) {
					String groupName=attr.getValue("group");
					if (groupName==null) groupName="";
					ArrayList<JCommandToggleButton> group=getGalleryGroup(groupName);
					group.add((JCommandToggleButton)button);
				} else jrb.addGalleryButton((JCommandButton)button,rep);
				String buttonGroup=attr.getValue("buttonGroup");
				if (buttonGroup!=null) {
					RibbonButtonGroups.getInstance().addButton(buttonGroup,button);
					button.putClientProperty("buttonGroupName",buttonGroup);
				}
				MenuAction.ACTIONS.put(button.getActionCommand(),new RibbonActionWrapper(button));
				button.setEnabled(isInitiallyEnabled);
				String extraText=attr.getValue("extraText");
				if (extraText!=null) button.setExtraText(extraText);
				return;
			} else if (("gallery".equals(localname))&&(jrb!=null)) {
				jrg=new JRibbonGallery();
				galleryVisibleButtonCount=new LinkedHashMap<ElementState,Integer>();
				galleryVisibleButtonCount.put(ElementState.SMALL,(attr.getIndex("smallWidth")>=0) ? Integer.parseInt(attr.getValue("smallWidth")) : 1);
				galleryVisibleButtonCount.put(ElementState.MEDIUM,(attr.getIndex("mediumWidth")>=0) ? Integer.parseInt(attr.getValue("mediumWidth")) : 2);
				galleryVisibleButtonCount.put(ElementState.BIG,(attr.getIndex("largeWidth")>=0) ? Integer.parseInt(attr.getValue("largeWidth")) : 3);
				galleryGroups=new LinkedHashMap<String,ArrayList<JCommandToggleButton>>();

				// String priorityStr=attr.getValue("priority");
				// RibbonElementPriority rep=RibbonElementPriority.TOP;
				// if ("MEDIUM".equals(priorityStr)) rep=RibbonElementPriority.MEDIUM; else if ("LOW".equals(priorityStr)) rep=RibbonElementPriority.LOW;

			} else if (("buttonStrip".equals(localname))&&(currentPanel!=null)) {
				jbs=new JButtonStrip();
				JPanel containingPanel=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
				containingPanel.add(jbs);
				containingPanel.setOpaque(false);
				containingPanel.setBorder(null);
				if (currentGroup!=null) currentGroup.add(containingPanel); else currentPanel.add(containingPanel);
				return;
			} else if (("panel".equals(localname))&&(jrb!=null)) {
				currentPanel=new JPanel();
				currentPanel.setOpaque(false);
				currentPanel.setBorder(null);
				String layoutType=(attr.getIndex("layout")>=0) ? new String(attr.getValue("layout")) : "rows";
				if ("grid".compareToIgnoreCase(layoutType)==0) {
					currentPanel.setLayout(new MixedGridLayout(3,0,2,2));
				} else {
					int rows=(attr.getIndex("rows")>=0) ? Integer.parseInt(attr.getValue("rows")) : 3;
					currentPanel.setLayout(new RowStripLayout(2,rows)); 
				}
				JPanel containingPanel=new JPanel(new FlowLayout(FlowLayout.CENTER,2,0));
				boolean hasLeftSeparator=(attr.getIndex("hasLeftSeparator")>=0) ? Boolean.parseBoolean(attr.getValue("hasLeftSeparator")) : false;
				boolean hasRightSeparator=(attr.getIndex("hasRightSeparator")>=0) ? Boolean.parseBoolean(attr.getValue("hasRightSeparator")) : false;
				containingPanel.setBorder(new RibbonSeparatorBorder(hasLeftSeparator,hasRightSeparator));
				containingPanel.setOpaque(false);
				containingPanel.add(currentPanel);
				jrb.addPanel(containingPanel);
				return;
			} else if (("stripButton".equals(localname))&&(jbs!=null)) {
				String iconFile=attr.getValue("icon");
				if (iconFile==null) iconFile=("resources/icons/Unknown.png");
				BasicResizableIcon bsi=new BasicResizableIcon(iconFile,null,false);
				boolean isToggle=(attr.getIndex("isToggle")>=0) ? Boolean.parseBoolean(attr.getValue("isToggle")) : false;
				boolean isEnabled=(attr.getIndex("isInitiallyEnabled")>=0) ? Boolean.parseBoolean(attr.getValue("isInitiallyEnabled")) : false;
				String popupType=attr.getValue("popupType");		

				final AbstractButton ab;
				if ((popupType!=null)&&("colorPanel".compareToIgnoreCase(popupType)==0)) {
					ab=new RibbonStripButton(bsi,true) {
						public void paintComponent(Graphics g) {
							super.paintComponent(g);
							Color iconColor=((ColorPanel)getPopupPanel().getComponent()).getCurrentColor();
							g.setColor(iconColor);
							g.fillRect(9,15,16,4);
						}
					
					};
				} else {
					ab=(isToggle) ? new RibbonToggleButton(bsi,false) : new RibbonStripButton(bsi,(popupType==null) ? false : true);
				}
				if (popupType!=null) {
					if ("colorPanel".compareToIgnoreCase(popupType)==0) {
						ColorPanel cp=new ColorPanel() {
							public void actionPerformed(ActionEvent ae) {
								super.actionPerformed(ae);
								PopupPanelManager.defaultManager().hidePopups(ab);
							}
						};
						JPopupPanel jpp=new JPopupPanel(cp,new Dimension(23*5,24*3));
						jpp.putClientProperty("paintBandTitle",Boolean.FALSE);
						((RibbonStripButton)ab).setPopupPanel(jpp);
					} else if (popupPanel!=null) {
						JPopupPanel jpp=new JPopupPanel(popupPanel,new Dimension(popupPanel.getComputedWidth(),popupPanel.getComputedHeight()));
						jpp.putClientProperty("paintBandTitle",Boolean.FALSE);
						((RibbonStripButton)ab).setPopupPanel(jpp);
						popupPanel=null;
					}
				}

				ab.setDisabledIcon(new BasicResizableIcon(iconFile,true));
				String commandName=attr.getValue("name");
				ab.setToolTipText(commandName);
				setActionListener(commandName,ab);
				setContext(commandName);
				ab.putClientProperty("ToggleButtonAction",(isToggle) ? new RibbonButtonActionListener(getActionListener(commandName,ab)) : getActionListener(commandName,ab));
				setAccelerator(ab,attr);
				jbs.add(ab);
				String buttonGroup=attr.getValue("buttonGroup");
				if (buttonGroup!=null) RibbonButtonGroups.getInstance().addButton(buttonGroup,ab);
				MenuAction.ACTIONS.put(ab.getActionCommand(),new RibbonActionWrapper(ab));
				ab.setEnabled(isEnabled);
				return;
			} else if (("comboBox".equals(localname))&&(currentPanel!=null)) {
				jcb=new JComboBox();
				jcb.setUI(new RibbonComboBoxUI());
				jcb.addActionListener(getActionListener("TEST",null));
				if (currentGroup!=null) currentGroup.add(jcb); else currentPanel.add(jcb);
				return;
			} else if (("comboBoxItem".equals(localname))&&(currentPanel!=null)) {
				String actionName=attr.getValue("name");
				if (actionName!=null) jcb.addItem(actionName);
			} else if (("group".equals(localname))&&(currentPanel!=null)) {
				currentGroup=new JPanel();
				currentGroup.setLayout(new BoxLayout(currentGroup,BoxLayout.X_AXIS));
				currentGroup.setOpaque(false);
				currentGroup.setBorder(null);
				currentPanel.add(currentGroup);
				return;
			} else if (("label".equals(localname))&&(currentPanel!=null)) {
				Icon ii=(attr.getIndex("icon")>=0) ? new ImageIcon(attr.getValue("icon")) : null;
				String labelText=(attr.getIndex("text")>=0) ? new String(attr.getValue("text")) : null;
				String alignment=(attr.getIndex("align")>=0) ? new String(attr.getValue("align")) : "right";
				JLabel jl=new JLabel();
				if (ii!=null) jl.setIcon(ii);
				if (labelText!=null) jl.setText(labelText);
				if (alignment.compareToIgnoreCase("left")==0) jl.setHorizontalAlignment(JLabel.LEFT);
				else if (alignment.compareToIgnoreCase("right")==0) jl.setHorizontalAlignment(JLabel.RIGHT);
				else jl.setHorizontalAlignment(JLabel.CENTER);
				jl.setOpaque(false);
				jl.setBorder(new EmptyBorder(0,2,0,2));
				jl.setForeground(Color.black);
				if (currentGroup!=null) currentGroup.add(jl); else currentPanel.add(jl);
				return;
			} else if (("checkbox".equals(localname))&&(currentPanel!=null)) {
				String labelText=(attr.getIndex("name")>=0) ? new String(attr.getValue("name")) : null;
				if (labelText!=null) {
					boolean isEnabled=(attr.getIndex("isInitiallyEnabled")>=0) ? Boolean.parseBoolean(attr.getValue("isInitiallyEnabled")) : true;
					boolean isSelected=(attr.getIndex("isInitiallySelected")>=0) ? Boolean.parseBoolean(attr.getValue("isInitiallySelected")) : false;
					JCheckBox checkbox=new JCheckBox(labelText);
					checkbox.setUI(new RibbonCheckBoxUI());
					setActionListener(labelText,checkbox);
					checkbox.putClientProperty("ToggleButtonAction",new RibbonButtonActionListener(getActionListener(labelText,checkbox)));
					checkbox.setEnabled(isEnabled);
					checkbox.setSelected(isSelected);
					setAccelerator(checkbox,attr);
					currentPanel.add(checkbox);
					MenuAction.ACTIONS.put(checkbox.getActionCommand(),new RibbonActionWrapper(checkbox));
				}
				return;
			} else if ("eventListener".equals(localname)) {
				String className=attr.getValue("class");
				try {
					Class c=Class.forName(className);
					MenuActionListener mal=null;
					if (c.isAssignableFrom(Plugin.class)) {
						mal=(MenuActionListener)PluginManager.getPlugin(c);
					} else if (c.isAssignableFrom(Tool.class)) {
						mal=(MenuActionListener)ToolManager.getTool(c);
					} else {
						mal=MenuActionListenerFactory.getListener(c);
					}
					listenerStack.push(mal);
				} catch (Exception exc) {
					LOG.error("Error handling event listener: "+className);
					exc.printStackTrace();
				}
				return;
			} else if ("popupPanel".equals(localname)) {
				gp=new ArrayList<JComponent>();
			} else if (("command".equals(localname))&&(gp!=null)) {
				String iconFile=attr.getValue("icon");
				String toggleType=attr.getValue("toggleType");
				if (toggleType==null) toggleType="none";
				if ((toggleType.compareToIgnoreCase("radioButton")==0)||(toggleType.compareToIgnoreCase("checkbox")==0)) {
					iconFile=("resources/icons/ribbon/checkbox.png");
				} else if (iconFile==null) iconFile=("resources/icons/ribbon/32x32/empty.png");
				BasicResizableIcon bsi=new BasicResizableIcon(iconFile,null,false);
				boolean isEnabled=(attr.getIndex("isInitiallyEnabled")>=0) ? Boolean.parseBoolean(attr.getValue("isInitiallyEnabled")) : false;
				boolean isDefault=(attr.getIndex("isDefault")>=0) ? Boolean.parseBoolean(attr.getValue("isDefault")) : false;
				String commandName=attr.getValue("name");				
				String buttonGroup=attr.getValue("buttonGroup");
				MenuButton mb=new MenuButton(bsi) {public void paintComponent(Graphics g) {super.paintComponent(g); paintSmallToggleMenuButton(g,24,19);}};
				if (buttonGroup!=null) {
					RibbonButtonGroups.getInstance().addButton(buttonGroup,mb);
					mb.putClientProperty("buttonGroupName",buttonGroup);
				}
				if (isDefault) {
					ButtonGroup bg=RibbonButtonGroups.getInstance().getGroup(buttonGroup);
					bg.clearSelection();
					bg.setSelected(mb.getModel(),true);
				}
				mb.setTitle(commandName);
				mb.setBorder(null);
				mb.setOpaque(false);
				mb.setDisabledIcon(new BasicResizableIcon(iconFile,true));
				ActionListener al=new RibbonButtonActionListener(getActionListener(commandName,mb));
				mb.addActionListener(al);
				gp.add(mb);
			} else if (("separator".equals(localname))&&(gp!=null)) {
				JSeparator js=new JSeparator();
				js.setForeground(new Color(255,255,255,96));
				js.setBackground(new Color(0,0,0,96));
				gp.add(js);
			}
		}

		// =======================================================================

		public void endElement(String uri, String localname, String qname) {

			if ("task".equals(localname)) {
				jr.addTask(currentTaskName,rt);
				rt=null;
			} else if ("band".equals(localname)) {
				rt.addBand(jrb);
				jrb=null;
			} else if ("buttonStrip".equals(localname)) {
				jbs=null;
			} else if ("panel".equals(localname)) {
				currentPanel=null;
			} else if ("group".equals(localname)) {
				currentGroup=null;
			} else if ("gallery".equals(localname)) {
				ArrayList<StringValuePair<List<JCommandToggleButton>>> galleryButtons=new ArrayList<StringValuePair<List<JCommandToggleButton>>>();
				Iterator i=galleryGroups.entrySet().iterator();
				while (i.hasNext()) {
					Map.Entry entry=(Map.Entry)i.next();
					ArrayList<JCommandToggleButton> group=galleryGroups.get(entry.getKey());
					galleryButtons.add(new StringValuePair<List<JCommandToggleButton>>((String)entry.getKey(),group));
				}

				// Create the gallery; note that jrg is not used as anything
				// but a placeholder. Override the behavior of the created
				// JRibbonGallery by adding a gallery panel

				jrb.addRibbonGallery(galleryButtons,galleryVisibleButtonCount,3,3,RibbonElementPriority.TOP);
				JRibbonGallery lastGallery=(JRibbonGallery)((LinkedList)jrb.getControlPanel().getRibbonGalleries(RibbonElementPriority.TOP)).getLast();
				GalleryPanel galleryPanel=new GalleryPanel(lastGallery);
				JIconPopupPanel jipp=new ImageViewerIconPopupPanel(galleryPanel,3,3); 
				jipp.putClientProperty("paintBandTitle",Boolean.FALSE);
				lastGallery.putClientProperty("popupIconGallery",jipp);
				jrg=null;
				galleryGroups=null;
				galleryVisibleButtonCount=null;
			}	else if ("eventListener".equals(localname)) {
				if (listenerStack.size()!=1) listenerStack.pop();	return;
			} else if ("galleryMultiButton".equals(localname)) {
				rmb.generatePopupPanel();
				rmb=null;
			} else if ("ribbonFlipButton".equals(localname)) {
				rfb=null;
			} else if ("popupPanel".equals(localname)) {
				popupPanel=new RibbonMenuPanel(false,24,225,gp);
				gp=null;
			}
		}
	}

	// =======================================================================

	private class ImageViewerIconPopupPanel extends JIconPopupPanel {

		public ImageViewerIconPopupPanel(JCommandButtonPanel iconPanel, int maxButtonColumns,	int maxVisibleButtonRows) {

			super(iconPanel,maxButtonColumns,maxVisibleButtonRows);
			scroll.getVerticalScrollBar().putClientProperty("__lightColorScrollbar",Boolean.TRUE);
		}

		public Dimension getPreferredSize() {Dimension d=super.getPreferredSize(); return new Dimension((int)d.getWidth()+8,(int)d.getHeight());}
	}

	// =======================================================================

	private class GalleryPanel extends JCommandButtonPanel {

		public GalleryPanel(JRibbonGallery jrg) {

			super(ElementState.BIG);
			ArrayList<String> al=new ArrayList<String>();
			for (int loop=0, n=jrg.getButtonCount(); loop<n; loop++) {
				final JCommandToggleButton jctb=jrg.getButtonAt(loop);
				JCommandToggleButton clonedButton=new JCommandToggleButton(jctb.getTitle(),(BasicResizableIcon)((BasicResizableIcon)(jctb.getIcon())).clone());
				clonedButton.putClientProperty("isRibbonToggleButton",Boolean.TRUE);
				jctb.getModel().addItemListener(new ClonedButtonSyncListener(clonedButton));
				clonedButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						if (!("pressed".equals(ae.getActionCommand()))) {
							PopupPanelManager.defaultManager().hideLastPopup();
							jctb.doClick(); 
						}
					}
				});
				clonedButton.setVisible(true);
				String buttonGroup=(String)jctb.getClientProperty("buttonGroupName");
				if (buttonGroup==null) buttonGroup=("Default Selections");
				if (!al.contains(buttonGroup)) {
					al.add(buttonGroup);
					addButtonGroup(buttonGroup,al.size()-1);
				}
				addButtonToGroup(buttonGroup,clonedButton);
			}
			al.clear();
			setSingleSelectionMode(false);
			setOpaque(false);
			setMaxButtonColumns(3);
			setToShowGroupLabels(true);
		}
	}

	// =======================================================================

	private class RibbonButtonActionListener extends AbstractAction {

		AbstractAction aa=null;
		boolean checkStatus=false;

		public RibbonButtonActionListener(AbstractAction aa) {super(); this.aa=aa;}
		public RibbonButtonActionListener(AbstractAction aa, boolean checkStatus) {super(); this.aa=aa; this.checkStatus=checkStatus;}

		private boolean actionCancelled() {int status=ApplicationContext.getContext().getLastActionStatus(); return ((status==JOptionPane.CLOSED_OPTION)||(status==JOptionPane.CANCEL_OPTION)) ? true : false;}

		public void actionPerformed(ActionEvent e) {

			final AbstractButton ab=(AbstractButton)e.getSource();
			if (SwingUtilities.getAncestorOfClass(JPopupPanel.class,ab)!=null) PopupPanelManager.defaultManager().hidePopups(null);
			if (!("pressed".equals(e.getActionCommand()))) {
				if (checkStatus) {
					aa.actionPerformed(e);
					boolean doAction=(!actionCancelled());
					if (doAction) {
						boolean b=ab.isSelected();
						DefaultButtonModel dbm=(DefaultButtonModel)ab.getModel();
						ButtonGroup bg=dbm.getGroup();
						if (bg!=null) bg.setSelected(dbm,!b); else ab.setSelected(!b);
						ab.repaint();
					} else {
						DefaultButtonModel dbm=(DefaultButtonModel)ab.getModel();
						ButtonGroup bg=dbm.getGroup();
						if (bg!=null) bg.clearSelection(); else ab.setSelected(false);  // Not a perfect situation; we should revert to the previous selection, but we don't have a record...
						ab.repaint();
					}
				} else {
					boolean b=ab.isSelected();
					DefaultButtonModel dbm=(DefaultButtonModel)ab.getModel();
					ButtonGroup bg=dbm.getGroup();
					if (bg!=null) bg.setSelected(dbm,!b); else ab.setSelected(!b);
					aa.actionPerformed(e);
					ab.repaint();
				} 
			}
		}
	}

	// =======================================================================

	private class RibbonActionWrapper extends MenuAction {

		AbstractButton ab=null;

		public RibbonActionWrapper(AbstractButton ab) {super(ab.getActionCommand()); this.ab=ab;}
		public AbstractButton getMenuItem() {return ab;}
		public void setEnabled(boolean b) {super.setEnabled(b); ab.setEnabled(b); ab.repaint();}
	}

	// =======================================================================

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(new ImageViewerLookAndFeel());
			LookAndFeelAddons.setAddon(ImageViewerLookAndFeelAddons.class);
		} catch (Exception exc) {
			LOG.error("Could not set imageViewer L&F.");
		}		

		Object[] defaults=new Object[] {"ToggleTabButton.disabledText",Color.gray};

		JFrame frame=new JFrame("imageviewer ribbon test");
		RibbonReader rr=new RibbonReader();
		JRibbon jr=rr.parseFile("config/ribbon.xml");
		frame.setLayout(new BorderLayout());
		frame.add(jr,BorderLayout.NORTH);

		JPanel jp=new JPanel();
		jp.setBackground(Color.black);
		frame.add(jp,BorderLayout.CENTER);
		frame.setPreferredSize(new Dimension(1000,300));
		frame.pack();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
