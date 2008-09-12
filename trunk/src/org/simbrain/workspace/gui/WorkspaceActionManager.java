/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2005-2006 Jeff Yoshimi <www.jeffyoshimi.net>
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
package org.simbrain.workspace.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

import org.simbrain.console.ConsoleComponent;
import org.simbrain.gauge.GaugeComponent;
import org.simbrain.resource.ResourceManager;
import org.simbrain.workspace.Workspace;
import org.simbrain.workspace.actions.ClearWorkspaceAction;
import org.simbrain.workspace.actions.GlobalRunAction;
import org.simbrain.workspace.actions.GlobalStopAction;
import org.simbrain.workspace.actions.GlobalUpdateAction;
import org.simbrain.workspace.actions.NewConsoleAction;
import org.simbrain.workspace.actions.NewDataWorldAction;
import org.simbrain.workspace.actions.NewGameWorld2dAction;
import org.simbrain.workspace.actions.NewGaugeAction;
import org.simbrain.workspace.actions.NewMidiWorldAction;
import org.simbrain.workspace.actions.NewNetworkAction;
import org.simbrain.workspace.actions.NewOdorWorldAction;
import org.simbrain.workspace.actions.NewOscWorldAction;
import org.simbrain.workspace.actions.NewPlotAction;
import org.simbrain.workspace.actions.NewTextWorldAction;
import org.simbrain.workspace.actions.NewThreeDeeWorldAction;
import org.simbrain.workspace.actions.NewVisionWorldAction;
import org.simbrain.workspace.actions.OpenCouplingListAction;
import org.simbrain.workspace.actions.OpenCouplingManagerAction;
import org.simbrain.workspace.actions.OpenDataWorldAction;
import org.simbrain.workspace.actions.OpenGaugeAction;
import org.simbrain.workspace.actions.OpenNetworkAction;
import org.simbrain.workspace.actions.OpenOdorWorldAction;
import org.simbrain.workspace.actions.OpenWorkspaceAction;
import org.simbrain.workspace.actions.OpenWorkspaceComponentListAction;
import org.simbrain.workspace.actions.QuitWorkspaceAction;
import org.simbrain.workspace.actions.SaveWorkspaceAction;
import org.simbrain.workspace.actions.SaveWorkspaceAsAction;
import org.simbrain.workspace.actions.WorkspaceAction;
import org.simbrain.workspace.actions.WorkspaceHelpAction;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.TargetError;
import bsh.util.JConsole;

/**
 * Workspace action manager.
 * 
 * <p>
 * This class contains references to all the actions for a Workspace.
 * </p>
 * 
 * <p>
 * These references are contained here instead of in Workspace simply to reduce
 * the amount of code in Workspace. Most but not all actions hold a reference to
 * the Workspace, passed in via their constructor.
 * </p>
 */
public class WorkspaceActionManager {

    /** New network action. */
    private final Action newNetworkAction;

    /** New odor world action. */
    private final Action newOdorWorldAction;

    /** New odor world action. */
    private final Action newThreeDeeWorldAction;

    /** New data world action. */
    private final Action newDataWorldAction;

    /** New game world 2d action. */
    private final Action newGameWorld2dAction;

    /** New MIDI world action. */
    private final Action newMidiWorldAction;

    /** New OSC world action. */
    private final Action newOscWorldAction;

    /** New text world action. */
    private final Action newTextWorldAction;

    /** New vision world action. */
    private final Action newVisionWorldAction;

    /** New gauge action. */
    private final Action newGaugeAction;

    /** New plot action. */
    private final Action newPlotAction;

    /** New console action. */
    private final Action newConsoleAction;

    /** Clear workspace action. */
    private final Action clearWorkspaceAction;

    /** Open data world action. */
    private final Action openDataWorldAction;

    /** Open gauge action. */
    private final Action openGaugeAction;

    /** Open network action. */
    private final Action openNetworkAction;

    /** Open odor world action. */
    private final Action openOdorWorldAction;

    /** Open workspace action. */
    private final Action openWorkspaceAction;

    /** Save workspace action. */
    private final Action saveWorkspaceAction;

    /** Save workspace as action. */
    private final Action saveWorkspaceAsAction;

    /** Workspace help action. */
    private final Action workspaceHelpAction;

    /** Quit workspace action. */
    private final Action quitWorkspaceAction;

    /** Global workspace update action. */
    private final Action globalUpdateAction;

    /** Opens the coupling manager. */
    private final Action openCouplingManagerAction;

