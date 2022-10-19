package me.elephantsuite.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankDeckCommand {

    @SubscribeEvent
    public void slashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("deck-rank")) {
            return;
        }

        Request request = new Request("deck/getAll", Method.GET, null);

        JsonObject object = request.makeRequest();

        JsonArray decks = object.get("context").getAsJsonObject().get("decks").getAsJsonArray();

        List<JsonObject> objList = new ArrayList<>();

        decks.forEach(jsonElement -> {
            objList.add(jsonElement.getAsJsonObject());
        });

        int index = decks.size() >= 10 ? 11 : decks.size() - 1;

        List<JsonObject> filteredObj = objList
                .stream()
                .sorted(Comparator.comparingInt(o -> o.get("numberOfLikes").getAsInt()))
                .toList()
                .subList(0, index);

        List<String> deckToString = new ArrayList<>();

        for (int i = 1; i <= filteredObj.size(); i++) {
            JsonObject obj = filteredObj.get(i - 1);
            deckToString.add(i  + ". " + obj.get("name").getAsString() + " (`" + obj.get("numberOfLikes").getAsInt() + "` likes)");
        }

        String deckStr = String.join("\n", deckToString);


        event.reply(deckStr).queue();

    }
}
