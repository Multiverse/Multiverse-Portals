package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.utils.MultiverseRegion;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Conditions;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mvp")
public class CreateCommand extends PortalCommand {

    public CreateCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("create")
    @CommandPermission("multiverse.portal.create")
    @Syntax("<name> [destination]")
    @CommandCompletion("@empty @MVWorlds|@destinations|here")
    @Description("Create a Portal.")
    public void onCreateCommand(@NotNull Player player,
                                @NotNull PortalPlayerSession portalSession,
                                @NotNull MultiverseWorld world,
                                @NotNull @Flags("type=portal name") @Conditions("creatablePortalName") String portalName,
                                @Nullable @Optional @Flags("type=destination") String destinationName) {

        MultiverseRegion region = portalSession.getSelectedRegion();
        if (region == null) {
            return;
        }

        PortalLocation location = new PortalLocation(region.getMinimumPoint(), region.getMaximumPoint(), world);
        if (!this.plugin.getPortalManager().addPortal(world, portalName, player.getName(), location)) {
            player.sendMessage(String.format("%sThere was an error creating portal '%s%s%s'! Check console for more details.",
                    ChatColor.RED, ChatColor.DARK_AQUA, portalName, ChatColor.RED));
            return;
        }

        MVPortal newPortal = this.plugin.getPortalManager().getPortal(portalName);
        portalSession.selectPortal(newPortal);

        player.sendMessage(String.format("New portal '%s' is created and selected!", ChatColor.DARK_AQUA + portalName + ChatColor.WHITE));

        if (destinationName == null) {
            return;
        }

        player.sendMessage((newPortal.setDestination(player, destinationName))
                ? String.format("Destination of new portal is %s%s%s.",
                ChatColor.AQUA, newPortal.getDestination().toString(), ChatColor.WHITE)
                : String.format("%sThere was an error setting Destination to %s%s%s. Is it formatted correctly?",
                ChatColor.RED, ChatColor.AQUA, destinationName, ChatColor.RED));
    }
}
