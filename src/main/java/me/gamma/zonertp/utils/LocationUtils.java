package me.gamma.zonertp.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class LocationUtils {
    
    private static final Set<Material> UNSAFE_BLOCKS = new HashSet<>();
    
    static {
        UNSAFE_BLOCKS.add(Material.LAVA);
        UNSAFE_BLOCKS.add(Material.STATIONARY_LAVA);
        UNSAFE_BLOCKS.add(Material.WATER);
        UNSAFE_BLOCKS.add(Material.STATIONARY_WATER);
        UNSAFE_BLOCKS.add(Material.CACTUS);
        UNSAFE_BLOCKS.add(Material.FIRE);
        UNSAFE_BLOCKS.add(Material.VINE);
        UNSAFE_BLOCKS.add(Material.WEB);
    }
    
    /**
     * Resultado de una búsqueda de ubicación segura.
     * groundFound indica si se encontró al menos un bloque sólido y seguro
     * en la columna, aunque no se haya podido ubicar un punto seguro sobre él.
     * Esto permite distinguir entre "zona sin suelo" (aire/vacío) y
     * "zona con suelo pero sin espacio libre" (llena de bloques).
     */
    public static class SafeLocationResult {
        private final Location location;
        private final boolean groundFound;
        
        public SafeLocationResult(Location location, boolean groundFound) {
            this.location = location;
            this.groundFound = groundFound;
        }
        
        public Location getLocation() {
            return location;
        }
        
        public boolean isGroundFound() {
            return groundFound;
        }
        
        public boolean isSuccess() {
            return location != null;
        }
    }
    
    public static boolean isSafeLocation(Location location) {
        if (location == null) return false;
        
        World world = location.getWorld();
        if (world == null) return false;
        
        if (location.getY() < 0 || location.getY() > world.getMaxHeight()) {
            return false;
        }
        
        Block feet = location.getBlock();
        Block head = location.clone().add(0, 1, 0).getBlock();
        Block ground = location.clone().add(0, -1, 0).getBlock();
        
        // El bloque donde están los pies y la cabeza deben ser transitables
        if (feet.getType().isSolid() || head.getType().isSolid()) {
            return false;
        }
        
        // El bloque del suelo debe ser sólido
        if (!ground.getType().isSolid()) {
            return false;
        }
        
        // Verificar bloques inseguros
        if (UNSAFE_BLOCKS.contains(ground.getType()) ||
            UNSAFE_BLOCKS.contains(feet.getType()) ||
            UNSAFE_BLOCKS.contains(head.getType())) {
            return false;
        }
        
        return true;
    }
    
    public static SafeLocationResult getHighestSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return new SafeLocationResult(null, false);
        
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int maxY = world.getMaxHeight() - 1;
        
        boolean groundFound = false;
        
        // Buscar el bloque sólido más alto
        for (int y = maxY; y >= 0; y--) {
            Block block = world.getBlockAt(x, y, z);
            
            if (block.getType().isSolid() && !UNSAFE_BLOCKS.contains(block.getType())) {
                groundFound = true;
                
                // Verificar la posición exacta sobre este bloque
                Location candidate = new Location(world, x + 0.5, y + 1, z + 0.5);
                if (isSafeLocation(candidate)) {
                    return new SafeLocationResult(candidate, true);
                }
                
                // Verificar offsets cercanos (para encontrar espacio en zonas pequeñas)
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dz == 0) continue;
                        
                        Location offsetCandidate = new Location(world, x + dx + 0.5, y + 1, z + dz + 0.5);
                        if (isSafeLocation(offsetCandidate)) {
                            return new SafeLocationResult(offsetCandidate, true);
                        }
                    }
                }
            }
        }
        
        return new SafeLocationResult(null, groundFound);
    }
}