package com.onarandombox.MultiversePortals;

import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockListener;

public class MVPBlockListener extends BlockListener {
    private MultiversePortals plugin;
    public MVPBlockListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }
    @Override
    public void onBlockFromTo(BlockFromToEvent event) {
        // TODO Auto-generated method stub
        plugin.getPortalManager();
        super.onBlockFromTo(event);
    }
}
