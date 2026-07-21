package org.mvplugins.multiverse.portals.commands;

import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.locale.MVPi18n;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
class RemoveCommand extends PortalsCommand {

    private final PortalManager portalManager;

    @Inject
    RemoveCommand(@NotNull PortalManager portalManager) {
        this.portalManager = portalManager;
    }

    @Subcommand("remove")
    @CommandPermission("multiverse.portal.remove")
    @CommandCompletion("@mvportals")
    @Syntax("<portal-name>")
    @Description("{@@mv-portals.remove.description}")
    void onRemoveCommand(
            MVCommandIssuer issuer,

            @Syntax("<portal-name>")
            @Description("{@@mv-portals.remove.name.description}")
            String portalName
    ) {
        if (!this.portalManager.isPortal(portalName)) {
            issuer.sendError(MVPi18n.REMOVE_NOTFOUND, Replace.NAME.with(portalName));
            return;
        }

        MVPortal portal = this.portalManager.removePortal(portalName, true);
        issuer.sendInfo(MVPi18n.REMOVE_SUCCESS, Replace.NAME.with(portal.getName()));
    }

    @Service
    private final static class LegacyAlias extends RemoveCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(PortalManager portalManager) {
            super(portalManager);
        }

        @Override
        @CommandAlias("mvpremove|mvpr")
        void onRemoveCommand(MVCommandIssuer issuer, String portalName) {
            super.onRemoveCommand(issuer, portalName);
        }
    }
}
