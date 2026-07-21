package org.mvplugins.multiverse.portals.commands;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class SelectCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;

    @Inject
    SelectCommand(@NotNull MultiversePortals plugin,
                  @NotNull PortalsConfig portalsConfig) {
        this.plugin = plugin;
        this.portalsConfig = portalsConfig;
    }

    @Subcommand("select")
    @CommandPermission("multiverse.portal.select,multiverse.portal.create")
    @CommandCompletion("@mvportals")
    @Syntax("<portal>")
    @Description("{@@mv-portals.select.description}")
    void onSelectCommand(
            @NotNull MVCommandIssuer issuer,

            @Flags("resolve=issuerOnly")
            Player player,

            @Optional
            @Syntax("<portal>")
            @Description("{@@mv-portals.select.portal.description}")
            MVPortal portal
    ) {
        if (portal == null) {
            MVPortal selected = this.plugin.getPortalSession(player).getSelectedPortal();
            if (this.plugin.getPortalSession(player).getSelectedPortal() == null) {
                issuer.sendError(MVPi18n.SELECT_NONE,
                        replace("{wandMaterial}").with(portalsConfig.getWandMaterial()));
                return;
            }
            issuer.sendInfo(MVPi18n.SELECT_CURRENT, Replace.NAME.with(selected.getName()));
            return;
        }

        this.plugin.getPortalSession(player).selectPortal(portal);
        issuer.sendInfo(MVPi18n.SELECT_SUCCESS, Replace.NAME.with(portal.getName()));
    }

    @Service
    private final static class LegacyAlias extends SelectCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin, PortalsConfig portalsConfig) {
            super(plugin, portalsConfig);
        }

        @Override
        @CommandAlias("mvpselect|mvps")
        void onSelectCommand(MVCommandIssuer issuer, Player player, MVPortal portal) {
            super.onSelectCommand(issuer, player, portal);
        }
    }
}
