package me.szumielxd.bestrpgstats;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UsersUpdater {
	
	
	private final BestrpgStats plugin;
	
	
	public UsersUpdater(BestrpgStats plugin) {
		this.plugin = plugin;
	}
	
	
	/**
	 * Update user's ranks.
	 */
	public void updateUsers() {
		try {
			Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
			this.plugin.getDB().savePlayerData(players);
			this.plugin.getDB().savePlayerItems(players);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
