/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.event;

import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.enums.PortalType;

/**
 * Multiverse 2
 *
 * @author fernferret
 */
public class MVPortalEvent extends Event implements Cancellable {
    private Player teleportee;
    private MVPortal sendingPortal;
    private MVDestination destination;
    private TravelAgent travelAgent;
    private boolean isCancelled;

    /**
     * Old constructor did not provide enough intel
     *
     * @deprecated use {@link MVPortalEvent(MVDestination destination, Player
     *             teleportee, TravelAgent travelAgent, MVPortal sendingPortal)}
     *             instead.
     */
    @Deprecated
    public MVPortalEvent(MVDestination destination, Player teleportee, TravelAgent travelAgent) {
        this.teleportee = teleportee;
        this.destination = destination;
        this.travelAgent = travelAgent;
    }

    public MVPortalEvent(MVDestination destination, Player teleportee, TravelAgent travelAgent, MVPortal sendingPortal) {
        this.teleportee = teleportee;
        this.destination = destination;
        this.travelAgent = travelAgent;
        this.sendingPortal = sendingPortal;
    }

    public MVPortalEvent(MVDestination destination, Player teleportee) {
        this(destination, teleportee, null, null);
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
    public MVDestination getDestination() {
        return this.destination;
    }

    /**
     * Returns the type of portal that was used.
     * <p/>
     * This will be Legacy for MV1 style portals and Normal for Portals that use the swirly purple goo.
     *
     * @return A {@link PortalType}
     */
    public PortalType getPortalType() {
        if (this.travelAgent == null) {
            return PortalType.Legacy;
        }
        return PortalType.Legacy;
    }

    /**
     * Returns the TravelAgent being used, or null if none.
     *
     * @return The {@link TravelAgent}.
     */
    public TravelAgent getTravelAgent() {
        return this.travelAgent;
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
