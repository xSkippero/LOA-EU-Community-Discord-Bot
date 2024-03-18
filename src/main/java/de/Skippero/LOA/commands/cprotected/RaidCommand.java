package de.Skippero.LOA.commands.cprotected;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cprotected
Created by Skippero
on 18.03.2024 , 15:18

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import de.Skippero.LOA.features.raid.Raid;
import de.Skippero.LOA.features.raid.RaidManager;
import de.Skippero.LOA.features.raid.RaidMeta;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.sql.SQLException;

public class RaidCommand extends LOACommand {
    public RaidCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if(commandGuild == null) {
            event.reply("Please use this command only on a guild").queue();
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.raid", commandGuild.getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping nameObj = event.getOption("name");
        OptionMapping descObj = event.getOption("desc");
        OptionMapping dpsCountObj = event.getOption("dpscount");
        OptionMapping suppCountObj = event.getOption("suppcount");
        OptionMapping startDateObj = event.getOption("startdate");
        OptionMapping durationObj = event.getOption("duration");

        String name = nameObj != null ? nameObj.getAsString() : "";
        String desc = descObj != null ? descObj.getAsString() : "";
        int dpsCount = dpsCountObj != null ? dpsCountObj.getAsInt() : 0;
        int suppCount = suppCountObj != null ? suppCountObj.getAsInt() : 0;
        String startDate = startDateObj != null ? startDateObj.getAsString() : "";
        String duration = durationObj != null ? durationObj.getAsString() : "";

        if(event.getOptions().size() != 6) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        RaidMeta meta = new RaidMeta(name,desc,duration,startDate);
        try {
            Raid raid = new Raid(event.getChannel().asTextChannel(), meta, dpsCount, suppCount);
            event.reply("Created Raid with ID #" + raid.getId()).setEphemeral(true).queue();
            RaidManager.raids.add(raid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
