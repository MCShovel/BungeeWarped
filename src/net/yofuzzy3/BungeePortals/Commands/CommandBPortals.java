	package net.yofuzzy3.BungeePortals.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.yofuzzy3.BungeePortals.BungeePortals;
import net.yofuzzy3.BungeePortals.Storage.PortalDataStore;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;

public class CommandBPortals implements CommandExecutor {

    private BungeePortals plugin;
    private Map<String, List<String>> selections = new HashMap<>();
    private PortalDataStore dataStore;

    public CommandBPortals(BungeePortals plugin, PortalDataStore dataStore) {
        this.plugin = plugin;
        this.dataStore = dataStore;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (!commandLabel.equalsIgnoreCase("BPortals")) {
            return false;
        }
        if (!sender.hasPermission("BungeePortals.command.BPortals")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
            return true;
        }

        if (args.length == 0) {
        	this.doHelp(sender);
        	return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
        	this.reloadConfig(sender);
        	return true;
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("clear")) {
        	this.clearSelection(sender);
        	return true;
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("remove")) {
        	this.removePortal(sender);
        	return true;
        }
        else if (args.length == 1 && args[0].equalsIgnoreCase("select")) {
        	this.storeSelection(sender);
        	return true;
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("create")) {
        	this.createPortal(sender, args);
        	return true;
        }
        else if (args.length == 2 && args[0].equalsIgnoreCase("tp")) {
        	this.teleportPlayer(sender, args);
        	return true;
        }
        else if (args.length == 3 && args[0].equalsIgnoreCase("dest")) {
        	this.createDestination(sender, args);
        	return true;
        }
        else  {
        	this.doHelp(sender);
            return true;
        }
    }

	private void doHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "BungeePortals v" + plugin.getDescription().getVersion() + " by YoFuzzy3");
        sender.sendMessage(ChatColor.GREEN + "/BPortals reload " + ChatColor.RED + "Reload all files and data.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals select <filter,list> " + ChatColor.RED + "Get selection.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals clear " + ChatColor.RED + "Clear selection.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals create <destination> " + ChatColor.RED + "Create portals.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals remove <destination> " + ChatColor.RED + "Remove portals.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals dest <server> <name>" + ChatColor.RED + "Create destination.");
        sender.sendMessage(ChatColor.GREEN + "/BPortals tp <name>" + ChatColor.RED + "Go to destination.");
        sender.sendMessage(ChatColor.BLUE + "Visit www.spigotmc.org/resources/bungeeportals.19 for help.");
	}

    private void reloadConfig(CommandSender sender) {
        plugin.loadConfigFiles();
        plugin.loadPortalsData();
        sender.sendMessage(ChatColor.GREEN + "All configuration files and data have been reloaded.");
	}
    
    private void clearSelection(CommandSender sender) {
        if (sender instanceof Player) {
            String playerName = sender.getName();
            if (selections.containsKey(playerName)) {
                selections.remove(playerName);
                sender.sendMessage(ChatColor.GREEN + "Selection cleared.");
            } else {
                sender.sendMessage(ChatColor.RED + "You haven't selected anything.");
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
        }
    }
    
    private void storeSelection(CommandSender sender) {
    	Player player;
    	String playerName;
    	
        if (sender instanceof Player) {
            player = (Player) sender;
            playerName = player.getName();
        }
        else { 
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
        	return;
        }
        Selection selection = plugin.worldEdit.getSelection(player);
        if (selection == null || !(selection instanceof CuboidSelection)) {
            sender.sendMessage(ChatColor.RED + "You have to first create a WorldEdit cuboid selection!");
            return;
        }

        List<Location> locations = getLocationsFromCuboid((CuboidSelection) selection);
        List<String> blocks = new ArrayList<>();
        int count = 0;
        for (Location location : locations) {
            Block block = player.getWorld().getBlockAt(location);
            if (!block.getType().isSolid()) {
                blocks.add(block.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ()));
                count++;
            }
        }
        selections.put(playerName, blocks);
        sender.sendMessage(ChatColor.GREEN + String.valueOf(count) + " blocks have been selected.");
        sender.sendMessage(ChatColor.GREEN + "Use the selection in the create and remove commands.");
    }
    
	private List<Location> getLocationsFromCuboid(CuboidSelection cuboid) {
        List<Location> locations = new ArrayList<>();
        Location minLocation = cuboid.getMinimumPoint();
        Location maxLocation = cuboid.getMaximumPoint();
        for (int i1 = minLocation.getBlockX(); i1 <= maxLocation.getBlockX(); i1++) {
            for (int i2 = minLocation.getBlockY(); i2 <= maxLocation.getBlockY(); i2++) {
                for (int i3 = minLocation.getBlockZ(); i3 <= maxLocation.getBlockZ(); i3++) {
                    locations.add(new Location(cuboid.getWorld(), i1, i2, i3));
                }
            }
        }
        return locations;
    }

	private void createDestination(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
            return;
        }
        Player player = (Player)sender;
        Location loc = player.getLocation();
        World world = player.getWorld();
    	String serverName = args[1];
    	String destName = args[2];
        dataStore.addDestination(
    		destName,
    		serverName,
    		world.getName(),
    		loc.getX(),
    		loc.getY(),
    		loc.getZ(),
    		loc.getPitch(),
    		loc.getYaw()
		);	
        sender.sendMessage(ChatColor.GREEN + " portal destination created.");
	}
	
	private void createPortal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
            return;
        }
        	
        String playerName = sender.getName();
        if (!selections.containsKey(playerName)) {
            sender.sendMessage(ChatColor.RED + "You haven't selected anything.");
        	return;
        }
        
        List<String> selection = selections.get(playerName);
        for (String block : selection) {
        	dataStore.addPortalBlock(block, args[1]);
            plugin.portalData.put(block, args[1]);
        }
        sender.sendMessage(ChatColor.GREEN + String.valueOf(selection.size()) + " portals have been created.");
	}
	
	private void removePortal(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
            return;
        }
        	
        String playerName = sender.getName();
        if (!selections.containsKey(playerName)) {
            sender.sendMessage(ChatColor.RED + "You haven't selected anything.");
        	return;
        }

        int count = 0;
        for (String block : selections.get(playerName)) {
            if (plugin.portalData.containsKey(block)) {
            	dataStore.removePortalBlock(block);
                plugin.portalData.remove(block);
                count++;
            }
        }
        sender.sendMessage(ChatColor.GREEN + String.valueOf(count) + " portals have been removed.");
	}

	private void teleportPlayer(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command.");
            return;
        }
    	
		dataStore.handlePlayerTeleport((Player)sender, args[1]);
	}
}
