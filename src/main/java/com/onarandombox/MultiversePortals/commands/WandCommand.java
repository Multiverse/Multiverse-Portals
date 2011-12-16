/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiversePortals.MultiversePortals;

public class WandCommand extends PortalCommand {

    public WandCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Gives a Portal Creation Wand");
        this.setCommandUsage("/mvp wand");
        this.setArgRange(0, 1);
        this.addKey("mvp wand");
        this.addKey("mvpwand");
        this.addKey("mvpw");
        this.setPermission("multiverse.portal.givewand", "Gives you the wand that MV uses. This will only work if you are NOT using WorldEdit.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        // Check for enabling/disabling wand
        if (args.size() > 0) {
            String arg = args.get(0);
            if (arg.equals("enable")) {
                this.plugin.setWandEnabled(true);
            } else if (arg.equals("disable")) {
                this.plugin.setWandEnabled(false);
            } else if (arg.equals("toggle")) {
                this.plugin.setWandEnabled(!this.plugin.isWandEnabled());
            } else {
                sender.sendMessage(ChatColor.RED + "You must specify one of 'enable,' 'disable,' or 'toggle!'");
            }
            return;
        }

        // Do the normal wand thing
        if (sender instanceof Player) {
            Player p = (Player) sender;
            if (this.plugin.getWEAPI() != null) {
                p.sendMessage(ChatColor.GREEN + "Cool!" + ChatColor.WHITE + " You're using" + ChatColor.AQUA + " WorldEdit! ");
                p.sendMessage("Just use " + ChatColor.GOLD + "the WorldEdit wand " + ChatColor.WHITE + "to perform portal selections!");
                return;
            }
            int itemType = this.plugin.getMainConfig().getInt("wand", MultiversePortals.DEFAULT_WAND);
            ItemStack wand = new ItemStack(itemType, 1);

            if (p.getItemInHand().getAmount() == 0) {
                p.setItemInHand(wand);
                p.sendMessage("You have been given a " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")!");
            } else {
                if (p.getInventory().addItem(wand).isEmpty()) {
                    p.sendMessage("A " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")" + ChatColor.WHITE + " has been placed in your inventory.");
                } else {
                    p.sendMessage("Your Inventory is full. A " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")" + ChatColor.WHITE + " has been placed dropped nearby.");
                    p.getWorld().dropItemNaturally(p.getLocation(), wand);
                }
            }
        }
    }
}
