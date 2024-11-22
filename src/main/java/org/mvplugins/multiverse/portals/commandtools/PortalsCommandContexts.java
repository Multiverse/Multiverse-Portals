package org.mvplugins.multiverse.portals.commandtools;

import org.mvplugins.multiverse.core.commandtools.MVCommandContexts;
import org.mvplugins.multiverse.external.acf.commands.BukkitCommandExecutionContext;
import org.mvplugins.multiverse.external.acf.commands.InvalidCommandArgument;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
public class PortalsCommandContexts {

    private final MultiversePortals plugin;
    private final PortalManager portalManager;

    @Inject
    PortalsCommandContexts(@NotNull MultiversePortals plugin, @NotNull PortalManager portalManager) {
        this.plugin = plugin;
        this.portalManager = portalManager;
    }

    public void registerContexts(MVCommandContexts commandContexts) {
        commandContexts.registerIssuerAwareContext(MVPortal.class, this::parseMVPortal);
    }

    private MVPortal parseMVPortal(BukkitCommandExecutionContext context) {
        String resolve = context.getFlagValue("resolve", "");

        MVPortal playerSelectedPortal = context.getIssuer().isPlayer()
                ? this.plugin.getPortalSession(context.getPlayer()).getSelectedPortal()
                : null;

        if (resolve.equals("issuerOnly")) {
            if (context.getIssuer().isPlayer() && playerSelectedPortal != null) {
                return playerSelectedPortal;
            }
            if (context.isOptional()) {
                return null;
            }
            throw new InvalidCommandArgument("This command can only be used by a player that has selected a portal with `/mvp select`.");
        }

        String portalName = context.getFirstArg();
        MVPortal portal = this.portalManager.getPortal(portalName);

        if (resolve.equals("issuerAware")) {
            if (portal != null) {
                context.popFirstArg();
                return portal;
            }
            if (context.getIssuer().isPlayer() && playerSelectedPortal != null) {
                return playerSelectedPortal;
            }
            if (context.isOptional()) {
                return null;
            }
            throw new InvalidCommandArgument("Either this command can only be used by a player that has selected a portal with `/mvp select` or the portal '" + portalName + "' does not exist.");
        }

        if (portal != null) {
            context.popFirstArg();
            return portal;
        }
        if (context.isOptional()) {
            return null;
        }
        throw new InvalidCommandArgument("Portal '" + portalName + "' does not exist.");
    }
}
