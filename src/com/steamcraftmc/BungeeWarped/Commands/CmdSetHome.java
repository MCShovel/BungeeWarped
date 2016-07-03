package com.steamcraftmc.BungeeWarped.Commands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;

public class CmdSetHome extends BaseCommand {

	public CmdSetHome(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.home", "sethome", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

		String name = "home";
		PlayerController ctrl = plugin.getPlayerController(player);
		if (args.length > 0) {
			name = args[0];
		}
		
		if (!name.matches("^\\w+$")) {
			player.sendMessage(plugin.config.InvalidNameForHome(name));
			return true;
		}
		
		boolean replace = false;
		List<NamedDestination> existing = plugin.dataStore.getPlayerHomes(ctrl.playerUuid, null);
		for(Iterator<NamedDestination> i = existing.iterator(); i.hasNext(); ) {
			NamedDestination dest = i.next();
			if (dest.name.equalsIgnoreCase(name)) {
				replace = true;
				break;
			}
		}

		int needed = 1 + existing.size() - (replace ? 1 : 0);
		if (needed > 1) {
			if (!player.hasPermission("bungeewarped.home.multi." + String.valueOf(needed)) && 
					!player.hasPermission("bungeewarped.home.unlimited")) {
				player.sendMessage(plugin.config.NoMoreHomesAllowed(needed));
				return true;
			}
		}
		
		ctrl.setHome(name, player.getLocation());
		player.sendMessage(plugin.config.HomeSetConfirm(name));
		return true;
	}

}
