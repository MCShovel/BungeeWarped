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
		if (args.length > 0) {
			name = args[0];
		}
		
		NamedDestination existing = plugin.dataStore.findPlayerHome(ctrl.playerUuid, name);
		if (existing != null) {
			plugin.dataStore.deletePlayerHome(ctrl.playerUuid, existing.name);
			player.sendMessage(plugin.config.HomeDelConfirm(existing.name));
		} else {
	        player.sendMessage(plugin.config.NoHomeFoundByName(name));
		}

		return true;
	}

}
