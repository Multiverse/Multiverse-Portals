package org.mvplugins.multiverse.portals.commands;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.utils.DisplayUtils;

@Service
class InfoCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final DisplayUtils displayUtils;

    @Inject
    InfoCommand(@NotNull MultiversePortals plugin, @NotNull DisplayUtils displayUtils) {
        this.plugin = plugin;
        this.displayUtils = displayUtils;
    }

    @Subcommand("info")
    @CommandPermission("multiverse.portal.info")
    @CommandCompletion("@mvportals")
    @Syntax("[portal]")
    @Description("Displays information about a portal.")
    void onInfoCommand(
            @NotNull MVCommandIssuer issuer,

            @Flags("resolve=issuerAware")
            @Syntax("[portal]")
            @Description("The portal to show info")
            MVPortal portal
    ) {
        if(issuer.isPlayer()) {
            Player p = issuer.getPlayer();
            this.plugin.getPortalSession(p).showDebugInfo(portal);
        } else {
            displayUtils.showStaticInfo(issuer.getIssuer(), portal, "Portal Info: ");
        }
    }

    @Service
    private final static class LegacyAlias extends InfoCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin, DisplayUtils displayUtils) {
            super(plugin, displayUtils);
        }

        @Override
        @CommandAlias("mvpinfo|mvpi")
        void onInfoCommand(MVCommandIssuer issuer, MVPortal portal) {
            super.onInfoCommand(issuer, portal);
        }
    }
}
