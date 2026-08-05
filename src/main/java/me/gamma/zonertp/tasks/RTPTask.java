package me.gamma.zonertp.tasks;

import me.gamma.zonertp.ZoneRTP;
import me.gamma.zonertp.models.Zone;
import me.gamma.zonertp.utils.LocationUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RTPTask extends BukkitRunnable {
    
    private final Player player;
    private final Zone zone;
    private final ZoneRTP plugin;
    private final int maxAttempts = 15;
    private boolean groundFoundAnyAttempt = false;
    private Location initialLocation;
    private BukkitTask countdownTask;
    private int remainingSeconds;
    private final int teleportDelay;
    private boolean cancelled = false;
    
    // Almacenar tareas activas por jugador
    private static final Map<UUID, RTPTask> activeTasks = new HashMap<>();
    
    public RTPTask(Player player, Zone zone) {
        this.player = player;
        this.zone = zone;
        this.plugin = ZoneRTP.getInstance();
        this.teleportDelay = plugin.getConfig().getInt("teleport-delay.seconds", 3);
        this.initialLocation = player.getLocation().clone();
        this.remainingSeconds = teleportDelay;
    }
    
    @Override
    public void run() {
        Location safeLocation = null;
        int attempts = 0;
        
        // Intentar encontrar una ubicación segura hasta maxAttempts veces
        while (attempts < maxAttempts) {
            attempts++;
            
            Location randomLoc = zone.getRandomLocation();
            LocationUtils.SafeLocationResult result = LocationUtils.getHighestSafeLocation(randomLoc, zone);
            
            if (result.isGroundFound()) {
                groundFoundAnyAttempt = true;
            }
            
            // Usar isInside() en lugar de isInsideXZ() para verificar también la altura
            if (result.getLocation() != null && zone.isInside(result.getLocation())) {
                safeLocation = result.getLocation();
                break;
            }
        }
        
        if (safeLocation != null) {
            final Location targetLocation = safeLocation;
            
            // Iniciar el cooldown de teletransporte en el hilo principal
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || cancelled) {
                        return;
                    }
                    
                    // Cancelar tarea anterior si existe
                    if (activeTasks.containsKey(player.getUniqueId())) {
                        activeTasks.get(player.getUniqueId()).cancelTeleport();
                    }
                    activeTasks.put(player.getUniqueId(), RTPTask.this);
                    
                    // Mostrar mensaje de inicio de teletransporte solo si no tiene bypass
                    if (!hasBypass()) {
                        String startMessage = plugin.getMessageManager().getMessage(
                            "teleport-start",
                            "zone", zone.getName(),
                            "seconds", teleportDelay
                        );
                        player.sendMessage(startMessage);
                    }
                    
                    // Iniciar countdown
                    startCountdown(targetLocation);
                }
            }.runTask(plugin);
        } else {
            // No se encontró ninguna ubicación segura tras agotar los intentos
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline() || cancelled) {
                        return;
                    }
                    
                    String errorMessage;
                    if (!groundFoundAnyAttempt) {
                        // Nunca se encontró un bloque sólido para pararse: zona en el aire / vacío
                        errorMessage = plugin.getMessageManager().getMessage(
                            "rtp-no-ground",
                            "zone", zone.getName()
                        );
                    } else {
                        // Se encontró suelo, pero nunca hubo espacio libre alrededor
                        errorMessage = plugin.getMessageManager().getMessage(
                            "rtp-no-space",
                            "zone", zone.getName()
                        );
                    }
                    
                    player.sendMessage(errorMessage);
                }
            }.runTask(plugin);
        }
    }
    
    private void startCountdown(Location targetLocation) {
        // Jugadores con bypass (permiso o admin) se teletransportan instantáneamente
        if (hasBypass()) {
            executeTeleport(targetLocation);
            return;
        }
        
        // Iniciar countdown
        countdownTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || cancelled) {
                    cancelTeleport();
                    return;
                }
                
                // Verificar si el jugador se movió
                if (hasPlayerMoved()) {
                    cancelTeleport();
                    String movedMessage = plugin.getMessageManager().getMessage(
                        "teleport-cancelled-moved",
                        "zone", zone.getName()
                    );
                    player.sendMessage(movedMessage);
                    activeTasks.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }
                
                if (remainingSeconds <= 0) {
                    // Ejecutar teletransporte
                    executeTeleport(targetLocation);
                    this.cancel();
                    return;
                }
                
                // Mostrar mensaje de countdown (opcional, cada segundo)
                if (remainingSeconds > 0 && remainingSeconds <= 3) {
                    String countdownMessage = plugin.getMessageManager().getMessage(
                        "teleport-countdown",
                        "zone", zone.getName(),
                        "seconds", remainingSeconds
                    );
                    player.sendMessage(countdownMessage);
                }
                
                remainingSeconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Ejecutar cada segundo
    }
    
    private void executeTeleport(Location targetLocation) {
        if (cancelled || !player.isOnline()) {
            return;
        }
        
        // Verificar si el jugador se movió antes del teletransporte
        if (!hasBypass() && hasPlayerMoved()) {
            cancelTeleport();
            String movedMessage = plugin.getMessageManager().getMessage(
                "teleport-cancelled-moved",
                "zone", zone.getName()
            );
            player.sendMessage(movedMessage);
            activeTasks.remove(player.getUniqueId());
            return;
        }
        
        // Teletransportar
        player.teleport(targetLocation);
        
        // Sonido
        if (plugin.getMessageManager().isSoundEnabled()) {
            try {
                player.playSound(targetLocation, plugin.getMessageManager().getSound(), 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
        
        // Mensaje de éxito
        String successMessage = plugin.getMessageManager().getMessage(
            "rtp-success",
            "zone", zone.getName()
        );
        player.sendMessage(successMessage);
        
        // Limpiar
        activeTasks.remove(player.getUniqueId());
        if (countdownTask != null) {
            countdownTask.cancel();
        }
    }
    
    private boolean hasPlayerMoved() {
        if (initialLocation == null) return false;
        
        Location current = player.getLocation();
        
        double dx = Math.abs(current.getX() - initialLocation.getX());
        double dy = Math.abs(current.getY() - initialLocation.getY());
        double dz = Math.abs(current.getZ() - initialLocation.getZ());
        
        return dx > 0.5 || dy > 0.5 || dz > 0.5;
    }
    
    /**
     * Determina si el jugador debe saltarse el cooldown/countdown de teletransporte,
     * ya sea por permiso explícito o por ser operador del servidor.
     */
    private boolean hasBypass() {
        return player.hasPermission("zonertp.bypasscooldown") || player.isOp();
    }
    
    public void cancelTeleport() {
        cancelled = true;
        if (countdownTask != null) {
            countdownTask.cancel();
        }
        activeTasks.remove(player.getUniqueId());
        this.cancel();
    }
}