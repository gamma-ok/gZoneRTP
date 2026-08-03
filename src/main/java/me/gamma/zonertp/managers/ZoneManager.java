package me.gamma.zonertp.managers;

import me.gamma.zonertp.ZoneRTP;
import me.gamma.zonertp.models.Zone;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ZoneManager {
    
    private final ZoneRTP plugin;
    private final Map<String, Zone> zones = new HashMap<>();
    private File zonesFile;
    private FileConfiguration zonesConfig;
    
    public ZoneManager(ZoneRTP plugin) {
        this.plugin = plugin;
        this.zonesFile = new File(plugin.getDataFolder(), "zones.yml");
        loadZones();
    }
    
    public void loadZones() {
        zones.clear();
        
        if (!zonesFile.exists()) {
            plugin.saveResource("zones.yml", false);
        }
        
        zonesConfig = YamlConfiguration.loadConfiguration(zonesFile);
        ConfigurationSection zonesSection = zonesConfig.getConfigurationSection("zones");
        
        if (zonesSection == null) return;
        
        for (String zoneName : zonesSection.getKeys(false)) {
            ConfigurationSection zoneSection = zonesSection.getConfigurationSection(zoneName);
            if (zoneSection == null) continue;
            
            World world = Bukkit.getWorld(zoneSection.getString("world"));
            if (world == null) {
                plugin.getLogger().warning("Mundo no encontrado para la zona: " + zoneName);
                continue;
            }
            
            Location min = new Location(
                world,
                zoneSection.getDouble("min.x"),
                zoneSection.getDouble("min.y"),
                zoneSection.getDouble("min.z")
            );
            
            Location max = new Location(
                world,
                zoneSection.getDouble("max.x"),
                zoneSection.getDouble("max.y"),
                zoneSection.getDouble("max.z")
            );
            
            Zone zone = new Zone(zoneName, world, min, max);
            zones.put(zoneName.toLowerCase(), zone);
        }
        
        plugin.getLogger().info("Se cargaron " + zones.size() + " zonas.");
    }
    
    public void saveZones() {
        ConfigurationSection zonesSection = zonesConfig.createSection("zones");
        
        for (Zone zone : zones.values()) {
            ConfigurationSection zoneSection = zonesSection.createSection(zone.getName());
            zoneSection.set("world", zone.getWorld().getName());
            
            ConfigurationSection minSection = zoneSection.createSection("min");
            minSection.set("x", zone.getMin().getX());
            minSection.set("y", zone.getMin().getY());
            minSection.set("z", zone.getMin().getZ());
            
            ConfigurationSection maxSection = zoneSection.createSection("max");
            maxSection.set("x", zone.getMax().getX());
            maxSection.set("y", zone.getMax().getY());
            maxSection.set("z", zone.getMax().getZ());
        }
        
        try {
            zonesConfig.save(zonesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("No se pudo guardar el archivo zones.yml: " + e.getMessage());
        }
    }
    
    public boolean addZone(Zone zone) {
        if (zones.containsKey(zone.getName().toLowerCase())) {
            return false;
        }
        zones.put(zone.getName().toLowerCase(), zone);
        saveZones();
        return true;
    }
    
    public boolean removeZone(String name) {
        Zone removed = zones.remove(name.toLowerCase());
        if (removed != null) {
            saveZones();
            return true;
        }
        return false;
    }
    
    public Zone getZone(String name) {
        return zones.get(name.toLowerCase());
    }
    
    public Map<String, Zone> getZones() {
        return new HashMap<>(zones);
    }
    
    public String getZoneList() {
        return zones.values().stream()
            .map(Zone::getName)
            .collect(Collectors.joining(", "));
    }
    
    public int getZoneCount() {
        return zones.size();
    }
}