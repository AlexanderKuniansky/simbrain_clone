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
package org.simbrain.network.gui.dialogs.synapse;

import java.util.ArrayList;
import java.util.List;

import org.simbrain.network.core.Synapse;
import org.simbrain.network.gui.dialogs.RandomPanelNetwork;
import org.simbrain.network.synapse_update_rules.RandomSynapseRule;

/**
 * <b>RandomSynapsePanel</b>.
 */
public class RandomSynapseRulePanel extends AbstractSynapsePanel {

    /** Random panel. */
    private RandomPanelNetwork rp = new RandomPanelNetwork(false);

    /**
     * This method is the default constructor.
     */
    public RandomSynapseRulePanel() {
        this.add(rp);
    }

    /**
     * Populate fields with current data.
     */
    public void fillFieldValues() {
        ArrayList randomPanels = new ArrayList();

        for (int i = 0; i < ruleList.size(); i++) {
            randomPanels.add(((RandomSynapseRule) ruleList.get(i))
                    .getRandomizer());
        }

        rp.fillFieldValues(randomPanels);
    }

    /**
     * Fill field values to default values for random neuron.
     */
    public void fillDefaultValues() {
        rp.fillDefaultValues();
    }

    /**
     * Called externally when the dialog is closed, to commit any changes made.
     */
    public void commitChanges() {
        for (int i = 0; i < ruleList.size(); i++) {
            RandomSynapseRule synapseRef = (RandomSynapseRule) ruleList.get(i);
            rp.commitRandom(synapseRef.getRandomizer());
        }
    }

	@Override
	public void commitChanges(List<Synapse> commitSynapses) {
		RandomSynapseRule synapseRef = new RandomSynapseRule();
		rp.commitRandom(synapseRef.getRandomizer());
		
		for(Synapse s : commitSynapses) {
			s.setLearningRule(synapseRef);
		}
		
	}

	@Override
	public void commitChanges(Synapse templateSynapse) {
		RandomSynapseRule synapseRef = new RandomSynapseRule();
		rp.commitRandom(synapseRef.getRandomizer());
		
		templateSynapse.setLearningRule(synapseRef);
		
	}
	
}