    /** Global workspace run action. */
    private final Action globalRunAction;

    /** Global workspace stop action. */
    private final Action globalStopAction;

    /** Opens the coupling list. */
    private final Action openCouplingListAction;

    private final Action openWorkspaceComponentListAction;

    /** Location of script menu directory. */
    private final static String SCRIPT_MENU_DIRECTORY = "." + System.getProperty("file.separator") + "scriptmenu";
    

    /**
     * Create a new workspace action manager for the specified workspace.
     * 
     * @param workspace
     *            workspace, must not be null
     */
    public WorkspaceActionManager(SimbrainDesktop desktop) {
        Workspace workspace = desktop.getWorkspace();
        clearWorkspaceAction = new ClearWorkspaceAction(workspace);

        openDataWorldAction = new OpenDataWorldAction();
        openGaugeAction = new OpenGaugeAction();
        openNetworkAction = new OpenNetworkAction(workspace);
        openOdorWorldAction = new OpenOdorWorldAction(workspace);

        openWorkspaceAction = new OpenWorkspaceAction(desktop);
        saveWorkspaceAction = new SaveWorkspaceAction(desktop);
        saveWorkspaceAsAction = new SaveWorkspaceAsAction(desktop);

        newNetworkAction = new NewNetworkAction(workspace);
        newGaugeAction = new NewGaugeAction(workspace);
        newConsoleAction = new NewConsoleAction(workspace);

        newDataWorldAction = new NewDataWorldAction(workspace);
        newGameWorld2dAction = new NewGameWorld2dAction(workspace);
        newMidiWorldAction = new NewMidiWorldAction(workspace);
        newOdorWorldAction = new NewOdorWorldAction(workspace);
        newOscWorldAction = new NewOscWorldAction(workspace);
        newThreeDeeWorldAction = new NewThreeDeeWorldAction(workspace);
        newTextWorldAction = new NewTextWorldAction(workspace);
        newVisionWorldAction = new NewVisionWorldAction(workspace);
        newPlotAction = new NewPlotAction(workspace);

        workspaceHelpAction = new WorkspaceHelpAction();

        quitWorkspaceAction = new QuitWorkspaceAction(desktop);

        globalUpdateAction = new GlobalUpdateAction(workspace);
        globalRunAction = new GlobalRunAction(workspace);
        globalStopAction = new GlobalStopAction(workspace);

        openCouplingManagerAction = new OpenCouplingManagerAction(desktop);
        openCouplingListAction = new OpenCouplingListAction(desktop);
        openWorkspaceComponentListAction = new OpenWorkspaceComponentListAction(desktop);
    }

    /**
     * Return a list of network control actions.
     * 
     * @return a list of network control actions
     */
    public List<Action> getGlobalControlActions() {
        return Arrays
                .asList(new Action[] { globalRunAction, globalStopAction });
    }

    /**
     * @return Open and save workspace actions.
     */
    public List<Action> getOpenSaveWorkspaceActions() {
        return Arrays.asList(new Action[] { openWorkspaceAction,
                saveWorkspaceAction, saveWorkspaceAsAction });
    }

    /**
     * @return Open worlds actions.
     */
    public List<Action> getOpenWorldActions() {
        return Arrays.asList(new Action[] { openDataWorldAction,
                openOdorWorldAction });
    }

    /**
     * @return New worlds actions.
     */
    public List<Action> getNewWorldActions() {
        return Arrays.asList(new Action[] { newDataWorldAction,
                newGameWorld2dAction, newMidiWorldAction, newOdorWorldAction,
                newOscWorldAction, newThreeDeeWorldAction, newTextWorldAction,
                newVisionWorldAction });
    }

    /**
     * @return Simbrain gauge actions.
     */
    public List<Action> getGaugeActions() {
        return Arrays.asList(new Action[] { newGaugeAction, newPlotAction });
    }

