package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class CmdTpaHere extends BaseCommand {

	public CmdTpaHere(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.tpahere", "tpahere", 1, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		if (!state.verifyCooldownForTp(TeleportReason.TPA, true)) {
			return true;
		}

		NamedDestination dest = NamedDestination.create(plugin, player.getName(), player.getLocation(), TeleportReason.TPA);
		plugin.sendPlayerMessage(player, args[0], "TpaRequest", 
					new String[] { "here", player.getName(), dest.toString() }
			);
		return true;
	}

}
