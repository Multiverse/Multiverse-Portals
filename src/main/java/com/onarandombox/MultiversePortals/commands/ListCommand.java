package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiverseCore.commandTools.PageFilter;
import com.onarandombox.MultiverseCore.commandTools.display.ColourAlternator;
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

        PageDisplay display = new PageDisplay(
                this.plugin,
                sender,
                ChatColor.AQUA + "==== [ Multiverse Portals List ] ====",
                buildPortalList(sender),
                pageFilter.getFilter(),
                new ColourAlternator(ChatColor.YELLOW, ChatColor.WHITE),
                pageFilter.getPage(),
                8
        );

        display.showContentAsync();
    }

    private ContentCreator<List<String>> buildPortalList(@NotNull CommandSender sender) {
        return () -> this.plugin.getPortalManager().getPortals(sender).stream()
                .map(portal -> portal.getName() + ChatColor.GRAY + " - "
                        + ChatColor.RED + portal.getOwner() + ChatColor.GRAY + " - "
                        + portal.getLocation().getMVWorld().getColoredWorldString())
                .collect(Collectors.toList());
    }
}
