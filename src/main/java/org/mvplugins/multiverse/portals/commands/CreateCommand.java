package org.mvplugins.multiverse.portals.commands;

import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.destination.DestinationInstance;
import org.mvplugins.multiverse.core.locale.message.MessageReplacement.Replace;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.MultiverseWorld;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Optional;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalLocation;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.locale.MVPi18n;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;
import org.mvplugins.multiverse.portals.utils.PortalManager;


@Service
class CreateCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final PortalManager portalManager;

    @Inject
    CreateCommand(@NotNull MultiversePortals plugin,
                  @NotNull PortalManager portalManager) {
        this.plugin = plugin;
        this.portalManager = portalManager;
    }

    @Subcommand("create")
    @CommandPermission("multiverse.portal.create")
    @CommandCompletion("@empty @mvworlds|@destinations")
    @Syntax("<portal-name> [destination]")
    @Description("{@@mv-portals.create.description}")
    void onCreateCommand(
            @NotNull MVCommandIssuer issuer,

            @Flags("resolve=issuerOnly")
            Player player,

            @Flags("resolve=issuerOnly")
            LoadedMultiverseWorld world,

            @Syntax("<portal-name>")
            @Description("{@@mv-portals.create.name.description}")
            String portalName,

            @Optional
            @Syntax("[destination]")
            @Description("{@@mv-portals.create.destination.description}")
            DestinationInstance<?, ?> destination
    ) {
        // todo: maybe make a CommandContext for PortalPlayerSession
        PortalPlayerSession ps = this.plugin.getPortalSession(player);

        MultiverseRegion region = ps.getSelectedRegion();
        if (region == null) {
            return;
        }

        if (!MVPortal.PORTAL_NAME_PATTERN.matcher(portalName).matches()) {
            issuer.sendError(MVPi18n.CREATE_INVALIDNAME);
            return;
        }

        MVPortal portal = this.portalManager.getPortal(portalName);
        PortalLocation location = new PortalLocation(region.getMinimumPoint(), region.getMaximumPoint(), (MultiverseWorld) world);
        if (this.portalManager.addPortal(world, portalName, player.getName(), location)) {
            issuer.sendInfo(MVPi18n.CREATE_SUCCESS, Replace.NAME.with(portalName));
            // If the portal did not exist, ie: we're creating it.
            // we have to re select it, because it would be null
            portal = this.portalManager.getPortal(portalName);

        } else {
            issuer.sendError(MVPi18n.CREATE_ALREADYEXISTS, Replace.NAME.with(portalName));
        }

        ps.selectPortal(portal);
        if (destination != null) {
            portal.setAction(destination.toString());
            this.plugin.savePortalsConfig();
        } else {
            issuer.sendError(MVPi18n.CREATE_ACTIONNOTSET);
        }

        // todo: Automatically get exact destination from player location
        // todo: Automatically get portal destination from player location
    }

    @Service
    private final static class LegacyAlias extends CreateCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin, PortalManager portalManager) {
            super(plugin, portalManager);
        }

        @Override
        @CommandAlias("mvpcreate|mvpc")
        void onCreateCommand(MVCommandIssuer issuer, Player player, LoadedMultiverseWorld world, String portalName,
                             DestinationInstance<?, ?> destination) {
            super.onCreateCommand(issuer, player, world, portalName, destination);
        }
    }
}
