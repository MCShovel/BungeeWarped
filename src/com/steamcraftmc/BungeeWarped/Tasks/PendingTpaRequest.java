package com.steamcraftmc.BungeeWarped.Tasks;

import org.bukkit.entity.Player;

import com.steamcraftmc.BungeeWarped.Controllers.PlayerController;

public interface PendingTpaRequest {
	public PendingRequestState getState();
	public void Complete(Player player, PlayerController cont, PendingRequestState state);
}
