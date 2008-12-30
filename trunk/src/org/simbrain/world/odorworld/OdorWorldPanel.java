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
package org.simbrain.world.odorworld;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.entities.MovingEntity;
import org.simbrain.world.odorworld.entities.StaticEntity;
import org.simbrain.world.odorworld.entities.Wall;

/**
 * <b>OdorWorld</b> is the lowest-level environment panel which contains most of the world's "logic". Creature and
 * flower/food icons are drawn here. Movement of the mouse in response to clicks and (very  minimal) world-editing
 * features are also handled here.   Finally, the stimulus to the network is  calculated here, on the  basis of the
 * creature's distance from objects, as follows:
 *
 * <ul>
 * <li>
 * Get the vector of values, the "smell signature," associated with each object.
 * </li>
 * <li>
 * Scale this signature by the creature's distance fromm each object.
 * </li>
 * <li>
 * Use the sum of these scaled smell signatures as input to the creature's network.
 * </li>
 * </ul>
 */
public class OdorWorldPanel extends JPanel implements KeyListener {

    private static final long serialVersionUID = 1L;

    /** Reference to model world. */
    private OdorWorld world;

    /** Color of the world background. */
    private Color backgroundColor = Color.white;

    /** The boolean that turns on and off wall drawing behavior for the mouse. */
    private boolean drawingWalls = false;

    /** Point being dragged. */
    private Point draggingPoint;

    /** Entity currently selected. */
    private OdorWorldEntity selectedEntity = null;

    /** Selected point. */
    private Point selectedPoint;

    /** First point for wall. */
    private Point wallPoint1;

    /** Second point for wall. */
    private Point wallPoint2;

    /** Distance in x direction. */
    private int distanceX = 0;

    /** Distance in y direction. */
    private int distanceY = 0;

    /** Contains the world. */
    private OdorWorldDesktopComponent parentFrame;

    /** World menu. */
    private OdorWorldMenu menu;

    /** Whether world has been updated yet; used by thread. */
    private boolean updateCompleted;

