package de.Skippero.LOA.commands.cpublic;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands
Created by Skippero
on 18.03.2024 , 14:40

*/

import de.Skippero.LOA.commands.LOACommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PingCommand extends LOACommand {
    public PingCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(true).flatMap(v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)).queue();
    }
}
