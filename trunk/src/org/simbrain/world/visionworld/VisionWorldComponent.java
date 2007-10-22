/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2006 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.world.visionworld;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.simbrain.workspace.WorkspaceComponentListener;
import org.simbrain.workspace.Consumer;
import org.simbrain.workspace.Coupling;
import org.simbrain.workspace.WorkspaceComponent;

/**
 * Vision world frame.
 */
public final class VisionWorldComponent extends WorkspaceComponent<WorkspaceComponentListener> {//implements CouplingContainer {

    /**
     * Create a new vision world frame with the specified workspace.
     *
     * @param workspace workspace, must not be null
     */
    public VisionWorldComponent(String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
//    public CouplingContainer getCouplingContainer() {
//        return this;
//    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public String getFileExtension() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void save(File saveFile) {
        // TODO Auto-generated method stub
        
    }


    /** {@inheritDoc} */
    public List<Consumer> getConsumers() {
        return Collections.<Consumer>emptyList();
    }


    /** {@inheritDoc} */
    public List<Coupling> getCouplings() {
        return Collections.<Coupling>emptyList();
    }


    /** {@inheritDoc} */
//    public List<Producer> getProducers() {
//        List<Producer> producers = new ArrayList<Producer>();
//        VisionWorldModel model = visionWorld.getModel();
//        SensorMatrix sensorMatrix = model.getSensorMatrix();
//        for (int column = 0, columns = sensorMatrix.columns(); column < columns; column++) {
//            for (int row = 0, rows = sensorMatrix.rows(); row < rows; row++) {
//                Sensor sensor = sensorMatrix.getSensor(row, column);
//                producers.add(sensor);
//            }
//        }
//        return Collections.unmodifiableList(producers);
//    }

    /** {@inheritDoc} */
    public void update() {
//        VisionWorldModel model = visionWorld.getModel();
//        PixelMatrix pixelMatrix = model.getPixelMatrix();
//        SensorMatrix sensorMatrix = model.getSensorMatrix();
//        for (int column = 0, columns = sensorMatrix.columns(); column < columns; column++) {
//            for (int row = 0, rows = sensorMatrix.rows(); row < rows; row++) {
//                Sensor sensor = sensorMatrix.getSensor(row, column);
//                sensor.sample(pixelMatrix);
//            }
//        }
    }

    @Override
    public void open(File openFile) {
        // TODO Auto-generated method stub
    }

}
