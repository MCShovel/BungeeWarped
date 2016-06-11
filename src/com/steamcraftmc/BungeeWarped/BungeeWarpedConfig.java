package com.steamcraftmc.BungeeWarped;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class BungeeWarpedConfig {
	private final BungeeWarpedBukkitPlugin plugin;
	private YamlConfiguration configFile;
	
	public BungeeWarpedConfig(BungeeWarpedBukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void load() {
        File cFile = new File(plugin.getDataFolder(), "config.yml");
        if (!cFile.exists()) {
            cFile.getParentFile().mkdirs();
            createConfigFile(plugin.getResource("config.yml"), cFile);
            plugin.log(Level.INFO, "Configuration file config.yml created!");
        }
        configFile = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        plugin.log(Level.INFO, "Configuration file config.yml loaded!");
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
        	plugin.log(Level.SEVERE, e.toString());
        }
    }

	private YamlConfiguration getConfig(String name) {
		if (this.configFile == null) {
			plugin.log(Level.SEVERE, "The configuration has not been loaded for property '" + name + "'.");
			return new YamlConfiguration();
		}
		return this.configFile;
	}
	
	private boolean getBoolean(String name, boolean def) {
		return getConfig(name).getBoolean(name, def);
	}
	
	private String getString(String name, String def) {
		return getConfig(name).getString(name, def);
	}
	
	private int getInt(String name, int def) {
		return getConfig(name).getInt(name, def);
	}
	
	
	
	
	public boolean enableBungeeCord() {
		return getBoolean("options.enableBungeeCord", true);
	}

	public boolean setSpawnOnTeleport(TeleportReason reason) {
		return true;
	}

	// ***********************************************************************
	// MySql Options:
	// ***********************************************************************
		
	public String mysqlHost() {
		return getString("mysql.host", "localhost");
	}
	public String mysqlDatabase() {
		return getString("mysql.database", "bungeewarped");
	}
	public int mysqlPort() {
		return getInt("mysql.port", 3306);
	}
	public String mysqlUsername() {
		return getString("mysql.username", "root");
	}
	public String mysqlPassword() {
		return getString("mysql.password", "password");
	}
	public String mysqlTablePrefix() {
		return getString("mysql.tablePrefix", "bwarped_");
	}

	// ***********************************************************************
	// Messages:
	// ***********************************************************************
	
	public String NoPortalAccess(String dest) {
		String message = getString("NoPortalPermissionMessage", "&cYou do not have access to use that.");
		message = message.replace("{destination}", dest);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String NoPlayerHomes() {
		String message = getString("NoPlayerHomes", "&cYou do not have a home set.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String NoHomeFoundByName(String name) {
		String message = getString("NoHomeFoundByName", "&cYou do not have a home called '{name}'.");
		message = message.replace("{name}", name);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String NoMoreHomesAllowed(int needed) {
		String message = getString("NoMoreHomesAllowed", "&cYou have too many homes set, use /delhome to remove some.");
		message = message.replace("{needed}", String.valueOf(needed));
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
