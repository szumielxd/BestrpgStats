package me.szumielxd.bestrpgstats.listeners;

import java.sql.SQLException;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import me.szumielxd.bestrpgstats.BestrpgStats;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class PlayerListener implements Listener {
	
	
	private BestrpgStats plugin;
	
	
	@EventHandler
	public void onQuit(@NotNull PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin.getInstance(), () -> {
			try {
				this.plugin.getDB().savePlayerData(player);
				this.plugin.getDB().savePlayerItems(player);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	

}
