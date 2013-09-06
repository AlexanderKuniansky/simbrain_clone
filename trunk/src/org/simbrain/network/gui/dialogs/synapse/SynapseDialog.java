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
package org.simbrain.network.gui.dialogs.synapse;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.simbrain.network.core.Neuron;
import org.simbrain.network.core.SpikingNeuronUpdateRule;
import org.simbrain.network.core.Synapse;
import org.simbrain.network.core.SynapseUpdateRule;
import org.simbrain.network.gui.NetworkUtils;
import org.simbrain.util.ClassDescriptionPair;
import org.simbrain.util.LabelledItemPanel;
import org.simbrain.util.ShowHelpAction;
import org.simbrain.util.StandardDialog;

/**
 * The <b>SynapseDialog</b> is initialized with a list of synapses. When the
 * dialog is closed the synapses are changed based on the state of the dialog.
 */
public class SynapseDialog extends StandardDialog implements ActionListener {

    /** Null string. */
    public static final String NULL_STRING = "...";

    /** Tabbed pane (only used if a spike responder pane is needed) */
    private JTabbedPane tabbedPane = new JTabbedPane();

    /** Main panel. */
    private Box mainPanel = Box.createVerticalBox();

    /** Spike response panel. */
    private SpikeResponsePanel spikeResponsePanel;

    /** Top panel. */
    private LabelledItemPanel topPanel = new LabelledItemPanel();

    /** Synapse panel. */
    private AbstractSynapsePanel synapsePanel = new ClampedSynapseRulePanel();

    /** Scrollpane for synapse panel. */
    private JScrollPane scrollPane;

    /** Id Label. */
    private JLabel idLabel = new JLabel();

    /** Strength field. */
    private JTextField tfStrength = new JTextField();

    /** Increment field. */
    private JTextField tfIncrement = new JTextField();

    /** Upper bound field. */
    private JTextField tfUpBound = new JTextField();

    /** Lower bound field. */
    private JTextField tfLowBound = new JTextField();

    /** Delay field. */
    private JTextField tfDelay = new JTextField();

    /** Upper label. */
    private JLabel upperLabel = new JLabel("Upper bound");

    /** Lower label. */
    private JLabel lowerLabel = new JLabel("Lower bound");

    /** Synapse type combo box. */
    private JComboBox cbSynapseType = new JComboBox(Synapse.getRuleList());

    /** The synapses being modified. */
    private List<Synapse> synapseList = new ArrayList<Synapse>();

    /** Weights have changed boolean. */
    private boolean weightsHaveChanged = false;

    /** Help Button. */
    private JButton helpButton = new JButton("Help");

    /** Show Help Action. */
    private ShowHelpAction helpAction;

    /**
     * Constructor for a list of SynapseNodes.
     *
     * @param synapseList list of synapses to modify
     */
    public SynapseDialog(final List<Synapse> synapseList) {
        this.synapseList = synapseList;
        init();
    }

    @Override
    protected void closeDialogOk() {
        super.closeDialogOk();
        commitChanges();
    }

    /**
     * Initializes the components on the panel.
     */
    private void init() {
        setTitle("Synapse Dialog");

        initSynapseType();
        fillFieldValues();
        updateHelp();

        helpButton.setAction(helpAction);
        this.addButton(helpButton);
        cbSynapseType.addActionListener(this);
        topPanel.addItem("Id:", idLabel);
        topPanel.addItem("Strength", tfStrength);
        topPanel.addItem("Increment", tfIncrement);

        String toolTipText = "<html>If text is grayed out, "
                + "this field is only used for graphics purposes <p> "
                + "(to determine what size this synapse should be).</html>";
        upperLabel.setToolTipText(toolTipText);
        lowerLabel.setToolTipText(toolTipText);
        topPanel.addItemLabel(upperLabel, tfUpBound);
        topPanel.addItemLabel(lowerLabel, tfLowBound);
        topPanel.addItem("Delay", tfDelay);
        topPanel.addItem("Learning rule", cbSynapseType);

        mainPanel.add(topPanel);
        initScrollPane(synapsePanel);
        mainPanel.add(scrollPane);

        // Add tab for setting spike responders, if needed
        ArrayList<Synapse> spikeRespondingSynapses = getSpikeRespondingSynapses();
        if (spikeRespondingSynapses.size() > 0) {
            spikeResponsePanel = new SpikeResponsePanel(
                    spikeRespondingSynapses, this);
            tabbedPane.addTab("Synaptic Efficacy", mainPanel);
            tabbedPane.addTab("Spike Response", spikeResponsePanel);
            setContentPane(tabbedPane);
            updateHelp();
        } else {
            setContentPane(mainPanel);
        }

    }

