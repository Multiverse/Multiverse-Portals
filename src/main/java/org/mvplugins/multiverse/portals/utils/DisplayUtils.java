package org.mvplugins.multiverse.portals.utils;

import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Service
public class DisplayUtils {

    private final WorldManager worldManager;
    private final MVEconomist economist;

    @Inject
    DisplayUtils(
            @NotNull WorldManager worldManager,
            @NotNull MVEconomist economist) {
        this.worldManager = worldManager;
        this.economist = economist;
    }

    public void showStaticInfo(CommandSender sender, MVPortal portal, String message) {
        sender.sendMessage(ChatColor.AQUA + "--- " + message + ChatColor.DARK_AQUA + portal.getName() + ChatColor.AQUA + " ---");
        String[] locParts = portal.getPortalLocation().toString().split(":");
        sender.sendMessage("Coords: " + ChatColor.GOLD + locParts[0] + ChatColor.WHITE + " to " + ChatColor.GOLD + locParts[1] + ChatColor.WHITE + " in " + ChatColor.GOLD + portal.getWorld().getName() );
        if (portal.getDestination() == null) {
            sender.sendMessage("Destination: " + ChatColor.RED + ChatColor.ITALIC + "NOT SET!");
        } else {
            String destination = portal.getDestination().toString();
            String destType = portal.getDestination().getIdentifier();
            if (destType.equals("w")) {
                MultiverseWorld destWorld = worldManager.getWorld(destination).getOrNull();
                if (destWorld != null) {
                    destination = "(World) " + ChatColor.DARK_AQUA + destination;
                }
            }
            if (destType.equals("p")) {
                // todo: I think should use instance check instead of destType prefix
                // String targetWorldName = portalManager.getPortal(portal.getDestination().getName()).getWorld().getName();
                // destination = "(Portal) " + ChatColor.DARK_AQUA + portal.getDestination().getName() + ChatColor.GRAY + " (" + targetWorldName + ")";
            }
            if (destType.equals("e")) {
                String destinationWorld = portal.getDestination().toString().split(":")[1];
                String destPart = portal.getDestination().toString().split(":")[2];
                String[] targetParts = destPart.split(",");
                int x, y, z;
                try {
                    x = (int) Double.parseDouble(targetParts[0]);
                    y = (int) Double.parseDouble(targetParts[1]);
                    z = (int) Double.parseDouble(targetParts[2]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return;
                }
                destination = "(Location) " + ChatColor.DARK_AQUA + destinationWorld + ", " + x + ", " + y + ", " + z;
            }
            if (destType.equals("i")) {
                destination = ChatColor.RED + "Invalid Destination!";
            }
            sender.sendMessage("Destination: " + ChatColor.GOLD + destination);
        }
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
