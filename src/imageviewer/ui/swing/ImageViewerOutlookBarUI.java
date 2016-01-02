/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
/**
 * L2FProd.com Common Components 6.11 License.
 *
 * Copyright 2005-2006 L2FProd.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package imageviewer.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.LookAndFeel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

import javax.swing.plaf.ButtonUI;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import com.l2fprod.common.swing.JOutlookBar;
import com.l2fprod.common.swing.PercentLayout;
import com.l2fprod.common.swing.PercentLayoutAnimator;
import com.l2fprod.common.swing.plaf.OutlookBarUI;
import com.l2fprod.common.util.JVM;

public class ImageViewerOutlookBarUI extends BasicTabbedPaneUI implements OutlookBarUI {

  private static final String BUTTON_ORIGINAL_FOREGROUND=new String("TabButton/foreground");
  private static final String BUTTON_ORIGINAL_BACKGROUND=new String("TabButton/background");

  public static ComponentUI createUI(JComponent c) {return new ImageViewerOutlookBarUI();}

  ContainerListener tabListener=null;
  Map buttonToTab=null, tabToButton=null;
  Component nextVisibleComponent=null;
  PercentLayoutAnimator animator=null;

	public JScrollPane makeScrollPane(Component component) {

    JScrollPane scroll=new JScrollPane();
    scroll.setBorder(BorderFactory.createEmptyBorder());
    if (component instanceof Scrollable) {
      scroll.getViewport().setView(component);
    } else {
      scroll.getViewport().setView(new ScrollableJPanel(component));
    }
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    return scroll;
  }

  protected void installDefaults() {

    super.installDefaults();
    TabLayout layout=new TabLayout();
    tabPane.setLayout(layout);
    layout.setLayoutConstraints(tabPane);
    updateTabLayoutOrientation();
    buttonToTab=new HashMap();
    tabToButton=new HashMap();
    LookAndFeel.installBorder(tabPane,"OutlookBar.border");
    LookAndFeel.installColors(tabPane,"OutlookBar.background","OutlookBar.foreground");
    tabPane.setOpaque(true);
    Component[] components=tabPane.getComponents();
    for (int i=0, c=components.length; i<c; i++) tabAdded(components[i]);
  }

  protected void uninstallDefaults() {
    List tabs=new ArrayList(buttonToTab.values());
    for (Iterator iter=tabs.iterator(); iter.hasNext(); ) {
      Component tab=(Component)iter.next();
      tabRemoved(tab);
    }        
    super.uninstallDefaults();    
  }
  
  protected void installListeners() {tabPane.addContainerListener(tabListener=createTabListener()); super.installListeners();}
  protected void uninstallListeners() {super.uninstallListeners(); tabPane.removeContainerListener(tabListener);}

  protected ContainerListener createTabListener() {return new ContainerTabHandler();}

  protected PropertyChangeListener createPropertyChangeListener() {return new PropertyChangeHandler();}

  public Rectangle getTabBounds(JTabbedPane pane, int index) {Component tab=pane.getComponentAt(index); return tab.getBounds();}

  public int getTabRunCount(JTabbedPane pane) {return 0;}

  public int tabForCoordinate(JTabbedPane pane, int x, int y) {
    int index=-1;
    for (int i=0, c=pane.getTabCount(); i<c; i++) {
      if (pane.getComponentAt(i).contains(x, y)) {
        index=i;
        break;
      }
    }
    return index;
  }

  protected int indexOfComponent(Component component) {
    int index=-1;
    Component[] components=tabPane.getComponents();
    for (int i=0; i<components.length; i++) {
      if (components[i] == component) {
        index=i;
        break;
      }
    }
    return index;
  }

  protected TabButton createTabButton() {TabButton button=new TabButton(); button.setOpaque(true); button.putClientProperty("Button.glow",Boolean.FALSE); return button;}

  protected void tabAdded(final Component newTab) {

    TabButton button=(TabButton)tabToButton.get(newTab);
    if (button==null) {
      button=createTabButton();
      button.putClientProperty(BUTTON_ORIGINAL_FOREGROUND,button.getForeground());
      button.putClientProperty(BUTTON_ORIGINAL_BACKGROUND,button.getBackground());
      buttonToTab.put(button,newTab);
      tabToButton.put(newTab,button);
      button.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {

          final Component current=getVisibleComponent();
          Component target=newTab;
          if (((JOutlookBar)tabPane).isAnimated() && current!=target && current!=null && target!=null) {
            if (animator!=null) animator.stop();
            animator=new PercentLayoutAnimator(tabPane,(PercentLayout)tabPane.getLayout()) {
              protected void complete() {
                super.complete();
                tabPane.setSelectedComponent(newTab);
                nextVisibleComponent=null;
                if (current.getParent()==tabPane) ((PercentLayout)tabPane.getLayout()).setConstraint(current,"100%");
              }
            };
            nextVisibleComponent=newTab;
            animator.setTargetPercent(current,1.0f,0.0f);
            animator.setTargetPercent(newTab,0.0f,1.0f);
            animator.start();
          } else {
            nextVisibleComponent=null;
            tabPane.setSelectedComponent(newTab);
          }
        }
      });
    } else {
      tabPane.remove(button);
    }
    updateTabButtonAt(tabPane.indexOfComponent(newTab));
    int index=indexOfComponent(newTab);
    tabPane.add(button, index);
    if (JVM.current().isOneDotFive()) assureRectsCreated(tabPane.getTabCount());
  }

  protected void tabRemoved(Component removedTab) {

    TabButton button=(TabButton)tabToButton.get(removedTab);
    tabPane.remove(button);
    buttonToTab.remove(button);
    tabToButton.remove(removedTab);
  }

  /**
   * Called whenever a property of a tab is changed
   * 
   * @param index
   */

  protected void updateTabButtonAt(int index) {

    TabButton button=buttonForTab(index);
    button.setText(tabPane.getTitleAt(index));
    button.setIcon(tabPane.getIconAt(index));
    button.setDisabledIcon(tabPane.getDisabledIconAt(index));
    Color background=tabPane.getBackgroundAt(index);
    if (background==null) background=(Color)button.getClientProperty(BUTTON_ORIGINAL_BACKGROUND);
    button.setBackground(background);
    Color foreground=tabPane.getForegroundAt(index);
    if (foreground==null) foreground=(Color)button.getClientProperty(BUTTON_ORIGINAL_FOREGROUND);
    button.setForeground(foreground);
    button.setToolTipText(tabPane.getToolTipTextAt(index));
    button.setDisplayedMnemonicIndex(tabPane.getDisplayedMnemonicIndexAt(index));
    button.setMnemonic(tabPane.getMnemonicAt(index));
    button.setEnabled(tabPane.isEnabledAt(index));
    button.setHorizontalAlignment(((JOutlookBar)tabPane).getAlignmentAt(index));
  }

  protected TabButton buttonForTab(int index) {Component component=tabPane.getComponentAt(index); return (TabButton)tabToButton.get(component);}

  class PropertyChangeHandler implements PropertyChangeListener {

    public void propertyChange(PropertyChangeEvent e) {

      String name=e.getPropertyName();
      if ("tabPropertyChangedAtIndex".equals(name)) {
        int index=((Integer)e.getNewValue()).intValue();
        updateTabButtonAt(index);
      } else if ("tabPlacement".equals(name)) {
        updateTabLayoutOrientation();
      }
    }
  }

  protected void updateTabLayoutOrientation() {

    TabLayout layout=(TabLayout)tabPane.getLayout();
    int placement=tabPane.getTabPlacement();
    if (placement==JTabbedPane.TOP || placement==JTabbedPane.BOTTOM) {
      layout.setOrientation(PercentLayout.HORIZONTAL);
    } else {
      layout.setOrientation(PercentLayout.VERTICAL);
    }
  }
  
  /**
   * Manages tabs being added or removed
   */

  class ContainerTabHandler extends ContainerAdapter {

    public void componentAdded(ContainerEvent e) {if (!(e.getChild() instanceof UIResource)) {Component newTab=e.getChild(); tabAdded(newTab);}}
    public void componentRemoved(ContainerEvent e) {if (!(e.getChild() instanceof UIResource)) {Component oldTab=e.getChild(); tabRemoved(oldTab);}}
  }

  /**
   * Layout for the tabs, buttons get preferred size, tabs get all
   */

  protected class TabLayout extends PercentLayout {

    public void addLayoutComponent(Component component, Object constraints) {

      if (constraints==null) {
        if (component instanceof TabButton) {
          super.addLayoutComponent(component,"");
        } else {
          super.addLayoutComponent(component,"100%");
        }
      } else {
        super.addLayoutComponent(component,constraints);
      }
    }

    public void setLayoutConstraints(Container parent) {

      Component[] components=parent.getComponents();
      for (int i=0, c=components.length; i<c; i++) {
        if (!(components[i] instanceof TabButton)) {
          super.addLayoutComponent(components[i],"100%");
        }
      }
    }

    public void layoutContainer(Container parent) {

      int selectedIndex=tabPane.getSelectedIndex();
      Component visibleComponent=getVisibleComponent();
      if (selectedIndex<0) {
        if (visibleComponent!=null) {
          setVisibleComponent(null);
        }
      } else {
        Component selectedComponent=tabPane.getComponentAt(selectedIndex);
        boolean shouldChangeFocus=false;
        if (selectedComponent!=null) {
          if (selectedComponent!=visibleComponent && visibleComponent!=null) {
            Component currentFocusOwner=KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            if (currentFocusOwner!=null && SwingUtilities.isDescendingFrom(currentFocusOwner,visibleComponent)) {
              shouldChangeFocus=true;
            }
          }
          setVisibleComponent(selectedComponent);
          Component[] components=parent.getComponents();
          for (int i=0; i<components.length; i++) {
            if (!(components[i] instanceof UIResource) && components[i].isVisible() && components[i]!=selectedComponent) {
              components[i].setVisible(false);
              setConstraint(components[i],"*");
            }
          }
          
          if (ImageViewerOutlookBarUI.this.nextVisibleComponent!=null) ImageViewerOutlookBarUI.this.nextVisibleComponent.setVisible(true);
        }

        super.layoutContainer(parent);
        if (shouldChangeFocus) {
          if (!requestFocusForVisibleComponent0()) {
            tabPane.requestFocus();
          }
        }
      }
    }
  }

  protected boolean requestFocusForVisibleComponent0() {

    Component visibleComponent=getVisibleComponent();
    if (visibleComponent.isFocusable()) {
      visibleComponent.requestFocus();
      return true;
    } else if (visibleComponent instanceof JComponent) {
      if (((JComponent)visibleComponent).requestDefaultFocus()) { return true; }
    }
    return false;
  }

  protected static class TabButton extends JButton implements UIResource {

    public TabButton() {}
    public TabButton(ButtonUI ui) {setUI(ui);}
  }

  /**
   * Overriden to return an empty adapter,the default listener was
   * just implementing the tab selection mechanism
   */

  protected MouseListener createMouseListener() {return new MouseAdapter() {};}

  /**
   * Wraps any component in a Scrollable JPanel so it can work
   * correctly within a viewport
   */

  private static class ScrollableJPanel extends JPanel implements Scrollable {

    public ScrollableJPanel(Component component) {setLayout(new BorderLayout(0,0)); add("Center",component); setOpaque(false);}
    public Dimension getPreferredScrollableViewportSize() {return (super.getPreferredSize());}
    public boolean getScrollableTracksViewportWidth() {return true;}
    public boolean getScrollableTracksViewportHeight() {return false;}
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {return 16;}
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {return 16;}
  }

  public void paint(Graphics g, JComponent c) {}
  protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {}
  protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {}
  protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {}
  protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {}
  protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {}
  protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {}
  protected void paintIcon(Graphics g, int tabPlacement, int tabIndex, Icon icon, Rectangle iconRect, boolean isSelected) {}
  protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect) {}
  protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {}
  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
  protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {}
  protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {}
}
