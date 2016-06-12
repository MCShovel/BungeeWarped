package com.steamcraftmc.BungeeWarped.Storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.entity.Player;
import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;

public class MySqlDataStore {
    private BungeeWarpedBukkitPlugin plugin;
    private MySQLCore database;
    private String tablePrefix;

	public MySqlDataStore(BungeeWarpedBukkitPlugin plugin) {
        this.plugin = plugin;
        this.tablePrefix = "bwarped_";
	}
	
	public void initialize() {
		if (database != null) {
			database.close();
		}
		
        this.plugin.log(Level.INFO, "Initializing Database...");
        this.database = new MySQLCore(plugin, 
    		plugin.config.mysqlHost(),
    		plugin.config.mysqlDatabase(), 
    		plugin.config.mysqlPort(),
    		plugin.config.mysqlUsername(),
    		plugin.config.mysqlPassword()
		);
        this.tablePrefix = plugin.config.mysqlTablePrefix();

        this.database.execute(
    		"CREATE TABLE IF NOT EXISTS `" + tablePrefix + "teleports` ( \n" +
            "  `bw_playerUUID` VARCHAR(40) NOT NULL, \n" +
            "  `bw_serverName` VARCHAR(45) NOT NULL, \n" +
            "  `bw_worldName` VARCHAR(45) NOT NULL, \n" +
            "  `bw_posX` DOUBLE NOT NULL, \n" +
            "  `bw_posY` DOUBLE NOT NULL, \n" +
            "  `bw_posZ` DOUBLE NOT NULL, \n" +
            "  `bw_pitch` DOUBLE NOT NULL, \n" +
            "  `bw_yaw` DOUBLE NOT NULL, \n" +
            "  `bw_reason` VARCHAR(45) NOT NULL, \n" +
            "  PRIMARY KEY (`bw_playerUUID`, `bw_serverName`)); \n" +
			"");

        this.database.execute(
    		"CREATE TABLE IF NOT EXISTS `" + tablePrefix + "destinations` ( \n" +
            "  `bw_name` VARCHAR(40) NOT NULL, \n" +
            "  `bw_serverName` VARCHAR(45) NOT NULL, \n" +
            "  `bw_worldName` VARCHAR(45) NOT NULL, \n" +
            "  `bw_posX` DOUBLE NOT NULL, \n" +
            "  `bw_posY` DOUBLE NOT NULL, \n" +
            "  `bw_posZ` DOUBLE NOT NULL, \n" +
            "  `bw_pitch` DOUBLE NOT NULL, \n" +
            "  `bw_yaw` DOUBLE NOT NULL, \n" +
            "  PRIMARY KEY (`bw_name`)); \n" +
			"");

        this.database.execute(
    		"CREATE TABLE IF NOT EXISTS `" + tablePrefix + "portals` ( \n" +
            "  `bw_block` VARCHAR(64) NOT NULL, \n" +
            "  `bw_destination` VARCHAR(40) NOT NULL, \n" +
            "  PRIMARY KEY (`bw_block`)); \n" +
			"");

        this.database.execute(
    		"CREATE TABLE IF NOT EXISTS `" + tablePrefix + "homes` ( \n" +
            "  `bw_playerUUID` VARCHAR(40) NOT NULL, \n" +
            "  `bw_playerName` VARCHAR(64) NOT NULL, \n" +
            "  `bw_name` VARCHAR(40) NOT NULL, \n" +
            "  `bw_serverName` VARCHAR(45) NOT NULL, \n" +
            "  `bw_worldName` VARCHAR(45) NOT NULL, \n" +
            "  `bw_posX` DOUBLE NOT NULL, \n" +
            "  `bw_posY` DOUBLE NOT NULL, \n" +
            "  `bw_posZ` DOUBLE NOT NULL, \n" +
            "  `bw_pitch` DOUBLE NOT NULL, \n" +
            "  `bw_yaw` DOUBLE NOT NULL, \n" +
            "  PRIMARY KEY (`bw_playerUUID`, `bw_name`)); \n" +
			"");

    	this.plugin.log(Level.INFO, "Database ready...");
	}

