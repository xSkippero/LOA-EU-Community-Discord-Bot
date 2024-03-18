package de.Skippero.LOA.commands.cprotected;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cprotected
Created by Skippero
on 18.03.2024 , 15:21

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;

public class MergeRoleCommand extends LOACommand {
    public MergeRoleCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if(event.getGuild() == null) {
            event.reply("Please use this command only on a guild").queue();
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.merge", commandGuild.getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping roleAObj = event.getOption("rolea");
        OptionMapping roleBObj = event.getOption("roleb");

        String roleAs = roleAObj != null ? roleAObj.getAsString() : "";
        String roleBs = roleBObj != null ? roleBObj.getAsString() : "";

        if(event.getOptions().size() != 2) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        Guild g = event.getGuild();

        List<Role> rolesA = g.getRolesByName(roleAs,false);
        List<Role> rolesB = g.getRolesByName(roleBs,false);

        if(rolesA.isEmpty()) {
            event.reply("Role A -> '" + roleAs + "' does not exist!").setEphemeral(true).queue();
            return;
        }

        if(rolesB.isEmpty()) {
            event.reply("Role B -> '" + roleBs + "' does not exist!").setEphemeral(true).queue();
            return;
        }

        Role roleA = rolesA.get(0);
        Role roleB = rolesB.get(0);

        List<Member> membersA = g.getMembersWithRoles(roleA);

        LOABot.log("Merging " + membersA.size() + " members into " + roleB.getName());

        for (Member member : membersA) {
            g.addRoleToMember(member,roleB).queue(done->LOABot.log("Added " + member.getEffectiveName() + " to role " + roleB.getName()));
            g.removeRoleFromMember(member,roleA).queue(done->LOABot.log("Removed " + member.getEffectiveName() + " from role " + roleA.getName()));
        }

        LOABot.log("Done starting addRoleToMember requests");

        event.reply("All merge requests started").setEphemeral(true).queue();
    }
}
