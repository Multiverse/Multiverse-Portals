package org.mvplugins.multiverse.portals.destination;

import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.destination.Destination;
import org.mvplugins.multiverse.core.destination.DestinationSuggestionPacket;
import org.mvplugins.multiverse.core.locale.MVCorei18n;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.teleportation.LocationManipulation;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.result.FailureReason;
import org.mvplugins.multiverse.external.acf.locales.MessageKey;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jetbrains.annotations.Nullable;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Collection;

@Service
public class PortalDestination implements Destination<PortalDestination, PortalDestinationInstance, PortalDestination.InstanceFailureReason> {

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
    public @NotNull Attempt<PortalDestinationInstance, InstanceFailureReason> getDestinationInstance(@Nullable String destinationParams) {
        String[] items = destinationParams.split(":");
        if (items.length > 3) {
            return Attempt.failure(InstanceFailureReason.INVALID_FORMAT, Message.of("Invalid format! Expected format is: p:portalName:[direction]"));
        }

        String portalName = items[0];
        MVPortal portal = portalManager.getPortal(portalName);
        if (portal == null) {
            return Attempt.failure(InstanceFailureReason.PORTAL_NOT_FOUND, Message.of("Portal '" + portalName + "' does not exist!"));
        }

        String direction = (items.length == 2) ? items[1] : null;
        float yaw = direction != null ? this.locationManipulation.getYaw(direction) : -1;

        return Attempt.success(new PortalDestinationInstance(this, portal, direction, yaw));
    }

    @Override
    public @NotNull Collection<DestinationSuggestionPacket> suggestDestinations(@NotNull CommandSender sender, @Nullable String s) {
        return portalManager.getAllPortals().stream()
                .map(p -> new DestinationSuggestionPacket(this, p.getName(), p.getName()))
                .toList();
    }

    public enum InstanceFailureReason implements FailureReason {
        INVALID_FORMAT(MVCorei18n.GENERIC_FAILURE),
        PORTAL_NOT_FOUND(MVCorei18n.GENERIC_FAILURE),
        ;

        private final MessageKeyProvider messageKey;

        InstanceFailureReason(MessageKeyProvider message) {
            this.messageKey = message;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public MessageKey getMessageKey() {
            return messageKey.getMessageKey();
        }
    }
}
