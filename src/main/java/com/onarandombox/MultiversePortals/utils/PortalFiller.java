package com.onarandombox.MultiversePortals.utils;

import java.util.logging.Level;

import com.onarandombox.MultiverseCore.MultiverseCore;

public class PortalFiller {
    private MultiverseCore plugin;

    public PortalFiller(MultiverseCore plugin) {
        this.plugin = plugin;
    }

    public boolean fillRegion(MultiverseRegion r) {
        if (r.getWidth() != 1 || r.getDepth() != 1) {
            this.plugin.log(Level.FINER, "Cannot fill portal, it is too big... w:[" + r.getWidth() + "] d:[" + r.getDepth() + "]");
            return false;
        }
        return true;
    }
}
