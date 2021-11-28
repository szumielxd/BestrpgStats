package me.szumielxd.bestrpgstats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UsersUpdater {
	
	
	private final BungeerpgStats plugin;
	
	
	public UsersUpdater(BungeerpgStats plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Update user's ranks.
	 */
	public void updateUsers() {
		try {
			this.plugin.getDB().savePlayerData(Bukkit.getOnlinePlayers().toArray(new Player[0]));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
