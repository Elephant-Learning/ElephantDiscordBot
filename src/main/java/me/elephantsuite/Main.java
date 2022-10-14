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

    //TODO include in gitignore to stop token getting leaked
    public static final PropertiesHandler TOKEN = PropertiesHandler
            .builder()
            .addConfigOption("token", "")
            .build();

    public static final PropertiesHandler CONFIG = PropertiesHandler
            .builder()
            .addConfigOption("statusType", OnlineStatus.ONLINE.toString())
            .addConfigOption("activityType", Activity.ActivityType.PLAYING.toString())
            .addConfigOption("activityText", "Helping People Memorize")
            .addConfigOption("createdCommands", "false")
            .build();

    public static final Map<String, PropertiesHandler> SERVER_CONFIG_LISTS = new HashMap<>();

    public static JDA JDA;

    public static void main(String[] args) {
        JDA = JDABuilder
                .createDefault(TOKEN.getConfigOption("token"))
                .setEventManager(new AnnotatedEventManager())
                .setActivity(Activity.of(CONFIG.getConfigOption("activityType", Activity.ActivityType::valueOf), CONFIG.getConfigOption("activityText")))
                .setStatus(CONFIG.getConfigOption("statusType", OnlineStatus::valueOf))
                .build();

        if (!CONFIG.getConfigOption("createdCommands", Boolean::valueOf)) {
            //TODO Add slash commands to register
            //see https://github.com/OverlordsIII/QuotebookBot/blob/master/src/main/java/io/github/overlordsiii/Main.java#L68
            CONFIG.setConfigOption("createdCommands", "true");
            CONFIG.reload();
        }
    }
}
