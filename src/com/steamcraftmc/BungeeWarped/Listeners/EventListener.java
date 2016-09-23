package com.steamcraftmc.BungeeWarped.Listeners;

import java.io.IOException;
import java.util.logging.Level;

import com.steamcraftmc.BungeeWarped.BungeeWarpedBukkitPlugin;
import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;


public class EventListener implements Listener {

    private BungeeWarpedBukkitPlugin plugin;

    public EventListener(BungeeWarpedBukkitPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onGetPlayerSpawnLocation(PlayerSpawnLocationEvent e) {
    	Player player = e.getPlayer();
		plugin.getPlayerController(player).onPlayerSpawn(e);
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
    public void onPlayerDeath(PlayerDeathEvent e) throws IOException {
		if(e.getEntity() instanceof Player) {
        	Player player = (Player) e.getEntity();
        	this.plugin.getPlayerController(player).storeBackLocation();
		}
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
	public void onEntityDamageEvent(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
        	Player player = (Player) event.getEntity();
        	PlayerController ctrl = plugin.getPlayerController(player);
        	
        	boolean isPlayerImmune = false == ctrl.canDamagePlayer();
    		//plugin.log(Level.INFO, player.getName() + " is immune = " + isPlayerImmune); 
        	if (isPlayerImmune) {
        		event.setCancelled(true);
        		plugin.log(Level.FINEST, "Player " + player.getName() + " is immune to damage from: " + event.getCause());
        	}
			
        	if (event instanceof EntityDamageByEntityEvent)
        	{
	        	Entity eattck = ((EntityDamageByEntityEvent)event).getDamager();
	        	
	        	if (eattck instanceof org.bukkit.entity.Arrow) {
	        		ProjectileSource shooter = ((org.bukkit.entity.Arrow)eattck).getShooter();
	        		if (shooter instanceof LivingEntity) {
	        			eattck = (Entity)shooter;
	        		}
	        	}
	        	
	        	
	        	if (eattck != null && eattck instanceof LivingEntity) {
	        		ctrl.onDamageByEntity();
	        	}
				if (eattck != null && eattck instanceof Player) {
					PlayerController attacker = plugin.getPlayerController((Player)eattck);
					ctrl.onCombatTag();
					attacker.onCombatTag();
	
					if (!attacker.canDamagePlayer()) {
		        		event.setCancelled(true);
					}
				}
			}
        }
	}
	
}
