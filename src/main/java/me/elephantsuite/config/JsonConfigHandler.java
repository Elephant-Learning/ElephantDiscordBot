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
import me.elephantsuite.util.JsonUtils;

public class JsonConfigHandler {

	private final Path configFilePath;

	private Map<String, Long> USER_ID_TO_ELEPHANT_ID = new HashMap<>();


	public JsonConfigHandler(String guildID) {
		configFilePath = CONFIG_HOME_DIRECTORY.resolve(guildID).resolve("users.json");

		if (!Files.exists(configFilePath.getParent())) {
			try {
				Files.createDirectories(configFilePath.getParent());
			} catch (IOException e) {
				Main.LOGGER.error("Error while initializing Server Config for server \"" + guildID + "\"!");
				e.printStackTrace();
			}
		}
	}

	public void save() throws IOException {
		JsonObject obj = new JsonObject();
		USER_ID_TO_ELEPHANT_ID.forEach((s, aLong) -> {
			obj.addProperty(s, aLong);
		});
		String file = JsonUtils.GSON.toJson(obj);

		Files.writeString(configFilePath, file);
	}

	public void load() throws IOException {
		if (!Files.exists(configFilePath)) {
			return;
		}

		String file = Files.readString(configFilePath);

		JsonObject obj = JsonUtils.GSON.fromJson(file, JsonObject.class);

		obj.entrySet().forEach(stringJsonElementEntry -> {
			this.USER_ID_TO_ELEPHANT_ID.put(stringJsonElementEntry.getKey(), stringJsonElementEntry.getValue().getAsLong());
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

	public long getElephantId(String discordId) {
		return USER_ID_TO_ELEPHANT_ID.get(discordId);
	}

	public long getElephantId(long discordId) {
		return getElephantId(String.valueOf(discordId));
	}

	public void addElephantDiscordUser(String userId, long elephantId) {
		this.USER_ID_TO_ELEPHANT_ID.put(userId, elephantId);
	}

	public boolean hasElephantId(long discordId) {
		return this.USER_ID_TO_ELEPHANT_ID.get(String.valueOf(discordId)) != null;
	}

	public void addElephantDiscordUser(long userId, long elephantId) {
		addElephantDiscordUser(String.valueOf(userId), elephantId);
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
}
