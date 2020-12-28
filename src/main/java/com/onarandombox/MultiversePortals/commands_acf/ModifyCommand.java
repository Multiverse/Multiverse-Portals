package com.onarandombox.MultiversePortals.commands_acf;

import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.enums.SetProperties;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Flags;
import com.onarandombox.acf.annotation.Optional;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@CommandAlias("mvp")
public class ModifyCommand extends PortalCommand {

    public ModifyCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("modify")
    @CommandPermission("multiverse.portal.modify")
    @Syntax("<property> <value> [portal]")
    @CommandCompletion("@portalProperties @empty @MVPortals")
    @Description("Modify portal properties.")
    public void onModifyCommand(@NotNull CommandSender sender,
                                @Nullable @Optional Player player,
                                @Nullable @Optional PortalPlayerSession portalSession,
                                @NotNull SetProperties property,
                                @NotNull @Flags("type=property value") String value,
                                @NotNull @Flags("defaultself") MVPortal portal) {

        if (property == SetProperties.destination) {
            if (value.equalsIgnoreCase("here") && player != null) {
                portal.setHereDestination(player, portalSession);
                return;
            }

            MVDestination destination = this.plugin.getCore()
                    .getDestFactory()
                    .getPlayerAwareDestination(value, player);

            sender.sendMessage((portal.setDestination(destination))
                    ? "Destination of new portal is " + ChatColor.AQUA + destination.toString() + ChatColor.WHITE + "."
                    : ChatColor.RED + "There was an error setting Destination " + ChatColor.AQUA + destination.toString()
                    + ChatColor.RED + ". Is it formatted correctly?");
        }

        sender.sendMessage((portal.setProperty(property.toString(), value))
                ? "Property " + property.toString() + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.GREEN + " was set to " + ChatColor.AQUA + value
                : "Error! Property " + property.toString() + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.RED + " could not be set to " + ChatColor.AQUA + value);
    }
}
