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
import org.mvplugins.multiverse.portals.enums.PortalConfigProperty;
import org.mvplugins.multiverse.portals.enums.SetProperties;
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
        commandCompletions.registerStaticCompletion("setproperties", commandCompletions.suggestEnums(SetProperties.class));
        commandCompletions.registerStaticCompletion("portalconfigproperties", portalsConfig.getStringPropertyHandle().getAllPropertyNames());
        commandCompletions.registerAsyncCompletion("portalconfigvalues", this::suggestPortalConfigValues);

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
}
