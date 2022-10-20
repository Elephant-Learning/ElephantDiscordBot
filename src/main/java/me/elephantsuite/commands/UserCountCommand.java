package me.elephantsuite.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import me.elephantsuite.util.JsonUtils;
import me.elephantsuite.util.ResponseUtils;
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

        event.deferReply().queue();

        Request request = new Request("login/userByName?name=&userId=1", Method.GET, null);

        JsonObject obj = request.makeRequest();

        if (ResponseUtils.isFailure(obj)) {
            event.getHook().editOriginal("Failure retrieving information!: " + ResponseUtils.getMessage(obj)).queue();
            return;
        }

        JsonObject ctx = obj.get("context").getAsJsonObject();

        JsonArray users = ctx.get("users").getAsJsonArray();

        event.getHook().editOriginal("Elephant currently has `" + users.size() + "` users.").queue();
    }
}
