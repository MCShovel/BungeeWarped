package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;

public class CmdDelHome extends BaseCommand {

	public CmdDelHome(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.home", "delhome", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

		String name = "home";
		PlayerController ctrl = plugin.getPlayerController(player);

		String puuid = ctrl.playerUuid;
		String pname = ctrl.playerName;
		
		if (args.length > 0) {
			int divide = args[0].indexOf(':');
			if (divide > 0) {
				if (!player.hasPermission("bungeewarped.home.others")) {
					player.sendMessage(plugin.config.NoHomeFoundByName(args[0]));
					return true;
				}
				puuid = null;
				pname = args[0].substring(0, divide);
				if (divide + 1 < args[0].length()) {
					args = new String[] { args[0].substring(divide + 1) };
				} else {
					args = new String[0];
				}
			}
		}
		
		if (args.length > 0) {
			name = args[0];
		}
		
		NamedDestination existing = plugin.dataStore.findPlayerHome(puuid, pname, name);
		if (existing != null) {
			plugin.dataStore.deletePlayerHome(puuid, pname, existing.name);
			player.sendMessage(plugin.config.HomeDelConfirm(existing.name));
		} else {
	        player.sendMessage(plugin.config.NoHomeFoundByName(name));
		}

		return true;
	}

}
