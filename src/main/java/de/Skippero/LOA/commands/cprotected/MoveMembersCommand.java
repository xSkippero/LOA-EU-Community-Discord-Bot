package de.Skippero.LOA.commands.cprotected;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cprotected
Created by Skippero
on 18.03.2024 , 15:19

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import de.Skippero.LOA.features.raid.Raid;
import de.Skippero.LOA.features.raid.RaidManager;
import de.Skippero.LOA.features.raid.RaidMember;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class MoveMembersCommand extends LOACommand {
    public MoveMembersCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if(commandGuild == null) {
            event.reply("Please use this command only on a guild").queue();
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.movemembers", commandGuild.getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping raidAObj = event.getOption("raida");
        OptionMapping raidBObj = event.getOption("raidb");
        long raidAId = raidAObj != null ? raidAObj.getAsInt() : 0;
        long raidBId = raidBObj != null ? raidBObj.getAsInt() : 0;

        if(event.getOptions().size() != 2) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        Raid raidA = RaidManager.getById(raidAId);
        Raid raidB = RaidManager.getById(raidBId);

        if(raidA == null || raidB == null) {
            event.reply("Please input raidIds which exist").setEphemeral(true).queue();
            return;
        }

        raidB.getActiveMembers().stream().mapToLong(RaidMember::getId).forEach(raidB::removeMember);
        raidB.getBenchedMembers().stream().mapToLong(RaidMember::getId).forEach(raidB::removeMember);
        raidA.getActiveMembers().forEach(activeMember -> raidB.addMember(activeMember.getUserId(), activeMember.isExp(), activeMember.getUserClass(), activeMember.getUserName()));
        raidA.getBenchedMembers().forEach(activeMember -> raidB.addMember(activeMember.getUserId(), activeMember.isExp(), activeMember.getUserClass(), activeMember.getUserName()));

        event.reply("Copied all members from raid #" + raidAId + " to raid #"+raidBId).setEphemeral(true).queue();
    }
}