    /**
     * Retrieve those synapses which are spike responders.
     *
     * @return the list of synapses which are spike responders
     */
    private ArrayList<Synapse> getSpikeRespondingSynapses() {
        ArrayList<Synapse> ret = new ArrayList<Synapse>();

        for (Synapse synapse : synapseList) {
            Neuron source = synapse.getSource();
            if (source != null) {
                if ((source.getUpdateRule() instanceof SpikingNeuronUpdateRule)
                        && (source != null)) {
                    ret.add(synapse);
                }
            }
        }
        return ret;
    }

    /**
     * Initialize the main synapse panel based on the type of the selected
     * synapses.
     */
    public void initSynapseType() {
        if (!NetworkUtils.isConsistent(synapseList, Synapse.class, "getType")) {
            cbSynapseType.addItem(AbstractSynapsePanel.NULL_STRING);
            cbSynapseType.setSelectedIndex(Synapse.getRuleList().length);
            // Default to clamped synapse panel
            synapsePanel = new ClampedSynapseRulePanel();
        } else {
            setComboBox(synapseList.get(0).getLearningRule().getDescription());
            Class<?> synapseType = ((ClassDescriptionPair) cbSynapseType
                    .getSelectedItem()).getTheClass();
            synapsePanel = getSynapsePanel(synapseType);
            synapsePanel.setRuleList(getRuleList());
            synapsePanel.fillFieldValues();
        }
    }

    /**
     * Utility for setting the selected item of a combo box based on a synapse's
     * update rule description.
     */
    private void setComboBox(final String description) {
        for (int i = 0; i < cbSynapseType.getItemCount(); i++) {
            ClassDescriptionPair pair = (ClassDescriptionPair) cbSynapseType
                    .getItemAt(i);
            if (pair.getDescription().equalsIgnoreCase(description)) {
                cbSynapseType.setSelectedIndex(i);
                return;
            }
        }
    }

