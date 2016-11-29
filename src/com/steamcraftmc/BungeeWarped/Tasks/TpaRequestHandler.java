package com.steamcraftmc.BungeeWarped.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class TpaRequestHandler implements Runnable, PendingTpaRequest {
	BungeeWarpedBukkitPlugin plugin;
	Player player;
	boolean directionHere;
	boolean forcedTp;
	String fromPlayer;
	int taskId = -1;
	PendingRequestState state = PendingRequestState.PENDING;
	NamedDestination destination;
	
	public TpaRequestHandler(BungeeWarpedBukkitPlugin plugin, Player player, String[] args) {
		this.plugin = plugin;
		this.player = player;
		this.directionHere = args[0].equals("here");
		this.forcedTp = args[0].equals("force");
		this.fromPlayer = args[1];
		if (args.length > 2) {
			this.destination = NamedDestination.fromString(args[2]);
		}
	}

	public void start() {
		if (!player.isOnline()) return;
		if (forcedTp) {
			NamedDestination dest = NamedDestination.create(plugin, player.getName(), player.getLocation(), TeleportReason.TPO);
			plugin.sendPlayerMessage(player, fromPlayer, "TpaResponse", 
						new String[] { player.getName(), PendingRequestState.FORCED.toString(), dest.toString() }
				);
		}
		else if (!player.hasPermission("bungeewarped.tpaccept")) {
			Complete(player, plugin.getPlayerController(player), PendingRequestState.NOTALLOWED);
		}
		else if (destination != null && !plugin.getPlayerController(player).canTeleportToWorld(destination)) {
			Complete(player, plugin.getPlayerController(player), PendingRequestState.NOTALLOWED);
		}
		else if (!plugin.getPlayerController(player).verifyCooldownForTp(TeleportReason.TPA, false)) {
			Complete(player, plugin.getPlayerController(player), PendingRequestState.TPCOOLDOWN);
		}
		else if (plugin.getPlayerController(player).setPendingRequest(this)) {
			player.sendMessage(
					directionHere 
					? plugin.config.TpaHereRequest(fromPlayer) 
					: plugin.config.TpaRequest(fromPlayer)
			);
			taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 2400);
		}
	}
	
	@Override
	public void run() {
		if (state == PendingRequestState.PENDING && player.isOnline()) {
			taskId = -1;
			Complete(player, plugin.getPlayerController(player), PendingRequestState.TIMEOUT);
			player.sendMessage(plugin.config.TpaTimeoutExpired());
		}
	}

	@Override
	public PendingRequestState getState() {
		return state;
	}
	
	@Override
	public void Complete(Player player, PlayerController cont, PendingRequestState state) {
		if (taskId != -1) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
		
		this.state = state;
		
		if (state == PendingRequestState.ACCEPT) {
			if (directionHere) {
				plugin.sendPlayerMessage(player, fromPlayer, "TpaResponse", 
						new String[] { player.getName(), state.toString() }
				);
				
				if (destination == null) {
					player.sendMessage("ERROR: No destination specified.");
					return;
				}
	
				destination.reason = TeleportReason.TPA;
				cont.teleportToDestination(destination);
			} else {
				NamedDestination dest = NamedDestination.create(plugin, player.getName(), player.getLocation(), TeleportReason.TPA);
				plugin.sendPlayerMessage(player, fromPlayer, "TpaResponse", 
							new String[] { player.getName(), state.toString(), dest.toString() }
					);
			}
		} else {
			plugin.sendPlayerMessage(player, fromPlayer, "TpaResponse", 
						new String[] { player.getName(), state.toString() }
				);
		}
		
	}
}


