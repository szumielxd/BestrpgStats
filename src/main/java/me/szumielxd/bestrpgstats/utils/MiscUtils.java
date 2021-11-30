package me.szumielxd.bestrpgstats.utils;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import ch.njol.skript.variables.Variables;
import me.clip.placeholderapi.PlaceholderAPI;

public class MiscUtils {
	
	
	public static long getDataLongValue(Player player, String accessor, long def) {
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
			return Optional.of(PlaceholderAPI.setPlaceholders(player, accessor)).filter(str -> !acc.equals(str)).map(str -> {
				try { Long.parseLong(str); } catch (NumberFormatException e) {} return (Long) null;
			}).orElse(def);
		}
		return def;
	}
	
	public static String getDataStringValue(Player player, String accessor, String def) {
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
	
	public static int getDataIntValue(Player player, String accessor, int def) {
		int index = accessor.indexOf('|');
		String type = accessor.substring(0, index);
		accessor = accessor.substring(index+1);
		
		if ("metadata".equalsIgnoreCase(type)) {
			index = accessor.indexOf('|');
			String plugin = accessor.substring(0, index);
			accessor = accessor.substring(index+1).replace("%player%", player.getName());
			return player.getMetadata(accessor).parallelStream().filter(meta -> Optional.ofNullable(meta.getOwningPlugin()).filter(pl -> plugin.equals(pl.getName())).isPresent()).findAny().map(MetadataValue::asInt).orElse(def);
		} else if ("skript".equalsIgnoreCase(type)) {
			Object obj = Variables.getVariable(accessor.replace("%player%", player.getName()), null, false);
			if (obj == null) return def;
			if (obj instanceof Number) return ((Number) obj).intValue();
			if (obj instanceof String) try { return Integer.parseInt((String) obj); } catch (NumberFormatException e) {}
			return def;
		} else if ("placeholderapi".equalsIgnoreCase(type)) {
			accessor = accessor.replace("%player%", player.getName());
			final String acc = accessor;
			return Optional.of(PlaceholderAPI.setPlaceholders(player, accessor)).filter(str -> !acc.equals(str)).map(str -> {
				try { Integer.parseInt(str); } catch (NumberFormatException e) {} return (Integer) null;
			}).orElse(def);
		}
		return def;
	}
	

}
