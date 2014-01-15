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

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;
import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.SpikingNeuronUpdateRule;
import org.simbrain.network.gui.NetworkActionManager;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.actions.TestInputAction;
import org.simbrain.network.gui.actions.edit.CopyAction;
import org.simbrain.network.gui.actions.edit.CutAction;
import org.simbrain.network.gui.actions.edit.DeleteAction;
import org.simbrain.network.gui.actions.edit.PasteAction;
import org.simbrain.network.gui.actions.neuron.SetNeuronPropertiesAction;
import org.simbrain.network.gui.dialogs.neuron.NeuronDialog;
import org.simbrain.util.Utils;
import org.piccolo2d.PNode;
import org.piccolo2d.nodes.PPath;
import org.piccolo2d.nodes.PText;
import org.piccolo2d.nodes.PPath.Float;
import org.piccolo2d.util.PBounds;

/**
 * <b>NeuronNode</b> is a Piccolo PNode corresponding to a Neuron in the neural
 * network model.
 */
public class NeuronNode extends ScreenElement implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(NeuronNode.class);

    /** The logical neuron this screen element represents. */
    protected Neuron neuron;

    /** Diameter of neuron. */
    private static final int DIAMETER = 24;

    /** Length of arrow. */
    private static final int ARROW_LINE = 20;

    /** Arrow associated with output node. */
    // private PPath outArrow;

    /** Arrow associated with input node. */
    // private PPath inArrow;

    /** Text showing sensory coupling information. */
    // private PText inLabel = new PText();

    /** Text showing motor coupling information. */
    // private PText outLabel = new PText();

    /** Font for input and output labels. */
    public static final Font IN_OUT_FONT = new Font("Arial", Font.PLAIN, 9);

    /** Main circle of node. */
    private PPath circle;

    /** A list of SynapseNodes connected to this NeuronNode; used for updating. */
    private HashSet connectedSynapses = new HashSet();

    /** Id reference to model neuron; used in persistence. */
    private String id;

    /** Number text inside neuron. */
    private PText activationText;

    /** Text corresponding to neuron's (optional) label. */
    private PText labelText = new PText("...");

    /** Text corresponding to neuron's update priority. */
    private PText priorityText = new PText("...");

    /** Background for label text, so that background objects don't show up. */
    private PNode labelBackground;

    /** Whether the node is currently moving or not. */
    private boolean isMoving = false;

    /** Conveinence reference to parent action manager. */
    private NetworkActionManager actionManager;

    /** Neuron Font. */
    public static final Font NEURON_FONT = new Font("Arial", Font.PLAIN, 11);

    /** Priority Font. */
    public static final Font PRIORITY_FONT = new Font("Courier", Font.PLAIN, 9);

    // TODO: These should be replaced with actual scaling of the text object.

    /** Neuron font bold. */
    public static final Font NEURON_FONT_BOLD = new Font("Arial", Font.BOLD, 11);

    /** Neuron font small. */
    public static final Font NEURON_FONT_SMALL = new Font("Arial", Font.PLAIN,
            9);

    /** Neuron font very small. */
    public static final Font NEURON_FONT_VERYSMALL = new Font("Arial",
            Font.PLAIN, 7);

    /** Color of "active" neurons, with positive values. */
    private static float hotColor = Color.RGBtoHSB(255, 0, 0, null)[0];

    /** Color of "inhibited" neurons, with negative values. */
    private static float coolColor = Color.RGBtoHSB(0, 0, 255, null)[0];

    /** Color of "spiking" synapse. */
    private static Color spikingColor = Color.yellow;

    /**
     * Create a new neuron node.
     *
     * @param net Reference to NetworkPanel
     * @param neuron reference to model neuron
     */
    public NeuronNode(final NetworkPanel net, final Neuron neuron) {
        super(net);
        this.neuron = neuron;
        this.centerFullBoundsOnPoint(neuron.getX(), neuron.getY());
        init();
    }

    /**
     * Initialize the NeuronNode.
     */
    private void init() {
        circle = PPath.createEllipse(0 - DIAMETER / 2, 0 - DIAMETER / 2,
                DIAMETER, DIAMETER);

        addChild(circle);

        // Handle input and output arrows
        // outArrow = createOutArrow();
        // inArrow = createInArrow();
        // inLabel = createInputLabel();
        // outLabel = createOutputLabel();
        // addChild(outArrow);
        // addChild(inArrow);
        // addChild(inLabel);
        // addChild(outLabel);
        // updateOutArrow();
        // updateInArrow();
        // updateInLabel();
        // updateOutLabel();

        setPriorityView(getNetworkPanel().getPrioritiesVisible());

        activationText = new PText(String.valueOf((int) Math.round(neuron
                .getActivation())));
        activationText.setFont(NEURON_FONT);
        setActivationTextPosition();
        addChild(activationText);

        // TODO: create a white background for the activation text
        labelBackground = new PNode();
        labelBackground.setPaint(this.getNetworkPanel().getBackground());
        labelBackground.setBounds(labelText.getBounds());
        labelBackground.addChild(labelText);
        addChild(labelBackground);

        resetColors();
        update();

        setPickable(true);
        //setChildrenPickable(false);

        addPropertyChangeListener(PROPERTY_FULL_BOUNDS, this);

        // The main circle is what users select
        PBounds bounds = circle.getBounds();
        setBounds(bounds);

        updateTextLabel();

        actionManager = getNetworkPanel().getActionManager();
    }

    /**
     * Toggles the visibility of the priority view text label.
     *
     * @param visibility whether the priority text label should be visible
     */
    public void setPriorityView(boolean visibility) {
        if (visibility == true) {
            priorityText = new PText("" + neuron.getUpdatePriority());
            priorityText.setFont(PRIORITY_FONT);
            setPriorityTextPosition();
            addChild(priorityText);
        } else {
            if (priorityText != null) {
                removeChild(priorityText);
            }
        }
    }

    /**
     * Set position of priority label.
     */
    private void setPriorityTextPosition() {
        if (priorityText == null) {
            return;
        }
        priorityText.setOffset(getX() + DIAMETER + 2, getY() + DIAMETER - 4);
    }

    /** @see ScreenElement */
    public boolean isSelectable() {
        return true;
    }

    /** @see ScreenElement */
    public boolean showSelectionHandle() {
        return true;
    }

    /** @see ScreenElement */
    public boolean isDraggable() {
        return true;
    }

    /** @see ScreenElement */
    protected boolean hasToolTipText() {
        return true;
    }

    /** @see ScreenElement */
    protected String getToolTipText() {
        String ret = new String();
        ret += neuron.getToolTipText();
        // ret += getCouplingText();
        return ret;
    }

    /**
     * Returns information about couplings.
     *
     * @return coupling information.
     */
    private String getCouplingText() {
        String ret = new String("");
        // if (neuron.isInput()) {
        // ret += " \n Sensory Coupling  ";
        // if (neuron.getSensoryCoupling().getAgent() == null) {
        // ret += " ** unattached ** ";
        // }
        // ret += "\n   World: " + neuron.getSensoryCoupling().getWorldName() +
        // " ";
        // ret += "\n   Agent: " + neuron.getSensoryCoupling().getAgentName() +
        // " ";
        // ret += "\n   Sensor: " + neuron.getSensoryCoupling().getShortLabel()
        // + " ";
        // }
        // if (neuron.isOutput()) {
        // ret += " \n Motor Coupling ";
        // if (neuron.getMotorCoupling().getAgent() == null) {
        // ret += " ** unattaached ** ";
        // }
        // ret += "\n   World: " + neuron.getMotorCoupling().getWorldName() +
        // " ";
        // ret += "\n   Agent: " + neuron.getMotorCoupling().getAgentName() +
        // " ";
        // ret += "\n   Command: " + neuron.getMotorCoupling().getShortLabel() +
        // " ";
        // }
        return ret;
    }

    /** @see ScreenElement */
    protected boolean hasContextMenu() {
        return true;
    }

    /**
     * Return the center of this node (the circle) in global coordinates.
     *
     * @return the center point of this node.
     */
    public Point2D getCenter() {
        return circle.getGlobalBounds().getCenter2D();
    }

    /** @see ScreenElement */
    protected JPopupMenu getContextMenu() {

        JPopupMenu contextMenu = new JPopupMenu();
        contextMenu.add(new CutAction(getNetworkPanel()));
        contextMenu.add(new CopyAction(getNetworkPanel()));
        contextMenu.add(new PasteAction(getNetworkPanel()));
        contextMenu.add(new DeleteAction(getNetworkPanel()));
        contextMenu.addSeparator();
        contextMenu.add(actionManager.getClearSourceNeuronsAction());
        contextMenu.add(actionManager.getSetSourceNeuronsAction());
        contextMenu.add(actionManager.getConnectionMenu());
        contextMenu.addSeparator();
        contextMenu.add(actionManager.getLayoutMenu());
        contextMenu.add(actionManager.getGroupMenu());
        contextMenu.addSeparator();
        // Add align and space menus if objects are selected
        if (getNetworkPanel().getSelectedNeurons().size() > 1) {
            contextMenu.add(this.getNetworkPanel().createAlignMenu());
            contextMenu.add(this.getNetworkPanel().createSpacingMenu());
            contextMenu.addSeparator();
        }
        contextMenu.add(new SetNeuronPropertiesAction(getNetworkPanel()));
        contextMenu.addSeparator();
        JMenu nodeSelectionMenu = new JMenu("Select");
        nodeSelectionMenu.add(actionManager
                .getSelectIncomingWeightsAction());
        nodeSelectionMenu.add(actionManager
                .getSelectOutgoingWeightsAction());
        contextMenu.add(nodeSelectionMenu);
        contextMenu.addSeparator();
        contextMenu.add(actionManager.getTestInputAction());
        contextMenu.add(actionManager.getShowWeightMatrixAction());
        return contextMenu;
    }

    /** @see ScreenElement */
    protected boolean hasPropertyDialog() {
        return true;
    }

    /** @see ScreenElement */
    protected JDialog getPropertyDialog() {
        NeuronDialog dialog = new NeuronDialog(this.getNetworkPanel()
                .getSelectedNeurons());
        return dialog;
    }

    /**
     * Update the neuron view based on the model neuron.
     */
    public void update() {
        updateColor();
        updateText();
    }

    /**
     * Update the text label.
     */
    public void updateTextLabel() {
        // Set label text
        if ((!neuron.getLabel().equalsIgnoreCase(""))
                || (!neuron.getLabel().equalsIgnoreCase(
                        NeuronDialog.NULL_STRING))) {
            labelText.setFont(NEURON_FONT);
            labelText.setText("" + neuron.getLabel());
            labelText.setOffset(circle.getX() - labelText.getWidth() / 2
                    + DIAMETER / 2, circle.getY() - DIAMETER / 2 - 1);
            labelBackground.setBounds(labelText.getFullBounds());

            // update bounds to include text
            PBounds bounds = circle.getBounds();
            bounds.add(labelText.localToParent(labelText.getBounds()));
            setBounds(bounds);

            // TODO: Make background bigger than label text
        }
    }

    /**
     * Sets the color of this neuron based on its activation level.
     */
    private void updateColor() {
        double activation = neuron.getActivation();

        // Force to blank if 0 (or close to it)
        if ((activation > -.1) && (activation < .1)) {
            circle.setPaint(Color.white);
        } else if (activation > 0) {

            float saturation = checkValid((float) Math.abs(activation
                    / neuron.getUpdateRule().getCeiling()));
            circle.setPaint(Color.getHSBColor(hotColor, saturation, 1));
        } else if (activation < 0) {

            float saturation = checkValid((float) Math.abs(activation
                    / neuron.getUpdateRule().getFloor()));
            circle.setPaint(Color.getHSBColor(coolColor, saturation, 1));
        }

        if (neuron.getUpdateRule() instanceof SpikingNeuronUpdateRule) {
            if (((SpikingNeuronUpdateRule) neuron.getUpdateRule()).hasSpiked()) {
               circle.setStrokePaint(spikingColor);
                // outArrow.setStrokePaint(NetworkGuiSettings.getSpikingColor());
            } else {
                circle.setStrokePaint(SynapseNode.getLineColor());
                // outArrow.setStrokePaint(NetworkGuiSettings.getLineColor());
            }
        }
    }

    /**
     * Check whether the specified saturation is valid or not.
     *
     * @param val the saturation value to check.
     * @return whether it is valid or not.
     */
    private float checkValid(final float val) {
        float tempval = val;

        if (val > 1) {
            tempval = 1;
        }

        if (val < 0) {
            tempval = 0;
        }

        return tempval;
    }

    /**
     * Creates an arrow which designates an on-screen neuron as an output node,
     * which sends signals to an external environment (the world object).
     *
     * @return an object representing the input arrow of a PNodeNeuron
     * @see org.simbrain.sim.world
     */
    private PPath createOutArrow() {
        PPath path = new PPath.Float();
        GeneralPath arrow = new GeneralPath();
        Point2D p = this.globalToLocal(this.getOffset());
        float cx = (float) p.getX() + DIAMETER / 2;
        float cy = (float) p.getY() + DIAMETER / 2;

        arrow.moveTo(cx, cy - DIAMETER / 2);
        arrow.lineTo(cx, cy - DIAMETER / 2 - ARROW_LINE);

        arrow.moveTo(cx, cy - DIAMETER / 2 - ARROW_LINE);
        arrow.lineTo(cx - DIAMETER / 4, cy - DIAMETER);

        arrow.moveTo(cx, cy - DIAMETER / 2 - ARROW_LINE);
        arrow.lineTo(cx + DIAMETER / 4, cy - DIAMETER);

        path.append(arrow, true);
        return path;
    }

    /**
     * Create the input label.
     *
     * @return the input label.
     */
    private PText createInputLabel() {
        PText ret = new PText();
        ret.setFont(IN_OUT_FONT);
        // ret.setPaint(getNetworkPanel().getLineColor());
        ret.translate(this.getX(), this.getY() + DIAMETER / 2 + ARROW_LINE + 15);
        return ret;
    }

    /**
     * Create the output label.
     *
     * @return the output label.
     */
    private PText createOutputLabel() {
        PText ret = new PText();
        ret.setFont(IN_OUT_FONT);
        // ret.setPaint(getNetworkPanel().getLineColor());
        ret.translate(this.getX(), this.getY() - DIAMETER / 2 - ARROW_LINE - 5);
        return ret;
    }

    /**
     * Creates an arrow which designates an on-screen neuron as an input node,
     * which receives signals from an external environment (the world object).
     *
     * @return an object representing the input arrow of a PNodeNeuron
     * @see org.simbrain.sim.world
     */
    private PPath createInArrow() {
        PPath path = new PPath.Float();
        GeneralPath arrow = new GeneralPath();
        Point2D p = this.globalToLocal(this.getOffset());
        float cx = (float) p.getX() + DIAMETER / 2;
        float cy = (float) p.getY() + DIAMETER / 2;
        float top = cy + DIAMETER / 2 + 1;

        arrow.moveTo(cx, top);
        arrow.lineTo(cx, top + ARROW_LINE);

        arrow.moveTo(cx, top);
        arrow.lineTo(cx - DIAMETER / 4, cy + DIAMETER / 2 + DIAMETER / 4);

        arrow.moveTo(cx, top);
        arrow.lineTo(cx + DIAMETER / 4, top + DIAMETER / 4);

        path.append(arrow, true);

        // path.addInputEventListener(new ToolTipTextUpdater() {
        //
        // /** @see ToolTipTextUpdater */
        // protected String getToolTipText() {
        // return getCouplingText();
        // }
        // });
        return path;
    }

    /**
     * Determine what color and and font to use for this neuron based in its
     * activation level.
     */
    private void updateText() {
        double act = neuron.getActivation();
        activationText.setScale(1);
        setActivationTextPosition();

        priorityText.setScale(1);
        setPriorityTextPosition();
        priorityText.setText("" + neuron.getUpdatePriority()); // todo: respond
                                                               // to listener

        if ((act > 0) && (neuron.getActivation() < 1)) { // Between 0 and 1
            // text.setPaint(Color.white);
            activationText.setFont(NEURON_FONT_BOLD);
            String text = Utils.round(act, 1);
            if (text.startsWith("0.")) {
                text = text.replaceAll("0.", ".");
                if (text.equals(".0")) {
                    text = "0";
                }
            } else {
                text = text.replaceAll(".0$", "");
            }
            activationText.setText(text);
        } else if ((act > -1) && (act < 0)) { // Between -1 and 0
            // text.setPaint(Color.white);
            activationText.setFont(NEURON_FONT_BOLD);
            activationText.setText(Utils.round(act, 1).replaceAll("^-0*", "-")
                    .replaceAll(".0$", ""));
        } else {
            // greater than 1 or less than -1
            activationText.setFont(NEURON_FONT_BOLD);
            if (Math.abs(act) < 10) {
                activationText.scale(.9);
            } else if (Math.abs(act) < 100) {
                activationText.scale(.8);
                activationText.translate(1, 1);
            } else {
                activationText.scale(.7);
                activationText.translate(-1, 2);
            }
            activationText.setText(String.valueOf((int) Math.round(act)));
        }
    }

    /**
     * Set basic position of text in the PNode, which is then adjusted depending
     * on the size of the text.
     */
    private void setActivationTextPosition() {
        if (activationText != null) {
            activationText.setOffset(circle.getX() + DIAMETER / 4 + 2,
                    circle.getY() + DIAMETER / 4 + 1);
        }
    }

    // /**
    // * Updates graphics depending on whether this is an input node or not.
    // */
    // public void updateInArrow() {
    // if (neuron.isInput()) {
    // inArrow.setVisible(true);
    // } else {
    // inArrow.setVisible(false);
    // }
    // }
    //
    // /**
    // * Updates graphics depending on whether this is an output node or not.
    // */
    // public void updateOutArrow() {
    // if (neuron.isOutput()) {
    // outArrow.setVisible(true);
    // } else {
    // outArrow.setVisible(false);
    // }
    // }
    //
    // /**
    // * Update the label showing sensory coupling information.
    // */
    // public void updateInLabel() {
    // if (getNetworkPanel().getInOutMode()) {
    // if (getNeuron().isInput()) {
    // // inLabel.setText(getNeuron().getSensoryCoupling().getShortLabel());
    // inLabel.setVisible(true);
    // } else {
    // inLabel.setVisible(false);
    // }
    // } else {
    // inLabel.setVisible(false);
    // }
    // }
    //
    // /**
    // * Update the label showing sensory coupling information.
    // */
    // public void updateOutLabel() {
    // if (getNetworkPanel().getInOutMode()) {
    // if (getNeuron().isOutput()) {
    // // outLabel.setText(getNeuron().getMotorCoupling().getShortLabel());
    // outLabel.setVisible(true);
    // } else {
    // outLabel.setVisible(false);
    // }
    // } else {
    // outLabel.setVisible(false);
    // }
    // }

    /**
     * Returns String representation of this NeuronNode.
     *
     * @return String representation of this node.
     */
    public String toString() {
        String ret = new String();
        ret += "NeuronNode: (" + this.getGlobalFullBounds().x + ")("
                + getGlobalFullBounds().y + ")\n";
        return ret;
    }

    /**
     * Return the neuron for this neuron node.
     *
     * @return the neuron for this neuron node
     */
    public Neuron getNeuron() {
        return neuron;
    }

    /**
     * Set the neuron for this neuron node to <code>neuron</code>.
     * <p>
     * This is a bound property.
     * </p>
     *
     * @param neuron neuron for this neuron node
     */
    public void setNeuron(final Neuron neuron) {

        Neuron oldNeuron = this.neuron;
        this.neuron = neuron;
        firePropertyChange(-1, "neuron", oldNeuron, neuron);
    }

    /** @see ScreenElement */
    protected JPopupMenu createContextMenu() {
        return getContextMenu();
    }

    /** @see PropertyChangeListener */
    public void propertyChange(final PropertyChangeEvent event) {
        updateSynapseNodePositions();
    }

    /**
     * Update the position of the model neuron based on the global coordinates
     * of this pnode.
     */
    public void pushViewPositionToModel() {
        // System.out.println("model neuron updated");
        Point2D p = this.getGlobalTranslation();
        getNeuron().setX(p.getX());
        getNeuron().setY(p.getY());
    }

    /**
     * Updates the position of the view neuron based on the position of the
     * model neuron.
     */
    public void pullViewPositionFromModel() {
        // System.out.println("view neuron updated");
        Point2D p = new Point2D.Double(getNeuron().getX(), getNeuron().getY());
        this.setGlobalTranslation(p);
    }

    /**
     * @return Returns the DIAMETER.
     */
    public static int getDIAMETER() {
        return DIAMETER;
    }

    /**
     * @return Connected synapses.
     */
    public Set getConnectedSynapses() {
        // TODO:
        // may want to make this set unmodifiable
        return connectedSynapses;
    }

    /**
     * Update connected synapse node positions.
     */
    public void updateSynapseNodePositions() {

        for (Iterator i = connectedSynapses.iterator(); i.hasNext();) {
            SynapseNode synapseNode = (SynapseNode) i.next();
            synapseNode.updatePosition();
        }
    }

    /**
     * Synapse node position updater, called in response to changes in this
     * neuron node's fullBounds property.
     */
    private class SynapseNodePositionUpdater implements PropertyChangeListener {

        /** @see PropertyChangeListener */
        public void propertyChange(final PropertyChangeEvent event) {
            updateSynapseNodePositions();
        }
    }

    /**
     * @param id The id to set.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return Returns the xpos.
     */
    public double getXpos() {
        return this.getGlobalBounds().getX();
    }

    /**
     * @param xpos The xpos to set.
     */
    public void setXpos(final double xpos) {
        Point2D p = new Point2D.Double(xpos, getYpos());
        globalToLocal(p);
        this.setBounds(p.getX(), p.getY(), this.getWidth(), this.getHeight());
    }

    /**
     * @return Returns the ypos.
     */
    public double getYpos() {
        return this.getGlobalBounds().getY();
    }

    /**
     * @param ypos The ypos to set.
     */
    public void setYpos(final double ypos) {
        Point2D p = new Point2D.Double(getXpos(), ypos);
        globalToLocal(p);
        this.setBounds(p.getX(), p.getY(), this.getWidth(), this.getHeight());
    }

    /**
     * Change the color of input and output nodes to reflect whether they are
     * 'attached' to an agent in a world.
     */
    public void updateAttachmentStatus() {
        // if (neuron.getSensoryCoupling() != null) {
        // if (neuron.getSensoryCoupling().isAttached()) {
        // inArrow.setStrokePaint(getNetworkPanel().getLineColor());
        // } else {
        // inArrow.setStrokePaint(Color.GRAY);
        // }
        // }
        //
        // if (neuron.getMotorCoupling() != null) {
        // if (neuron.getMotorCoupling().isAttached()) {
        // outArrow.setStrokePaint(getNetworkPanel().getLineColor());
        // } else {
        // outArrow.setStrokePaint(Color.GRAY);
        // }
        // }
    }

    /** @see ScreenElement */
    public void resetColors() {
        circle.setStrokePaint(SynapseNode.getLineColor());
        labelBackground.setPaint(NetworkPanel.getBackgroundColor());
        // inArrow.setStrokePaint(NetworkGuiSettings.getLineColor());
        // outArrow.setStrokePaint(NetworkGuiSettings.getLineColor());
        updateColor();
    }

    /**
     * @return Returns the isMoving.
     */
    public boolean isMoving() {
        return isMoving;
    }

    /**
     * @param isMoving The isMoving to set.
     */
    public void setMoving(final boolean isMoving) {
        this.isMoving = isMoving;
    }

    /** @see ScreenElement. */
    public void setGrouped(final boolean isGrouped) {
        super.setGrouped(isGrouped);
        for (Iterator i = connectedSynapses.iterator(); i.hasNext();) {
            SynapseNode synapseNode = (SynapseNode) i.next();
            synapseNode.setGrouped(isGrouped);
        }
    }

    /**
     * @return the hotColor
     */
    public static float getHotColor() {
        return hotColor;
    }

    /**
     * @param hotColor the hotColor to set
     */
    public static void setHotColor(float hotColor) {
        NeuronNode.hotColor = hotColor;
    }

    /**
     * @return the coolColor
     */
    public static float getCoolColor() {
        return coolColor;
    }

    /**
     * @param coolColor the coolColor to set
     */
    public static void setCoolColor(float coolColor) {
        NeuronNode.coolColor = coolColor;
    }

    /**
     * @return the spikingColor
     */
    public static Color getSpikingColor() {
        return spikingColor;
    }

    /**
     * @param spikingColor the spikingColor to set
     */
    public static void setSpikingColor(Color spikingColor) {
        NeuronNode.spikingColor = spikingColor;
    }
}
