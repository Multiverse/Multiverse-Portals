package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.acf.CommandHelp;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.HelpCommand;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@CommandAlias("mvp")
public class UsageCommand extends PortalCommand {

    public UsageCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @HelpCommand
    @Subcommand("help")
    @CommandPermission("multiverse.portal.help")
    @Syntax("[filter] [page]")
    @CommandCompletion("@subCommands:mvp")
    @Description("Show Multiverse Command usage.")
    public void onUsageCommand(@NotNull CommandSender sender,
                               @NotNull CommandHelp help) {

        this.plugin.getCore().getMVCommandManager().showUsage(help);
    }
}
