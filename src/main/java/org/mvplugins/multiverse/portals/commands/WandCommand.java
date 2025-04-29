package org.mvplugins.multiverse.portals.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
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
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.WorldEditConnection;

@Service
class WandCommand extends PortalsCommand {

    private final MultiversePortals plugin;

    @Inject
    WandCommand(@NotNull MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Subcommand("wand")
    @CommandPermission("multiverse.portal.givewand")
    @CommandCompletion("enable|disable|toggle")
    @Syntax("[enable|disable|toggle]")
    @Description("Gives you the wand that MV uses. This will only work if you are NOT using WorldEdit.")
    void onWandCommand(
            @Flags("resolve=issuerOnly")
            Player player,

            @Optional
            @Single
            @Syntax("[enable|disable|toggle]")
            @Description("Enable, disable, or toggle the wand.")
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
                player.sendMessage(ChatColor.RED + "You must specify one of 'enable,' 'disable,' or 'toggle!'");
            }
            return;
        }

        WorldEditConnection worldEdit = plugin.getWorldEditConnection();
        if (worldEdit != null && worldEdit.isConnected()) {
            player.sendMessage(ChatColor.GREEN + "Cool!" + ChatColor.WHITE + " You're using" + ChatColor.AQUA + " WorldEdit! ");
            player.sendMessage("Just use " + ChatColor.GOLD + "the WorldEdit wand " + ChatColor.WHITE + "to perform portal selections!");
            return;
        }
        ItemStack wand = new ItemStack(plugin.getWandMaterial(), 1);

        if (player.getInventory().getItemInMainHand().getAmount() == 0) {
            player.getInventory().setItemInMainHand(wand);
            player.sendMessage("You have been given a " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")!");
        } else {
            if (player.getInventory().addItem(wand).isEmpty()) {
                player.sendMessage("A " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")" + ChatColor.WHITE + " has been placed in your inventory.");
            } else {
                player.sendMessage("Your Inventory is full. A " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")" + ChatColor.WHITE + " has been placed dropped nearby.");
                player.getWorld().dropItemNaturally(player.getLocation(), wand);
            }
        }
    }

    @Service
    private final static class LegacyAlias extends WandCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin) {
            super(plugin);
        }

        @Override
        @CommandAlias("mvpwand|mvpw")
        void onWandCommand(Player player, String action) {
            super.onWandCommand(player, action);
        }
    }
}
