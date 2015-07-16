package com.onarandombox.MultiversePortals;

import com.sk89q.worldedit.bukkit.WorldEditAPI;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldEditConnection {

    private final Plugin connectingPlugin;

    private WorldEditPlugin worldEditPlugin;
    WorldEditAPI worldEditAPI;

    WorldEditConnection(Plugin plugin) {
        if (plugin == null) {
            throw new RuntimeException("plugin must not be null.");
        }
        this.connectingPlugin = plugin;
    }

    private WorldEditPlugin retrieveWorldEditPluginFromServer() {
        Plugin plugin = connectingPlugin.getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null) {
            return null;
        } else if (plugin instanceof WorldEditPlugin) {
            return (WorldEditPlugin) plugin;
        } else {
            connectingPlugin.getLogger().warning("WorldEdit v" + plugin.getDescription().getVersion()
                    + " is incompatible with " + connectingPlugin.getDescription().getName() + " v"
                    + connectingPlugin.getDescription().getVersion());
            return null;
        }
    }

    /**
     * Attempts to connect to the WorldEdit plugin.
     *
     * @return true if the WorldEdit plugin is available and able to be interfaced with.
     */
    boolean connect() {
        if (!isConnected()) {
            worldEditPlugin = retrieveWorldEditPluginFromServer();
            if (worldEditPlugin != null) {
                this.worldEditAPI = new WorldEditAPI(worldEditPlugin);
                connectingPlugin.getLogger().info("Found WorldEdit. Using it for selections.");
                return true;
            }
        }
        return false;
    }

    void disconnect() {
        worldEditPlugin = null;
        this.worldEditAPI = null;
    }

    /**
     * Tests the connection to the WorldEdit plugin.
     *
     * @return true if current connected to the WorldEdit plugin.
     */
    public boolean isConnected() {
        return worldEditPlugin != null;
    }

    private Selection getSelection(Player player) {
        if (!isConnected()) {
            throw new RuntimeException("WorldEdit connection is unavailable.");
        }

        return worldEditPlugin.getSelection(player);
    }

    /**
     * @return the maximum point of the player's WorldEdit selection or null if the player has no selection.
     */
    public Location getSelectionMaxPoint(Player player) {
        if (player == null) {
            throw new RuntimeException("player must not be null.");
        }

        if (!isConnected()) {
            throw new RuntimeException("WorldEdit connection is unavailable.");
        }

        Selection selection = getSelection(player);
        return selection != null ? selection.getMaximumPoint() : null;
    }

    /**
     * @return the minimum point of the player's WorldEdit selection or null if the player has no selection.
     */
    public Location getSelectionMinPoint(Player player) {
        if (player == null) {
            throw new RuntimeException("player must not be null.");
        }

        if (!isConnected()) {
            throw new RuntimeException("WorldEdit connection is unavailable.");
        }

        Selection selection = getSelection(player);
        return selection != null ? selection.getMinimumPoint() : null;
    }

    /**
     * @return true if the player has currently has a WorldEdit selection.
     */
    public boolean isSelectionAvailable(Player player) {
        if (player == null) {
            throw new RuntimeException("player must not be null.");
        }

        if (!isConnected()) {
            throw new RuntimeException("WorldEdit connection is unavailable.");
        }

        return getSelection(player) != null;
    }
}
