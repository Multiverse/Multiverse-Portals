package com.onarandombox.MultiversePortals.commands_acf;

import com.onarandombox.MultiverseCore.commandTools.ColourAlternator;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.acf.annotation.CommandAlias;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class RootCommand extends PortalCommand {

    public RootCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @CommandAlias("mvp")
    public void onRootCommand(@NotNull CommandSender sender) {
        this.plugin.getCore().getMVCommandManager().showPluginInfo(
                sender,
                this.plugin.getDescription(),
                new ColourAlternator(ChatColor.DARK_RED, ChatColor.RED),
                "mvp"
        );
    }
}
