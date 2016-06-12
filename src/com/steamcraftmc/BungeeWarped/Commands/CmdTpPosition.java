package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class CmdTpPosition extends BaseCommand {

	public CmdTpPosition(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.op.tppos", "tppos", 3, 7);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		
		NamedDestination dest = NamedDestination.create(plugin, player.getName(), player.getLocation(), TeleportReason.TPPOS);
		try {
			dest.X = parseRelative(dest.X, args[0]);
			dest.Y = parseRelative(dest.Y, args[1]);
			dest.Z = parseRelative(dest.Z, args[2]);
			
			if (args.length > 3) {
				dest.yaw = Float.parseFloat(args[3]);

				if (args.length > 4) {
					dest.pitch = Float.parseFloat(args[4]);

					if (args.length > 5) {
						dest.worldName = args[5];

						if (args.length > 6) {
							dest.serverName = args[6];
						}
					}
				}
			}
		} catch(Exception ex) {
			player.sendMessage(ChatColor.RED + ex.getMessage());
		}

		state.teleportToDestination(dest);
		return true;
	}

	private double parseRelative(double current, String string) {
		boolean rel = false;
		if (string.startsWith("~")) {
			rel = true;
			string = string.substring(1);
			if (string.equals("")) {
				return current;
			}
		}

		double val = Double.parseDouble(string);
		if (rel) {
			val = current + val;
		}
		
		return val;
	}
}
