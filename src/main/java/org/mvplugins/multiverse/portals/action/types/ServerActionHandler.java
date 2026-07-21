package org.mvplugins.multiverse.portals.action.types;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.action.ActionFailureReason;
import org.mvplugins.multiverse.portals.action.ActionHandler;
import org.mvplugins.multiverse.portals.locale.MVPi18n;


final class ServerActionHandler extends ActionHandler<ServerActionHandlerType, ServerActionHandler> {

    private final MultiversePortals plugin;
    private final String serverName;

    ServerActionHandler(ServerActionHandlerType handlerType, MultiversePortals plugin, String serverName) {
        super(handlerType);
        this.plugin = plugin;
        this.serverName = serverName;
    }

    @Override
    public @NotNull Attempt<Void, ActionFailureReason> runAction(@NotNull MVPortal portal, @NotNull Entity entity) {
        if (!(entity instanceof Player player)) {
            return Attempt.failure(ActionFailureReason.INSTANCE,
                    Message.of(MVPi18n.ACTION_SERVER_PLAYERSONLY));
        }
        return Try
                .run(() -> {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Connect");
                    out.writeUTF(serverName);
                    player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
                })
                .map(Attempt::<Void, ActionFailureReason>success)
                .recover(ex -> Attempt.failure(ActionFailureReason.INSTANCE,
                        Message.of(MVPi18n.ACTION_SERVER_PROXYERROR, Replace.ERROR.with(ex))))
                .getOrElse(() -> Attempt.failure(ActionFailureReason.INSTANCE,
                        Message.of(MVPi18n.ACTION_SERVER_PROXYUNKNOWNERROR)));
    }

    @Override
    public Message actionDescription(Entity entity) {
        return Message.of(MVPi18n.ACTION_SERVER_DESCRIPTION, Replace.NAME.with(serverName));
    }

    @Override
    public @NotNull String serialise() {
        return serverName;
    }
}
