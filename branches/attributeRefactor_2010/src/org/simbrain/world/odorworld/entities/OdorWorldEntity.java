package org.simbrain.world.odorworld.entities;

import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.simbrain.util.environment.SmellSource;
import org.simbrain.world.odorworld.OdorWorld;
import org.simbrain.world.odorworld.behaviors.Behavior;
import org.simbrain.world.odorworld.behaviors.StationaryBehavior;
import org.simbrain.world.odorworld.effectors.Effector;
import org.simbrain.world.odorworld.sensors.Sensor;

/**
 * Adapted and extended from From Developing Games in Java, by David Brackeen.
 */
public abstract class OdorWorldEntity {

    /**
     * Animation used to depict this object. If the animation has one frame this
     * is equivalent to just using a single image to represent it.
     */
    private Animation animation;  

    /** Name of this entity. */
    private String name;

    /** X Position. */
    protected float x;

    /** Y Position. */
    protected float y;

    /** X Velocity. */
    protected float dx = .05f;

    /** Y Velocity. */
    protected float dy = .05f;

    /** Back reference to parent parentWorld. */
    private OdorWorld parentWorld;

    /** Sensors. */
    private List<Sensor> sensors= new ArrayList<Sensor>();

    /** Effectors. */
    private List<Effector> effectors = new ArrayList<Effector>();

    /** Behavior. */
    protected Behavior behavior = new StationaryBehavior();

    /** Smell Source (if any). */
    private SmellSource smellSource;

    /** True if a collision occurred in the last time step. */
    private boolean collision;

    /** Enable sensors. If not the agent is "blind." */
    private boolean sensorsEnabled = true;

    /** Enable effectors.  If not the agent is "paralyzed. */
    private boolean effectorsEnabled = true;

    /** If true, show sensors. */
    private boolean showSensors = true;

    /**
     * Updates this OdorWorldEntity's Animation and its position based on the
     * velocity.
     */
    public abstract void update(final long elapsedTime);

    /**
     * Called before update() if the creature collided with a tile horizontally.
     */
    public void collideHorizontal() {
        behavior.collisionX();
        collision = true;
    }

    /**
     * Called before update() if the creature collided with a tile vertically.
     */
    public void collideVertical() {
        behavior.collissionY();
        collision = true;
    }

    /**
     * Construct an entity from an animation.
     *
     * @param animation animation to use.
     */
    public OdorWorldEntity(final Animation anim, OdorWorld world) {
        this.animation = anim;
        this.parentWorld = world;
        anim.start();
    }

    /**
     * Construct an odor world entity from a single image location.
     *
     * @param imageLocation the image location
     */
    public OdorWorldEntity(final String imageLocation, OdorWorld world) {
        this.animation = new Animation(imageLocation);
        this.parentWorld = world;
        animation.start();
    }

    /**
     * Construct an entity.
     *
     * @param world parent world of entity
     */
    public OdorWorldEntity(OdorWorld world) {
        this.parentWorld = world;
    }

    /**
     * Gets this OdorWorldEntity's current x position.
     */
    public float getX() {
        return x;
    }

    /**
     * Gets this OdorWorldEntity's current y position.
     */
    public float getY() {
        return y;
    }

    /**
     * Sets this OdorWorldEntity's current x position.
     */
    public void setX(final float x) {
        this.x = x;
    }

    /**
     * Sets this OdorWorldEntity's current y position.
     */
    public void setY(final float y) {
        this.y = y;
    }

    /**
     * Gets this OdorWorldEntity's width, based on the size of the current
     * image.
     */
    public int getWidth() {
        return animation.getImage().getWidth(null);
    }

    /**
     * Gets this OdorWorldEntity's height, based on the size of the current
     * image.
     */
    public int getHeight() {
        return animation.getImage().getHeight(null);
    }

    /**
     * Gets the horizontal velocity of this OdorWorldEntity in pixels per
     * millisecond.
     */
    public float getVelocityX() {
        return dx;
    }

    /**
     * Gets the vertical velocity of this OdorWorldEntity in pixels per
     * millisecond.
     */
    public float getVelocityY() {
        return dy;
    }

    /**
     * Sets the horizontal velocity of this OdorWorldEntity in pixels per
     * millisecond.
     */
    public void setVelocityX(final float dx) {
        this.dx = dx;
    }

    /**
     * Sets the vertical velocity of this OdorWorldEntity in pixels per
     * millisecond.
     */
    public void setVelocityY(final float dy) {
        this.dy = dy;
    }

    /**
     * Get the entity's name.
     *
     * @return entity's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the entity's name.
     *
     * @param string name for entity.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets this OdorWorldEntity's current image.
     */
    public Image getImage() {
        return animation.getImage();
    }

