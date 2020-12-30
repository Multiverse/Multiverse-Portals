package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiverseCore.commandTools.display.ColourAlternator;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.Description;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RootCommand extends PortalCommand {

    public RootCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @CommandAlias("mvp")
    @Description("Multiverse-Portals")
    public void onRootCommand(@NotNull CommandSender sender) {
        this.plugin.getCore().getMVCommandManager().showPluginInfo(
                sender,
                this.plugin.getDescription(),
                new ColourAlternator(ChatColor.DARK_RED, ChatColor.RED),
                "mvp"
        );
    }
}
