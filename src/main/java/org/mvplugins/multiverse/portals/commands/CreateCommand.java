package org.mvplugins.multiverse.portals.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.api.destination.DestinationInstance;
import org.mvplugins.multiverse.core.api.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.commandtools.MVCommandManager;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalLocation;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;
import org.mvplugins.multiverse.portals.utils.PortalManager;

@Service
@CommandAlias("mvp")
public class CreateCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final PortalManager portalManager;

    @Inject
    protected CreateCommand(
            @NotNull MVCommandManager commandManager,
            @NotNull MultiversePortals plugin,
            @NotNull PortalManager portalManager) {
        super(commandManager);
        this.plugin = plugin;
        this.portalManager = portalManager;
    }

    @CommandAlias("mvpcreate|mvpc")
    @Subcommand("create")
    @CommandPermission("multiverse.portal.create")
    @CommandCompletion("@empty @mvworlds|@destinations")
    @Syntax("<portal-name> <destination>")
    @Description("Creates a new portal, assuming you have a region selected.")
    void onCreateCommand(
            @Flags("resolve=issuerOnly")
            Player player,

            @Flags("resolve=issuerOnly")
            LoadedMultiverseWorld world,

            @Syntax("<portal-name>")
            String portalName,

            @Syntax("<destination>")
            DestinationInstance<?, ?> destination
    ) {
        // todo: maybe make a CommandContext for PortalPlayerSession
        PortalPlayerSession ps = this.plugin.getPortalSession(player);

        MultiverseRegion region = ps.getSelectedRegion();
        if (region == null) {
            return;
        }

        if (!MVPortal.PORTAL_NAME_PATTERN.matcher(portalName).matches()) {
            player.sendMessage(String.format("%sInvalid portal name. It must not contain dot or special characters.", ChatColor.RED));
            return;
        }

        MVPortal portal = this.portalManager.getPortal(portalName);
        PortalLocation location = new PortalLocation(region.getMinimumPoint(), region.getMaximumPoint(), world);
        if (this.portalManager.addPortal(world, portalName, player.getName(), location)) {
            player.sendMessage("New portal (" + ChatColor.DARK_AQUA + portalName + ChatColor.WHITE + ") created and selected!");
            // If the portal did not exist, ie: we're creating it.
            // we have to re select it, because it would be null
            portal = this.portalManager.getPortal(portalName);

        } else {
            player.sendMessage("New portal (" + ChatColor.DARK_AQUA + portalName + ChatColor.WHITE + ") was NOT created!");
            player.sendMessage("It already existed and has been selected.");
        }

        ps.selectPortal(portal);
        portal.setDestination(destination);

        // todo: Automatically get exact destination from player location
        // todo: Automatically get portal destination from player location
    }
}
