package me.szumielxd.bestrpgstats.hikari;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.jetbrains.annotations.NotNull;
import com.zaxxer.hikari.HikariConfig;

import me.szumielxd.bestrpgstats.BestrpgStats;

public class MariaDB extends HikariDB {

	public MariaDB(BestrpgStats plugin, DatabaseConfig dbconfig) {
		super(plugin, dbconfig);
	}

	/**
	 * Get default port for this implementation of HikariCP.
	 * 
	 * @return default port
	 */
	@Override
	protected int getDefaultPort() {
		return 3306;
	}

	/**
	 * Modify and setup connection properties.
	 * 
	 * @param properties default properties map
	 */
	@Override
	protected void setupProperties(@NotNull HikariConfig config, @NotNull Map<String, String> properties) {
		properties = new HashMap<>(properties);
		//properties.putIfAbsent("socketTimeout", "30000");
		properties.putIfAbsent("serverTimezone", "UTC");
		properties.forEach((k,v) -> config.addDataSourceProperty(k, v));
	}

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
	@Override
	public void setupDatabase(@NotNull HikariConfig config, @NotNull String address, int port, @NotNull String database, @NotNull String user, @NotNull String password) {
		try {
			DataSource ds = DataSource.class.cast(this.plugin.getInstance().getJarClassLoader().loadClass("me.szumielxd.bestrpgstats.lib.org.mariadb.jdbc.MariaDbDataSource").getConstructor(String.class, Integer.TYPE, String.class).newInstance(address, port, database));
			config.setDataSource(ds);
			//config.addDataSourceProperty("serverName", address);
			//config.addDataSourceProperty("port", port);
			//config.addDataSourceProperty("databaseName", database);
			config.setUsername(user);
			config.setPassword(password);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { throw new RuntimeException(e); }
	}

}
