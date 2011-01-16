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
package org.simbrain.workspace;

/**
 * Objects of this class contain everything necessary to produce a particular
 * consumer or producer, as well as information used in displaying the potential
 * attribute (for GUI elements which manage coupling creation).
 *
 * @author jyoshimi
 */
public class PotentialAttribute {

    /** Parent workspace component. */
    private WorkspaceComponent parent;

    /** Potential producing or consuming object. */
    private Object baseObject;

    /** Name of the method that sets or gets the attribute. */
    private String methodName;

    /** The data type (double, string, etc) of a consumer or producer. */
    private Class<?> dataType;

    /** Description of this type of potential attribute. */
    private String description;

    /**
     * Construct a potential attribute.
     *
     * @param parent parent workspace component
     * @param methodName method name
     * @param dataType class of data
     */
    protected PotentialAttribute(WorkspaceComponent parent, Object object, 
            String methodName, Class<?> dataType, String description) {
        this.parent = parent;
        this.baseObject = object;
        this.methodName = methodName;
        this.dataType = dataType;
        this.description = description;
    }

    /**
     * Actualize this potential attribute into a producer.
     *
     * @return the producer corresponding to this potential attribute.
     */
    public Producer<?> createProducer() {
        return parent.getAttributeManager().createProducer(this);
    }

    /**
     * Actualize this potential attribute into a consumer.
     *
     * @return the consumer corresponding to this potential attribute.
     */
    public Consumer<?> createConsumer() {
        return parent.getAttributeManager().createConsumer(this);
    }

    /**
     * Returns a description of this potential attribute; used in GUI.
     *
     * @return a description of this attribute
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the parent
     */
    public WorkspaceComponent getParent() {
        return parent;
    }

    /**
     * @return the methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * @return the attributeType
     */
    public Class<?> getDataType() {
        return dataType;
    }

    /**
     * @return the object
     */
    public Object getBaseObject() {
        return baseObject;
    }

}
