package com.onarandombox.MultiversePortals.listeners;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiverseCore.listeners.MultiverseCoreListener;
import com.onarandombox.MultiversePortals.MultiversePortals;

public class MVPCoreListener extends MultiverseCoreListener {
    private MultiversePortals plugin;

    public MVPCoreListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onVersionRequest(MVVersionEvent event) {
        event.appendVersionInfo(this.plugin.getVersionInfo());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMVConfigReload(MVConfigReloadEvent event) {
        plugin.reloadConfigs();
        event.addConfig("Multiverse-Portals - portals.yml");
        event.addConfig("Multiverse-Portals - config.yml");
    }
}
