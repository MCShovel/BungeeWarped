package com.steamcraftmc.BungeeWarped.Tasks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;


public class RequestServerName implements Runnable {
	private final BungeeWarpedBukkitPlugin plugin;
	private final Player player;
	public RequestServerName(BungeeWarpedBukkitPlugin plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
	}
	
	@Override
	public void run() {
		if (!player.isOnline()) return;
		plugin.log(Level.INFO, "Sending message: GetServer");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetServer");
        player.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
	
	public void runAfter(int serverTicks) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, serverTicks);
	}
}
