/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;

/**
 * This is a placeholder of good things to come...
 *
 * @author fernferret
 */
public class MultiverseRegion {

    private Vector min;
    private Vector max;
    private LoadedMultiverseWorld world;

    public MultiverseRegion(Object pos1, Object pos2, LoadedMultiverseWorld w) {
        // Creating soft dependencies on WE
        if (pos1 instanceof com.sk89q.worldedit.math.BlockVector3 && pos2 instanceof com.sk89q.worldedit.math.BlockVector3) {
            com.sk89q.worldedit.math.BlockVector3 weV1 = (com.sk89q.worldedit.math.BlockVector3) pos1;
            com.sk89q.worldedit.math.BlockVector3 weV2 = (com.sk89q.worldedit.math.BlockVector3) pos2;
            Vector tmp1 = new Vector(weV1.getX(), weV1.getY(), weV1.getZ());
            Vector tmp2 = new Vector(weV2.getX(), weV2.getY(), weV2.getZ());
            this.min = Vector.getMinimum(tmp1, tmp2);
            this.max = Vector.getMaximum(tmp1, tmp2);
            this.world = w;
        }
    }

    public MultiverseRegion(Location loc1, Location loc2, LoadedMultiverseWorld w) {
        this(loc1.toVector(), loc2.toVector(), w);
    }

    public MultiverseRegion(Vector pos1, Vector pos2, LoadedMultiverseWorld w) {
        this.min = Vector.getMinimum(pos1, pos2);
        this.max = Vector.getMaximum(pos1, pos2);
        this.world = w;
    }

    public Vector getMinimumPoint() {
        return this.min;
    }

    public Vector getMaximumPoint() {
        return this.max;
    }

    public LoadedMultiverseWorld getWorld() {
        return this.world;
    }

    public int getWidth() {
        return Math.abs((this.max.getBlockX() + 1) - this.min.getBlockX());
    }

    public int getHeight() {
        return Math.abs((this.max.getBlockY() + 1) - this.min.getBlockY());
    }

    public int getDepth() {
        return Math.abs((this.max.getBlockZ() + 1) - this.min.getBlockZ());
    }

    public int getArea() {
        return this.getWidth() * this.getHeight() * this.getDepth();
    }

    public boolean containsVector(Location l) {
        if (!this.world.getBukkitWorld().map(w -> w.equals(l.getWorld())).getOrElse(false)) {
            return false;
        }
        if (!(l.getBlockX() >= min.getBlockX() && l.getBlockX() <= max.getBlockX())) {
            return false;
        }
        if (!(l.getBlockZ() >= min.getBlockZ() && l.getBlockZ() <= max.getBlockZ())) {
            return false;
        }
        if (!(l.getBlockY() >= min.getBlockY() && l.getBlockY() <= max.getBlockY())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.world.getName() + ":" +
                this.min.getX() + "," +
                this.min.getY() + "," +
                this.min.getZ() + ":" +
                this.max.getX() + "," +
                this.max.getY() + "," +
                this.max.getZ();
    }
}
