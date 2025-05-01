package org.mvplugins.multiverse.portals.commands;

import org.jvnet.hk2.annotations.Contract;
import org.mvplugins.multiverse.core.command.MultiverseCommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;

/**
 * Base class for all portal commands
 */
@Contract
@CommandAlias("mvp")
public abstract class PortalsCommand extends MultiverseCommand {
}
