package com.steamcraftmc.BungeeWarped.Commands;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;
import com.steamcraftmc.BungeeWarped.Storage.TeleportReason;

public class CmdTop extends BaseCommand {

	public CmdTop(BungeeWarpedBukkitPlugin plugin) {
		super(plugin, "bungeewarped.top", "top", 0, 0);
	}

	@Override
	protected boolean doCommand(Player player, PlayerController state, Command cmd, String[] args) {

		PlayerController ctrl = plugin.getPlayerController(player);
		if (!ctrl.verifyCooldownForTp(TeleportReason.TOP, true)) {
			return true;
		}
		
		Location ploc = player.getLocation();
		World world = player.getWorld();
		int x = ploc.getBlockX(),
			y = ploc.getBlockY(),
			z = ploc.getBlockZ();

		int topY = 1 + world.getHighestBlockYAt(x, z);
		int topmost = (world.getMaxHeight() - 1);

		//In the nether?
		if (world.getBiome(x, z) == Biome.HELL) {
			topmost = 127;
			topY = topmost;
		}
		else if (topY == 1) {
			topY = topmost;
		}
		
		if (topY >= topmost) {
			plugin.log(Level.INFO, "top " + topY + " is higher than max of " + topmost);

			boolean valid = false;
			int countAir = 0;
			while (--topY > y + 1) {
				if (world.getBlockAt(x, topY, z).getType() == Material.AIR) {
					countAir++;
				} else {
					if (countAir >= 2) {
						topY++;
						valid = true;
						break;
					}
					countAir = 0;
				}
			}
			
			if (!valid) {
				player.sendMessage(plugin.config.NoValidTop());
				return true;
			}
		}

		boolean isAirFeet = (world.getBlockAt(x, topY, z).getType() == Material.AIR);
		boolean isAirHead = (world.getBlockAt(x, topY + 1, z).getType() == Material.AIR);
		
		if (topY <= y + 0.9 || !isAirFeet || !isAirHead) {
			plugin.log(Level.INFO, "top " + topY + " is under the player at " + y);
			player.sendMessage(plugin.config.NoValidTop());
			return true;
		}
		
		plugin.log(Level.INFO, "Teleporting player top: " + topY);

		ploc.setX(x + 0.5);
		ploc.setY(topY);
		ploc.setZ(z + 0.5);

		NamedDestination dest = NamedDestination.create(plugin, player.getName(), ploc, TeleportReason.TOP);
		state.teleportToDestination(dest);
		return true;
	}
}
