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

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import org.simbrain.world.odorworld.effectors.Effector;
import org.simbrain.world.odorworld.entities.Animation;
import org.simbrain.world.odorworld.entities.OdorWorldEntity;
import org.simbrain.world.odorworld.sensors.Sensor;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Core model class of Odor World, which contains a list of entities in the world.
 * 
 * Some code from Developing Games in Java, by David Brackeen.
 */
public class OdorWorld {

    /** List of odor world entities. */
    private List<OdorWorldEntity> entityList = new ArrayList<OdorWorldEntity>();
    
    /** Listeners on this odor world. */
    private List<WorldListener> listenerList = new ArrayList<WorldListener>();

    /** Tile map. */
    private TileMap map;

    /** Point cache used in collision detection. */
    private Point pointCache = new Point();
    
    /** Renderer for this world. */
    private OdorWorldRenderer renderer;
    
    /** Whether or not sprites wrap around or are halted at the borders */
    //private boolean wrapAround = true;

    /**
     * Default constructor.
     */
    OdorWorld() {
        renderer = new OdorWorldRenderer();
        map = new TileMap(8, 8);
        
        //renderer.setBackground(ResourceManager.getImage("dirt.jpg"));
        //map.setTile(1, 2, ResourceManager.getImage("Tulip.gif"));
        //map.setTile(3, 3, ResourceManager.getImage("Tulip.gif"));
        //map.setTile(5, 5, ResourceManager.getImage("Tulip.gif"));
    }
    
    /**
     * Update world.
     */
    public void update() {
        for (OdorWorldEntity object : entityList) {
            object.updateSensors();
            object.applyEffectors();
            updateSprite(object, 1); // time defaults to 1 now
        }
        fireUpdateEvent();
    }

    /**
     * Add an Odor World Entity.
     *
     * @param entity the entity to add
     */
    public void addEntity(final OdorWorldEntity entity) {
        // TODO: As usual, need a system for naming things..
        entity.setName(entity.getClass().getSimpleName() + "-" + (entityList.size()+1));
        
        entity.setParentWorld(this);
        
        //centerSprite(sprite, tileX,tileY);

        // Add entity to the map
        map.addSprite(entity);
        entityList.add(entity);
        
        // Fire entity added event
        fireEntityAdded(entity);
    }

//    /**
//     * Add an odor world entity.
//     * 
//     * @param entity entity to add
//     * @param tileX x position of entity
//     * @param tileY y position of entity
//     */
//    private void addEntity(final OdorWorldEntity entity, final int tileX, final int tileY) {
//
//            entity.setX(tileX);
//            entity.setY(tileY);
//            entity.setVelocityX( -1 + (float) Math.random() * 2);
//            entity.setVelocityY(-1 + (float) Math.random() * 2);
//            addEntity(entity);
//    }
    
//    //TODO: way to add an entity with an animation
//
//    /**
//     * Add a basic entity at specified point.
//     * 
//     * @param p the location where the object should be added
//     */
//    public void addBasicEntity(final int x, final int y, final String imageName) {
//        
//        Animation anim = new Animation(imageName);
//        //animation.addFrame(ResourceManager.getImage("Mouse_0.gif"), 150);
//        //animation.addFrame(ResourceManager.getImage("Mouse_345.gif"), 150);
//
//        BasicEntity entity = new BasicEntity(this, anim);
//        addEntity(entity, x, y);
//        fireUpdateEvent();
//    }
//    
//    /**
//     * Currently mouse is the only option!
//     */
//    public void addRotatingEntity(final double[] p) {
//        RotatingEntity entity = new RotatingEntity(this);
//        entity.addEffector(new RotationEffector(entity));
//        entity.addSensor(new SmellSensor(entity));
//        addEntity(entity, (int) p[0], (int) p[1]);
//        fireUpdateEvent();
//    }
    
    /**
     * Returns a properly initialized xstream object.
     * @return the XStream object
     */
    static XStream getXStream() {
        XStream xstream = new XStream(new DomDriver());
        xstream.omitField(OdorWorld.class, "listenerList");
        xstream.omitField(Animation.class, "frames");
        return xstream;
    }
    
