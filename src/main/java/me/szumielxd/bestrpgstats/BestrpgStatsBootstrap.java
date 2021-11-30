package me.szumielxd.bestrpgstats;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import lombok.Getter;
import me.szumielxd.bestrpgstats.loader.DependencyLoader;
import me.szumielxd.bestrpgstats.loader.JarClassLoader;

public class BestrpgStatsBootstrap extends JavaPlugin {
	
	
	private BestrpgAbstractPlugin realPlugin;
	@Getter private DependencyLoader dependencyLoader;
	@Getter private JarClassLoader jarClassLoader;
	
	
	@Override
	public void onLoad() {
		this.dependencyLoader = new DependencyLoader(this);
		this.jarClassLoader = this.dependencyLoader.load();
		try {
			Class<?> clazz = this.jarClassLoader.loadClass("me.szumielxd.bestrpgstats.BestrpgStats");
			Class<? extends BestrpgAbstractPlugin> subClazz = clazz.asSubclass(BestrpgAbstractPlugin.class);
			this.realPlugin = subClazz.getConstructor(BestrpgStatsBootstrap.class).newInstance(this);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
		this.realPlugin.onLoad();
	}
	
	
	@Override
	public void onEnable() {
		this.realPlugin.onEnable();
	}
	
	
	@Override
	public void onDisable() {
		this.realPlugin.onDisable();
	}
	
	
	public static abstract class BestrpgAbstractPlugin {
		
		
		@Getter private final @NotNull BestrpgStatsBootstrap instance;
		
		public BestrpgAbstractPlugin(@NotNull BestrpgStatsBootstrap instance) {
			this.instance = instance;
		}
		
		
		public void onLoad() {}
		public void onEnable() {}
		public void onDisable() {}
		
		
		public Logger getLogger() {
			return this.instance.getLogger();
		}
		
		
		public PluginCommand getCommand(String command) {
			return this.instance.getCommand(command);
		}
		
		
		public Server getServer() {
			return this.instance.getServer();
		}
		
		
		public File getDataFolder() {
			return this.instance.getDataFolder();
		}
		
		
	}
	

}
