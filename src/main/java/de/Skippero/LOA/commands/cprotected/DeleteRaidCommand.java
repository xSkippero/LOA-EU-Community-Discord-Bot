package de.Skippero.LOA.commands.cprotected;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cprotected
Created by Skippero
on 18.03.2024 , 15:20

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import de.Skippero.LOA.features.raid.Raid;
import de.Skippero.LOA.features.raid.RaidManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class DeleteRaidCommand extends LOACommand {
    public DeleteRaidCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if(commandGuild == null) {
            event.reply("Please use this command only on a guild").queue();
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.deleteraid", commandGuild.getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping raidIdObj = event.getOption("raidid");
        long raidId = raidIdObj != null ? raidIdObj.getAsInt() : 0;

        if(event.getOptions().size() != 1) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        Raid raid = RaidManager.getById(raidId);

        if(raid == null) {
            event.reply("Please input raidIds which exist").setEphemeral(true).queue();
            return;
        }

        raid.deleteRaid();
        raid.deleteMessage();

        event.reply("Successfully deleted the raid").setEphemeral(true).queue();
    }
}
