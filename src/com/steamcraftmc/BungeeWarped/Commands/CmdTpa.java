package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public class CmdTpa extends BaseCommand {

	public CmdTpa(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.tp.there", "tpa", 1, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		plugin.sendPlayerMessage(player, args[0], "TpaRequest", 
					new String[] { "there", player.getName() }
			);
		return true;
	}

}
