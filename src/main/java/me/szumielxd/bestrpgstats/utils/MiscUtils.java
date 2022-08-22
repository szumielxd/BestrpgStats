package me.szumielxd.bestrpgstats.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kitteh.vanish.VanishPlugin;
import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import ch.njol.skript.variables.Variables;
import lombok.experimental.UtilityClass;
import me.clip.placeholderapi.PlaceholderAPI;
import me.szumielxd.bestrpgstats.BestrpgStats;
import me.szumielxd.bestrpgstats.ConfigKey;

@UtilityClass
public class MiscUtils {
	
	
	private static Pattern TALISMAN_PATTERN;
	
	
	public static void init(@NotNull BestrpgStats plugin) {
		Objects.requireNonNull(plugin, "plugin cannot be null");
		TALISMAN_PATTERN = Pattern.compile(plugin.getConfiguration().getString(ConfigKey.PLAYER_ITEM_TALISMAN_NAME_REGEX));
	}
	
	
	public static long getDataLongValue(@NotNull Player player, @NotNull String accessor, long def) {
		Objects.requireNonNull(player, "player cannot be null");
		Objects.requireNonNull(accessor, "accessor cannot be null");
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
			if (obj instanceof String) try {
				return Long.parseLong((String) obj);
			} catch (NumberFormatException e) {
				// skipping
			}
			return def;
		} else if ("placeholderapi".equalsIgnoreCase(type)) {
			accessor = accessor.replace("%player%", player.getName());
			final String acc = "%"+accessor+"%";
			return Optional.of(PlaceholderAPI.setPlaceholders(player, acc)).filter(str -> !acc.equals(str)).map(str -> {
				try {
					return Long.valueOf(str);
				} catch (NumberFormatException e) {
					return null;
				}
			}).orElse(def);
		}
		return def;
	}
	
	public static @Nullable String getDataStringValue(@NotNull Player player, @NotNull String accessor, @Nullable String def) {
		Objects.requireNonNull(player, "player cannot be null");
		Objects.requireNonNull(accessor, "accessor cannot be null");
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
			final String acc = "%"+accessor+"%";
			return Optional.of(PlaceholderAPI.setPlaceholders(player, acc)).filter(acc::equalsIgnoreCase).orElse(def);
		}
		return def;
	}
	
	public static int getDataIntValue(@NotNull Player player, @NotNull String accessor, int def) {
		Objects.requireNonNull(player, "player cannot be null");
		Objects.requireNonNull(accessor, "accessor cannot be null");
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
			if (obj instanceof String) try {
				return Integer.parseInt((String) obj);
			} catch (NumberFormatException e) {
				// skipping
			}
			return def;
		} else if ("placeholderapi".equalsIgnoreCase(type)) {
			accessor = accessor.replace("%player%", player.getName());
			final String acc = "%"+accessor+"%";
			return Optional.of(PlaceholderAPI.setPlaceholders(player, acc)).filter(str -> !acc.equals(str)).map(str -> {
				try { 
					Integer.valueOf(str);
				} catch (NumberFormatException e) {
					// defaulting to def
				} return def;
			}).orElse(def);
		}
		return def;
	}
	
	
	public static boolean isTalisman(@Nullable ItemStack item) {
		if (item == null) return false;
		if (!item.hasItemMeta()) return false;
		if (!item.getItemMeta().hasDisplayName()) return false;
		return TALISMAN_PATTERN.matcher(item.getItemMeta().getDisplayName()).matches();
	}
	
	
	public static boolean isVanished(@NotNull Player player) {
		Objects.requireNonNull("player cannot be null");
		try {
			Class.forName("com.earth2me.essentials.Essentials");
			Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
			User user = ess.getUser(player);
			if (user != null && user.isVanished()) return true;
		} catch (ClassNotFoundException e) {
			// Essentials not found
		}
		try {
			Class.forName("org.kitteh.vanish.VanishPlugin");
			VanishPlugin vnp = (VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket");
			if (vnp.getManager().isVanished(player)) return true;
		} catch (ClassNotFoundException e) {
			// VanishNoPacket not found
		}
		return false;
	}
	
	
	public static boolean isNotVanished(@NotNull Player player) {
		return !isVanished(player);
	}
	

}