    /**
     * Get bounds, based on current image.
     *
     * @return bounds of this entity.
     */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, getWidth(), getHeight());
    }

    /**
     * Reduced bounds used for some entities, to improve the look of collisions
     * and blocking.
     *
     * TODO: This may not work well when shapes (not pixel images) are used.
     *
     * @return reduced bounds.
     */
    public Rectangle getReducedBounds() {
        Rectangle ret = getBounds();
        ret.grow(-getHeight() / 5, -getWidth() / 5);
        return ret;
    }

    /**
     * Add an effector.
     *
     * @param effector effector to add
     */
    public void addEffector(final Effector effector) {
        effectors.add(effector);
        parentWorld.fireEffectorAdded(effector);
    }

    /**
     * Add a sensor.
     *
     * @param sensor sensor to add
     */
    public void addSensor(final Sensor sensor) {
        sensors.add(sensor);
        parentWorld.fireSensorAdded(sensor);
    }

    /**
     * Apply impact of all effectors.
     */
    public void applyEffectors() {
        if (effectorsEnabled) {
            for (Effector effector : effectors) {
                effector.activate();
            }
        }
    }

    /**
     * Update all sensors.
     */
    public void updateSensors() {
        if (sensorsEnabled) {
            for (Sensor sensor : sensors) {
                sensor.update();
            }
        }
    }

    /**
     * @return the smellSource
     */
    public SmellSource getSmellSource() {
        return smellSource;
    }

    /**
     * @param smellSource the smellSource to set
     */
    public void setSmellSource(final SmellSource smellSource) {
        this.smellSource = smellSource;
        smellSource.setLocation(this.getLocation());
    }

    /**
     * @return the parentWorld
     */
    public OdorWorld getParentWorld() {
        return parentWorld;
    }
    
    /**
     * Returns the location of the center of this entity as a double array.
     *
     * @return center location of the entity.
     */
    public double[] getCenterLocation() {
        return new double[] { x + getWidth()/2, y + getHeight()/2 };
    }


	/**
     * Returns the location of the entity as a double array.
     *
     * @return location of the entity.
     */
    public double[] getLocation() {
        return new double[] { x, y };
    }

    /**
     * Set the location of this entity.
     *
     * @param x x coordinate
     * @param y y coordinate
     */
    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the animation associated with this entity
     */
    public Animation getAnimation() {
        return animation;
    }

    /**
     * @param animation the animation to set
     */
    public void setAnimation(final Animation animation) {
        this.animation = animation;
    }

    /**
     * Initialize the animation from stored image location(s).
     */
    public void postSerializationInit() {
        getAnimation().initializeImages(); //TODO
    }

    /**
     * @return the sensors
     */
    public List<Sensor> getSensors() {
        return sensors;
    }

    /**
     * @param sensors the sensors to set
     */
    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    /**
     * @return the effectors
     */
    public List<Effector> getEffectors() {
        return effectors;
    }

    /**
     * @param effectors the effectors to set
     */
    public void setEffectors(List<Effector> effectors) {
        this.effectors = effectors;
    }

    /**
     * @return true if a collision occurred
     */
    public boolean hasCollided() {
        return collision;
    }

    /**
     * @param collission the collision to set
     */
    public void setHasCollided(boolean collission) {
        this.collision = collission;
    }

    /**
     * @return the sensorsEnabled
     */
    public boolean isSensorsEnabled() {
        return sensorsEnabled;
    }

    /**
     * @param sensorsEnabled the sensorsEnabled to set
     */
    public void setSensorsEnabled(boolean sensorsEnabled) {
        this.sensorsEnabled = sensorsEnabled;
    }

    /**
     * @return the effectorsEnabled
     */
    public boolean isEffectorsEnabled() {
        return effectorsEnabled;
    }

    /**
     * @param effectorsEnabled the effectorsEnabled to set
     */
    public void setEffectorsEnabled(boolean effectorsEnabled) {
        this.effectorsEnabled = effectorsEnabled;
    }

    /**
     * @return the showSensors
     */
    public boolean isShowSensors() {
        return showSensors;
    }

    /**
     * @param showSensors the showSensors to set
     */
    public void setShowSensors(boolean showSensors) {
        this.showSensors = showSensors;
    }

    /**
     * Returns true if the entity is blocked from moving.
     *
     * @return true if blocked, false otherwise.
     */
    public boolean isBlocked() {
        if (getParentWorld().isObjectsBlockMovement()) {
            if (hasCollided()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the behavior
     */
    public Behavior getBehavior() {
        return behavior;
    }

    /**
     * @param behavior the behavior to set
     */
    public void setBehavior(Behavior behavior) {
        this.behavior = behavior;
    }

}