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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;

import org.simbrain.network.connections.ConnectNeurons;
import org.simbrain.network.connections.OneToOne;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.gui.dialogs.synapse.SynapseDialog;

/**
 * <b>OneToOnePanel</b> creates a dialog for setting preferences of one to one
 * neuron connections.
 */
public class OneToOnePanel extends AbstractConnectionPanel {

    /** Sets the connection orientation. */
    private JComboBox orientationBox;

    /** Sets whether connections are bidirectional. */
    private JCheckBox bidirectionalConnection = new JCheckBox();

    /** Set base synapse type. */
    private JButton setSynapseType = new JButton();

    /**
     * Default constructor.
     */
    public OneToOnePanel(final OneToOne connection) {
        super(connection);
        orientationBox = new JComboBox(OneToOne.getOrientationTypes());
        setSynapseType.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ArrayList<Synapse> list = new ArrayList<Synapse>();
                Synapse temp = connection.getBaseSynapse();
                list.add(temp);
                SynapseDialog dialog = new SynapseDialog(list);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
                Synapse synapse = dialog.getSynapseList().get(0);
                connection.setBaseSynapse(synapse);
                setSynapseType.setText(synapse.getType());
            }

        });
        setSynapseType.setText(connection.getBaseSynapse().getType());
        addItem("Synapse Type:", setSynapseType);
        addItem("Orientation: ", orientationBox);
        addItem("Bidirectional Connections: ", bidirectionalConnection);
    }

    /**
     * {@inheritDoc}
     */
    public void commitChanges() {
        ((OneToOne) connection)
                .setUseBidirectionalConnections(bidirectionalConnection
                        .isSelected());
        ((OneToOne) connection)
                .setConnectOrientation((Comparator) orientationBox
                        .getSelectedItem());
    }

    /**
     * {@inheritDoc}
     */
    public void fillFieldValues() {
        bidirectionalConnection.setSelected(((OneToOne) connection)
                .isUseBidirectionalConnections());
        orientationBox.setSelectedItem(((OneToOne) connection)
                .getConnectOrientation());
    }

    /**
     *
     */
    public void fillFieldValues(ConnectNeurons connection) {
        // TODO Auto-Generated Method Stub
    }

}
