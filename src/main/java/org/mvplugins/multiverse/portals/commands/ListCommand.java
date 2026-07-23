package org.mvplugins.multiverse.portals.commands;

import org.bukkit.command.CommandSender;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
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
import org.mvplugins.multiverse.portals.locale.MVPi18n;
import org.mvplugins.multiverse.portals.utils.DisplayUtils;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.ArrayList;
import java.util.List;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class ListCommand extends PortalsCommand {

    private static final int ITEMS_PER_PAGE = 9;

    private final PortalManager portalManager;
    private final WorldManager worldManager;
    private final DisplayUtils displayUtils;

    @Inject
    ListCommand(@NotNull PortalManager portalManager,
                @NotNull WorldManager worldManager,
                @NotNull DisplayUtils displayUtils) {
        this.portalManager = portalManager;
        this.worldManager = worldManager;
        this.displayUtils = displayUtils;
    }

    @Subcommand("list")
    @CommandPermission("multiverse.portal.list")
    @CommandCompletion("@empty @empty")
    @Syntax("[filter/world] [page]")
    @Description("{@@mv-portals.list.description}")
    void onListCommand(
            @NotNull MVCommandIssuer issuer,

            @Optional
            @Syntax("[filter/world]")
            @Description("{@@mv-portals.list.filter.description}")
            String filterOrWorld,

            @Default("1")
            @Syntax("[page]")
            @Description("{@@mv-portals.list.page.description}")
            int page
    ) {
        String filter = filterOrWorld;

        MultiverseWorld world = this.worldManager.getLoadedWorld(filter).getOrNull();
        if (world != null) {
            filter = null;
        }

        List<String> portals = new ArrayList<>(getPortals(issuer, world, filter, page));

        if (portals.isEmpty() && filter == null) {
            page = (int) Math.ceil(1F * getPortals(issuer, world, filter).size() / ITEMS_PER_PAGE);
            portals.addAll(getPortals(issuer, world, filter, page));
        }

        int portalCount = getPortals(issuer, world, filter).size();
        int totalPages = (int) Math.ceil(1F * portalCount / ITEMS_PER_PAGE);
        Message worldMessage = world == null
                ? Message.of("")
                : Message.of(MVPi18n.LIST_HEADER_WORLD, Replace.WORLD.with(world.getAlias()));
        Message filterMessage = filter == null
                ? Message.of("")
                : Message.of(MVPi18n.LIST_HEADER_FILTER, replace("{filter}").with(filter));
        issuer.sendInfo(MVPi18n.LIST_HEADER,
                Replace.COUNT.with(portalCount),
                Replace.WORLD.with(worldMessage),
                replace("{filter}").with(filterMessage),
                replace("{page}").with(page),
                replace("{totalPages}").with(totalPages));

        for (String portal : portals) {
            issuer.sendInfo(portal);
        }
    }

    private List<String> getPortals(MVCommandIssuer issuer, MultiverseWorld world, String filter) {
        List<String> portals = new ArrayList<>();
        if (filter == null) {
            filter = "";
        }
        CommandSender sender = issuer.getIssuer();
        for (MVPortal portal : (world == null)
                ? this.portalManager.getPortals(sender)
                : this.portalManager.getPortals(sender, world)) {
            String destination = displayUtils.formatActionAsMVDestinationMessage(portal).formatted(
                    issuer);
            if (portal.getName().toLowerCase().contains(filter.toLowerCase()) || destination.toLowerCase().contains(filter.toLowerCase())) {
                portals.add(Message.of(MVPi18n.LIST_ENTRY,
                        Replace.NAME.with(portal.getName()),
                        Replace.DESTINATION.with(destination)).formatted(issuer));
            }
        }
        java.util.Collections.sort(portals);
        return portals;
    }

    private List<String> getPortals(MVCommandIssuer issuer, MultiverseWorld world, String filter, int page) {
        List<String> portals = new ArrayList<>();
        for (int i = 0; i < getPortals(issuer, world, filter).size(); i++) {
            if ((i >= (page * ITEMS_PER_PAGE) - ITEMS_PER_PAGE && i <= (page * ITEMS_PER_PAGE) - 1)) {
                portals.add(getPortals(issuer, world, filter).get(i));
            }
        }
        return portals;
    }

    @Service
    private final static class LegacyAlias extends ListCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(PortalManager portalManager, WorldManager worldManager, DisplayUtils displayUtils) {
            super(portalManager, worldManager, displayUtils);
        }

        @Override
        @CommandAlias("mvplist|mvpl")
        void onListCommand(MVCommandIssuer issuer, String filterOrWorld, int page) {
            super.onListCommand(issuer, filterOrWorld, page);
        }
    }
}
