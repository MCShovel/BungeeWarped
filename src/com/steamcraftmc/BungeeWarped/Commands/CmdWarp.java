package com.steamcraftmc.BungeeWarped.Commands;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.PlayerState;

public class CmdWarp extends BaseCommand {

	public CmdWarp(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.warp.use", "warp", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerState state, Command cmd, String[] args) {

		if (args.length == 0) {
			doListWarps(player);
			return true;
		}
		if (args.length == 1) {
			doWarp(player, args[0].toLowerCase());
			return true;
		}
		
		return false;
	}

	private void doWarp(Player player, String name) {

		NamedDestination dest = plugin.dataStore.getDestination(name);
        if (dest == null) {
        	List<NamedDestination> possible = new ArrayList<NamedDestination>();
    		List<NamedDestination> all = plugin.dataStore.getDestinations();
    		for(Iterator<NamedDestination> i = all.iterator(); i.hasNext(); ) {
    			NamedDestination test = i.next();
    			if (test.name.toLowerCase().startsWith(name)) {
    				possible.add(test);
    			}
    		}
    		if (possible.size() == 1) {
    			dest = possible.get(0);
    		}
    		else {
    			player.sendMessage(ChatColor.RED + "The warp '" + name + "' does not exist.");
    			return;
    		}
        }

        if (!player.hasPermission("bungeewarped.warp.location.*") && !player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase())) {
        	player.sendMessage(ChatColor.RED + "The warp '" + name + "' does not exist.");
            return;
        }

		plugin.dataStore.handlePlayerTeleportTo(player, dest);
	}

	private void doListWarps(Player player) {
		List<NamedDestination> possible = plugin.dataStore.getDestinations();
		
		StringBuilder sb = new StringBuilder();
		for(Iterator<NamedDestination> i = possible.iterator(); i.hasNext(); ) {
			NamedDestination dest = i.next();

	        if (player.hasPermission("bungeewarped.warp.location.*") || player.hasPermission("bungeewarped.warp.location." + dest.name.toLowerCase())) {
	        	if (sb.length() > 0) {
	        		sb.append(", ");
	        	}
        		sb.append(dest.name);
	        }
		}

    	player.sendMessage(ChatColor.GOLD + "Warps" + ChatColor.WHITE + ": " + sb);
	}
}
