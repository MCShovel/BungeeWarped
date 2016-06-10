package com.steamcraftmc.BungeeWarped.Storage;

public class PlayerState {	
	public long joinTime;
	public long lastCombat;

	public PlayerState() {
		joinTime = System.currentTimeMillis();
	}
}
