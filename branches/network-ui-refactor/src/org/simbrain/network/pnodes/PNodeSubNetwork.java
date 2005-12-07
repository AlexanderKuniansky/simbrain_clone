/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.network.pnodes;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;

import org.simbrain.network.NetworkPanel;
import org.simbrain.network.ScreenElement;
import org.simnet.interfaces.ComplexNetwork;
import org.simnet.interfaces.Network;
import org.simnet.interfaces.Neuron;
import org.simnet.interfaces.Synapse;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;


/**
 * <b>PNodeSubNetwork</b> represents a container for a subnetwork.
 */
public class PNodeSubNetwork extends PNode implements ScreenElement {
    private static final int INDENT = 5;
    private static final int Y_ADJUST = 16;
    private static final int HEIGHT_ADJUST = 20;
    private static Color subnetColor = Color.GRAY;
    public static final Font LABEL_FONT = new Font("Arial", Font.PLAIN, 8);
    private Network subnet;
    private NetworkPanel parentPanel;
    PBounds cachedChildBounds = new PBounds();
    PBounds comparisonBounds = new PBounds();
    private PText labelNode;
    private String label = "subnetwork"; // TODO: need a way to pass this in

    public PNodeSubNetwork() {
        super();
    }

    public PNodeSubNetwork(final Network subnet, final NetworkPanel parentpanel) {
        super();
        this.subnet = subnet;
        parentPanel = parentpanel;
        init();
    }

    public void init() {
        /*
         * Would have liked to add this as a child of this node.  However,
         * doing so affects the value returned by the getUnionOfChildrenBounds()
         * call in the paint method.  Would like to have not included the
         * labelNode as part of that calculation.  So, for now, adding the labelNode
         * to the parent layer instead.
         */
        labelNode = new PText(label);
        labelNode.setFont(LABEL_FONT);
        parentPanel.getLayer().addChild(labelNode);
    }

    public static PNodeSubNetwork getDuplicate(final PNodeSubNetwork toCopy, final NetworkPanel np) {
        PNodeSubNetwork ret = new PNodeSubNetwork(toCopy.getSubnet(), np);

        return ret;
    }

    /**
     * Change the default paint to fill an expanded bounding box based on its children's bounds
     */
    public void paint(final PPaintContext ppc) {
        if (parentPanel.outlineSubnetwork() == false) {
            /*
             * Do not paint boundary if option is turned off from menu.
             */
            labelNode.setVisible(false);
        } else {
            labelNode.setVisible(true);

            Graphics2D g2 = ppc.getGraphics();
            g2.setPaint(subnetColor);

            PBounds bounds = getUnionOfChildrenBounds(null);
            bounds.setRect(
                           bounds.getX() - INDENT, bounds.getY() - INDENT - Y_ADJUST, bounds.getWidth() + (2 * INDENT),
                           bounds.getHeight() + HEIGHT_ADJUST + (2 * INDENT));

            g2.draw(bounds);

            /*
             * Draw rectangle for label tab area
             */
            PBounds labelBounds = new PBounds();
            labelBounds.setRect(bounds.getX(), bounds.getY(), bounds.getWidth(), 16);
            g2.draw(labelBounds);

            /*
             * Move labelNode into the label tab area.
             */
            labelNode.setBounds(bounds.getX() + 3, bounds.getY() + 3, bounds.getWidth(), 16);
        }
    }

    /**
     * Change the full bounds computation to take into account that we are expanding the children's bounds Do this
     * instead of overriding getBoundsReference() since the node is not volatile
     */
    public PBounds computeFullBounds(final PBounds dstBounds) {
        PBounds result = getUnionOfChildrenBounds(dstBounds);

        cachedChildBounds.setRect(result);
        result.setRect(
                       result.getX() - INDENT, result.getY() - INDENT - Y_ADJUST, result.getWidth() + (2 * INDENT),
                       result.getHeight() + HEIGHT_ADJUST + (2 * INDENT));
        localToParent(result);

        return result;
    }

    /**
     * This is a crucial step.  We have to override this method to invalidate the paint each time the bounds are
     * changed so we repaint the correct region
     */
    public boolean validateFullBounds() {
        comparisonBounds = getUnionOfChildrenBounds(comparisonBounds);

        if (!cachedChildBounds.equals(comparisonBounds)) {
            setPaintInvalid(true);
        }

        return super.validateFullBounds();
    }

    public void addToNetwork(final NetworkPanel np) {
        return;
    }

