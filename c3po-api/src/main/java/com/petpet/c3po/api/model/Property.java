/*******************************************************************************
 * Copyright 2013 Petar Petrov <me@petarpetrov.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.petpet.c3po.api.model;

import com.petpet.c3po.api.model.helper.PropertyType;

import java.io.Serializable;

/**
 * A domain object encapsulating a property document.
 *
 * @author Petar Petrov <me@petarpetrov.org>
 */
public class Property implements Model, Serializable {

    /**
     * The id of the proeprty.
     */
    private String id;

    /**
     * The key of the property.
     */
    private String key;

    /**
     * The type of the property.
     */
    private String type;

    /**
     * A default constructor.
     */
    public Property() {

    }

    /**
     * Creates a property with the given key as key and id and sets the type to a
     * string.
     *
     * @param key the key of the property.
     */
    public Property(String key) {
        this.id = key;
        this.key = key;
        this.type = PropertyType.STRING.name();
    }

    /**
     * Creates a property with the given key as key and id and sets the type to
     * the given type. If the type is null, then STRING is used.
     *
     * @param key
     * @param type
     */
    public Property(String key, PropertyType type) {
        this(key);
        type = (type == null) ? PropertyType.STRING : type;
        this.type = type.name();
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Property other = (Property) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }

}
