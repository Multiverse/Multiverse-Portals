package com.onarandombox.MultiversePortals.commands;

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
            sender.sendMessage((portal.setDestination(player, value))
                    ? String.format("Destination of portal '%s%s%s' is now %s%s%s.",
                    ChatColor.AQUA, portal.getName(), ChatColor.WHITE, ChatColor.AQUA, portal.getDestination().toString(), ChatColor.WHITE)
                    : String.format("%sThere was an error setting Destination to %s%s%s. Is it formatted correctly?",
                    ChatColor.RED, ChatColor.AQUA, value, ChatColor.RED));
        }

        sender.sendMessage((portal.setProperty(property.toString(), value))

                ? String.format("%sSuccess! %sProperty %s%s%s of Portal %s%s%s was set to %s%s%s.",
                ChatColor.GREEN, ChatColor.WHITE, ChatColor.AQUA, property.toString(), ChatColor.WHITE,
                ChatColor.YELLOW, portal.getName(), ChatColor.WHITE, ChatColor.GREEN, value, ChatColor.WHITE)

                : String.format("%sError! Property %s%s%s of Portal %s%s%s could not be set to %s%s%s.",
                ChatColor.RED, ChatColor.AQUA, property.toString(), ChatColor.WHITE,
                ChatColor.YELLOW, portal.getName(), ChatColor.RED, ChatColor.DARK_AQUA, value, ChatColor.RED));
    }
}
