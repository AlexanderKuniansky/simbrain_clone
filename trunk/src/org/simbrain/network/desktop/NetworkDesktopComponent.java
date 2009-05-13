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
package org.simbrain.network.desktop;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.simbrain.network.NetworkComponent;
import org.simbrain.network.gui.NetworkPanel;
import org.simbrain.network.gui.actions.ShowNetworkPreferencesAction;
import org.simbrain.workspace.gui.ComponentMenu;
import org.simbrain.workspace.gui.GenericFrame;
import org.simbrain.workspace.gui.GuiComponent;


/**
 * Network frame.
 */
public final class NetworkDesktopComponent extends GuiComponent<NetworkComponent> {

    private static final long serialVersionUID = 1L;

    /** Network panel. */
    private final NetworkPanelDesktop networkPanel;
    
    /**
     * Create a new network frame.
     */
    public NetworkDesktopComponent(final GenericFrame frame, final NetworkComponent component) {
        super(frame, component);
        this.setPreferredSize(new Dimension(450,400));

        networkPanel = new NetworkPanelDesktop(this, component.getRootNetwork());

         //TODO: Wire up preferences
        // component.setCurrentDirectory(currentDirectory);
        // component.setCurrentFile(currentFile);
        
        // Place networkPanel in a buffer so that toolbars don't get in the way of canvas elements
        setLayout(new BorderLayout());

        // Put it all together
        add("Center", networkPanel);
        createAndAttachMenus();
    }
    
    JMenuBar menuBar = null;

    /**
     * Create and attach the menus for this network frame.
     */
    private void createAndAttachMenus() {

        menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(networkPanel.createEditMenu());
        menuBar.add(networkPanel.createInsertMenu());
        menuBar.add(networkPanel.createViewMenu());
        menuBar.add(new ComponentMenu("Couple", this.getWorkspaceComponent()
                .getWorkspace(), this.getWorkspaceComponent()));
        menuBar.add(networkPanel.createHelpMenu());
        getParentFrame().setJMenuBar(menuBar);
    }
    
    
    /**
     * Create and return a new File menu for this rootNetwork panel.
     *
     * @return a new File menu for this rootNetwork panel
     */
    JMenu createFileMenu() {

        JMenu fileMenu = new JMenu("File");

        fileMenu.add(new OpenNetworkAction(this));
        fileMenu.add(new SaveNetworkAction(this));
        fileMenu.add(new SaveAsNetworkAction(this));
        fileMenu.addSeparator();
        fileMenu.add(new ShowNetworkPreferencesAction(networkPanel));
        fileMenu.addSeparator();

        fileMenu.add(new CloseNetworkAction(this));

        return fileMenu;
    }
    
    @Override
    public void postAddInit() {
        if (this.getParentFrame().getJMenuBar() == null) {
            createAndAttachMenus();
        }
        
        //TODO: Below only needs to happen when opening; but currently it happens
        //      also when creating a new network
        networkPanel.clearPanel();
        if (networkPanel.getRootNetwork() != this.getWorkspaceComponent().getRootNetwork()) {
            networkPanel.setRootNetwork(this.getWorkspaceComponent()
                    .getRootNetwork().getRootNetwork());
        }
        networkPanel.getRootNetwork().getParent().addListener(networkPanel);
        networkPanel.syncToModel();
        networkPanel.initGui();
    }

    /**
     * Return the network panel for this network frame.
     *
     * @return the network panel for this network frame
     */
    public NetworkPanel getNetworkPanel() {
        return networkPanel;
    }

    @Override
    public void closing() {
        // TODO Auto-generated method stub
    }

}