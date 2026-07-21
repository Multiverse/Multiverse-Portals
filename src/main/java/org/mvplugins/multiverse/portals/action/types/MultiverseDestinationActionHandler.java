package org.mvplugins.multiverse.portals.action.types;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.teleportation.PassengerMode;
import org.mvplugins.multiverse.core.teleportation.PassengerModes;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandler;
import org.mvplugins.multiverse.portals.locale.MVPi18n;


final class MultiverseDestinationActionHandler extends ActionHandler<MultiverseDestinationActionHandlerType, MultiverseDestinationActionHandler> {

    private final AsyncSafetyTeleporter teleporter;
    private final DestinationInstance<?, ?> destinationInstance;

    MultiverseDestinationActionHandler(MultiverseDestinationActionHandlerType handlerType,
                                       AsyncSafetyTeleporter teleporter,
                                       DestinationInstance<?, ?> destinationInstance) {
        super(handlerType);
        this.teleporter = teleporter;
        this.destinationInstance = destinationInstance;
    }

    @Override
    public @NotNull Attempt<Void, ActionFailureReason> runAction(@NotNull MVPortal portal, @NotNull Entity entity) {
        teleporter.to(destinationInstance)
                .checkSafety(portal.getCheckDestinationSafety() && destinationInstance.checkTeleportSafety())
                .passengerMode(passengerModeFor(portal, entity))
                .teleportSingle(entity)
                .onFailure(reason -> Logging.warning(
                        "Failed to teleport entity '%s' to destination '%s'. Reason: %s",
                        entity.getName(), destinationInstance, reason)
                );
        return Attempt.success(null);
    }

    @Override
    public @NotNull Message actionDescription(Entity entity) {
        return Message.of(MVPi18n.ACTION_MULTIVERSEDESTINATION_DESCRIPTION,
                Replace.DESTINATION.with(destinationInstance.toString()));
    }

    private PassengerMode passengerModeFor(MVPortal portal, Entity entity) {
        if (entity instanceof Vehicle) {
            return PassengerModes.RETAIN_ALL;
        }
        return portal.getTeleportNonPlayers() ? PassengerModes.RETAIN_ALL : PassengerModes.DISMOUNT_VEHICLE;
    }

    @Override
    public @NotNull String serialise() {
        return destinationInstance.toString();
    }
}
