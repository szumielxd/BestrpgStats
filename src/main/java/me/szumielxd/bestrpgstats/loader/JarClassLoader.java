package me.szumielxd.bestrpgstats.loader;

import java.net.URL;
import java.net.URLClassLoader;

import org.jetbrains.annotations.NotNull;

public class JarClassLoader extends URLClassLoader {
	
	static {
		ClassLoader.registerAsParallelCapable();
	}

	public JarClassLoader(@NotNull URL[] urls, @NotNull ClassLoader parent) {
		super(urls, parent);
	}

}
