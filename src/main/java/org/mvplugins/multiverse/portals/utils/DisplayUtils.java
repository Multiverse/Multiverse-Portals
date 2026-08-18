package org.mvplugins.multiverse.portals.utils;

import org.bukkit.entity.Entity;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.external.acf.locales.MessageKeyProvider;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.utils.text.ChatTextFormatter;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandler;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
public class DisplayUtils {

    private final WorldManager worldManager;
    private final MVEconomist economist;
    private final MVCommandManager commandManager;

    @Inject
    DisplayUtils(
            @NotNull WorldManager worldManager,
            @NotNull MVEconomist economist,
            @NotNull MVCommandManager commandManager) {
        this.worldManager = worldManager;
        this.economist = economist;
        this.commandManager = commandManager;
    }

    /**
     * Shows information about a portal using a custom, non-localized header.
     *
     * @param sender The recipient of the portal information.
     * @param portal The portal to describe.
     * @param message The custom header text.
     */
    public void showStaticInfo(CommandSender sender, MVPortal portal, String message) {
        commandManager.getCommandIssuer(sender).sendInfo(MVPi18n.PORTALINFO_CUSTOMHEADER,
                replace("{header}").with(message),
                Replace.NAME.with(portal.getName()));
        showStaticInfoBody(sender, portal);
    }

    /**
     * Shows information about a portal using the given localized header.
     *
     * @param sender The recipient of the portal information.
     * @param portal The portal to describe.
     * @param headerKey The locale key for the header.
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    public void showStaticInfo(CommandSender sender, MVPortal portal, MessageKeyProvider headerKey) {
        MVCommandIssuer issuer = commandManager.getCommandIssuer(sender);

        issuer.sendInfo(headerKey, Replace.NAME.with(portal.getName()));
        showStaticInfoBody(sender, portal);
    }

    private void showStaticInfoBody(CommandSender sender, MVPortal portal) {
        MVCommandIssuer issuer = commandManager.getCommandIssuer(sender);
        String[] locParts = portal.getPortalLocation().toString().split(":");
        issuer.sendInfo(MVPi18n.PORTALINFO_COORDINATES,
                replace("{fromLocation}").with(locParts[1]),
                replace("{toLocation}").with(locParts[2]),
                Replace.WORLD.with(portal.getMultiverseWorld()
                        .map(MultiverseWorld::getName)
                        .map(Message::of)
                        .getOrElse(Message.of(MVPi18n.PORTALINFO_WORLD_ERROR))));
        issuer.sendInfo(MVPi18n.PORTALINFO_ACTIONTYPE,
                replace("{actionType}").with(portal.getActionType()));
        if (portal.getAction().isEmpty()) {
            issuer.sendInfo(MVPi18n.PORTALINFO_ACTION_NOTSET);
        } else {
            issuer.sendInfo(MVPi18n.PORTALINFO_ACTION,
                    replace("{action}").with(portal.getAction()));
        }
        Attempt<? extends ActionHandler<?, ?>, ActionFailureReason> actionHandler = portal.getActionHandler();

        issuer.sendInfo(MVPi18n.PORTALINFO_CHECKDESTINATIONSAFETY,
                Replace.VALUE.with(formatBoolean(portal.getCheckDestinationSafety())));
        issuer.sendInfo(MVPi18n.PORTALINFO_TELEPORTNONPLAYERS,
                Replace.VALUE.with(formatBoolean(portal.getTeleportNonPlayers())));
        showPortalPriceInfo(portal, sender);

        if (sender instanceof Entity entity) {
            actionHandler.map(handler -> handler.actionDescription(entity))
                    .onSuccess(actionMessage -> issuer.sendInfo(MVPi18n.PORTALINFO_PLAYERACTION,
                            replace("{action}").with(actionMessage)));
        }

        actionHandler.onFailure(failure -> issuer.sendError(MVPi18n.PORTALINFO_ACTIONERROR,
                Replace.ERROR.with(failure.getFailureMessage())));
    }

    private Message formatBoolean(Boolean bool) {
        return Message.of(bool ? MVPi18n.GENERIC_TRUE : MVPi18n.GENERIC_FALSE);
    }

    @ApiStatus.AvailableSince("5.2")
    public String formatActionAsMVDestination(MVPortal portal) {
        return ChatTextFormatter.colorize(
                formatActionAsMVDestinationMessage(portal).formatted(commandManager.getLocales()));
    }

    /**
     * Formats a portal action as a localizable Multiverse destination description.
     *
     * @param portal The portal whose action should be formatted.
     * @return The localizable destination description.
     *
     * @since 5.3
     */
    @ApiStatus.AvailableSince("5.3")
    public Message formatActionAsMVDestinationMessage(MVPortal portal) {
        String[] split = portal.getAction().split(":", 2);
        String destination = split.length == 2 ? split[1] : "";
        String destType = split.length == 2 ? split[0] : "";
        if (destType.equals("w")) {
            MultiverseWorld destWorld = worldManager.getWorld(destination).getOrNull();
            if (destWorld != null) {
                return Message.of(MVPi18n.DESTINATION_WORLD, Replace.DESTINATION.with(destination));
            }
        }
        if (destType.equals("p")) {
            // todo: I think should use instance check instead of destType prefix
        }
        if (destType.equals("e")) {
            String destinationWorld = portal.getAction().split(":")[1];
            String destPart = portal.getAction().split(":")[2];
            String[] targetParts = destPart.split(",");
            int x, y, z;
            try {
                x = (int) Double.parseDouble(targetParts[0]);
                y = (int) Double.parseDouble(targetParts[1]);
                z = (int) Double.parseDouble(targetParts[2]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return Message.of(MVPi18n.DESTINATION_INVALIDLOCATION);
            }
            return Message.of(MVPi18n.DESTINATION_LOCATION,
                    Replace.WORLD.with(destinationWorld),
                    Replace.LOCATION.with(x + ", " + y + ", " + z));
        }
        if (destType.equals("i")) {
            return Message.of(MVPi18n.DESTINATION_INVALID);
        }
        return Message.of(MVPi18n.DESTINATION_RAW, Replace.DESTINATION.with(portal.getAction()));
    }

    public void showPortalPriceInfo(MVPortal portal, CommandSender sender) {
        MVCommandIssuer issuer = commandManager.getCommandIssuer(sender);
        if (portal.getPrice() > 0D) {
            issuer.sendInfo(MVPi18n.PORTALINFO_PRICE,
                    Replace.VALUE.with(economist.formatPrice(portal.getPrice(), portal.getCurrency())));
        } else if (portal.getPrice() < 0D) {
            issuer.sendInfo(MVPi18n.PORTALINFO_PRICE,
                    Replace.VALUE.with(economist.formatPrice(-portal.getPrice(), portal.getCurrency())));
        } else {
            issuer.sendInfo(MVPi18n.PORTALINFO_PRICE_FREE);
        }
    }
}
