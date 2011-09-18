/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.event;

import com.onarandombox.utils.MVDestination;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

/**
 * What type of portal was used?
 *
 * If Legacy, a MV1 style portal was used.
 * If Normal, a Nether style portal (with purple goo) was used.
 *
 */
enum PortalType { Legacy, Normal }

/**
 * Multiverse 2
 *
 * @author fernferret
 */
public class MVPortalEvent extends Event implements Cancellable{
    private Player teleportee;
    private MVDestination destination;
    private TravelAgent travelAgent;
    private boolean isCancelled;

    public MVPortalEvent(MVDestination destination, Player teleportee, TravelAgent travelAgent) {
        super("MVPortal");
        this.teleportee = teleportee;
        this.destination = destination;
        this.travelAgent = travelAgent;
    }

    public MVPortalEvent(MVDestination destination, Player teleportee) {
        this(destination,teleportee,null);
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
     *
     * This will be Legacy for MV1 style portals and Normal for Portals that use the swirly purple goo.
     *
     * @return A {@link PortalType}
     */
    public PortalType getPortalType() {
        if(this.travelAgent == null) {
            return PortalType.Legacy;
        }
        return PortalType.Legacy;
    }

    /**
     * Returns the TravelAgent being used, or null if none.
     * @return The {@link TravelAgent}.
     */
    public TravelAgent getTravelAgent() {
        return this.travelAgent;
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
