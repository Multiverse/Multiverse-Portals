package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.IgnoreIfCancelled;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.enums.MoveType;
import org.mvplugins.multiverse.portals.event.MVPortalEvent;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
final class MVPPlayerPortalListener implements PortalsListener {

    private final PortalManager portalManager;
    private final PortalsConfig portalsConfig;
    private final BlockSafety blockSafety;
    private final MultiversePortals plugin;
    private final PortalListenerHelper helper;

    @Inject
    MVPPlayerPortalListener(@NotNull PortalManager portalManager,
                            @NotNull PortalsConfig portalsConfig,
                            @NotNull BlockSafety blockSafety,
                            @NotNull MultiversePortals plugin,
                            @NotNull PortalListenerHelper helper) {
        this.portalManager = portalManager;
        this.portalsConfig = portalsConfig;
        this.blockSafety = blockSafety;
        this.plugin = plugin;
        this.helper = helper;
    }

    @EventMethod
    @IgnoreIfCancelled
    void playerPortal(PlayerPortalEvent event) {
        Logging.finer("onPlayerPortal called!");
        Player player = event.getPlayer();
        Location playerPortalLoc = player.getLocation();
        // Determine if we're in a portal
        MVPortal portal = portalManager.getPortal(player, playerPortalLoc, false);
        // Even if the location was null, we still have to see if
        // someone wasn't exactly on (because they can do this).
        if (portal == null) {
            // Check around the player to make sure
            playerPortalLoc = this.blockSafety.findPortalBlockNextTo(event.getFrom());
            if (playerPortalLoc != null) {
                Logging.finer("Player was outside of portal, The location has been successfully translated.");
                portal = portalManager.getPortal(player, playerPortalLoc, false);
            }
        }

        if (portal == null) {
            return;
        }

        Logging.finer("There was a portal found!");
        if (!portalsConfig.getPortalsDefaultToNether()) {
            event.setCancelled(true);
        }

        if (!portal.isFrameValid(playerPortalLoc)) {
            helper.sendInvalidFrameMessage(player);
            event.setCancelled(true);
            return;
        }

        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        ps.setStaleLocation(playerPortalLoc, MoveType.PLAYER_MOVE);
        if (ps.showDebugInfo()) {
            event.setCancelled(true);
            return;
        }
        if (ps.checkAndSendCooldownMessage()) {
            Logging.fine("Player denied teleportation due to cooldown.");
            return;
        }
        PortalListenerHelper.PortalUseResult portalUseResult = helper.checkPlayerCanUsePortal(portal, player);
        if (!portalUseResult.canUse()) {
            return;
        }

        MVPortalEvent portalEvent = new MVPortalEvent(portal.getDestination(), player, portal);
        this.plugin.getServer().getPluginManager().callEvent(portalEvent);

        if (portalEvent.isCancelled()) {
            Logging.fine("Someone cancelled the MVPlayerPortal Event!");
            return;
        }
        if (portalUseResult.needToPay()) {
            helper.payPortalEntryFee(portal, player);
        }

        Logging.fine("[PlayerPortalEvent] Portal action for player: " + player);
        helper.stateSuccess(player.getDisplayName(), portal.getName());
        portal.runActionFor(player)
                .onSuccess(() -> {
                    event.setCancelled(true);
                });
    }
}
