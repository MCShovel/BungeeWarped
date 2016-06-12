package com.steamcraftmc.BungeeWarped.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class TpaResponseHandler implements Runnable {

	private BungeeWarpedBukkitPlugin plugin;
	private Player player;
	private String fromPlayer;//, fromPlayerUuid;
	private PendingRequestState response;
	private NamedDestination destination;

	public TpaResponseHandler(BungeeWarpedBukkitPlugin plugin, Player player, String[] args) {
		this.plugin = plugin;
		this.player = player;
		this.fromPlayer = args[0];
		this.response = PendingRequestState.valueOf(args[1]);
		if (args.length > 2) {
			this.destination = NamedDestination.fromString(args[2]);
		}
	}

	public void start() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 10);
	}
	
	@Override
	public void run() {
		if (player.isOnline()) {
			switch(response) {
			case ACCEPT:
				player.sendMessage(plugin.config.RequestAccepted(fromPlayer));
				if (destination != null) {
					destination.reason = TeleportReason.TPA;
					plugin.getPlayerController(player).teleportToDestination(destination);
				}
				break;
			case CANCEL:
				player.sendMessage(plugin.config.RequestCancelled(fromPlayer));
				break;
			case DENY:
				player.sendMessage(plugin.config.RequestDenied(fromPlayer));
				break;
			case LOGOUT:
				player.sendMessage(plugin.config.RequestTargetLogout(fromPlayer));
				break;
			case TIMEOUT:
				player.sendMessage(plugin.config.RequestTimeout(fromPlayer));
				break;
			case REPLACED:
				player.sendMessage(plugin.config.RequestInterrupt(fromPlayer));
				break;
			case TPCOOLDOWN:
				player.sendMessage(plugin.config.RequesteeOnCooldown(fromPlayer));
				break;
			case NOTALLOWED:
				player.sendMessage(plugin.config.RequesteeCanNotTp(fromPlayer));
				break;
			case FORCED:
				if (destination != null) {
					destination.reason = TeleportReason.TPO;
					plugin.getPlayerController(player).teleportToDestination(destination);
				}
				break;
			case PENDING:
			default:
				break;
			}
		}
	}
}
