package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiverseCore.commandTools.contexts.PageFilter;
import com.onarandombox.MultiverseCore.commandTools.display.ColorAlternator;
import com.onarandombox.MultiverseCore.commandTools.display.ContentCreator;
import com.onarandombox.MultiverseCore.commandTools.display.page.PageDisplay;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

@CommandAlias("mvp")
public class ListCommand extends PortalCommand {

    public ListCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("list")
    @CommandPermission("multiverse.portal.list")
    @Syntax("[filler] [page]")
    @Description("Displays a listing of all portals that you can enter.")
    public void onListCommand(@NotNull CommandSender sender,
                              @NotNull PageFilter pageFilter) {

        new PageDisplay().withSender(sender)
                .withHeader(String.format("%s==== [ Portals List %s| %sname - owner - world %s] ====",
                        ChatColor.DARK_AQUA, ChatColor.DARK_GRAY, ChatColor.AQUA, ChatColor.DARK_AQUA))
                .withCreator(buildPortalList(sender))
                .withPageFilter(pageFilter)
                .withColors( new ColorAlternator(ChatColor.YELLOW, ChatColor.WHITE))
                .build()
                .runTaskAsynchronously(this.plugin);
    }

    private ContentCreator<List<String>> buildPortalList(@NotNull CommandSender sender) {
        return () -> this.plugin.getPortalManager().getPortals(sender).stream()
                .map(portal -> portal.getName() + ChatColor.GRAY + " - "
                        + ChatColor.RED + portal.getOwner() + ChatColor.GRAY + " - "
                        + portal.getLocation().getMVWorld().getColoredWorldString())
                .collect(Collectors.toList());
    }
}