    public NamedDestination getPlayerJoinLocation(String uuid) {

	    String serverName = plugin.bungeeServerName;

	    try {
		    ResultSet result = this.database.select(
		    		"SELECT `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw`, `bw_reason` FROM `" + tablePrefix + "teleports` \n" +
		    		"WHERE `bw_playerUUID` = '" + uuid + "' AND `bw_serverName` = '" + serverName + "';");
	    
			if (result.first()) {
				NamedDestination loc;
				try {
					int ix = 0;
					loc = new NamedDestination();
					loc.serverName = plugin.getServerName(); 
					loc.worldName = result.getString(++ix);
					loc.X = result.getDouble(++ix);
					loc.Y = result.getDouble(++ix);
					loc.Z = result.getDouble(++ix);
		    	    loc.pitch = (float)result.getDouble(++ix);
		    	    loc.yaw = (float)result.getDouble(++ix);
		    	    loc.reason = Enum.valueOf(TeleportReason.class, result.getString(++ix));
				}
				finally {
					result.close();
				}

				return loc;
			}
		} catch (SQLException e1) {
        	plugin.log(Level.SEVERE, "SQLException! " + e1.getMessage());
		}
	    return null;
    }
    
    public void removePlayerJoinLocation(String uuid) {
	    String serverName = plugin.bungeeServerName;
	    
		this.database.execute(
	    		"DELETE FROM `" + tablePrefix + "teleports` \n" +
	    		"WHERE `bw_playerUUID` = '" + uuid + "' AND `bw_serverName` = '" + serverName + "';");
    }
    
	public void saveTeleportDestination(String uuid, NamedDestination dest) {
    	this.database.execute(
			"INSERT INTO `" + tablePrefix + "teleports` \n" +
    			"(`bw_playerUUID`, `bw_serverName`, `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw`, `bw_reason`) \n" +
    			"VALUES ( \n" +
    			"'" + uuid + "', \n" +
    			"'" + dest.serverName + "', \n" +
    			"'" + dest.worldName + "', \n" +
    			dest.X + ", \n" +
    			dest.Y + ", \n" +
    			dest.Z + ", \n" +
    			dest.pitch + ", \n" +
    			dest.yaw + ", \n" +
    			"'" + dest.reason.toString() + "' ) \n" +
    			"ON DUPLICATE KEY UPDATE \n" +
    			"bw_worldName = '" + dest.worldName + "', \n" +
    			"bw_posX = " + dest.X + ", \n" +
    			"bw_posY = " + dest.Y + ", \n" +
    			"bw_posZ = " + dest.Z + ", \n" +
    			"bw_pitch = " + dest.pitch + ", \n" +
    			"bw_yaw = " + dest.yaw + ", \n" +
    			"bw_reason = '" + dest.reason.toString() + "'; \n"
			);
    }

	public void addPortalBlock(String block, String destName) {
    	this.database.execute(
			"INSERT INTO `" + tablePrefix + "portals` \n" +
    			"(`bw_block`, `bw_destination`) \n" +
    			"VALUES ( \n" +
    			"'" + block + "', \n" +
    			"'" + destName + "' ) \n" +
    			"ON DUPLICATE KEY UPDATE \n" +
    			"bw_block = '" + block + "', \n" +
    			"bw_destination = '" + destName + "'; \n"
    			);
	}

	public void removePortalBlock(String block) {
	    this.database.execute(
	    		"DELETE FROM `" + tablePrefix + "portals` \n" +
	    		"WHERE `bw_block` = '" + block + "';");
	}

	public Map<String, String> getPortalBlocks() {
        Map<String, String> portalData = new HashMap<>();
        
	    try {
		    ResultSet result = this.database.select(
		    		"SELECT `bw_block`, `bw_destination` FROM `" + tablePrefix + "portals`;");
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
			"INSERT INTO `" + tablePrefix + "destinations` \n" +
    			"(`bw_name`, `bw_serverName`, `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw`) \n" +
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
    			"bw_serverName = '" + serverName + "', \n" +
    			"bw_worldName = '" + worldName + "', \n" +
    			"bw_posX = " + x + ", \n" +
    			"bw_posY = " + y + ", \n" +
    			"bw_posZ = " + z + ", \n" +
    			"bw_pitch = " + pitch + ", \n" +
    			"bw_yaw = " + yaw + "; \n"
    			);
	}

