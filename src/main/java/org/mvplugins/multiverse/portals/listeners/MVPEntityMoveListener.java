package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.dynamiclistener.DynamicListener;
import org.mvplugins.multiverse.core.dynamiclistener.EventRunnable;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.EventClass;
import org.mvplugins.multiverse.core.dynamiclistener.annotations.IgnoreIfCancelled;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@ApiStatus.Internal
@Service
public final class MVPEntityMoveListener implements DynamicListener {

    private final PortalListenerHelper helper;
    private final PortalManager portalManager;
    private final PortalsConfig portalsConfig;

    @Inject
    MVPEntityMoveListener(@NotNull PortalListenerHelper helper,
                          @NotNull PortalManager portalManager,
                          @NotNull PortalsConfig portalsConfig) {
        this.helper = helper;
        this.portalManager = portalManager;
        this.portalsConfig = portalsConfig;
    }

    @EventClass("io.papermc.paper.event.entity.EntityMoveEvent")
    @IgnoreIfCancelled
    EventRunnable<?> entityMove() {
        return new EventRunnable<EntityMoveEvent>() {
            @Override
            public void onEvent(EntityMoveEvent event) {
                if (helper.isWithinSameBlock(event.getFrom(), event.getTo())) {
                    return;
                }

                LivingEntity entity = event.getEntity();
                Location location = entity.getLocation();

                MVPortal portal = portalManager.getPortal(location);
                if (portal == null
                        || !portal.getTeleportNonPlayers()
                        || (portalsConfig.getNetherAnimation() && !portal.isLegacyPortal())) {
                    return;
                }

                Logging.fine("[EntityMoveEvent] Portal action for entity: " + entity);
                helper.stateSuccess(entity.getName(), portal.getName());
                portal.runActionFor(entity);
            }
        };
    }
}
