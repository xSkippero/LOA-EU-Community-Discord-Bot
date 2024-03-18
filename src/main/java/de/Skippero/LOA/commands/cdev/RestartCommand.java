package de.Skippero.LOA.commands.cdev;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cdev
Created by Skippero
on 18.03.2024 , 15:14

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RestartCommand extends LOACommand {
    public RestartCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() != 397006908424454147L) {
            event.reply("You do not have the required permissions to execute this command").setEphemeral(true).queue();
        } else {
            event.reply("Restarting...").setEphemeral(true).queue();
            LOABot.log("Manually restarted bot");
            LOABot.restartBot();
        }
    }
}
