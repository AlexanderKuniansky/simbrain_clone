/*
 * Part of Simbrain--a java-based neural network kit
 * Copyright (C) 2003 Jeff Yoshimi <www.jeffyoshimi.net>
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

package org.simbrain.network;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.hisee.core.Gauge;
import org.simbrain.network.dialog.BackpropDialog;
import org.simbrain.network.dialog.BackpropTrainingDialog;
import org.simbrain.network.dialog.HopfieldDialog;
import org.simbrain.network.dialog.LearnDialog;
import org.simbrain.network.dialog.NetworkDialog;
import org.simbrain.network.dialog.NeuronDialog;
import org.simbrain.network.dialog.SynapseDialog;
import org.simbrain.network.dialog.WTADialog;
import org.simbrain.network.old.NetworkSerializer;
import org.simbrain.network.pnodes.PNodeLine;
import org.simbrain.network.pnodes.PNodeNeuron;
import org.simbrain.network.pnodes.PNodeText;
import org.simbrain.network.pnodes.PNodeWeight;
import org.simbrain.resource.ResourceManager;
import org.simbrain.world.World;
import org.simnet.interfaces.ComplexNetwork;
import org.simnet.interfaces.Network;
import org.simnet.interfaces.Neuron;
import org.simnet.interfaces.Synapse;
import org.simnet.networks.Backprop;
import org.simnet.networks.ContainerNetwork;
import org.simnet.networks.Hopfield;
import org.simnet.networks.WinnerTakeAll;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.event.PZoomEventHandler;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * <b>NetworkPanel</b> is the main GUI view for the neural network model.  
 * It handles the construction, modification, and analysis of {@link org.simbrain.simnet}
 * neural networks.
 */
public class NetworkPanel extends PCanvas implements ActionListener {

	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// TODO:  The drawing system is ok for nodes and weights, but needs to be redesigned with 
	//		  future expansion in mind. In particular:
	//
	//		1. Make it possible for developers to create their own network screen objects,
	//		   without having to change any of the code for add /delete / copy / paste (or later
	//		   group / ungroup).  All the developer should have to do is specify some set of properties for
	// 		   the screen object (how it is drawn, if it can be moved, what its selection bounds are, etc), 
	//		   and copy, paste, etc. should come "for free"
	//
	//		   addPNode(PNode theNode, double x, double y)
	//		   getBounds(), isMovable(), getSelectionBounds(), isSelectable(), connectTo(), edit() / showPrefs()
	//		   randomize(), increment(), really anywhere I've got instanceof's below
	//
	//			Perhaps a screen-object class or interface, which extends PNode
	//
	//		2. Make the class PNodeLine part of PNodeWeight
	//
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// the neural-network object
	protected ContainerNetwork network = new ContainerNetwork();

	// reference to a world object
	protected World theWorld = null;

	// Selected objects. 
	private ArrayList selection = new ArrayList();

	//Vector of gauges
	private ArrayList gaugeList = new ArrayList();
	//	Vector of gauge values, one vector for each gauge; effectively a matrix
	private Vector gaugedObjects = new Vector(); 
	
	// List of PNodes
	private ArrayList nodeList = new ArrayList();

	// Interaction modes
	public static final int WORLD_TO_NET = 0;
	public static final int NET_TO_WORLD = 1;
	public static final int BOTH_WAYS = 2;
	public static final int NEITHER_WAY = 3;

	// Mode variables
	private int interactionMode = DEFAULT_INTERACTION_MODE;
	private boolean buildToggle = true;
	private boolean inOutMode = false;
	private boolean isAutoZoom = true;
	private boolean prevAutoZoom = isAutoZoom;
	
	// Cursor modes
	public static final int NORMAL = 1;
	public static final int PAN = 2;
	public static final int ZOOMIN = 3;
	public static final int ZOOMOUT = 4;
	private int cursorMode;
	private int prevCursorMode;

	// Misc
	protected JFrame owner;
	private NetworkThread theThread;
	private NetworkSerializer theSerializer;
	// Use when activating netpanel functions from a thread
	private boolean update_completed = false;

	//Values stored in user preferences
	private Color backgroundColor =
		new Color(UserPreferences.getBackgroundColor());
	public static final int DEFAULT_INTERACTION_MODE = BOTH_WAYS;

	// Piccolo stuff
	private PPanEventHandler panEventHandler;
	private PZoomEventHandler zoomEventHandler;
	protected MouseEventHandler mouseEventHandler;
	protected KeyEventHandler keyEventHandler;

	// JComponents
	private JToolBar topTools = new JToolBar();
	private JToolBar buildTools = new JToolBar();
	private JToolBar iterationBar = new JToolBar();
	private JPanel bottomPanel = new JPanel();
	private JLabel timeLabel = new JLabel("0");
	private JButton clearBtn =
		new JButton(ResourceManager.getImageIcon("Eraser.gif"));
	private JButton randBtn =
		new JButton(ResourceManager.getImageIcon("Rand.gif"));
	private JButton playBtn =
		new JButton(ResourceManager.getImageIcon("Play.gif"));
	protected JButton stepBtn =
		new JButton(ResourceManager.getImageIcon("Step.gif"));
	private JButton interactionBtn =
		new JButton(ResourceManager.getImageIcon("BothWays.gif"));
	private JButton buildBtn =
		new JButton(ResourceManager.getImageIcon("Build.gif"));
	private JButton panBtn =
		new JButton(ResourceManager.getImageIcon("Pan.gif"));
	private JButton arrowBtn =
		new JButton(ResourceManager.getImageIcon("Arrow.gif"));
	private JButton refreshBtn =
		new JButton(ResourceManager.getImageIcon("Refresh.gif"));
	private JButton zoomInBtn =
		new JButton(ResourceManager.getImageIcon("ZoomIn.gif"));
	private JButton zoomOutBtn =
		new JButton(ResourceManager.getImageIcon("ZoomOut.gif"));
	private JButton gaugeBtn =
		new JButton(ResourceManager.getImageIcon("Gauge.gif"));
	private JButton newNodeBtn =
		new JButton(ResourceManager.getImageIcon("New.gif"));
	private JButton dltBtn =
		new JButton(ResourceManager.getImageIcon("Delete.gif"));
	private JButton inputBtn =
		new JButton(ResourceManager.getImageIcon("Input.gif"));
	private JButton outputBtn =
		new JButton(ResourceManager.getImageIcon("Output.gif"));
	private JButton iterationBtn = new JButton("Iterations");
	
	
	public NetworkPanel() {
	}
	
	/**
	 * Constructs a new network panel
	 *
	 * @param owner Reference to Simulation frame
	 */
	public NetworkPanel(JFrame owner) {
		this.owner = owner;
		this.setPreferredSize(new Dimension(400, 200));
		init();
	}
	
