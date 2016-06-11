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
	String fromPlayer, fromPlayerUuid;
	int taskId = -1;
	PendingRequestState state = PendingRequestState.PENDING;
	NamedDestination destination;
	
	public TpaRequestHandler(BungeeWarpedBukkitPlugin plugin, Player player, String[] args) {
		this.plugin = plugin;
		this.player = player;
		this.directionHere = args[0] == "here";
		this.fromPlayer = args[1];
		this.fromPlayerUuid = args[2];
		if (args.length > 3) {
			this.destination = NamedDestination.fromString(args[3]);
		}
	}

	public void start() {
		if (!player.isOnline()) return;
		if (plugin.getPlayerController(player).setPendingRequest(this)) {
			player.sendMessage(
					directionHere 
					? plugin.config.TpaRequest(fromPlayer) 
					: plugin.config.TpaHereRequest(fromPlayer)
			);
			taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 120000);
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
		
		if (state == PendingRequestState.ACCEPT) {
			if (directionHere) {
				plugin.sendPlayerMessage(player, fromPlayer, "TpaResponse", 
						new String[] { player.getName(), player.getUniqueId().toString(), state.toString() }
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
							new String[] { player.getName(), player.getUniqueId().toString(), state.toString(), dest.toString() }
					);
			}
		} else {
			plugin.sendPlayerMessage(player, fromPlayer, "TpaResponse", 
						new String[] { player.getName(), player.getUniqueId().toString(), state.toString() }
				);
		}
		
	}
}


