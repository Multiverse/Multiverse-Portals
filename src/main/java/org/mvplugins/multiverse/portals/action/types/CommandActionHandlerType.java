package org.mvplugins.multiverse.portals.action.types;

import org.bukkit.command.CommandSender;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandlerType;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import java.util.Collection;
import java.util.List;

@Service
final class CommandActionHandlerType extends ActionHandlerType<CommandActionHandlerType, CommandActionHandler> {

    @Inject
    CommandActionHandlerType() {
        super("command");
    }

    @Override
    public @NotNull Attempt<CommandActionHandler, ActionFailureReason> parseHandler(@NotNull CommandSender sender,
                                                                                    @NotNull String action) {
        if (action.isEmpty()) {
            return Attempt.failure(ActionFailureReason.INSTANCE,
                    Message.of(MVPi18n.ACTION_COMMAND_INVALID));
        }
        return Attempt.success(new CommandActionHandler(this, CommandRunner.fromString(action)));
    }

    @Override
    public Collection<String> suggestActions(CommandSender sender, String input) {
        //todo possibly use command map to suggest tab completion
        return List.of("op:", "console:");
    }
}
