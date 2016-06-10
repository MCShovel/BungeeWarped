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
		return false;
	}

	private void createDestination(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
            return;
        }
        Player player = (Player)sender;
        Location loc = player.getLocation();
        World world = player.getWorld();
    	String serverName = world.getName();
	    serverName = plugin.configFile.getString("servers." + serverName + ".name", serverName);
    	
    	String destName = args[2];
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
        sender.sendMessage(ChatColor.GOLD + "Warp '" + destName + "' created.");
	}
}
