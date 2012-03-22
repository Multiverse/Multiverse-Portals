/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiverseCore.utils.LocationManipulation;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalLocation;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.utils.MultiverseRegion;

public class CreateCommand extends PortalCommand {

    public CreateCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Create a Portal");
        this.setCommandUsage("/mvp create" + ChatColor.GREEN + " {NAME}" + ChatColor.GOLD + " [DESTINATION]");
        this.setArgRange(1, 2);
        this.addKey("mvp create");
        this.addKey("mvpc");
        this.addKey("mvpcreate");
        this.setPermission("multiverse.portal.create", "Creates a new portal, assuming you have a region selected.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        Player p = null;
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command must be run by a player");
            return;
        }
        p = (Player) sender;

        if (!this.plugin.getCore().getMVWorldManager().isMVWorld(p.getWorld().getName())) {
            this.plugin.getCore().showNotMVWorldMessage(sender, p.getWorld().getName());
            return;
        }
        MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(p.getWorld().getName());

        PortalPlayerSession ps = this.plugin.getPortalSession(p);

        MultiverseRegion r = ps.getSelectedRegion();
        if (r == null) {
            return;
        }
        MVPortal portal = this.plugin.getPortalManager().getPortal(args.get(0));
        PortalLocation location = new PortalLocation(r.getMinimumPoint(), r.getMaximumPoint(), world);
        if (this.plugin.getPortalManager().addPortal(world, args.get(0), p.getName(), location)) {
            sender.sendMessage("New portal(" + ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + ") created and selected!");
            // If the portal did not exist, ie: we're creating it.
            // we have to re select it, because it would be null
            portal = this.plugin.getPortalManager().getPortal(args.get(0));

        } else {
            sender.sendMessage("New portal(" + ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + ") was NOT created!");
            sender.sendMessage("It already existed and has been selected.");
        }

        ps.selectPortal(portal);

        if (args.size() > 1 && portal != null) {
            String dest = args.get(1);
            if (dest.equalsIgnoreCase("here")) {
                MVPortal standingIn = ps.getUncachedStandingInPortal();
                if (standingIn != null) {
                    // If they're standing in a portal. treat it differently, niftily you might say...
                    String cardinal = LocationManipulation.getDirection(p.getLocation());
                    portal.setDestination("p:" + standingIn.getName() + ":" + cardinal);
                } else {
                    portal.setExactDestination(p.getLocation());
                }
            } else if (dest.matches("(i?)cannon-[\\d]+(\\.[\\d]+)?")) {
                // We found a Cannon Destination!
                Location l = p.getLocation();
                try {
                    Double speed = Double.parseDouble(args.get(1).split("-")[1]);
                    portal.setDestination("ca:" + l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + ":" + l.getPitch() + ":" + l.getYaw() + ":" + speed);
                } catch (NumberFormatException e) {
                    portal.setDestination("i:invalid");
                }

            } else {
                portal.setDestination(args.get(1));
            }

        }
    }
}
