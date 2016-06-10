package com.steamcraftmc.BungeeWarped;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.steamcraftmc.BungeeWarped.Storage.MySqlDataStore;
import com.steamcraftmc.BungeeWarped.Storage.PlayerState;
import com.steamcraftmc.BungeeWarped.Listeners.EventListener;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class BungeeWarpedBukkitPlugin extends JavaPlugin implements PluginMessageListener {

    private final Logger logger = Bukkit.getLogger();
    public Map<String, String> portalData = new HashMap<>();
    public MySqlDataStore dataStore;
    public WorldEditPlugin worldEdit;
    public YamlConfiguration configFile;
    public String bungeeServerName;

    public void onEnable() {
        long time = System.currentTimeMillis();
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
        	throw new NullPointerException("[BungeeWarped] WorldEdit not found, disabling feature...");
        } else {
        	worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
        logger.log(Level.INFO, "[BungeeWarped] Plugin channel registered!");
        dataStore = new MySqlDataStore(this);
        loadConfigFiles();
        loadPortalsData();

        new com.steamcraftmc.BungeeWarped.Commands.CmdPortal(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdWarp(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdSetWarp(this);
        new com.steamcraftmc.BungeeWarped.Commands.CmdDelWarp(this);
        
        logger.log(Level.INFO, "[BungeeWarped] Commands registered!");
        getServer().getPluginManager().registerEvents(new EventListener(this, dataStore), this);
        logger.log(Level.INFO, "[BungeeWarped] Events registered!");
        logger.log(Level.INFO, "[BungeeWarped] Version " + getDescription().getVersion() + " has been enabled. (" + (System.currentTimeMillis() - time) + "ms)");
    }

    public void onDisable() {
        logger.log(Level.INFO, "[BungeeWarped] Version " + getDescription().getVersion() + " has been disabled.");
    }

    public void log(Level severity, String text) {
        logger.log(severity, "[BungeeWarped] " + text);
    }

    private class RequestServerName implements Runnable {
    	private final BungeeWarpedBukkitPlugin plugin;
    	private final Player player;
    	public RequestServerName(BungeeWarpedBukkitPlugin plugin, Player player) {
    		this.plugin = plugin;
    		this.player = player;
    	}
		@Override
		public void run() {
			if (!player.isOnline()) return;
			logger.info("[BungeeWarped] Sending message: GetServer");
	        ByteArrayDataOutput out = ByteStreams.newDataOutput();
	        out.writeUTF("GetServer");
	        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
		}
    }
    
    
    public void updateBungeeServerName(Player player) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(this,
				new BungeeWarpedBukkitPlugin.RequestServerName(this, player), 30L);
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
				logger.info("[BungeeWarped] Received bungee server name: " + name);
				this.dataStore.handlePlayerJoin(player);
			}
		}
	}
    
    private void createConfigFile(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadConfigFiles() {
        File cFile = new File(getDataFolder(), "config.yml");
        if (!cFile.exists()) {
            cFile.getParentFile().mkdirs();
            createConfigFile(getResource("config.yml"), cFile);
            logger.log(Level.INFO, "[BungeeWarped] Configuration file config.yml created!");
        }
        configFile = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
        logger.log(Level.INFO, "[BungeeWarped] Configuration file config.yml loaded!");

        dataStore.initialize();
    }

    public void loadPortalsData() {
        try {
            long time = System.currentTimeMillis();
            this.portalData = dataStore.getPortalBlocks();
            logger.log(Level.INFO, "[BungeeWarped] Portal data loaded! (" + (System.currentTimeMillis() - time) + "ms)");
        } catch (Exception e) {
            logger.severe("[BungeeWarped] Load Failed: " + e.toString());
        	this.portalData = new HashMap<>();
        }
    }
    
    public PlayerState getPlayerState(Player player) {
    	return new PlayerState();
    }
}
