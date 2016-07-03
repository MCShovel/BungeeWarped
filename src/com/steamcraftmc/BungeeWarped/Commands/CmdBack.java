package com.steamcraftmc.BungeeWarped.Commands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class CmdBack extends BaseCommand {

	public CmdBack(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.back", "back", 0, 0);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

		PlayerController ctrl = plugin.getPlayerController(player);
		if (!ctrl.verifyCooldownForTp(TeleportReason.BACK, true)) {
			return true;
		}
		
		NamedDestination dest = plugin.dataStore.getPlayerBack(ctrl.playerUuid, ctrl.playerName);
		if (dest == null) {
			dest.reason = TeleportReason.BACK;
			player.sendMessage(plugin.config.NoPlayerBack());
		}

		dest.reason = TeleportReason.HOME;
		ctrl.teleportToDestination(dest);
		return true;
	}

}
