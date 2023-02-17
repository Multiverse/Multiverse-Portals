package com.onarandombox.MultiversePortals.runnables;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.destination.InvalidDestination;
import com.onarandombox.MultiverseCore.utils.MVEconomist;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import com.onarandombox.MultiversePortals.listeners.PlayerListenerHelper;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

public class PortalPollTask extends BukkitRunnable {
    private final MultiversePortals plugin;
    private final PlayerListenerHelper helper;

    public PortalPollTask(MultiversePortals plugin) {
        this.plugin = plugin;
        this.helper = new PlayerListenerHelper(plugin);
    }

    @Override
    public void run() {
        // first, we'll get a list of portals with valid destinations
        List<MVPortal> mvPortalList = this.plugin.getPortalManager().getAllPortals();
        mvPortalList.removeIf(mvPortal -> !mvPortal.isLegacyPortal()
                || mvPortal.getDestination() == null
                || mvPortal.getDestination() instanceof InvalidDestination);

        for (MVPortal mvPortal : mvPortalList) {
            Collection<Entity> entityList = mvPortal.getWorld().getNearbyEntities(BoundingBox.of(
                    mvPortal.getLocation().getMinimum().clone(),
                    mvPortal.getLocation().getMaximum().clone().add(new Vector(1, 1, 1))));

            for (Entity entity : entityList) {
                if (!entity.isInsideVehicle()) {
                    boolean canUsePortal;

                    if (entity instanceof Player) {
                        canUsePortal = this.performPlayerChecks((Player) entity, mvPortal);
                    } else {
                        canUsePortal = MultiversePortals.TeleportEntities && mvPortal.getTeleportNonPlayers();
                        Iterator<Entity> passengerIterator = entity.getPassengers().iterator();

                        while (canUsePortal && passengerIterator.hasNext()) {
                            Entity passenger = passengerIterator.next();

                            if (passenger instanceof Player) {
                                canUsePortal = this.performPlayerChecks((Player) passenger, mvPortal);
                            }
                        }
                    }

                    if (canUsePortal) {
                        List<Entity> passengerList = entity.getPassengers();
                        Location destination = mvPortal.getDestination().getLocation(entity);

                        entity.eject();
                        entity.teleport(mvPortal.getDestination().getLocation(entity));

                        for (Entity passenger : passengerList) {
                            passenger.teleport(destination);
                            entity.addPassenger(passenger);
                        }
                    }
                }
            }
        }
    }

    private boolean performPlayerChecks(Player player, MVPortal mvPortal) {
        if (!mvPortal.isFrameValid(mvPortal.getDestination().getLocation(null))) {
            player.sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material." + ChatColor.RED + " You should exit it now.");
            return false;
        }

        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        if (mvPortal.getHandlerScript() != null && !mvPortal.getHandlerScript().isEmpty()) {
            try {
                return helper.scriptPortal(player, mvPortal.getDestination(), mvPortal, ps);
            } catch (IllegalStateException ignore) {
                // Portal not handled by script
            }
        }

        if (ps.checkAndSendCooldownMessage()) {
            return false;
        }

        // If they're using Access and they don't have permission and they're NOT exempt, return false, they're not allowed to tp.
        // No longer checking exemption status
        if (MultiversePortals.EnforcePortalAccess && !this.plugin.getCore().getMVPerms().hasPermission(player, mvPortal.getPermission().getName(), true)) {
            this.helper.stateFailure(player.getDisplayName(), mvPortal.getName());
            return false;
        }

        boolean shouldPay = false;
        double price = mvPortal.getPrice();
        Material currency = mvPortal.getCurrency();
        MVEconomist economist = plugin.getCore().getEconomist();

        // Stop the player if the portal costs and they can't pay
        if (price != 0D && !player.hasPermission(mvPortal.getExempt())) {
            shouldPay = true;
            if (price > 0D && !economist.isPlayerWealthyEnough(player, price, currency)) {
                player.sendMessage(economist.getNSFMessage(currency,
                        "You need " + economist.formatPrice(price, currency) + " to enter the " + mvPortal.getName() + " portal."));
                return false;
            }
        }

        MVPortalEvent portalEvent = new MVPortalEvent(mvPortal.getDestination(), player, null, mvPortal);
        this.plugin.getServer().getPluginManager().callEvent(portalEvent);

        if (portalEvent.isCancelled()) {
            Logging.log(Level.FINE, "Someone cancelled the MVPlayerPortal Event!");
            return false;
        } else if (shouldPay) {
            if (price < 0D) {
                economist.deposit(player, -price, currency);
            } else {
                economist.withdraw(player, price, currency);
            }
            player.sendMessage(String.format("You have %s %s for using %s.",
                    price > 0D ? "been charged" : "earned",
                    economist.formatPrice(price, currency),
                    mvPortal.getName()));
        }

        return true;
    }
}
