package org.mvplugins.multiverse.portals.utils;

import org.bukkit.entity.Entity;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandler;

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

    public void showStaticInfo(CommandSender sender, MVPortal portal, String message) {
        MVCommandIssuer issuer = commandManager.getCommandIssuer(sender);

        issuer.sendMessage(ChatColor.AQUA + "--- " + message + ChatColor.DARK_AQUA + portal.getName() + ChatColor.AQUA + " ---");
        String[] locParts = portal.getPortalLocation().toString().split(":");
        issuer.sendMessage(ChatColor.WHITE + "Coords: " + ChatColor.GOLD + locParts[1] + ChatColor.WHITE + " to "
                + ChatColor.GOLD + locParts[2] + ChatColor.WHITE + " in " + ChatColor.GOLD
                + portal.getMultiverseWorld().map(MultiverseWorld::getName).getOrElse(ChatColor.RED + "!!ERROR!!"));
        issuer.sendMessage(ChatColor.WHITE + "Configured Action Type: " + ChatColor.GOLD + portal.getActionType());
        if (portal.getAction().isEmpty()) {
            issuer.sendMessage(ChatColor.WHITE + "Configured Action: " + ChatColor.RED + ChatColor.ITALIC + "NOT SET!");
        } else {
            issuer.sendMessage(ChatColor.WHITE + "Configured Action: " + ChatColor.GOLD + portal.getAction());
        }
        Attempt<? extends ActionHandler<?, ?>, ActionFailureReason> actionHandler = portal.getActionHandler();

        issuer.sendMessage(ChatColor.WHITE + "Check Destination Safety: " + formatBoolean(portal.getCheckDestinationSafety()));
        issuer.sendMessage(ChatColor.WHITE + "Teleport Non Players: " + formatBoolean(portal.getTeleportNonPlayers()));
        showPortalPriceInfo(portal, sender);

        if (sender instanceof Entity entity) {
            actionHandler.map(handler -> handler.actionDescription(entity))
                    .onSuccess(actionMessage -> {
                        issuer.sendMessage("");
                        issuer.sendMessage(Message.of(ChatColor.WHITE + "Your action when using Portal: {action}",
                                replace("{action}").with(actionMessage)));
                    });
        }

        actionHandler.onFailure(failure -> {
            issuer.sendMessage("");
            issuer.sendError(failure.getFailureMessage());
        });
    }

    private String formatBoolean(Boolean bool) {
        return bool ? ChatColor.GREEN + "true" : ChatColor.RED + "false";
    }

    @ApiStatus.AvailableSince("5.2")
    public String formatActionAsMVDestination(MVPortal portal) {
        String[] split = portal.getAction().split(":", 2);
        String destination = split.length == 2 ? split[1] : "";
        String destType = split.length == 2 ? split[0] : "";
        if (destType.equals("w")) {
            MultiverseWorld destWorld = worldManager.getWorld(destination).getOrNull();
            if (destWorld != null) {
                return "(World) " + ChatColor.DARK_AQUA + destination;
            }
        }
        if (destType.equals("p")) {
            // todo: I think should use instance check instead of destType prefix
            // String targetWorldName = portalManager.getPortal(portal.getDestination().getName()).getWorld().getName();
            // destination = "(Portal) " + ChatColor.DARK_AQUA + portal.getDestination().getName() + ChatColor.GRAY + " (" + targetWorldName + ")";
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
                return ChatColor.RED + "Invalid Exact World Location!";
            }
            return "(Location) " + ChatColor.DARK_AQUA + destinationWorld + ", " + x + ", " + y + ", " + z;
        }
        if (destType.equals("i")) {
            return ChatColor.RED + "Invalid Destination!";
        }
        return ChatColor.DARK_AQUA + portal.getAction();
    }

    public void showPortalPriceInfo(MVPortal portal, CommandSender sender) {
        if (portal.getPrice() > 0D) {
            sender.sendMessage("Price: " + ChatColor.GREEN + economist.formatPrice(portal.getPrice(), portal.getCurrency()));
        } else if (portal.getPrice() < 0D) {
            sender.sendMessage("Price: " + ChatColor.GREEN + economist.formatPrice(-portal.getPrice(), portal.getCurrency()));
        } else {
            sender.sendMessage("Price: " + ChatColor.GREEN + "FREE!");
        }
    }
}
