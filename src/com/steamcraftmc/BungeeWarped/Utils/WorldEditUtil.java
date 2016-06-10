package com.steamcraftmc.BungeeWarped.Utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.selections.CuboidSelection;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;

public class WorldEditUtil {

	private final BungeeWarpedBukkitPlugin plugin;
	public WorldEditUtil (BungeeWarpedBukkitPlugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean hasSelection(Player player) {
		Selection selection = plugin.worldEdit.getSelection(player);
        if (selection == null || !(selection instanceof CuboidSelection)) {
            return false;
        }
        return true;
	}
	
	public Location getMinimumPoint(Player player) {
		Selection selection = plugin.worldEdit.getSelection(player);
        CuboidSelection cuboid = (CuboidSelection)selection;
        return cuboid.getMinimumPoint();
    }
	
	public Location getMaximumPoint(Player player) {
		Selection selection = plugin.worldEdit.getSelection(player);
        CuboidSelection cuboid = (CuboidSelection)selection;
        return cuboid.getMaximumPoint();
    }
}
