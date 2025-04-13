package org.mvplugins.multiverse.portals.command;

import org.mvplugins.multiverse.core.command.MVCommandCompletions;
import org.mvplugins.multiverse.core.command.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandCompletionContext;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.enums.PortalConfigProperty;
import org.mvplugins.multiverse.portals.enums.SetProperties;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Collection;

@Service
public class PortalsCommandCompletions {

    private final PortalManager portalManager;

    @Inject
    PortalsCommandCompletions(@NotNull PortalManager portalManager, @NotNull MVCommandManager commandManager) {
        this.portalManager = portalManager;
        registerCompletions(commandManager.getCommandCompletions());
    }

    private void registerCompletions(MVCommandCompletions commandCompletions) {
        commandCompletions.registerAsyncCompletion("mvportals", this::suggestPortals);
        commandCompletions.registerStaticCompletion("setproperties", commandCompletions.suggestEnums(SetProperties.class));
        commandCompletions.registerStaticCompletion("portalconfigproperty", commandCompletions.suggestEnums(PortalConfigProperty.class));

        commandCompletions.setDefaultCompletion("mvportals", MVPortal.class);
    }

    private Collection<String> suggestPortals(BukkitCommandCompletionContext context) {
        return this.portalManager.getPortals(context.getSender()).stream()
                .map(MVPortal::getName)
                .toList();
    }
}
