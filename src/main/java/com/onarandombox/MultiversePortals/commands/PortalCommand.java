/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.pneumaticraft.commandhandler.multiverse.Command;
/**
 * Convenience class so we don't have to cast each time.
 * @author fernferret
 *
 */
public abstract class PortalCommand extends Command {

    protected MultiversePortals plugin;
    public PortalCommand(MultiversePortals plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public abstract void runCommand(CommandSender sender, List<String> args);

}
