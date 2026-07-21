package org.mvplugins.multiverse.portals.commands;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.WorldEditConnection;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

@Service
class WandCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;

    @Inject
    WandCommand(@NotNull MultiversePortals plugin,
                @NotNull PortalsConfig portalsConfig) {
        this.plugin = plugin;
        this.portalsConfig = portalsConfig;
    }

    @Subcommand("wand")
    @CommandPermission("multiverse.portal.givewand")
    @CommandCompletion("enable|disable|toggle")
    @Syntax("[enable|disable|toggle]")
    @Description("{@@mv-portals.wand.description}")
    void onWandCommand(
            @NotNull MVCommandIssuer issuer,

            @Flags("resolve=issuerOnly")
            Player player,

            @Optional
            @Single
            @Syntax("[enable|disable|toggle]")
            @Description("{@@mv-portals.wand.action.description}")
            String action
    ) {
        if (action != null) {
            if (action.equals("enable")) {
                this.plugin.setWandEnabled(true);
            } else if (action.equals("disable")) {
                this.plugin.setWandEnabled(false);
            } else if (action.equals("toggle")) {
                this.plugin.setWandEnabled(!this.plugin.isWandEnabled());
            } else {
                issuer.sendError(MVPi18n.WAND_INVALIDACTION);
            }
            return;
        }

        WorldEditConnection worldEdit = plugin.getWorldEditConnection();
        if (worldEdit != null && worldEdit.isConnected()) {
            issuer.sendInfo(MVPi18n.WAND_WORLDEDIT);
            return;
        }
        ItemStack wand = new ItemStack(portalsConfig.getWandMaterial(), 1);

        if (player.getInventory().getItemInMainHand().getAmount() == 0) {
            player.getInventory().setItemInMainHand(wand);
            issuer.sendInfo(MVPi18n.WAND_GIVEN,
                    replace("{wandMaterial}").with(wand.getType()));
        } else {
            if (player.getInventory().addItem(wand).isEmpty()) {
                issuer.sendInfo(MVPi18n.WAND_INVENTORY,
                        replace("{wandMaterial}").with(wand.getType()));
            } else {
                issuer.sendInfo(MVPi18n.WAND_DROPPED,
                        replace("{wandMaterial}").with(wand.getType()));
                player.getWorld().dropItemNaturally(player.getLocation(), wand);
            }
        }
    }

    @Service
    private final static class LegacyAlias extends WandCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin, PortalsConfig portalsConfig) {
            super(plugin, portalsConfig);
        }

        @Override
        @CommandAlias("mvpwand|mvpw")
        void onWandCommand(MVCommandIssuer issuer, Player player, String action) {
            super.onWandCommand(issuer, player, action);
        }
    }
}
