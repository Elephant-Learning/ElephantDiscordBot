package me.elephantsuite.commands;

import com.google.gson.JsonObject;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.io.IOException;

public class UserCountCommand {

    @SubscribeEvent
    public void onSlashCommandEvent(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("user-count")) {
            return;
        }

        Request request = new Request("login/getUserByName?userId=1&name=''", Method.GET, null);

        JsonObject obj = request.makeRequest();

        int size = obj.get("users").getAsJsonArray().size();

        event.reply("Elephant currently has `" + size + "` users.").queue();
    }
}
