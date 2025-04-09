package org.mvplugins.multiverse.portals.destination;

import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.core.destination.DestinationSuggestionPacket;
import org.mvplugins.multiverse.core.utils.StringFormatter;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
public class RandomPortalDestination implements Destination<RandomPortalDestination, RandomPortalDestinationInstance, FailureReason> {

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
    public @Nullable Attempt<RandomPortalDestinationInstance, FailureReason> getDestinationInstance(@Nullable String destinationParams) {
        List<String> portalNames = Arrays.stream(destinationParams.split(",")).toList();
        return Attempt.success(new RandomPortalDestinationInstance(this, portalManager, portalNames));
    }

    @Override
    public @NotNull Collection<DestinationSuggestionPacket> suggestDestinations(@NotNull CommandSender sender, @Nullable String s) {
        // todo: suggest all the portal names comma seperated
        return List.of();
    }
}
