package org.simbrain.workspace.gui;

import javax.swing.JCheckBoxMenuItem;

import org.simbrain.workspace.ConsumingAttribute;
import org.simbrain.workspace.ProducingAttribute;
import org.simbrain.workspace.WorkspaceComponent;

/**
 * Packages an object with a jmenu item to make it easy to pass them along
 * through action events.
 * 
 */
public class CouplingMenuItem extends JCheckBoxMenuItem {

    /** The default serial version ID. */
    private static final long serialVersionUID = 1L;

    /** Reference to producing attribute. */
    private ProducingAttribute<?> producingAttribute = null;

    /** Reference to consuming attribute. */
    private ConsumingAttribute<?> consumingAttribute = null;

    /** Reference to a coupling container. */
    private WorkspaceComponent<?> component = null;

    /**
     * The type of menu item being created. These items can be used to draw
     * information from a single producer or consumer, or lists of either.
     */
    public enum EventType {
        /** Identifies a single consumer event. */
        SINGLE_PRODUCER,
        /** Identifies a single producer event. */
        SINGLE_CONSUMER,
        /** Identifies a single producer list event. */
        PRODUCER_LIST,
        /** Identifies a single consumer list event. */
        CONSUMER_LIST
    }

    /** The event type for this event. */
    private final EventType eventType;

    /**
     * Creates a new instance.
     * 
     * @param component The component that this menuItem belongs to.
     * @param type The type of event this menuItem should fire.
     */
    public CouplingMenuItem(final WorkspaceComponent<?> component, final EventType type) {
        this.component = component;
        this.eventType = type;
        setSelected(true);
    }

    /**
     * Creates a new instance as a single consumer item.
     * 
     * @param consumingAttribute The consuming attribute this meunItem is associated with.
     */
    public CouplingMenuItem(final ConsumingAttribute<?> consumingAttribute) {
        super(consumingAttribute.getAttributeDescription());
        this.eventType = EventType.SINGLE_CONSUMER;
        this.consumingAttribute = consumingAttribute;
        setSelected(true);
    }

    /**
     * Creates a new instance as a single producer item.
     * 
     * @param producingAttribute The producing attribute this meunItem is associated with.
     */
    public CouplingMenuItem(final ProducingAttribute<?> producingAttribute) {
        super(producingAttribute.getAttributeDescription());
        this.eventType = EventType.SINGLE_PRODUCER;
        this.producingAttribute = producingAttribute;
        setSelected(true);
    }

    /**
     * @return the consumingAttribute
     */
    public ConsumingAttribute<?> getConsumingAttribute() {
        return consumingAttribute;
    }

    /**
     * @param consumingAttribute the consumingAttribute to set
     */
    public void setConsumingAttribute(final ConsumingAttribute<?> consumingAttribute) {
        this.consumingAttribute = consumingAttribute;
    }

    /**
     * @return the producingAttribute
     */
    public ProducingAttribute<?> getProducingAttribute() {
        return producingAttribute;
    }

    /**
     * @param producingAttribute the producingAttribute to set
     */
    public void setProducingAttribute(final ProducingAttribute<?> producingAttribute) {
        this.producingAttribute = producingAttribute;
    }

    /**
     * @return the container
     */
    public WorkspaceComponent<?> getWorkspaceComponent() {
        return component;
    }

    /**
     * @return the eventType
     */
    public EventType getEventType() {
        return eventType;
    }
}
