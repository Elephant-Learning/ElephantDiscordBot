package me.elephantsuite.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.elephantsuite.Main;
import me.elephantsuite.config.JsonConfigHandler;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import me.elephantsuite.util.ResponseUtils;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class GetUserFoldersCommand {

    @SubscribeEvent
    public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("get-folder")) {
            return;
        }

        JsonConfigHandler handler = Main.USER_CONFIG_LISTS.get(event.getGuild().getId());

        User discordUser = Objects.requireNonNull(event.getOption("user")).getAsUser();

        if (!handler.hasElephantId(discordUser.getIdLong())) {
            event.reply(discordUser.getAsMention() + " has not verified yet! Do `/verify` to verify yourself!").queue();
            return;
        }

        event.deferReply().queue();

        long elephantId = handler.getElephantId(discordUser.getId());

        Request getUserInfo = new Request("login/user?id=" + elephantId, Method.GET, null);

        JsonObject response = getUserInfo.makeRequest();

        if (ResponseUtils.isFailure(response)) {
            event.getHook().editOriginal("Failure retrieving information!: " + ResponseUtils.getMessage(response)).queue();
            return;
        }

        JsonObject user = response.get("context").getAsJsonObject().get("user").getAsJsonObject();

        JsonArray folders = user.get("folders").getAsJsonArray();

        if (folders.size() == 0) {
            event.getHook().editOriginal("User has no folders!").queue();
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append(discordUser.getAsMention()).append("'s folders:\n");

        for (JsonElement jsonElement : folders) {
            JsonObject obj = jsonElement.getAsJsonObject();
            String name = obj.get("name").getAsString();
            builder
                .append(name)
                .append("\n");
            Map<String, Integer> sortedDecks = new HashMap<>();
            JsonArray array = obj.get("deckIds").getAsJsonArray();
            for (JsonElement element : array) {
                long deckId = element.getAsLong();

                JsonObject deckResponse = GetUserStatsCommand.deckIdToDeck(deckId);
                JsonObject deck = Objects.requireNonNull(deckResponse).get("context").getAsJsonObject().get("deck").getAsJsonObject();

                sortedDecks.put(deck.get("name").getAsString(), deck.get("numberOfLikes").getAsInt());
            }

            sortedDecks = RankSongCommand.sortValues(sortedDecks);

            sortedDecks.forEach((s, integer) -> {
                builder.append("\t").append(s).append("(`").append(integer).append("` Likes)").append("\n");
            });
        }

        event.getHook().editOriginal(builder.toString()).queue();


    }
}
