package com.onarandombox.MultiversePortals.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.MVWorld;

/**
 * This is a placeholder of good things to come...
 * 
 * @author fernferret
 */
public class MultiverseRegion {

    private Vector min;
    private Vector max;
    private MVWorld world;

    public MultiverseRegion(Object pos1, Object pos2, MVWorld w) {
        // Creating soft dependencies on WE
        if (pos1 instanceof com.sk89q.worldedit.Vector && pos2 instanceof com.sk89q.worldedit.Vector) {
            com.sk89q.worldedit.Vector weV1 = (com.sk89q.worldedit.Vector) pos1;
            com.sk89q.worldedit.Vector weV2 = (com.sk89q.worldedit.Vector) pos2;
            Vector tmp1 = new Vector(weV1.getX(), weV1.getY(), weV1.getZ());
            Vector tmp2 = new Vector(weV2.getX(), weV2.getY(), weV2.getZ());
            this.min = Vector.getMinimum(tmp1, tmp2);
            this.max = Vector.getMaximum(tmp1, tmp2);
            this.world = w;
        }
    }

    public MultiverseRegion(Vector pos1, Vector pos2, MVWorld w) {
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
    
    public MVWorld getWorld() {
        return this.world;
    }

    public int getArea() {
        int width = Math.abs((this.max.getBlockX() + 1) - this.min.getBlockX());
        int height = Math.abs((this.max.getBlockY() + 1) - this.min.getBlockY());
        int depth = Math.abs((this.max.getBlockZ() + 1) - this.min.getBlockZ());
        return width * height * depth;
    }
    
    public boolean containsVector(Location l) {
        if(!this.world.getCBWorld().equals(l.getWorld())) {
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

}
