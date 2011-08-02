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
        this.setArgRange(0, 0);
        this.addKey("mvp wand");
        this.addKey("mvpwand");
        this.addKey("mvpw");
        this.setPermission("multiverse.portal.givewand", "Gives you the wand that MV uses. This will only work if you are NOT using WorldEdit.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
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
