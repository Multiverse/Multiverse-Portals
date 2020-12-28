package com.onarandombox.MultiversePortals.commands_acf;

import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mvp")
public class InfoCommand extends PortalCommand {

    public InfoCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("info")
    @CommandPermission("multiverse.portal.info")
    @Syntax("<portal>")
    @CommandCompletion("@MVPortals")
    @Description("Displays information about a portal.")
    public void onInfoCommand(@NotNull CommandSender sender,
                              @Nullable @Optional PortalPlayerSession playerSession,

                              @Syntax("<portal>")
                              @Description("Portal name you want info to be displayed.")
                              @NotNull MVPortal portal) {

        if (playerSession != null) {
            playerSession.showDebugInfo(portal);
            return;
        }
        PortalPlayerSession.showStaticInfo(sender, portal, "Portal Info: ");
    }
}
