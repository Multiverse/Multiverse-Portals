package org.mvplugins.multiverse.portals.commands;

import com.dumptruckman.minecraft.util.Logging;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
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
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MVPortal;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.mvplugins.multiverse.portals.PortalLocation;
import org.mvplugins.multiverse.portals.PortalPlayerSession;
import org.mvplugins.multiverse.portals.enums.SetProperties;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;

@Service
@CommandAlias("mvp")
class ModifyCommand extends PortalsCommand {

    private final MultiversePortals plugin;
    private final WorldManager worldManager;

    @Inject
    ModifyCommand(@NotNull MultiversePortals plugin, @NotNull WorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }

    @CommandAlias("mvpmodify|mvpm")
    @Subcommand("modify")
    @CommandPermission("multiverse.portal.modify")
    @CommandCompletion("@mvportals @setproperties @empty")
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
            SetProperties property,

            @Single
            @Syntax("<value>")
            @Description("The value to set.")
            String value
    ) {
        Logging.info("Modifying portal: " + portal.getName() + " property: " + property + " value: " + value);
        // Simply chop off the rest, if they have loc, that's good enough!
        if (property == SetProperties.loc || property == SetProperties.location) {
            if (!issuer.isPlayer()) {
                issuer.sendMessage("You must be a player to use location property!");
                return;
            }
            this.setLocation(portal, issuer.getPlayer());
            return;
        }
        String propertyString = property.toString().toLowerCase();
        if (this.setProperty(portal, propertyString, value)) {
            issuer.sendMessage("Property " + property + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.GREEN + " was set to " + ChatColor.AQUA + value);
        } else {
            issuer.sendMessage("Property " + property + " of Portal " + ChatColor.YELLOW + portal.getName() + ChatColor.RED + " was NOT set to " + ChatColor.AQUA + value);
            if (propertyString.equalsIgnoreCase("dest") || propertyString.equalsIgnoreCase("destination")) {
                issuer.sendMessage("Multiverse could not find the destination: " + ChatColor.GOLD + value);
            }
        }
    }

    private boolean setProperty(MVPortal selectedPortal, String property, String value) {
        return selectedPortal.setProperty(property, value);
    }

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
}
