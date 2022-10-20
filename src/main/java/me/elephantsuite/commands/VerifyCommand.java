package me.elephantsuite.commands;

import com.google.gson.JsonObject;
import me.elephantsuite.Main;
import me.elephantsuite.config.JsonConfigHandler;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import me.elephantsuite.util.ResponseUtils;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.IOException;
import java.util.Objects;

public class VerifyCommand {

    @SubscribeEvent
    public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("verify")) {
            return;
        }

        event.deferReply().queue();

        String email = Objects.requireNonNull(event.getInteraction().getOption("email")).getAsString();
        String password = Objects.requireNonNull(event.getInteraction().getOption("password")).getAsString();

        JsonObject object = new JsonObject();

        object.addProperty("email", email);
        object.addProperty("password", password);

        Request loginRequest = new Request("login", Method.POST, object);

        JsonObject response = loginRequest.makeRequest();

        if (ResponseUtils.isFailure(response)) {
            event.getHook().editOriginal("An Error Occurred!: `" + ResponseUtils.getMessage(response) + "`").queue();
            return;
        }

        JsonObject user = response.get("context").getAsJsonObject().get("user").getAsJsonObject();

        long elephantId = user.get("id").getAsLong();

        JsonConfigHandler handler = Main.USER_CONFIG_LISTS.get(event.getGuild().getId());

        handler.addElephantDiscordUser(event.getUser().getId(), elephantId);
        handler.reload();

        Main.USER_CONFIG_LISTS.replace(event.getGuild().getId(), handler);

        event.getHook().editOriginal("Linked " + event.getUser().getAsMention() +  " to ElephantUser `"  + user.get("fullName").getAsString() + "`").queue();


    }
}
