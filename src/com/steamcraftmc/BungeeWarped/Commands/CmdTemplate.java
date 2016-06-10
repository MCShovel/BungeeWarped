package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.PlayerState;

public class CmdTemplate extends BaseCommand {

	public CmdTemplate(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.portal.cmd", "portal", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerState state, Command cmd, String[] args) {
		return false;
	}

}
