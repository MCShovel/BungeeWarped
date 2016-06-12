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
import com.steamcraftmc.BungeeWarped.Tasks.PendingRequestState;
import com.steamcraftmc.BungeeWarped.Tasks.PendingTpaRequest;
import com.steamcraftmc.BungeeWarped.Tasks.PlayerTeleportDelay;

public class PlayerController {
    private final BungeeWarpedBukkitPlugin plugin;
    public Player player;
    public final String playerName;
    public final String playerUuid;

	public long joinTime;
	public long lastCombat;
	public long lastTpTime;
	
	public boolean isInsidePortal;
	public long portalEnterTime;
	public String portalDestination;
	
	private PendingTpaRequest pendingRequest;
	public boolean ignoreRequests;

	public PlayerController(BungeeWarpedBukkitPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.playerName = player.getName();
        this.playerUuid = player.getUniqueId().toString();
	}

	public void onPlayerJoin() {
		joinTime = System.currentTimeMillis();
		isInsidePortal = true;
		pendingRequest = null;
		
		NamedDestination loc = plugin.dataStore.getPlayerJoinLocation(this.playerUuid);
		if (loc != null) {
			teleportToLocation(loc);
		    plugin.dataStore.removePlayerJoinLocation(this.playerUuid);
		}
	}

	public void onPlayerQuit() {
		completePendingRequest(PendingRequestState.LOGOUT);
	}

	public void onCombatTag() {
		this.lastCombat = System.currentTimeMillis();
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

	private void teleportToLocation(NamedDestination dest) {
		Location loc = dest.toPlayerLocation();
		if (plugin.config.setSpawnOnTeleport(dest.reason)) {
			player.setBedSpawnLocation(loc, true);
		}
	    player.teleport(loc, TeleportCause.PLUGIN);
	}

    public void teleportToDestinationName(String destName, TeleportReason reason) {
    	NamedDestination dest = plugin.dataStore.getDestination(destName);

        if (dest == null || dest.name == null) {
        	player.sendMessage(ChatColor.RED + "The destination does not exist.");
        	return;
        }
        else if(!player.hasPermission("bungeewarped.warp.location.*") && !player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase())) {
    	    player.sendMessage(plugin.config.NoPortalAccess(dest.name));
            return;
        }

    	dest.reason = reason;
    	teleportToDestination(dest);
    }

	public void teleportToDestination(NamedDestination dest) {
		if (dest.reason == TeleportReason.HOME || dest.reason == TeleportReason.PORTAL 
			|| dest.reason == TeleportReason.WARP || dest.reason == TeleportReason.TPA) {
			new PlayerTeleportDelay(plugin, this, dest).start();
		}
		else {
			teleportToDestinationNow(dest);
		}
	}
	

	public void teleportToDestinationNow(NamedDestination dest) {
	
		this.lastTpTime = System.currentTimeMillis();
		player.sendMessage(plugin.config.Teleporting());
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

	public boolean completePendingRequest(PendingRequestState state) {
		PendingTpaRequest old = pendingRequest;
		pendingRequest = null;
		if (old != null && old.getState() == PendingRequestState.PENDING) {
			old.Complete(player, this, state);
			return true;
		}
		return false;
	}

	public boolean setPendingRequest(PendingTpaRequest req) {
		completePendingRequest(PendingRequestState.REPLACED);
		if (ignoreRequests) {
			req.Complete(player, this, PendingRequestState.DENY);
			return false;
		} else {
			pendingRequest = req;
			return true;
		}
	}
	
	private int checkCombatDelay(int combatDelay) {
		long now = System.currentTimeMillis();
		long exp = lastCombat + (combatDelay * 1000);
		if (now > exp) 
			return 0;
		return (int)((exp - now) / 1000);
	}

	private int checkCooldownTime(int cooldown) {
		long now = System.currentTimeMillis();
		long exp = lastTpTime + (cooldown * 1000);
		if (now > exp) 
			return 0;
		return (int)((exp - now) / 1000);
	}
	
	public boolean verifyCooldownForTp(TeleportReason reason, boolean notify) {
		int cooldown = plugin.config.getTpCooldownTime(reason);
		int combatDelay = plugin.config.getCombatDelay(reason);

		if (player.hasPermission("bungeewarped.op.bypass.*") 
			|| player.hasPermission("bungeewarped.op.bypass.delay." + reason.toString().toLowerCase())) {
			cooldown = 0;
		}
		if (player.hasPermission("bungeewarped.op.bypass.*") || player.hasPermission("bungeewarped.op.bypass.delay.combat")) {
			combatDelay = 0;
		}
		
		if (combatDelay > 0) {
			int waitTime = checkCombatDelay(combatDelay);
			if (waitTime > 0) {
				if (notify)
					player.sendMessage(plugin.config.TeleportCancelledCombat(waitTime));
				return false;
			}
		}
		if (cooldown > 0) {
			int waitTime = checkCooldownTime(cooldown);
			if (waitTime > 0) {
				if (notify)
					player.sendMessage(plugin.config.TeleportCancelledCooldown(waitTime));
				return false;
			}
		}
		
		return true;
	}
}