package com.onarandombox.MultiversePortals;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.onarandombox.utils.Destination;
import com.onarandombox.utils.InvalidDestination;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.regions.Region;

public class PortalPlayerSession {
    private MultiversePortals plugin;
    private Player player;

    private MVPortal portalSelection = null;
    private MVPortal standingIn = null;
    private boolean debugMode;
    private boolean staleLocation;
    private boolean hasMovedOutOfPortal = true;
    private Location loc;

    public PortalPlayerSession(MultiversePortals plugin, Player p) {
        this.plugin = plugin;
        this.player = p;
        this.setLocation(this.player.getLocation());
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
        this.setStandinginLocation();
    }

    private void setStandinginLocation() {
        if (this.standingIn == null) {
            this.standingIn = this.plugin.getPortalManager().isPortal(this.player, this.loc);
        } else if (this.plugin.getPortalManager().isPortal(this.player, this.loc) == null) {
            this.hasMovedOutOfPortal = true;
            this.standingIn = null;
        } else {
            this.hasMovedOutOfPortal = false;
        }
    }
    
    public boolean doTeleportPlayer() {
        return this.hasMovedOutOfPortal == true && this.standingIn != null;
    }

    public Location getLocation() {
        return this.loc;
    }

    public void setStaleLocation(Location loc) {
        if (this.getLocation().getBlockX() == loc.getBlockX() && this.getLocation().getBlockY() == loc.getBlockY() && this.getLocation().getBlockZ() == loc.getBlockZ()) {
            this.setStaleLocation(true);
        } else {
            this.setLocation(loc); // Update the Players Session to the new Location.
            this.setStaleLocation(false);
        }

    }

    public Region getSelectedRegion() {
        WorldEditAPI api = this.plugin.getWEAPI();
        if (api == null) {
            this.player.sendMessage("Did not find the WorldEdit API...");
            this.player.sendMessage("It is currently required to use Multiverse-Portals.");
            return null;
        }
        LocalSession s = api.getSession(this.player);
        Region r = null;
        try {
            r = s.getSelection(s.getSelectionWorld());
        } catch (IncompleteRegionException e) {
            this.player.sendMessage("You haven't finished your selection.");
            return null;
        }
        return r;
    }

    public MVPortal getStandingInPortal() {
        return this.standingIn;
    }

    public void playerDidTeleport() {
        this.hasMovedOutOfPortal = false;
    }
}
