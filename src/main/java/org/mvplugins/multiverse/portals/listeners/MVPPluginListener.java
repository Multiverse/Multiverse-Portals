/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.listeners;

import com.dumptruckman.minecraft.util.Logging;
import org.mvplugins.multiverse.core.MultiverseCore;
import org.mvplugins.multiverse.external.jakarta.inject.Inject;
import org.mvplugins.multiverse.external.jetbrains.annotations.NotNull;
import org.mvplugins.multiverse.external.jvnet.hk2.annotations.Service;
import org.mvplugins.multiverse.portals.MultiversePortals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

@Service
public class MVPPluginListener implements PortalsListener {

    private final MultiversePortals plugin;

    @Inject
    MVPPluginListener(@NotNull MultiversePortals plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void pluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(((MultiverseCore) this.plugin.getServer().getPluginManager().getPlugin("Multiverse-Core")));
            this.plugin.getServer().getPluginManager().enablePlugin(this.plugin);
        } else if (event.getPlugin().getDescription().getName().equals("MultiVerse")) {
            if (event.getPlugin().isEnabled()) {
                this.plugin.getServer().getPluginManager().disablePlugin(event.getPlugin());
                Logging.warning("I just disabled the old version of Multiverse for you. You should remove the JAR now, your configs have been migrated.");
            }
        }
    }

    @EventHandler
    public void pluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getDescription().getName().equals("Multiverse-Core")) {
            this.plugin.setCore(null);
            this.plugin.getServer().getPluginManager().disablePlugin(this.plugin);
        }
    }
}
