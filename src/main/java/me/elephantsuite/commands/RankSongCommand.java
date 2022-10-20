package me.elephantsuite.commands;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import me.elephantsuite.util.ResponseUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class RankSongCommand {

	@SubscribeEvent
	public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
		if (event.getGuild() == null) {
			event.reply("Must use guild to run this command!").queue();
			return;
		}

		if (!event.getName().contains("rank-song")) {
			return;
		}

		event.deferReply().queue();

		Request request = new Request("login/userByName?name=&userId=1", Method.GET, null);

		JsonObject obj = request.makeRequest();

		if (ResponseUtils.isFailure(obj)) {
			event.getHook().editOriginal("Failure retrieving information!: " + ResponseUtils.getMessage(obj)).queue();
			return;
		}

		JsonObject ctx = obj.get("context").getAsJsonObject();

		JsonArray users = ctx.get("users").getAsJsonArray();

		HashMap<String, Integer> songLikeMap = new HashMap<>();

		for (JsonElement jsonElement : users) {
			JsonObject object = jsonElement.getAsJsonObject();

			JsonArray likeSongs = object.get("likedSongs").getAsJsonArray();

			for (JsonElement jsonElement1 : likeSongs) {
				String song = jsonElement1.getAsString();

				if (!songLikeMap.containsKey(song)) {
					songLikeMap.put(song, 1);
				} else {
					int prev = songLikeMap.get(song);
					songLikeMap.replace(song, prev + 1);
				}
			}
		}

		songLikeMap = sortValues(songLikeMap);

		String str = songLikeMap
			.entrySet()
			.stream()
			.map(stringIntegerEntry -> "`" + stringIntegerEntry.getKey() + "` (`" + stringIntegerEntry.getValue() + "` likes)")
			.collect(Collectors.joining("\n"));

		if (str.isEmpty()) {
			str = "No songs liked by any users at this moment.";
		}

		event.getHook().editOriginal(str).queue();
	}

	public static HashMap<String, Integer> sortValues(Map<String, Integer> map) {
		List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());

		list.sort(Map.Entry.comparingByValue((o1, o2) -> {
			if (o1 > o2) {
				return -1;
			} else if (o2 > o1) {
				return 1;
			}

			return 0;
		}));

		HashMap<String, Integer> sortedHashMap = new LinkedHashMap<>();

		list.forEach(stringIntegerEntry -> {
			sortedHashMap.put(stringIntegerEntry.getKey(), stringIntegerEntry.getValue());
		});

		return sortedHashMap;
	}
}
