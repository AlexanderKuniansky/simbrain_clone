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
package org.simbrain.network.gui.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.simbrain.network.groups.LayeredNetwork;
import org.simbrain.network.groups.SubnetworkGroup;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.interfaces.Group;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * Represents a {@link org.simbrain.network.interfaces.Group}.  This class can be 
 * used for default behavior.
 * 
 * Subclasses can provide custom behavior:
 * - Context menu
 * - Tooltips, etc. 
 * - Formatting for interaction box 
 * - Formatting for outline
 * - Insets for outline
 */
public class GroupNode extends PPath implements PropertyChangeListener {

    /** References to outlined objects. */
    private ArrayList<PNode> outlinedObjects = new ArrayList<PNode>();

    /** Default stroke (dashed line). */
    private static final BasicStroke DEFAULT_STROKE = new BasicStroke(1.0f,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10.0f}, 0.0f);

    /** Default outline padding. */
    private final double DEFAULT_PADDING = 2.0d;
    
    /** Distance between nodes and the outline itself. */
    private double outlinePadding = DEFAULT_PADDING;

    /** Interaction box. */
    private InteractionBox interactionBox;

    /** Network panel. */
    private NetworkPanel networkPanel;
    
    /** The model group. */
    private final Group group;

    /**
     * Create a new abstract subnetwork node from the specified parameters.
     *
     * @param networkPanel
     *            networkPanel for this subnetwork node, must not be null.
     * @param group
     *            the group object being represented
     */
    public GroupNode(final NetworkPanel networkPanel, final Group group) {
        this.networkPanel = networkPanel;
        this.group = group;
        setStroke(DEFAULT_STROKE);
        setStrokePaint(Color.yellow);
        InteractionBox box = new InteractionBox(networkPanel, this);
        setInteractionBox(box);
        this.setConextMenu(getDefaultContextMenu());
        this.setTextLabel(group.getLabel());

    }
    
    /**
     * Set the interaction box. 
     *
     * @param interactionBox
     */
    protected final void setInteractionBox(InteractionBox newBox) {
        if (this.interactionBox != null) {
            this.removeChild(interactionBox);
        }
        this.interactionBox = newBox;
        this.addChild(interactionBox);
        updateInteractionBox();
        updateText();
    }
    
    /**
     * Returns a reference to the interaction box.
     *
     * @return the interactionBox.
     */
    protected InteractionBox getInteractionBox() {
        return interactionBox;
    }

    /** @see PPath. */
    public void propertyChange(final PropertyChangeEvent arg0) {
        updateBounds();
    }


    /**
     * Creates default actions for all model group nodes.
     *
     * @return context menu populated with default actions.
     */
    protected JPopupMenu getDefaultContextMenu() {
        JPopupMenu ret = new JPopupMenu();
        Action groupOnOff = new AbstractAction("Group is active") {
            public void actionPerformed(final ActionEvent event) {
                //group.toggleOnOff(); //REDO
            }
        };
        Action removeGroup = new AbstractAction("Remove group") {
            public void actionPerformed(final ActionEvent event) {
                getNetworkPanel().getRootNetwork().deleteGroup(group);
            }
        };
        Action editGroupName = new AbstractAction("Edit group name...") {
            public void actionPerformed(final ActionEvent event) {
                String newName = JOptionPane.showInputDialog("Name:");
                GroupNode.this.setTextLabel(newName);
            }
        };
        JCheckBoxMenuItem groupOnOffItem = new JCheckBoxMenuItem(groupOnOff);
        ret.add(groupOnOffItem);
        ret.add(removeGroup);
        ret.add(editGroupName);
        return ret;
    }

    /**
     * Add a node for reference.
     *
     * @param node node to add.
     */
    public void addPNode(final PNode node) {
        node.addPropertyChangeListener(this);
        // Below was the source of major performance issues
        //node.getParent().addPropertyChangeListener(this); 
        outlinedObjects.add(node);
    }
    
    /**
     * Remove a reference node.
     *
     * @param node node to remove.
     */
    public void removePNode(final PNode node) {
        outlinedObjects.remove(node);
        node.removePropertyChangeListener(this);
        if (outlinedObjects.isEmpty()) {
            this.removeFromParent();
        }
    }

    /**
     * Update the text label to reflect underlying group label.
     */
    public void updateText() {
        this.setTextLabel(group.getLabel());
        this.updateInteractionBox();
    }

    /**
     * @return the group
     */
    public Group getGroup() {
        return group;
    }
    
    /**
     * Updated bounds of outline based on location of its outlined objects.
     */
    public void updateBounds() {

        PBounds bounds = new PBounds();
        for (PNode node : outlinedObjects) {
            PBounds childBounds = node.getGlobalBounds();
            bounds.add(childBounds);
        }

        bounds.setRect(bounds.getX() - outlinePadding,
                bounds.getY() - outlinePadding,
                bounds.getWidth() + (2 * outlinePadding),
                bounds.getHeight() + (2 * outlinePadding));

        setPathToRectangle((float) bounds.getX(), (float) bounds.getY(),
                            (float) bounds.getWidth(), (float) bounds.getHeight());

        updateInteractionBox();
        moveToBack();
    }

    /**
     * Update location of interaction box.
     */
    protected void updateInteractionBox() {
        interactionBox.setOffset(
                getBounds().getX() + interactionBox.getBoxOffset_X(),
                getBounds().getY() - interactionBox.getHeight()
                        + interactionBox.getBoxOffset_Y());
    }

    /**
     * Set the context menu on the interaction box.
     *
     * @param menu the new menu.
     */
    public void setConextMenu(final JPopupMenu menu) {
        interactionBox.setContextMenu(menu);
    }

    /**
     * Set a text label on the interaction box.
     *
     * @param text the text to set.
     */
    public void setTextLabel(final String text) {
        interactionBox.setText(text);
    }

    /**
     * @return the networkPanel
     */
    public NetworkPanel getNetworkPanel() {
        return networkPanel;
    }

    /**
     * @param padding the padding to set
     */
    protected void setOutlinePadding(double padding) {
        this.outlinePadding = padding;
    }
    
    /**
     * @return the padding
     */
    protected double getOutlinePadding() {
        return outlinePadding;
    }

    /**
     * Select all grouped objects.
     */
    protected void selectAllNodes() {
        System.out.println(group.getLabel());
        if ((group instanceof LayeredNetwork)
                || (group instanceof SubnetworkGroup)) {
            networkPanel.setSelection(getChildrenNeuronNodes(this));
        } else {
            networkPanel.setSelection(outlinedObjects);
        }
    }

    /**
     * @return the outlinedObjects
     */
    protected List<PNode> getOutlinedObjects() {
        return outlinedObjects;
    }

    /**
     * Helper method to get all children neuron nodes.  Recursively finds all
     * children neuron nodes.
     *
     * @param parentNode the node whose children are being sought
     * @return the list of children neuron nodes.
     */
    protected List<NeuronNode> getChildrenNeuronNodes(GroupNode parentNode) {
        
        List<NeuronNode> ret = new ArrayList<NeuronNode>();
        for (PNode node : parentNode.getOutlinedObjects()) {
            if (node instanceof NeuronNode) {
                ret.add((NeuronNode) node);
            } else if (node instanceof GroupNode) {
                ret.addAll(getChildrenNeuronNodes((GroupNode)node));
            }
        }
        return ret;
    }
    
}