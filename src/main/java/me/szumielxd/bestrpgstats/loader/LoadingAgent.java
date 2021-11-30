package me.szumielxd.bestrpgstats.loader;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.jar.JarFile;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class LoadingAgent {
    private static Instrumentation inst = null;
    
 // The JRE will call method before launching your main()
    public static void premain(final String a, final Instrumentation inst) {
    	LoadingAgent.inst = inst;
    }

    // The JRE will call method before launching your main()
    public static void agentmain(final String a, final Instrumentation inst) {
    	LoadingAgent.inst = inst;
    }

    @SuppressWarnings("resource")
	public static void addClassPath(@NotNull File file) {
    	Objects.requireNonNull(file, "'file' cannot be null.");
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        try {
            // If Java 9 or higher use Instrumentation
            if (!(cl instanceof URLClassLoader)) {
            	Bukkit.getLogger().info(String.format("Loading plugin '%s' (Java 9+)", file.getName()));
                inst.appendToSystemClassLoaderSearch(new JarFile(file)); // it shouldn't be closed
                return;
            }

            // If Java 8 or below fallback to old method
            Bukkit.getLogger().info(String.format("Loading plugin '%s' (Java 8-)", file.getName()));
            Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            m.setAccessible(true);
            m.invoke(cl, file.toURI().toURL());
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException e) { e.printStackTrace(); }
    }

}
