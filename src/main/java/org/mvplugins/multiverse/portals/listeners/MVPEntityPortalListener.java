package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityPortalEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.DynamicListener;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventMethod;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.IgnoreIfCancelled;
import org.mvplugins.multiverse.core.teleportation.BlockSafety;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@ApiStatus.Internal
@Service
public final class MVPEntityPortalListener implements DynamicListener {

    private final PortalManager portalManager;
    private final PortalsConfig portalsConfig;
    private final BlockSafety blockSafety;
    private final PortalListenerHelper helper;

    @Inject
    MVPEntityPortalListener(@NotNull PortalManager portalManager,
                            @NotNull PortalsConfig portalsConfig,
                            @NotNull BlockSafety blockSafety,
                            @NotNull PortalListenerHelper helper) {
        this.portalManager = portalManager;
        this.portalsConfig = portalsConfig;
        this.blockSafety = blockSafety;
        this.helper = helper;
    }

    @EventMethod
    @IgnoreIfCancelled
    void entityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();

        MVPortal portal = portalManager.getPortal(location);
        if (portal == null) {
            // Check around the player to make sure
            Location translatedLoc = this.blockSafety.findPortalBlockNextTo(event.getFrom());
            if (translatedLoc != null) {
                Logging.finer("Entity was outside of portal, The location has been successfully translated.");
                portal = portalManager.getPortal(translatedLoc);
            }
        }

        if (portal == null) {
            return;
        }
        if (!portal.getTeleportNonPlayers()) {
            if (!portalsConfig.getPortalsDefaultToNether()) {
                event.setCancelled(true);
            }
            return;
        }

        Logging.fine("[EntityPortalEvent] Portal action for entity: " + entity);
        helper.stateSuccess(entity.getName(), portal.getName());
        var finalPortal = portal;
        portal.runActionFor(entity)
                .onSuccess(() -> {
                    helper.sendActionSuccessMessage(finalPortal, entity);
                    event.setCancelled(true);
                });
    }
}
