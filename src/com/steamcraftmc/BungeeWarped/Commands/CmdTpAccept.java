package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Tasks.PendingRequestState;

public class CmdTpAccept extends BaseCommand {

	public CmdTpAccept(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.tpaccept", "tpaccept", 0, 0);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		if (plugin.getPlayerController(player).completePendingRequest(PendingRequestState.ACCEPT)) {
			player.sendMessage(plugin.config.TeleportAccepted());
		}
		else {
			player.sendMessage(plugin.config.NoPendingRequest());
		}
		return true;
	}
}
