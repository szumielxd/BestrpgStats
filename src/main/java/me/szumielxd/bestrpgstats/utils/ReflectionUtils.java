package me.szumielxd.bestrpgstats.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ReflectionUtils {
	
	
	public static final String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
	public static final Class<?> CraftItemStack = getCraftClass("inventory.CraftItemStack");
	public static final Method CraftItemStack_asNMSCopy = getMethod(CraftItemStack, "asNMSCopy", ItemStack.class);
	public static final Class<?> NBTTagCompound = getNMSClass("NBTTagCompound");
	public static final Class<?> NBTTagList = getNMSClass("NBTTagList");
	
	
	public static Class<?> getCraftClass(String craftPath) {
		try {
			return Class.forName(String.format("org.bukkit.craftbukkit.%s.%s", VERSION, craftPath));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static Class<?> getNMSClass(String craftPath) {
		try {
			return Class.forName(String.format("net.minecraft.server.%s.%s", VERSION, craftPath));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static Method getMethod(Class<?> clazz, String methodName, Class<?>... args) {
		try {
			return clazz.getMethod(methodName, args);
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static Object getFieldValue(Object obj, String fieldName) {
		try {
			Field f = obj.getClass().getDeclaredField(fieldName);
			f.setAccessible(true);
			return f.get(obj);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
			if (e instanceof NoSuchFieldException) {
				new RuntimeException(String.format("Cannot find field `%s` in class `%s`", fieldName, obj.getClass()), e).printStackTrace();
			} else {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public static Object invokeMethod(Class<?> clazz, Object obj, String methodName, Object... args) {
		try {
			Class<?>[] arr = Stream.of(args).map(Object::getClass).toArray(Class[]::new);
			return clazz.getMethod(methodName, arr).invoke(obj, args);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}
	

}
