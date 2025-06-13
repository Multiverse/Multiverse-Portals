package org.mvplugins.multiverse.portals.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mvplugins.multiverse.core.command.LegacyAliasCommand;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.command.MVCommandIssuer;
import org.mvplugins.multiverse.core.command.MVCommandManager;import org.mvplugins.multiverse.external.acf.commands.annotation.CommandAlias;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandCompletion;
import org.mvplugins.multiverse.external.acf.commands.annotation.CommandPermission;
import org.mvplugins.multiverse.external.acf.commands.annotation.Description;
import org.mvplugins.multiverse.external.acf.commands.annotation.Flags;
import org.mvplugins.multiverse.external.acf.commands.annotation.Single;
import org.mvplugins.multiverse.external.acf.commands.annotation.Subcommand;
import org.mvplugins.multiverse.external.acf.commands.annotation.Syntax;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalLocation;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.enums.SetProperties;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;

@Service
class ModifyCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final WorldManager worldManager;

    @Inject
    ModifyCommand(@NotNull MultiversePortals plugin, @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @Subcommand("modify")
    @CommandPermission("multiverse.portal.modify")
    @CommandCompletion("@mvportals @portalpropertynames @portalpropertyvalues")
    @Syntax("[portal] <property> <value>")
    @Description("Allows you to modify all values that can be set.")
    public void onModifyCommand(
            MVCommandIssuer issuer,

            @Flags("resolve=issuerAware")
            @Syntax("[portal]")
            @Description("The portal to modify.")
            MVPortal portal,

            @Syntax("<property>")
            @Description("The property to modify.")
            String property,

            @Single
            @Syntax("<value>")
            @Description("The value to set.")
            String value
    ) {
        Logging.info("Modifying portal: " + portal.getName() + " property: " + property + " value: " + value);
        // todo: set location property
        portal.getStringPropertyHandle().setPropertyString(property, value)
                .onSuccess(ignore -> {
                    this.plugin.savePortalsConfig();
                    issuer.sendMessage("Property " + property + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.GREEN + " was set to " + ChatColor.AQUA + value);
                }).onFailure(failure -> {
                    issuer.sendMessage("Property " + property + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.RED + " was NOT set to " + ChatColor.AQUA + value);
                });
    }

    // todo: set location property
    private void setLocation(MVPortal selectedPortal, Player player) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        MultiverseRegion r = ps.getSelectedRegion();
        if (r != null) {
            LoadedMultiverseWorld world = this.worldManager.getLoadedWorld(player.getWorld().getName()).getOrNull();
            PortalLocation location = new PortalLocation(r.getMinimumPoint(), r.getMaximumPoint(), world);
            selectedPortal.setPortalLocation(location);
            player.sendMessage("Portal location has been set to your " + ChatColor.GREEN + "selection" + ChatColor.WHITE + "!");
        }
    }

    @Service
    private final static class LegacyAlias extends ModifyCommand implements LegacyAliasCommand {
        @Inject
        LegacyAlias(MultiversePortals plugin, WorldManager worldManager) {
            super(plugin, worldManager);
        }

        @Override
        @CommandAlias("mvpmodify|mvpm")
        public void onModifyCommand(MVCommandIssuer issuer, MVPortal portal, String property, String value) {
            super.onModifyCommand(issuer, portal, property, value);
        }
    }
}
