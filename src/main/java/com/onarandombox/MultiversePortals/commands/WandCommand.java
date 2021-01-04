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
            player.sendMessage(String.format("Selection wand is now %s%s!",
                    (state) ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled", ChatColor.WHITE));
            return;
        }

        if (isUseWorldEdit(player)) {
            return;
        }

        ItemStack wand = new ItemStack(this.plugin.getWandMaterial(), 1);

        if (player.getInventory().getItemInMainHand().getAmount() == 0) {
            player.getInventory().setItemInMainHand(wand);
            player.sendMessage(String.format("You have been given a %sMultiverse Portal Wand(%s)!",
                    ChatColor.GREEN, wand.getType()));
            return;
        }
        if (player.getInventory().addItem(wand).isEmpty()) {
            player.sendMessage(String.format("A %sMultiverse Portal Wand(%s) %shas been placed in your inventory.",
                    ChatColor.GREEN, wand.getType(), ChatColor.WHITE));
            return;
        }
        player.sendMessage(String.format("Your Inventory is full. A %sMultiverse Portal Wand(%s) %sas been placed dropped nearby.",
                ChatColor.GREEN, wand.getType(), ChatColor.WHITE));
        player.getWorld().dropItemNaturally(player.getLocation(), wand);
    }

    private boolean isUseWorldEdit(@NotNull Player player) {
        WorldEditConnection worldEdit = this.plugin.getWorldEditConnection();
        if (worldEdit != null && worldEdit.isConnected()) {
            player.sendMessage(String.format("%sCool! %sYou are using %sWorldEdit!",
                    ChatColor.GREEN, ChatColor.WHITE, ChatColor.AQUA));
            player.sendMessage(String.format("Just use %sthe WorldEdit %s//wand %sto perform portal selections!",
                    ChatColor.GOLD, ChatColor.DARK_PURPLE, ChatColor.WHITE));
            return true;
        }
        return false;
    }
}