    /**
     * Make a list of script actions by iterating through script menu directory.
     * 
     * @return script action
     */
    public List<Action> getScriptActions(Workspace workspace) {
        ArrayList<Action> list = new ArrayList<Action>();
        File dir = new File(SCRIPT_MENU_DIRECTORY);
        if (dir.isDirectory() == false) {
            return null; // Throw exception instead?
        }        
        // TODO: look for other endings and invoke relevant script types
        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".bsh")) {
                list.add(new ScriptAction(workspace, file.getName()));
            }
        }
        return list;
    }
    


    /**
     * Create an action based on the name of a script.
     */
    public final class ScriptAction extends WorkspaceAction {

        private String scriptName;

        private Workspace workspace;
        /**
         * Create a new add gauge action with the specified workspace.
         */
        public ScriptAction(Workspace workspace, String scriptName) {
            super(scriptName, workspace);
            // putValue(SHORT_DESCRIPTION, name);
            this.scriptName = scriptName;
            this.workspace = workspace;
        }

        /** @see AbstractAction */
        public void actionPerformed(final ActionEvent event) {

            Interpreter interpreter = new Interpreter();
            
            try {
                interpreter.set("workspace", workspace);
                interpreter.source(SCRIPT_MENU_DIRECTORY + System.getProperty("file.separator") + scriptName);
            } catch (FileNotFoundException e) {
               System.out.println("File not found");
               e.printStackTrace();
            } catch (IOException e) {
               System.out.println("IO Exception");
               e.printStackTrace();
            } catch (EvalError e) {
                System.out.println("Evaluation error");
                e.printStackTrace();
            }
        }
    }

    /**
     * @return new network action.
     */
    public Action getNewNetworkAction() {
        return newNetworkAction;
    }

    /**
     * @return the newConsoleAction.
     */
    public Action getNewConsoleAction() {
        return newConsoleAction;
    }

    /**
     * @return the newDataWorldAction.
     */
    public Action getNewDataWorldAction() {
        return newDataWorldAction;
    }

    /**
     * @return the newGameWorld2dAction.
     */
    public Action getNewGameWorld2dAction() {
        return newGameWorld2dAction;
    }

    /**
     * @return the newGaugeAction.
     */
    public Action getNewGaugeAction() {
        return newGaugeAction;
    }

    /**
     * @return the newOdorWorldAction.
     */
    public Action getNewOdorWorldAction() {
        return newOdorWorldAction;
    }

    /**
     * @return the newTextWorldAction.
     */
    public Action getNewTextWorldAction() {
        return newTextWorldAction;
    }

    /**
     * @return the newVisionWorldAction.
     */
    public Action getNewVisionWorldAction() {
        return newVisionWorldAction;
    }

    /**
     * @return the clearWorkspaceAction.
     */
    public Action getClearWorkspaceAction() {
        return clearWorkspaceAction;
    }

    /**
     * @return the openDataWorldAction.
     */
    public Action getOpenDataWorldAction() {
        return openDataWorldAction;
    }

    /**
     * @return the openGaugeAction.
     */
    public Action getOpenGaugeAction() {
        return openGaugeAction;
    }

    /**
     * @return the openNetworkAction.
     */
    public Action getOpenNetworkAction() {
        return openNetworkAction;
    }

    /**
     * @return the openOdorWorldAction.
     */
    public Action getOpenOdorWorldAction() {
        return openOdorWorldAction;
    }

    /**
     * @return the openWorkspaceAction.
     */
    public Action getOpenWorkspaceAction() {
        return openWorkspaceAction;
    }

    /**
     * @return the saveWorkspaceAction.
     */
    public Action getSaveWorkspaceAction() {
        return saveWorkspaceAction;
    }

    /**
     * @return the saveWorkspaceAsAction.
     */
    public Action getSaveWorkspaceAsAction() {
        return saveWorkspaceAsAction;
    }

    /**
     * @return the workspaceHelpAction.
     */
    public Action getWorkspaceHelpAction() {
        return workspaceHelpAction;
    }

    /**
     * @return the quitWorkspaceAction.
     */
    public Action getQuitWorkspaceAction() {
        return quitWorkspaceAction;
    }

    public Action getNewPlotAction() {
        return newPlotAction;
    }

    /**
     * @return the globalUpdateAction.
     */
    public Action getGlobalUpdateAction() {
        return globalUpdateAction;
    }

    /**
     * @return the openCouplingManagerAction.
     */
    public Action getOpenCouplingManagerAction() {
        return openCouplingManagerAction;
    }

    /**
     * @return the globalRunAction.
     */
    public Action getGlobalRunAction() {
        return globalRunAction;
    }

    /**
     * @return the globalStopAction.
     */
    public Action getGlobalStopAction() {
        return globalStopAction;
    }

    /**
     * @return the openCouplingListAction.
     */
    public Action getOpenCouplingListAction() {
        return openCouplingListAction;
    }

    /**
     * @return the openWorkspaceComponentListAction.
     */
    public Action getOpenWorkspaceComponentListAction() {
        return openWorkspaceComponentListAction;
    }
}
