package me.szumielxd.bestrpgstats.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.njol.skript.variables.Variables;
import me.clip.placeholderapi.PlaceholderAPI;
import me.szumielxd.bestrpgstats.BungeerpgStats;
import me.szumielxd.bestrpgstats.Config;
import me.szumielxd.bestrpgstats.ConfigKey;

public abstract class HikariDB {
	
	
	protected final BungeerpgStats plugin;
	protected final DatabaseConfig dbconfig;
	protected HikariDataSource hikari;
	
	private final String DB_HOST;
	private final String DB_NAME;
	private final String DB_USER;
	private final String DB_PASSWD;

	
	private static @NotNull String escapeSql(@NotNull String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	
	public HikariDB(BungeerpgStats plugin, DatabaseConfig dbconfig) {
		this.plugin = plugin;
		this.dbconfig = dbconfig;
		
		DB_HOST = dbconfig.getHost();
		DB_NAME = dbconfig.getDatabase();
		DB_USER = dbconfig.getUser();
		DB_PASSWD = dbconfig.getPassword();
		
	}
	
	
	/**
	 * Get default port for this implementation of HikariCP.
	 * 
	 * @return default port
	 */
	protected abstract int getDefaultPort();
	
	
	/**
	 * Setup database connection properties.
	 */
	public HikariDB setup() {
		HikariConfig config = new HikariConfig();
		config.setPoolName("portfel-hikari");
		final String[] host = DB_HOST.split(":");
		int port = this.getDefaultPort();
		if (host.length > 1) {
			try {
				port = Integer.parseInt(host[1]);
			} catch (NumberFormatException e) {}
		}
		this.setupDatabase(config, host[0], port, DB_NAME, DB_USER, DB_PASSWD);
		
		PoolOptions options = this.dbconfig.getPoolOptions();
		Map<String, String> properties = options.getProperties();
		this.setupProperties(config, properties);
		
		config.setMaximumPoolSize(options.getMaxPoolSize());
		config.setMinimumIdle(options.getMinIdle());
		config.setMaxLifetime(options.getMaxLifetime());
		config.setKeepaliveTime(options.getKeepAlive());
		config.setConnectionTimeout(options.getConnTimeout());
		config.setInitializationFailTimeout(-1);
		
		this.hikari = new HikariDataSource(config);
		return this;
	}
	
	/**
	 * Modify and setup connection properties.
	 * 
	 * @param properties default properties map
	 */
	protected abstract void setupProperties(@NotNull HikariConfig config, @NotNull Map<String, String> properties);
	
	/**
	 * Setup database connection.
	 * 
	 * @param config database configuration object
	 * @param address connection's address
	 * @param port connection's port
	 * @param database database name
	 * @param user database user name
	 * @param password database password
	 */
	public abstract void setupDatabase(@NotNull HikariConfig config, @NotNull String address, int port, @NotNull String database, @NotNull String user, @NotNull String password);
	
	/**
	 * Get database connection.
	 * 
	 * @return database connection
	 * @throws SQLException when cannot establish database connection
	 */
	public Connection connect() throws SQLException {
		if (this.hikari == null) throw new SQLException("Unable to get a connection from the pool. (hikari is null)");
		Connection conn = this.hikari.getConnection();
		if (conn == null) throw new SQLException("Unable to get a connection from the pool. (connection is null)");
		return conn;
	}
	
	/**
	 * Check if database is connected.
	 * 
	 * @return true if connection to database is opened
	 */
	public boolean isConnected() {
		return this.isValid() && !this.hikari.isClosed();
	}
	
	/**
	 * Check if database connection is valid.
	 * 
	 * @return true if connection to database is valid
	 */
	public boolean isValid() {
		return this.hikari != null;
	}
	
	/**
	 * Shutdown database
	 */
	public void shutdown() {
		if (this.hikari != null) this.hikari.close();
	}
	
	/**
	 * Save all given player data to database.
	 * 
	 * @implNote Thread unsafe.
	 * @param players players to save
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public void savePlayerData(@NotNull Player... players) throws SQLException {
		this.checkConnection();
		if (players.length == 0) return;
		try (Connection conn = this.connect()) {
			players = Stream.of(players).distinct().toArray(Player[]::new);
			String metaName = "";
			String pluginName = "";
			players[0].getMetadata(metaName).parallelStream().filter(m -> pluginName.equals(m.getOwningPlugin().getName())).map(MetadataValue::asLong).findAny().orElse(null);
			Variables.getVariable(metaName, null, false);
			String sql = "INSERT INTO `rpg_players` (`username`, `uuid`, `last-online`, `class`, `level`, `anihilus`, `strength`, `dexterity`, `luck`, `intelligence`, `strength_item`, `dexterity_item`, `luck_item`, `intelligence_item`, `strength_bonus`, `dexterity_bonus`, `luck_bonus`, `intelligence_bonus`) VALUES %s ON DUPLICATE KEY UPDATE `username` = VALUES(`username`), `last-online` = VALUES(`last-online`), `class` = VALUES(`class`), `level` = VALUES(`level`), `anihilus` = VALUES(`anihilus`), `strength` = VALUES(`strength`), `dexterity` = VALUES(`dexterity`), `luck` = VALUES(`luck`), `intelligence` = VALUES(`intelligence`), `strength_item` = VALUES(`strength_item`), `dexterity_item` = VALUES(`dexterity_item`), `luck_item` = VALUES(`luck_item`), `intelligence_item` = VALUES(`intelligence_item`), `strength_bonus` = VALUES(`strength_bonus`), `dexterity_bonus` = VALUES(`dexterity_bonus`), `luck_bonus` = VALUES(`luck_bonus`), `intelligence_bonus` = VALUES(`intelligence_bonus`)";
			sql = String.format(sql, String.join(", ", Stream.of(players).parallel().map(p -> "(?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").toArray(String[]::new)));
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				int index = 1;
				Config cfg = this.plugin.getConfiguration();
				for (Player player : players) {
					stm.setString(index++, player.getName());
					stm.setString(index++, player.getUniqueId().toString());
					
					stm.setString(index++, getDataStringValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_CLASS), ""));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LEVEL), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_ANIHILUS), 0L));
					
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE), 0L));
					
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_ITEM), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_ITEM), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_ITEM), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_ITEM), 0L));
					
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_BONUS), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_BONUS), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_BONUS), 0L));
					stm.setLong(index++, getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_BONUS), 0L));
				}
				stm.executeLargeUpdate();
			}
		}
	}
	
	
	private static long getDataLongValue(Player player, String accessor, long def) {
		int index = accessor.indexOf('|');
		String type = accessor.substring(0, index);
		accessor = accessor.substring(index+1);
		
		if ("metadata".equalsIgnoreCase(type)) {
			index = accessor.indexOf('|');
			String plugin = accessor.substring(0, index);
			accessor = accessor.substring(index+1).replace("%player%", player.getName());
			return player.getMetadata(accessor).parallelStream().filter(meta -> Optional.ofNullable(meta.getOwningPlugin()).filter(pl -> plugin.equals(pl.getName())).isPresent()).findAny().map(MetadataValue::asLong).orElse(def);
		} else if ("skript".equalsIgnoreCase(type)) {
			Object obj = Variables.getVariable(accessor.replace("%player%", player.getName()), null, false);
			if (obj == null) return def;
			if (obj instanceof Number) return ((Number) obj).longValue();
			if (obj instanceof String) try { return Long.parseLong((String) obj); } catch (NumberFormatException e) {}
			return def;
		} else if ("placeholderapi".equalsIgnoreCase(type)) {
			accessor = accessor.replace("%player%", player.getName());
			final String acc = accessor;
			return Optional.of(PlaceholderAPI.setPlaceholders(player, accessor)).filter(str -> acc.equalsIgnoreCase(str)).map(str -> {
				try { Long.parseLong(str); } catch (NumberFormatException e) {} return (Long) null;
			}).orElse(def);
		}
		return def;
	}
	
	
	private static String getDataStringValue(Player player, String accessor, String def) {
		int index = accessor.indexOf('|');
		String type = accessor.substring(0, index);
		accessor = accessor.substring(index+1);
		
		if ("metadata".equalsIgnoreCase(type)) {
			index = accessor.indexOf('|');
			String plugin = accessor.substring(0, index);
			accessor = accessor.substring(index+1).replace("%player%", player.getName());
			return player.getMetadata(accessor).parallelStream().filter(meta -> Optional.ofNullable(meta.getOwningPlugin()).filter(pl -> plugin.equals(pl.getName())).isPresent()).findAny().map(MetadataValue::asString).orElse(def);
		} else if ("skript".equalsIgnoreCase(type)) {
			Object obj = Variables.getVariable(accessor.replace("%player%", player.getName()), null, false);
			if (obj == null) return def;
			if (obj instanceof String) return (String) obj;
			return obj.toString();
		} else if ("placeholderapi".equalsIgnoreCase(type)) {
			accessor = accessor.replace("%player%", player.getName());
			final String acc = accessor;
			return Optional.of(PlaceholderAPI.setPlaceholders(player, accessor)).filter(str -> acc.equalsIgnoreCase(str)).orElse(def);
		}
		return def;
	}
	
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection() {
		if (!this.isConnected()) this.setup();
	}

}
