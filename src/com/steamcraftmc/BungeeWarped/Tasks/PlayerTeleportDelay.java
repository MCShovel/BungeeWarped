package com.steamcraftmc.BungeeWarped.Tasks;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;
import com.steamcraftmc.BungeeWarped.Storage.NamedDestination;

public class PlayerTeleportDelay implements Runnable {
		
	private BungeeWarpedBukkitPlugin plugin;
	private PlayerController playerCtrl;
	private NamedDestination destination;
	private int delay;
	private boolean effects;
	private int effectStrength;
	private int taskId;
	private Block startedAt;
	private int movementAllowed;
	private final long startTime;
	private boolean startmsg;

	public PlayerTeleportDelay(BungeeWarpedBukkitPlugin plugin, PlayerController ctrl, NamedDestination dest) {
		this.plugin = plugin;
		this.playerCtrl = ctrl;
		this.destination = dest;
		this.startedAt = ctrl.player.getLocation().getBlock();
		this.movementAllowed = plugin.config.getTpMovementAllowed(dest.reason);
		this.delay = plugin.config.getTpWarmupTime(dest.reason);
		this.effects = plugin.config.getTpEffects(dest.reason);
		this.effectStrength = 1;
		this.startTime = System.currentTimeMillis();
		
		if (ctrl.player.hasPermission("bungeewarped.bypass.*")
			|| ctrl.player.hasPermission("bungeewarped.bypass." + dest.reason.toString().toLowerCase())) {
			this.delay = 0;
			this.effects = false;
		}
	}

	public void start() {
		if (!playerCtrl.verifyCooldownForTp(this.destination.reason, true)) {
			return;
		}

		if (delay > 0) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 20, 20);
		}
		else {
			playerCtrl.teleportToDestinationNow(destination);
		}
	}

	@Override
	public void run() {
		if (!playerCtrl.player.isOnline()) {
			Bukkit.getScheduler().cancelTask(taskId);
			return;
		}
		
		if (playerCtrl.lastDamageTime > this.startTime || playerCtrl.lastCombat > this.startTime) {
			Bukkit.getScheduler().cancelTask(taskId);
			playerCtrl.player.sendMessage(plugin.config.TeleportCancelled());
			return;
		}
		if (this.movementAllowed >= 0) {
			Block curr = playerCtrl.player.getLocation().getBlock();
			int dist = Math.max(
					Math.abs(curr.getX() - startedAt.getX()),
					Math.max(
						Math.abs(curr.getY() - startedAt.getY()),
						Math.abs(curr.getZ() - startedAt.getZ())
						)
					);
			if (dist > movementAllowed) {
				Bukkit.getScheduler().cancelTask(taskId);
				if (effects) 
					playerCtrl.player.removePotionEffect(PotionEffectType.CONFUSION);

				playerCtrl.player.sendMessage(plugin.config.TeleportCancelled());
				return;
			}
		}
		
		if (--delay <= 0) {
			Bukkit.getScheduler().cancelTask(taskId);
			if (effects) 
				playerCtrl.player.removePotionEffect(PotionEffectType.CONFUSION);
			playerCtrl.teleportToDestinationNow(destination);
			return;
		}

		if (delay <= 5) {
			playerCtrl.player.sendMessage(plugin.config.TeleportingAfterDelay(delay));

			if (effects) {
				effectStrength = Math.min(8, Math.max(++effectStrength, 5));
				PotionEffect effect = new PotionEffect(
		            PotionEffectType.CONFUSION,
		            (delay * 20) + 120,
		            effectStrength,
		            true, 
		            true);
	
				effect.apply(playerCtrl.player);
			}
		}
		else if (effects && delay <= 10) {
			PotionEffect effect = new PotionEffect(
	            PotionEffectType.CONFUSION,
	            (delay * 20) + 120,
	            1,
	            true, 
	            true);

			effect.apply(playerCtrl.player);
		}
		else if (!startmsg) {
			startmsg = true;
			playerCtrl.player.sendMessage(plugin.config.TeleportingAfterDelay(delay + 1));
		}
	}
}
