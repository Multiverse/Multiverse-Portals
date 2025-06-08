/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals;

import java.util.Date;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.economy.MVEconomist;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.portals.config.PortalsConfig;
import org.mvplugins.multiverse.portals.enums.MoveType;
import org.mvplugins.multiverse.portals.utils.DisplayUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import org.mvplugins.multiverse.portals.utils.MultiverseRegion;
import org.mvplugins.multiverse.portals.utils.PortalManager;

public class PortalPlayerSession {
    private final MultiversePortals plugin;
    private final PortalsConfig portalsConfig;
    private final PortalManager portalManager;
    private final WorldManager worldManager;
    private final DisplayUtils displayUtils;
    private final MVEconomist economist;
    private final Player player;

    private MVPortal portalSelection = null;
    private MVPortal standingIn = null;
    private boolean debugMode = false;
    private boolean staleLocation;
    private boolean hasMovedOutOfPortal = true;
    private Location loc;
    private Vector rightClick;
    private Vector leftClick;
    private LoadedMultiverseWorld rightClickWorld;
    private LoadedMultiverseWorld leftClickWorld;
    private Date lastTeleportTime;

    public PortalPlayerSession(MultiversePortals plugin, Player p) {
        this.plugin = plugin;
        this.portalsConfig = plugin.getServiceLocator().getService(PortalsConfig.class);
        this.portalManager = plugin.getServiceLocator().getService(PortalManager.class);
        this.worldManager = plugin.getServiceLocator().getService(WorldManager.class);
        this.displayUtils = plugin.getServiceLocator().getService(DisplayUtils.class);
        this.economist = plugin.getServiceLocator().getService(MVEconomist.class);
        this.player = p;
        this.setLocation(p.getLocation());
        this.lastTeleportTime = new Date(new Date().getTime() - this.portalsConfig.getPortalCooldown());
    }

    public boolean selectPortal(MVPortal portal) {
        this.portalSelection = portal;
        return true;
    }

