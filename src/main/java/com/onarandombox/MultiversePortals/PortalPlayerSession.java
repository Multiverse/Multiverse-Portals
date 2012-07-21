/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals;

import java.util.Date;
import java.util.logging.Level;

import com.onarandombox.MultiversePortals.enums.MoveType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.fernferret.allpay.multiverse.GenericBank;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.utils.MultiverseRegion;
import com.onarandombox.MultiversePortals.utils.PortalManager;

public class PortalPlayerSession {
    private MultiversePortals plugin;
    private String playerName;

    private MVPortal portalSelection = null;
    private MVPortal standingIn = null;
    private boolean debugMode = false;
    private boolean staleLocation;
    private boolean hasMovedOutOfPortal = true;
    private Location loc;
    private Vector rightClick;
    private Vector leftClick;
    private MultiverseWorld rightClickWorld;
    private MultiverseWorld leftClickWorld;
    private Date lastTeleportTime;

    public PortalPlayerSession(MultiversePortals plugin, Player p) {
        this.plugin = plugin;
        this.playerName = p.getName();
        this.setLocation(p.getLocation());
        this.lastTeleportTime = new Date(new Date().getTime() - this.plugin.getCooldownTime());
    }

    public boolean selectPortal(MVPortal portal) {
        this.portalSelection = portal;
        return true;
    }

    public MVPortal getSelectedPortal() {
        return this.portalSelection;
    }

    private Player getPlayerFromName() {
        return this.plugin.getServer().getPlayer(playerName);
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (this.debugMode) {
            this.getPlayerFromName().sendMessage("Portal debug mode " + ChatColor.GREEN + "ENABLED");
            this.getPlayerFromName().sendMessage("Use " + ChatColor.DARK_AQUA + "/mvp debug" + ChatColor.WHITE + " to disable.");
        } else {
            this.getPlayerFromName().sendMessage("Portal debug mode " + ChatColor.RED + "DISABLED");
        }
    }

    public boolean isDebugModeOn() {
        return this.debugMode;
    }

    public void setStaleLocation(boolean active) {
        this.staleLocation = active;
    }

    public boolean isStaleLocation() {
        return this.staleLocation;
    }

    private void setLocation(Location loc) {
        this.loc = loc;
        this.setStandinginLocation();
    }

    private void setStandinginLocation() {
        // If they're not in a portal and this location is a portal
        if (this.standingIn == null && this.plugin.getPortalManager().isPortal(this.loc)) {
            this.standingIn = this.plugin.getPortalManager().getPortal(this.loc);
        // There is no portal here.
        } else if (!this.plugin.getPortalManager().isPortal(this.loc)) {
            this.hasMovedOutOfPortal = true;
            this.standingIn = null;
        } else {
            this.hasMovedOutOfPortal = false;
        }
    }

    public boolean doTeleportPlayer(MoveType eventType) {
        if (eventType == MoveType.PLAYER_MOVE && this.getPlayerFromName().isInsideVehicle()) {
            return false;
        }
        return this.hasMovedOutOfPortal && this.standingIn != null;
    }

    public Location getLocation() {
        return this.loc;
    }

    public void setStaleLocation(Location loc, MoveType moveType) {
        if (this.getPlayerFromName() == null) {
            // This should never happen, but seems to when someone gets kicked.
            return;
        }
        if (this.getPlayerFromName().isInsideVehicle() && moveType != MoveType.VEHICLE_MOVE) {
            return;
        }
        // If the player has not moved, they have a stale location
        if (this.getLocation().getBlockX() == loc.getBlockX() && this.getLocation().getBlockY() == loc.getBlockY() && this.getLocation().getBlockZ() == loc.getBlockZ()) {
            this.setStaleLocation(true);
        } else {
            // Update the Players Session to the new Location.
            this.setLocation(loc);
            // The location is no longer stale.
            this.setStaleLocation(false);
        }
    }

