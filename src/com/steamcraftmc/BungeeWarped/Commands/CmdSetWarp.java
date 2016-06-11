package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public class CmdSetWarp extends BaseCommand {

	public CmdSetWarp(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.warp.create", "setwarp", 1, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {
		if (plugin.bungeeServerName == null) {
            player.sendMessage(ChatColor.RED + "BungeeCord server name was not found.");
			return true;
		}

		if (args.length == 1) {
			createDestination(player, args[0]);
			return true;
		}

		return false;
	}

	private void createDestination(Player player, String destName) {
        Location loc = player.getLocation();
        World world = player.getWorld();
	    String serverName = plugin.bungeeServerName;

    	plugin.dataStore.addDestination(
    		destName,
    		serverName,
    		world.getName(),
    		loc.getX(),
    		loc.getY(),
    		loc.getZ(),
    		loc.getPitch(),
    		loc.getYaw()
		);	
        player.sendMessage(ChatColor.GOLD + "Warp '" + destName + "' created.");
	}
}
