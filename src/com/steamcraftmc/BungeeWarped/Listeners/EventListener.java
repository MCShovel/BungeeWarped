package com.steamcraftmc.BungeeWarped.Listeners;

import java.io.IOException;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class EventListener implements Listener {

    private BungeeWarpedBukkitPlugin plugin;

    public EventListener(BungeeWarpedBukkitPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
		this.plugin.registerPlayerJoined(e.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
		this.plugin.getPlayerController(e.getPlayer()).onPlayerQuit();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) throws IOException {
    	Player player = e.getPlayer();
        Block block = player.getLocation().getBlock();
        String data = player.getWorld().getName() + "#" + String.valueOf(block.getX()) + "#" + String.valueOf(block.getY()) + "#" + String.valueOf(block.getZ());

        PlayerController ctrl = this.plugin.getPlayerController(e.getPlayer());
        String destination = plugin.portalData.get(data);
        
        ctrl.stepIntoPortal(plugin.portalData.containsKey(data), destination);
    }

    @EventHandler
    public void onPlayerPortalEvent(org.bukkit.event.player.PlayerPortalEvent event) {
		Player player = event.getPlayer();
		if (!player.hasPermission("bungeewarped.vanillaportals")) {
			event.setCancelled(true);
		}
    }

	@EventHandler(priority=EventPriority.NORMAL)
	public void onEntityDamageEvent(EntityDamageByEntityEvent event) {
		if(event.getEntity() instanceof Player) {
        	Player player = (Player) event.getEntity();
			Entity eattck = event.getDamager();
			if (eattck != null && eattck instanceof Player) {
				Player attacker = (Player)eattck;

				plugin.getPlayerController(player).onCombatTag();
				plugin.getPlayerController(attacker).onCombatTag();
			}
        }
	}
	
}