    final ActionListener copyListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            WorldClipboard.copyItem(selectedEntity);
        }
    };
    
    final ActionListener cutListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            WorldClipboard.cutItem(selectedEntity, OdorWorldPanel.this);
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
    
    final ActionListener pasteListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            WorldClipboard.pasteItem(selectedPoint, OdorWorldPanel.this);
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
    
    final ActionListener clearAllListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            world.clearAllEntities();
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);   
        }
    };
    
    final ActionListener deleteListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            world.removeEntity(selectedEntity);
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
        
    final ActionListener addListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            world.addStaticEntity(new double[]{selectedPoint.x, selectedPoint.y});
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        } 
    };
    
    final ActionListener propsListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            showGeneralDialog();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
        
    final ActionListener objectPropsListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            showEntityDialog((StaticEntity) selectedEntity);
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
    
    int mouseCounter = 0;
    
    final ActionListener addAgentListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            MovingEntity mouse = new MovingEntity(world, 
                "Mouse " + (++mouseCounter), new double[]{selectedPoint.x, selectedPoint.y});            
            world.addEntity(mouse);
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
        
    final ActionListener wallListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            drawingWalls = true;
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };
        
    final ActionListener wallPropsListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            showWallDialog((Wall) selectedEntity);
            getParentFrame().repaint();
            getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
        }
    };

    /**
     * Default constructor.
     */
    public OdorWorldPanel() {
    }

    /**
     * Construct a world, set its background color.
     * @param wf the frame in which this world is rendered
     */
    public OdorWorldPanel(final OdorWorldDesktopComponent parent) {
        parentFrame = parent;
        world = parent.getWorkspaceComponent().getWorld();

        setBackground(backgroundColor);
        this.addMouseListener(mouseListener);
        this.addMouseMotionListener(mouseDraggedListener);
        this.addKeyListener(this);
        this.setFocusable(true);

        menu = new OdorWorldMenu(this);

        menu.initMenu();
    }



    private final MouseListener mouseListener = new MouseAdapter() {

        /**
         * Task to perform when mouse button is pressed.
         * @param mouseEvent Mouse event
         */
        public void mousePressed(final MouseEvent mouseEvent) {

            // Select Entity
            selectedEntity = null;
            selectedPoint = mouseEvent.getPoint();
            for (int i = 0; (i < world.getEntityList().size()) && (selectedEntity == null); i++) {
                final OdorWorldEntity entity = (OdorWorldEntity) world.getEntityList().get(i);
                if (entity.getBounds().contains(selectedPoint)) {
                    selectedEntity = entity;
                }
            }

            if (selectedEntity != null) {
                distanceX = (int) selectedEntity.getLocation()[0] - mouseEvent.getPoint().x;
                distanceY = (int) selectedEntity.getLocation()[1] - mouseEvent.getPoint().y;
                if (selectedEntity instanceof MovingEntity) {
                    world.setCurrentCreature((MovingEntity) selectedEntity);	
                }
            }

            //submits point for wall drawing
            if (drawingWalls) {
                mouseEvent.getPoint();
                setWallPoint1(selectedPoint);
            }

            //Show popupmenu for right click
            if (mouseEvent.isControlDown() || (mouseEvent.getButton() == MouseEvent.BUTTON3)) {
                final JPopupMenu menu = buildPopupMenu(selectedEntity);
                menu.show(OdorWorldPanel.this, (int) selectedPoint.getX(), (int) selectedPoint.getY());
            } else if (mouseEvent.getClickCount() == 2) { //open dialogue for that world-item
                if (selectedEntity instanceof Wall) {
                    showWallDialog((Wall) selectedEntity);
                } else {
                    showEntityDialog(selectedEntity);
                }

                getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
            }

            final java.awt.Container container = getParent().getParent();
            container.repaint();
        }

        /**
         * Task to perform when mouse button is released.
         * @param mouseEvent Mouse event
         */
        public void mouseReleased(final MouseEvent mouseEvent) {
            if (drawingWalls) {
                setWallPoint2(mouseEvent.getPoint());
                world.addWall();
                draggingPoint = null;
                getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
            }
        }
    };

    private final MouseMotionListener mouseDraggedListener = new MouseMotionAdapter() {
        /**
         * Task to perform when mouse button is held and mouse moved.
         * @param e Mouse event
         */
        public void mouseDragged(final MouseEvent e) {

        	if (drawingWalls) {
                draggingPoint = e.getPoint();
                repaint();
            }

            final Point test = new Point(e.getPoint().x + distanceX, e.getPoint().y + distanceY);
            if ((selectedEntity != null) && getBounds().intersects((selectedEntity.getBounds()))) {
                selectedEntity.setLocation(new double[]{test.x, test.y});
                repaint();
                getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
            }
        }
    };

    /**
     * Task to perform when keyboard button is released.
     * @param k Keyboard event.
     */
    public void keyReleased(final KeyEvent k) {
    }

    /**
     * Task to perform when keyboard button is typed.
     * @param k Keyboard event.
     */
    public void keyTyped(final KeyEvent k) {
    }

    /**
     * Task to perform when keyboard button is pressed.
     * @param k Keyboard event.
     */
    public void keyPressed(final KeyEvent k) {
        if (k.getKeyCode() == KeyEvent.VK_SPACE) {
            //this.fireWorldChanged();
        }

        //TODO!

        if (k.getKeyCode() == KeyEvent.VK_UP) {
            world.getCurrentCreature().moveStraight();
        }// else if (k.getKeyCode() == KeyEvent.VK_DOWN) {
//            world.getCurrentCreature().goStraightBackward(1);
//        } else if (k.getKeyCode() == KeyEvent.VK_RIGHT) {
//            world.getCurrentCreature().turnRight(OdorWorld.manualMotionTurnIncrement);
//        } else if (k.getKeyCode() == KeyEvent.VK_LEFT) {
//            world.getCurrentCreature().turnLeft(OdorWorld.manualMotionTurnIncrement);
//        } else if ((k.getKeyCode() == KeyEvent.VK_DELETE) || (k.getKeyCode() == KeyEvent.VK_BACK_SPACE)) {
//            world.removeEntity(selectedEntity);
//            this.getParentFrame().repaint();
//        }

        if (k.getKeyCode() != KeyEvent.VK_SPACE) {
            //this.fireWorldChanged();
        }

        repaint();
        this.getParentFrame().getWorkspaceComponent().setChangedSinceLastSave(true);
    }

    /**
     * passed two points, determineUpperLeft returns the upperleft point of the rect. they form
     *
     * @param p1 the first point
     * @param p2 the second point
     *
     * @return the point which is the upperleft of the rect.
     */
    private Point determineUpperLeft(final Point p1, final Point p2) {
        final Point temp = new Point();

        if (p1.x < p2.x) {
            temp.x = p1.x;
        } else if (p1.x >= p2.x) {
            temp.x = p2.x;
        }

        if (p1.y < p2.y) {
            temp.y = p1.y;
        } else if (p1.y >= p2.y) {
            temp.y = p2.y;
        }

        return temp;
    }

    /**
     * Paints graphical component.
     * @param g Graphic to paint
     */
    @Override
    public void paintComponent(final Graphics g) {
        super.paintComponent(g);
        paintWorld(g);
    }

    /**
     * Paint all the objects in the world.
     *
     * @param g Reference to the world's graphics object
     */
    public void paintWorld(final Graphics g) {

        for(OdorWorldEntity entity : world.getEntityList()) {
        	if (entity.getLifeCycleObject() != null) {
        		if (entity.getLifeCycleObject().isDead()) {
        			continue;
        		}
        	}
        	entity.paintEntity(this, g);
        }
 
        g.setColor(Color.WHITE);
        setBackground(backgroundColor);

        // For editing walls.  TODO: Move to wall class.
        if (drawingWalls && (draggingPoint != null)) {
            final Point upperLeft = determineUpperLeft(getWallPoint1(), draggingPoint);
            final int width = Math.abs(getWallPoint1().x - draggingPoint.x);
            final int height = Math.abs(getWallPoint1().y - draggingPoint.y);
            g.setColor(Color.BLACK);
            g.drawRect(upperLeft.x, upperLeft.y, width, height);
        }
    }

    /**
     * Call up a {@link DialogOdorWorldEntity} for a world object nearest to a specified point.
     *
     * @param theEntity the non-creature entity closest to this point will have a dialog called up
     */
    public void showEntityDialog(final OdorWorldEntity theEntity) {
//    	
//        DialogOdorWorldEntity theDialog = null;
//
//        if (theEntity != null) {
//            theDialog = new DialogOdorWorldEntity(theEntity);
//            theDialog.pack();
//            theDialog.setLocationRelativeTo(null);
//            theDialog.setVisible(true);
//
//            if (!theDialog.hasUserCancelled()) {
//                theDialog.commitChanges();
//
//                //TODO!
////                if (theEntity instanceof OdorWorldAgent) {
////                    theDialog.getStimPanel().commitChanges();
////                    theDialog.getAgentPanel().commitChanges();
////                    theDialog.commitChanges();
////                } else {
////                    theDialog.getStimPanel().commitChanges();
////                    theDialog.commitChanges();
////                }
//            }
//
//            repaint();
//        }
    }

    /**
     * Shows the wall properties dialog box.
     * @param theWall Wall for which to set properties
     */
    public void showWallDialog(final Wall theWall) {
        DialogOdorWorldWall theDialog = null;

        theDialog = new DialogOdorWorldWall(this, theWall);
        theDialog.pack();
        theDialog.setVisible(true);

        // TODO
        if (!theDialog.hasUserCancelled()) {
//            theDialog.getStimPanel().commitChanges();
            theDialog.commitChanges();
        }

        repaint();
    }

    /**
     * Shows the general world preferences dialog.
     */
    public void showGeneralDialog() {
        final DialogOdorWorld theDialog = new DialogOdorWorld(this);
        theDialog.pack();
        theDialog.setLocationRelativeTo(null);
        theDialog.setVisible(true);

        if (!theDialog.hasUserCancelled()) {
            theDialog.setValues();
        }

        repaint();
    }

    /**
     * @return The selected abstract entity.
     */
    public OdorWorldEntity getSelectedEntity() {
        return selectedEntity;
    }

    /**
     * Create a popup menu based on location of mouse click.
     *
     * @param theEntity the entity for which to build the menu
     * @return the popup menu
     */
    public JPopupMenu buildPopupMenu(final OdorWorldEntity theEntity) {
        final JPopupMenu ret = new JPopupMenu();

        ret.add(menu.getCopyItem());
        ret.add(menu.getCutItem());
        ret.add(menu.getDeleteItem());

        if (theEntity instanceof StaticEntity) {
            ret.addSeparator();
            ret.add(menu.getObjectPropsItem());
        } else if (theEntity instanceof Wall) {
            ret.addSeparator();
            ret.add(menu.getWallPropsItem());
        } else {
            if (WorldClipboard.getClipboardEntity() != null) {
                ret.add(menu.getPasteItem());
                ret.addSeparator();
            }

            ret.add(menu.getAddItem());
            ret.add(menu.getAddAgentItem());
            ret.add(menu.getWallItem());
        }

        ret.addSeparator();
        ret.add(menu.getPropsItem());

        return ret;
    }

    /**
     * @return Returns the parentFrame.
     */
    public OdorWorldDesktopComponent getParentFrame() {
        return parentFrame;
    }

    /**
     * @param parentFrame The parentFrame to set.
     */
    public void setParentFrame(final OdorWorldDesktopComponent parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * @return Background color of world.
     */
    public int getBackgroundColor() {
        return backgroundColor.getRGB();
    }

    /**
     * Sets the background color of the world.
     * @param backgroundColor Color
     */
    public void setBackgroundColor(final int backgroundColor) {
        this.backgroundColor = new Color(backgroundColor);
    }

    /**
     * @param wallPoint1 The wallPoint1 to set.
     */
    private void setWallPoint1(final Point wallPoint1) {
        this.wallPoint1 = wallPoint1;
    }

    /**
     * @return Returns the wallPoint1.
     */
    private Point getWallPoint1() {
        return wallPoint1;
    }

    /**
     * @param wallPoint2 The wallPoint2 to set.
     */
    private void setWallPoint2(final Point wallPoint2) {
        this.wallPoint2 = wallPoint2;
    }

    /**
     * @return Returns the wallPoint2.
     */
    private Point getWallPoint2() {
        return wallPoint2;
    }

    /**
     * Used by script thread to ensure that an update cycle is complete before
     * updating again.
     *
     * @return whether the world has been updated or not
     */
    public boolean isUpdateCompleted() {
        return updateCompleted;
    }

    /**
     * Used by script thread to ensure that an update cycle is complete before
     * updating again.
     *
     * @param b whether the world has been updated or not.
     */
    public void setUpdateCompleted(final boolean b) {
        updateCompleted = b;
    }

    /**
     * @return the world
     */
    public OdorWorld getWorld() {
        return world;
    }

    /**
     * @param world the world to set
     */
    public void setWorld(OdorWorld world) {
        this.world = world;
    }
}