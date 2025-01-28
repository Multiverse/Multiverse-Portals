package org.mvplugins.multiverse.portals.destination;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.portals.MVPortal;

public class PortalDestinationInstance extends DestinationInstance<PortalDestinationInstance, PortalDestination> {

    private final MVPortal portal;
    private final String direction;
    private final float yaw;

    protected PortalDestinationInstance(
            @NotNull PortalDestination destination,
            @NotNull MVPortal portal,
            @Nullable String direction,
            float yaw
    ) {
        super(destination);
        this.portal = portal;
        this.direction = direction;
        this.yaw = yaw;
    }

    public String getDirection() {
        return direction;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        return Option.of(portal.getSafePlayerSpawnLocation())
                .peek(l -> l.setYaw(yaw));
    }

    @Override
    public @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee) {
        return Option.none();
    }

    @Override
    public boolean checkTeleportSafety() {
        return portal.useSafeTeleporter();
    }

    @Override
    public @NotNull Option<String> getFinerPermissionSuffix() {
        return Option.of(portal.getName());
    }

    @Override
    protected @NotNull String serialise() {
        if (this.direction != null) {
            return this.portal.getName() + ":" + this.direction;
        }
        return this.portal.getName();
    }
}
