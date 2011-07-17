package com.onarandombox.MultiversePortals;

import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.onarandombox.MultiverseCore.MVPlayerSession;

public class MVPPlayerListener extends PlayerListener {
    private MultiversePortals plugin;
    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        MVPlayerSession ps = this.plugin.core.getPlayerSession(event.getPlayer());
        
        // If the location is stale, ie: the player isn't actually moving xyz coords, they're looking around
        if(ps != null && ps.isStaleLocation()) {
            return;
        }
        
        // Otherwise, they actually moved. Check to see if their loc is inside a portal!
    }

}
