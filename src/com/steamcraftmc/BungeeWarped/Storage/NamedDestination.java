package com.steamcraftmc.BungeeWarped.Storage;

import java.io.StringReader;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;

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
	
	public static NamedDestination create(BungeeWarpedBukkitPlugin plugin, String name, Location loc, TeleportReason reason) {
		NamedDestination dest = new NamedDestination();
		dest.name = name;
		dest.serverName = plugin.getServerName();
		dest.worldName = loc.getWorld().getName();
		dest.X = loc.getX();
		dest.Y = loc.getY();
		dest.Z = loc.getZ();
		dest.pitch = loc.getPitch();
		dest.yaw = loc.getYaw();
		dest.reason = reason;
		return dest;
	}

    public String toString() { 
    	StringBuilder sb = new StringBuilder();
    	sb.append(name);
    	sb.append('\n');
    	sb.append(serverName);
    	sb.append('\n');
    	sb.append(worldName);
    	sb.append('\n');
    	sb.append(X);
    	sb.append('\n');
    	sb.append(Y);
    	sb.append('\n');
    	sb.append(Z);
    	sb.append('\n');
    	sb.append(pitch);
    	sb.append('\n');
    	sb.append(yaw);
    	sb.append('\n');
    	sb.append(reason);
    	return sb.toString();
    }
    
    public static NamedDestination fromString(String data) {
		NamedDestination dest = new NamedDestination();
		int ix = -1;
		String[] lines = data.split("\n");
    	dest.name = lines[++ix];
    	dest.serverName = lines[++ix];
    	dest.worldName = lines[++ix];
    	dest.X = Double.parseDouble(lines[++ix]);
    	dest.Y = Double.parseDouble(lines[++ix]);
    	dest.Z = Double.parseDouble(lines[++ix]);
    	dest.pitch = Float.parseFloat(lines[++ix]);
    	dest.yaw = Float.parseFloat(lines[++ix]);
    	dest.reason = TeleportReason.valueOf(lines[++ix]);
    	return dest;
    }
}
