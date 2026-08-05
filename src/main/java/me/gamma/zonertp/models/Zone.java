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
    
    /**
     * Verifica si una ubicación está dentro de la zona en los 3 ejes (X, Y, Z)
     */
    public boolean isInside(Location location) {
        if (!location.getWorld().equals(world)) return false;
        
        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();
        
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX());
        double minY = Math.min(min.getY(), max.getY());
        double maxY = Math.max(min.getY(), max.getY());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ());
        
        return x >= minX && x <= maxX &&
               y >= minY && y <= maxY &&
               z >= minZ && z <= maxZ;
    }
    
    /**
     * Verifica si una ubicación está dentro de la zona en X y Z (ignora Y)
     */
    public boolean isInsideXZ(Location location) {
        if (!location.getWorld().equals(world)) return false;
        
        double x = location.getX();
        double z = location.getZ();
        
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ());
        
        return x >= minX && x <= maxX &&
               z >= minZ && z <= maxZ;
    }
    
    public Location getRandomLocation() {
        double minX = Math.min(min.getX(), max.getX());
        double maxX = Math.max(min.getX(), max.getX());
        double minZ = Math.min(min.getZ(), max.getZ());
        double maxZ = Math.max(min.getZ(), max.getZ());
        
        double x = minX + (Math.random() * (maxX - minX + 1));
        double z = minZ + (Math.random() * (maxZ - minZ + 1));
        return new Location(world, x, 0, z);
    }
}