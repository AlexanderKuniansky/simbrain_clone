/*
 * Part of Simbrain--a java-based neural network kit Copyright (C) 2005,2007 The
 * Authors. See http://www.simbrain.net/credits This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version. This program is
 * distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. You
 * should have received a copy of the GNU General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 59 Temple Place
 * - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.simbrain.network.gui.dialogs.connect;

import java.awt.BorderLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.simbrain.network.connections.ConnectNeurons;
import org.simbrain.util.LabelledItemPanel;

/**
 * <b>AbstractConnectionPanel</b> is the abstract panel for all specific panels
 * for setting connection properties.
 */
public abstract class AbstractConnectionPanel extends JPanel {

    /** Main panel. */
    protected LabelledItemPanel mainPanel = new LabelledItemPanel();

    /** Reference to underlying connection object. */
    protected ConnectNeurons connection;

    /**
     * Adds a new item.
     *
     * @param text Text to add
     * @param comp SimbrainComponent to add
     */
    public void addItem(final String text, final JComponent comp) {
        mainPanel.addItem(text, comp);
    }

    /**
     * Adds a new item label.
     *
     * @param text Text to add
     * @param comp Component to add.
     */
    public void addItemLabel(final JLabel text, final JComponent comp) {
        mainPanel.addItemLabel(text, comp);
    }

    /**
     * This method is the default constructor.
     */
    public AbstractConnectionPanel(final ConnectNeurons connection) {
        this.connection = connection;
        this.setLayout(new BorderLayout());
        this.add(mainPanel, BorderLayout.CENTER);
    }

    /**
     * Populate fields with current data.
     */
    public abstract void fillFieldValues();

    /**
     * Populates fields with data from a connect neurons object. 
     * @param connection
     * @throws ClassCastException may occur if a sub-class of connect neurons
     * not consistent with one of the sub-classes of AbstractConnectionPanel
     * is passed into this method. 
     */
    public abstract void fillFieldValues(ConnectNeurons connection)
    		throws ClassCastException;
    
    /**
     * Called externally when the dialog is closed, to commit any changes made.
     */
    public abstract void commitChanges();

    /**
     * Add notes or other text to bottom of panel. Can be html formatted.
     *
     * @param text Text to be added
     */
    public void addBottomText(final String text) {
        JPanel labelPanel = new JPanel();
        JLabel theLabel = new JLabel(text);
        labelPanel.add(theLabel);
        this.add(labelPanel, BorderLayout.SOUTH);
    }

    /**
     * @return the ConnectNeurons object
     */
	public ConnectNeurons getConnection() {
		return connection;
	}

	/**
	 * @param connection the desired ConnectNeurons object
	 */
	public void setConnection(ConnectNeurons connection) {
		this.connection = connection;
	}
}
