
package org.simbrain.network.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import org.simbrain.network.NetworkPanel;
import org.simnet.coupling.InteractionMode;

import org.simbrain.network.nodes.NeuronNode;

import org.simbrain.resource.ResourceManager;
import org.simnet.neurons.LinearNeuron;

/**
 * New neuron action.
 */
public final class NewNeuronAction
    extends AbstractAction {

    /** Network panel. */
    private final NetworkPanel networkPanel;


    /**
     * Create a new neuron action with the specified network panel.
     *
     * @param networkPanel network panel, must not be null
     */
    public NewNeuronAction(final NetworkPanel networkPanel) {
        super("New Neuron");

        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }

        this.networkPanel = networkPanel;
        putValue(SMALL_ICON, ResourceManager.getImageIcon("New.gif"));
        putValue(SHORT_DESCRIPTION, "Add new node(p)");

        networkPanel.getInputMap().put(KeyStroke.getKeyStroke('p'), this);
        networkPanel.getActionMap().put(this, this);

    }


    /** @see AbstractAction */
    public final void actionPerformed(final ActionEvent event) {

        LinearNeuron neuron = new LinearNeuron();
        neuron.setActivation(1);
        networkPanel.getNetwork().addNeuron(neuron);
        networkPanel.repaint();
    }
}