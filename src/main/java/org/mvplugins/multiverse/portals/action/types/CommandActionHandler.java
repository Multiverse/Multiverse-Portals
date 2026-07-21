package org.mvplugins.multiverse.portals.action.types;

import org.bukkit.entity.Entity;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandler;

final class CommandActionHandler extends ActionHandler<CommandActionHandlerType, CommandActionHandler> {

    private final CommandRunner commandRunner;

    CommandActionHandler(CommandActionHandlerType handlerType, CommandRunner commandRunner) {
        super(handlerType);
        this.commandRunner = commandRunner;
    }

    @Override
    public @NotNull Attempt<Void, ActionFailureReason> runAction(@NotNull MVPortal portal, @NotNull Entity entity) {
        commandRunner.runCommand(entity);
        return Attempt.success(null);
    }

    @Override
    public @NotNull Message actionDescription(Entity entity) {
        return commandRunner.actionDescription(entity);
    }

    @Override
    public @NotNull String serialise() {
        return commandRunner.rawCmd;
    }
}
