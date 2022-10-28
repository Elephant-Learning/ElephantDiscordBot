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
import java.util.stream.Collectors;

public class UserGetDecksCommand {

    @SubscribeEvent
    public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("get-deck")) {
            return;
        }

        User discordUser = Objects.requireNonNull(event.getOption("user")).getAsUser();

        JsonConfigHandler handler = Main.USER_CONFIG_LISTS.get(event.getGuild().getId());

        if (!handler.hasElephantId(discordUser.getIdLong())) {
            event.reply(discordUser.getAsMention() + " has not verified yet! Do `/verify` to verify yourself!").queue();
            return;
        }


        event.deferReply().queue();

        long elephantId = handler.getElephantId(discordUser.getId());

        JsonObject response = Request.getUserInfo(elephantId);

        if (ResponseUtils.isFailure(response)) {
            event.getHook().editOriginal("Failure retrieving information!: " + ResponseUtils.getMessage(response)).queue();
            return;
        }

        JsonObject user = response.get("context").getAsJsonObject().get("user").getAsJsonObject();

        JsonArray array = user.get("decks").getAsJsonArray();

        Map<String, Integer> strs = new HashMap<>();

        for (JsonElement jsonElement : array) {
            JsonObject obj = jsonElement.getAsJsonObject();

            String name = obj.get("name").getAsString();

            if (!strs.containsKey(name)) {
                strs.put(name, obj.get("numberOfLikes").getAsInt());
            }
        }

        strs = RankSongCommand.sortValues(strs);

        String str = strs
                .entrySet()
                .stream()
                .map(stringIntegerEntry -> "`" + stringIntegerEntry.getKey() + "` (`" + stringIntegerEntry.getValue() + "` likes)")
                .collect(Collectors.joining("\n"));

        str = discordUser.getAsMention() + "'s Decks:\n" + str;

        event.getHook().editOriginal(str).queue();
    }
}
