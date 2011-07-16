package com.onarandombox.MultiversePortals;

import java.util.Arrays;
import java.util.List;

import org.bukkit.util.Vector;

public class PortalLocation {

    private Vector pos1;
    private Vector pos2;
    private boolean validLocation = false;

    public PortalLocation(Vector pos1, Vector pos2) {
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.validLocation = true;
    }

    public PortalLocation() {
    }

    public static PortalLocation parseLocation(String locationString) {
        String[] split = locationString.split(":");
        if (split.length != 2) {
            System.out.print("MVP - Failed Parsing Portal location: " + locationString);
            return getInvalidPortalLocation();
        }
        Vector pos1 = parseVector(split[0]);
        Vector pos2 = parseVector(split[1]);

        if (pos1 == null || pos2 == null) {
            System.out.print("MVP - Failed Parsing Portal location: " + locationString);
            return new PortalLocation(pos1, pos2);
        }
        return getInvalidPortalLocation();

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

    public boolean setLocation(Vector v1, Vector v2) {
        if (v1 == null || v2 == null) {
            this.validLocation = false;
            this.pos1 = null;
            this.pos2 = null;
        } else {
            this.validLocation = true;
            this.pos1 = v1;
            this.pos2 = v2;    
        }
        return this.validLocation;
    }

    public boolean setLocation(String v1, String v2) {
        if (v1 == null || v2 == null) {
            this.validLocation = false;
            this.pos1 = null;
            this.pos2 = null;
            return false;
        } else {
            return this.setLocation(parseVector(v1), parseVector(v2));
        }
        
    }

    public boolean isValidLocation() {
        return this.validLocation;
    }
    
    public List<Vector> getVectors() {
        return Arrays.asList(this.pos1, this.pos2);
    }

}
