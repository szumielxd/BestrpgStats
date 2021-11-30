package me.szumielxd.bestrpgstats.hikari;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.njol.skript.variables.Variables;
import me.szumielxd.bestrpgstats.BestrpgStats;
import me.szumielxd.bestrpgstats.Config;
import me.szumielxd.bestrpgstats.ConfigKey;
import me.szumielxd.bestrpgstats.utils.MiscUtils;
import me.szumielxd.bestrpgstats.utils.NBTUtils;

public abstract class HikariDB {
	
	
	private static Gson GSON_OUT_PARSER = new GsonBuilder().disableHtmlEscaping().disableInnerClassSerialization().create();
	
	
	protected final BestrpgStats plugin;
	protected final DatabaseConfig dbconfig;
	protected HikariDataSource hikari;
	
	private final String DB_HOST;
	private final String DB_NAME;
	private final String DB_USER;
	private final String DB_PASSWD;

	
	private static @NotNull String escapeSql(@NotNull String text) {
		return text.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
	}
	
	
	public HikariDB(BestrpgStats plugin, DatabaseConfig dbconfig) {
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
					
					stm.setString(index++, MiscUtils.getDataStringValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_CLASS), ""));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LEVEL), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_ANIHILUS), 0L));
					
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE), 0L));
					
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_ITEM), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_ITEM), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_ITEM), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_ITEM), 0L));
					
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_BONUS), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_BONUS), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_BONUS), 0L));
					stm.setLong(index++, MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_BONUS), 0L));
				}
				stm.executeUpdate();
			}
		}
	}
	
	
	/**
	 * Save all given player data to database.
	 * 
	 * @implNote Thread unsafe.
	 * @param players players to save
	 * @throws SQLException when cannot establish the connection to the database
	 */
	public void savePlayerItems(@NotNull Player... players) throws SQLException {
		this.checkConnection();
		if (players.length == 0) return;
		try (Connection conn = this.connect()) {
			players = Stream.of(players).distinct().toArray(Player[]::new);
			String metaName = "";
			String pluginName = "";
			players[0].getMetadata(metaName).parallelStream().filter(m -> pluginName.equals(m.getOwningPlugin().getName())).map(MetadataValue::asLong).findAny().orElse(null);
			Variables.getVariable(metaName, null, false);
			String sql = "INSERT INTO `rpg_items` (`uuid`, `max-mana`, `max-hp`, `main-hand`, `off-hand`, `hotbar_0`, `hotbar_1`, `hotbar_2`, `hotbar_3`, `hotbar_4`, `hotbar_5`, `hotbar_6`, `hotbar_7`, `hotbar_8`, `armor_0`, `armor_1`, `armor_2`, `armor_3`, `talizman_0`, `talizman_1`, `talizman_2`) VALUES %s ON DUPLICATE KEY UPDATE `max-mana` = VALUES(`max-mana`), `max-hp` = VALUES(`max-hp`), `main-hand` = VALUES(`main-hand`), `off-hand` = VALUES(`off-hand`), `hotbar_0` = VALUES(`hotbar_0`), `hotbar_1` = VALUES(`hotbar_1`), `hotbar_2` = VALUES(`hotbar_2`), `hotbar_3` = VALUES(`hotbar_3`), `hotbar_4` = VALUES(`hotbar_4`), `hotbar_5` = VALUES(`hotbar_5`), `hotbar_6` = VALUES(`hotbar_6`), `hotbar_7` = VALUES(`hotbar_7`), `hotbar_8` = VALUES(`hotbar_8`), `armor_0` = VALUES(`armor_0`), `armor_1` = VALUES(`armor_1`), `armor_2` = VALUES(`armor_2`), `armor_3` = VALUES(`armor_3`), `talizman_0` = VALUES(`talizman_0`), `talizman_1` = VALUES(`talizman_1`), `talizman_2` = VALUES(`talizman_2`)";
			sql = String.format(sql, String.join(", ", Stream.of(players).parallel().map(p -> "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)").toArray(String[]::new)));
			try (PreparedStatement stm = conn.prepareStatement(sql)) {
				int index = 1;
				Config cfg = this.plugin.getConfiguration();
				for (Player player : players) {
					stm.setString(index++, player.getUniqueId().toString());
					
					stm.setInt(index++, MiscUtils.getDataIntValue(player, cfg.getString(ConfigKey.PLAYER_ITEM_VARIABLE_MAX_MANA), 0));
					stm.setLong(index++, (long) player.getHealthScale());
					stm.setInt(index++, player.getInventory().getHeldItemSlot());
					
					PlayerInventory inv = player.getInventory();
					// off-hand
					stm.setString(index++, GSON_OUT_PARSER.toJson(Optional.ofNullable(inv.getItemInOffHand()).filter(is -> !Material.AIR.equals(is.getType())).map(NBTUtils::getItemAsJson).orElseGet(JsonObject::new)));
					// hotbar
					for (int i = 0; i < 9; i++) {
						stm.setString(index++, GSON_OUT_PARSER.toJson(Optional.ofNullable(inv.getItem(i)).filter(is -> !Material.AIR.equals(is.getType())).map(NBTUtils::getItemAsJson).orElseGet(JsonObject::new)));
					}
					// armor
					ItemStack[] armor = inv.getArmorContents();
					for (int i = 0; i < 4; i++) {
						stm.setString(index++, GSON_OUT_PARSER.toJson(Optional.ofNullable(armor[i]).filter(is -> !Material.AIR.equals(is.getType())).map(NBTUtils::getItemAsJson).orElseGet(JsonObject::new)));
					}
					// talisman
					for (int i = 0; i < 3; i++) {
						stm.setString(index++, GSON_OUT_PARSER.toJson(Optional.ofNullable(inv.getItem(17+(i*9))).filter(is -> MiscUtils.isTalisman(is)).filter(is -> !Material.AIR.equals(is.getType())).map(NBTUtils::getItemAsJson).orElseGet(JsonObject::new)));
					}
					
				}
				stm.executeUpdate();
			}
		}
	}
	
	
	/**
	 * Check if connection can be obtained, otherwise creates new one.
	 */
	public void checkConnection() {
		if (!this.isConnected()) this.setup();
	}

}
