package com.onarandombox.MultiversePortals.commands_acf;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Single;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import com.onarandombox.acf.annotation.Values;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mvp")
public class DebugCommand extends PortalCommand {

    public DebugCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("debug")
    @CommandPermission("multiverse.portal.debug")
    @Syntax("[on|off]")
    //TODO: Have a complete called @toggles
    @CommandCompletion("on|off")
    @Description("Instead of teleporting you to a place when you walk into a portal you will see the details about it. This command toggles.")
    public void onDebugCommand(@NotNull Player player,
                                     @Nullable @Optional @Single @Values("on|off") String mode) {

        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        ps.setDebugMode((mode == null)
                ? !ps.isDebugModeOn()
                : mode.equalsIgnoreCase("on"));
    }
}
