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
import org.bukkit.plugin.java.JavaPlugin;

import com.steamcraftmc.BungeeWarped.Storage.PortalDataStore;
import com.steamcraftmc.BungeeWarped.Commands.CommandBPortals;
import com.steamcraftmc.BungeeWarped.Listeners.EventListener;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class BungeeWarpedBukkitPlugin extends JavaPlugin {

    private Logger logger = Bukkit.getLogger();
    public Map<String, String> portalData = new HashMap<>();
    public PortalDataStore dataStore;
    public WorldEditPlugin worldEdit;
    public YamlConfiguration configFile;

    public void onEnable() {
        long time = System.currentTimeMillis();
        if (getServer().getPluginManager().getPlugin("WorldEdit") == null) {
        	throw new NullPointerException("[BungeeWarped] WorldEdit not found, disabling feature...");
        } else {
        	worldEdit = (WorldEditPlugin) getServer().getPluginManager().getPlugin("WorldEdit");
        }
        
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        logger.log(Level.INFO, "[BungeeWarped] Plugin channel registered!");
        dataStore = new PortalDataStore(this);
        loadConfigFiles();
        loadPortalsData();
        getCommand("BPortals").setExecutor(new CommandBPortals(this, dataStore));
        logger.log(Level.INFO, "[BungeeWarped] Commands registered!");
        getServer().getPluginManager().registerEvents(new EventListener(this, dataStore), this);
        logger.log(Level.INFO, "[BungeeWarped] Events registered!");
        logger.log(Level.INFO, "[BungeeWarped] Version " + getDescription().getVersion() + " has been enabled. (" + (System.currentTimeMillis() - time) + "ms)");
    }

    public void onDisable() {
        logger.log(Level.INFO, "[BungeeWarped] Version " + getDescription().getVersion() + " has been disabled.");
    }

    public void log(String text) {
        logger.log(Level.SEVERE, "[BungeeWarped] " + text);
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
}
