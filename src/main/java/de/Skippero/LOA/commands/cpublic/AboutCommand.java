package de.Skippero.LOA.commands.cpublic;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands
Created by Skippero
on 18.03.2024 , 15:12

*/

import de.Skippero.LOA.commands.LOACommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class AboutCommand extends LOACommand {
    public AboutCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        event.reply("""
                        This bot checks the status page of LostARK (EU) at predefined intervals and displays any changes in a Discord channel
                        Bot by Skippero
                        https://github.com/xSkippero/LOA-EUW-Status-Discord-Bot-""")
                .setEphemeral(true).queue();
    }
}
