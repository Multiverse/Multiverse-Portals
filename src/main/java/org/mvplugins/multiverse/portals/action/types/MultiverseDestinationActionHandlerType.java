package org.mvplugins.multiverse.portals.action.types;

import org.bukkit.command.CommandSender;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.destination.DestinationsProvider;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandlerType;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import java.util.Collection;

@Service
final class MultiverseDestinationActionHandlerType extends ActionHandlerType<MultiverseDestinationActionHandlerType, MultiverseDestinationActionHandler> {

    private final DestinationsProvider destinationsProvider;
    private final AsyncSafetyTeleporter teleporter;

    @Inject
    MultiverseDestinationActionHandlerType(@NotNull DestinationsProvider destinationsProvider,
                                           @NotNull AsyncSafetyTeleporter teleporter) {
        super("multiverse-destination");
        this.destinationsProvider = destinationsProvider;
        this.teleporter = teleporter;
    }

    @Override
    public @NotNull Attempt<MultiverseDestinationActionHandler, ActionFailureReason> parseHandler(@NotNull CommandSender sender,
                                                                                                  @NotNull String action) {
        if (action.isEmpty()) {
            return Attempt.failure(ActionFailureReason.INSTANCE,
                    Message.of(MVPi18n.ACTION_MULTIVERSEDESTINATION_INVALID));
        }
        return destinationsProvider.parseDestination(sender, action)
                .transform(ActionFailureReason.INSTANCE)
                .map(destinationInstance ->
                        new MultiverseDestinationActionHandler(this, teleporter, destinationInstance));
    }

    @Override
    public @NotNull Collection<String> suggestActions(@NotNull CommandSender sender, @NotNull String input) {
        return destinationsProvider.suggestDestinationStrings(sender, input);
    }
}
