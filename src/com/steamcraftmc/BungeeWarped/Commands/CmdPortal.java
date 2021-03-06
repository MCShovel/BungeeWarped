package com.steamcraftmc.BungeeWarped.Commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Utils.WorldEditUtil;

public class CmdPortal extends BaseCommand {

	public CmdPortal(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.portal", "portal", 0, 2);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
        	this.doHelp(player);
        	return true;
        }
        else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
            	this.reloadConfig(player);
            	return true;
            }
            else if (args[0].equalsIgnoreCase("remove")) {
            	this.removePortal(player);
            	return true;
            }
        }
        else if (args.length == 2) {
        	if (args[0].equalsIgnoreCase("create")) {
            	this.createPortal(player, args[1]);
            	return true;
            }
        }
        
		return false;
	}

	private void doHelp(Player sender) {
        sender.sendMessage(ChatColor.YELLOW + "---- " + ChatColor.GOLD + "BungeeWarped v" + ChatColor.RED + 
        		plugin.getDescription().getVersion() + ChatColor.GOLD + " by MCShovel" + ChatColor.YELLOW + " ----");
        
        if (sender.hasPermission("bungeewarped.*")) {
        	sender.sendMessage(ChatColor.GOLD + "/portal reload" + ChatColor.WHITE + ": Reload all files and data.");
        }
        if (sender.hasPermission("bungeewarped.portal")) {
        	sender.sendMessage(ChatColor.GOLD + "/portal create <warp>" + ChatColor.WHITE + ": Create a portal to a warp.");
        	sender.sendMessage(ChatColor.GOLD + "/portal remove" + ChatColor.WHITE + ": Remove a portal.");
        }
        sender.sendMessage(ChatColor.BLUE + "Visit http://github.com/MCShovel/BungeeWarped/wiki for help.");
	}

    private void reloadConfig(CommandSender sender) {
        plugin.reload();
        sender.sendMessage(plugin.config.ConfigReloaded());
	}
    
    private List<String> getWorldEditSelection(Player player) {
    	if (plugin.worldEdit == null) {
    		player.sendMessage(plugin.config.WorldEditMissing());
            return null;
    	}
    	
    	WorldEditUtil weUtil = new WorldEditUtil(plugin);
    	if (!weUtil.hasSelection(player)) {
    		player.sendMessage(plugin.config.WorldEditNoSel());
            return null;
    	}
    	
    	return getSelection(player, weUtil.getMinimumPoint(player), weUtil.getMaximumPoint(player));
    }
    
    private List<String> getSelection(Player player, Location minLocation, Location maxLocation) {
    	List<String> blocks = new ArrayList<>();
    	World world = player.getWorld();
        String prefix = world.getName() + "#";
    	
        for (int i1 = minLocation.getBlockX(); i1 <= maxLocation.getBlockX(); i1++) {
            for (int i2 = minLocation.getBlockY(); i2 <= maxLocation.getBlockY(); i2++) {
                for (int i3 = minLocation.getBlockZ(); i3 <= maxLocation.getBlockZ(); i3++) {

                	Block block = world.getBlockAt(i1, i2, i3);
                    if (!block.getType().isSolid()) {
                        blocks.add(prefix + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ()));
                    }
                }
            }
        }

        if (blocks.size() == 0) {
    		player.sendMessage(plugin.config.EmptySelection());
            return null;
        }
        if (blocks.size() > 1000) {
    		player.sendMessage(plugin.config.SelectionTooLarge(blocks.size()));
            return null;
        }

        return blocks;
    }
    
	private void createPortal(Player player, String name) {
        List<String> selection = getWorldEditSelection(player);
        if (selection == null) {
        	return;
        }

        for (String block : selection) {
        	plugin.dataStore.addPortalBlock(block, name);
            plugin.portalData.put(block, name);
        }
        player.sendMessage(plugin.config.PortalCreated(name, selection.size()));
	}
	
	private void removePortal(Player player) {
        List<String> selection = getWorldEditSelection(player);
        if (selection == null) {
        	return;
        }

        int count = 0;
        for (String block : selection) {
            if (plugin.portalData.containsKey(block)) {
            	plugin.dataStore.removePortalBlock(block);
                plugin.portalData.remove(block);
                count++;
            }
        }
        player.sendMessage(plugin.config.PortalRemoved(count));
	}

}