    /**
     * Standard method call made to objects after they are deserialized.
     * See:
     * http://java.sun.com/developer/JDCTechTips/2002/tt0205.html#tip2
     * http://xstream.codehaus.org/faq.html
     * 
     * @return Initialized object.
     */
    private Object readResolve() {
        listenerList = new ArrayList<WorldListener>();
        for (OdorWorldEntity entity : entityList) {
            entity.postSerializationInit();
        }
        return this;
    }

    /**
     * Updates the creature.
     * 
     * TODO: to be rewritten
     */
    private void updateSprite(final OdorWorldEntity sprite, final long elapsedTime) {

        // Collision detection
        float dx = sprite.getVelocityX();
        float oldX = sprite.getX();
        float newX = oldX + dx * elapsedTime;
        float dy = sprite.getVelocityY();
        float oldY = sprite.getY();
        float newY = oldY + dy * elapsedTime;

        // Handle tile collisions
        //        Point tile = getTileCollision(creature, newX, creature.getY());
        //        if (tile != null) {
        //            // Line up with the tile boundary
        //            if (dx > 0) {
        //                creature.setX(
        //                    OdorWorldRenderer.tilesToPixels(tile.x) -
        //                    creature.getWidth());
        //            }
        //            else if (dx < 0) {
        //                creature.setX(
        //                    OdorWorldRenderer.tilesToPixels(tile.x + 1));
        //            }
        //        }
        //        tile = getTileCollision(creature, creature.getX(), newY);
        //        if (tile != null) {
        //            // Line up with the tile boundary
        //            if (dx > 0) {
        //                creature.setY(
        //                    OdorWorldRenderer.tilesToPixels(tile.y) -
        //                    creature.getHeight());
        //            }
        //            else if (dx < 0) {
        //                creature.setY(
        //                    OdorWorldRenderer.tilesToPixels(tile.y + 1));
        //            }
        //        }

        // Handle sprite collisions 
        if (xCollission(sprite, newX)) {
            sprite.collideHorizontal();
        } else {
            //sprite.setX(newX);
        }
        if (yCollission(sprite, newY)) {
            sprite.collideVertical();
        } else {
            //sprite.setY(newY);
        }

        //        if (wrapAround) {
        //            if (creature.getX() >= worldWidth) {
        //                creature.setX(creature.getX() - worldWidth);
        //            }
        //            if (creature.getX() < 0) {
        //                creature.setX(creature.getX() + worldWidth);
        //            }
        //            if (creature.getY() >= worldHeight) {
        //                creature.setY(creature.getY() - worldHeight);
        //            }
        //            if (creature.getY() < 0) {
        //                creature.setY(creature.getY() + worldHeight);
        //            }
        //        }

        // Update creature
        sprite.update(elapsedTime);
        
        //System.out.println("x: " + creature.getX() + " y:" + creature.getY());
}
    
    
    /**
     * Handle collisions in x directions.
     * 
     * @param entityToCheck
     * @param xCheck position to check
     * @return whether or not a collision occurred.
     */
    private boolean xCollission(OdorWorldEntity entityToCheck, float xCheck) {
        
        // Hit a wall
        if ((entityToCheck.getX() < 0) || (entityToCheck.getX() > getWidth())) {
            return true;
        }
        
        // Check for collisions with sprites
        for (OdorWorldEntity sprite : entityList) {
            
            if (sprite == entityToCheck) {
                continue;
            }
            
            if ((entityToCheck.getX() > sprite.getX()) &&
                    (entityToCheck.getX() < (sprite.getX() + sprite.getWidth()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handle collisions in y directions.
     * 
     * @param entityToCheck
     * @param yCheck position to check
     * @return whether or not a collision occurred.
     */
    private boolean yCollission(OdorWorldEntity entityToCheck, float yCheck) {
        // Hit a wall
        if ((entityToCheck.getY() < 0) || (entityToCheck.getY() > getHeight())) {
            return true;
        }
        // Check for collisions with sprites
        for (OdorWorldEntity sprite : entityList) {
            
            if (sprite == entityToCheck) {
                continue;
            }
            
            if ((entityToCheck.getY() > sprite.getY()) &&
                    (entityToCheck.getY() < (sprite.getY() + sprite.getHeight()))) {
                return true;
            }
        }
        return false;
    }

    
    /**
     * Gets the tile that a Sprites collides with. Only the OdorWorldEntity's X or Y
     * should be changed, not both. Returns null if no collision is detected.
     */
    public Point getTileCollision(OdorWorldEntity sprite, float newX, float newY) {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // Get the tile locations
        int fromTileX = OdorWorldRenderer.pixelsToTiles(fromX);
        int fromTileY = OdorWorldRenderer.pixelsToTiles(fromY);
        int toTileX = OdorWorldRenderer
                .pixelsToTiles(toX + sprite.getWidth() - 1);
        int toTileY = OdorWorldRenderer.pixelsToTiles(toY + sprite.getHeight()
                - 1);

        // Check each tile for a collision
        for (int x = fromTileX; x <= toTileX; x++) {
            for (int y = fromTileY; y <= toTileY; y++) {
                if (x < 0 || x >= map.getWidth() || map.getTile(x, y) != null) {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // No collision found
        return null;
    }

    /**
     * Render the world.  Forwarded to renderer.
     *
     * @param g graphics object.
     * @param screenWidth width of screen
     * @param screenHeight height of screen
     */
    public void draw(Graphics2D g, int screenWidth, int screenHeight) {
        renderer.draw(g, map, screenWidth, screenHeight);
    }

    /**
     * Add a world listener.
     *
     * @param listener listener to add.
     */
    public void addListener(WorldListener listener) {
        listenerList.add(listener);
    }

    /**
     * Returns the list of entities.
     *
     * @return the entity list
     */
    public List<OdorWorldEntity> getObjectList() {
        return entityList;
    }

    /**
     * Returns width of world in pixels.
     *
     * @return width in pixels.
     */
    public int getWidth() {
        return OdorWorldRenderer.tilesToPixels(map.getWidth());
    }
    
    /**
     * Returns height of world in pixels.
     *
     * @return height of world
     */
    public int getHeight() {
        return OdorWorldRenderer.tilesToPixels(map.getHeight());
    }

    /**
     * Fire entity added event.
     *
     * @param entity entity that was added
     */
    public void fireEntityAdded(final OdorWorldEntity entity) {
        for (WorldListener listener : listenerList) {
            listener.entityAdded(entity);
        }
    }
    
    /**
     * Fire entity removed event.
     *
     * @param entity entity that was removed
     */
    public void fireEntityRemoved(final OdorWorldEntity entity) {
        for (WorldListener listener : listenerList) {
            listener.entityRemoved(entity);
        }
    }
    
    /***
     * Fire sensor added event.
     *
     * @param sensor sensor that was added
     */
    public void fireSensorAdded(final Sensor sensor) {
        for (WorldListener listener : listenerList) {
            listener.sensorAdded(sensor);
        }
    }

    /**
     * Fire sensor removed event.
     *
     * @param sensor sensor that was removed
     */
    public void fireSensorRemoved(final Sensor sensor) {
        for (WorldListener listener : listenerList) {
            listener.sensorRemoved(sensor);
        }
    }

    /**
     * Fire effector added event.
     *
     * @param effector effector that was added
     */
    public void fireEffectorAdded(final Effector effector) {
        for (WorldListener listener : listenerList) {
            listener.effectorAdded(effector);
        }
    }

    /**
     * Fire effector removed event.
     *
     * @param effector effector that was removed
     */
    public void fireEffectorRemoved(final Effector effector) {
        for (WorldListener listener : listenerList) {
            listener.effectorRemoved(effector);
        }
    }

    /**
     * Fire an update event.
     */
    public void fireUpdateEvent() {
        for (WorldListener listener : listenerList) {
            listener.updated();
        }
    }
    
}