package com.steamcraftmc.BungeeWarped.Commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.PlayerState;

public class CmdSetWarp extends BaseCommand {

	public CmdSetWarp(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.portal.cmd", "portal", 0, 1);
	}

	@Override
	protected boolean doCommand(Player player, PlayerState state, Command cmd, String[] args) {
		if (plugin.bungeeServerName == null) {
            sender.sendMessage(ChatColor.RED + "BungeeCord server name was not found.");
			return true;
		}

		createDestination(player, destName);
		return false;
	}

	private void createDestination(Player player, String destName) {
        if (!(sender instanceof Player)) {
            player.sendMessage(ChatColor.RED + "Only players can use that command.");
            return;
        }
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