    public MVPortal getSelectedPortal() {
        return this.portalSelection;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        if (this.debugMode) {
            this.player.sendMessage("Portal debug mode " + ChatColor.GREEN + "ENABLED");
            this.player.sendMessage("Use " + ChatColor.DARK_AQUA + "/mvp debug" + ChatColor.WHITE + " to disable.");
        } else {
            this.player.sendMessage("Portal debug mode " + ChatColor.RED + "DISABLED");
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
        this.setStandingInLocation();
    }

    private void setStandingInLocation() {
        // If they're not in a portal and this location is a portal
        if (this.standingIn == null && this.portalManager.isPortal(this.loc)) {
            this.standingIn = this.portalManager.getPortal(this.loc);
            // There is no portal here.
        } else if (!this.portalManager.isPortal(this.loc)) {
            this.hasMovedOutOfPortal = true;
            this.standingIn = null;
        } else {
            this.hasMovedOutOfPortal = false;
        }
    }

    public boolean doTeleportPlayer(MoveType eventType) {
        if (eventType == MoveType.PLAYER_MOVE && this.player.isInsideVehicle()) {
            return false;
        }
        return this.hasMovedOutOfPortal && this.standingIn != null;
    }

    public Location getLocation() {
        return this.loc;
    }

    public void setStaleLocation(Location loc, MoveType moveType) {
        if (this.player == null) {
            // This should never happen, but seems to when someone gets kicked.
            return;
        }
        if (this.player.isInsideVehicle() && moveType != MoveType.VEHICLE_MOVE) {
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

    public boolean setLeftClickSelection(Vector v, LoadedMultiverseWorld world) {
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
        this.player.sendMessage(message);
        return true;
    }

    public boolean setRightClickSelection(Vector v, LoadedMultiverseWorld world) {
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
        this.player.sendMessage(message);
        return true;

    }

    public MultiverseRegion getSelectedRegion() {
        WorldEditConnection worldEdit = plugin.getWorldEditConnection();
        if (worldEdit != null && worldEdit.isConnected()) {
            if (worldEdit.isSelectionAvailable(this.player)) {
                Location minPoint = worldEdit.getSelectionMinPoint(this.player);
                Location maxPoint = worldEdit.getSelectionMaxPoint(this.player);
                if (minPoint != null && maxPoint != null && minPoint.getWorld().equals(maxPoint.getWorld())) {
                    return new MultiverseRegion(minPoint, maxPoint,
                            this.worldManager.getLoadedWorld(minPoint.getWorld().getName()).getOrNull());
                } else {
                    this.player.sendMessage("You haven't finished your selection.");
                    return null;
                }
            } else {
                this.player.sendMessage("You must have a WorldEdit selection to do this.");
                return null;
            }
        }
        // They're using our crappy selection:
        if (this.leftClick == null) {
            this.player.sendMessage("You need to LEFT click on a block with your wand!");
            return null;
        }
        if (this.rightClick == null) {
            this.player.sendMessage("You need to RIGHT click on a block with your wand!");
            return null;
        }
        if (!this.leftClickWorld.equals(this.rightClickWorld)) {
            this.player.sendMessage("You need to select both coords in the same world!");
            this.player.sendMessage("Left Click Position was in:" + this.leftClickWorld.getAlias());
            this.player.sendMessage("Right Click Position was in:" + this.rightClickWorld.getAlias());
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
        return this.standingIn = this.portalManager.getPortal(this.loc);
    }

    /**
     * This method should be called every time a player teleports to a portal.
     *
     * @param location
     */
    public void playerDidTeleport(Location location) {
        if (portalManager.getPortal(location) != null) {
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

        displayUtils.showStaticInfo(this.player, this.standingIn, "You are currently standing in ");
        displayUtils.showPortalPriceInfo(this.standingIn, this.player);
        return true;
    }

    public boolean showDebugInfo(MVPortal portal) {
        if (portal.playerCanEnterPortal(this.player)) {
            displayUtils.showStaticInfo(this.player, portal, "Portal Info ");
            displayUtils.showPortalPriceInfo(portal, this.player);
        } else {
            Logging.info("Player " + this.player.getName() + " walked through" + portal.getName() + " with debug on.");
        }
        return true;
    }

    public void setTeleportTime(Date date) {
        this.lastTeleportTime = date;
    }

    /**
     * Checks if the teleport cooldown is still in effect. If it is, a message is sent
     * to the player informing them.
     *
     * @return True if the teleport cooldown is still in effect, false otherwise.
     */
    public boolean checkAndSendCooldownMessage() {
        long cooldownMs = this.getRemainingTeleportCooldown();
        if (cooldownMs > 0) {
            this.player.sendMessage(this.getCooldownMessage(cooldownMs));
            return true;
        }

        return false;
    }

    /**
     * Get the remaining teleport cooldown time in milliseconds.
     * If the value returned is not positive, the cooldown is no longer in effect.
     *
     * @return The remaining teleport cooldown time in milliseconds. Note that a
     *         negative value may be returned if the cooldown is no longer in effect.
     */
    private long getRemainingTeleportCooldown() {
        long cooldownEndMs = this.lastTeleportTime.getTime() + this.portalsConfig.getPortalCooldown();
        long timeMs = (new Date()).getTime();
        return cooldownEndMs - timeMs;
    }

    /**
     * Constructs a message informing a player about the
     * remaining cooldown time.
     *
     * @param cooldownMs The cooldown time in milliseconds.
     * @return           A message to be sent to a player, informing them about the remaining cooldown time.
     */
    private String getCooldownMessage(long cooldownMs) {
        return "There is a portal " + ChatColor.AQUA + "cooldown "
                + ChatColor.WHITE + "in effect. Please try again in "
                + ChatColor.GOLD + this.formatCooldownTime(cooldownMs)
                + ChatColor.WHITE + ".";
    }

    /**
     * Converts a long representing a time in milliseconds to a human-readable String.
     *
     * @param cooldownMs Time in milliseconds.
     * @return Human-readable String with the given time in seconds.
     */
    private String formatCooldownTime(long cooldownMs) {
        if (cooldownMs < 1000) {
            return "1s";
        }

        return (cooldownMs / 1000) + "s";
    }
}
