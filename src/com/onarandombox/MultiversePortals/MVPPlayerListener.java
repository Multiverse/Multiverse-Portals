package com.onarandombox.MultiversePortals;

import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;

public class MVPPlayerListener extends PlayerListener {
    private MultiversePortals plugin;
    public MVPPlayerListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action act = event.getAction();
        if(act == Action.LEFT_CLICK_BLOCK || act == Action.RIGHT_CLICK_BLOCK) {
            
        }
        super.onPlayerInteract(event);
    }

}
