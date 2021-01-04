package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvp")
public class RemoveCommand extends PortalCommand {

    public RemoveCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("remove")
    @CommandPermission("multiverse.portal.remove")
    @Syntax("<portal>")
    @CommandCompletion("@MVPortals")
    @Description("Remove a portal.")
    public void onRemoveCommand(@NotNull CommandSender sender,

                                @Syntax("<portal>")
                                @Description("Portal name you remove.")
                                @NotNull MVPortal portal) {

        MVPortal removedPortal = this.plugin.getPortalManager().removePortal(portal.getName(), true);
        sender.sendMessage((removedPortal == null)
                ? String.format("%sThere was an error removing portal '%s'.", ChatColor.RED, portal.getName())
                : String.format("Portal %s%s%s was %sremoved %ssuccessfully!",
                ChatColor.AQUA, portal.getName(), ChatColor.WHITE, ChatColor.RED, ChatColor.WHITE));
    }
}
