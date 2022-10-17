package me.elephantsuite;

import me.elephantsuite.config.PropertiesHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
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
            .build();

    public static final PropertiesHandler CONFIG = PropertiesHandler
            .builder()
            .addConfigOption("statusType", OnlineStatus.ONLINE.toString())
            .addConfigOption("activityType", Activity.ActivityType.PLAYING.toString())
            .addConfigOption("activityText", "Helping People Memorize")
            .addConfigOption("createdCommands", "false")
            .addConfigOption("developmentMode", "true")
            .build();

    //TODO Add JSON User configs
    public static final Map<String, PropertiesHandler> SERVER_CONFIG_LISTS = new HashMap<>();

    public static JDA JDA;

    public static void main(String[] args) throws InterruptedException {
        JDA = JDABuilder
                .createDefault(PRIVATE_CONFIG.getConfigOption("token"))
                .setEventManager(new AnnotatedEventManager())
                .setActivity(Activity.of(CONFIG.getConfigOption("activityType", Activity.ActivityType::valueOf), CONFIG.getConfigOption("activityText")))
                .setStatus(CONFIG.getConfigOption("statusType", OnlineStatus::valueOf))
                .build();

        if (CONFIG.getConfigOption("developmentMode", Boolean::valueOf)) {
            JDA.getGuilds().forEach(g -> {
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
            if (!SERVER_CONFIG_LISTS.containsKey(guild.getId())) {

                SERVER_CONFIG_LISTS.put(guild.getId(), PropertiesHandler
                        .builder()
                        .serverConfig()
                        .setFileName(guild.getId() + ".properties")
                        //TODO Add guild config values when needed
                        //for sanity
                        .addConfigOption("name", guild.getName())
                        .build()
                );
            }
        });
    }
}
