package de.Skippero.LOA.events;

import de.Skippero.LOA.LOABot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnSlashCommandInteraction extends ListenerAdapter {

    private final Map<String, Long> timer = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("ping")) {
            long time = System.currentTimeMillis();
            event.reply("Pong!").setEphemeral(true).flatMap(v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)).queue();
            System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " executed " + "/ping");
        } else if (event.getName().equalsIgnoreCase("update")) {
            if (event.getUser().getIdLong() != 397006908424454147L) {
                event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/update");
            } else {
                String id = event.getUser().getId();
                if (timer.containsKey(id) && timer.get(id) >= System.currentTimeMillis()) {
                    timer.remove(id);
                    event.reply("The Bot is updating itself, restarting soon...").setEphemeral(true).queue();
                    System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " updated the Bot");
                    runUpdateScriptAsync();
                } else if (timer.containsKey(id) && timer.get(id) < System.currentTimeMillis()) {
                    sendConfirm(event);
                } else if (!timer.containsKey(id)) {
                    sendConfirm(event);
                }
            }
        } else if (event.getName().equals("reload")) {
            if (event.getUser().getIdLong() != 397006908424454147L) {
                event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/update");
            } else {
                event.reply("Reloading Configs...").setEphemeral(true).queue();
                LOABot.manualReload();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " updated the server-configurations via /reload");
            }
        } else if (event.getName().equals("restart")) {
            if (event.getUser().getIdLong() != 397006908424454147L) {
                event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/restart");
            } else {
                event.reply("Restarting...").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " restarted the bot via /restart");
                LOABot.restartBot();
            }
        } else if (event.getName().equals("stop")) {
            if (event.getUser().getIdLong() != 397006908424454147L) {
                event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/stop");
            } else {
                event.reply("Stopping...").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " stopped the bot via /stop");
                LOABot.getQueryHandler().closeConnection();
                System.exit(0);
            }
        } else if (event.getName().equalsIgnoreCase("about")) {
            event.reply("This bot checks the status page of LostARK (EU) at predefined intervals and displays any changes in a Discord channel\n" + "Bot by Skippero, v. 0.2\n" + "https://github.com/xSkippero/LOA-EUW-Status-Discord-Bot-").setEphemeral(true).queue();
            System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " executed " + "/about");
        } else if (event.getName().equalsIgnoreCase("config")) {
            if (event.isFromGuild()) {
                if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.config", event.getGuild().getName())) {
                    if (event.getOptions().isEmpty()) {
                        event.reply("You entered the Configuration Menu\n" + "Usage:\n" + "/config <Property> <Value>\n\n" + "Properties:\n" + "pushNotifications: <'true','false'>\n" + "pushChannelName: <'value'>\n" + "statusChannelName: <'value'>").setEphemeral(true).queue();
                    } else if (event.getOptions().size() == 1) {
                        event.reply("You are missing on Argument").setEphemeral(true).queue();
                    } else if (event.getOptions().size() == 2) {
                        String property = event.getOption("property").getAsString();
                        String value = event.getOption("value").getAsString();
                        if (!property.equals("pushNotifications") && !property.equals("pushChannelName") && !property.equals("statusChannelName")) {
                            event.reply("Please provide a Property from the List").setEphemeral(true).queue();
                            return;
                        }
                        if (property.equals("pushNotifications")) {
                            if (!value.equals("true") && !value.equals("false")) {
                                event.reply("Please provide true or false").setEphemeral(true).queue();
                                return;
                            }
                        }
                        LOABot.getQueryHandler().updateProperty(event.getGuild().getName(), property, value);
                        event.reply("You updated the setting, the changes will take effect when the Config-Reload happens (in approximately " + Math.round((LOABot.nextUpdateTimestamp - System.currentTimeMillis()) / 1000D / 60D) + " minutes)").setEphemeral(true).queue();
                        LOABot.updateNotify.put(event.getUser(), event.getGuild().getName());
                        System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " updated " + property + " to " + value + " on " + event.getGuild().getName());
                    }
                } else {
                    event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                    System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/config");
                }
            } else {
                event.reply("Please use this command only on a Server").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/config via PM");
            }
        } else if (event.getName().equalsIgnoreCase("permissions")) {
            if (event.isFromGuild()) {
                String guildName = event.getGuild().getName();
                if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.permissions", guildName)) {
                    String action = event.getOption("action").getAsString();
                    Member user = event.getOption("user").getAsMember();
                    if (event.getOptions().size() == 2) {
                        if(!action.equalsIgnoreCase("list")) {
                            event.reply("Please provide either another Action with a Permission or List to show the permissions of the given user").setEphemeral(true).queue();
                        } else {
                            List<String> perms = LOABot.getQueryHandler().getPermissionForServer(user.getId(),guildName);
                            if(!perms.isEmpty()) {
                                StringBuilder builder = new StringBuilder();
                                builder.append("Permissions from ").append(user.getNickname()).append(":\n");
                                perms.forEach(perm -> {
                                    builder.append("- ").append(perm).append("\n");
                                });
                                event.reply(builder.toString()).setEphemeral(true).queue();
                            }else{
                                event.reply("The given user has no permissions").setEphemeral(true).queue();
                            }
                            System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " listed the permissions from "+ user.getNickname() + " on " + guildName);
                        }
                    }else if(event.getOptions().size() == 3) {
                        if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("remove")) {
                            event.reply("Please provide one of the given Actions: add/remove").setEphemeral(true).queue();
                        }else {
                            String userId = user.getId();
                            String permission = event.getOption("permission").getAsString();

                            if(action.equalsIgnoreCase("add")) {
                                LOABot.getQueryHandler().insertUserProperty(guildName,userId,permission);
                                event.reply("Added the permission '" + permission + "' to " + user.getNickname()).setEphemeral(true).queue();
                                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " added the permission '" + permission + "' to "+ user.getNickname() + " on " + guildName);
                            }else if(action.equalsIgnoreCase("remove")) {
                                LOABot.getQueryHandler().removeUserProperty(guildName,userId,permission);
                                event.reply("Removed the permission '" + permission + "' from " + user.getNickname()).setEphemeral(true).queue();
                                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " removed the permission '" + permission + "' from "+ user.getNickname() + " on " + guildName);
                            }
                        }
                    }
                } else {
                    event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                    System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/config");
                }
            } else {
                event.reply("Please use this command only on a Server").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/config via PM");
            }
        }
    }

    public void sendConfirm(SlashCommandInteractionEvent e) {
        timer.put(e.getUser().getId(), System.currentTimeMillis() + 3000);
        e.reply("Please confirm your choice to Update the Bot in the next 3 seconds, to do that enter the Command again").setEphemeral(true).queue();
    }

    public void runUpdateScriptAsync() {
        try {
            LOABot.getQueryHandler().closeConnection();
            Runtime.getRuntime().exec("./updateInScreen.sh");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
