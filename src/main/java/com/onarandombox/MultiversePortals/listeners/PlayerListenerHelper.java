package com.onarandombox.MultiversePortals.listeners;

import buscript.multiverse.Buscript;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;

public class PlayerListenerHelper {

    private MultiversePortals plugin;

    public PlayerListenerHelper(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    void stateSuccess(String playerName, String worldName) {
        this.plugin.log(Level.FINE, String.format(
                "MV-Portals is allowing Player '%s' to use the portal '%s'.",
                playerName, worldName));
    }

    void stateFailure(String playerName, String portalName) {
        this.plugin.log(Level.FINE, String.format(
                "MV-Portals is DENYING Player '%s' access to use the portal '%s'.",
                playerName, portalName));
    }

    void performTeleport(Player player, Location to, PortalPlayerSession ps, MVDestination d) {
        SafeTTeleporter playerTeleporter = this.plugin.getCore().getSafeTTeleporter();
        TeleportResult result = playerTeleporter.safelyTeleport(player, player, d);
        if (result == TeleportResult.SUCCESS) {
            ps.playerDidTeleport(to);
            ps.setTeleportTime(new Date());
            this.stateSuccess(player.getDisplayName(), d.getName());
        } else {
            this.stateFailure(player.getDisplayName(), d.getName());
        }
    }

    boolean scriptPortal(Player player, MVDestination d, MVPortal portal, PortalPlayerSession ps) {
        Buscript buscript = plugin.getCore().getScriptAPI();
        File handlerScript = new File(buscript.getScriptFolder(), portal.getHandlerScript());
        if (handlerScript.exists()) {
            TravelAgent agent = new MVTravelAgent(this.plugin.getCore(), d, player);
            buscript.getGlobalScope().put("portal", buscript.getGlobalScope(), portal);
            buscript.getGlobalScope().put("player", buscript.getGlobalScope(), player);
            buscript.getGlobalScope().put("travelAgent", buscript.getGlobalScope(), agent);
            buscript.getGlobalScope().put("allowPortal", buscript.getGlobalScope(), true);
            buscript.getGlobalScope().put("portalSession", buscript.getGlobalScope(), ps);
            buscript.executeScript(handlerScript, player.getName());
            buscript.getGlobalScope().put("portal", buscript.getGlobalScope(), null);
            buscript.getGlobalScope().put("player", buscript.getGlobalScope(), null);
            buscript.getGlobalScope().put("travelAgent", buscript.getGlobalScope(), null);
            buscript.getGlobalScope().put("portalSession", buscript.getGlobalScope(), null);
            Object allowObject = buscript.getGlobalScope().get("allowPortal", buscript.getGlobalScope());
            buscript.getGlobalScope().put("allowPortal", buscript.getGlobalScope(), null);
            if (allowObject instanceof Boolean) {
                if (((Boolean) allowObject)) {
                    MVPortalEvent portalEvent = new MVPortalEvent(d, player, agent, portal);
                    this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                    if (!portalEvent.isCancelled()) {
                        return true;
                    }
                    plugin.log(Level.FINE, "A plugin cancelled the portal after script handling.");
                    return false;
                } else {
                    plugin.log(Level.FINE, "Portal denied by script!");
                    return false;
                }
            } else {
                plugin.log(Level.FINE, "Portal denied by script because allowPortal not a boolean!");
                return false;
            }
        }
        throw new IllegalStateException();
    }
}
