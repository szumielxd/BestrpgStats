package me.szumielxd.bestrpgstats.listeners;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.szumielxd.bestrpgstats.BestrpgStats;
import me.szumielxd.bestrpgstats.Config;
import me.szumielxd.bestrpgstats.ConfigKey;
import me.szumielxd.bestrpgstats.utils.MiscUtils;
import me.szumielxd.bestrpgstats.utils.NBTUtils;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public class PortListener {
	
	
	private static Gson GSON_IN_PARSER = new GsonBuilder().disableHtmlEscaping().generateNonExecutableJson().disableInnerClassSerialization().create();
	private static Gson GSON_OUT_PARSER = new GsonBuilder().disableHtmlEscaping().disableInnerClassSerialization().create();
	
	
	private ServerSocket server = null;
	
	private final BestrpgStats plugin;
	
	
	private void debug(@NotNull String format, @NotNull Object... args) {
		if (this.plugin.getConfiguration().getBoolean(ConfigKey.SERVER_QUERY_DEBUG)) this.plugin.getLogger().info(String.format(format, args));
	}
	
	
	public void startPortListener() {
		final int port = this.plugin.getConfiguration().getInt(ConfigKey.SERVER_QUERY_LISTENED_PORT);
		
		new Thread("BestrpgStats-PortListener") {
			
			@Override
			public void run() {
				try {
					debug("Starting port listener task...");
					PortListener.this.server = new ServerSocket(port);
					debug("Successfully started port listener on %s:%d.", PortListener.this.server.getInetAddress().getHostAddress(), PortListener.this.server.getLocalPort());
					while (PortListener.this.server != null) {
						Socket socket = null;
						try {
							socket = PortListener.this.server.accept();
						} catch (SocketException ex) {
							break;
						}
						new ClientHandler(socket).start();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				debug("Stopped port listener task...");
			}
			
		}.start();
	}
	
	
	public void stopPortListener() {
		if (this.server != null && !this.server.isClosed()) {
			try {
				debug("Stopping port listener...");
				this.server.close();
				this.server = null;
				debug("Stopped port listener.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public class ClientHandler extends Thread {
		
		
		private final Socket client;
		private PrintWriter out;
		private InputStream in;
		
		
		public ClientHandler(Socket client) {
			super("BestrpgStats-ClientHandler");
			this.client = client;
			debug("[%s] Received connection from %s:%d", this.client, this.client.getInetAddress().getHostAddress(), this.client.getPort());
		}
		
		
		public void run() {
			try {
				this.client.setSoTimeout(10);
				this.out = new PrintWriter(this.client.getOutputStream());
				this.in = new DataInputStream(this.client.getInputStream());
				StringBuilder sb = new StringBuilder();
				try {
					for (int ch = in.read(); ch >= 0; ch = in.read()) {
						sb.append((char) ch);
					}
				} catch (SocketTimeoutException ex) {}
				String text = sb.toString();
				debug("[%s] Received message(%d): %s", this.client, text.length(), text);
				if (text.length() > 9999) return;
				JsonObject json = GSON_IN_PARSER.fromJson(text, JsonObject.class);
				if (!json.has("key")) return;
				if (!plugin.getConfiguration().getString(ConfigKey.SERVER_QUERY_SECRET).equals(json.get("key").getAsString())) return;
				debug("[%s] Password accepted", this.client);
				if (json.has("action")) {
				
					String action = json.get("action").getAsString().toUpperCase();
					
					// get_full_stats
					if ("GET_FULL_STATS".equals(action)) {
						if (json.has("data") && json.get("data").isJsonObject()) {
							JsonObject data = json.get("data").getAsJsonObject();
							debug("[%s] Recognised action: %s", this.client, "GET_FULL_STATS");
							Player player = Optional.of(data).map(d -> d.get("username")).filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).map(str -> {
								try {
									return plugin.getServer().getPlayer(UUID.fromString(str));
								} catch (IllegalArgumentException e) {
									return plugin.getServer().getPlayerExact(str);
								}
							}).filter(MiscUtils::isNotVanished).orElse(null);
							if (player != null) {
								debug("[%s] Recognised user: %s", this.client, player.getName());
								JsonObject res = new JsonObject();
								JsonObject stats = new JsonObject();
								res.add("stats", stats);
								res.addProperty("action", "GET_FULL_STATS");
								Config cfg = plugin.getConfiguration();
								res.addProperty("username", player.getName());
								res.addProperty("uuid", player.getUniqueId().toString());
								stats.addProperty("class", MiscUtils.getDataStringValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_CLASS), ""));
								stats.addProperty("level", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LEVEL), 0L));
								stats.addProperty("anihilus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_ANIHILUS), 0L));
								
								stats.addProperty("strength", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH), 0L));
								stats.addProperty("dexterity", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY), 0L));
								stats.addProperty("luck", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK), 0L));
								stats.addProperty("intelligence", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE), 0L));
								
								stats.addProperty("strength_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_ITEM), 0L));
								stats.addProperty("dexterity_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_ITEM), 0L));
								stats.addProperty("luck_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_ITEM), 0L));
								stats.addProperty("intelligence_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_ITEM), 0L));
								
								stats.addProperty("strength_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_BONUS), 0L));
								stats.addProperty("dexterity_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_BONUS), 0L));
								stats.addProperty("luck_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_BONUS), 0L));
								stats.addProperty("intelligence_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_BONUS), 0L));
								
								res.addProperty("maxMana", MiscUtils.getDataIntValue(player, cfg.getString(ConfigKey.PLAYER_ITEM_VARIABLE_MAX_MANA), 0));
								res.addProperty("mana", MiscUtils.getDataIntValue(player, cfg.getString(ConfigKey.PLAYER_ITEM_VARIABLE_MANA), 0));
								res.addProperty("maxHealth", (long) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
								res.addProperty("health", (long) Math.min(player.getHealth(), res.get("maxHealth").getAsDouble()));
								res.addProperty("mainHand", player.getInventory().getHeldItemSlot());
								
								PlayerInventory inv = player.getInventory();
								
								res.add("offHand", Optional.ofNullable(inv.getItemInOffHand()).filter(it -> !Material.AIR.equals(it.getType())).map(NBTUtils::getItemAsJson).orElseGet(JsonObject::new));
								
								JsonArray hotbar = new JsonArray();
								IntStream.range(0, 9).mapToObj(i -> Optional.ofNullable(inv.getItem(i)).filter(it -> !Material.AIR.equals(it.getType())).orElse(null)).map(NBTUtils::getItemAsJson).forEach(hotbar::add);
								res.add("hotbar", hotbar);
								
								JsonArray armor = new JsonArray();
								Lists.reverse(Arrays.asList(inv.getArmorContents())).stream().map(i -> Optional.ofNullable(i).filter(it -> !Material.AIR.equals(it.getType())).orElse(null)).map(NBTUtils::getItemAsJson).forEach(armor::add);
								res.add("armor", armor);
								
								JsonArray talismans = new JsonArray();
								IntStream.of(17, 26, 35).mapToObj(i -> Optional.ofNullable(inv.getItem(i)).filter(it -> !Material.AIR.equals(it.getType())).filter(MiscUtils::isTalisman).orElse(null)).map(NBTUtils::getItemAsJson).forEach(talismans::add);
								res.add("talismans", talismans);
								
								String result = GSON_OUT_PARSER.toJson(res);
								debug("[%s] Sent result: %s", this.client, result);
								this.out.write(result);
							}
						}
					}
					
					// get_stats
					if ("GET_STATS".equals(action)) {
						if (json.has("data") && json.get("data").isJsonObject()) {
							JsonObject data = json.get("data").getAsJsonObject();
							debug("[%s] Recognised action: %s", this.client, "GET_STATS");
							Player player = Optional.of(data).map(d -> d.get("username")).filter(JsonElement::isJsonPrimitive).map(JsonElement::getAsString).map(str -> {
								try {
									return plugin.getServer().getPlayer(UUID.fromString(str));
								} catch (IllegalArgumentException e) {
									return plugin.getServer().getPlayerExact(str);
								}
							}).filter(MiscUtils::isNotVanished).orElse(null);
							if (player != null) {
								debug("[%s] Recognised user: %s", this.client, player.getName());
								JsonObject res = new JsonObject();
								JsonObject stats = new JsonObject();
								res.add("stats", stats);
								res.addProperty("action", "GET_FULL_STATS");
								Config cfg = plugin.getConfiguration();
								res.addProperty("username", player.getName());
								res.addProperty("uuid", player.getUniqueId().toString());
								stats.addProperty("class", MiscUtils.getDataStringValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_CLASS), ""));
								stats.addProperty("level", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LEVEL), 0L));
								stats.addProperty("anihilus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_ANIHILUS), 0L));
								
								stats.addProperty("strength", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH), 0L));
								stats.addProperty("dexterity", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY), 0L));
								stats.addProperty("luck", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK), 0L));
								stats.addProperty("intelligence", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE), 0L));
								
								stats.addProperty("strength_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_ITEM), 0L));
								stats.addProperty("dexterity_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_ITEM), 0L));
								stats.addProperty("luck_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_ITEM), 0L));
								stats.addProperty("intelligence_item", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_ITEM), 0L));
								
								stats.addProperty("strength_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_STRENGTH_BONUS), 0L));
								stats.addProperty("dexterity_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_DEXTERITY_BONUS), 0L));
								stats.addProperty("luck_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_LUCK_BONUS), 0L));
								stats.addProperty("intelligence_bonus", MiscUtils.getDataLongValue(player, cfg.getString(ConfigKey.PLAYER_DATA_VARIABLE_INTELLIGENCE_BONUS), 0L));
								
								String result = GSON_OUT_PARSER.toJson(res);
								debug("[%s] Sent result: %s", this.client, result);
								this.out.write(result);
							}
						}
					}
					// get_online_players
					if ("GET_ONLINE_PLAYERS".equals(action)) {
						debug("[%s] Recognised action: %s", this.client, "GET_ONLINE_PLAYERS");
						JsonObject res = new JsonObject();
						JsonArray onlinePlayers = new JsonArray();
						res.add("onlinePlayers", onlinePlayers);
						res.addProperty("action", "GET_ONLINE_PLAYERS");
						Bukkit.getOnlinePlayers().parallelStream().filter(MiscUtils::isNotVanished).map(Player::getName).map(JsonPrimitive::new).forEach(onlinePlayers::add);
						String result = GSON_OUT_PARSER.toJson(res);
						debug("[%s] Sent result: %s", this.client, result);
						this.out.write(result);
					}
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				this.out.flush();
				try {
					this.in.close();
					this.out.close();
					this.client.close();
				} catch (IOException e) {}
				debug("[%s] closing...", this.client);
			}
		}
	}
	

}
