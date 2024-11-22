package org.mvplugins.multiverse.portals.commands;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalPlayerSession;

@Service
@CommandAlias("mvp")
public class DebugCommand extends PortalsCommand {

    private final MultiversePortals plugin;

    @Inject
    DebugCommand(@NotNull MVCommandManager commandManager, @NotNull MultiversePortals plugin) {
        super(commandManager);
        this.plugin = plugin;
    }

    @CommandAlias("mvpdebug|mvpd")
    @Subcommand("debug")
    @CommandPermission("multiverse.portal.debug")
    @CommandCompletion("on|off")
    @Syntax("[on|off]")
    @Description("Instead of teleporting you to a place when you walk into a portal you will see the details about it. This command toggles.")
    void onDebugCommand(
        @Flags("resolve=issuerOnly")
        Player player,

        @Optional
        @Single
        @Syntax("[on|off]")
        String toggle
    ) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        if (toggle != null) {
            ps.setDebugMode(toggle.equalsIgnoreCase("on"));
            return;
        }
        ps.setDebugMode(!ps.isDebugModeOn());
    }
}
