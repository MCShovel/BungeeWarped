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
		if (reason == TeleportReason.HOME || reason == TeleportReason.PORTAL || reason == TeleportReason.WARP)
			return true;
		return false;
	}

	public int getTpWarmupTime(TeleportReason reason) {
		if (reason == TeleportReason.PORTAL) {
			return 5;
		}
		return 15;
	}
	
	public int getTpCooldownTime(TeleportReason reason) {
		return 60;
	}

	public int getCombatDelay(TeleportReason reason) {
		return 60;
	}
	
	public int getTpMovementAllowed(TeleportReason reason) {
		if (reason == TeleportReason.PORTAL) {
			return 0;
		}
		return 1;
	}
	
	public boolean getTpEffects(TeleportReason reason) {
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
	
	public String BungeeCordError() {
		String message = getString("BungeeCordError", "&cBungeeCord has not responded, check your configuration.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String PermissionDenied(String name) {
		String message = getString("PermissionDenied", "&4You do not have permission to use that command.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String ConfigReloaded() {
		String message = getString("ConfigReloaded", "&6All configuration files and data have been reloaded.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String WorldEditMissing() {
		String message = getString("WorldEditMissing", "&cUnable to locate WorldEdit plugin.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String WorldEditNoSel() {
		String message = getString("WorldEditNoSel", "&cPlease make a WorldEdit selection.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String EmptySelection() {
		String message = getString("EmptySelection", "&cThe selection is currently empty.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String SelectionTooLarge(int blocks) {
		String message = getString("SelectionTooLarge", "&cThe selected area of {blocks} blocks is too large.");
		message = message.replace("{blocks}", String.valueOf(blocks));
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String PortalCreated(String name, int size) {
		String message = getString("PortalCreated", "&6Portal created to {name} ({size} blocks).");
		message = message.replace("{name}", name);
		message = message.replace("{size}", String.valueOf(size));
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String PortalRemoved(int size) {
		String message = getString("PortalRemoved", "&6Removed {size} portal blocks).");
		message = message.replace("{size}", String.valueOf(size));
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String NoPortalAccess(String dest) {
		String message = getString("NoPortalAccess", "&cYou do not have access to use that.");
		message = message.replace("{destination}", dest);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String NoPlayerHomes() {
		String message = getString("NoPlayerHomes", "&cYou do not have a home set.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String NoHomeFoundByName(String name) {
		String message = getString("NoHomeFoundByName", "&6Home &c{name} &6was not found.");
		message = message.replace("{name}", name);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String HomeSetConfirm(String name) {
		String message = getString("HomeSetConfirm", "&6Home &c{name}&6 created.");
		message = message.replace("{name}", String.valueOf(name));
		return ChatColor.translateAlternateColorCodes('&', message);		
	}

	public String HomeDelConfirm(String name) {
		String message = getString("HomeDelConfirm", "&6Home &c{name}&6 removed.");
		message = message.replace("{name}", String.valueOf(name));
		return ChatColor.translateAlternateColorCodes('&', message);		
	}

	public String HomesList(String list) {
		String message = getString("HomesList", "&6Homes&f: {list}");
		message = message.replace("{list}", String.valueOf(list));
		return ChatColor.translateAlternateColorCodes('&', message);		
	}

	public String NoMoreHomesAllowed(int needed) {
		String message = getString("NoMoreHomesAllowed", "&cYou have too many homes set, use /delhome to remove some.");
		message = message.replace("{needed}", String.valueOf(needed));
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String WarpFullName(String name) {
		String message = getString("WarpFullName", "&6Use the full warp name &c{name}&6.");
		message = message.replace("{name}", name);
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String WarpNotFound(String name) {
		String message = getString("WarpNotFound", "&6Warp &c{name} &6was not found.");
		message = message.replace("{name}", name);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String WarpSetConfirm(String name) {
		String message = getString("WarpSetConfirm", "&6Warp &c{name}&6 created.");
		message = message.replace("{name}", String.valueOf(name));
		return ChatColor.translateAlternateColorCodes('&', message);		
	}

	public String WarpDelConfirm(String name) {
		String message = getString("WarpDelConfirm", "&6Warp &c{name}&6 removed.");
		message = message.replace("{name}", String.valueOf(name));
		return ChatColor.translateAlternateColorCodes('&', message);		
	}

	public String WarpsList(String list) {
		String message = getString("WarpsList", "&6Warps&f: {list}");
		message = message.replace("{list}", String.valueOf(list));
		return ChatColor.translateAlternateColorCodes('&', message);		
	}

	public String TpaTimeoutExpired() {
		String message = getString("TpaTimeoutExpired", "&6The request has timed out.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String TpaRequest(String player) {
		String message = getString("TpaRequest", 
				"&c{player}&6 has requested to teleport to you.\n" +
				"To teleport, type &2/tpaccept&6.\n" +
				"To deny this request, type &c/tpdeny&6.\n" +
				"This request will timeout after &c120 seconds&6."
				);
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String TpaHereRequest(String player) {
		String message = getString("TpaHereRequest", 
				"&c{player}&6 has requested you to teleport to them.\n" +
				"To teleport, type &2/tpaccept&6.\n" +
				"To deny this request, type &c/tpdeny&6.\n" +
				"This request will timeout after &c120 seconds&6."
				);
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String NoPendingRequest() {
		String message = getString("NoPendingRequest", "&cYou do not have a pending request.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TeleportAccepted() {
		String message = getString("TeleportAccepted", "&6Teleport request accepted.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TeleportDenied() {
		String message = getString("TeleportDenied", "&6Teleport request denied.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TpRequestIgnore() {
		String message = getString("TpRequestIgnore", "&6Now ignoring teleport requests.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TpRequestUnignore() {
		String message = getString("TpRequestUnignore", "&6Now accepting teleport requests.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequestAccepted(String player) {
		String message = getString("RequestAccepted", "&c{player} &6has accepted your request.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequestCancelled(String player) {
		String message = getString("RequestCancelled", "&6Teleport request cancelled.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequestDenied(String player) {
		String message = getString("RequestDenied", "&c{player} &6has denied your request.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequestTargetLogout(String player) {
		String message = getString("RequestTargetLogout", "&c{player} &6has logged out.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequestTimeout(String player) {
		String message = getString("RequestTimeout", "&6Teleport request timed out.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequestInterrupt(String player) {
		String message = getString("RequestInterrupt", "&cTeleport request was interrupted by someone else.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequesteeOnCooldown(String player) {
		String message = getString("RequesteeOnCooldown", "&cThat user is unable to teleport right now.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String RequesteeCanNotTp(String player) {
		String message = getString("RequesteeCanNotTp", "&cThat user is not allowed to teleport.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String PlayerNotFound(String player) {
		String message = getString("PlayerNotFound", "&cPlayer not found.");
		message = message.replace("{player}", player);
		return ChatColor.translateAlternateColorCodes('&', message);
	}
	
	public String TeleportingAfterDelay(int delay) {
		String message = getString("TeleportingAfterDelay", "&8Teleporting in {delay} seconds...");
		message = message.replace("{delay}", String.valueOf(delay));
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TeleportCancelled() {
		String message = getString("TeleportCancelled", "&6Teleport cancelled.");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String Teleporting() {
		String message = getString("Teleporting", "&6Teleporting...");
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TeleportCancelledCombat(int delay) {
		String message = getString("TeleportCancelledCombat", "&cYou have been in combat, wait {delay} seconds.");
		message = message.replace("{delay}", String.valueOf(delay));
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String TeleportCancelledCooldown(int delay) {
		String message = getString("TeleportCancelledCooldown", "&cPlease wait {delay} seconds before teleporting again.");
		message = message.replace("{delay}", String.valueOf(delay));
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}
