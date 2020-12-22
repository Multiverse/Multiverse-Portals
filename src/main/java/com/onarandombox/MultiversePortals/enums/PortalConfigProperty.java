/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.enums;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Multiverse 2
 *
 * @author fernferret
 */
public final class PortalConfigProperty<T> {

    private static final Map<String, PortalConfigProperty<?>> propertyValues = new HashMap<>(7);

    public static final PortalConfigProperty<Integer> WAND = new PortalConfigProperty<>("wand", Integer.class);
    public static final PortalConfigProperty<Integer> PORTAL_COOLDOWN = new PortalConfigProperty<>("portalcooldown", Integer.class);
    public static final PortalConfigProperty<Boolean> USE_ON_MOVE = new PortalConfigProperty<>("useonmove", Boolean.class);
    public static final PortalConfigProperty<Boolean> PORTALS_DEFAULT_TO_NETHER = new PortalConfigProperty<>("portalsdefaulttonether", Boolean.class);
    public static final PortalConfigProperty<Boolean> ENFORCE_PORTAL_ACCESS = new PortalConfigProperty<>("enforceportalaccess", Boolean.class);
    public static final PortalConfigProperty<Boolean> CLEAR_ON_REMOVE = new PortalConfigProperty<>("clearonremove", Boolean.class);
    public static final PortalConfigProperty<Boolean> TELEPORT_VEHICLES = new PortalConfigProperty<>("teleportvehicles", Boolean.class);

    private final String name;
    private final Class<T> type;

    public PortalConfigProperty(String name, Class<T> type) {
        this.name = name;
        this.type = type;
        propertyValues.put(name, this);
    }

    public String getName() {
        return name;
    }

    public Class<T> getType() {
        return type;
    }

    public static PortalConfigProperty<?> getByName(String name) {
        return propertyValues.get(name.toLowerCase());
    }

    public static Collection<PortalConfigProperty<?>> values() {
        return propertyValues.values();
    }

    public static Collection<String> valueNames() {
        return propertyValues.keySet();
    }
}
