package com.steamcraftmc.BungeeWarped.Tasks;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;

public class BungeePlayerMessage implements Runnable, PluginMessageListener {

	BungeeWarpedBukkitPlugin plugin;
	private Player sender;
	private String target, command;
	private String[] arguments;

	public BungeePlayerMessage(BungeeWarpedBukkitPlugin plugin, Player sender, String target, String command, String[] args) {
		this.plugin = plugin;
		this.sender = sender;
		this.target = target;
		this.command = command;
		this.arguments = new String[args.length];
		System.arraycopy( args, 0, this.arguments, 0, args.length );
	}

	public void start() {
		if (!plugin.config.enableBungeeCord()) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPlayer not found."));
			return;
		}

		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 1);
	}
	
	@Override
	public void run() {
		if (!sender.isOnline()) return;

		plugin.log(Level.INFO, "Sending message: PlayerList");
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerList");
        out.writeUTF("ALL");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!sender.isOnline() || !channel.equals("BungeeCord")) {
			return;
		}

		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		plugin.log(Level.INFO, "Received message on: " + subchannel);
		if (subchannel.equals("PlayerList") && in.readUTF().equalsIgnoreCase("ALL")) {

	        Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(plugin, "BungeeCord", this);

			boolean exact = false;
			String responseText = in.readUTF();
			plugin.log(Level.INFO, "Players online: " + responseText);
			String[] allPlayers = responseText.split(", ");
			ArrayList<String> possible = new ArrayList<>();
			String match = this.target.toLowerCase();
			
			for (int ix=0; ix < allPlayers.length; ix++) {
				String nm = allPlayers[ix];
				int ldif = nm.length() - match.length();
				if (ldif == 0 && nm.equalsIgnoreCase(match)) {
					exact = true;
					break;
				}
				if (ldif > 0 && nm.toLowerCase().startsWith(match)) {
					possible.add(allPlayers[ix]);
				}
			}

			if (!exact && possible.size() != 1) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cPlayer not found."));
				return;
			}
			if (!exact) {
				target = possible.get(0);
			}

			send();
		}
	}

	public void send() {
		plugin.log(Level.FINER, "Sending command: " + command);
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        
        out.writeUTF("ForwardToPlayer");
        out.writeUTF(target);
        out.writeUTF("BungeeWarped");

        ByteArrayDataOutput msg = ByteStreams.newDataOutput();
        
        msg.writeUTF(command);
        msg.writeInt(arguments.length);
        
        for(int ix = 0; ix < arguments.length; ix++) {
        	msg.writeUTF(arguments[ix]);
        }
        
        out.writeShort(msg.toByteArray().length);
        out.write(msg.toByteArray());
        sender.sendPluginMessage(plugin, "BungeeCord", out.toByteArray());
	}
}
