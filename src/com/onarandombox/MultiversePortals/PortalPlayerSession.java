package com.onarandombox.MultiversePortals;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class PortalPlayerSession {
    private MultiversePortals plugin;
    private Player player;

    private MVPortal portalSelection = null;
    private boolean debugMode;

    public PortalPlayerSession(MultiversePortals plugin, Player p) {
        this.plugin = plugin;
        this.player = p;
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
}
