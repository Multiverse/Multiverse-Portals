/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.utils;

import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;

public class PortalProperty<T> {

    private final PortalConfigProperty<T> property;
    private final T value;

    public PortalProperty(PortalConfigProperty<T> property, T value) {
        this.property = property;
        this.value = value;
    }

    public PortalConfigProperty<T> getProperty() {
        return property;
    }

    public T getValue() {
        return value;
    }
}
