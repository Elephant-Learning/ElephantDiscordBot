package me.elephantsuite;

import me.elephantsuite.commands.*;
import me.elephantsuite.config.JsonConfigHandler;
import me.elephantsuite.config.PropertiesHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class Main {

    public static final Logger LOGGER = LogManager.getLogger("ElephantDiscordBot");

    public static final PropertiesHandler PRIVATE_CONFIG = PropertiesHandler
            .builder()
            .addConfigOption("token", "")
            .addConfigOption("elephantBackendDomain", "")
            .setFileName("elephant-discord-private-config")
            .build();

    public static final PropertiesHandler CONFIG = PropertiesHandler
            .builder()
            .addConfigOption("statusType", OnlineStatus.ONLINE.toString())
            .addConfigOption("activityType", Activity.ActivityType.PLAYING.toString())
            .addConfigOption("activityText", "Helping People Memorize")
            .addConfigOption("createdCommands", "false")
            .addConfigOption("developmentMode", "true")
            .setFileName("elephant-discord-bot")
            .build();

   // public static final Map<String, PropertiesHandler> SERVER_CONFIG_LISTS = new HashMap<>();
    // not needed right now, uncomment for later possibly

    public static final Map<String, JsonConfigHandler> USER_CONFIG_LISTS = new HashMap<>();

    public static JDA JDA;

    public static void main(String[] args) throws InterruptedException {
        JDA = JDABuilder
                .createDefault(PRIVATE_CONFIG.getConfigOption("token"))
                .setEventManager(new AnnotatedEventManager())
                .addEventListeners(new UserCountCommand(), new DeckCountCommand(), new RankDeckCommand(), new WebsiteCommand(), new RankSongCommand())
                .addEventListeners(new VerifyCommand(), new GetUserStatsCommand(), new UserGetDecksCommand())
                .setActivity(Activity.of(CONFIG.getConfigOption("activityType", Activity.ActivityType::valueOf), CONFIG.getConfigOption("activityText")))
                .setStatus(CONFIG.getConfigOption("statusType", OnlineStatus::valueOf))
                .build();

        if (CONFIG.getConfigOption("developmentMode", Boolean::valueOf)) {
            JDA.awaitReady().getGuilds().forEach(g -> {
                g.updateCommands()
                    .addCommands(Commands.slash("user-count", "Gets the amount of users using elephant"))
                    .addCommands(Commands.slash("deck-count", "Gets the amount of decks in elephant"))
                    .addCommands(Commands.slash("rank-deck", "Ranks the top 10 liked decks in elephant"))
                    .addCommands(Commands.slash("rank-song", "Ranks songs in Elephant based on like total"))
                    .addCommands(Commands.slash("website", "Gives link to elephant website"))
                    .addCommands(Commands.slash("verify", "Verifies your discord account to your elephant account")
                        .addOption(OptionType.STRING, "email", "Your email that you used to register for elephant", true)
                        .addOption(OptionType.STRING, "password", "Your password that you used to register for elephant", true))
                    .addCommands(Commands.slash("user-stats", "Gets user stats (Must be verified with /verify to run)")
                        .addOption(OptionType.USER, "user", "User to get stats of", true))
                    .addCommands(Commands.slash("get-deck", "Gets another user's decks (Must be verified with /verify to run)")
                        .addOption(OptionType.USER, "user", "User to get decks of", true))
                    .queue();
                LOGGER.info("Created dev commands for guild " + g.getName());
                //TODO Add slash commands to register for guilds only (much faster)
                //see https://github.com/OverlordsIII/QuotebookBot/blob/master/src/main/java/io/github/overlordsiii/Main.java#L68
            });
        }

        if (!CONFIG.getConfigOption("createdCommands", Boolean::valueOf) && !CONFIG.getConfigOption("developmentMode", Boolean::valueOf)) {
            //TODO Add slash commands to register
            //see https://github.com/OverlordsIII/QuotebookBot/blob/master/src/main/java/io/github/overlordsiii/Main.java#L68
            CONFIG.setConfigOption("createdCommands", "true");
            CONFIG.reload();
        }



        JDA.awaitReady().getGuilds().forEach(guild -> {
            if (!USER_CONFIG_LISTS.containsKey(guild.getId())) {
                JsonConfigHandler handler = new JsonConfigHandler(guild.getId());
                handler.initialize();
                USER_CONFIG_LISTS.put(guild.getId(), handler);
            }


            // doesn't seem like this is needed for right now
            /*
            if (!SERVER_CONFIG_LISTS.containsKey(guild.getId())) {

                SERVER_CONFIG_LISTS.put(guild.getId(), PropertiesHandler
                        .builder()
                        .serverConfig()
                        .setFileName(guild.getId() + ".properties")
                        //for sanity
                        .addConfigOption("name", guild.getName())
                        .build()
                );
            }
             */
        });

    }
}
