package com.onarandombox.MultiversePortals.utils;

import org.bukkit.Location;
import org.bukkit.TravelAgent;
import org.bukkit.entity.Player;

import com.onarandombox.MultiverseCore.MVTeleport;
import com.onarandombox.MultiverseCore.MultiverseCore;

public class MVTravelAgent implements TravelAgent {
    private Location location;
    private MultiverseCore core;
    private Player player;

    public MVTravelAgent(MultiverseCore multiverseCore, Location l, Player p) {
        System.out.print("Init Called");
        this.location = l;
        this.core = multiverseCore;
        this.player = p;
    }
    @Override
    public TravelAgent setSearchRadius(int radius) {
        return this;
    }

    @Override
    public int getSearchRadius() {
        return 0;
    }

    @Override
    public TravelAgent setCreationRadius(int radius) {
        return this;
    }

    @Override
    public int getCreationRadius() {
        return 0;
    }

    @Override
    public boolean getCanCreatePortal() {
        return false;
    }

    @Override
    public void setCanCreatePortal(boolean create) {
    }

    @Override
    public Location findOrCreate(Location location) {
        System.out.print("fondOrCreate Called");
        return this.getSafeLocation();
    }

    @Override
    public Location findPortal(Location location) {
        System.out.print("findPortal Called");
        return this.getSafeLocation();
    }

    @Override
    public boolean createPortal(Location location) {
        return false;
    }
    
    private Location getSafeLocation() {
        MVTeleport teleporter = new MVTeleport(this.core);
        Location newLoc = teleporter.getSafeLocation(this.player, this.location);
        if(newLoc == null) {
            return this.player.getLocation();
        }
        return newLoc;
        
    }

}
