/*******************************************************************************
 * Copyright (c) 2011 UCLA Medical Imaging Informatics Group
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 ******************************************************************************/
package imageviewer.ui.dialog;

import javax.swing.JDialog; 
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.GridLayout;
import javax.swing.ListSelectionModel;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.Dimension;

/**
 * Specialized modal dialog for user selection from a list of
 * items.  Includes a scroll pane with a list of selectable
 * values and a select all button not found in JOptionPane
 * windows.
 *
 * @author Kyle Singleton
 * @version $Revision: 1.1 $ $Date: 2008/12/05 10:23:00 $
 */

public class MultiSelectDialog extends JDialog implements ActionListener {
    private JPanel selectionPanel = null;
    private JList selectList = null;
    private JButton confirm = null;
    private JButton selectAll = null;
    private JButton cancel = null;
    private String[] selectionResult = new String[0];

    public String[] getSelected() { return selectionResult; }

    /**
     * Opens a dialog box which will display a message and a list of selectable values
     * to be displayed in a list.
     *
     * @param frame - parent frame calling the dialog
     * @param modal -  boolean value indicating if this dialog should be modal
     * @param programMessage - message to be displayed as directions for the dialog window
     * @param option - an array of options to be provided to the user as a list
     */

    public MultiSelectDialog(JFrame frame, boolean modal, String programMessage, Object[] options) {
        super(frame, modal);
	GridLayout selectLayout = new GridLayout(3,1);
        selectionPanel = new JPanel(selectLayout);
        getContentPane().add(selectionPanel);
	JPanel buttonPanel = new JPanel();

	confirm = new JButton("Confirm");
	confirm.addActionListener(this);

        selectAll = new JButton("Select All");
        selectAll.addActionListener(this);
   
        cancel = new JButton("Cancel");
        cancel.addActionListener(this);

	Color newColor = new Color(56,66,81);

	buttonPanel.add(selectAll);
	buttonPanel.add(confirm);
	buttonPanel.add(cancel);
	buttonPanel.setOpaque(true);
	buttonPanel.setBackground(newColor);

	selectList = new JList(options);
	selectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	selectList.setLayoutOrientation(JList.VERTICAL);
	selectList.setVisibleRowCount(10);
	selectList.setSelectionBackground(new Color(0,102,153));

	JLabel aLabel = new JLabel(programMessage);
	aLabel.setForeground(java.awt.Color.WHITE);

        selectionPanel.add(aLabel);
	selectionPanel.add(new JScrollPane(selectList));
        selectionPanel.add(buttonPanel);
	selectionPanel.setOpaque(true);
	selectionPanel.setBackground(newColor);
	selectionPanel.setPreferredSize(new Dimension(300,200));
        pack();
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(confirm == e.getSource()) {

	    Object[] tempResult = selectList.getSelectedValues();
            selectionResult = new String[tempResult.length];
	    System.arraycopy(tempResult,0,selectionResult,0,tempResult.length);
            setVisible(false);
        }
        else if(cancel == e.getSource()) {
	    selectionResult = new String[0];
            setVisible(false);
        }
	else if(selectAll == e.getSource()) {
	    int maxIndex = selectList.getModel().getSize()-1;
	    selectList.getSelectionModel().setSelectionInterval(0,maxIndex);
	}
    }
    
}
