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
package org.simbrain.network.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.NetworkUpdateManagerPanel;
import org.simbrain.resource.ResourceManager;
import org.simbrain.util.StandardDialog;

/**
 * Show network updater panel.
 */
public final class ShowNetworkUpdaterDialog extends AbstractAction {

    /** Network panel. */
    private final NetworkPanel networkPanel;

    /**
     * Construct the action.
     *
     * @param networkPanel networkPanel, must not be null
     */
    public ShowNetworkUpdaterDialog(final NetworkPanel networkPanel) {

        super("Update sequence...");
        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }
        putValue(SMALL_ICON, ResourceManager.getImageIcon("Sequence.png"));

        this.networkPanel = networkPanel;
    }

    /** @see AbstractAction */
    public void actionPerformed(final ActionEvent event) {

        final NetworkUpdateManagerPanel updatePanel = new NetworkUpdateManagerPanel(
                networkPanel.getNetwork());
        StandardDialog dialog = new StandardDialog();
        dialog.setContentPane(updatePanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}