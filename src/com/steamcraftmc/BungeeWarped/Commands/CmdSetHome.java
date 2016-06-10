package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.PlayerState;

public class CmdSetHome extends BaseCommand {

	public CmdSetHome(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.home.create", "sethome", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerState state, Command cmd, String[] args) {
		if (args.length == 0) {
			player.setBedSpawnLocation(player.getLocation(), true);
			return true;
		}
		return false;
	}

}
