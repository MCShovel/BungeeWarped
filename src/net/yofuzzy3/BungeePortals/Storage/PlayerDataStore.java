package net.yofuzzy3.BungeePortals.Storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import net.yofuzzy3.BungeePortals.BungeePortals;

public class PlayerDataStore {
    private BungeePortals plugin;
    private MySQLCore database;

	public PlayerDataStore(BungeePortals plugin) {
        this.plugin = plugin;
        this.plugin.log("Initializing Database Connection...");
        this.database = new MySQLCore(plugin, 
    		plugin.configFile.getString("mysql.host", "localhost"),
    		plugin.configFile.getString("mysql.database", "bportals"), 
    		plugin.configFile.getInt("mysql.port", 3306),
    		plugin.configFile.getString("mysql.username", "root"),
    		plugin.configFile.getString("mysql.password", "password")
		);

        if (!this.database.existsTable("bportal_playerDestination")) {
        	this.plugin.log("Creating Database Schema...");
        	this.database.execute(
    		"CREATE TABLE `bportal_playerDestination` ( \n" +
            "  `pd_playerUUID` VARCHAR(40) NOT NULL, \n" +
            "  `pd_serverName` VARCHAR(45) NOT NULL, \n" +
            "  `pd_worldName` VARCHAR(45) NOT NULL, \n" +
            "  `pd_posX` DOUBLE NOT NULL, \n" +
            "  `pd_posY` DOUBLE NOT NULL, \n" +
            "  `pd_posZ` DOUBLE NOT NULL, \n" +
            "  `pd_pitch` DOUBLE NOT NULL, \n" +
            "  `pd_yaw` DOUBLE NOT NULL, \n" +
            "  PRIMARY KEY (`pd_playerUUID`, `pd_serverName`)); \n" +
			"");
    	}
    	this.plugin.log("Database ready...");
	}

    public void handlePlayerJoin(PlayerJoinEvent e) {
	
	    Player player = e.getPlayer();
    	String uuid = player.getUniqueId().toString();
	    String serverName = player.getWorld().getName();
	    serverName = plugin.configFile.getString("servers." + serverName + ".name", serverName);

	    ResultSet result = this.database.select(
	    		"SELECT `pd_worldName`, `pd_posX`, `pd_posY`, `pd_posZ`, `pd_pitch`, `pd_yaw` FROM `bportal_playerDestination` \n" +
	    		"WHERE `pd_playerUUID` = '" + uuid + "' AND `pd_serverName` = '" + serverName + "';");
	    
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
			    		"DELETE FROM `bportal_playerDestination` \n" +
			    		"WHERE `pd_playerUUID` = '" + uuid + "' AND `pd_serverName` = '" + serverName + "';");
			}
		} catch (SQLException e1) {
        	plugin.log("SQLException! " + e1.getMessage());
		}
    }
    
    public String handlePlayerInPortal(Player player, String destination) {

        String targetServer = plugin.configFile.getString("destinations." + destination + ".server", destination);
        String targetWorld = plugin.configFile.getString("destinations." + destination + ".world", null);
        
        if (targetServer != null && targetWorld != null && 
        		(player.hasPermission("BungeePortals.portal.*") || player.hasPermission("BungeePortals.portal." + destination))) {

        	String uuid = player.getUniqueId().toString();
        	String x = String.valueOf(plugin.configFile.getInt("destinations." + destination + ".pos.X", 0));
        	String y = String.valueOf(plugin.configFile.getInt("destinations." + destination + ".pos.Y", 64));
        	String z = String.valueOf(plugin.configFile.getInt("destinations." + destination + ".pos.Z", 0));
        	String pitch = String.valueOf(plugin.configFile.getInt("destinations." + destination + ".pos.pitch", 0));
        	String yaw = String.valueOf(plugin.configFile.getInt("destinations." + destination + ".pos.yaw", 0));
        	
        	this.database.execute(
    			"INSERT INTO `bportal_playerDestination` \n" +
        			"(`pd_playerUUID`, `pd_serverName`, `pd_worldName`, `pd_posX`, `pd_posY`, `pd_posZ`, `pd_pitch`, `pd_yaw`) \n" +
        			"VALUES ( \n" +
        			"'" + uuid + "', \n" +
        			"'" + targetServer + "', \n" +
        			"'" + targetWorld + "', \n" +
        			x + ", \n" +
        			y + ", \n" +
        			z + ", \n" +
        			pitch + ", \n" +
        			yaw + " ) \n" +
        			"ON DUPLICATE KEY UPDATE \n" +
        			"pd_worldName = '" + targetWorld + "', \n" +
        			"pd_posX = " + x + ", \n" +
        			"pd_posY = " + y + ", \n" +
        			"pd_posZ = " + z + ", \n" +
        			"pd_pitch = " + pitch + ", \n" +
        			"pd_yaw = " + yaw + "; \n"
        			);
        }
        
        return targetServer;
    }
}
