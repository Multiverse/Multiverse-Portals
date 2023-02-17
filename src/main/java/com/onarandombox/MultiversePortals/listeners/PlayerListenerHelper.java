package com.onarandombox.MultiversePortals.listeners;

import java.io.File;
import java.util.Date;
import java.util.logging.Level;

import com.dumptruckman.minecraft.util.Logging;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.MultiverseCore.enums.TeleportResult;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import com.onarandombox.MultiversePortals.MVPortal;
import com.onarandombox.MultiversePortals.MultiversePortals;
import com.onarandombox.MultiversePortals.PortalPlayerSession;
import com.onarandombox.MultiversePortals.event.MVPortalEvent;
import com.onarandombox.buscript.Buscript;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PlayerListenerHelper {

    private MultiversePortals plugin;

    public PlayerListenerHelper(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    public void stateSuccess(String playerName, String worldName) {
        Logging.fine(String.format(
                "MV-Portals is allowing Player '%s' to use the portal '%s'.",
                playerName, worldName));
    }

    public void stateFailure(String playerName, String portalName) {
        Logging.fine(String.format(
                "MV-Portals is DENYING Player '%s' access to use the portal '%s'.",
                playerName, portalName));
    }

    public void performTeleport(Player player, Location to, PortalPlayerSession ps, MVDestination d) {
        if (!plugin.getCore().getMVConfig().getEnforceAccess() || (d.getRequiredPermission() == null)
                || (d.getRequiredPermission().length() == 0) || player.hasPermission(d.getRequiredPermission())) {
            SafeTTeleporter playerTeleporter = this.plugin.getCore().getSafeTTeleporter();
            TeleportResult result = playerTeleporter.safelyTeleport(player, player, d);
            if (result == TeleportResult.SUCCESS) {
                ps.playerDidTeleport(to);
                ps.setTeleportTime(new Date());
                this.stateSuccess(player.getDisplayName(), d.getName());
                return;
            }
        }
        this.stateFailure(player.getDisplayName(), d.getName());
    }

    public boolean scriptPortal(Player player, MVDestination d, MVPortal portal, PortalPlayerSession ps) {
        Buscript buscript = plugin.getCore().getScriptAPI();
        File handlerScript = new File(buscript.getScriptFolder(), portal.getHandlerScript());
        if (handlerScript.exists()) {
            MVPTravelAgent agent = new MVPTravelAgent(this.plugin.getCore(), d, player);
            buscript.setScriptVariable("portal", portal);
            buscript.setScriptVariable("player", player);
            buscript.setScriptVariable("travelAgent", agent);
            buscript.setScriptVariable("allowPortal", true);
            buscript.setScriptVariable("portalSession", ps);
            buscript.executeScript(handlerScript, player.getName());
            buscript.setScriptVariable("portal", null);
            buscript.setScriptVariable("player", null);
            buscript.setScriptVariable("travelAgent", null);
            buscript.setScriptVariable("portalSession", null);
            Object allowObject = buscript.getScriptVariable("allowPortal");
            buscript.setScriptVariable("allowPortal", null);
            if (allowObject instanceof Boolean) {
                if (((Boolean) allowObject)) {
                    MVPortalEvent portalEvent = new MVPortalEvent(d, player, agent, portal);
                    this.plugin.getServer().getPluginManager().callEvent(portalEvent);
                    if (!portalEvent.isCancelled()) {
                        return true;
                    }
                    Logging.fine("A plugin cancelled the portal after script handling.");
                    return false;
                } else {
                    Logging.fine("Portal denied by script!");
                    return false;
                }
            } else {
                Logging.fine("Portal denied by script because allowPortal not a boolean!");
                return false;
            }
        }
        throw new IllegalStateException();
    }
}
