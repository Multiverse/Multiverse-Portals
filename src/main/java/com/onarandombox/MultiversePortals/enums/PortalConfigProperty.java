/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.enums;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Multiverse 2
 *
 * @author fernferret
 */
public enum PortalConfigProperty {
    wand, useonmove, portalsdefaulttonether, enforceportalaccess,
    portalcooldown, clearonremove, teleportvehicles;

    private static final String stringValues;

    static {
        StringBuilder buffer = new StringBuilder();

        Arrays.stream(PortalConfigProperty.values())
                .map(Enum::toString)
                .forEach(prop -> buffer.append(prop).append(' '));

        stringValues = buffer.toString();
    }

    public static String getAllValues() {
        return stringValues;
    }
}
