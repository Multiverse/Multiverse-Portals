/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands_acf;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.acf.BaseCommand;


public abstract class PortalCommand extends BaseCommand {

    protected final MultiversePortals plugin;

    protected PortalCommand(MultiversePortals plugin) {
        this.plugin = plugin;
    }
}
