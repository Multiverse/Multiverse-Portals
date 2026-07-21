package org.mvplugins.multiverse.portals.action.types;

import org.bukkit.command.CommandSender;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandlerType;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import java.util.Collection;

@Service
final class ServerActionHandlerType extends ActionHandlerType<ServerActionHandlerType, ServerActionHandler> {

    private final MultiversePortals plugin;
    private final BungeeServerList bungeeServerList;

    @Inject
    ServerActionHandlerType(@NotNull MultiversePortals plugin, @NotNull BungeeServerList bungeeServerList) {
        super("server");
        this.plugin = plugin;
        this.bungeeServerList = bungeeServerList;
    }

    @Override
    public @NotNull Attempt<ServerActionHandler, ActionFailureReason> parseHandler(@NotNull CommandSender sender,
                                                                                   @NotNull String action) {
        if (action.isEmpty()) {
            return Attempt.failure(ActionFailureReason.INSTANCE,
                    Message.of(MVPi18n.ACTION_SERVER_INVALID));
        }
        return Attempt.success(new ServerActionHandler(this, plugin, action));
    }

    @Override
    public @NotNull Collection<String> suggestActions(@NotNull CommandSender sender, @NotNull String input) {
        return bungeeServerList.getServerNames();
    }
}
