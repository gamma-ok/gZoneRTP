package me.gamma.zonertp.utils;

import me.gamma.zonertp.ZoneRTP;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

public class WorldEditUtils {
    
    private static Plugin worldEditPlugin;
    private static Method getSelectionMethod;
    private static Method getWidthMethod;
    private static Method getLengthMethod;
    private static Method getHeightMethod;
    private static Method getNativeMinimumPointMethod;
    private static Method getNativeMaximumPointMethod;
    private static Method getXMethod;
    private static Method getYMethod;
    private static Method getZMethod;
    private static boolean initialized = false;
    
    static {
        try {
            Plugin plugin = ZoneRTP.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
            if (plugin != null) {
                worldEditPlugin = plugin;
                
                // Usar reflection para acceder a WorldEdit sin dependencias directas
                Class<?> worldEditClass = plugin.getClass();
                Class<?> selectionClass = Class.forName("com.sk89q.worldedit.bukkit.selections.Selection");
                Class<?> cuboidSelectionClass = Class.forName("com.sk89q.worldedit.bukkit.selections.CuboidSelection");
                Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
                
                getSelectionMethod = worldEditClass.getMethod("getSelection", Player.class);
                getWidthMethod = selectionClass.getMethod("getWidth");
                getLengthMethod = selectionClass.getMethod("getLength");
                getHeightMethod = selectionClass.getMethod("getHeight");
                getNativeMinimumPointMethod = cuboidSelectionClass.getMethod("getNativeMinimumPoint");
                getNativeMaximumPointMethod = cuboidSelectionClass.getMethod("getNativeMaximumPoint");
                getXMethod = vectorClass.getMethod("getX");
                getYMethod = vectorClass.getMethod("getY");
                getZMethod = vectorClass.getMethod("getZ");
                
                initialized = true;
                ZoneRTP.getInstance().getLogger().info("WorldEdit " + plugin.getDescription().getVersion() + " detectado correctamente.");
            } else {
                ZoneRTP.getInstance().getLogger().warning("WorldEdit no encontrado.");
            }
        } catch (Exception e) {
            ZoneRTP.getInstance().getLogger().warning("Error al inicializar WorldEditUtils: " + e.getMessage());
            // Intentar con método alternativo
            try {
                initializeAlternative();
            } catch (Exception ex) {
                ZoneRTP.getInstance().getLogger().warning("No se pudo inicializar WorldEditUtils.");
            }
        }
    }
    
    private static void initializeAlternative() throws Exception {
        Plugin plugin = ZoneRTP.getInstance().getServer().getPluginManager().getPlugin("WorldEdit");
        if (plugin != null) {
            worldEditPlugin = plugin;
            Class<?> worldEditClass = plugin.getClass();
            getSelectionMethod = worldEditClass.getMethod("getSelection", Player.class);
            initialized = true;
        }
    }
    
    public static boolean hasSelection(Player player) {
        if (!initialized || worldEditPlugin == null || player == null) return false;
        
        try {
            Object selection = getSelectionMethod.invoke(worldEditPlugin, player);
            if (selection == null) return false;
            
            // Intentar obtener dimensiones
            try {
                int width = (int) getWidthMethod.invoke(selection);
                int length = (int) getLengthMethod.invoke(selection);
                int height = (int) getHeightMethod.invoke(selection);
                return width > 0 && length > 0 && height > 0;
            } catch (Exception e) {
                // Si falla, asumir que hay selección
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }
    
    public static Location[] getSelectionCorners(Player player) {
        if (!initialized || worldEditPlugin == null || player == null) return null;
        
        try {
            Object selection = getSelectionMethod.invoke(worldEditPlugin, player);
            if (selection == null) return null;
            
            World world = player.getWorld();
            
            // Intentar obtener puntos nativos
            try {
                Object minPoint = getNativeMinimumPointMethod.invoke(selection);
                Object maxPoint = getNativeMaximumPointMethod.invoke(selection);
                
                double minX = (double) getXMethod.invoke(minPoint);
                double minY = (double) getYMethod.invoke(minPoint);
                double minZ = (double) getZMethod.invoke(minPoint);
                
                double maxX = (double) getXMethod.invoke(maxPoint);
                double maxY = (double) getYMethod.invoke(maxPoint);
                double maxZ = (double) getZMethod.invoke(maxPoint);
                
                // Asegurar que min < max
                Location min = new Location(world, 
                    Math.min(minX, maxX),
                    Math.min(minY, maxY),
                    Math.min(minZ, maxZ)
                );
                
                Location max = new Location(world,
                    Math.max(minX, maxX),
                    Math.max(minY, maxY),
                    Math.max(minZ, maxZ)
                );
                
                return new Location[]{min, max};
            } catch (Exception e) {
                // Fallback: intentar con método alternativo
                try {
                    Method getRegionMethod = selection.getClass().getMethod("getRegion");
                    Object region = getRegionMethod.invoke(selection);
                    
                    Method getMinMethod = region.getClass().getMethod("getMinimumPoint");
                    Method getMaxMethod = region.getClass().getMethod("getMaximumPoint");
                    
                    Object minPoint = getMinMethod.invoke(region);
                    Object maxPoint = getMaxMethod.invoke(region);
                    
                    Class<?> vectorClass = Class.forName("com.sk89q.worldedit.Vector");
                    getXMethod = vectorClass.getMethod("getX");
                    getYMethod = vectorClass.getMethod("getY");
                    getZMethod = vectorClass.getMethod("getZ");
                    
                    double minX = (double) getXMethod.invoke(minPoint);
                    double minY = (double) getYMethod.invoke(minPoint);
                    double minZ = (double) getZMethod.invoke(minPoint);
                    
                    double maxX = (double) getXMethod.invoke(maxPoint);
                    double maxY = (double) getYMethod.invoke(maxPoint);
                    double maxZ = (double) getZMethod.invoke(maxPoint);
                    
                    Location min = new Location(world, minX, minY, minZ);
                    Location max = new Location(world, maxX, maxY, maxZ);
                    
                    return new Location[]{min, max};
                } catch (Exception ex) {
                    ZoneRTP.getInstance().getLogger().warning("Error al obtener esquinas: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            ZoneRTP.getInstance().getLogger().warning("Error general al obtener selección: " + e.getMessage());
        }
        
        return null;
    }
    
    public static boolean isWorldEditAvailable() {
        return initialized && worldEditPlugin != null;
    }
}