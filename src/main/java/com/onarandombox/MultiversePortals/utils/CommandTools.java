/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.utils;

import com.onarandombox.MultiverseCore.commandTools.MVCommandManager;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.commands_acf.ConfigCommand;
import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;
import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.InvalidCommandArgument;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

public class CommandTools {

    private final MultiversePortals plugin;
    private final MVCommandManager manager;

    public CommandTools(@NotNull MultiversePortals plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCore().getMVCommandManager();

        // Completions
        this.manager.getCommandCompletions().registerStaticCompletion("MVPConfigs", this::suggestMVPConfigs);

        // Contexts
        this.manager.getCommandContexts().registerContext(PortalProperty.class, this::derivePortalProperty);

        // Conditions

        // Commands
        this.manager.registerCommand(new ConfigCommand(this.plugin));
    }

    @NotNull
    private Collection<String> suggestMVPConfigs() {
        return PortalConfigProperty.valueNames();
    }

    @NotNull
    private PortalProperty<?> derivePortalProperty(@NotNull BukkitCommandExecutionContext context) {
        int argLength = context.getArgs().size();
        if (argLength == 0) {
            throw new InvalidCommandArgument("You need to specify a config property and value to set.");
        }
        if (argLength == 1) {
            throw new InvalidCommandArgument("You need to specify a value to set.");
        }

        String propertyString = context.popFirstArg();
        PortalConfigProperty<?> property = PortalConfigProperty.getByName(propertyString);
        if (property == null) {
            throw new InvalidCommandArgument("'" + propertyString + "' is not a valid config property.");
        }

        Class<?> valueType = property.getType();
        String value = context.getFirstArg();
        Object result = this.manager.getCommandContexts().getResolver(valueType).getContext(context);
        if (result == null) {
            context.getSender().sendMessage(ChatColor.RED + "'" + value + "' is not a valid value.");
            context.getSender().sendMessage(ChatColor.RED + "Value need to be a " + valueType.getTypeName());
            throw new InvalidCommandArgument();
        }
        if (result instanceof Integer && ((int) result) < 0) {
            throw new InvalidCommandArgument(ChatColor.RED + "Value cannot be a negative number.");
        }

        return new PortalProperty(property, result);
    }


}