	public void initCastor() {
		network.init();
		Iterator i = nodeList.iterator();
		while  (i.hasNext()) {
			Object o = i.next();
			this.getLayer().addChild((PNode)o);
			if (o instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron)o;
				n.setParentPanel(this);
				n.init();
				n.setInput(n.getNeuron().isInput());
				n.setOutput(n.getNeuron().isOutput());
			}
			if (o instanceof PNodeWeight) {
				((PNodeWeight)o).init();
			}
			
		}
	}
	
	public void init() {
		this.setBackground(new Color(UserPreferences.getBackgroundColor()));
		theSerializer = new NetworkSerializer(this);
		clearBtn.addActionListener(this);
		randBtn.addActionListener(this);
		playBtn.addActionListener(this);
		stepBtn.addActionListener(this);
		buildBtn.addActionListener(this);
		interactionBtn.addActionListener(this);
		panBtn.addActionListener(this);
		arrowBtn.addActionListener(this);
		refreshBtn.addActionListener(this);
		zoomInBtn.addActionListener(this);
		zoomOutBtn.addActionListener(this);
		gaugeBtn.addActionListener(this);
		newNodeBtn.addActionListener(this);
		dltBtn.addActionListener(this);
		inputBtn.addActionListener(this);
		outputBtn.addActionListener(this);
		iterationBtn.addActionListener(this);

		clearBtn.setToolTipText("Set selected nodes to 0");
		randBtn.setToolTipText("Randomize selected nodes and weights");
		playBtn.setToolTipText("Iterate network update algorithm");
		stepBtn.setToolTipText("Step network update algorithm");
		buildBtn.setToolTipText("Add /remove pallette of build tools");
		interactionBtn.setToolTipText(
			"Determine how network and world interact");
		panBtn.setToolTipText("Pan and right-drag-zoom mode (H)");
		arrowBtn.setToolTipText("Selection mode (V)");
		zoomInBtn.setToolTipText("Zoom in mode (Z)");
		zoomOutBtn.setToolTipText("Zoom out mode (Z)");
		gaugeBtn.setToolTipText("Add gauge to simulation");
		newNodeBtn.setToolTipText("Add new node");
		dltBtn.setToolTipText("Delete selected node");
		inputBtn.setToolTipText("Set to input node");
		outputBtn.setToolTipText("Set to output node");
		iterationBtn.setToolTipText("Reset iterations");

		topTools.add(zoomInBtn);
		topTools.add(zoomOutBtn);
		topTools.addSeparator();
		topTools.addSeparator();
		topTools.add(arrowBtn);
		topTools.add(panBtn);
		topTools.addSeparator();
		topTools.addSeparator();
		topTools.add(playBtn);
		topTools.add(stepBtn);
		topTools.addSeparator();
		topTools.addSeparator();
		topTools.add(randBtn);
		topTools.add(clearBtn);
		topTools.add(buildBtn);
		topTools.addSeparator();
		topTools.addSeparator();
		topTools.add(gaugeBtn);
		topTools.add(interactionBtn);

		buildTools.add(newNodeBtn);
		buildTools.add(dltBtn);
		buildTools.add(inputBtn);
		buildTools.add(outputBtn);

		this.setLayout(new BorderLayout());
		add("North", topTools);

		iterationBar.add(iterationBtn);
		iterationBar.addSeparator();
		iterationBar.addSeparator();
		iterationBar.add(timeLabel);
		bottomPanel.add(buildTools);
		bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		bottomPanel.add(iterationBar);
		add("South", bottomPanel);

		if (buildToggle == false) {
			bottomPanel.setVisible(false);
		}

		this.cursorMode = NORMAL;
		panEventHandler = this.getPanEventHandler();
		this.removeInputEventListener(this.getPanEventHandler());
		zoomEventHandler = this.getZoomEventHandler();
		this.removeInputEventListener(this.getZoomEventHandler());
		// Create and register event handlers
		mouseEventHandler = new MouseEventHandler(this, getLayer());
		keyEventHandler = new KeyEventHandler(this);
		addInputEventListener(mouseEventHandler);
		addInputEventListener(keyEventHandler);
		getRoot().getDefaultInputManager().setKeyboardFocus(keyEventHandler);

	}

	public ArrayList getNodeList() {
		return nodeList;
	}
	public ArrayList getSelection() {
		return selection;
	}
	public ArrayList getGauges() {
		return gaugeList;
	}
	public ContainerNetwork getNetwork() {
		return this.network;
	}
	public void save() {
		theSerializer.writeNet(this.getCurrentFile());
	}
	public void saveAs() {
		theSerializer.showSaveFileDialog();
	}
	public void open(File theFile) {
		theSerializer.readNetwork(theFile);
	}
	public void open() {
		theSerializer.showOpenFileDialog();
	}
	public void openOld(){
		theSerializer.showOpenFileDialogOld();
	}
	public File getCurrentFile() {
		return theSerializer.getCurrentFile();
	}
	public boolean getInOutMode() {
		return inOutMode;
	}
	
	public void setInOutMode(boolean b) {
		inOutMode = b;
	}
	
	/**
	 * Returns a reference to the Simulation frame.  Used to provide access
	 * to Simulation level methods.
	 * 
	 * @return reference to the Simulation frame
	 */
	public Frame getParentFrame() {
		return owner;
	}

	/**
	 * If true, show labels of input and output  nodes so that these can be conneced with the world component
	 */
	public void showInOut(boolean b) {
		setInOutMode(b);
		if (b == true) {
			Iterator i = nodeList.iterator();
			while (i.hasNext()) {
				PNode pn = (PNode) i.next();
				if (pn instanceof PNodeNeuron) {
					if(((PNodeNeuron)pn).getNeuron().isInput() || ((PNodeNeuron)pn).getNeuron().isOutput()) {
						((PNodeNeuron)pn).showInOut(true);
					}
				}
			}
		}
		else {
			Iterator i = nodeList.iterator();
			while (i.hasNext()) {
				PNode pn = (PNode) i.next();
				if (pn instanceof PNodeNeuron) {
					if(((PNodeNeuron)pn).getNeuron().isInput() || ((PNodeNeuron)pn).getNeuron().isOutput()) {
						((PNodeNeuron)pn).showInOut(false);
					}
				}
			}
		}
		
	}
	
	/**
	 * Set the background color, store it to user preferences, and repaint the panel
	 * 
	 * @param clr new background color for network panel
	 */
	public void setBackgroundColor(Color clr) {
		backgroundColor = clr;
		this.setBackground(backgroundColor);
		UserPreferences.setBackgroundColor(clr.getRGB());
		repaint();
	}

	/**
	 * Returns a refrence to the network selection event handler
	 * 
	 * @return reference to network handler
	 */
	public MouseEventHandler getHandle() {
		return mouseEventHandler;
	}

	/**
	 * Registers the neural network which this PCanvas represents
	 * 
	 * @param network reference to the neural network object
	 */
	public void setNetwork(ContainerNetwork network) {
		this.network = network;
	}

	/**
	 * Registers the environment which the neural network will interact with
	 * 
	 * @param w reference to the world object
	 */
	public void setWorld(World w) {
		theWorld = w;
	}

	/* (non-Javadoc)
	 * @see java.awt.Component#getPreferredSize()
	 */
	public Dimension getPreferredSize() {
		return (new Dimension(400, 400));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		// Handle pop-up menu events
		Object o = e.getSource();
		if (o instanceof JMenuItem) {
			JMenuItem m = (JMenuItem)o;
			String text = ((JMenuItem)o).getText();
						
			if (text.equalsIgnoreCase("Connect")) {
				connectSelected();
			} else if (text.equalsIgnoreCase("Delete")) {
				deleteSelection();	
			} else if (text.equalsIgnoreCase("Cut")) {
				mouseEventHandler.cutToClipboard();
			} else if (text.equalsIgnoreCase("Copy")) {
				mouseEventHandler.copyToClipboard();
			} else if (text.equalsIgnoreCase("Paste")) {
				mouseEventHandler.pasteFromClipboard();
			} else if (text.equalsIgnoreCase("Set properties")) {
				showPrefsDialog(mouseEventHandler.getCurrentNode());				
			} else if (text.equalsIgnoreCase("Horizontal")) {
				alignHorizontal();
			} else if (text.equalsIgnoreCase("Vertical")) {
				alignVertical();
			} else if (text.equalsIgnoreCase("Horizontally")) {
				spacingHorizontal();
			} else if (text.equalsIgnoreCase("Vertically")) {
				spacingVertical();
			} else if (text.equalsIgnoreCase("Set network properties")) {
				showNetworkPrefs();			
			} else if (text.equalsIgnoreCase("Winner take all network")) {
				showWTADialog();
			} else if (text.equalsIgnoreCase("Hopfield network")) {
				showHopfieldDialog();
			} else if (text.equalsIgnoreCase("Backprop network")) {
				showBackpropDialog();
			} else if (text.equalsIgnoreCase("Train backprop network")) {
				Network net  = ((PNodeNeuron)mouseEventHandler.getCurrentNode()).getNeuron().getNeuronParent().getNetworkParent();
				if (net != null) {
					showBackpropTraining((Backprop)net);					
				}
			}  else if (text.equalsIgnoreCase("Randomize network")) {
				Network net  = ((PNodeNeuron)mouseEventHandler.getCurrentNode()).getNeuron().getNeuronParent().getNetworkParent();
				if (net != null) {
					if (net instanceof Backprop) {
						((Backprop)net).randomize();			
					}
				}
				net  = ((PNodeNeuron)mouseEventHandler.getCurrentNode()).getNeuron().getNeuronParent();
				if (net != null) {
					if (net instanceof Hopfield) {
						((Hopfield)net).randomizeWeights();
					}					
				}
				renderObjects();
			} else if (text.equalsIgnoreCase("Train hopfield network")) {
				Network net  = ((PNodeNeuron)mouseEventHandler.getCurrentNode()).getNeuron().getNeuronParent();
				if (net != null) {
					((Hopfield)net).train();
					renderObjects();
				}
			}
			return;
		}
		
		
		// Handle button events
		JButton btemp = (JButton)o;

		if (btemp == clearBtn) {
			clearSelection();
		} else if (btemp == randBtn) {
			randomizeSelection();
		} else if (btemp == iterationBtn) {
			network.setTime(0);
			timeLabel.setText("0");
		} else if (btemp == stepBtn) {
			updateNetworkAndWorld();
		} else if (btemp == gaugeBtn) {
			addGauge();
		} else if (btemp == playBtn) {
			if (theThread == null) {
				theThread = new NetworkThread(this);
			}
			if (theThread.isRunning() == false) {

				playBtn.setIcon(ResourceManager.getImageIcon("Stop.gif"));
				playBtn.setToolTipText(
					"Stop iterating network update algorithm");
				theThread.setRunning(true);
				theThread.start();
			} else {
				playBtn.setIcon(ResourceManager.getImageIcon("Play.gif"));
				playBtn.setToolTipText(
					"Start iterating network update algorithm");
				theThread.setRunning(false);
				theThread = null;
			}
		} else if (btemp == buildBtn) {
			if (buildToggle == false) {
				bottomPanel.setVisible(true);
				buildToggle = true;
			} else {
				bottomPanel.setVisible(false);
				buildToggle = false;
			}
		} else if (btemp == newNodeBtn) {
			addNeuron();
		} else if (btemp == dltBtn) {
			deleteSelection();
		} else if (btemp == inputBtn) {
			setInputs();
		} else if (btemp == outputBtn) {
			setOutputs();
		} else if (btemp == interactionBtn) {
			if (interactionMode == 3) {
				interactionMode = 0;
			} else {
				interactionMode++;
			}
			switch (interactionMode) {
				case WORLD_TO_NET :
					interactionBtn.setIcon(
						ResourceManager.getImageIcon("WorldToNet.gif"));
					interactionBtn.setToolTipText(
						"World is sending stimuli to the network");
					break;
				case NET_TO_WORLD :
					interactionBtn.setIcon(
						ResourceManager.getImageIcon("NetToWorld.gif"));
					interactionBtn.setToolTipText(
						"Network output is moving the creature");
					break;
				case BOTH_WAYS :
					interactionBtn.setIcon(
						ResourceManager.getImageIcon("BothWays.gif"));
					interactionBtn.setToolTipText(
						"World and network are interacting");
					break;
				case NEITHER_WAY :
					interactionBtn.setIcon(
						ResourceManager.getImageIcon("NeitherWay.gif"));
					interactionBtn.setToolTipText(
						"World and network are disconnected");
					break;
			}
		} else if (btemp == panBtn) { 
			if (cursorMode != PAN)
				setCursorMode(PAN);
		} else if (btemp == arrowBtn) {
			if (cursorMode!= NORMAL)
				setCursorMode(NORMAL);
		} else if (btemp == zoomInBtn) {
			if (cursorMode != ZOOMIN)
				setCursorMode(ZOOMIN);
		} else if (btemp == zoomOutBtn) {
			if (cursorMode != ZOOMOUT)
				setCursorMode(ZOOMOUT);
		}
	}

	public void setInteractionMode(int i) {
		if ((interactionMode > 0) && (interactionMode < 4)) {
			interactionMode = i;
		}
	}
	public int getInteractionMode() {
		return interactionMode;
	}

	/**
	 * Returns the on-screen neurons
	 * 
	 * @return a collection of PNodeNeurons
	 */
	public Collection getNeuronList() {
		Collection v = new Vector();
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				v.add(pn);
			}
		}
		return v;
	}
	
	/**
	 * Returns the on-screen syanpses
	 * 
	 * @return a collection of PNodeNeurons
	 */
	public Collection getSynapseList() {
		Collection v = new Vector();
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeWeight) {
				v.add(pn);
			}
		}
		return v;
	}

	/**
	 * Returns the selected PNodeNeurons
	 * 
	 * @return selected PNodeNeurons 
	 */
	public ArrayList getSelectedPNodeNeurons() {
		ArrayList ret = new ArrayList();
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				ret.add(pn);
			}
		}
		return ret;
	}
	
	/**
	 * Returns the on-screen neurons
	 * 
	 * @return selected neurons 
	 */
	public ArrayList getSelectedNeurons() {
		ArrayList neurons = new ArrayList();
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				neurons.add(((PNodeNeuron) pn).getNeuron());
			}
		}
		return neurons;
	}
	/**
	 * Returns the on-screen weights
	 * 
	 * @return selecteed Weights
	 */
	public ArrayList getSelectedWeights() {
		ArrayList v = new ArrayList();
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeWeight) {
				v.add(((PNodeWeight) pn).getWeight());
			}
		}
		return v;
	}

	/**
	 * Toggle between pan and zoom mode
	 * 
	 * @param newmode mode to set cursor to
	 */
	public void setCursorMode(int newmode) {
		if (newmode != cursorMode) {
			prevCursorMode = cursorMode;
			cursorMode = newmode;
			if (newmode == PAN) {
				isAutoZoom = prevAutoZoom;
				this.addInputEventListener(this.panEventHandler);
				this.addInputEventListener(this.zoomEventHandler);
				this.removeInputEventListener(this.mouseEventHandler);
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			} else if (newmode == ZOOMIN) {
				if (prevCursorMode != ZOOMOUT) prevAutoZoom = isAutoZoom;
				isAutoZoom = false; 
				if (prevCursorMode == PAN) {
					this.removeInputEventListener(this.panEventHandler);
					this.removeInputEventListener(this.zoomEventHandler);
					this.addInputEventListener(this.mouseEventHandler);					
				}
				setCursor(Toolkit.getDefaultToolkit().createCustomCursor(ResourceManager.getImage("ZoomIn.gif"), new Point(9,9), "zoom_in"));
			} else if (newmode == ZOOMOUT) {
				if (prevCursorMode != ZOOMIN) prevAutoZoom = isAutoZoom;
				isAutoZoom = false; 
				if (prevCursorMode == PAN) {
					this.removeInputEventListener(this.panEventHandler);
					this.removeInputEventListener(this.zoomEventHandler);
					this.addInputEventListener(this.mouseEventHandler);					
				}
				setCursor(Toolkit.getDefaultToolkit().createCustomCursor(ResourceManager.getImage("ZoomOut.gif"), new Point(9,9), "zoom_out"));
			} else if (newmode == NORMAL) {
				isAutoZoom = prevAutoZoom;
				if (prevCursorMode == PAN) {
					this.removeInputEventListener(this.panEventHandler);
					this.removeInputEventListener(this.zoomEventHandler);
					this.addInputEventListener(this.mouseEventHandler);					
				}
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}
	}

	
	/**
	 * Update the network, gauges, and world.
	 * This is where the main control between components happens.
	 * Called by world component (on clicks), and the network-thread.
	 */
	public synchronized void updateNetwork() {
		
		// Get stimulus vector from world and update input nodes
		if ((interactionMode == WORLD_TO_NET) || (interactionMode == BOTH_WAYS)) {
			updateNetworkInputs();
		}
		this.network.update(); // Call Network's update function
		timeLabel.setText("" + network.getTime()); //Update the timeLabel

		renderObjects();

		// Send state-information to gauge(s)
		for (int i = 0; i < gaugeList.size(); i++) {
			Gauge theGauge = (Gauge) gaugeList.get(i);
			theGauge.addDatapoint(getNetworkState(i));
			//			if (theGauge.isOn() == true) {
			//				System.out.println("Gauge[" + i + "]:" + getNetworkState(i));
			//			}
		}
		clearNetworkInputs();
		update_completed = true;  
	}

	/**
	 * Update network then get output from the world object
	 */
	public synchronized void updateNetworkAndWorld() {
		updateNetwork();
		// Update World
		if ((interactionMode == NET_TO_WORLD) || (interactionMode == BOTH_WAYS)) {
			updateWorld();
		}
	}
	
	/**
	 * Go through each output node and send the associated output value to the world
	 * component
	 */
	public void updateWorld() {
		Iterator it = this.network.getOutputs().iterator();
		while (it.hasNext()) {
			Neuron n = (Neuron)it.next();
			theWorld.motorCommand(n.getOutputLabel(), n.getActivation());
		}
	}
	
	/**
	 * Update input nodes of the network based on the state of the world
	 */
	public void updateNetworkInputs() {
		Iterator it = this.network.getInputs().iterator();
		theWorld.updateStimulus();
		while (it.hasNext()) {
			Neuron n = (Neuron)it.next();
			n.setInputValue(theWorld.getStimulus(n.getInputLabel()));
		}
	}
	

	public void clearNetworkInputs() {
		Iterator it = this.network.getInputs().iterator();
		while (it.hasNext()) {
			Neuron n = (Neuron)it.next();
			n.setInputValue(0);
		}
	}
	
	/**
	 * Used by Network thread to ensure that an update cycle is complete before updating again.
	 * 
	 */
	public boolean isUpdateCompleted() {
		return update_completed;
	}

	public void setUpdateCompleted(boolean b) {
		update_completed = b;
	}

	/////////////////////////////////////////////////////
	// Neuron and weight deletion and addition methods //
	/////////////////////////////////////////////////////

	/**
	 * Delete all neurons, reset gauges
	 */
	public void deleteAll() {
		selectAll();
		deleteSelection();
		renderObjects();
		resetGauges();
	}
	
	/**
	 * Place an existing network into the current network
	 * 
	 * @param np the network to place
	 */
	public void placeNetwork(NetworkPanel np) {
		
		//Add logical network
		Network net = np.getNetwork();
		network.addNeuronList((ArrayList)net.getNeuronList());
		network.getWeightList().addAll(net.getWeightList());
		network.getInputs().addAll(net.getInputs());
		network.getOutputs().addAll(net.getOutputs());
		if(net instanceof ComplexNetwork) {
			network.addNetworkList(((ComplexNetwork)net).getNetworkList());
		}
		
		//Add graphics
		Point2D p = mouseEventHandler.getLastLeftClicked();
		Iterator it = np.getNodeList().iterator();
		while(it.hasNext()) {
			PNode n = ((PNode)it.next());
			nodeList.add(n);
			this.getLayer().addChild(n);
			if(n instanceof PNodeNeuron) {
				((PNodeNeuron)n).setParentPanel(this);
				n.translate(p.getX(), p.getY());
			}
			mouseEventHandler.select(n); //TODO: Why do I have to call mouseEventHandler's version of this?
		}
		renderObjects();
	}
	
	
	/**
	 * Adds a simnet network 
	 * 
	 * @param net the net to add
	 * @param layout how to lay out the neurons in the network
	 */
	public void addNetwork(Network net, String layout) {
		network.addNetwork(net);
		int numRows = (int)Math.sqrt(net.getNeuronCount());
		int increment = 45;
		
		if(layout.equalsIgnoreCase("Line")) {
			for (int i = 0; i < net.getNeuronCount(); i++) {
				double x = getLastClicked().getX();
				double y = getLastClicked().getY();
				PNodeNeuron theNode = new PNodeNeuron(x + i * increment, y, net.getNeuron(i), this);
				nodeList.add(theNode);
				this.getLayer().addChild(theNode);
			}
			
		} else if (layout.equalsIgnoreCase("Grid")) {
			for (int i = 0; i < net.getNeuronCount(); i++) {
				double x = getLastClicked().getX() + (i % numRows) * increment;
				double y = getLastClicked().getY() + (i / numRows) * increment;
				PNodeNeuron theNode = new PNodeNeuron(x , y, net.getNeuron(i), this);
				nodeList.add(theNode);
				this.getLayer().addChild(theNode);
			}			
		} else if (layout.equalsIgnoreCase("Layers")) {
			if (! (net instanceof ComplexNetwork)) {
				return;
			}
			ComplexNetwork cn = (ComplexNetwork)net;
			double x = getLastClicked().getX();
			double y = getLastClicked().getY() + cn.getNetworkList().size() * increment;
			
			for (int i = 0; i < cn.getNetworkList().size(); i++) {
				for(int j = 0; j < cn.getNetwork(i).getNeuronCount(); j++) {
					PNodeNeuron theNode = new PNodeNeuron(x + j * increment, y - i * increment, cn.getNetwork(i).getNeuron(j),this);
					nodeList.add(theNode);
					this.getLayer().addChild(theNode);
				}
			}
		}
		
		for (int i = 0; i < net.getWeightCount(); i++) {
			Synapse s = net.getWeight(i);
			PNodeWeight theNode = new PNodeWeight(findPNodeNeuron(s.getSource()), findPNodeNeuron(s.getTarget()), s);
			nodeList.add(theNode);
			this.getLayer().addChild(theNode);
		}
		
		renderObjects();
		repaint();		
	}
	

	/**
	 * Adds a node (neuron or weight) the the network
	 * 
	 * @param theNode the node to add to the network
	 * @param  whether the newly added node should be the only selected node
	 */
	public void addNode(PNode theNode, boolean select) {
		nodeList.add(theNode);
		if (theNode instanceof PNodeNeuron) {
			Neuron n = (((PNodeNeuron) theNode).getNeuron());
			network.addNeuron(n);
			((PNodeNeuron) theNode).setInput(n.isInput());
			((PNodeNeuron) theNode).setOutput(n.isOutput());
		} else if (theNode instanceof PNodeWeight) {
			network.addWeight(((PNodeWeight) theNode).getWeight());
		}
		this.getLayer().addChild(theNode);
		if (select == true) {
			this.mouseEventHandler.unselectAll();
			this.mouseEventHandler.select(theNode);
		}
		resetGauges();
	}

	/**
	 * Add a new PNodeNeuron to the network, either at the last position clicked on 
	 * screen or to the right of the last selected neuron
	 */
	protected void addNeuron() {
		PNodeNeuron theNode;
		PNode selectNeuron = getSingleSelection();

		// If a node is selected, put a new node to its left
		if (selectNeuron != null) {
			theNode = new PNodeNeuron(
					getGlobalX((PNode) selectNeuron) + PNodeNeuron.neuronScale + 45,
					getGlobalY((PNode) selectNeuron), this);
			network.addNeuron(theNode.getNeuron());
		}
		// Else put the new node at the last clicked position on-screen
		else {
			int new_x, new_y;
			Point2D thePoint = mouseEventHandler.getLastLeftClicked();
			//TODO: Put handler here for two cases: No neurons on screen or some neurons on screen.
			if (thePoint == null) {
				return;
			}
			theNode = new PNodeNeuron(thePoint, this);
			network.addNeuron(theNode.getNeuron());

		}
		theNode.getNeuron().setNeuronParent(network);
		theNode.setId(theNode.getNeuron().getId());
		nodeList.add(theNode);
		this.getLayer().addChild(theNode);
		renderObjects();
		this.mouseEventHandler.unselectAll();
		this.mouseEventHandler.select(theNode);
	}

	/**
	 * Add a PNodeNeuron corresponding to an already-constructed Neuron object
	 * 
	 * @param x x position of the new PNodeNeuron
	 * @param y y position of the new PNodeNeuron
	 * @param neuron reference to the Neuron object
	 */
	public void addNeuron(int x, int y, Neuron neuron) {
		PNodeNeuron theNode = new PNodeNeuron(x, y, neuron, this);
		network.addNeuron(theNode.getNeuron());
		theNode.getNeuron().setNeuronParent(network);
		theNode.setInput(neuron.isInput());
		theNode.setOutput(neuron.isOutput());
		nodeList.add(theNode);
		this.getLayer().addChild(theNode);
	}
	

	/**
	 * Create a PNodeWeight connecting two PNodeNeurons
	 * 
	 * @param source source PNodeNeuron
	 * @param target target PNodeNeuron
	 * @param weight weight, to be associated with a PNodeWeight, connecting source and target
	 */
	public void addWeight(PNodeNeuron source, PNodeNeuron target, Synapse weight) {
		weight.setSource(source.getNeuron());
		weight.setTarget(target.getNeuron());
		network.addWeight(weight);
		PNodeWeight theNode = new PNodeWeight(source, target, weight);
		theNode.render();
		nodeList.add(theNode);
		getLayer().addChild(theNode);
		getHandle().addSelectableNode(theNode);

	}

	/**
	 * Create a PNodeWeight connecting two PNodeNeurons
	 * 
	 * @param source source PNodeNeuron
	 * @param target target PNodeNeuron
	 */
	protected void addWeight(PNodeNeuron source, PNodeNeuron target) {
		PNodeWeight w = new PNodeWeight(source, target);
		// This creates the new network weight in addition to the new PNodeWeight
		nodeList.add(w);
		w.render();
		network.addWeight(w.getWeight());
		getLayer().addChild(w);
		getHandle().addSelectableNode(w);
	}

	/**
	 * Used by updateWeights
	 * @return true if the weight exists, false otherwise
	 */
	public boolean checkWeight(Synapse w) {
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeWeight) {
				if (((PNodeWeight) pn).getWeight().equals(w)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 *  Make sure all weights in the logical network are represented by PNodeWeights
	 *  in the Network Panel view.  Handles cases where connections are added directly
	 *  to the network object
	 */
	public void updateWeights() {
		// Update node_list with all weights
		ArrayList weights = (ArrayList) network.getWeightList();
		for (int i = 0; i < weights.size(); i++) {
			Synapse w = (Synapse) weights.get(i);
			if ((checkWeight(w) == false)) {
				addWeight(findPNodeNeuron(w.getSource()), findPNodeNeuron(w.getTarget()), w);
			}
		}
	}

	/**
	 * Connect all selected nodes to currently clicked node in netpanel
	 */
	public void connectSelected() {

		PNode currentNode = mouseEventHandler.getCurrentNode();

		if ((currentNode != null) && (currentNode instanceof PNodeNeuron)) {
			for (int i = 0; i < selection.size(); i++) {
				PNode n = (PNode)selection.get(i);
				if (n instanceof PNodeNeuron) {
					addWeight((PNodeNeuron)n, (PNodeNeuron) currentNode);
				}
			}
			currentNode.moveToFront();	
		}
	}
	
	public void connectSelectedTo(PNodeNeuron target) {

		if (target != null) {
			for (int i = 0; i < selection.size(); i++) {
				PNode n = (PNode)selection.get(i);
				if (n instanceof PNodeNeuron) {
					addWeight((PNodeNeuron)n, (PNodeNeuron) target);
				}
			}
			target.moveToFront();	
		}
	}
	
	/**
	 * Find the PNodeNeuron associated with a logical Neuron
	 * 
	 * @param n refrence to the Neuron object to be assocaited with a PNodeNeuron
	 * @return PNodeNeuron associated with the provided neuron object
	 */
	public PNodeNeuron findPNodeNeuron(Neuron n) {
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				if (((PNodeNeuron) pn).getNeuron().equals(n)) {
					return (PNodeNeuron) pn;
				}
			}
		}
		return null; // PNode not found

	}

	/**
	 * Delete a PNode (Neuron or weight) from the NetworkPanel
	 * 
	 * @param node PNode to be deleted fromm network
	 */
	public void deleteNode(PNode node) {
				
		if (node instanceof PNodeNeuron) {
			Neuron neuron = ((PNodeNeuron) node).getNeuron();
			// Clear out PNodeWeights connected to node
			for (int i = 0; i < nodeList.size(); i++) {
				PNode pn = (PNode) nodeList.get(i);
				if (pn instanceof PNodeWeight) {
					if (neuron.connectedToWeight(((PNodeWeight) pn).getWeight()) == true) {
						nodeList.remove(i);
						this.getLayer().removeChild(pn);
						i -= 1;
						// Must adjust the index to the resized array_list.  TODO: Is there a more elegant way to do this?
					}
				}
			}
			network.deleteNeuron(((PNodeNeuron) node).getNeuron());
			this.getLayer().removeChild(node);
			nodeList.remove(node);
		} else if (node instanceof PNodeWeight) {
			PNodeWeight w = (PNodeWeight)node;
			w.setSource(null);
			// Must remove source and target's reference to this weight
			w.setTarget(null);
			w.getWeight().getTarget().getNeuronParent().deleteWeight(w.getWeight());
			if (this.getLayer().isAncestorOf(node)) { 
				this.getLayer().removeChild(node);
			}
			nodeList.remove(node);
		}
	
		resetGauges(); // TODO: Check whether this is a monitored node, and reset gauge if it is.

	}
	
	public void addText(String text) {
		PNodeText theText = new PNodeText(text);
		theText.addToPanel(this);		
	}

	//////////////////////////////////////
	// Node select and unselect methods //
	//////////////////////////////////////

	/**
	 * Select a PNode (Neuron or weight). Called by networkSelectionEvent handler
	 *
	 * @param node PNode to select
	 */
	public void select(PNode node) {
		if (selection.contains(node)) {
			return;
		}
		selection.add(node);
	}

	/**
	 * Select all PNodes (Neurons and Weights)
	 */
	public void selectAll() {
		selection.clear();
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			select(pn);
		}
		renderObjects();
	}

	/**
	 * Select all PNodeNeurons
	 */
	public void selectNeurons() {
		selection.clear();
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				select(pn);
			}
		}
		renderObjects();
	}

	/**
	 * Select all PNodeWeights
	 */
	public void selectWeights() {
		selection.clear();
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeWeight) {
				select(pn);
			}
		}
		renderObjects();
	}

	/**
	 * Unselect all selected PNodes
	 */
	public void unselectAll() {
		this.selection.clear();
		renderObjects();
	}

	/**
	 * Unselect a specific PNode 
	 * 
	 * @param node the PNode to unselect
	 */
	public void unselect(PNode node) {
		this.selection.remove(node);
	}

	/**
	 * Delete the currently selected PNodes (Neurons and Weights)
	 */
	public void deleteSelection() {
		
		for (Iterator e = selection.iterator(); e.hasNext();) {
			PNode node = (PNode) e.next();
			deleteNode(node);
		}
		selection.clear();
		renderObjects();
	}

	/**
	 * Get a reference to the currently selected node.  Assumes that only one PNode is selected.
	 * 
	 * @return Reference to a single selected node
	 */
	public PNode getSingleSelection() {
		if (this.selection.size() != 1) {
			return null;
		}
		return (PNode) selection.get(0);
	}

	/**
	 * Checks whether all objects in a set are logical Neurons
	 * 
	 * @param set a collection of objects
	 * @return true if all objects in the collection are Neurons
	 */
	private boolean allIsNeuron(Collection set) {
		Iterator i = set.iterator();
		while (i.hasNext()) {
			Object n = i.next();
			if (!(n instanceof Neuron))
				return false;
		}
		return true;
	}

	/**
	 * Returns the x position (in global coords) of input node's bounds.
	 * It simply gets the position of the input node, after that converts
	 * it to Global coordinate system before returning the x value.
	 *
	 * The reason for using getGlobalX() and getGlobalY() is that
	 * PNode.getX() and PNode.getY() only returns position of PNode in
	 * local coordinate system. However, sometimes the position
	 * of a node in global coordinate system (which in this case is
	 * NetWorkPanel's system ) is needed for moving, calculating..
	 *
	 * @param node PNode that has the x position to be returned.
	 * @return x position (in global coords) of input node's bounds.
	 */
	public static double getGlobalX(PNode node) {
		Point2D p = new Point2D.Double(node.getX(), node.getY());
		return node.localToGlobal(p).getX();
	}

	/**
	 * Returns the y position (in global coords) of input node's bounds.
	 * It simply get the position of the input node, afterthat converts
	 * it to Global coordinate system before returning the y value.
	 *
	 * The reason for using getGlobalX() and getGlobalY() is that
	 * PNode.getX() and PNode.getY() only returns position of PNode in
	 * local coordinate system. However, sometimes the position
	 * of a node in global coordinate system (which in this case is
	 * NetWorkPanel's system ) is needed for moving, calculating..
	 *
	 * @param node PNode that has the y position to be returned.
	 * @return y position (in global coords) of input node's bounds.
	 */
	public static double getGlobalY(PNode node) {
		Point2D p = new Point2D.Double(node.getX(), node.getY());
		return node.localToGlobal(p).getY();
	}

	/**
		* Return global coordinates of x centerpoint of a Neuron
		*
		* @param node the PNodeNeuron whose centerpoint is desired
		* @return x coordinate (in global coordinates) of the PNodeNeuron
		*/
	public static double getGlobalCenterX(PNodeNeuron node) {
		Point2D p =
			new Point2D.Double(
				node.getX() + PNodeNeuron.NEURON_HALF,
				node.getY() + PNodeNeuron.NEURON_HALF);
		return node.localToGlobal(p).getX();
	}

	/**
	 * Return global coordinates of y centerpoint of a Neuron
	 *
	 * @param node the PNodeNeuron whose centerpoint is desired
	 * @return y coordinate (in global coordinates) of the PNodeNeuron
	 */
	public static double getGlobalCenterY(PNodeNeuron node) {
		double y = node.getY() + PNodeNeuron.NEURON_HALF;
		//Compensate for size of input/output line.
		if (node.isInput())
			y += PNodeNeuron.ARROW_LINE;
		if (node.isOutput())
			y -= PNodeNeuron.ARROW_LINE;
		Point2D p =
			new Point2D.Double(node.getX() + PNodeNeuron.NEURON_HALF, y);
		return node.localToGlobal(p).getY();
	}

	/**
	 * Set activation of selected neurons to zero
	 */
	protected void clearSelection() {
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				((PNodeNeuron) pn).getNeuron().setActivation(0);
			}
		}
		renderObjects();
	}
	
	public void clearAll() {
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				((PNodeNeuron) pn).getNeuron().setActivation(0);
			}
		}
		renderObjects();	
	}

	/**
	 * Randomize selected neurons and weights
	 */
	protected void randomizeSelection() {
		for (Iterator en = this.selection.iterator();
			en.hasNext();
			) {
			PNode node = (PNode) en.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				n.randomize();
			} else if (node instanceof PNodeWeight) {
				PNodeWeight w = (PNodeWeight) node;
				w.randomize();
			}
		}
		renderObjects();
	}

	/**
	 * Increment selected objects (weights and neurons)
	 */
	public void incrementSelectedObjects() {
		ArrayList v =  getSelection();

		for (Iterator en = v.iterator(); en.hasNext();) {
			Object o = en.next();
			if (o instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) o;
				n.upArrow();
			}
			if (o instanceof PNodeWeight) {
				PNode n = (PNode) o;
				PNodeWeight w = (PNodeWeight) n;
				w.upArrow();
			}
		}
	}

	/**
	 * Decrement selected objects (weights and neurons)
	 */
	public void decrementSelectedObjects() {
		ArrayList v =  getSelection();

		for (Iterator en = v.iterator(); en.hasNext();) {
			Object o = en.next();
			if (o instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) o;
				n.downArrow();
			}
			if (o instanceof PNodeWeight) {
				PNode n = (PNode) o;
				PNodeWeight w = (PNodeWeight) n;
				w.downArrow();
			}
		}
	}

	/**
	 * Toggle input-status of selected neurons.  Change non-input neurons to input neurons
	 * and vice-versa
	 */
	protected void setInputs() {
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				if (((PNodeNeuron) pn).isInput()) {
					((PNodeNeuron) pn).setInput(false);
				} else {
					((PNodeNeuron) pn).setInput(true);
				}
			}
		}
		renderObjects();
	}
	/**
	 * Toggle output-status of selected neurons.  Change non-output neurons to output neurons
	 * and vice-versa
	 */
	protected void setOutputs() {
		Iterator i = selection.iterator();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				if (((PNodeNeuron) pn).isOutput()) {
					((PNodeNeuron) pn).setOutput(false);
				} else {
					((PNodeNeuron) pn).setOutput(true);
				}
			}
		}
		renderObjects();
	}

	/**
	 *  Show neuron or weight dialog.  If multiple nodes are selected show all of given type
	 * 
	 * @param theNode node for which to show dialog
	 */
	public void showPrefsDialog(PNode theNode) {
		PNode lastNode = getSingleSelection();
		if (theNode instanceof PNodeNeuron) {	
			showNeuronPrefs();
		} else if (theNode.getParent() instanceof PNodeWeight) {
			showWeightPrefs();
		}
	}
	

	/**
	 * Show dialog for weight settings
	 * 
	 * @param theWeight the weight which will be modified
	 */
	public void showWeightPrefs() {
		ArrayList synapses = getSelectedWeights();

		if(synapses.size() == 0) {
			PNode p = mouseEventHandler.getCurrentNode();
			if (p instanceof PNodeWeight)
				synapses.add(((PNodeWeight)p).getWeight());
			else return;
		}
		
		SynapseDialog theDialog = new SynapseDialog(synapses);
		theDialog.pack();
		theDialog.show();	
		
		if(!theDialog.hasUserCancelled())
		{
			theDialog.getValues();
		}
		renderObjects();
	}

	/**
	 * Show dialog for selected neurons
	 * 
	 * @param theNeuron the neuron which will be modified
	 */
	public void showNeuronPrefs() {

		ArrayList pnodes = getSelectedPNodeNeurons();
		
		//If no neurons are selected use the node that was clicked on
		if(pnodes.size() == 0) {
			PNode p = mouseEventHandler.getCurrentNode();
			if (p instanceof PNodeNeuron)
				pnodes.add(((PNodeNeuron)p).getNeuron());
			else return;
		}
		
		NeuronDialog theDialog = new NeuronDialog(pnodes);
		theDialog.pack();
		theDialog.show();	
		
		if(!theDialog.hasUserCancelled())
		{
			theDialog.commmitChanges();
		}
		renderObjects();
	}
	
	public void showWTADialog() {
		
		WTADialog dialog = new WTADialog(this);
		dialog.pack();
		dialog.show();
		if(!dialog.hasUserCancelled())
		{
			WinnerTakeAll wta = new WinnerTakeAll(dialog.getNumUnits());
			this.addNetwork(wta, dialog.getCurrentLayout());
		}
		renderObjects();
	}
	
	public void showHopfieldDialog() {
		
		HopfieldDialog dialog = new HopfieldDialog();
		dialog.pack();
		dialog.show();
		if(!dialog.hasUserCancelled())
		{
			Hopfield hop = new Hopfield(dialog.getNumUnits());
			this.addNetwork(hop, dialog.getCurrentLayout());
		}
		repaint();
	}
	
	public void showBackpropDialog() {
		
		BackpropDialog dialog = new BackpropDialog(this);
		dialog.pack();
		dialog.show();
		if(!dialog.hasUserCancelled())
		{
			Backprop bp = new Backprop();
			bp.setN_inputs(dialog.getNumInputs());
			bp.setN_hidden(dialog.getNumHidden());
			bp.setN_outputs(dialog.getNumOutputs());
			bp.defaultInit();
			this.addNetwork(bp, "Layers");
		}
		renderObjects();
	}
	
	public void alignHorizontal() {
		Iterator i = selection.iterator();
		double min = Double.MAX_VALUE;
		while(i.hasNext()){ 
			PNode node = (PNode) i.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				if (n.getYpos() < min) {
					min = n.getYpos();
				}
			} 
		}
		i = selection.iterator();		
		while(i.hasNext()){ 
			PNode node = (PNode) i.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				n.setYpos(min);
			} 
		}
		renderObjects();
	}
	
	public void alignVertical() {
		Iterator i = selection.iterator();
		double min = Double.MAX_VALUE;
		while(i.hasNext()){ 
			PNode node = (PNode) i.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				if (n.getXpos() < min) {
					min = n.getXpos();
				}
			} 
		}
		i = selection.iterator();		
		while(i.hasNext()){ 
			PNode node = (PNode) i.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				n.setXpos(min);
			} 
		}
		renderObjects();
	}
	
	public void spacingHorizontal() {
		Iterator i = selection.iterator();
		double min = Double.MAX_VALUE;
		double max = 0;
		while(i.hasNext()){
			PNode node = (PNode) i.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				if (n.getXpos() < min){
					min = n.getXpos();
				}if (n.getXpos() > max){
					max = n.getXpos();
				}
			}
		}
	}
	
	public void spacingVertical() {
		Iterator i = selection.iterator();
		double min = Double.MAX_VALUE;
		double max = 0;
		while(i.hasNext()){
			PNode node = (PNode) i.next();
			if (node instanceof PNodeNeuron) {
				PNodeNeuron n = (PNodeNeuron) node;
				if (n.getYpos() < min){
					min = n.getYpos();
				}if (n.getYpos() > max){
					max = n.getYpos();
				}
			}
		}
	}
	
	public void showBackpropTraining(Backprop bp) {
		
		BackpropTrainingDialog dialog = new BackpropTrainingDialog(this, bp);
		dialog.pack();
		dialog.show();
		renderObjects();
	
	}
	
	public void showNetworkPrefs() {

		NetworkDialog dialog = new NetworkDialog(this);
		dialog.pack();
		dialog.show();
		if(dialog.hasUserCancelled())
		{
			dialog.returnToDefault();
		} else {
			theSerializer.setUsingTabs(dialog.isUsingIndent());
		}
		renderObjects();
	}
	/**
	 * Secret test method--to be removed
	 */
	public void connectLayers() {

		JDialog theDialog = new LearnDialog(this.owner, this);
		theDialog.show();
	}

