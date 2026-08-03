package me.gamma.zonertp.managers;

import me.gamma.zonertp.ZoneRTP;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class MessageManager {
    
    private final ZoneRTP plugin;
    private String prefix;
    private List<String> helpMessages;
    private String noPermission;
    private String playerOnly;
    private String invalidUsage;
    private String zoneNotFound;
    private String zoneCreated;
    private String zoneDeleted;
    private String zoneList;
    private String zoneExists;
    private String noSelection;
    private String noZones;
    private String rtpSuccess;
    private String rtpFailed;
    private String rtpNoSpace;
    private String rtpFallback;
    private String cooldownMessage;
    private String reloadSuccess;
    private String teleportStart;
    private String teleportCountdown;
    private String teleportCancelledMoved;
    private int cooldownSeconds;
    private boolean soundEnabled;
    private String soundName;
    private int teleportDelay;
    
    public MessageManager(ZoneRTP plugin) {
        this.plugin = plugin;
        loadMessages();
    }
    
    public void loadMessages() {
        FileConfiguration config = plugin.getConfig();
        
        this.prefix = colorize(config.getString("messages.prefix", "&6[ZoneRTP] &r"));
        
        // Cargar mensajes de ayuda
        this.helpMessages = new ArrayList<>();
        List<String> helpConfig = config.getStringList("messages.help");
        if (helpConfig != null && !helpConfig.isEmpty()) {
            for (String line : helpConfig) {
                helpMessages.add(colorize(line));
            }
        } else {
            helpMessages.add(colorize("&6&lZoneRTP Help"));
            helpMessages.add(colorize("&e/zrtp <zona> &7- &fTeletransporta a una zona específica"));
            helpMessages.add(colorize("&e/zrtp random &7- &fTeletransporta a una zona aleatoria"));
            helpMessages.add(colorize("&e/zrtp create <nombre> &7- &fCrea una zona con selección WorldEdit"));
            helpMessages.add(colorize("&e/zrtp delete <nombre> &7- &fElimina una zona"));
            helpMessages.add(colorize("&e/zrtp list &7- &fLista todas las zonas"));
            helpMessages.add(colorize("&e/zrtp reload &7- &fRecarga configuración y zonas"));
        }
        
        // Cargar mensajes individuales
        this.noPermission = colorize(config.getString("messages.no-permission", "&cNo tienes permiso para usar este comando."));
        this.playerOnly = colorize(config.getString("messages.player-only", "&cEste comando solo puede ser usado por jugadores."));
        this.invalidUsage = colorize(config.getString("messages.invalid-usage", "&cUso incorrecto. Usa &f/zrtp help &cpara ayuda."));
        this.zoneNotFound = colorize(config.getString("messages.zone-not-found", "&cLa zona &f{zone} &cno existe."));
        this.zoneCreated = colorize(config.getString("messages.zone-created", "&a¡Zona &f{zone} &acreada exitosamente!"));
        this.zoneDeleted = colorize(config.getString("messages.zone-deleted", "&aZona &f{zone} &aeliminada."));
        this.zoneList = colorize(config.getString("messages.zone-list", "&aZonas disponibles: &f{zones}"));
        this.zoneExists = colorize(config.getString("messages.zone-exists", "&cYa existe una zona con el nombre &f{zone}&c."));
        this.noSelection = colorize(config.getString("messages.no-selection", "&cNo tienes una selección activa de WorldEdit."));
        this.noZones = colorize(config.getString("messages.no-zones", "&cNo hay zonas registradas."));
        this.rtpSuccess = colorize(config.getString("messages.rtp-success", "&a¡Teletransportado a la zona &f{zone}&a!"));
        this.rtpFailed = colorize(config.getString("messages.rtp-failed", "&cNo se pudo encontrar una ubicación segura en la zona &f{zone}&c."));
        this.rtpNoSpace = colorize(config.getString("messages.rtp-no-space", "&cNo hay suficiente espacio en &f{zone} &cpara teletransportarte."));
        this.rtpFallback = colorize(config.getString("messages.rtp-fallback", "&eTeletransportado a una ubicación de respaldo en &f{zone}&e."));
        this.cooldownMessage = colorize(config.getString("messages.cooldown", "&cDebes esperar &f{seconds}s &cantes de usar RTP nuevamente."));
        this.reloadSuccess = colorize(config.getString("messages.reload-success", "&aConfiguración y zonas recargadas."));
        
        // Nuevos mensajes para el teleport delay
        this.teleportStart = colorize(config.getString("messages.teleport-start", 
            "&eTeletransportando a &f{zone} &een &f{seconds}s &e(no te muevas)"));
        this.teleportCountdown = colorize(config.getString("messages.teleport-countdown", 
            "&eTeletransportando en &f{seconds}s&e..."));
        this.teleportCancelledMoved = colorize(config.getString("messages.teleport-cancelled-moved", 
            "&cTeletransporte cancelado porque te moviste."));
        
        // Cargar configuración
        this.cooldownSeconds = config.getInt("cooldown.seconds", 5);
        this.teleportDelay = config.getInt("teleport-delay.seconds", 3);
        this.soundEnabled = config.getBoolean("sound.enabled", true);
        this.soundName = config.getString("sound.name", "ENDERMAN_TELEPORT");
    }
    
    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    public List<String> getHelpMessages() {
        return new ArrayList<>(helpMessages);
    }
    
    public String getMessage(String key, Object... replacements) {
        String message = "";
        switch (key.toLowerCase()) {
            case "no-permission": message = noPermission; break;
            case "player-only": message = playerOnly; break;
            case "invalid-usage": message = invalidUsage; break;
            case "zone-not-found": message = zoneNotFound; break;
            case "zone-created": message = zoneCreated; break;
            case "zone-deleted": message = zoneDeleted; break;
            case "zone-list": message = zoneList; break;
            case "zone-exists": message = zoneExists; break;
            case "no-selection": message = noSelection; break;
            case "no-zones": message = noZones; break;
            case "rtp-success": message = rtpSuccess; break;
            case "rtp-failed": message = rtpFailed; break;
            case "rtp-no-space": message = rtpNoSpace; break;
            case "rtp-fallback": message = rtpFallback; break;
            case "cooldown": message = cooldownMessage; break;
            case "reload-success": message = reloadSuccess; break;
            case "teleport-start": message = teleportStart; break;
            case "teleport-countdown": message = teleportCountdown; break;
            case "teleport-cancelled-moved": message = teleportCancelledMoved; break;
            default: return "Mensaje no encontrado: " + key;
        }
        
        // Reemplazar placeholders
        if (replacements != null && replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    String key2 = "{" + replacements[i] + "}";
                    String value = replacements[i + 1].toString();
                    message = message.replace(key2, value);
                }
            }
        }
        
        return prefix + message;
    }
    
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
    
    public int getTeleportDelay() {
        return teleportDelay;
    }
    
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    public Sound getSound() {
        try {
            return Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            return Sound.ENDERMAN_TELEPORT;
        }
    }
}