package com.onarandombox.MultiversePortals.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Type;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.fernferret.allpay.GenericBank;
import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.utils.InvalidDestination;
import com.onarandombox.utils.MVDestination;

public class MVPPlayerListener extends PlayerListener {
    // This is a wooden axe

    private MultiversePortals plugin;

    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) {
            return;
        }
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.playerDidTeleport(event.getTo());
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (this.plugin.getWEAPI() != null || !this.plugin.getCore().getPermissions().hasPermission(event.getPlayer(), "multiverse.portal.create", true)) {
            return;
        }
        int itemType = this.plugin.getMainConfig().getInt("wand", MultiversePortals.DEFAULT_WAND);
        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.getPlayer().getItemInHand().getTypeId() == itemType) {
            MVWorld world = this.plugin.getCore().getMVWorld(event.getPlayer().getWorld().getName());
            this.plugin.getPortalSession(event.getPlayer()).setLeftClickSelection(event.getClickedBlock().getLocation().toVector(), world);
            event.setCancelled(true);
        }
        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().getItemInHand().getTypeId() == itemType) {
            MVWorld world = this.plugin.getCore().getMVWorld(event.getPlayer().getWorld().getName());
            this.plugin.getPortalSession(event.getPlayer()).setRightClickSelection(event.getClickedBlock().getLocation().toVector(), world);
            event.setCancelled(true);
        }
    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer(); // Grab Player
        Location loc = p.getLocation(); // Grab Location
        /**
         * Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
         */
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.setStaleLocation(loc, Type.PLAYER_MOVE);

        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (ps.isStaleLocation()) {
            return;
        }

        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null
        // AND if we did not show debug info, do the stuff
        // The debug is meant to toggle.
        if (portal != null && ps.doTeleportPlayer(Type.PLAYER_MOVE) && !ps.showDebugInfo()) {
            MVDestination d = portal.getDestination();
            if (d == null) {
                return;
            }
            // Vector v = event.getPlayer().getVelocity();
            // System.out.print("Vector: " + v.toString());
            // System.out.print("Fall Distance: " + event.getPlayer().getFallDistance());
            event.getPlayer().setFallDistance(0);

            if (d instanceof InvalidDestination) {
                // System.out.print("Invalid dest!");
                return;
            }

            MVWorld world = this.plugin.getCore().getMVWorld(d.getLocation(p).getWorld().getName());
            if (world == null) {
                return;
            }
            // If the player does not have to pay, return now.
            if (world.isExempt(event.getPlayer()) || portal.isExempt(event.getPlayer())) {
                performTeleport(event, ps, d);
                return;
            }
            GenericBank bank = plugin.getCore().getBank();
            if (bank.hasEnough(event.getPlayer(), portal.getPrice(), portal.getCurrency(), "You need " + bank.getFormattedAmount(portal.getPrice(), portal.getCurrency()) + " to enter the " + portal.getName() + " portal.")) {
                bank.pay(event.getPlayer(), portal.getPrice(), portal.getCurrency());
                performTeleport(event, ps, d);
            }
        }
    }

    private void performTeleport(PlayerMoveEvent event, PortalPlayerSession ps, MVDestination d) {
        MVTeleport playerTeleporter = new MVTeleport(this.plugin.getCore());
        if (playerTeleporter.safelyTeleport(event.getPlayer(), d.getLocation(event.getPlayer()))) {
            ps.playerDidTeleport(event.getTo());
            event.getPlayer().setVelocity(d.getVelocity());
        }
    }
}
