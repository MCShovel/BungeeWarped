package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public class CmdTpOverride extends BaseCommand {

	public CmdTpOverride(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.op.tpo", "tpo", 1, 2);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		
		if (args.length == 1) {
			plugin.sendPlayerMessage(player, args[0], "TpaRequest", 
					new String[] { "force", player.getName() }
			);
		}
		else if (args.length == 2) {
			plugin.sendPlayerMessage(player, args[1], "TpaRequest", 
					new String[] { "force", args[0] }
			);
		}
		
		return true;
	}

}
