package com.steamcraftmc.BungeeWarped.Tasks;

public enum PendingRequestState {
	PENDING,
	CANCEL,
	ACCEPT,
	DENY,
	TIMEOUT,
	REPLACED,
	LOGOUT, 
	FORCED
}
