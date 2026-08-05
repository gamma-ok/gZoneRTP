package me.gamma.zonertp.commands;

import me.gamma.zonertp.ZoneRTP;
import me.gamma.zonertp.managers.MessageManager;
import me.gamma.zonertp.managers.ZoneManager;
import me.gamma.zonertp.models.Zone;
import me.gamma.zonertp.tasks.RTPTask;
import me.gamma.zonertp.utils.WorldEditUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ZoneRTPCommand implements CommandExecutor {
    
    private final ZoneRTP plugin;
    private final ZoneManager zoneManager;
    private final MessageManager messageManager;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    
    public ZoneRTPCommand(ZoneRTP plugin) {
        this.plugin = plugin;
        this.zoneManager = plugin.getZoneManager();
        this.messageManager = plugin.getMessageManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Verificar permiso básico para cualquier comando
        if (!sender.hasPermission("zonertp.user")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            if (sender.hasPermission("zonertp.admin")) {
                sendHelp(sender);
            } else {
                sender.sendMessage(messageManager.getMessage("no-permission"));
            }
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreate(sender, args);
            case "delete":
                return handleDelete(sender, args);
            case "list":
                return handleList(sender);
            case "reload":
                return handleReload(sender);
            case "random":
                return handleRandom(sender);
            case "help":
                return handleHelp(sender);
            default:
                return handleRTP(sender, subCommand);
        }
    }
    
    private boolean handleHelp(CommandSender sender) {
        if (!sender.hasPermission("zonertp.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        sendHelp(sender);
        return true;
    }
    
    private void sendHelp(CommandSender sender) {
        for (String line : messageManager.getHelpMessages()) {
            sender.sendMessage(line);
        }
    }
    
    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zonertp.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messageManager.getMessage("invalid-usage"));
            return true;
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("player-only"));
            return true;
        }
        
        Player player = (Player) sender;
        String zoneName = args[1];
        
        if (zoneManager.getZone(zoneName) != null) {
            sender.sendMessage(messageManager.getMessage("zone-exists", "zone", zoneName));
            return true;
        }
        
        if (!WorldEditUtils.hasSelection(player)) {
            sender.sendMessage(messageManager.getMessage("no-selection"));
            return true;
        }
        
        Location[] corners = WorldEditUtils.getSelectionCorners(player);
        if (corners == null || corners.length < 2) {
            sender.sendMessage(messageManager.getMessage("no-selection"));
            return true;
        }
        
        Zone zone = new Zone(zoneName, player.getWorld(), corners[0], corners[1]);
        
        if (zoneManager.addZone(zone)) {
            sender.sendMessage(messageManager.getMessage("zone-created", "zone", zoneName));
        } else {
            sender.sendMessage(messageManager.getMessage("zone-exists", "zone", zoneName));
        }
        
        return true;
    }
    
    private boolean handleDelete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("zonertp.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        if (args.length < 2) {
            sender.sendMessage(messageManager.getMessage("invalid-usage"));
            return true;
        }
        
        String zoneName = args[1];
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            sender.sendMessage(messageManager.getMessage("zone-not-found", "zone", zoneName));
            return true;
        }
        
        if (zoneManager.removeZone(zoneName)) {
            sender.sendMessage(messageManager.getMessage("zone-deleted", "zone", zoneName));
        } else {
            sender.sendMessage(messageManager.getMessage("zone-not-found", "zone", zoneName));
        }
        
        return true;
    }
    
    private boolean handleList(CommandSender sender) {
        int count = zoneManager.getZoneCount();
        if (count == 0) {
            sender.sendMessage(messageManager.getMessage("no-zones"));
            return true;
        }
        
        String zoneList = zoneManager.getZoneList();
        sender.sendMessage(messageManager.getMessage("zone-list", "zones", zoneList));
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("zonertp.admin")) {
            sender.sendMessage(messageManager.getMessage("no-permission"));
            return true;
        }
        
        plugin.reloadConfigs();
        sender.sendMessage(messageManager.getMessage("reload-success"));
        return true;
    }
    
    private boolean handleRandom(CommandSender sender) {
        if (zoneManager.getZoneCount() == 0) {
            sender.sendMessage(messageManager.getMessage("no-zones"));
            return true;
        }
        
        Zone[] zones = zoneManager.getZones().values().toArray(new Zone[0]);
        Zone randomZone = zones[(int) (Math.random() * zones.length)];
        
        executeRTP(sender, randomZone.getName());
        return true;
    }
    
    private boolean handleRTP(CommandSender sender, String zoneName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("player-only"));
            return true;
        }
        
        Zone zone = zoneManager.getZone(zoneName);
        if (zone == null) {
            sender.sendMessage(messageManager.getMessage("zone-not-found", "zone", zoneName));
            return true;
        }
        
        executeRTP(sender, zoneName);
        return true;
    }
    
    private void executeRTP(CommandSender sender, String zoneName) {
        Player player = (Player) sender;
        Zone zone = zoneManager.getZone(zoneName);
        
        if (zone == null) {
            sender.sendMessage(messageManager.getMessage("zone-not-found", "zone", zoneName));
            return;
        }
        
        // Verificar cooldown de comando: solo si NO tiene bypass (permiso o admin)
        if (!hasBypass(player)) {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            long cooldownTime = messageManager.getCooldownSeconds() * 1000L;
            
            if (cooldowns.containsKey(playerId)) {
                long timeLeft = (cooldowns.get(playerId) + cooldownTime) - currentTime;
                if (timeLeft > 0) {
                    int seconds = (int) Math.ceil(timeLeft / 1000.0);
                    sender.sendMessage(messageManager.getMessage("cooldown", "seconds", seconds));
                    return;
                }
            }
            
            cooldowns.put(playerId, currentTime);
        }
        
        // Ejecutar RTP en tarea asíncrona
        RTPTask task = new RTPTask(player, zone);
        task.runTask(plugin);
    }
    
    /**
     * Determina si el jugador debe saltarse el cooldown de teletransporte,
     * ya sea por permiso explícito o por ser administrador del servidor.
     */
    private boolean hasBypass(Player player) {
        return player.hasPermission("zonertp.bypasscooldown") || player.isOp();
    }
}