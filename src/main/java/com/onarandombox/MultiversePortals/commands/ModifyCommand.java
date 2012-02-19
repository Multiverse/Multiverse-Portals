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
import com.onarandombox.MultiversePortals.enums.SetProperties;
import com.onarandombox.MultiversePortals.utils.MultiverseRegion;

/**
 * Allows modification of portal location, destination and owner. NOT name at this time.
 *
 * @author fernferret
 */
public class ModifyCommand extends PortalCommand {

    public ModifyCommand(MultiversePortals plugin) {
        super(plugin);
        this.setName("Modify a Portal (Set a value)");
        this.setCommandUsage("/mvp modify" + ChatColor.GREEN + " {PROPERTY}" + ChatColor.GOLD + " [VALUE] -p [PORTAL]");
        this.setArgRange(1, 4);
        this.addKey("mvp modify");
        this.addKey("mvpmodify");
        this.addKey("mvpm");
        this.setPermission("multiverse.portal.modify", "Allows you to modify all values that can be set.", PermissionDefault.OP);
    }

    @Override
    public void runCommand(CommandSender sender, List<String> args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Sorry, right now this command is player only :(");
            return;
        }

        Player player = (Player) sender;
        if (!validateAction(args.get(0))) {
            sender.sendMessage("Sorry, you cannot " + ChatColor.AQUA + "SET" + ChatColor.WHITE + " the property " +
                    ChatColor.DARK_AQUA + args.get(0) + ChatColor.WHITE + ".");
            return;
        }

        if (!validCommand(args, SetProperties.valueOf(args.get(0)))) {
            sender.sendMessage("Looks like you forgot or added an extra parameter.");
            sender.sendMessage("Please try again, or see our Wiki for help!");
            return;
        }
        String portalName = extractPortalName(args);
        MVPortal selectedPortal = null;
        // If they provided -p PORTALNAME, try to retrieve it
        if (portalName != null) {
            selectedPortal = this.plugin.getPortalManager().getPortal(portalName);
            if (selectedPortal == null) {
                sender.sendMessage("Sorry, the portal " + ChatColor.RED + portalName + ChatColor.WHITE + " did not exist!");
                return;
            }
        }
        // If they didn't provide -p, then try to use their selected portal
        if (selectedPortal == null) {
            selectedPortal = this.getUserSelectedPortal(player);
        }

        if (selectedPortal == null) {
            sender.sendMessage("You need to select a portal using " + ChatColor.AQUA + "/mvp select {NAME}");
            sender.sendMessage("or append " + ChatColor.DARK_AQUA + "-p {PORTAL}" + ChatColor.WHITE + " to this command.");
            return;
        } else {
            portalName = selectedPortal.getName();
        }

        if (portalName != null) {
            // Simply chop off the rest, if they have loc, that's good enough!
            if (SetProperties.valueOf(args.get(0)) == SetProperties.loc || SetProperties.valueOf(args.get(0)) == SetProperties.location) {
                this.setLocation(selectedPortal, player);
                return;
            }

            if (SetProperties.valueOf(args.get(0)) == SetProperties.dest || SetProperties.valueOf(args.get(0)) == SetProperties.destination) {
                if (args.get(1).equalsIgnoreCase("here")) {
                    PortalPlayerSession ps = this.plugin.getPortalSession(player);
                    MVPortal standingIn = ps.getUncachedStandingInPortal();
                    Location l = player.getLocation();
                    if (standingIn != null) {
                        String cardinal = LocationManipulation.getDirection(l);
                        args.set(1, "p:" + standingIn.getName() + ":" + cardinal);
                    } else {
                        args.set(1, "e:" + l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + ":" + l.getPitch() + ":" + l.getYaw());
                    }
                } else if (args.get(1).matches("(i?)cannon-[\\d]+(\\.[\\d]+)?")) {
                    // We found a Cannon Destination!
                    Location l = player.getLocation();
                    try {
                        Double speed = Double.parseDouble(args.get(1).split("-")[1]);
                        args.set(1, "ca:" + l.getWorld().getName() + ":" + l.getX() + "," + l.getY() + "," + l.getZ() + ":" + l.getPitch() + ":" + l.getYaw() + ":" + speed);
                    } catch (NumberFormatException e) {
                    }

                }
            }

            if (this.setProperty(selectedPortal, args.get(0), args.get(1))) {
                sender.sendMessage("Property " + args.get(0) + " of Portal " + ChatColor.YELLOW + selectedPortal.getName() + ChatColor.GREEN + " was set to " + ChatColor.AQUA + args.get(1));
            } else {
                sender.sendMessage("Property " + args.get(0) + " of Portal " + ChatColor.YELLOW + selectedPortal.getName() + ChatColor.RED + " was NOT set to " + ChatColor.AQUA + args.get(1));
                if (args.get(0).equalsIgnoreCase("dest") || args.get(0).equalsIgnoreCase("destination")) {
                    sender.sendMessage("Multiverse could not find the destination: " + ChatColor.GOLD + args.get(1));
                }
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
            MultiverseWorld world = this.plugin.getCore().getMVWorldManager().getMVWorld(player.getWorld().getName());
            PortalLocation location = new PortalLocation(r.getMinimumPoint(), r.getMaximumPoint(), world);
            selectedPortal.setPortalLocation(location);
            player.sendMessage("Portal location has been set to your " + ChatColor.GREEN + "selection" + ChatColor.WHITE + "!");
        }
    }

    private MVPortal getUserSelectedPortal(Player player) {
        PortalPlayerSession ps = this.plugin.getPortalSession(player);
        return ps.getSelectedPortal();
    }

    private boolean validCommand(List<String> args, SetProperties property) {
        // This means that they did not specify the -p or forgot the [PORTAL]

        if (property == SetProperties.loc && args.size() % 2 == 0) {
            return false;
        } else if (property != SetProperties.loc && args.size() % 2 != 0) {
            return false;
        }
        return true;
    }

    private String extractPortalName(List<String> args) {
        if (!args.contains("-p")) {
            return null;
        }
        int index = args.indexOf("-p");
        // Now we remove the -p
        args.remove(index);
        // Now we remove and return the portalname
        return args.remove(index);
    }

    protected static boolean validateAction(String property) {
        try {
            SetProperties.valueOf(property);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
