package me.elephantsuite.commands;

import com.google.gson.JsonObject;
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
            event.getHook().editOriginal("Invalid Password/Email!: `" + ResponseUtils.getMessage(response) + "`").queue();
            return;
        }

        long elephantId = response.get("context").getAsJsonObject().get("user")



    }
}
