/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.core.dynamiclistener.DynamicListener;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.DefaultEventPriority;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.IgnoreIfCancelled;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.enums.MoveType;
import org.mvplugins.multiverse.portals.event.MVPortalEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

@ApiStatus.Internal
@Service
public final class MVPPlayerMoveListener implements DynamicListener {

    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;
    private final PortalListenerHelper helper;

    @Inject
    MVPPlayerMoveListener(
            @NotNull MultiversePortals plugin,
            @NotNull PortalsConfig portalsConfig,
            @NotNull PortalListenerHelper helper) {
        this.plugin = plugin;
        this.portalsConfig = portalsConfig;
        this.helper = helper;
    }

    @EventMethod
    @IgnoreIfCancelled
    @DefaultEventPriority(EventPriority.LOW)
    void playerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer(); // Grab Player
        Location loc = player.getLocation(); // Grab Location

        // Check the Player has actually moved a block to prevent unneeded calculations... This is to prevent huge performance drops on high player count servers.
        PortalPlayerSession ps = this.plugin.getPortalSession(event.getPlayer());
        ps.setStaleLocation(loc, MoveType.PLAYER_MOVE);

        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if (ps.isStaleLocation()) {
            return;
        }
        MVPortal portal = ps.getStandingInPortal();
        // If the portal is not null, and it's a legacy portal,
        // and we didn't show debug info (the debug is meant to toggle), do the stuff.
        if (portal == null
                || (portalsConfig.getNetherAnimation() && !portal.isLegacyPortal())
                || !ps.doTeleportPlayer(MoveType.PLAYER_MOVE)
                || ps.showDebugInfo()) {
            return;
        }

        player.setFallDistance(0);

        if (!portal.isFrameValid(loc)) {
            player.sendMessage("This portal's frame is made of an " + ChatColor.RED + "incorrect material. You should exit it now.");
            return;
        }
        if (ps.checkAndSendCooldownMessage()) {
            return;
        }

        PortalListenerHelper.PortalUseResult portalUseResult = helper.checkPlayerCanUsePortal(portal, player);
        if (!portalUseResult.canUse()) {
            return;
        }

        // call event for other plugins
        MVPortalEvent portalEvent = new MVPortalEvent(portal.getDestination(), event.getPlayer(), portal);
        this.plugin.getServer().getPluginManager().callEvent(portalEvent);
        if (portalEvent.isCancelled()) {
            return;
        }
        if (portalUseResult.needToPay()) {
            helper.payPortalEntryFee(portal, player);
        }

        Logging.fine("[PlayerMoveEvent] Portal action for player: " + player);
        portal.runActionFor(player);
    }
}
