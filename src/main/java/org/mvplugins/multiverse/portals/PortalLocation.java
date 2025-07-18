/*
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.
 * Multiverse 2 is licensed under the BSD License.
 * For more information please check the README.md file included
 * with this project
 */

package org.mvplugins.multiverse.portals;

import java.util.Arrays;
import java.util.List;

import com.dumptruckman.minecraft.util.Logging;
import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.util.Vector;

import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.portals.utils.MultiverseRegion;

import javax.annotation.Nullable;

public class PortalLocation {

    public static PortalLocation parseLocation(String locationString) {
        String[] split = locationString.split(":");
        if (split.length != 3) {
            Logging.warning("Failed Parsing Location (Format Error, was expecting: `WORLD:X,Y,Z:X,Y,Z`, but got: `" + locationString + "`)");
            return getInvalidPortalLocation();
        }

        String worldName = split[0];
        LoadedMultiverseWorld world = MultiverseCoreApi.get().getWorldManager().getLoadedWorld(worldName).getOrNull();
        if (world == null) {
            Logging.warning("Failed Parsing World (World Error, World did not exist or was not imported into Multiverse-Core!)");
            return getInvalidPortalLocation();
        }

        Vector pos1 = parseVector(split[1]);
        Vector pos2 = parseVector(split[2]);
        if (pos1 == null || pos2 == null) {
            Logging.warning("Failed Parsing Location (Vector Error, was expecting: `WORLD:X,Y,Z:X,Y,Z`, but got: `" + locationString + "`)");
            return getInvalidPortalLocation();
        }
        return new PortalLocation(pos1, pos2, world);
    }

    public static PortalLocation parseLocation(String locationString, LoadedMultiverseWorld world, String portalName) {
        String[] split = locationString.split(":");
        if (split.length != 2) {
            Logging.warning("Failed Parsing Location for: " + portalName + " (Format Error, was expecting: `X,Y,Z:X,Y,Z`, but got: `" + locationString + "`)");
            return getInvalidPortalLocation();
        }
        if (world == null) {
            Logging.warning("Failed Parsing World for: " + portalName + " (World Error, World did not exist or was not imported into Multiverse-Core!)");
            return getInvalidPortalLocation();
        }

        Vector pos1 = parseVector(split[0]);
        Vector pos2 = parseVector(split[1]);

        if (pos1 == null || pos2 == null) {
            Logging.warning("Failed Parsing Location for: " + portalName + " (Vector Error, was expecting: `X,Y,Z:X,Y,Z`, but got: `" + locationString + "`)");
            return getInvalidPortalLocation();
        }
        return new PortalLocation(pos1, pos2, world);
    }

    private static PortalLocation getInvalidPortalLocation() {
        return new PortalLocation();
    }

    private MultiverseRegion region;
    private boolean validLocation = false;

    public PortalLocation() {
    }

    public PortalLocation(Vector pos1, Vector pos2, LoadedMultiverseWorld world) {
        this.validLocation = this.setLocation(pos1, pos2, world);
    }

    /**
     * This constructor takes the Vectors from WorldEdit and converts them to Bukkit vectors.
     *
     * @param minPt
     * @param maxPt
     */
    public PortalLocation(BlockVector3 minPt, BlockVector3 maxPt, LoadedMultiverseWorld world) {
        this(new Vector(minPt.getX(), minPt.getY(), minPt.getZ()), new Vector(maxPt.getX(), maxPt.getY(), maxPt.getZ()), world);
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

    public boolean setLocation(Vector v1, Vector v2, LoadedMultiverseWorld world) {
        if (v1 == null || v2 == null || world == null) {
            this.validLocation = false;
            this.region = null;
        } else {
            this.validLocation = true;
            this.region = new MultiverseRegion(v1, v2, world);
        }
        return this.validLocation;
    }

    public boolean setLocation(String v1, String v2, LoadedMultiverseWorld world) {
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

    public @Nullable LoadedMultiverseWorld getMVWorld() {
        if (this.region == null) {
            return null;
        }
        return this.region.getWorld();
    }

    public @Nullable MultiverseRegion getRegion() {
        return this.region;
    }

    @Override
    public String toString() {
        return this.region == null ? "" : this.region.toString();
    }
}
