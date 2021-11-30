package me.szumielxd.bestrpgstats;

import java.util.Optional;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.bestrpgstats.BestrpgStatsBootstrap.BestrpgAbstractPlugin;
import me.szumielxd.bestrpgstats.hikari.DatabaseConfig;
import me.szumielxd.bestrpgstats.hikari.HikariDB;
import me.szumielxd.bestrpgstats.hikari.MariaDB;
import me.szumielxd.bestrpgstats.hikari.MysqlDB;
import me.szumielxd.bestrpgstats.hikari.PoolOptions;

public class BestrpgStats extends BestrpgAbstractPlugin {
	
	
	public static final String PREFIX = "§7[§b§lL§3§lP§a§lSync§7] §3";
	
	
	private HikariDB database;
	@Getter private Config configuration;
	@Getter private UsersUpdater usersUpdater;
	//@Getter private DependencyLoader dependencyLoader;
	//@Getter private JarClassLoader jarClassLoader;
	
	
	public BestrpgStats(@NotNull BestrpgStatsBootstrap plugin) {
		super(plugin);
		System.out.println(String.format("####### ClassLoader: %s", getClass().getClassLoader().getClass().getName()));
	}
	
	
	/*private String getPid() {
		RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		String pid = bean.getName();
		if (pid.contains("@")) {
			pid = pid.substring(0, pid.indexOf("@"));
		}
		return pid;
	}

	public void attachAgent() {
		try {
			//Class<?> VirtualMachine = Class.forName("com.sun.tools.attach.VirtualMachine");
			//System.loadLibrary("attach");
			//Object vm = VirtualMachine.getMethod("attach", String.class).invoke(null, this.getPid());
			this.getLogger().info(String.format("############ %s", VirtualMachine.list()));
			VirtualMachine vm = VirtualMachine.attach(VirtualMachine.list().get(0));
			//Properties props = (Properties) VirtualMachine.getMethod("getSystemProperties").invoke(vm);
			Properties props = vm.getSystemProperties();
			//VirtualMachine.getMethod("loadAgent", File.class).invoke(null, new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath());
			new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath();
			vm.loadAgent(new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getAbsolutePath());
			//VirtualMachine.getMethod("detach").invoke(vm);
			vm.detach();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	
	@Override
	public void onEnable() {
		//this.attachAgent();
		//this.dependencyLoader = new DependencyLoader(this);
		//this.jarClassLoader = this.dependencyLoader.load();
		//this.dependencyLoader.load();
		ConfigurationSerialization.registerClass(DatabaseConfig.class);
		ConfigurationSerialization.registerClass(PoolOptions.class);
		this.configuration = new Config(this).init(ConfigKey.values());
		//
		this.getCommand("bestrpgstats").setExecutor(new MainCommand(this));
		//
		this.getLogger().info("Establishing connection with databases...");
		this.database = Optional.of(this.getConfiguration().get(ConfigKey.DATABASES)).map(DatabaseConfig.class::cast)
				.map(cfg -> cfg.getType().equalsIgnoreCase("mariadb")? new MariaDB(this, cfg) : new MysqlDB(this, cfg)).map(HikariDB::setup).get();
		//
		this.usersUpdater = new UsersUpdater(this);
		this.getServer().getScheduler().runTaskTimerAsynchronously(this.getInstance(), this.usersUpdater::updateUsers, 20L, 5*60*20L);
	}
	
	
	@Override
	public void onDisable() {
		ConfigurationSerialization.unregisterClass(DatabaseConfig.class);
		ConfigurationSerialization.unregisterClass(PoolOptions.class);
		this.getServer().getScheduler().cancelTasks(this.getInstance());
		if (this.database != null) this.database.shutdown();
	}
	
	
	public HikariDB getDB() {
		return this.database;
	}
	
	
	
	

}
