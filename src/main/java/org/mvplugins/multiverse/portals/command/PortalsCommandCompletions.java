package org.mvplugins.multiverse.portals.command;

import org.mvplugins.multiverse.core.command.MVCommandCompletions;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.core.config.handle.PropertyModifyAction;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandCompletionContext;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.external.vavr.control.Try;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Collection;
import java.util.Collections;

@Service
public class PortalsCommandCompletions {

    private final PortalManager portalManager;
    private final PortalsConfig portalsConfig;

    @Inject
    PortalsCommandCompletions(@NotNull PortalManager portalManager,
                              @NotNull PortalsConfig portalsConfig,
                              @NotNull MVCommandManager commandManager) {
        this.portalManager = portalManager;
        this.portalsConfig = portalsConfig;
        registerCompletions(commandManager.getCommandCompletions());
    }

    private void registerCompletions(MVCommandCompletions commandCompletions) {
        commandCompletions.registerAsyncCompletion("mvportals", this::suggestPortals);
        commandCompletions.registerStaticCompletion("portalconfigproperties", portalsConfig.getStringPropertyHandle().getAllPropertyNames());
        commandCompletions.registerAsyncCompletion("portalconfigvalues", this::suggestPortalConfigValues);
        commandCompletions.registerAsyncCompletion("portalproperties", this::suggestPortalPropertyNames);
        commandCompletions.registerAsyncCompletion("portalpropertynames", this::suggestPortalPropertyNames);
        commandCompletions.registerAsyncCompletion("portalpropertyvalues", this::suggestPortalPropertyValues);

        commandCompletions.setDefaultCompletion("mvportals", MVPortal.class);
    }

    private Collection<String> suggestPortalConfigValues(BukkitCommandCompletionContext context) {
        return Try.of(() -> context.getContextValue(String.class))
                .map(propertyName -> portalsConfig.getStringPropertyHandle()
                        .getSuggestedPropertyValue(propertyName, context.getInput(), PropertyModifyAction.SET))
                .getOrElse(Collections.emptyList());
    }

    private Collection<String> suggestPortals(BukkitCommandCompletionContext context) {
        return this.portalManager.getPortals(context.getSender()).stream()
                .map(MVPortal::getName)
                .toList();
    }

    private Collection<String> suggestPortalPropertyNames(BukkitCommandCompletionContext context) {
        return Try.of(() -> context.getContextValue(MVPortal.class))
                .map(portal -> portal.getStringPropertyHandle().getAllPropertyNames())
                .getOrElse(Collections.emptyList());
    }

    private Collection<String> suggestPortalPropertyValues(BukkitCommandCompletionContext context) {
        return Try.of(() -> {
            MVPortal portal = context.getContextValue(MVPortal.class);
            String propertyName = context.getContextValue(String.class);
            return portal.getStringPropertyHandle().getSuggestedPropertyValue(
                    propertyName,
                    context.getInput(),
                    PropertyModifyAction.SET,
                    context.getSender()
            );
        }).getOrElse(Collections.emptyList());
    }
}
