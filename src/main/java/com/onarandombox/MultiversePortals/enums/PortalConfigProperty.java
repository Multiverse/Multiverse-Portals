/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.enums;

/**
 * Multiverse 2
 *
 * @author fernferret
 */
public enum PortalConfigProperty {
    wand, useonmove, portalsdefaulttonether, enforceportalaccess,
    portalcooldown, clearonremove, teleportvehicles;


    public static String getAllValues() {
        String buffer = "";
        for (PortalConfigProperty c : PortalConfigProperty.values()) {
            // All values will NOT Contain spaces.
            buffer += c.toString() + " ";
        }
        return buffer;
    }
}
