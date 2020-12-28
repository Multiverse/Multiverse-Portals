/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2020.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.utils;

import com.onarandombox.MultiverseCore.commandTools.MVCommandManager;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.commands_acf.ConfigCommand;
import com.onarandombox.MultiversePortals.commands_acf.CreateCommand;
import com.onarandombox.MultiversePortals.commands_acf.DebugCommand;
import com.onarandombox.MultiversePortals.commands_acf.InfoCommand;
import com.onarandombox.MultiversePortals.commands_acf.ListCommand;
import com.onarandombox.MultiversePortals.commands_acf.RemoveCommand;
import com.onarandombox.MultiversePortals.commands_acf.RootCommand;
import com.onarandombox.MultiversePortals.commands_acf.SelectCommand;
import com.onarandombox.MultiversePortals.commands_acf.UsageCommand;
import com.onarandombox.MultiversePortals.commands_acf.WandCommand;
import com.onarandombox.MultiversePortals.enums.PortalConfigProperty;
import com.onarandombox.acf.BukkitCommandCompletionContext;
import com.onarandombox.acf.BukkitCommandExecutionContext;
import com.onarandombox.acf.BukkitCommandIssuer;
import com.onarandombox.acf.ConditionContext;
import com.onarandombox.acf.ConditionFailedException;
import com.onarandombox.acf.InvalidCommandArgument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

public class CommandTools {

    private final MultiversePortals plugin;
    private final PortalManager portalManager;
    private final MVCommandManager manager;

    public CommandTools(@NotNull MultiversePortals plugin) {
        this.plugin = plugin;
        this.portalManager = plugin.getPortalManager();
        this.manager = plugin.getCore().getMVCommandManager();

        // Completions
        this.manager.getCommandCompletions().registerAsyncCompletion("MVPortals", this::suggestMVPortals);
        this.manager.getCommandCompletions().registerStaticCompletion("MVPConfigs", this::suggestMVPConfigs);

        // Contexts
        this.manager.getCommandContexts().registerIssuerOnlyContext(PortalPlayerSession.class, this::derivePortalPlayerSession);
        this.manager.getCommandContexts().registerIssuerAwareContext(MVPortal.class, this::deriveMVPortal);
        this.manager.getCommandContexts().registerContext(PortalProperty.class, this::derivePortalProperty);

        // Conditions
        this.manager.getCommandConditions().addCondition(String.class, "creatablePortalName", this::checkCreatablePortalName);

        // Commands
        this.manager.registerSubModule("mvp", new RootCommand(this.plugin));
        this.manager.registerCommand(new ConfigCommand(this.plugin));
        this.manager.registerCommand(new DebugCommand(this.plugin));
        this.manager.registerCommand(new SelectCommand(this.plugin));
        this.manager.registerCommand(new ListCommand(this.plugin));
        this.manager.registerCommand(new RemoveCommand(this.plugin));
        this.manager.registerCommand(new WandCommand(this.plugin));
        this.manager.registerCommand(new InfoCommand(this.plugin));
        this.manager.registerCommand(new UsageCommand(this.plugin));
        this.manager.registerCommand(new CreateCommand(this.plugin));
    }

    private void checkCreatablePortalName(@NotNull ConditionContext<BukkitCommandIssuer> context,
                                          @NotNull BukkitCommandExecutionContext executionContext,
                                          @NotNull String portalName) {

        if (this.portalManager.isPortal(portalName)) {
            throw new ConditionFailedException("Portal '" + ChatColor.DARK_AQUA + portalName + ChatColor.RED
                    + "' already exists. You can run " + ChatColor.AQUA + "/mvp select " + portalName + ChatColor.RED + " to select it.");
        }
    }

    @NotNull
    private Collection<String> suggestMVPortals(@NotNull BukkitCommandCompletionContext context) {
        return this.portalManager.getPortals(context.getSender()).stream()
                .unordered()
                .map(MVPortal::getName)
                .collect(Collectors.toList());
    }

    @NotNull
    private Collection<String> suggestMVPConfigs() {
        return PortalConfigProperty.valueNames();
    }

    @Nullable
    private PortalPlayerSession derivePortalPlayerSession(@NotNull BukkitCommandExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            if (context.isOptional()) {
                return null;
            }
            throw new InvalidCommandArgument("You need to be a player to run this command.", false);
        }
        PortalPlayerSession portalSession = this.plugin.getPortalSession(player);
        if (portalSession == null) {
            throw new InvalidCommandArgument("There was an issue getting your portal sessions! Please report to the authors.", false);
        }
        return portalSession;
    }

    @Nullable
    private MVPortal deriveMVPortal(@NotNull BukkitCommandExecutionContext context) {
        String portalName = context.popFirstArg();
        if (portalName == null) {
            if (context.isOptional()) {
                return null;
            }
            throw new InvalidCommandArgument("You need to specify a portal name.");
        }

        if (!this.portalManager.isPortal(portalName)) {
            throw new InvalidCommandArgument("You do not have have any portal named '" + portalName + "'.");
        }

        MVPortal portal = this.portalManager.getPortal(portalName, context.getSender());
        if (portal == null) {
            throw new InvalidCommandArgument("You do not have permission to access this portal.");
        }

        return portal;
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

        return new PortalProperty(property, parseValueClass(context, property));
    }

    @NotNull
    private Object parseValueClass(@NotNull BukkitCommandExecutionContext context,
                                   @NotNull PortalConfigProperty<?> property) {

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
        return result;
    }
}