    /**
     * Initialize a new network
     */
    public void initSubnet(final String layout) {
        int numRows = (int) Math.sqrt(subnet.getNeuronCount());
        int increment = 45;
        double x, y;
        double base_x = parentPanel.getLastClicked().getX();
        double base_y = parentPanel.getLastClicked().getY();

        if (layout.equalsIgnoreCase("Line")) {
            for (int i = 0; i < subnet.getNeuronCount(); i++) {
                PNodeNeuron theNode = new PNodeNeuron(base_x + (i * increment), base_y, subnet.getNeuron(i), parentPanel);
                parentPanel.addNode(theNode, false);
                addChild(theNode);
            }
        } else if (layout.equalsIgnoreCase("Grid")) {
 
            for (int i = 0; i < subnet.getNeuronCount(); i++) {
                x = base_x + ((i % numRows) * increment);
                y = base_y +  ((i / numRows) * increment);
                PNodeNeuron theNode = new PNodeNeuron(x, y, subnet.getNeuron(i), parentPanel);
                parentPanel.addNode(theNode, false);
                addChild(theNode);
            }
        } else if (layout.equalsIgnoreCase("Layers")) {
            if (!(subnet instanceof ComplexNetwork)) {
                return;
            }

            ComplexNetwork cn = (ComplexNetwork) subnet;
            y = base_y + cn.getNetworkList().size() * increment;

            for (int i = 0; i < cn.getNetworkList().size(); i++) {
                for (int j = 0; j < cn.getNetwork(i).getNeuronCount(); j++) {
                    int bpnetinc = ((cn.getNetwork(0).getNeuronCount() - cn.getNetwork(i).getNeuronCount()) * increment) / 2;
                    PNodeNeuron theNode = new PNodeNeuron(base_x + bpnetinc + (j * increment), y - (i * increment),
                                                          cn.getNetwork(i).getNeuron(j), parentPanel);
                    parentPanel.addNode(theNode, false);
                    addChild(theNode);
                }
            }
        } else if (layout.equalsIgnoreCase("Elman")) {
            if (!(subnet instanceof ComplexNetwork)) {
                return;
            }

            ComplexNetwork cn = (ComplexNetwork) subnet;
            int intervalInputs = ((cn.getNetwork(0).getNeuronCount() - cn.getNetwork(0).getNeuronCount()) * increment) / 2;
            int beginCopyX = (int)(base_x + ((cn.getNetwork(0).getNeuronCount() + 2) * intervalInputs));
            
            for (int i = 0; i < cn.getNetworkList().size(); i++) {
                // The copied layer
                if (i == 3) {
                    for (int j = 0; j < cn.getNetwork(i).getNeuronCount(); j++) {
                        int bpnetinc = ((cn.getNetwork(0).getNeuronCount() - cn.getNetwork(i).getNeuronCount()) * increment) / 2;
                        PNodeNeuron theNode = new PNodeNeuron(
                                beginCopyX + (cn.getNetwork(i - 1).getNeuronCount() * increment)
                                 + intervalInputs + (j * increment), base_y,
                                cn.getNetwork(i).getNeuron(j), parentPanel);
                        parentPanel.addNode(theNode, false);
                        addChild(theNode);
                    }                    
                    continue;
                }
                for (int j = 0; j < cn.getNetwork(i).getNeuronCount(); j++) {
                    int bpnetinc = ((cn.getNetwork(0).getNeuronCount() - cn.getNetwork(i).getNeuronCount()) * increment) / 2;
                    PNodeNeuron theNode = new PNodeNeuron(base_x + bpnetinc + (j * increment), base_y - (i * increment),
                                                          cn.getNetwork(i).getNeuron(j), parentPanel);
                    parentPanel.addNode(theNode, false);
                    addChild(theNode);
                }
            }
        }

        for (int i = 0; i < subnet.getWeightCount(); i++) {
            Synapse s = subnet.getWeight(i);
            PNodeWeight theNode = new PNodeWeight(
                                                  parentPanel.findPNodeNeuron(s.getSource()),
                                                  parentPanel.findPNodeNeuron(s.getTarget()), s);

            parentPanel.addNode(theNode, false);
            addChild(theNode);
        }
    }

    /**
     * Selects all the child nodes of this subnetwork.
     */
    public void selectAllChildNodes() {
        ArrayList list = new ArrayList();
        Iterator it = getChildrenIterator();

        while (it.hasNext()) {
            list.add(it.next());
        }

        parentPanel.setSelection(list);
    }

    /**
     * Get all the pnodeneurons in this net
     */
    public ArrayList getPNodeNeurons() {
        ArrayList list = new ArrayList();
        Iterator it = getChildrenIterator();

        while (it.hasNext()) {
            Object o = it.next();

            if (o instanceof PNodeNeuron) {
                list.add(o);
            }
        }

        return list;
    }

    public void drawBoundary() {
        return;
    }

    public boolean isSelectable() {
        return true;
    }

    /**
     * @param np Reference to parent NetworkPanel
     */
    public void initCastor(final NetworkPanel np) {
        parentPanel = np;

        if (subnet instanceof ComplexNetwork) {
            ComplexNetwork cnet = (ComplexNetwork) subnet;
            for (int i = 0; i < cnet.getFlatNeuronList().size(); i++) {
                PNodeNeuron pn = parentPanel.findPNodeNeuron((Neuron) cnet.getFlatNeuronList().get(i));
                addChild(pn);
            }
        } else {
            for (int i = 0; i < subnet.getNeuronCount(); i++) {
                PNodeNeuron pn = parentPanel.findPNodeNeuron(subnet.getNeuron(i));
                addChild(pn);
            }
        }
        init();
    }

    public void delete() {
        labelNode.removeFromParent();
    }

    public void randomize() {
        return;
    }

    public void increment() {
        return;
    }

    public void decrement() {
        return;
    }

    public void nudge(final int offsetX, final int offsetY, final double nudgeAmount) {
        offset(offsetX * nudgeAmount, offsetY * nudgeAmount);
    }

    public void renderNode() {
        return;
    }

    public void resetLineColors() {
        return;
    }

    public Network getSubnet() {
        return subnet;
    }

    public void setSubnet(final Network subnet) {
        this.subnet = subnet;
    }
}
