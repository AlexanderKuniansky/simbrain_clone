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
package org.simbrain.network.gui.dialogs.layout;

import javax.swing.JTextField;

import org.simbrain.network.layouts.HexagonalGridLayout;
import org.simbrain.network.layouts.Layout;


/**
 * <b>LayoutPanel</b> allows the user to define the layout of a network.
 */
public class HexagonalGridLayoutPanel extends AbstractLayoutPanel {

    /** Spacing field. */
    private JTextField tfNumColumns = new JTextField("3");

    /** Spacing field. */
    private JTextField tfHSpacing = new JTextField("50");

    /** Vertical spacing field. */
    private JTextField tfVSpacing = new JTextField("50");

    /**
     * Default constructor.
     */
    public HexagonalGridLayoutPanel() {
        this.addItem("Number of columns", tfNumColumns);
        this.addItem("Horizontal spacing between neurons", tfHSpacing);
        this.addItem("Vertical spacing between neurons", tfVSpacing);
    }

    /** @see AbstractLayoutPanel */
    public Layout getNeuronLayout() {
        HexagonalGridLayout layout = new HexagonalGridLayout(Double.parseDouble(tfHSpacing
                .getText()), Double.parseDouble(tfVSpacing.getText()), Integer
                .parseInt(tfNumColumns.getText()));
        return layout;
    }

    /**
     * Applies settings after OK button is pressed.
     */
    public void commitChanges() {
        HexagonalGridLayout.setNumColumns(Integer.parseInt(tfNumColumns.getText()));
        HexagonalGridLayout.setHSpacing(Double.parseDouble(tfHSpacing.getText()));
        HexagonalGridLayout.setVSpacing(Double.parseDouble(tfVSpacing.getText()));
    }

    @Override
    public void fillFieldValues() {
        tfNumColumns.setText(Integer.toString(HexagonalGridLayout.getNumColumns()));
        tfHSpacing.setText(Double.toString(HexagonalGridLayout.getHSpacing()));
        tfVSpacing.setText(Double.toString(HexagonalGridLayout.getVSpacing()));
    }
}