package com.steamcraftmc.BungeeWarped.Storage;

import java.io.ByteArrayOutputStream;		
import java.io.DataOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;

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

    public void handlePlayerJoin(Player player) {

    	String uuid = player.getUniqueId().toString();
	    String serverName = player.getWorld().getName();
	    serverName = plugin.bungeeServerName;

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
				player.setBedSpawnLocation(loc, true);
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
    	handlePlayerTeleportTo(player, this.getDestination(destName));
    }

	public void handlePlayerTeleportTo(Player player, NamedDestination dest) {
	
        if (dest == null || dest.name == null) {
        	player.sendMessage(ChatColor.RED + "The destination does not exist.");
        	return;
        }
        else if(!player.hasPermission("bungeewarped.portal.use") || (
        !player.hasPermission("bungeewarped.warp.location.*") && !player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase()))) {
    	    player.sendMessage(plugin.configFile.getString("NoPortalPermissionMessage").replace("{destination}", dest.name).replaceAll("(&([a-f0-9l-or]))", "\u00A7$2"));
            return;
        }
        
    	World localtp = player.getServer().getWorld(dest.worldName);
    	if (localtp != null) {
    		Location loc = new Location(localtp, dest.X, dest.Y, dest.Z);
    	    loc.setPitch(dest.pitch);
    	    loc.setYaw(dest.yaw);
			player.setBedSpawnLocation(loc, true);
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

	public NamedDestination findDestinationForPlayer(Player player, String name) {
		NamedDestination dest = findDestination(name);
        if (dest == null || (!player.hasPermission("bungeewarped.warp.location.*") && !player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase()))) {
        	player.sendMessage(ChatColor.RED + "That location does not exist.");
            return null;
        }
        return dest;
	}
	
	public NamedDestination findDestination(String name) {
		NamedDestination dest = getDestination(name);
        if (dest == null) {
        	List<NamedDestination> possible = new ArrayList<NamedDestination>();
    		List<NamedDestination> all = plugin.dataStore.getDestinations();
    		for(Iterator<NamedDestination> i = all.iterator(); i.hasNext(); ) {
    			NamedDestination test = i.next();
    			if (test.name.toLowerCase().startsWith(name)) {
    				possible.add(test);
    			}
    		}
    		if (possible.size() == 1) {
    			dest = possible.get(0);
    		}
        }
        return dest;
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
	    
	    return dest.name != null ? dest : null;
	}

	public List<NamedDestination> getDestinations() {
		List<NamedDestination> all = new ArrayList<NamedDestination>();
	    try {
		    ResultSet result = this.database.select(
		    		"SELECT `bp_name`, `bp_serverName`, `bp_worldName`, `bp_posX`, `bp_posY`, `bp_posZ`, `bp_pitch`, `bp_yaw` FROM `bportal_destinations`;");

			try {
				while (result.next()) {
					int ix = 0;
					NamedDestination dest = new NamedDestination();
					dest.name = result.getString(++ix);
					dest.serverName = result.getString(++ix);
					dest.worldName = result.getString(++ix);
					dest.X = result.getDouble(++ix);
					dest.Y = result.getDouble(++ix);
					dest.Z = result.getDouble(++ix);
					dest.pitch = (float)result.getDouble(++ix);
					dest.yaw = (float)result.getDouble(++ix);
					all.add(dest);
				}
			}
			finally {
				result.close();
			}
			
		} catch (SQLException e1) {
        	plugin.log(Level.SEVERE, "SQLException! " + e1.getMessage());
		}
	    
	    return all;
	}

	public void deleteDestination(Player player, String name) {
		this.database.execute(
	    		"DELETE FROM `bportal_destinations` \n" +
	    		"WHERE `bp_name` = '" + name + "';");
	}

}
