/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals.listeners;

import com.onarandombox.MultiverseCore.event.MVConfigReloadEvent;
import com.onarandombox.MultiverseCore.event.MVVersionEvent;
import com.onarandombox.MultiversePortals.MultiversePortals;
import org.bukkit.event.CustomEventListener;
import org.bukkit.event.Event;

public class MVPConfigReloadListener extends CustomEventListener {
    private MultiversePortals plugin;

    public MVPConfigReloadListener(MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCustomEvent(Event event) {
        if (event.getEventName().equals("MVConfigReload") && event instanceof MVConfigReloadEvent) {
            plugin.reloadConfigs();
            ((MVConfigReloadEvent) event).addConfig("Multiverse-Portals - portals.yml");
            ((MVConfigReloadEvent) event).addConfig("Multiverse-Portals - config.yml");
        } else if (event.getEventName().equals("MVVersionEvent") && event instanceof MVVersionEvent) {
            ((MVVersionEvent) event).appendVersionInfo(this.plugin.getVersionInfo());
        }
    }
}
