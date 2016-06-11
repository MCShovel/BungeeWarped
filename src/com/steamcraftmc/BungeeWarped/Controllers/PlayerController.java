package com.steamcraftmc.BungeeWarped.Controllers;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class PlayerController {
    private final BungeeWarpedBukkitPlugin plugin;
    private final Player player;
    public final String playerName;
    public final String playerUuid;

	public long joinTime;
	public long lastCombat;
	
	public boolean isInsidePortal;
	public long portalEnterTime;
	public String portalDestination;

	public PlayerController(BungeeWarpedBukkitPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerName = player.getName();
        this.playerUuid = player.getUniqueId().toString();
	}

	public void onPlayerJoin() {
		joinTime = System.currentTimeMillis();
		isInsidePortal = true;
		
		NamedDestination loc = plugin.dataStore.getPlayerJoinLocation(this.playerUuid);
		if (loc != null) {
			teleportToLocation(loc);
		    plugin.dataStore.removePlayerJoinLocation(this.playerUuid);
		}
	}

	public void stepIntoPortal(boolean isInPortal, String destination) {
		if (!this.isInsidePortal && isInPortal) {
			this.portalEnterTime = System.currentTimeMillis();
			this.isInsidePortal = true;
			
			if (!player.hasPermission("bungeewarped.portal.use")) {
	    	    player.sendMessage(plugin.config.NoPortalAccess(destination));
			}
			teleportToDestinationName(destination, TeleportReason.PORTAL);
		}
		else if (this.isInsidePortal && !isInPortal) {
			this.isInsidePortal = false;
		}
		else {
			// nothing to do...
		}
	}

	public void teleportToLocation(NamedDestination dest) {
		Location loc = dest.toPlayerLocation();
		if (plugin.config.setSpawnOnTeleport(dest.reason)) {
			player.setBedSpawnLocation(loc, true);
		}
	    player.teleport(loc, TeleportCause.PLUGIN);
	}

    public void teleportToDestinationName(String destName, TeleportReason reason) {
    	NamedDestination dest = plugin.dataStore.getDestination(destName);
    	dest.reason = reason;
    	teleportToDestination(dest);
    }

	public void teleportToDestination(NamedDestination dest) {
	
        if (dest == null || dest.name == null) {
        	player.sendMessage(ChatColor.RED + "The destination does not exist.");
        	return;
        }
        else if(!player.hasPermission("bungeewarped.warp.location.*") && !player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase())) {
    	    player.sendMessage(plugin.config.NoPortalAccess(dest.name));
            return;
        }

        if (dest.serverName == null || dest.serverName.equalsIgnoreCase(plugin.getServerName())) {
        	teleportToLocation(dest);
        	return;
        }

        plugin.dataStore.saveTeleportDestination(playerUuid, dest);
        sendToServer(dest.serverName);
    }

	public void sendToServer(String serverName) {
    	try {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        DataOutputStream dos = new DataOutputStream(baos);
	        dos.writeUTF("Connect");
			dos.writeUTF(serverName);
	        player.sendPluginMessage(plugin, "BungeeCord", baos.toByteArray());
	        baos.close();
	        dos.close();
    	}
    	catch (Exception e) {
        	plugin.log(Level.SEVERE, "Exception! " + e.toString());
    	}
	}
	
	public void setHome(String name, Location location) {
		plugin.dataStore.addPlayerHome(playerUuid, playerName, 
				name, plugin.getServerName(), location.getWorld().getName(),
				location.getBlockX(), location.getBlockY(), location.getBlockZ(), 
				location.getPitch(), location.getYaw());
		
	}
}
