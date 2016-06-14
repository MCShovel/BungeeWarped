package com.steamcraftmc.BungeeWarped.Commands;

import java.util.logging.Level;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class BaseCommand implements CommandExecutor {

    protected final BungeeWarpedBukkitPlugin plugin;
    protected final String permission;
    protected final String cmdName;
    private final int minArgs;
    private final int maxArgs;

    public BaseCommand(BungeeWarpedBukkitPlugin plugin, String permission, String command, int minArg, int maxArg) {
        this.plugin = plugin;
        this.permission = permission;
        this.cmdName = command;
        this.minArgs = minArg;
        this.maxArgs = maxArg;

        this.plugin.getCommand(this.cmdName).setExecutor(this);
    }
    
    protected abstract boolean doCommand(Player player, PlayerController controller, Command cmd, String[] args);
    
    public final boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
    	
        if (cmd == null || !cmdName.equalsIgnoreCase(cmd.getName()) || !(sender instanceof Player) ||
        		args == null || args.length < minArgs || args.length > maxArgs) {
            return false;
        }
        Player player = (Player) sender;
        
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.config.PermissionDenied(permission));
            plugin.log(Level.SEVERE, "The user " + player.getName() + " need permission " + permission + " to access to the command " + cmdName);
            return true;
        }
		if (!plugin.isReady(player)) {
			return true;
		}

		if (!sender.hasPermission("bungeewarped.bypass.blacklist") && plugin.config.getCommandIsBlacklisted(player.getWorld().getName(), cmdName)) {
            sender.sendMessage(plugin.config.NotAllowedHere(cmdName));
            return true;
		}
		
        PlayerController state = this.plugin.getPlayerController(player);
        if (player != null && state != null) {
        	return doCommand(player, state, cmd, args);
        }
        
    	return true;
    }
}
