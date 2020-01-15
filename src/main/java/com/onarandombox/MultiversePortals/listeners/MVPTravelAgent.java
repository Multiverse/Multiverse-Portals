package com.onarandombox.MultiversePortals.listeners;

import java.util.logging.Level;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVDestination;
import com.onarandombox.MultiverseCore.utils.BukkitTravelAgent;
import com.onarandombox.MultiverseCore.utils.MVTravelAgent;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPortalEvent;

class MVPTravelAgent extends MVTravelAgent {

    MVPTravelAgent(MultiverseCore multiverseCore, MVDestination d, Player p) {
        super(multiverseCore, d, p);
    }

    void setPortalEventTravelAgent(PlayerPortalEvent event) {
        boolean error = false;
        try {
            Class.forName("org.bukkit.TravelAgent");
            new BukkitTravelAgent(this).setPortalEventTravelAgent(event);
        } catch (ClassNotFoundException ignore) {
            error = true;
        }
        try {
            event.setCanCreatePortal(false);
            event.setCreationRadius(0);
            event.setSearchRadius(0);
        } catch (NoSuchMethodError ignore) {
            error = true;
        }
        if (error) {
            core.log(Level.FINE, "Neither TravelAgent nor portal creation/search methods available for PlayerPortalEvent for " + player.getName());
        }
    }
}
