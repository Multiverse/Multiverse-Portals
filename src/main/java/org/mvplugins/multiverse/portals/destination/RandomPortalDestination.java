package org.mvplugins.multiverse.portals.destination;

import org.mvplugins.multiverse.core.api.destination.Destination;
import org.mvplugins.multiverse.core.api.destination.DestinationSuggestionPacket;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandIssuer;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class RandomPortalDestination implements Destination<RandomPortalDestination, RandomPortalDestinationInstance> {

    private final PortalManager portalManager;

    @Inject
    RandomPortalDestination(PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "rp";
    }

    @Override
    public @Nullable RandomPortalDestinationInstance getDestinationInstance(@Nullable String destinationParams) {
        List<String> portalNames = Arrays.stream(destinationParams.split(",")).toList();
        return new RandomPortalDestinationInstance(this, portalManager, portalNames);
    }

    @Override
    public @NotNull Collection<DestinationSuggestionPacket> suggestDestinations(@NotNull BukkitCommandIssuer bukkitCommandIssuer, @Nullable String s) {
        // todo: suggest all the portal names comma seperated
        return List.of();
    }
}
