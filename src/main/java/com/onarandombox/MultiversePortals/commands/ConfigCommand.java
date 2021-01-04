/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import com.onarandombox.MultiverseCore.commandTools.display.ColorAlternator;
import com.onarandombox.MultiverseCore.commandTools.display.ContentCreator;
import com.onarandombox.MultiverseCore.commandTools.display.ContentFilter;
import com.onarandombox.MultiverseCore.commandTools.display.inline.KeyValueDisplay;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;
import com.onarandombox.MultiversePortals.utils.PortalProperty;
import com.onarandombox.acf.annotation.CommandAlias;
import com.onarandombox.acf.annotation.CommandCompletion;
import com.onarandombox.acf.annotation.CommandPermission;
import com.onarandombox.acf.annotation.Description;
import com.onarandombox.acf.annotation.Subcommand;
import com.onarandombox.acf.annotation.Syntax;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@CommandAlias("mvp")
@Subcommand("config")
@CommandPermission("multiverse.portal.config")
public class ConfigCommand extends PortalCommand {

    public ConfigCommand(MultiversePortals plugin) {
        super(plugin);
    }

    @Subcommand("list [filter]")
    @Description("View Global MV Portals Variables.")
    public void onConfigListCommand(@NotNull CommandSender sender,
                                    @NotNull ContentFilter filter) {

        new KeyValueDisplay().withSender(sender)
                .withHeader(String.format("%s===[ Multiverse-Portals Config ]===", ChatColor.RED))
                .withCreator(getPortalsConfigMap())
                .withFilter(filter)
                .withColors(new ColorAlternator(ChatColor.GREEN, ChatColor.GOLD))
                .build()
                .runTaskAsynchronously(this.plugin);
    }

    private ContentCreator<Map<String, Object>> getPortalsConfigMap() {
        return () -> new HashMap<String, Object>() {{
            PortalConfigProperty.valueNames()
                    .forEach(prop -> put(prop, plugin.getMainConfig().get(prop, ChatColor.RED + "NOT SET")));
        }};
    }

    @Subcommand("set")
    @Syntax("<property> <value>")
    @CommandCompletion("@MVPConfigs")
    @Description("Set Global MV Portals Variables.")
    public void onConfigSetCommand(@NotNull CommandSender sender,

                                   @Syntax("<property> <value>")
                                   @Description("Config key, and the value you want to set it to.")
                                   @NotNull PortalProperty<?> property) {

        String propertyName = property.getProperty().getName();
        try {
            this.plugin.getMainConfig().set(propertyName, property.getValue());
        }
        catch (Exception e) {
            sender.sendMessage(String.format("%sThere was an issue setting property '%s' to '%s'.",
                    ChatColor.RED, propertyName, property.getValue()));
            return;
        }

        if (!this.plugin.saveMainConfig()) {
            sender.sendMessage(String.format("%sFailed! Check your console for more details.", ChatColor.RED));
            return;
        }

        sender.sendMessage(String.format("%sSuccess! %sProperty '%s%s%s' is now set to '%s%s%s'.",
                ChatColor.GREEN, ChatColor.WHITE, ChatColor.AQUA, propertyName,
                ChatColor.WHITE, ChatColor.AQUA, property.getValue(), ChatColor.WHITE));
        this.plugin.reloadConfigs(false);
    }
}
