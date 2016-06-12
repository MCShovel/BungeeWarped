package com.steamcraftmc.BungeeWarped.Commands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public class CmdWarp extends BaseCommand {

	public CmdWarp(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.warp.use", "warp", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		if (!state.verifyCooldownForTp(TeleportReason.WARP)) {
			return true;
		}

		if (args.length == 0) {
			doListWarps(player);
			return true;
		}
		if (args.length == 1) {
			doWarp(player, args[0].toLowerCase());
			return true;
		}
		
		return false;
	}

	private void doWarp(Player player, String name) {

		NamedDestination dest = plugin.dataStore.findDestinationForPlayer(player, name);

        if (dest == null || dest.name == null) {
        	player.sendMessage(ChatColor.RED + "The destination does not exist.");
        	return;
        }
        else if(!player.hasPermission("bungeewarped.warp.location.*") && !player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase())) {
    	    player.sendMessage(plugin.config.NoPortalAccess(dest.name));
            return;
        }

		dest.reason = TeleportReason.WARP;
		plugin.getPlayerController(player).teleportToDestination(dest);
	}

	private void doListWarps(Player player) {
		List<NamedDestination> possible = plugin.dataStore.getDestinations();
		
		StringBuilder sb = new StringBuilder();
		for(Iterator<NamedDestination> i = possible.iterator(); i.hasNext(); ) {
			NamedDestination dest = i.next();

	        if (player.hasPermission("bungeewarped.warp.location.*") || player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase())) {
	        	if (sb.length() > 0) {
	        		sb.append(", ");
	        	}
        		sb.append(dest.name);
	        }
		}

    	player.sendMessage(ChatColor.GOLD + "Warps" + ChatColor.WHITE + ": " + sb);
	}
}
