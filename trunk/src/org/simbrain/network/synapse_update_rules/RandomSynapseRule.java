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
package org.simbrain.network.synapse_update_rules;

import org.simbrain.network.core.Synapse;
import org.simbrain.network.core.SynapseUpdateRule;
import org.simbrain.util.randomizer.Randomizer;

/**
 * <b>RandomSynapse</b>.
 */
public class RandomSynapseRule extends SynapseUpdateRule {

    /** Randomizer. */
    private Randomizer randomizer = new Randomizer();

    @Override
    public void init(Synapse synapse) {
    }

    @Override
    public String getDescription() {
        return "Random";
    }

    @Override
    public SynapseUpdateRule deepCopy() {
        RandomSynapseRule rs = new RandomSynapseRule();
        rs.randomizer = new Randomizer(randomizer);
        return rs;
    }

    @Override
    public void update(Synapse synapse) {
        randomizer.setUpperBound(synapse.getUpperBound());
        randomizer.setLowerBound(synapse.getLowerBound());
        synapse.setStrength(synapse.clip(randomizer.getRandom()));
    }

    /**
     * @return Returns the randomizer.
     */
    public Randomizer getRandomizer() {
        return randomizer;
    }
}