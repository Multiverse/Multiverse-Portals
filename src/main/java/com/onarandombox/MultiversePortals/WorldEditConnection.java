package com.onarandombox.MultiversePortals;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldEditConnection {

    private final Plugin connectingPlugin;

    private WorldEditPlugin worldEditPlugin;
    WorldEdit worldEdit;

    WorldEditConnection(Plugin plugin) {
        if (plugin == null) {
            throw new RuntimeException("plugin must not be null.");
        }
        this.connectingPlugin = plugin;
    }

    private WorldEditPlugin retrieveWorldEditPluginFromServer() {
        Plugin plugin = connectingPlugin.getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin == null) {
            plugin = connectingPlugin.getServer().getPluginManager().getPlugin("FastAsyncWorldEdit");
        }

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
                this.worldEdit = worldEditPlugin.getWorldEdit();
                connectingPlugin.getLogger().info(String.format("Found %s. Using it for selections.", worldEditPlugin.getName()));
                return true;
            }
        }
        return false;
    }

    void disconnect() {
        worldEditPlugin = null;
        this.worldEdit = null;
    }

    /**
     * Tests the connection to the WorldEdit plugin.
     *
     * @return true if current connected to the WorldEdit plugin.
     */
    public boolean isConnected() {
        return worldEditPlugin != null;
    }

    private Region getSelection(Player player) {
        if (!isConnected()) {
            throw new RuntimeException("WorldEdit connection is unavailable.");
        }
        try {
            return worldEdit.getSessionManager().get(new BukkitPlayer(worldEditPlugin, player)).getSelection(new BukkitWorld(player.getWorld()));
        } catch (IncompleteRegionException e) {
            return null;
        }
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

        Region selection = getSelection(player);
        if (selection != null) {
            BlockVector3 point = selection.getMaximumPoint();
            return new Location(player.getWorld(), point.getBlockX(), point.getBlockY(), point.getBlockZ());
        }
        return null;
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

        Region selection = getSelection(player);
        if (selection != null) {
            BlockVector3 point = selection.getMinimumPoint();
            return new Location(player.getWorld(), point.getBlockX(), point.getBlockY(), point.getBlockZ());
        }
        return null;
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