    public boolean setLeftClickSelection(Vector v, MultiverseWorld world) {
        if(!this.plugin.isWandEnabled()) {
            return false;
        }
        this.leftClick = v;
        this.leftClickWorld = world;
        String message = ChatColor.AQUA + "First position set to: (" + v.getBlockX() + ", " + v.getBlockY() + ", " + v.getBlockZ() + ")";
        if (this.leftClickWorld == this.rightClickWorld && this.rightClick != null) {
            MultiverseRegion tempReg = new MultiverseRegion(this.leftClick, this.rightClick, this.leftClickWorld);
            message += ChatColor.GOLD + " (" + tempReg.getArea() + " blocks)";
        }
        this.getPlayerFromName().sendMessage(message);
        return true;
    }

    public boolean setRightClickSelection(Vector v, MultiverseWorld world) {
        if(!this.plugin.isWandEnabled()) {
            return false;
        }
        this.rightClick = v;
        this.rightClickWorld = world;
        String message = ChatColor.AQUA + "Second position set to: (" + v.getBlockX() + ", " + v.getBlockY() + ", " + v.getBlockZ() + ")";
        if (this.leftClickWorld == this.rightClickWorld && this.leftClick != null) {
            MultiverseRegion tempReg = new MultiverseRegion(this.leftClick, this.rightClick, this.leftClickWorld);
            message += ChatColor.GOLD + " (" + tempReg.getArea() + " blocks)";
        }
        this.getPlayerFromName().sendMessage(message);
        return true;

    }

    public MultiverseRegion getSelectedRegion() {
        // Did not find WE
        MultiverseRegion r = null;
        if (this.plugin.getWEAPI() != null) {
            System.out.println("WEAPI");
            try {
                // GAH this looks SO ugly keeping no imports :( see if I can find a workaround
                r = new MultiverseRegion(this.plugin.getWEAPI().getSession(this.getPlayerFromName()).getSelection(this.plugin.getWEAPI().getSession(this.getPlayerFromName()).getSelectionWorld()).getMinimumPoint(),
                        this.plugin.getWEAPI().getSession(this.getPlayerFromName()).getSelection(this.plugin.getWEAPI().getSession(this.getPlayerFromName()).getSelectionWorld()).getMaximumPoint(),
                        this.plugin.getCore().getMVWorldManager().getMVWorld(this.getPlayerFromName().getWorld().getName()));
            } catch (Exception e) {
                this.getPlayerFromName().sendMessage("You haven't finished your selection.");
                return null;
            }
            return r;
        }
        // They're using our crappy selection:
        if (this.leftClick == null) {
            this.getPlayerFromName().sendMessage("You need to LEFT click on a block with your wand!");
            return null;
        }
        if (this.rightClick == null) {
            this.getPlayerFromName().sendMessage("You need to RIGHT click on a block with your wand!");
            return null;
        }
        if (!this.leftClickWorld.equals(this.rightClickWorld)) {
            this.getPlayerFromName().sendMessage("You need to select both coords in the same world!");
            this.getPlayerFromName().sendMessage("Left Click Position was in:" + this.leftClickWorld.getColoredWorldString());
            this.getPlayerFromName().sendMessage("Right Click Position was in:" + this.rightClickWorld.getColoredWorldString());
            return null;
        }
        return new MultiverseRegion(this.leftClick, this.rightClick, this.leftClickWorld);
    }

    /**
     * If a player teleports from A - B, this method will report A even if the player is in B.
     * This is done for hysteresis. For the exact detection please use {@link #getUncachedStandingInPortal()}
     *
     * @return The {@link MVPortal} the player is standing in.
     */
    public MVPortal getStandingInPortal() {
        return this.standingIn;
    }

    /**
     * This will ALWAYS return the portal a player is actually in. For hysteresis see {@link #getStandingInPortal()}.
     *
     * @return The {@link MVPortal} the player is standing in.
     */
    public MVPortal getUncachedStandingInPortal() {
        return this.standingIn = this.plugin.getPortalManager().getPortal(this.loc);
    }

