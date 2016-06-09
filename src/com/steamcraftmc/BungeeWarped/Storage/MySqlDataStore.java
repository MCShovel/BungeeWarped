package com.steamcraftmc.BungeeWarped.Storage;

import java.io.ByteArrayOutputStream;		
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;

public class MySqlDataStore {
    private BungeeWarpedBukkitPlugin plugin;
    private MySQLCore database;

	public MySqlDataStore(BungeeWarpedBukkitPlugin plugin) {
        this.plugin = plugin;
	}
	
	public void initialize() {
		if (database != null) {
			database.close();
		}
		
        this.plugin.log(Level.INFO, "Initializing Database Connection...");
        this.database = new MySQLCore(plugin, 
    		plugin.configFile.getString("mysql.host", "localhost"),
    		plugin.configFile.getString("mysql.database", "bportals"), 
    		plugin.configFile.getInt("mysql.port", 3306),
    		plugin.configFile.getString("mysql.username", "root"),
    		plugin.configFile.getString("mysql.password", "password")
		);

        if (!this.database.existsTable("bportal_teleports")) {
        	this.plugin.log(Level.INFO, "Creating bportal_teleports Schema...");
        	this.database.execute(
    		"CREATE TABLE `bportal_teleports` ( \n" +
            "  `bp_playerUUID` VARCHAR(40) NOT NULL, \n" +
            "  `bp_serverName` VARCHAR(45) NOT NULL, \n" +
            "  `bp_worldName` VARCHAR(45) NOT NULL, \n" +
            "  `bp_posX` DOUBLE NOT NULL, \n" +
            "  `bp_posY` DOUBLE NOT NULL, \n" +
            "  `bp_posZ` DOUBLE NOT NULL, \n" +
            "  `bp_pitch` DOUBLE NOT NULL, \n" +
            "  `bp_yaw` DOUBLE NOT NULL, \n" +
            "  PRIMARY KEY (`bp_playerUUID`, `bp_serverName`)); \n" +
			"");
    	}
        if (!this.database.existsTable("bportal_destinations")) {
        	this.plugin.log(Level.WARNING, "Creating bportal_destinations Schema...");
        	this.database.execute(
    		"CREATE TABLE `bportal_destinations` ( \n" +
            "  `bp_name` VARCHAR(40) NOT NULL, \n" +
            "  `bp_serverName` VARCHAR(45) NOT NULL, \n" +
            "  `bp_worldName` VARCHAR(45) NOT NULL, \n" +
            "  `bp_posX` DOUBLE NOT NULL, \n" +
            "  `bp_posY` DOUBLE NOT NULL, \n" +
            "  `bp_posZ` DOUBLE NOT NULL, \n" +
            "  `bp_pitch` DOUBLE NOT NULL, \n" +
            "  `bp_yaw` DOUBLE NOT NULL, \n" +
            "  PRIMARY KEY (`bp_name`)); \n" +
			"");
    	}
        if (!this.database.existsTable("bportal_portals")) {
        	this.plugin.log(Level.WARNING, "Creating bportal_portals Schema...");
        	this.database.execute(
    		"CREATE TABLE `bportal_portals` ( \n" +
            "  `bp_block` VARCHAR(64) NOT NULL, \n" +
            "  `bp_destination` VARCHAR(40) NOT NULL, \n" +
            "  PRIMARY KEY (`bp_block`)); \n" +
			"");
    	}
    	this.plugin.log(Level.INFO, "Database ready...");
	}

    public void handlePlayerJoin(PlayerJoinEvent e) {
	
	    Player player = e.getPlayer();
    	String uuid = player.getUniqueId().toString();
	    String serverName = player.getWorld().getName();
	    serverName = plugin.configFile.getString("servers." + serverName + ".name", serverName);

	    ResultSet result = this.database.select(
	    		"SELECT `bp_worldName`, `bp_posX`, `bp_posY`, `bp_posZ`, `bp_pitch`, `bp_yaw` FROM `bportal_teleports` \n" +
	    		"WHERE `bp_playerUUID` = '" + uuid + "' AND `bp_serverName` = '" + serverName + "';");
	    
	    try {
			if (result.first()) {
				Location loc;
				try {
					int ix = 0;
		    	    World world = player.getServer().getWorld(result.getString(++ix));
		    	    loc = new Location(world, result.getDouble(++ix), result.getDouble(++ix), result.getDouble(++ix));
		    	    loc.setPitch((float)result.getDouble(++ix));
		    	    loc.setYaw((float)result.getDouble(++ix));
				}
				finally {
					result.close();
				}
	    	    player.teleport(loc, TeleportCause.PLUGIN);

	    	    this.database.execute(
			    		"DELETE FROM `bportal_teleports` \n" +
			    		"WHERE `bp_playerUUID` = '" + uuid + "' AND `bp_serverName` = '" + serverName + "';");
			}
		} catch (SQLException e1) {
        	plugin.log(Level.SEVERE, "SQLException! " + e1.getMessage());
		}
    }
    
