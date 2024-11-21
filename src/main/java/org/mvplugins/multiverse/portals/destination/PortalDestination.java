package org.mvplugins.multiverse.portals.destination;

import org.mvplugins.multiverse.core.api.LocationManipulation;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandIssuer;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Collection;

@Service
public class PortalDestination implements Destination<PortalDestination, PortalDestinationInstance> {

    private final PortalManager portalManager;
    private final LocationManipulation locationManipulation;

    @Inject
    PortalDestination(@NotNull PortalManager portalManager, @NotNull LocationManipulation locationManipulation) {
        this.portalManager = portalManager;
        this.locationManipulation = locationManipulation;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "p";
    }

    @Override
    public @Nullable PortalDestinationInstance getDestinationInstance(@Nullable String destinationParams) {
        String[] items = destinationParams.split(":");
        if (items.length > 3) {
            return null;
        }

        String portalName = items[0];
        MVPortal portal = portalManager.getPortal(portalName);
        if (portal == null) {
            return null;
        }

        String direction = (items.length == 2) ? items[1] : null;
        float yaw = direction != null ? this.locationManipulation.getYaw(direction) : -1;

        return new PortalDestinationInstance(this, portal, direction, yaw);
    }

    @Override
    public @NotNull Collection<String> suggestDestinations(@NotNull BukkitCommandIssuer bukkitCommandIssuer, @Nullable String s) {
        return portalManager.getAllPortals().stream().map(MVPortal::getName).toList();
    }
}