    /**
     * This method should be called every time a player teleports to a portal.
     *
     * @param location
     */
    public void playerDidTeleport(Location location) {
        PortalManager pm = this.plugin.getPortalManager();
        if (pm.getPortal(location) != null) {
            this.hasMovedOutOfPortal = false;
            return;
        }
        this.hasMovedOutOfPortal = true;
    }

    public boolean hasMovedOutOfPortal() {
        return this.hasMovedOutOfPortal;
    }

    public boolean showDebugInfo() {
        if (!this.isDebugModeOn()) {
            return false;
        }

        if (this.standingIn == null) {
            return false;
        }

        showStaticInfo(this.getPlayerFromName(), this.standingIn, "You are currently standing in ");
        this.showPortalPriceInfo(this.standingIn);
        return true;
    }

    public boolean showDebugInfo(MVPortal portal) {
        if (portal.playerCanEnterPortal(this.getPlayerFromName())) {
            showStaticInfo(this.getPlayerFromName(), portal, "Portal Info ");
            showPortalPriceInfo(portal);
        } else {
            this.plugin.log(Level.INFO, "Player " + this.playerName + " walked through" + portal.getName() + " with debug on.");
        }
        return true;
    }

    private void showPortalPriceInfo(MVPortal portal) {
        getPlayerFromName().sendMessage("More details for you: " + ChatColor.GREEN + portal.getDestination());
        if (portal.getPrice() > 0D) {
            GenericBank bank = this.plugin.getCore().getBank();
            getPlayerFromName().sendMessage("Price: " + ChatColor.GREEN + bank.getFormattedAmount(getPlayerFromName(), portal.getPrice(), portal.getCurrency()));
        } else if (portal.getPrice() < 0D) {
            GenericBank bank = this.plugin.getCore().getBank();
            getPlayerFromName().sendMessage("Prize: " + ChatColor.GREEN + bank.getFormattedAmount(getPlayerFromName(), Math.abs(portal.getPrice()), portal.getCurrency()));
        } else {
            getPlayerFromName().sendMessage("Price: " + ChatColor.GREEN + "FREE!");
        }
    }

    public static void showStaticInfo(CommandSender sender, MVPortal portal, String message) {
        sender.sendMessage(message + ChatColor.DARK_AQUA + portal.getName());
        sender.sendMessage("It's coords are: " + ChatColor.GOLD + portal.getLocation().toString());
        if (portal.getDestination() == null) {
            sender.sendMessage("This portal has " + ChatColor.RED + "NO DESTINATION SET.");
        } else {
            sender.sendMessage("It will take you to a location of type: " + ChatColor.AQUA + portal.getDestination().getType());
            sender.sendMessage("The destination's name is: " + ChatColor.GREEN + portal.getDestination().getName());
        }
    }

    public void setTeleportTime(Date date) {
        this.lastTeleportTime = date;
    }

    public boolean allowTeleportViaCooldown(Date date) {
        return (date.after(new Date(this.lastTeleportTime.getTime() + this.plugin.getCooldownTime())));
    }

    public String getFriendlyRemainingTimeMessage() {
        String remaining = "There is a portal " + ChatColor.AQUA + "cooldown" + ChatColor.WHITE + " in effect. Please try again in " + ChatColor.GOLD
        + Integer.toString((int) this.getRemainingCooldown() / 1000) + "s.";
        int time = (int) this.getRemainingCooldown() / 1000;
        // Account for the fact that 0 seconds means 1
        time++;
        if (time <= 0) {
            remaining += "< 1s" + ChatColor.WHITE + ".";
        } else {
            remaining += time + "s" + ChatColor.WHITE + ".";
        }
        return remaining;
    }

    public long getRemainingCooldown() {
        //Calculate the remaining cooldown period
        long remainingcooldown = (this.plugin.getCooldownTime() - ((new Date()).getTime() - this.lastTeleportTime.getTime()));
        if (remainingcooldown < 0) {
            remainingcooldown = 0;
        }
        return remainingcooldown;

    }
}
