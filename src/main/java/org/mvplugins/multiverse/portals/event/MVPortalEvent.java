/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.enums.PortalType;

/**
 * Multiverse 2
 *
 * @author fernferret
 */
public class MVPortalEvent extends Event implements Cancellable {
    private Player teleportee;
    private MVPortal sendingPortal;
    private DestinationInstance<?, ?> destination;
    private boolean isCancelled;

    public MVPortalEvent(DestinationInstance<?, ?> destination, Player teleportee, MVPortal sendingPortal) {
        this.teleportee = teleportee;
        this.destination = destination;
        this.sendingPortal = sendingPortal;
    }

    public MVPortalEvent(DestinationInstance<?, ?> destination, Player teleportee) {
        this(destination, teleportee, null);
    }

    private static final HandlerList HANDLERS = new HandlerList();

    /**
     * {@inheritDoc}
     */
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the handler list. This is required by the event system.
     * @return A list of HANDLERS.
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Returns the player who will be teleported by this event.
     *
     * @return The player who will be teleported by this event.
     */
    public Player getTeleportee() {
        return this.teleportee;
    }

    /**
     * Returns the location the player was before the teleport.
     *
     * @return The location the player was before the teleport.
     */
    public Location getFrom() {
        return this.teleportee.getLocation();
    }

    /**
     * Returns the destination that the player will spawn at.
     *
     * @return The destination the player will spawn at.
     */
    public DestinationInstance<?, ?> getDestination() {
        return this.destination;
    }

    /**
     * Returns the type of portal that was used.
     *
     * This will be Legacy for MV1 style portals and Normal for Portals that use the swirly purple goo.
     *
     * @return The {@link PortalType} of the sending portal.
     * @throws IllegalStateException If this portal's location is no longer valid.
     * @deprecated Use {@link MVPortal#getPortalType()} instead (i.e. {@code getSendingPortal().getPortalType()}).
     */
    @Deprecated
    public PortalType getPortalType() throws IllegalStateException {
        return this.getSendingPortal().getPortalType();
    }

    /**
     * Returns the Portal sending the player
     *
     * @return The portal the player is sent from
     */
    public MVPortal getSendingPortal() {
        return this.sendingPortal;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }
}
