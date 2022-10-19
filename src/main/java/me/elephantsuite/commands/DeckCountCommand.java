package me.elephantsuite.commands;

import com.google.gson.JsonObject;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.io.IOException;

public class DeckCountCommand {

    @SubscribeEvent
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("deck-count")) {
            return;
        }

        event.deferReply().queue();


        Request request = new Request("deck/getAll", Method.GET, null);

        JsonObject object = request.makeRequest();

        int decks = object.get("context").getAsJsonObject().get("decks").getAsJsonArray().size();

        event.getHook().editOriginal("Elephant currently has `" + decks + "` decks in use.").queue();

    }
}
