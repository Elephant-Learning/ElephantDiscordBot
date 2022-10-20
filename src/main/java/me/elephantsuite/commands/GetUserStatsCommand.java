package me.elephantsuite.commands;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GetUserStatsCommand {

    @SubscribeEvent
    public void onSlashCommand(SlashCommandInteractionEvent event) throws IOException, InterruptedException {
        if (event.getGuild() == null) {
            event.reply("Must use guild to run this command!").queue();
            return;
        }

        if (!event.getName().contains("user-stats")) {
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

        JsonObject stats = user.get("elephantUserStatistics").getAsJsonObject();

        int daysStreak = stats.get("daysStreak").getAsInt();

        double usageTime = stats.get("usageTime").getAsDouble();

        LocalDateTime lastLoggedIn = LocalDateTime.parse(stats.get("lastLoggedIn").getAsString());

        List<Long> recentlyViewedDecks = new ArrayList<>();
        stats.get("recentlyViewedDeckIds").getAsJsonArray().forEach(jsonElement -> recentlyViewedDecks.add(jsonElement.getAsLong()));

        List<String> strings = recentlyViewedDecks
                .stream()
                .map(aLong -> {
                    try {
                        JsonObject jsonObject = deckIdToDeck(aLong);

                        if (ResponseUtils.isFailure(jsonObject)) {
                            Main.LOGGER.error(ResponseUtils.getMessage(jsonObject));
                            return null;
                        }

                        return jsonObject;
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(object -> {
                    if (object == null) {
                        return null;
                    }

                    return object.get("context").getAsJsonObject().get("deck").getAsJsonObject();
                })
                .map(jsonObject -> {
                    if (jsonObject == null) {
                        return null;
                    }

                    String deckName = jsonObject.get("name").getAsString();
                    String authorName;
                    try {
                        authorName = getFullNameByUserId(jsonObject.get("authorId").getAsLong());
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return "`" + deckName + "` - By " + "`" + authorName + "`";
                }).toList();

        List<String> numberedDecks = new ArrayList<>();

        for (int i = 1; i <= strings.size(); i++) {
            numberedDecks.add(i + ". " + strings.get(i - 1));
        }

        String decks = String.join("\n", numberedDecks);

        String finalStr = discordUser.getAsMention() + "'s user stats:\n" +
                "Days Streak: " + daysStreak + "\n" +
                "Usage Time: " + usageTime + "\n" +
                "Last Logged In: " + lastLoggedIn.getMonth() + " " + lastLoggedIn.getDayOfMonth() + ", " + lastLoggedIn.getYear() + "\n" +
                "Recently Viewed Decks:\n" +
                decks;

        event.getHook().editOriginal(finalStr).queue();

    }

    private static String getFullNameByUserId(long userId) throws IOException, InterruptedException {
        Request request = new Request("login/user?id=" + userId, Method.GET, null);

        JsonObject object = request.makeRequest();

        if (ResponseUtils.isFailure(object)) {
            Main.LOGGER.error("Failure retrieving information!: " + ResponseUtils.getMessage(object));
            return null;
        }

        return object.get("context").getAsJsonObject().get("user").getAsJsonObject().get("fullName").getAsString();
    }

    private static JsonObject deckIdToDeck(long deckId) throws IOException, InterruptedException {
        Request request = new Request("deck/get?id=" + deckId, Method.GET, null);

        return request.makeRequest();
    }
}
