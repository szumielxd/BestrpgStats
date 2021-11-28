package me.szumielxd.bestrpgstats;

import java.util.Optional;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import me.szumielxd.bestrpgstats.hikari.DatabaseConfig;
import me.szumielxd.bestrpgstats.hikari.HikariDB;
import me.szumielxd.bestrpgstats.hikari.MariaDB;
import me.szumielxd.bestrpgstats.hikari.MysqlDB;
import me.szumielxd.bestrpgstats.hikari.PoolOptions;

public class BungeerpgStats extends JavaPlugin {
	
	
	public static final String PREFIX = "§7[§b§lL§3§lP§a§lSync§7] §3";
	
	
	private HikariDB database;
	@Getter private Config configuration;
	@Getter private UsersUpdater usersUpdater;
	
	
	@Override
	public void onEnable() {
		ConfigurationSerialization.registerClass(DatabaseConfig.class);
		ConfigurationSerialization.registerClass(PoolOptions.class);
		this.configuration = new Config(this).init(ConfigKey.values());
		//
		this.getCommand("lpgroupsync").setExecutor(new MainCommand(this));
		//
		this.getLogger().info("Establishing connection with databases...");
		this.database = Optional.of(this.getConfiguration().get(ConfigKey.DATABASES)).map(DatabaseConfig.class::cast)
				.map(cfg -> cfg.getType().equalsIgnoreCase("mariadb")? new MariaDB(this, cfg) : new MysqlDB(this, cfg)).map(HikariDB::setup).get();
		//
		this.usersUpdater = new UsersUpdater(this);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, this.usersUpdater::updateUsers, 20L, 5*60*20L);
	}
	
	
	@Override
	public void onDisable() {
		ConfigurationSerialization.unregisterClass(DatabaseConfig.class);
		ConfigurationSerialization.unregisterClass(PoolOptions.class);
		this.getServer().getScheduler().cancelTasks(this);
		if (this.database != null) this.database.shutdown();
	}
	
	
	public HikariDB getDB() {
		return this.database;
	}
	
	
	
	

}