	public NamedDestination findDestinationForPlayer(Player player, String name) {
		NamedDestination dest = findDestination(name);
        if (dest == null) {
        	player.sendMessage(plugin.config.WarpNotFound(name));
            return null;
        }
        if (!player.hasPermission("bungeewarped.location.*") && !player.hasPermission("bungeewarped.location." + dest.name.toLowerCase())) {
        	player.sendMessage(plugin.config.NoPortalAccess(dest.name));
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
		    		"SELECT `bw_name`, `bw_serverName`, `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw` FROM `" + tablePrefix + "destinations` \n" +
		    		"WHERE `bw_name` = '" + destName + "';");
		    
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
		    		"SELECT `bw_name`, `bw_serverName`, `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw` FROM `" + tablePrefix + "destinations`;");

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
					dest.reason = TeleportReason.WARP;
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
	    		"DELETE FROM `" + tablePrefix + "destinations` \n" +
	    		"WHERE `bw_name` = '" + name + "';");
	}

	public List<NamedDestination> getPlayerHomes(String playerUuid) {
		List<NamedDestination> all = new ArrayList<NamedDestination>();
	    try {
		    ResultSet result = this.database.select(
		    		"SELECT `bw_name`, `bw_serverName`, `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw` FROM `" + tablePrefix + "homes`" +
		    		"WHERE `bw_playerUUID` = '" + playerUuid + "';");

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
					dest.reason = TeleportReason.HOME;
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


	public NamedDestination findPlayerHome(String playerUuid, String name) {
		name = name.toLowerCase();
    	List<NamedDestination> possible = new ArrayList<NamedDestination>();
		List<NamedDestination> all = getPlayerHomes(playerUuid);
		for(Iterator<NamedDestination> i = all.iterator(); i.hasNext(); ) {
			NamedDestination test = i.next();
			if (test.name.toLowerCase().startsWith(name)) {
				possible.add(test);
			}
		}
		if (possible.size() == 1) {
			return possible.get(0);
		}
		return null;
	}

	public void addPlayerHome(String playerUuid, String playerName,
			String name, String serverName, String worldName, 
			int x, int y, int z, float pitch, double yaw) {

    	this.database.execute(
			"INSERT INTO `" + tablePrefix + "homes` \n" +
    			"(`bw_playerUUID`, `bw_playerName`, `bw_name`, `bw_serverName`, `bw_worldName`, `bw_posX`, `bw_posY`, `bw_posZ`, `bw_pitch`, `bw_yaw`) \n" +
    			"VALUES ( \n" +
    			"'" + playerUuid + "', \n" +
    			"'" + playerName + "', \n" +
    			"'" + name + "', \n" +
    			"'" + serverName + "', \n" +
    			"'" + worldName + "', \n" +
    			x + ", \n" +
    			y + ", \n" +
    			z + ", \n" +
    			pitch + ", \n" +
    			yaw + " ) \n" +
    			"ON DUPLICATE KEY UPDATE \n" +
    			"bw_playerName = '" + playerName + "', \n" +
    			"bw_serverName = '" + serverName + "', \n" +
    			"bw_worldName = '" + worldName + "', \n" +
    			"bw_posX = " + x + ", \n" +
    			"bw_posY = " + y + ", \n" +
    			"bw_posZ = " + z + ", \n" +
    			"bw_pitch = " + pitch + ", \n" +
    			"bw_yaw = " + yaw + "; \n"
    			);
	}


	public void deletePlayerHome(String playerUuid, String name) {
		this.database.execute(
	    		"DELETE FROM `" + tablePrefix + "homes` \n" +
	    		"WHERE `bw_playerUUID` = '" + playerUuid + "' AND `bw_name` = '" + name + "';");
	}

}
