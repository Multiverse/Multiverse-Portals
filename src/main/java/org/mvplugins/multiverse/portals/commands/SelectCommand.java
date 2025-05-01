package org.mvplugins.multiverse.portals.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandManager;
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

@Service
class SelectCommand extends PortalsCommand {

    private final MultiversePortals plugin;

    @Inject
    SelectCommand(@NotNull MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Subcommand("select")
    @CommandPermission("multiverse.portal.select,multiverse.portal.create")
    @CommandCompletion("@mvportals")
    @Syntax("<portal>")
    @Description("Selects a portal so you can perform multiple modifications on it.")
    void onSelectCommand(
            @Flags("resolve=issuerOnly")
            Player player,

            @Optional
            @Syntax("<portal>")
            @Description("The portal to select")
            MVPortal portal
    ) {
        if (portal == null) {
            MVPortal selected = this.plugin.getPortalSession(player).getSelectedPortal();
            if (this.plugin.getPortalSession(player).getSelectedPortal() == null) {
                player.sendMessage("You have not selected a portal yet!");
                player.sendMessage("Use a " + ChatColor.GREEN + plugin.getWandMaterial() + ChatColor.WHITE + " to do so!");
                return;
            }
            player.sendMessage("You have selected: " + ChatColor.DARK_AQUA + selected.getName());
            return;
        }

        this.plugin.getPortalSession(player).selectPortal(portal);
        player.sendMessage("Portal: " + ChatColor.DARK_AQUA + portal.getName() + ChatColor.WHITE + " has been selected.");
    }

    @Service
    private final static class LegacyAlias extends SelectCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin) {
            super(plugin);
        }

        @Override
        @CommandAlias("mvpselect|mvps")
        void onSelectCommand(Player player, MVPortal portal) {
            super.onSelectCommand(player, portal);
        }
    }
}
