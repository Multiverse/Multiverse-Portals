package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.WorldEditConnection;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.acf.annotation.Values;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mvp")
public class WandCommand extends PortalCommand {

    public WandCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("wand")
    @CommandPermission("multiverse.portal.givewand")
    @Syntax("[on|off]")
    @CommandCompletion("@toggles")
    @Description("Gives you the wand that MV uses. This will only work if you are NOT using WorldEdit.")
    public void onWandCommand(@NotNull Player player,

                              @Syntax("[on|off]")
                              @Description("Set if wand function should be active.")
                              @Nullable @Optional @Values("@toggles") Boolean state) {

        if (state != null) {
            this.plugin.setWandEnabled(state);
            player.sendMessage("Selection wand is now " + ((state)
                    ? ChatColor.GREEN + "enabled"
                    : ChatColor.RED + "disabled") + ChatColor.WHITE + "!");
            return;
        }

        if (isUseWorldEdit(player)) {
            return;
        }

        ItemStack wand = new ItemStack(this.plugin.getWandMaterial(), 1);

        if (player.getInventory().getItemInMainHand().getAmount() == 0) {
            player.getInventory().setItemInMainHand(wand);
            player.sendMessage("You have been given a " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")!");
            return;
        }

        if (player.getInventory().addItem(wand).isEmpty()) {
            player.sendMessage("A " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")" + ChatColor.WHITE + " has been placed in your inventory.");
            return;
        }

        player.sendMessage("Your Inventory is full. A " + ChatColor.GREEN + "Multiverse Portal Wand(" + wand.getType() + ")" + ChatColor.WHITE + " has been placed dropped nearby.");
        player.getWorld().dropItemNaturally(player.getLocation(), wand);
    }

    private boolean isUseWorldEdit(@NotNull Player player) {
        WorldEditConnection worldEdit = this.plugin.getWorldEditConnection();
        if (worldEdit != null && worldEdit.isConnected()) {
            player.sendMessage(ChatColor.GREEN + "Cool!" + ChatColor.WHITE + " You're using" + ChatColor.AQUA + " WorldEdit! ");
            player.sendMessage("Just use " + ChatColor.GOLD + "the WorldEdit " + ChatColor.DARK_PURPLE + "//wand " + ChatColor.WHITE + "to perform portal selections!");
            return true;
        }
        return false;
    }
}
