package me.elephantsuite.config;

import static me.elephantsuite.config.PropertiesHandler.CONFIG_HOME_DIRECTORY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.elephantsuite.Main;

public class JsonConfigHandler {

	private final Path configFilePath;

	private final Map<String, Long> USER_ID_TO_ELEPHANT_ID = new HashMap<>();


	public JsonConfigHandler(String guildID) {
		configFilePath = CONFIG_HOME_DIRECTORY.resolve(guildID).resolve("users.json");

		if (!Files.exists(configFilePath.getParent())) {
			try {
				Files.createDirectories(configFilePath.getParent());
			} catch (IOException e) {
				Main.LOGGER.error("Error while initializing Server Config for server \"" + guildID  + "\"!");
				e.printStackTrace();
			}
		}
	}

	public void save() {
		JsonArray array = new JsonArray();
		USER_ID_TO_ELEPHANT_ID.forEach((s, aLong) -> {
		});
	}

	public void initialize() {
		try {
			load();
			save();
		} catch (IOException e) {
			Main.LOGGER.error("Error while initializing user configs for server " + "\"" + configFilePath + "\"" + "!");
			e.printStackTrace();
		}
	}

	public void reload() {
		try {
			save();
			load();
		} catch (IOException e) {
			Main.LOGGER.error("Error while reloading user configs for server " + "\"" + configFilePath + "\"" + "!");
			e.printStackTrace();
		}


}
