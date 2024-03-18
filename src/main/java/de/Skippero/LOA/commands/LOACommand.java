package de.Skippero.LOA.commands;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands
Created by Skippero
on 18.03.2024 , 14:37

*/

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class LOACommand {

    public String commandName;

    public LOACommand(String commandName) {
        this.commandName = commandName;
    };

    public void onExecute(SlashCommandInteractionEvent slashCommandEvent) {
        String name = slashCommandEvent.getName();
        User executor = slashCommandEvent.getUser();
        Guild guild = slashCommandEvent.getGuild();
        MessageChannel channel = slashCommandEvent.getChannel();
        execute(name, executor, guild, channel, slashCommandEvent);
    };

    public abstract void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event);

}
