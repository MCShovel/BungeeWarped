package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public class CmdDelWarp extends BaseCommand {

	public CmdDelWarp(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.setwarp", "delwarp", 1, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

		if (args.length == 1) {
			deleteWarp(player, args[0].toLowerCase());
			return true;
		}
		
		return false;
	}

	private void deleteWarp(Player player, String name) {

		NamedDestination dest = plugin.dataStore.findDestinationForPlayer(player, name);
		if (dest != null) {
			if (dest.name.equalsIgnoreCase(name)) {
				plugin.dataStore.deleteDestination(player, dest.name);
		        player.sendMessage(plugin.config.WarpDelConfirm(dest.name));
			} else {
		        player.sendMessage(plugin.config.WarpFullName(dest.name));
			}
		}
		else {
	        player.sendMessage(plugin.config.WarpNotFound(name));
		}
	}
}