    public void handlePlayerTeleport(Player player, String destName) {

    	NamedDestination dest = this.getDestination(destName);
    	
        if (dest.name == null || !dest.name.equalsIgnoreCase(destName)) {
        	player.sendMessage(ChatColor.RED + "The destination '" + destName + "' does not exist.");
        }
        else if(!player.hasPermission("bungeewarped.portal.*") && !player.hasPermission("bungeewarped.portal." + dest.name)) {
    	    player.sendMessage(plugin.configFile.getString("NoPortalPermissionMessage").replace("{destination}", dest.name).replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
            return;
        }
        
    	World localtp = player.getServer().getWorld(dest.worldName);
    	if (localtp != null) {
    		Location loc = new Location(localtp, dest.X, dest.Y, dest.Z);
    	    loc.setPitch(dest.pitch);
    	    loc.setYaw(dest.yaw);
    	    player.teleport(loc, TeleportCause.PLUGIN);
    	    return;
    	}
    	
    	String uuid = player.getUniqueId().toString();
    	this.database.execute(
			"INSERT INTO `bportal_teleports` \n" +
    			"(`bp_playerUUID`, `bp_serverName`, `bp_worldName`, `bp_posX`, `bp_posY`, `bp_posZ`, `bp_pitch`, `bp_yaw`) \n" +
    			"VALUES ( \n" +
    			"'" + uuid + "', \n" +
    			"'" + dest.serverName + "', \n" +
    			"'" + dest.worldName + "', \n" +
    			dest.X + ", \n" +
    			dest.Y + ", \n" +
    			dest.Z + ", \n" +
    			dest.pitch + ", \n" +
    			dest.yaw + " ) \n" +
    			"ON DUPLICATE KEY UPDATE \n" +
    			"bp_worldName = '" + dest.worldName + "', \n" +
    			"bp_posX = " + dest.X + ", \n" +
    			"bp_posY = " + dest.Y + ", \n" +
    			"bp_posZ = " + dest.Z + ", \n" +
    			"bp_pitch = " + dest.pitch + ", \n" +
    			"bp_yaw = " + dest.yaw + "; \n"
    			);

    	try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(baos);
	        dos.writeUTF("Connect");
			dos.writeUTF(dest.serverName);
	        player.sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());
	        baos.close();
	        dos.close();
    	}
    	catch (Exception e) {
        	plugin.log(Level.SEVERE, "Exception! " + e.toString());
    	}
    }

	public void addPortalBlock(String block, String destName) {
    	this.database.execute(
			"INSERT INTO `bportal_portals` \n" +
    			"(`bp_block`, `bp_destination`) \n" +
    			"VALUES ( \n" +
    			"'" + block + "', \n" +
    			"'" + destName + "' ) \n" +
    			"ON DUPLICATE KEY UPDATE \n" +
    			"bp_block = '" + block + "', \n" +
    			"bp_destination = '" + destName + "'; \n"
    			);
	}

	public void removePortalBlock(String block) {
	    this.database.execute(
	    		"DELETE FROM `bportal_portals` \n" +
	    		"WHERE `bp_block` = '" + block + "';");
	}

	public Map<String, String> getPortalBlocks() {
        Map<String, String> portalData = new HashMap<>();
        
	    try {
		    ResultSet result = this.database.select(
		    		"SELECT `bp_block`, `bp_destination` FROM `bportal_portals`;");
		    try {
				while (result.next()) {
					portalData.put(result.getString(1), result.getString(2));
				}
		    }
		    finally {
		    	result.close();
		    }
		    
			plugin.log(Level.INFO, "Loaded portal locations: " + String.valueOf(portalData.size()));
			return portalData;
		} catch (SQLException e1) {
        	plugin.log(Level.SEVERE, "SQLException! " + e1.getMessage());
		}
	    
	    return portalData;
	}

	public void addDestination(String destName, String serverName, String worldName,
			double x, double y, double z, float pitch, float yaw) {

    	this.database.execute(
			"INSERT INTO `bportal_destinations` \n" +
    			"(`bp_name`, `bp_serverName`, `bp_worldName`, `bp_posX`, `bp_posY`, `bp_posZ`, `bp_pitch`, `bp_yaw`) \n" +
    			"VALUES ( \n" +
    			"'" + destName + "', \n" +
    			"'" + serverName + "', \n" +
    			"'" + worldName + "', \n" +
    			x + ", \n" +
    			y + ", \n" +
    			z + ", \n" +
    			pitch + ", \n" +
    			yaw + " ) \n" +
    			"ON DUPLICATE KEY UPDATE \n" +
    			"bp_serverName = '" + serverName + "', \n" +
    			"bp_worldName = '" + worldName + "', \n" +
    			"bp_posX = " + x + ", \n" +
    			"bp_posY = " + y + ", \n" +
    			"bp_posZ = " + z + ", \n" +
    			"bp_pitch = " + pitch + ", \n" +
    			"bp_yaw = " + yaw + "; \n"
    			);
	}

	public NamedDestination getDestination(String destName) {

		NamedDestination dest = new NamedDestination();
	    try {
		    ResultSet result = this.database.select(
		    		"SELECT `bp_name`, `bp_serverName`, `bp_worldName`, `bp_posX`, `bp_posY`, `bp_posZ`, `bp_pitch`, `bp_yaw` FROM `bportal_destinations` \n" +
		    		"WHERE `bp_name` = '" + destName + "';");
		    
			if (result.first()) {
				try {
					int ix = 0;
					dest.name = result.getString(++ix);
					dest.serverName = result.getString(++ix);
					dest.worldName = result.getString(++ix);
					dest.X = result.getDouble(++ix);
					dest.Y = result.getDouble(++ix);
					dest.Z = result.getDouble(++ix);
					dest.pitch = (float)result.getDouble(++ix);
					dest.yaw = (float)result.getDouble(++ix);
				}
				finally {
					result.close();
				}
			}
		} catch (SQLException e1) {
        	plugin.log(Level.SEVERE, "SQLException! " + e1.getMessage());
		}
	    
	    return dest;
	}
}
