package me.elephantsuite.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

public class WebsiteCommand {

	@SubscribeEvent
	public void onSlashCommand(SlashCommandInteractionEvent event) {
		if (event.getGuild() == null) {
			event.reply("Must use guild to run this command!").queue();
			return;
		}

		if (!event.getName().contains("website")) {
			return;
		}

		event.reply("https://elephantsuite.me/").queue();
	}
}
