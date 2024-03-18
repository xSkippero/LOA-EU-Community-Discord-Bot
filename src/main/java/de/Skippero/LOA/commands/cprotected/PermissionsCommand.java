package de.Skippero.LOA.commands.cprotected;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cprotected
Created by Skippero
on 18.03.2024 , 15:17

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PermissionsCommand extends LOACommand {
    public PermissionsCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if (event.isFromGuild() && commandGuild != null) {
            String guildName = commandGuild.getId();
            if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.permissions", guildName)) {
                if(event.getOptions().isEmpty() || event.getOptions().size() < 2 || event.getOption("action") == null) {
                    event.reply("Missing arguments").setEphemeral(true).queue();
                    return;
                }
                OptionMapping actionObj = event.getOption("action");
                OptionMapping userObj = event.getOption("user");
                String action = actionObj != null ? actionObj.getAsString() : "";
                Member user = userObj != null ? userObj.getAsMember() : null;

                if(action.isEmpty() || user == null) {
                    event.reply("Input values were found as empty, please report this if it happens again.").setEphemeral(true).queue(m->m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }

                if (event.getOptions().size() == 2) {
                    if(!action.equalsIgnoreCase("list")) {
                        event.reply("Please provide either another action with a permission or List to show the permissions of the given user").setEphemeral(true).queue();
                    } else {
                        List<String> perms = LOABot.getQueryHandler().getPermissionForServer(user.getId(), guildName);
                        if(!perms.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("Permissions from ").append(user.getAsMention()).append(":\n");
                            perms.forEach(perm -> {
                                builder.append("- ").append(perm).append("\n");
                            });
                            event.reply(builder.toString()).setEphemeral(true).queue();
                        }else{
                            event.reply(user.getAsMention() + " has no permissions").setEphemeral(true).queue();
                        }
                        LOABot.log("listed the permissions for "+ user.getEffectiveName() + " on " + guildName);
                    }
                }else if(event.getOptions().size() == 3) {
                    if(user.isOwner() && !event.getMember().getId().equals(user.getId())) {
                        event.reply("You cannot change the permissions of the server-owner").setEphemeral(true).queue();
                        return;
                    }

                    if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("remove")) {
                        event.reply("Please provide one of the given actions: add/remove").setEphemeral(true).queue();
                    }else {
                        String userId = user.getId();
                        OptionMapping permissionObj = event.getOption("permission");
                        String permission = permissionObj != null ? permissionObj.getAsString() : "";

                        if(permission.isEmpty()) {
                            event.reply("Input values were found as empty, please report this if it happens again.").setEphemeral(true).queue(m->m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                            return;
                        }

                        if(action.equalsIgnoreCase("add")) {
                            LOABot.getQueryHandler().insertUserProperty(guildName,userId,permission);
                            event.reply("Added the permission '" + permission + "' to " + user.getAsMention()).setEphemeral(true).queue();
                            LOABot.log("added the permission '" + permission + "' to "+ user.getEffectiveName() + " on " + guildName);
                        }else if(action.equalsIgnoreCase("remove")) {
                            LOABot.getQueryHandler().removeUserProperty(guildName,userId,permission);
                            event.reply("Removed the permission '" + permission + "' from " + user.getAsMention()).setEphemeral(true).queue();
                            LOABot.log("removed the permission '" + permission + "' from "+ user.getEffectiveName() + " on " + guildName);
                        }
                    }
                }
            } else {
                event.reply("You do not have the required permissions to execute this command").setEphemeral(true).queue();
            }
        } else {
            event.reply("Please use this command only on a server").setEphemeral(true).queue();
        }
    }
}
