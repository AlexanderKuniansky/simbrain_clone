/*
 * Copyright (C) 2005,2007 The Authors.  See http://www.simbrain.net/credits
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.simbrain.network;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.simbrain.network.interfaces.Neuron;
import org.simbrain.network.interfaces.RootNetwork;
import org.simbrain.network.interfaces.Synapse;
import org.simbrain.network.listeners.NetworkEvent;
import org.simbrain.network.listeners.NeuronListener;
import org.simbrain.workspace.PotentialAttribute;
import org.simbrain.workspace.AttributeType;
import org.simbrain.workspace.WorkspaceComponent;

/**
 * Network component.
 */
public final class NetworkComponent extends WorkspaceComponent {

    /** Reference to root network, the main model network. */
    private RootNetwork rootNetwork = new RootNetwork();

    /**
     * Create a new network component.
     */
    public NetworkComponent(final String name) {
        super(name);
        init();
    }

    /**
     * Create a new network component.
     */
    public NetworkComponent(final String name, final RootNetwork network) {
        super(name);
        this.rootNetwork = network;
        init();
    }

    /**
     * By default, neuronwrappers are all that is added.
     */
    private void init() {

        getAttributeTypes().add(new AttributeType("Neuron", "Activation", double.class, true));
        getAttributeTypes().add(new AttributeType("Neuron", "UpperBound", double.class, false));
        getAttributeTypes().add(new AttributeType("Neuron", "LowerBound", double.class, false));
        getAttributeTypes().add(new AttributeType("Neuron", "Label", String.class, false));
        getAttributeTypes().add(new AttributeType("Synapse", "Strength", double.class, false));

        rootNetwork.addNeuronListener(new NeuronListener() {
            /**
             * {@inheritDoc}
             */
            public void neuronAdded(NetworkEvent<Neuron> e) {
                NetworkComponent.this.firePotentialAttributeUpdateEvent(NetworkComponent.this);
            }

            /**
             * {@inheritDoc}
             */
            public void neuronTypeChanged(NetworkEvent<Neuron> e) {
            }

            /**
             * {@inheritDoc}
             */
            public void neuronMoved(NetworkEvent<Neuron> e) {
            }

            /**
             * {@inheritDoc}
             */
            public void neuronRemoved(NetworkEvent<Neuron> e) {
                NetworkComponent.this.firePotentialAttributeUpdateEvent(NetworkComponent.this);
            }

            public void neuronChanged(NetworkEvent<Neuron> e) {
            }
        });
    }

    /**
     * {@inheritDoc}
     */
     public static NetworkComponent open(final InputStream input, final String name, final String format) {
        RootNetwork newNetwork = (RootNetwork) RootNetwork.getXStream().fromXML(input);
        return new NetworkComponent(name, newNetwork);
    }

    @Override
    public void save(final OutputStream output, final String format) {
        RootNetwork.getXStream().toXML(rootNetwork, output);
    }

    /**
     * Returns the root network.
     *
     * @return the root network
     */
    public RootNetwork getRootNetwork() {
        return rootNetwork;
    }

    @Override
    public void update() {
        rootNetwork.update();
    }

    @Override
    public void closing() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getXML() {
        return RootNetwork.getXStream().toXML(rootNetwork);
    }


    @Override
    public List<PotentialAttribute> getPotentialConsumers() {
        List<PotentialAttribute> returnList = new ArrayList<PotentialAttribute>();
        for (AttributeType type : this.getAttributeTypes()) {
            if (type.isVisible()) {
                if (type.getTypeID().equalsIgnoreCase("Neuron")) {
                    for (Neuron neuron : rootNetwork.getFlatNeuronList()) {
                        returnList.add(new PotentialAttribute(this, neuron.getId(), neuron, type));
                    }
                } else if (type.getTypeID().equalsIgnoreCase("Synapse")) {
                    for (Synapse synapse : rootNetwork.getFlatSynapseList()) {
                        returnList.add(new PotentialAttribute(this, synapse.getId(), synapse, type));
                    }
                }

            }
        }
        return returnList;
    }

    @Override
    public List<PotentialAttribute> getPotentialProducers() {
        // Thus far, potential consumer and producers are the same
        return getPotentialConsumers();
    }

    // TODO: Link to NetworkSettings.
//    @Override
//    public void setCurrentDirectory(final String currentDirectory) {
//        super.setCurrentDirectory(currentDirectory);
////        NetworkPreferences.setCurrentDirectory(currentDirectory);
//    }
//
//    @Override
//    public String getCurrentDirectory() {
////       return NetworkPreferences.getCurrentDirectory();
//        return null;
//    }


}