    /**
     * Returns synapse panel corresponding to the given update rule. Assumes the
     * panel class name = update rule class name + "Panel" E.g. "HebbianSynapse"
     * > "HebbianSynapsePanel".
     *
     * @param updateRuleClass the class to match
     * @return panel the matching panel
     */
    public static AbstractSynapsePanel getSynapsePanel(Class<?> updateRuleClass) {
        // The panel name to look for
        String panelClassName = "org.simbrain.network.gui.dialogs.synapse."
                + updateRuleClass.getSimpleName() + "Panel";

        try {
            return (AbstractSynapsePanel) Class.forName(panelClassName)
                    .newInstance();
        } catch (ClassNotFoundException e) {
            System.err.print("The class, \"" + panelClassName
                    + "\", was not found.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Return the neuron update rules associated with the selected neurons.
     *
     * @return the rule list.
     */
    private ArrayList<SynapseUpdateRule> getRuleList() {
        ArrayList<SynapseUpdateRule> ret = new ArrayList<SynapseUpdateRule>();
        for (Synapse synapse : synapseList) {
            ret.add(synapse.getLearningRule());
        }
        return ret;
    }

    /**
     * Change all the synapses from their current type to the new specified in
     * the dialog.
     */
    public void changeSynapseTypes() {
        Object selected = cbSynapseType.getSelectedItem();
        if (selected != NULL_STRING) {
            String name = ((ClassDescriptionPair) selected).getSimpleName();
            for (int i = 0; i < synapseList.size(); i++) {
                synapseList.get(i).setLearningRule(name);
            }
        }
    }

    /**
     * Respond to synapse type changes.
     *
     * @param e Action event
     */
    public void actionPerformed(final ActionEvent e) {

        weightsHaveChanged = true;
        updateHelp();
        Object selected = cbSynapseType.getSelectedItem();
        if (selected != NULL_STRING) {
            mainPanel.remove(scrollPane);
            synapsePanel = getSynapsePanel(((ClassDescriptionPair) selected)
                    .getTheClass());
            synapsePanel.fillDefaultValues();
            initScrollPane(synapsePanel);
            mainPanel.add(scrollPane);
            scrollPane.revalidate();
            pack();
            centerDialog();
        }

    }

    /**
     * Set the initial values of dialog components.
     */
    private void fillFieldValues() {
        Synapse synapseRef = synapseList.get(0);

        if (synapseList.size() == 1) {
            idLabel.setText(synapseRef.getId());
        } else {
            idLabel.setText(NULL_STRING);
        }
        tfStrength.setText(Double.toString(synapseRef.getStrength()));
        tfIncrement.setText(Double.toString(synapseRef.getIncrement()));
        tfLowBound.setText(Double.toString(synapseRef.getLowerBound()));
        tfUpBound.setText(Double.toString(synapseRef.getUpperBound()));
        tfDelay.setText(Integer.toString(synapseRef.getDelay()));

        synapsePanel.fillFieldValues();

        if (spikeResponsePanel != null) {
            spikeResponsePanel.fillFieldValues();
        }

        // Handle consistency of multiple selections
        if (!NetworkUtils.isConsistent(synapseList, Synapse.class,
                "getStrength")) {
            tfStrength.setText(NULL_STRING);
        }

        if (!NetworkUtils.isConsistent(synapseList, Synapse.class,
                "getIncrement")) {
            tfIncrement.setText(NULL_STRING);
        }

        if (!NetworkUtils.isConsistent(synapseList, Synapse.class,
                "getLowerBound")) {
            tfLowBound.setText(NULL_STRING);
        }

        if (!NetworkUtils.isConsistent(synapseList, Synapse.class,
                "getUpperBound")) {
            tfUpBound.setText(NULL_STRING);
        }

        if (!NetworkUtils.isConsistent(synapseList, Synapse.class, "getDelay")) {
            tfDelay.setText(NULL_STRING);
        }

    }

    /**
     * Called externally when the dialog is closed, to commit any changes made.
     */
    public void commitChanges() {
        for (int i = 0; i < synapseList.size(); i++) {
            Synapse synapseRef = synapseList.get(i);

            if (!tfStrength.getText().equals(NULL_STRING)) {
                synapseRef
                        .setStrength(Double.parseDouble(tfStrength.getText()));
            }

            if (!tfIncrement.getText().equals(NULL_STRING)) {
                synapseRef.setIncrement(Double.parseDouble(tfIncrement
                        .getText()));
            }

            if (!tfUpBound.getText().equals(NULL_STRING)) {
                synapseRef
                        .setUpperBound(Double.parseDouble(tfUpBound.getText()));
            }

            if (!tfLowBound.getText().equals(NULL_STRING)) {
                synapseRef.setLowerBound(Double.parseDouble(tfLowBound
                        .getText()));
            }

            if (!tfDelay.getText().equals(NULL_STRING)) {
                synapseRef.setDelay(Integer.parseInt(tfDelay.getText()));
            }
        }

        if (weightsHaveChanged) {
            changeSynapseTypes();
        }

        if (spikeResponsePanel != null) {
            spikeResponsePanel.commitChanges();
        }

        // Notify the network that changes have been made
        Synapse firstSynapse = synapseList.get(0);
        if (firstSynapse.getParentNetwork() != null) {
            firstSynapse.getParentNetwork().fireNetworkChanged();
        }

        // Now commit changes specific to the synapse type
        synapsePanel.setRuleList(getRuleList());
        synapsePanel.commitChanges();
    }

    /**
     * Set the help page based on the currently selected synapse type.
     */
    public void updateHelp() {
        if (cbSynapseType.getSelectedItem() == NULL_STRING) {
            helpAction = new ShowHelpAction("Pages/Network/synapse.html");
        } else if (spikeResponsePanel != null) {
            String spacelessString = spikeResponsePanel.getResponseFunction()
                    .replace(" ", "");
            spacelessString = spacelessString.substring(0, 1).toLowerCase()
                    .concat(spacelessString.substring(1));
            helpAction = new ShowHelpAction(
                    "Pages/Network/synapse/spikeresponders/" + spacelessString
                            + ".html");
        } else {
            String name = ((ClassDescriptionPair) cbSynapseType
                    .getSelectedItem()).getSimpleName().replaceAll("Rule", "");
            name = name.substring(0, 1).toLowerCase().concat(name.substring(1));
            helpAction = new ShowHelpAction("Pages/Network/synapse/" + name
                    + ".html");
        }
        helpButton.setAction(helpAction);
    }

    /**
     * @return the synapseList
     */
    public List<Synapse> getSynapseList() {
        return synapseList;
    }

    /**
     * @param synapseList the synapseList to set
     */
    public void setSynapseList(List<Synapse> synapseList) {
        this.synapseList = synapseList;
    }
    /**
     * Initialize the scroll panel: set its properties and re-init the scrollbar
     * policy. This is called every time a new neuron type is selected, so that
     * the panel can be laid out again.
     *
     * @param npanel
     */
    private void initScrollPane(JPanel panel) {
        scrollPane = new JScrollPane(panel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    }
}
