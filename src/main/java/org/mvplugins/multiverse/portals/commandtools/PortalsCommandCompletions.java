package org.mvplugins.multiverse.portals.commandtools;

import org.mvplugins.multiverse.core.commandtools.MVCommandCompletions;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandCompletionContext;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.enums.SetProperties;
import org.mvplugins.multiverse.portals.utils.PortalManager;

import java.util.Collection;

@Service
public class PortalsCommandCompletions {

    private final PortalManager portalManager;

    @Inject
    PortalsCommandCompletions(@NotNull PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    public void registerCompletions(MVCommandCompletions commandCompletions) {
        commandCompletions.registerAsyncCompletion("mvportals", this::suggestPortals);
        commandCompletions.registerStaticCompletion("setproperties", commandCompletions.suggestEnums(SetProperties.class));

        commandCompletions.setDefaultCompletion("mvportals", MVPortal.class);
    }

    private Collection<String> suggestPortals(BukkitCommandCompletionContext context) {
        return this.portalManager.getPortals(context.getSender()).stream()
                .map(MVPortal::getName)
                .toList();
    }
}
