
package org.simbrain.network.actions;

import org.simbrain.network.NetworkPanel;
import org.simnet.coupling.InteractionMode;

import org.simbrain.resource.ResourceManager;

/**
 * Network to world interaction mode action.
 */
public final class NetworkToWorldInteractionModeAction
    extends InteractionModeAction {

    /**
     * Create a new network to world interaction mode action.
     *
     * @param networkPanel network panel, must not be null
     */
    public NetworkToWorldInteractionModeAction(final NetworkPanel networkPanel) {
        super("Network to world", networkPanel, InteractionMode.NETWORK_TO_WORLD);

        putValue(SMALL_ICON, ResourceManager.getImageIcon("WorldToNet.gif"));
        putValue(SHORT_DESCRIPTION, "World is sending stimuli to the network");
    }
}