//	/**
//	 * Connect two layers of neurons using a weight matrix
//	 * 
//	 * @param src source layer
//	 * @param tar target layer
//	 * @param mat matrix of connections 
//	 */
//	public void connectLayers(
////		NeuronLayer src,
////		NeuronLayer tar,
////		double[][] mat) {
////		network.connectLayers(src, tar, mat);
////		this.updateWeights();
//
//	}

	/**
	 * Add selected neurons to a new layer object
	 */
	public void addLayer() {
		Iterator i = selection.iterator();
		Vector the_neurons = new Vector();
		while (i.hasNext()) {
			PNode pn = (PNode) i.next();
			if (pn instanceof PNodeNeuron) {
				the_neurons.add(((PNodeNeuron) pn).getNeuron());
			}
		}
		//network.addLayer((Collection) the_neurons);
	}

	////////////////////////////
	// Gauge handling methods //
	////////////////////////////

	/**
	 * Add a new gauge
	 */
	public void addGauge() {
		
		Gauge theGauge = new Gauge(5 + (gaugeList.size() % 4) * 330, 490, 300, 300);
		String newName = new String("Gauge " + (gaugeList.size() + 1));
		theGauge.setName(newName);
		theGauge.setUsingOnOff(true);
		theGauge.setUsingHotPoint(true);
		theGauge.setDefaultDir("." + NetworkSerializer.FS + "simulations" + NetworkSerializer.FS + "gauges");
		
        WindowAdapter windowAdapter = new WindowAdapter()
        {        		
            public void windowClosing(WindowEvent windowEvent)
            {
            			gaugeList.remove(gaugeList.size()-1);
            }  
            
        };
        
		theGauge.getGp().getTheFrame().addWindowListener(windowAdapter);
		
		
		//TODO: Make above directory a field of simulation?

		//		JFrame gaugeFrame = new JFrame(newName);
		//		Vector v = new Vector(network.getNeuronList());
		//		// By default gauge all neurons in the current network
		//		Gauge theGauge = new Gauge(newName, v);
		//		gaugeFrame.getContentPane().add(theGauge);
		//		gaugeFrame.setBounds(505 + gaugeList.size() * 130, 480, 300, 300);
		//		//Control where new gauges go
		//		gaugeFrame.show();

		gaugeList.add(theGauge);
		gaugedObjects.setSize(gaugeList.size());
		resetGauge(gaugeList.size()-1);
	}



	/**
	 * Extract the current state of the network relative to a gauge
	 * 
	 * @param index gauge to extract values from
	 * @return an array of doubles corresponding to the current state represented on the gauge
	 */
	protected double[] getNetworkState(int index) {

		if (gaugedObjects.get(index) == null) {
			System.out.println("Null pointer in Network.getNetworkState()");
			resetGauge(index);
		}

		ArrayList state_vars = (ArrayList)gaugedObjects.get(index);
		
		double ret[] = new double[state_vars.size()];

		Iterator it = state_vars.iterator();
		int i = 0;
		while (it.hasNext()) {
		//for (int i = 0; i < state_vars.size(); i++) {
			//Object o = state_vars.get(i);
			Object o = it.next();
			if (o instanceof Neuron) {
				//TODO: First two cases are only called in the default case.  Indicative of poor design for selection vector.  It should only be populated by PNodes...
				Neuron n = (Neuron) o;
				ret[i] = n.getActivation();
			} else if (o instanceof Synapse) {
				Synapse w = (Synapse) o;
				ret[i] = w.getStrength();
			} else if (o instanceof PNodeNeuron) {
				Neuron n = ((PNodeNeuron) o).getNeuron();
				ret[i] = n.getActivation();
			} else if (o instanceof PNodeWeight) {
				Synapse w = ((PNodeWeight) o).getWeight();
				ret[i] = w.getStrength();
			}
			i++;
		}
		return ret;
	}

	/**
	 * Sets which objects (nodes or weights) should be gauged
	 *
	 * @param i index of the gauge to set
	 * @param v objects (neurons and weights) to gauge
	 */
	protected void setGaugedObjects(int i, ArrayList v) {
		gaugedObjects.set(i, v.clone());
		((Gauge)gaugeList.get(i)).init(v.size());
		//((Gauge)gaugeList.get(i)).initProjector(); //TODO: Fix this!
	}
	
	/**
	 * Resets gauge so that it gauges default objects in the neural network
	 * (currently all neurons)
	 * 
	 * @param index index of the gauge to resent
	 */
	public void resetGauge(int index) {
		ArrayList v = new ArrayList(network.getFlatNeuronList());
		// By default gauge all neurons in the current network
		setGaugedObjects(index, v);
	}
	/**
	 * Reset all gauges so that they gauge a default set of objects (currently all neurons)
	 */
	public void resetGauges() {
		for (int i = 0; i < gaugeList.size(); i++) {
			resetGauge(i);
		}
	}

	/**
	 * Sets a gauge to gauge selected objects.
	 * Called when setGauge is called on the network menu (in Simulation)
	 *
	 * @param index index of the gauge to update.
	 */
	public void updateGauge(int index) {
		Gauge theGauge = (Gauge) gaugeList.get(index);
		//		if (theGauge != null) {
		//			System.out.println(
		//				"selection[" + index + "]" + "=" + selection.size());
		//			for (int i = 0; i < selection.size(); i++) {
		//				Object o = selection.get(i);
		//				System.out.print("Obj_" + i + " " + o.toString() + " -- ");
		//			}
		//			System.out.println("");
		setGaugedObjects(index, selection);
	}

	/////////////////////////////
	// Other Graphics Methods //
	/////////////////////////////

	//TODO: JAVA DOC
	public void repaint() {
		super.repaint();
		if ((network != null) && (nodeList != null) && (nodeList.size() > 1) && (cursorMode != PAN) && (isAutoZoom == true)) { centerCamera(); } 
	}
	/**
	 * Pans the camera to the origin of the canvas coordinate system
	 */
	public void centerCameraToScreenSize() {
		PCamera cam = this.getCamera();
		PBounds pb = new PBounds(0, 0, owner.getWidth(), owner.getHeight());
		cam.animateViewToCenterBounds(pb, true, 0);
	}

	/**
	 * Centers the neural network in the middle of the PCanvas
	 */
	public void centerCamera() {
		PCamera cam = this.getCamera();
		PBounds pb = getNetworkBounds();
		cam.animateViewToCenterBounds(pb, true, 0);
	}

	/**	  
	 * Helper method for centerCamera().  Gets the bounds of the neural network by
	 * computing the highest and lowest PNodes in each direction 
	 * 
	 * @return the bounds of the network
	 */
	public PBounds getNetworkBounds() {

		double x_low = 0;
		double x_hi = 0;
		double y_low = 0;
		double y_hi = 0;

		Iterator i = nodeList.iterator();

		PNode pn = (PNode) i.next();
		double x, y;
		Rectangle2D rec = null;

		//TODO: Make general
		if (pn instanceof PNodeNeuron) {
			x_hi = x_low = getGlobalCenterX((PNodeNeuron) pn);
			y_hi = y_low = getGlobalCenterY((PNodeNeuron) pn);
		}

		while (i.hasNext()) {
			pn = (PNode) i.next();

			if (pn instanceof PNodeNeuron) {
				x = getGlobalCenterX((PNodeNeuron) pn);
				y = getGlobalCenterY((PNodeNeuron) pn);

				if (x_low > x)
					x_low = x;
				if (x_hi < x)
					x_hi = x;
				if (y_low > y)
					y_low = y;
				if (y_hi < y)
					y_hi = y;
			}
		}

		//If there is just one neuron
		if (x_hi == x_low) {
			double SINGLE_BOUNDS = 40;
			PBounds ret =
				new PBounds(
					(float) (x_low - SINGLE_BOUNDS),
					(float) (y_low - SINGLE_BOUNDS),
					(float) (PNodeNeuron.neuronScale +  2 * SINGLE_BOUNDS),
					(float) (PNodeNeuron.neuronScale + 2 * SINGLE_BOUNDS));
			return ret;
		}

		//Normal case of more than one neuron
		double width = x_hi - x_low;
		double height = y_hi - y_low;
		double buffer = 25; // Extra space framing the network, useful when the camera is zoomed way in.
		double scale = .15; // Amount by which to expand the bounding box; a percentage of width and height
		PBounds ret =
			new PBounds(
				(float) (x_low - (width * scale) - buffer),
				(float) (y_low - (height * scale) - buffer),
				(float) (width + (2 * width * scale) + (2 * buffer)),
				(float) (height + (2 * height * scale) + (2 * buffer)));

		return ret;
	}

	/**
	 * Nudge selected object  
	 *
	 * @param offset_x amount to nudge in the x direction
	 * @param offset_y amount to nudge in the y direction
	 */
	protected void nudge(int offset_x, int offset_y) {
		Iterator it = getSelection().iterator();
		while (it.hasNext()) {
			PNode pn = (PNode) it.next();
			if (!(pn instanceof PNodeWeight)) {
				pn.offset(offset_x, offset_y);
			}

		}
		renderObjects();
	}

	/**
	 *  Calls render methods of PNodeNeurons and PNodeWeights before painting
	 */
	public synchronized void renderObjects() {
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode n = (PNode) i.next();
			if (n instanceof PNodeNeuron) {
				((PNodeNeuron) n).render();
				n.moveToFront();
			} else if (n instanceof PNodeWeight) {
				((PNodeWeight)n).render();
				((PNodeWeight)n).weightLine.moveToBack();
			} else if (n instanceof PNodeText) {
				
			}
		}
	}


	/**
	 * Reset everything without deleting any nodes or weights. Clear the gauges.  Unselect all.  Reset the time.  Used
	 * when reading in a new network.
	 */
	public void resetNetwork() {
		mouseEventHandler.unselectAll();
		network.setTime(0);
		timeLabel.setText("" + network.getTime());
		resetGauges();

	}

	/**
	 * @return true if auto-zooming is on, false otherwise
	 */
	public boolean isAutoZoom() {
		return isAutoZoom;
	}

	/**
	 * @param b true if auto-zooming is on, false otherwise
	 */
	public void setAutoZoom(boolean b) {
		isAutoZoom = b;
	}
	
	/**
	 * Print debug information to standard output
	 */
	public void debug() {

		System.out.println("---------- Network GUI Debug --------");		
		System.out.println("" + nodeList.size() + " nodes.");
		System.out.println("" + selection.size() + " selected nodes.");	

		System.out.println("\n---------- Neural Network Debug --------");				
		getNetwork().debug();
				
		Iterator i = getSelection().iterator();
		
		if (i.hasNext()) {
			 System.out.println("\n---------- Selected Neurons Debug--------"); 
		} 		
		while (i.hasNext()) {
			PNode n = (PNode)i.next();
			if (n instanceof PNodeNeuron) {
				((PNodeNeuron)n).getNeuron().debug();
			}
		}

	}
	

	/**
	 * @return refrence to the current world object
	 */
	public World getWorld() {
		return theWorld;
	}

	/**
	 * Resets all PNodes to graphics values,which may have been changed by the user
	 */
	public void resetGraphics() {
		Iterator i = nodeList.iterator();
		while (i.hasNext()) {
			PNode n = (PNode)i.next();
			if (n instanceof PNodeWeight) {
				((PNodeWeight)n).getWeightLine().setStrokePaint(PNodeLine.getLineColor());
			}
		}
	}

	/**
	 * @return Returns the theWorld.
	 */
	public World getTheWorld() {
		return theWorld;
	}
	/**
	 * @param theWorld The theWorld to set.
	 */
	public void setTheWorld(World theWorld) {
		this.theWorld = theWorld;
	}
	/**
	 * @return Returns the gaugedObjects.
	 */
	public Vector getGaugedObjects() {
		return gaugedObjects;
	}
	/**
	 * @param gaugedObjects The gaugedObjects to set.
	 */
	public void setGaugedObjects(Vector gaugedObjects) {
		this.gaugedObjects = gaugedObjects;
	}
	/**
	 * @return Returns the gaugeList.
	 */
	public ArrayList getGaugeList() {
		return gaugeList;
	}
	/**
	 * @param gaugeList The gaugeList to set.
	 */
	public void setGaugeList(ArrayList gaugeList) {
		this.gaugeList = gaugeList;
	}

	/**
	 * @param node_list The node_list to set.
	 */
	public void setNodeList(ArrayList node_list) {
		this.nodeList = node_list;
	}
	/**
	 * @param selection The selection to set.
	 */
	public void setSelection(ArrayList selection) {
		this.selection = selection;
	}
	
	/**
	 * Assign unique ids to PNodes; for serialization
	 *
	 */
	public void updateIds() {
		Iterator i = getNeuronList().iterator();
		while(i.hasNext()) {
			PNodeNeuron n = (PNodeNeuron)i.next();
			n.setId("p" + n.getNeuron().getId());
		}
	}
	
	/**
	 * Forwards results of mouseHandler method
	 * 
	 * @return the last point clicked on screen
	 */
	public Point2D getLastClicked() {
		return mouseEventHandler.getLastLeftClicked();
	}
	/**
	 * @return Returns the theSerializer.
	 */
	public NetworkSerializer getSerializer() {
		return theSerializer;
	}
	/**
	 * @return Returns the cursorMode.
	 */
	public int getCursorMode() {
		return cursorMode;
	}
}