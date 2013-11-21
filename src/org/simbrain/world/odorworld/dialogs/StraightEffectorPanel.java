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
package org.simbrain.world.odorworld.dialogs;

import javax.swing.JTextField;

import org.simbrain.world.odorworld.effectors.StraightMovement;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.RotatingEntity;

/**
 * Panel to add a straight movement effector to an entity.
 *
 * @author Lam Nguyen
 *
 */
public class StraightEffectorPanel extends AbstractEffectorPanel {

    /** Text field to edit label. */
    private JTextField label = new JTextField("Go-Straight");

    /** Text field to edit the base movement rate. */
    private JTextField bma = new JTextField("" + 0);

    /** Entity to which a straight movement effector is being added. */
    private RotatingEntity entity;

    /**
     * Default constructor.
     *
     * @param entity the entity to which a straight movement effector is added.
     */
    public StraightEffectorPanel(OdorWorldEntity entity) {
        this.entity = (RotatingEntity) entity;
        addItem("Label", label);
        addItem("Base movement amount", bma); // TODO: Better name?
        setVisible(true);
    }

    @Override
    public void commitChanges() {
        entity.addEffector(new StraightMovement(entity, bma.getText()));
    }

    /** Save changes to an edited straight movement effector. */
    public void commitChanges(StraightMovement effector) {
        effector.setLabel(label.getText());
        effector.setScalingFactor(Double.parseDouble(bma.getText()));
    }

    /** Fill in appropriate text fields when straight movement effector is being modified. */
    public void fillFieldValues(StraightMovement effector) {
        label.setText("" + effector.getLabel());
        bma.setText("" + effector.getScalingFactor());
    }

    @Override
    public void fillFieldValues() {
        // TODO Auto-generated method stub

    }
}
