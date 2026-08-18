package org.mvplugins.multiverse.portals.action;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.core.locale.message.Message;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.utils.result.Attempt;
import org.mvplugins.multiverse.external.jetbrains.annotations.ApiStatus;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.vavr.control.Option;
import org.mvplugins.multiverse.portals.locale.MVPi18n;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.mvplugins.multiverse.core.locale.message.MessageReplacement.replace;

/**
 * Provides various actions that can be performed when a portal is used. This extends to more than just teleporting.
 * <br/>
 * Built-in action handler types include "server", "command", and "multiverse-destination", which can be used to
 * perform actions such as sending players to different servers, executing commands, or teleporting to predefined
 * destinations, respectively.
 * <br/>
 * You can also create custom action handler types by implementing the {@link ActionHandlerType} class and
 * registering them with this provider.
 *
 * @since 5.2
 */
@ApiStatus.AvailableSince("5.2")
@Service
public final class ActionHandlerProvider {

    private final Map<String, ActionHandlerType<?, ?>> handlerTypeMap = new HashMap<>();

    /**
     * Register a new action handler type.
     *
     * @param handlerType The action handler type to register
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public void registerHandlerType(@NotNull ActionHandlerType<?, ?> handlerType) {
        handlerTypeMap.put(handlerType.getName(), handlerType);
    }

    /**
     * Get all registered handler types names.
     *
     * @return Names of all registered handler types
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public @NotNull Collection<String> getAllHandlerTypeNames() {
        return handlerTypeMap.keySet();
    }

    /**
     * Get the handler type by name.
     *
     * @param name  The name of the handler type
     * @return The handler type, or a failure reason if not found
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public @NotNull Attempt<? extends ActionHandlerType<?, ?>, ActionFailureReason> getHandlerType(@NotNull String name) {
        return Option.of(handlerTypeMap.get(name))
                .map(Attempt::<ActionHandlerType<?, ?>, ActionFailureReason>success)
                .getOrElse(() -> Attempt.failure(ActionFailureReason.INSTANCE,
                        Message.of(MVPi18n.ACTION_UNKNOWN_TYPE,
                                Replace.NAME.with(name),
                                replace("{types}").with(String.join(", ", handlerTypeMap.keySet())))));
    }

    /**
     * Parse the action handler from the given type and action string, using the console as the command sender.
     *
     * @param actionType The type of the action handler
     * @param action     The action string
     * @return The parsed action handler, or a failure reason if type is invalid or action string is of invalid format.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public @NotNull Attempt<? extends ActionHandler<?, ?>, ActionFailureReason> parseHandler(@NotNull String actionType,
                                                                                             @NotNull String action) {
        return parseHandler(Bukkit.getConsoleSender(), actionType, action);
    }

    /**
     * Parse the action handler from the given type and action string.
     *
     * @param sender     The command sender parsing the action
     * @param actionType The type of the action handler
     * @param action     The action string
     * @return The parsed action handler, or a failure reason if type is invalid or action string is of invalid format.
     *
     * @since 5.2
     */
    @ApiStatus.AvailableSince("5.2")
    public @NotNull Attempt<? extends ActionHandler<?, ?>, ActionFailureReason> parseHandler(@NotNull CommandSender sender,
                                                                                             @NotNull String actionType,
                                                                                             @NotNull String action) {
        return getHandlerType(actionType)
                .mapAttempt(actionHandlerType -> actionHandlerType.parseHandler(sender, action));
    }
}
