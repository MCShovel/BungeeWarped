package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public class CmdTpToggle extends BaseCommand {

	public CmdTpToggle(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.tp.toggle", "tptoggle", 0, 0);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		boolean value = !state.ignoreRequests;
		state.ignoreRequests = value;
		if (value) {
			player.sendMessage(plugin.config.TpRequestIgnore());
		} else {			
			player.sendMessage(plugin.config.TpRequestUnignore());
		}
		return true;
	}
}
