package me.szumielxd.bestrpgstats;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

public class MainCommand implements TabExecutor {
	
	
	private final BungeerpgStats plugin;
	
	
	public MainCommand(BungeerpgStats plugin) {
		this.plugin = plugin;
	}
	

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = new ArrayList<>();
		if (args.length == 1) {
			String arg = args[0].toLowerCase();
			if ("save".startsWith(arg)) list.add("save");
			if ("save-all".startsWith(arg)) list.add("save-all");
		} else if (args.length == 2) {
			if ("save".equalsIgnoreCase(args[0])) return null;
		}
		return list;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("save-all")) {
				sender.sendMessage(BungeerpgStats.PREFIX + "Forcing update of LuckPerms groups data...");
				this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
					this.plugin.getUsersUpdater().updateUsers();
					sender.sendMessage(BungeerpgStats.PREFIX + "§aSuccessfully updated groups data.");
				});
				return true;
			} else if (args[0].equalsIgnoreCase("save")) {
				if (args.length == 2) {
					Player pl = Bukkit.getPlayerExact(args[1]);
					if (pl == null) {
						sender.sendMessage(BungeerpgStats.PREFIX + String.format("§cCannot find player for §4%s§c.", args[1]));
						return true;
					}
					try {
						this.plugin.getDB().savePlayerData(pl);
						sender.sendMessage(BungeerpgStats.PREFIX + String.format("§aSuccessfully updated player data for %s.", pl.getName()));
					} catch (SQLException e) {
						sender.sendMessage(BungeerpgStats.PREFIX + String.format("§4An error occured while attempting to save player data. See console for more informations."));
						e.printStackTrace();
					}
				}
			}
		}
		sender.sendMessage(BungeerpgStats.PREFIX + String.format("Usage: §a/%s save|save-all", label));
		return true;
	}

}
