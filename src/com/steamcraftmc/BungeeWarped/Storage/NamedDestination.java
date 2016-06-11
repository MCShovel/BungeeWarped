package com.steamcraftmc.BungeeWarped.Storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class NamedDestination {
	public String name;
	public String serverName;
	public String worldName;
	public double X;
	public double Y;
	public double Z;
	public float pitch;
	public float yaw;
	public TeleportReason reason;
	
	public Location toPlayerLocation() {
	    World world = Bukkit.getServer().getWorld(worldName);
	    Location loc = new Location(world, X, Y, Z);
	    loc.setPitch(pitch);
	    loc.setYaw(yaw);
	    return loc;
	}
}
