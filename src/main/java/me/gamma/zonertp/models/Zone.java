package me.gamma.zonertp.models;

import org.bukkit.Location;
import org.bukkit.World;

public class Zone {
    
    private final String name;
    private final World world;
    private final Location min;
    private final Location max;
    
    public Zone(String name, World world, Location min, Location max) {
        this.name = name;
        this.world = world;
        this.min = min;
        this.max = max;
    }
    
    public String getName() {
        return name;
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location getMin() {
        return min;
    }
    
    public Location getMax() {
        return max;
    }
    
    public boolean isInside(Location location) {
        if (!location.getWorld().equals(world)) return false;
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        return x >= min.getX() && x <= max.getX() &&
               y >= min.getY() && y <= max.getY() &&
               z >= min.getZ() && z <= max.getZ();
    }
    
    public Location getRandomLocation() {
        double x = min.getX() + (Math.random() * (max.getX() - min.getX() + 1));
        double z = min.getZ() + (Math.random() * (max.getZ() - min.getZ() + 1));
        return new Location(world, x, 0, z);
    }
}