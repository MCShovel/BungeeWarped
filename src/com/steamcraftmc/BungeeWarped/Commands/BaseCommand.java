package com.steamcraftmc.BungeeWarped.Commands;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Storage.PlayerState;

import org.bukkit.ChatColor;
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
    }
    
    protected abstract boolean doCommand(Player player, PlayerState state, Command cmd, String[] args);
    
    public final boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd == null || !cmdName.equalsIgnoreCase(cmd.getName()) || !(sender instanceof Player) ||
        		args == null || args.length < minArgs || args.length > maxArgs) {
            return false;
        }
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }

        Player player = (Player) sender;
        PlayerState state = this.plugin.getPlayerState(player);
        if (player != null && state != null) {
        	return doCommand(player, state, cmd, args);
        }
        
    	return true;
    }
}
