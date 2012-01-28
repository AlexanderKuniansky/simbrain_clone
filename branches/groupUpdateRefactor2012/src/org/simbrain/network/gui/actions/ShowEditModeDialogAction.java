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
package org.simbrain.network.gui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.simbrain.network.groups.subnetworks.Competitive;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.layouts.Layout;
import org.simbrain.resource.ResourceManager;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.StandardDialog;

/**
 * Show network preferences action.
 */
public final class ShowEditModeDialogAction extends AbstractAction {

    /** Network panel. */
    private final NetworkPanel networkPanel;

    /** Wand radius. */
    JTextField wandRadius = new JTextField();

    /**
     * Create a new show network preferences action with the specified network
     * panel.
     *
     * @param networkPanel networkPanel, must not be null
     */
    public ShowEditModeDialogAction(final NetworkPanel networkPanel) {

        super("Wand / Edit Mode Preferences...");

        if (networkPanel == null) {
            throw new IllegalArgumentException("networkPanel must not be null");
        }

        this.networkPanel = networkPanel;
        putValue(SMALL_ICON, ResourceManager.getImageIcon("Prefs.png"));
        this.putValue(this.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit()
                        .getMenuShortcutKeyMask()));

    }

    /**
     * Dialog for editing the wand properties.
     */
    private class WandDialog extends StandardDialog {

        @Override
        protected void closeDialogOk() {
            int rad = Integer.parseInt(wandRadius.getText());
            if (rad < 15) {
                rad = 15;
            }
            networkPanel.getEditMode().setWandRadius(rad);
            networkPanel.updateCursor();
            networkPanel.repaint();
            super.closeDialogOk();
        }

    }

    /** @see AbstractAction */
    public void actionPerformed(final ActionEvent event) {

        SwingUtilities.invokeLater(new Runnable() {

            /** @see Runnable */
            public void run() {
                WandDialog dialog = new WandDialog();
                LabelledItemPanel panel = new LabelledItemPanel();
                wandRadius.setText(""
                        + networkPanel.getEditMode().getWandRadius());
                panel.addItem("Wand radius", wandRadius);
                dialog.setContentPane(panel);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
    }
}