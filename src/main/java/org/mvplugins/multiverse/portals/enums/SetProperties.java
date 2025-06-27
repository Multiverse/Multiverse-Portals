/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.enums;

import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.portals.MVPortal;

/**
 * @deprecated Use {@link MVPortal#getStringPropertyHandle()} instead to get property names.
 */
@Deprecated(since = "5.1", forRemoval = true)
@ApiStatus.ScheduledForRemoval(inVersion = "6.0")
public enum SetProperties {
    destination, dest, owner, loc, location, price, currency, curr, safe, telenonplayers, handlerscript
}
