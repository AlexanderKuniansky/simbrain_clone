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
package org.simbrain.network.gui.actions.toolbar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;

import org.simbrain.network.gui.NetworkPanel;

/**
 * Clamps neurons action.
 *
 * TODO: This is no longer used. In case the code is useful to consult it is
 * being left here for now but should eventually be removed (JY 2013).
 */
public final class ShowClampToolBarAction extends AbstractAction {

    /** Network panel. */
    private final NetworkPanel networkPanel;

    /**
     * Create a new clamp neurons action with the specified network panel.
     *
     * @param networkPanel networkPanel, must not be null
     */
    public ShowClampToolBarAction(final NetworkPanel networkPanel) {

        super("Clamp Toolbar");

        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }

        this.networkPanel = networkPanel;

    }

    /** @see AbstractAction */
    public void actionPerformed(final ActionEvent event) {

        // Perform action
        JCheckBoxMenuItem cb = (JCheckBoxMenuItem) event.getSource();

        // Determine status
        // networkPanel.getClampToolBar().setVisible(cb.isSelected());

    }

    // For reference: From NetworkActionManager. See r2805
    // /**
    // * Return the show clamp tool bar menu item.
    // *
    // * @return the show clamp tool bar menu item
    // */
    // public JCheckBoxMenuItem getShowClampToolBarMenuItem() {
    // JCheckBoxMenuItem actionWrapper = new JCheckBoxMenuItem(
    // showClampToolBarAction);
    // actionWrapper.setSelected(networkPanel.getClampToolBar().isVisible());
    // return actionWrapper;
    // }

}