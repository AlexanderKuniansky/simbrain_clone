/*
 * Part of Simbrain--a java-based neural network kit
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
package org.simbrain.network.gui.dialogs.neuron.generator_panels;

import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.NeuronUpdateRule;
import org.simbrain.network.gui.NetworkUtils;
import org.simbrain.network.gui.dialogs.neuron.AbstractNeuronRulePanel;
import org.simbrain.network.neuron_update_rules.activity_generators.StochasticRule;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.SimbrainConstants;

/**
 * <b>StochasticNeuronPanel</b>.
 */
public class StochasticGeneratorPanel extends AbstractNeuronRulePanel {

    /** Firing probability field. */
    private JTextField tfFiringProbability = new JTextField();

    /** A reference to the neuron rule being edited. */
    private StochasticRule neuronRef = new StochasticRule();
    
    /** Main panel. */
    private LabelledItemPanel mainPanel = new LabelledItemPanel();


    /**
     * Creates an instance of this panel.
     *
     */
    public StochasticGeneratorPanel() {
        super();
        mainPanel.addItem("Firing Probability", tfFiringProbability);
        add(mainPanel);
    }

    /**
     * Populates the fields with current data.
     * @param ruleList
     */
    public void fillFieldValues(List<NeuronUpdateRule> ruleList) {

        neuronRef = (StochasticRule) ruleList.get(0);

        // (Below) Handle consistency of multiple selections

        // Handle Firing Probability
        if (!NetworkUtils.isConsistent(ruleList, StochasticRule.class,
                "getFiringProbability"))
            tfFiringProbability.setText(SimbrainConstants.NULL_STRING);
        else
            tfFiringProbability.setText(Double.toString(neuronRef
                    .getFiringProbability()));

    }

    /**
     * Populates the fields with default data.
     */
    public void fillDefaultValues() {
        StochasticRule neuronRef = new StochasticRule();
        tfFiringProbability.setText(Double.toString(neuronRef
                .getFiringProbability()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges(Neuron neuron) {

        if (neuron.getUpdateRule() instanceof StochasticRule) {
            neuronRef = (StochasticRule) neuron.getUpdateRule();
        } else {
            neuron.setUpdateRule(neuronRef);
        }

        // Firing Probability
        if (!tfFiringProbability.getText()
                .equals(SimbrainConstants.NULL_STRING))
            neuronRef.setFiringProbability(Double
                    .parseDouble(tfFiringProbability.getText()));

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commitChanges(List<Neuron> neurons) {

        // Firing Probability
        if (!tfFiringProbability.getText()
                .equals(SimbrainConstants.NULL_STRING))
            neuronRef.setFiringProbability(Double
                    .parseDouble(tfFiringProbability.getText()));

        for (Neuron n : neurons) {
            n.setUpdateRule(neuronRef);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NeuronUpdateRule getPrototypeRule() {
        return neuronRef;
    }

    @Override
    protected void writeValuesToRules(List<Neuron> neurons) {
        // TODO Auto-generated method stub

    }
}
