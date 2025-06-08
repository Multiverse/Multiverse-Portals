/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.enums;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.config.handle.StringPropertyHandle;
import org.mvplugins.multiverse.portals.config.PortalsConfig;

/**
 * Multiverse 2
 *
 * @author fernferret
 *
 * @deprecated Use {@link PortalsConfig#getStringPropertyHandle()} instead.
 */
@Deprecated(since = "5.1", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0")
public enum PortalConfigProperty {
    wand, useonmove, portalsdefaulttonether, enforceportalaccess,
    portalcooldown, clearonremove, teleportvehicles;


    /**
     * @deprecated Use {@link PortalsConfig#getStringPropertyHandle()} and {@link StringPropertyHandle#getAllPropertyNames()} instead.
     */
    @Deprecated(since = "5.1", forRemoval = true)
    @ApiStatus.ScheduledForRemoval(inVersion = "6.0")
    public static String getAllValues() {
        String buffer = "";
        for (PortalConfigProperty c : PortalConfigProperty.values()) {
            // All values will NOT Contain spaces.
            buffer += c.toString() + " ";
        }
        return buffer;
    }
}
