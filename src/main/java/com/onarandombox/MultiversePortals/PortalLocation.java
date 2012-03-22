/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package com.onarandombox.MultiversePortals;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.util.Vector;

import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.MultiversePortals.utils.MultiverseRegion;

public class PortalLocation {

    private MultiverseRegion region;
    private boolean validLocation = false;

    public PortalLocation(Vector pos1, Vector pos2, MultiverseWorld world) {
        this.validLocation = this.setLocation(pos1, pos2, world);
        ;
    }

    public PortalLocation() {
    }

    /**
     * This constructor takes the Vectors from WorldEdit and converts them to Bukkit vectors.
     *
     * @param minPt
     * @param maxPt
     */
    public PortalLocation(com.sk89q.worldedit.Vector minPt, com.sk89q.worldedit.Vector maxPt, MultiverseWorld world) {
        this(new Vector(minPt.getX(), minPt.getY(), minPt.getZ()), new Vector(maxPt.getX(), maxPt.getY(), maxPt.getZ()), world);
    }

    public static PortalLocation parseLocation(String locationString, MultiverseWorld world, String portalName) {
        String[] split = locationString.split(":");
        if (split.length != 2) {
            MultiversePortals.staticLog(Level.WARNING, "Failed Parsing Location for: " + portalName + " (Format Error, was expecting: `X,Y,Z:X,Y,Z`, but got: `" + locationString + "`)");
            return getInvalidPortalLocation();
        }
        if (world == null) {
            MultiversePortals.staticLog(Level.WARNING, "Failed Parsing World for: " + portalName + " (World Error, World did not exist or was not imported into Multiverse-Core!)");
            return getInvalidPortalLocation();
        }

        Vector pos1 = parseVector(split[0]);
        Vector pos2 = parseVector(split[1]);

        if (pos1 == null || pos2 == null) {
            MultiversePortals.staticLog(Level.WARNING, "Failed Parsing Location for: " + portalName + " (Vector Error, was expecting: `X,Y,Z:X,Y,Z`, but got: `" + locationString + "`)");
            return getInvalidPortalLocation();
        }
        return new PortalLocation(pos1, pos2, world);

    }

    private static PortalLocation getInvalidPortalLocation() {
        return new PortalLocation();
    }

    private static Vector parseVector(String vectorString) {
        String[] stringCoords = vectorString.split(",");
        double[] coords = new double[3];
        for (int i = 0; i < 3; i++) {
            try {
                coords[i] = Double.parseDouble(stringCoords[i]);
            } catch (NumberFormatException e) {
                coords[i] = 0;
                return null;
            }
        }
        return new Vector(coords[0], coords[1], coords[2]);
    }

    public boolean setLocation(Vector v1, Vector v2, MultiverseWorld world) {
        if (v1 == null || v2 == null || world == null) {
            this.validLocation = false;
            this.region = null;
        } else {
            this.validLocation = true;
            this.region = new MultiverseRegion(v1, v2, world);
        }
        return this.validLocation;
    }

    public boolean setLocation(String v1, String v2, MultiverseWorld world) {
        if (v1 == null || v2 == null) {
            this.validLocation = false;
            this.region = null;
            return false;
        } else {
            return this.setLocation(parseVector(v1), parseVector(v2), world);
        }

    }

    public boolean isValidLocation() {
        return this.validLocation;
    }

    public List<Vector> getVectors() {
        return Arrays.asList(this.region.getMinimumPoint(), this.region.getMaximumPoint());
    }

    public Vector getMinimum() {
        return this.region.getMinimumPoint();
    }

    public Vector getMaximum() {
        return this.region.getMaximumPoint();
    }

    @Override
    public String toString() {
        if (this.region == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.region.getMinimumPoint().getX() + ",");
        sb.append(this.region.getMinimumPoint().getY() + ",");
        sb.append(this.region.getMinimumPoint().getZ() + ":");
        sb.append(this.region.getMaximumPoint().getX() + ",");
        sb.append(this.region.getMaximumPoint().getY() + ",");
        sb.append(this.region.getMaximumPoint().getZ());
        return sb.toString();
    }

    public MultiverseWorld getMVWorld() {
        if (this.region == null) {
            return null;
        }
        return this.region.getWorld();
    }

    public MultiverseRegion getRegion() {
        return this.region;
    }
}
