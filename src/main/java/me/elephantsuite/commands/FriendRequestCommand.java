package me.elephantsuite.commands;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import me.elephantsuite.Main;
import me.elephantsuite.config.JsonConfigHandler;
import me.elephantsuite.request.Method;
import me.elephantsuite.request.Request;
import me.elephantsuite.util.NotificationType;
import me.elephantsuite.util.ResponseUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FriendRequestCommand {

    private List<JsonObject> users = null;

    @SubscribeEvent
    public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("friend")) {
            return;
        }

        event.deferReply().queue();

        JsonConfigHandler handler = Main.USER_CONFIG_LISTS.get(event.getGuild().getId());

        long elephantId;
        OptionMapping userMapping = event.getOption("user");
        OptionMapping emailMapping = event.getOption("email");
        OptionMapping nameMapping = event.getOption("name");

        User executingUser = event.getUser();
        if (!handler.hasElephantId(executingUser.getIdLong())) {
            event.getHook().editOriginal( "You have not verified yet! Do `/verify` to verify yourself!").queue();
            return;
        }

        long executingUserId = handler.getElephantId(executingUser.getId());

        if (userMapping != null) {
            User discordUser = userMapping.getAsUser();
            if (!handler.hasElephantId(discordUser.getIdLong())) {
                event.getHook().editOriginal(discordUser.getAsMention() + " has not verified yet! Do `/verify` to verify yourself!").queue();
                return;
            }

            elephantId = handler.getElephantId(discordUser.getId());
        } else if (emailMapping != null) {
            String email = emailMapping.getAsString();

            Request getUserReq = new Request("login/userByEmail?email=" + email, Method.GET, null);

            JsonObject response = getUserReq.makeRequest();

            if (ResponseUtils.isFailure(response)) {
                event.getHook().editOriginal("Failure while sending request to user via email: " + ResponseUtils.getMessage(response)).queue();
                return;
            }

            elephantId = response.get("context").getAsJsonObject().get("user").getAsJsonObject().get("id").getAsLong();
        } else if (nameMapping != null) {
            String name = nameMapping.getAsString();

            JsonObject response = new Request("login/userByName?name=" + name + "&userId=" + executingUserId, Method.GET, null).makeRequest();

            if (ResponseUtils.isFailure(response)) {
                event.getHook().editOriginal("Error while getting user by name: " + ResponseUtils.getMessage(response)).queue();
                return;
            }

            JsonArray users = response.get("context").getAsJsonObject().get("users").getAsJsonArray();

            List<JsonObject> userObjs = new ArrayList<>();

            for (JsonElement user : users) {
                userObjs.add(user.getAsJsonObject());
            }

            this.users = userObjs;

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle("Choose what user you want to friend")
                    .setAuthor(event.getUser().getName(), event.getUser().getAvatarUrl());

            for (int i = 0; i < userObjs.size(); i++) {
                embed.addField(String.valueOf(i + 1), userObjs.get(i).get("fullName").getAsString(), true);
            }

            MessageCreateAction action = event.getChannel().sendMessageEmbeds(embed.build());

            List<ItemComponent> components = new ArrayList<>();

            for (int i = 0; i < userObjs.size(); i++) {
                components.add(Button.primary(String.valueOf(i + 1), userObjs.get(i).get("fullName").getAsString()));
            }

            List<List<ItemComponent>> rows = getSubLists(components, 5);

            rows.forEach(action::addActionRow);

            action.queue();

            return;
        } else {
            event.getHook().editOriginal("Please fill out the email or user option!").queue();
            return;
        }



        String targetUserName = getNameOfUser(elephantId);

        if (targetUserName.startsWith("Error: ")) {
            event.getHook().editOriginal(targetUserName).queue();
            return;
        }



        String executingUserName = getNameOfUser(executingUserId);

        if (executingUserName.startsWith("Error: ")) {
            event.getHook().editOriginal(targetUserName).queue();
            return;
        }

        JsonObject object = Request.getUserInfo(executingUserId);

        if (ResponseUtils.isFailure(object)) {
            event.getHook().editOriginal("Error while getting current friends: " + ResponseUtils.getMessage(object)).queue();
            return;
        }

        object = object.get("context").getAsJsonObject().get("user").getAsJsonObject();

        JsonArray friendIds = object.get("friendIds").getAsJsonArray();

        if (friendIds.contains(new JsonPrimitive(elephantId))) {
            event.getHook().editOriginal("You are already friended with that user!").queue();
            return;
        }



        JsonObject friendRequestBody = new JsonObject();
        friendRequestBody.addProperty("type", NotificationType.FRIEND_REQUEST.name());
        friendRequestBody.addProperty("message", executingUserName + " sent " + targetUserName + " a friend request!");
        friendRequestBody.addProperty("senderId", executingUserId);
        friendRequestBody.addProperty("recipientId", elephantId);

        JsonObject response = new Request("notifications/sendFriendRequest", Method.POST, friendRequestBody).makeRequest();

        if (ResponseUtils.isFailure(response)) {
            event.getHook().editOriginal("Error while sending friend request: " +  ResponseUtils.getMessage(response)).queue();
            return;
        }

        event.getHook().editOriginal("Sent friend request to user! Tell them to check elephant dashboard to accept!").queue();

    }

    @SubscribeEvent
    public void onButtonClick(ButtonInteractionEvent event) throws IOException, InterruptedException {
        int num = Integer.parseInt(event.getInteraction().getComponentId());
        if (event.getGuild() == null){
            return;
        }



        event.deferReply().queue();

        JsonObject user = this.users.get(num - 1);

        JsonConfigHandler handler = Main.USER_CONFIG_LISTS.get(event.getGuild().getId());

        long executingUserId = handler.getElephantId(event.getUser().getId());

        long targetUserId = user.get("id").getAsLong();

        String executingUserName = getNameOfUser(executingUserId);

        String targetUserName = getNameOfUser(targetUserId);

        if (executingUserName.startsWith("Error:") || targetUserName.startsWith("Error: ")) {
            event.getHook().editOriginal(executingUserName + "\n" + targetUserName).queue();
            return;
        }

        JsonObject friendRequestBody = new JsonObject();
        friendRequestBody.addProperty("type", NotificationType.FRIEND_REQUEST.name());
        friendRequestBody.addProperty("message", executingUserName + " sent " + targetUserName + " a friend request!");
        friendRequestBody.addProperty("senderId", executingUserId);
        friendRequestBody.addProperty("recipientId", targetUserId);

        JsonObject response = new Request("notifications/sendFriendRequest", Method.POST, friendRequestBody).makeRequest();

        if (ResponseUtils.isFailure(response)) {
            event.getHook().editOriginal("Error while sending friend request: " +  ResponseUtils.getMessage(response)).queue();
            return;
        }

        users = null;
        event.getHook().editOriginal("Sent friend request to user! Tell them to check elephant dashboard to accept!").queue();
    }

    private static String getNameOfUser(long elephantId) throws IOException, InterruptedException {
        JsonObject response = Request.getUserInfo(elephantId);

        if (ResponseUtils.isFailure(response)) {
            return "Error: " + ResponseUtils.getMessage(response);
        }

        JsonObject user = response.get("context").getAsJsonObject().get("user").getAsJsonObject();

        return user.get("fullName").getAsString();
    }

    private static <T> List<List<T>> getSubLists(List<T> parentList, int childListSize) {
        int currentIndex = 0;

        List<List<T>> result = new ArrayList<>();

        while (currentIndex < parentList.size())  {
            if (currentIndex + 5 > parentList.size() - 1) {
                result.add(parentList.subList(currentIndex, parentList.size()));
            } else {
                result.add(parentList.subList(currentIndex, currentIndex + 5));
            }
            currentIndex += 5;
        }

        return result;
    }


}
