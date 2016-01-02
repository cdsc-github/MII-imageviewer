/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class MircFrame extends JFrame implements ActionListener {

    ArrayList <BufferedImage> images;
    int index;
    JButton button = new JButton ("Next");
    JLabel iconLabel = new JLabel (new ImageIcon());

    public MircFrame (ArrayList<BufferedImage> images) {

      index = 0;

      this.images = images;
      setSize (new Dimension (640,480));
      setVisible (true);

      button.addActionListener(this);
      button.setSize (75, 30);
      add (button);
      button.setVisible (true);

      add (iconLabel);

      displayNextImage ();
    }

    public void displayNextImage ()  {

	ImageIcon icon = new ImageIcon (images.get(index));
	iconLabel.setIcon (icon);

	index++;

	if (index == images.size())
	    index = 0;

    }

    public void actionPerformed (ActionEvent e)  {
     	displayNextImage();
    }



}

