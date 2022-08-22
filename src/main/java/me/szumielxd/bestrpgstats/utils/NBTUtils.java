package me.szumielxd.bestrpgstats.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class NBTUtils {
	
	
	
	
	public static @NotNull JsonObject getItemAsJson(@Nullable ItemStack item) {
		if (item == null) return new JsonObject();
		try {
			Object nms = ReflectionUtils.CraftItemStack_asNMSCopy.invoke(null, item);
			Object nbt = ReflectionUtils.NBTTagCompound.getConstructor().newInstance();
			nms.getClass().getMethod("save", ReflectionUtils.NBTTagCompound).invoke(nms, nbt);
			return getNBTToJson(nbt);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	public static JsonObject getNBTToJson(Object nbt) {
		JsonObject json = new JsonObject();
		Map<String,?> map = (Map<String, ?>) ReflectionUtils.getFieldValue(nbt, "map");
		Set<String> keys = map.keySet();
		keys.forEach(key -> {
			Object value = ReflectionUtils.invokeMethod(ReflectionUtils.NBTTagCompound, nbt, "get", key);
			if(ReflectionUtils.NBTTagCompound.isInstance(value)) json.add(key, getNBTToJson(value));
			else if(ReflectionUtils.NBTTagList.isInstance(value)) json.add(key, getNBTListToList((ArrayList<?>) ReflectionUtils.getFieldValue(value, "list")));
			else json.add(key, getObjectToJson(ReflectionUtils.getFieldValue(value, "data")));
		});
		return json;
	}
	
	
	public static JsonArray getNBTListToList(ArrayList<?> nbtList) {
		JsonArray arr = new JsonArray();
		nbtList.stream().map(nbt -> {
			if(ReflectionUtils.NBTTagCompound.isInstance(nbt)) return getNBTToJson(nbt);
			else if(ReflectionUtils.NBTTagList.isInstance(nbt)) return getNBTListToList((ArrayList<?>) ReflectionUtils.getFieldValue(nbt, "list"));
			return getObjectToJson(ReflectionUtils.getFieldValue(nbt, "data"));
		}).forEach(arr::add);
		return arr;
	}
	
	
	public static JsonObject jsonObjectDeepClone(JsonObject json) {
		if(json == null) return null;
		JsonObject newJson = new JsonObject();
		json.entrySet().stream().forEach(e -> {
			if(e.getValue().isJsonObject()) newJson.add(e.getKey(), jsonObjectDeepClone(e.getValue().getAsJsonObject()));
			else if(e.getValue().isJsonArray()) newJson.add(e.getKey(), jsonArrayDeepClone(e.getValue().getAsJsonArray()));
			else newJson.add(e.getKey(), e.getValue());
		});
		return newJson;
	}
	
	
	public static JsonArray jsonArrayDeepClone(JsonArray json) {
		if (json == null) return null;
		JsonArray newJson = new JsonArray();
		json.forEach(e -> {
			if(e.isJsonObject()) newJson.add(jsonObjectDeepClone(e.getAsJsonObject()));
			else if(e.isJsonArray()) newJson.add(jsonArrayDeepClone(e.getAsJsonArray()));
			else newJson.add(e);
		});
		return newJson;
	}
	
	
	public static JsonElement getObjectToJson(Object obj) {
		if (obj == null) return JsonNull.INSTANCE;
		if (obj instanceof JsonElement) return (JsonElement) obj;
		if (obj instanceof Boolean) return new JsonPrimitive((Boolean) obj);
		if (obj instanceof Character) return new JsonPrimitive((Character) obj);
		if (obj instanceof Number) return new JsonPrimitive((Number) obj);
		if (obj instanceof String) return new JsonPrimitive((String) obj);
		return new Gson().toJsonTree(obj);
	}
	

}
