package org.mvplugins.multiverse.portals.commands;

import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.command.MultiverseCommand;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Contract;

/**
 * Base class for all portal commands
 */
@Contract
public abstract class PortalsCommand extends MultiverseCommand {
    protected PortalsCommand(@NotNull MVCommandManager commandManager) {
        super(commandManager, "mvp");
    }
}
