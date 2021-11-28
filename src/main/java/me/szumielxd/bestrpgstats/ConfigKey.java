package me.szumielxd.bestrpgstats;

import java.util.Collections;

import org.jetbrains.annotations.NotNull;

import me.szumielxd.bestrpgstats.hikari.DatabaseConfig;

public enum ConfigKey {
	
	DATABASES("database", DatabaseConfig.deserialize(Collections.emptyMap())),
	
	PLAYER_DATA_VARIABLE_CLASS("player.data-variable.class", "metadata|Skript|stats_class"),
	PLAYER_DATA_VARIABLE_LEVEL("player.data-variable.level", "metadata|Skript|stats_level"),
	PLAYER_DATA_VARIABLE_ANIHILUS("player.data-variable.anihilus", "metadata|Skript|stats_anihilus"),
	//
	PLAYER_DATA_VARIABLE_STRENGTH("player.data-variable.strength", "metadata|Skript|stats_strength"),
	PLAYER_DATA_VARIABLE_DEXTERITY("player.data-variable.dexterity", "metadata|Skript|stats_dexterity"),
	PLAYER_DATA_VARIABLE_LUCK("player.data-variable.luck", "metadata|Skript|stats_luck"),
	PLAYER_DATA_VARIABLE_INTELLIGENCE("player.data-variable.intelligence", "metadata|Skript|stats_intelligence"),
	//
	PLAYER_DATA_VARIABLE_STRENGTH_ITEM("player.data-variable.strength-item", "metadata|Skript|stats_strength_item"),
	PLAYER_DATA_VARIABLE_DEXTERITY_ITEM("player.data-variable.dexterity-item", "metadata|Skript|stats_dexterity_item"),
	PLAYER_DATA_VARIABLE_LUCK_ITEM("player.data-variable.luck-item", "metadata|Skript|stats_luck_item"),
	PLAYER_DATA_VARIABLE_INTELLIGENCE_ITEM("player.data-variable.intelligence-item", "metadata|Skript|stats_intelligence_item"),
	//
	PLAYER_DATA_VARIABLE_STRENGTH_BONUS("player.data-variable.strength-bonus", "metadata|Skript|stats_strength_bonus"),
	PLAYER_DATA_VARIABLE_DEXTERITY_BONUS("player.data-variable.dexterity-bonus", "metadata|Skript|stats_dexterity_bonus"),
	PLAYER_DATA_VARIABLE_LUCK_BONUS("player.data-variable.luck-bonus", "metadata|Skript|stats_luck_bonus"),
	PLAYER_DATA_VARIABLE_INTELLIGENCE_BONUS("player.data-variable.intelligence-bonus", "metadata|Skript|stats_intelligence_bonus"),
	;

	private final String path;
	private final Object defaultValue;
	private final Class<?> type;
	
	private ConfigKey(@NotNull String path, @NotNull Object defaultValue) {
		this.path = path;
		this.defaultValue = defaultValue;
		this.type = defaultValue.getClass();
	}
	
	public String getPath() {
		return this.path;
	}
	
	public Object getDefault() {
		return this.defaultValue;
	}
	
	public Class<?> getType() {
		return this.type;
	}

}
