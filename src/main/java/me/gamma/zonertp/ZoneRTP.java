package me.gamma.zonertp;

import me.gamma.zonertp.commands.ZoneRTPCommand;
import me.gamma.zonertp.managers.MessageManager;
import me.gamma.zonertp.managers.ZoneManager;
import me.gamma.zonertp.utils.WorldEditUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class ZoneRTP extends JavaPlugin {
    
    private static ZoneRTP instance;
    private ZoneManager zoneManager;
    private MessageManager messageManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Verificar WorldEdit
        if (!WorldEditUtils.isWorldEditAvailable()) {
            getLogger().severe("¡WorldEdit no está instalado o no se pudo inicializar!");
            getLogger().severe("ZoneRTP requiere WorldEdit 6.x para funcionar.");
            getLogger().severe("El plugin se deshabilitará.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Inicializar managers
        this.messageManager = new MessageManager(this);
        this.zoneManager = new ZoneManager(this);
        
        // Registrar comandos
        getCommand("zonertp").setExecutor(new ZoneRTPCommand(this));
        
        // Cargar configuraciones
        saveDefaultConfig();
        reloadConfigs();
        
        getLogger().info("ZoneRTP ha sido habilitado correctamente!");
        getLogger().info("WorldEdit " + getServer().getPluginManager().getPlugin("WorldEdit").getDescription().getVersion() + " detectado.");
    }
    
    @Override
    public void onDisable() {
        if (zoneManager != null) {
            zoneManager.saveZones();
        }
        getLogger().info("ZoneRTP ha sido deshabilitado.");
    }
    
    public void reloadConfigs() {
        reloadConfig();
        messageManager.loadMessages();
        zoneManager.loadZones();
    }
    
    public static ZoneRTP getInstance() {
        return instance;
    }
    
    public ZoneManager getZoneManager() {
        return zoneManager;
    }
    
    public MessageManager getMessageManager() {
        return messageManager;
    }
}