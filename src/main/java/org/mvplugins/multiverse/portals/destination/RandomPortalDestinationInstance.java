package org.mvplugins.multiverse.portals.destination;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.List;

import static org.mvplugins.multiverse.external.acf.commands.ACFUtil.RANDOM;

public class RandomPortalDestinationInstance extends DestinationInstance<RandomPortalDestinationInstance, RandomPortalDestination> {

    private final PortalManager portalManager;
    private final List<String> portalNames;

    public RandomPortalDestinationInstance(
            @NotNull RandomPortalDestination destination,
            @NotNull PortalManager portalManager,
            @NotNull List<String> portalNames) {
        super(destination);
        this.portalManager = portalManager;
        this.portalNames = portalNames;
    }

    @Override
    public @NotNull Option<Location> getLocation(@NotNull Entity teleportee) {
        List<String> portalNames = this.portalNames.isEmpty()
                ? portalManager.getAllPortals().stream().map(MVPortal::getName).toList()
                : this.portalNames;
        String targetPortalName = portalNames.get(RANDOM.nextInt(portalNames.size()));
        return Option.of(portalManager.getPortal(targetPortalName))
                .map(MVPortal::getSafePlayerSpawnLocation);
    }

    @Override
    public @NotNull Option<Vector> getVelocity(@NotNull Entity teleportee) {
        return Option.none();
    }

    @Override
    public boolean checkTeleportSafety() {
        return true;
    }

    @Override
    public @NotNull Option<String> getFinerPermissionSuffix() {
        return Option.none();
    }

    @Override
    protected @NotNull String serialise() {
        return String.join(",", portalNames);
    }
}
