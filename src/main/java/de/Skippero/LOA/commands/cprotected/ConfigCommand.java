package de.Skippero.LOA.commands.cprotected;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.commands.cprotected
Created by Skippero
on 18.03.2024 , 15:15

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.util.concurrent.TimeUnit;

public class ConfigCommand extends LOACommand {
    public ConfigCommand(String commandName) {
        super(commandName);
    }

    @Override
    public void execute(String commandName, User commandExecutor, Guild commandGuild, MessageChannel commandChannel, SlashCommandInteractionEvent event) {
        if (event.isFromGuild() && commandGuild != null) {
            if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.config", commandGuild.getId())) {
                if (event.getOptions().isEmpty()) {
                    event.reply("You entered the configuration menu\n" + "Usage:\n" + "/config <property> <value>\n\n" + "Properties:\n" + "pushNotifications: <'true','false'>\n" + "pushChannelName: <'value'>\n" + "statusChannelName: <'value'>").setEphemeral(true).queue();
                } else if (event.getOptions().size() == 1) {
                    event.reply("You are missing an argument").setEphemeral(true).queue();
                } else if (event.getOptions().size() == 2) {
                    OptionMapping propertyObj = event.getOption("property");
                    OptionMapping valueObj = event.getOption("value");
                    String property = propertyObj == null ? "" : propertyObj.getAsString();
                    String value = valueObj == null ? "" : valueObj.getAsString();

                    if(property.isEmpty() || value.isEmpty()) {
                        event.reply("Input values were found as empty, please report this if it happens again.").setEphemeral(true).queue(m->m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                        return;
                    }

                    if (!property.equals("pushNotifications") && !property.equals("pushChannelName") && !property.equals("statusChannelName") && !property.equals("merchantChannelName")) {
                        event.reply("Please provide a Property from the List").setEphemeral(true).queue();
                        return;
                    }
                    if (property.equals("pushNotifications")) {
                        if (!value.equals("true") && !value.equals("false")) {
                            event.reply("Please provide true or false").setEphemeral(true).queue();
                            return;
                        }
                    }
                    LOABot.getQueryHandler().updateProperty(commandGuild.getId(), property, value);
                    event.reply("You updated the setting, the changes will take effect when the config-reload happens (in approximately " + Math.round((LOABot.nextUpdateTimestamp - System.currentTimeMillis()) / 1000D / 60D) + " minutes)").setEphemeral(true).queue();
                    LOABot.updateNotify.put(event.getUser(), commandGuild.getId());
                    LOABot.log("updated " + property + " to " + value + " on " + commandGuild.getName());
                }
            } else {
                event.reply("You do not have the required permissions to execute this command").setEphemeral(true).queue();
            }
        } else {
            event.reply("Please use this command only on a server").setEphemeral(true).queue();
        }
    }
}
