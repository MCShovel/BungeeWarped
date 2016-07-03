package com.steamcraftmc.BungeeWarped.Commands;

import java.util.Iterator;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class CmdHome extends BaseCommand {

	public CmdHome(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.home", "home", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

		PlayerController ctrl = plugin.getPlayerController(player);
		if (!ctrl.verifyCooldownForTp(TeleportReason.HOME, true)) {
			return true;
		}
		
		boolean implieduse = true;
		String puuid = ctrl.playerUuid;
		String pname = ctrl.playerName;
		
		if (args.length > 0) {
			int divide = args[0].indexOf(':');
			if (divide > 0) {
				if (!player.hasPermission("bungeewarped.home.others")) {
					player.sendMessage(plugin.config.NoHomeFoundByName(args[0]));
					return true;
				}
				implieduse = false;
				puuid = null;
				pname = args[0].substring(0, divide);
				if (divide + 1 < args[0].length()) {
					args = new String[] { args[0].substring(divide + 1) };
				} else {
					args = new String[0];
				}
			}
		}
		
		String name;
		NamedDestination dest;
		
		if (args.length == 0) {
			List<NamedDestination> possible = plugin.dataStore.getPlayerHomes(puuid, pname);
			if (possible.size() == 0) {
				player.sendMessage(plugin.config.NoPlayerHomes());
			}
			else if (implieduse && possible.size() == 1) {
				dest = possible.get(0);
				dest.reason = TeleportReason.HOME;
				ctrl.teleportToDestination(dest);
			}
			else {
				StringBuilder sb = new StringBuilder();
				for(Iterator<NamedDestination> i = possible.iterator(); i.hasNext(); ) {
					dest = i.next();
		        	if (sb.length() > 0) {
		        		sb.append(", ");
		        	}
	        		sb.append(dest.name);
				}

		    	player.sendMessage(plugin.config.HomesList(sb.toString()));
			}
			return true;
		}
		else {
			name = args[0];
		}

		dest = plugin.dataStore.findPlayerHome(puuid, pname, name);
		if (dest == null) {
			player.sendMessage(plugin.config.NoHomeFoundByName(name));
			return true;
		}

		dest.reason = TeleportReason.HOME;
		ctrl.teleportToDestination(dest);
		return true;
	}

}
