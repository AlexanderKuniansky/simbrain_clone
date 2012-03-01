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
package org.simbrain.network.trainers;

import java.util.EventListener;
import java.util.List;

import org.simbrain.network.interfaces.Network;
import org.simbrain.network.interfaces.Neuron;

/**
 * Observer class for trainer objects.
 *
 * @author jyoshimi
 */
public interface TrainerListener extends EventListener {

    /**
     * Called when the error value is updated.  Only applies to iterable methods.
     */
    void errorUpdated();

    /**
     * The trainer's input data changed.
     *
     * @param inputData the new input data.
     */
    void inputDataChanged(double[][] inputData);

    /**
     * The trainer's training data changed.
     *
     * @param trainingData the new training data
     */
    void trainingDataChanged(double[][] trainingData);

}
