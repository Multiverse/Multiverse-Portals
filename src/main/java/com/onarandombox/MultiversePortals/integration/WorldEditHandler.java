package com.onarandombox.MultiversePortals.integration;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.onarandombox.MultiversePortals.MultiversePortals;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;

/**
 * @author dmulloy2
 */

public class WorldEditHandler {
    private WorldEditPlugin worldEdit;

    public WorldEditHandler(MultiversePortals plugin) {
        try {
            worldEdit = (WorldEditPlugin) plugin.getServer().getPluginManager().getPlugin("WorldEdit");
            plugin.getLogger().info("Found WorldEdit, using it for selections.");
        } catch (Throwable ex) {
        }
    }

    public boolean isEnabled() {
        return worldEdit != null;
    }

    public Location getMaxPoint(Player player) {
        if (!isEnabled()) {
            return null;
        }

        Selection selection = getSelection(player);
        return selection != null ? selection.getMaximumPoint() : null;
    }

    public Location getMinPoint(Player player) {
        if (!isEnabled()) {
            return null;
        }

        Selection selection = getSelection(player);
        return selection != null ? selection.getMinimumPoint() : null;
    }

    public Selection getSelection(Player player) {
        if (!isEnabled()) {
            return null;
        }

        return worldEdit.getSelection(player);
    }

    public boolean hasSelection(Player player) {
        if (!isEnabled()) {
            return false;
        }

        return getSelection(player) != null;
    }
}
