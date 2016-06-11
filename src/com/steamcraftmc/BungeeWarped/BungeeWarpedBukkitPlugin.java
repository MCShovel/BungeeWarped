package com.steamcraftmc.BungeeWarped;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.steamcraftmc.BungeeWarped.Storage.MySqlDataStore;
import com.steamcraftmc.BungeeWarped.Tasks.RequestServerName;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Listeners.EventListener;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class BungeeWarpedBukkitPlugin extends JavaPlugin implements PluginMessageListener {

    private final Logger logger = Bukkit.getLogger();
    public final BungeeWarpedConfig config;
    public final MySqlDataStore dataStore;

    public Map<String, String> portalData = new HashMap<>();
    public WorldEditPlugin worldEdit;
    public String bungeeServerName;

    public BungeeWarpedBukkitPlugin() {
    	config = new BungeeWarpedConfig(this);
		dataStore = new MySqlDataStore(this);
    }
    
    public void onEnable() {
        long time = System.currentTimeMillis();
        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
        	worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        log(Level.INFO, "Plugin channel registered!");

        reload();
        
        new com.steamcraftmc.BungeeWarped.Commands.CmdPortal(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdWarp(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdSetWarp(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdDelWarp(this);

        new com.steamcraftmc.BungeeWarped.Commands.CmdHome(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdSetHome(this);
        
        log(Level.INFO, "Commands registered!");
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        log(Level.INFO, "Events registered!");
        log(Level.INFO, "Version " + getDescription().getVersion() + " has been enabled. (" + (System.currentTimeMillis() - time) + "ms)");
    }

    public void onDisable() {
        log(Level.INFO, "Version " + getDescription().getVersion() + " has been disabled.");
    }

    public void log(Level severity, String text) {
        logger.log(severity, "[BungeeWarped] " + text);
    }

	public void reload() {
    	config.load();
        dataStore.initialize();
        loadPortalsData();
        if (!config.enableBungeeCord()) {
        	this.bungeeServerName = "{none}";
        }
	}

	public String getServerName() {
    	if (this.bungeeServerName == null) {
    		log(Level.SEVERE, "The BungeeCord server is not set.");
    		return "[error]";
    	}
		return bungeeServerName;
	}
    
    public void registerPlayerJoined(Player player) {
    	if (this.bungeeServerName == null) {
    		new RequestServerName(this, player).runAfter(30);
    	}
    	else {
    		this.getPlayerController(player).onPlayerJoin();
    	}
    }
    
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord")) {
			return;
		}
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equals("GetServer")) {
			String name = in.readUTF();
			if (name != null) {
				this.bungeeServerName = name;
				log(Level.INFO, "Received bungee server name: " + name);
	    		this.getPlayerController(player).onPlayerJoin();
			}
		}
	}

    public void loadPortalsData() {
        try {
            long time = System.currentTimeMillis();
            this.portalData = dataStore.getPortalBlocks();
            log(Level.INFO, "Portal data loaded! (" + (System.currentTimeMillis() - time) + "ms)");
        } catch (Exception e) {
            log(Level.SEVERE, "Load Failed: " + e.toString());
        	this.portalData = new HashMap<>();
        }
    }
    
    public PlayerController getPlayerController(Player player) {
    	return new PlayerController(this, player);
    }
}
