package com.onarandombox.MultiversePortals.utils;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
/**
 * This is a placeholder of good things to come...
 * @author fernferret
 *
 */
public class MultiverseRegion extends CuboidRegion{

    public MultiverseRegion(Vector pos1, Vector pos2) {
        super(pos1, pos2);
        // TODO Auto-generated constructor stub
    }

    public static MultiverseRegion getMVRegion(Region selectedRegion) {
        if(selectedRegion == null) {
            return null;
        }
        return new MultiverseRegion(selectedRegion.getMinimumPoint(), selectedRegion.getMaximumPoint());
    }

}
