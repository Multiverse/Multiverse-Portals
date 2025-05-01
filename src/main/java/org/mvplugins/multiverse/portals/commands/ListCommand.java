package org.mvplugins.multiverse.portals.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Default;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.ArrayList;
import java.util.List;

@Service
class ListCommand extends PortalsCommand {

    private static final int ITEMS_PER_PAGE = 9;

    private final PortalManager portalManager;
    private final WorldManager worldManager;

    @Inject
    ListCommand(@NotNull PortalManager portalManager, @NotNull WorldManager worldManager) {
        this.portalManager = portalManager;
        this.worldManager = worldManager;
    }

    @Subcommand("list")
    @CommandPermission("multiverse.portal.list")
    @CommandCompletion("@empty @empty")
    @Syntax("[filter/world] [page]")
    @Description("Displays a listing of all portals that you can enter.")
    void onListCommand(
            @NotNull CommandSender sender,

            @Optional
            @Syntax("[filter/world]")
            @Description("Filter by name or world")
            String filterOrWorld,

            @Default("1")
            @Syntax("[page]")
            @Description("Page to display")
            int page
    ) {
        String filter = filterOrWorld;

        MultiverseWorld world = this.worldManager.getLoadedWorld(filter).getOrNull();
        if (world != null) {
            filter = null;
        }

        List<String> portals = new ArrayList<>(getPortals(sender, world, filter, page));

        if (portals.isEmpty() && filter == null) {
            page = (int) Math.ceil(1F * getPortals(sender, world, filter).size() / ITEMS_PER_PAGE);
            portals.addAll(getPortals(sender, world, filter, page));
        }

        String titleString = ChatColor.AQUA + String.valueOf(getPortals(sender, world, filter).size()) + " Portals";
        if (world != null) {
            titleString += " in " + ChatColor.YELLOW + world.getAlias();
        }
        if (filter != null) {
            titleString += ChatColor.GOLD + " [" + filter + "]";
        }

        titleString += ChatColor.GOLD + " - Page " + page + "/" + (int) Math.ceil(1F * getPortals(sender, world, filter).size() / ITEMS_PER_PAGE);
        sender.sendMessage(ChatColor.AQUA + "--- " + titleString + ChatColor.AQUA + " ---");

        for (String portal : portals) {
            sender.sendMessage(portal);
        }
    }

    private List<String> getPortals(CommandSender sender, MultiverseWorld world, String filter) {
        List<String> portals = new ArrayList<>();
        if (filter == null) {
            filter = "";
        }
        for (MVPortal portal : (world == null) ? this.portalManager.getPortals(sender) : this.portalManager.getPortals(sender, world)) {
            String destination = "";
            if (portal.getDestination() != null) {
                destination = portal.getDestination().toString();
                String destType = portal.getDestination().getIdentifier();
                if (destType.equals("w")) {
                    MultiverseWorld destWorld = this.worldManager.getLoadedWorld(destination).getOrNull();
                    if (destWorld != null) {
                        destination = "(World) " + ChatColor.DARK_AQUA + destination;
                    }
                }
                if (destType.equals("p")) {
                    // todo: I think should use instance check instead of destType prefix
                    // String targetWorldName = this.portalManager.getPortal(portal.getDestination().getName()).getWorld().getName();
                    // destination = "(Portal) " + ChatColor.DARK_AQUA + portal.getDestination().getName() + ChatColor.GRAY + " (" + targetWorldName + ")";
                }
                if (destType.equals("e")) {
                    String destinationWorld = portal.getDestination().toString().split(":")[1];
                    String destPart = portal.getDestination().toString().split(":")[2];
                    String[] locParts = destPart.split(",");
                    int x, y, z;
                    try {
                        x = (int) Double.parseDouble(locParts[0]);
                        y = (int) Double.parseDouble(locParts[1]);
                        z = (int) Double.parseDouble(locParts[2]);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        continue;
                    }
                    if (destType.equals("i")) {
                        destination = ChatColor.RED + "Invalid destination";
                    }
                    destination = "(Location) " + ChatColor.DARK_AQUA + destinationWorld + ", " + x + ", " + y + ", " + z;
                }
            }

            if (portal.getName().toLowerCase().contains(filter.toLowerCase()) || (portal.getDestination() != null && destination.toLowerCase().contains(filter.toLowerCase()))) {
                portals.add(ChatColor.YELLOW + portal.getName() + ((portal.getDestination() != null) ? (ChatColor.AQUA + " -> " + ChatColor.GOLD + destination) : ""));
            }
        }
        java.util.Collections.sort(portals);
        return portals;
    }

    private List<String> getPortals(CommandSender sender, MultiverseWorld world, String filter, int page) {
        List<String> portals = new ArrayList<>();
        for (int i = 0; i < getPortals(sender, world, filter).size(); i++) {
            if ((i >= (page * ITEMS_PER_PAGE) - ITEMS_PER_PAGE && i <= (page * ITEMS_PER_PAGE) - 1)) {
                portals.add(getPortals(sender, world, filter).get(i));
            }
        }
        return portals;
    }

    @Service
    private final static class LegacyAlias extends ListCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(PortalManager portalManager, WorldManager worldManager) {
            super(portalManager, worldManager);
        }

        @Override
        @CommandAlias("mvplist|mvpl")
        void onListCommand(CommandSender sender, String filterOrWorld, int page) {
            super.onListCommand(sender, filterOrWorld, page);
        }
    }
}
