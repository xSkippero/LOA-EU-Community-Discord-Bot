package de.Skippero.LOA.events;

import de.Skippero.LOA.LOABot;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class OnSlashCommandInteraction extends ListenerAdapter {

    private final Map<String, Long> timer = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("ping")) {
            long time = System.currentTimeMillis();
            event.reply("Pong!").setEphemeral(true).flatMap(v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)).queue();
            System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " executed " + "/ping");
        } else if (event.getName().equalsIgnoreCase("update")) {
            if (event.getUser().getIdLong() != 397006908424454147L) {
                event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " tried to execute " + "/update");
            } else {
                String id = event.getUser().getId();
                if (timer.containsKey(id) && timer.get(id) >= System.currentTimeMillis()) {
                    timer.remove(id);
                    event.reply("The Bot is updating itself, restarting soon...").setEphemeral(true).queue();
                    System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " updated the Bot");
                    runUpdateScriptAsync();
                } else if (timer.containsKey(id) && timer.get(id) < System.currentTimeMillis()) {
                    sendConfirm(event);
                } else if (!timer.containsKey(id)) {
                    sendConfirm(event);
                }
            }
        }else if(event.getName().equalsIgnoreCase("about")) {
            event.reply("This bot checks the status page of LostARK (EU) at predefined intervals and displays any changes in a Discord channel\n" + "Bot by Skippero, v. 0.2\n" + "https://github.com/xSkippero/LOA-EUW-Status-Discord-Bot-").setEphemeral(true).queue();
            System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " executed " + "/about");
        }else if(event.getName().equalsIgnoreCase("config")) {
            if(event.isFromGuild()) {
                if(event.getMember() != null && event.getMember().isOwner()) {
                    if(event.getOptions().isEmpty()) {
                        event.reply("You entered the Configuration Menu\n" + "Usage:\n" + "/config <Property> <Value>\n\n" + "Properties:\n" + "pushNotifications: <'true','false'>\n" + "pushChannelName: <'value'>\n" + "statusChannelName: <'value'>").setEphemeral(true).queue();
                    }else if(event.getOptions().size() == 1){
                        event.reply("You are missing on Argument").setEphemeral(true).queue();
                    }else if(event.getOptions().size() == 2) {
                        String property = event.getOption("property").getAsString();
                        String value = event.getOption("value").getAsString();
                        if(!property.equals("pushNotifications") && !property.equals("pushChannelName") && !property.equals("statusChannelName")) {
                            event.reply("Please provide a Property from the List").setEphemeral(true).queue();
                            return;
                        }
                        if(property.equals("pushNotifications")) {
                            if(!value.equals("true") && !value.equals("false")) {
                                event.reply("Please provide true or false").setEphemeral(true).queue();
                                return;
                            }
                        }
                        LOABot.getQueryHandler().updateProperty(event.getGuild().getName(),property,value);
                        event.reply("You updated the setting, the changes will take effect when the Config-Reload happens (every 2 hours)").setEphemeral(true).queue();
                        LOABot.updateNotify.put(event.getUser(),event.getGuild().getName());
                        System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " updated " + property + " to " + value + " on " + event.getGuild().getName());
                    }
                }else{
                    event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                    System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " tried to execute " + "/config");
                }
            }else{
                event.reply("Please use this command only on a Server").setEphemeral(true).queue();
                System.out.println("["+new Date().toGMTString()+"]" + " " + event.getUser().getName() + " tried to execute " + "/config via PM");
            }
            
        }
    }

    public void sendConfirm(SlashCommandInteractionEvent e) {
        timer.put(e.getUser().getId(), System.currentTimeMillis() + 3000);
        e.reply("Please confirm your choice to Update the Bot in the next 3 seconds, to do that enter the Command again").setEphemeral(true).queue();
    }

    public void runUpdateScriptAsync() {
        try {
            Runtime.getRuntime().exec("./updateInScreen.sh");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
