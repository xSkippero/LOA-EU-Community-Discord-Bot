package de.Skippero.LOA.events;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.features.merchants.*;
import de.Skippero.LOA.utils.MessageColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.*;

public class OnSlashCommandInteraction extends ListenerAdapter {

    private final Map<String, Long> timer = new HashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equalsIgnoreCase("ping")) {
            long time = System.currentTimeMillis();
            event.reply("Pong!").setEphemeral(true).flatMap(v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)).queue();
            System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " executed " + "/ping");
        } else if (event.getName().equalsIgnoreCase("debug")) {
            if (event.getUser().getIdLong() != 397006908424454147L) {
                return;
            }
            long time = System.currentTimeMillis()/1000;
            event.reply("<t:" + time + ">").queue();
            if(event.getGuild() != null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(MessageColor.GREEN.getColor());
                eb.setDescription("Nia" + " is now online");
                eb.setTitle(":white_check_mark:" + " Status Update <t:" + time + ">");
                event.getMessageChannel().sendMessageEmbeds(eb.build()).queue();

                MerchantItem card = new MerchantItem("Wei",MerchantItemType.CARD, MerchantItemRarity.LEGENDARY,"Card for the 'Light of Salvation' (+15% DMG in total) Cardset");
                MerchantItem rapport = new MerchantItem("TEST",MerchantItemType.RAPPORT,MerchantItemRarity.EPIC);
                Merchant merchant = new Merchant("TEST","TEST-ENTRY","Battlebound_Plains",rapport,card);
                MerchantManager.sendMerchantUpdate(merchant,true, false, event.getGuild().getJDA().getTextChannelById(event.getMessageChannel().getId()));

            }
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
            event.reply("This bot checks the status page of LostARK (EU) at predefined intervals and displays any changes in a Discord channel\n" + "Bot by Skippero, v. " + LOABot.botVersion + "\n" + "https://github.com/xSkippero/LOA-EUW-Status-Discord-Bot-").setEphemeral(true).queue();
            System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " executed " + "/about");
        } else if (event.getName().equalsIgnoreCase("config")) {
            if (event.isFromGuild()) {
                if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.config", event.getGuild().getId())) {
                    if (event.getOptions().isEmpty()) {
                        event.reply("You entered the Configuration Menu\n" + "Usage:\n" + "/config <Property> <Value>\n\n" + "Properties:\n" + "pushNotifications: <'true','false'>\n" + "pushChannelName: <'value'>\n" + "statusChannelName: <'value'>\n" + "merchantChannelName: <'value'>").setEphemeral(true).queue();
                    } else if (event.getOptions().size() == 1) {
                        event.reply("You are missing on Argument").setEphemeral(true).queue();
                    } else if (event.getOptions().size() == 2) {
                        String property = event.getOption("property").getAsString();
                        String value = event.getOption("value").getAsString();
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
                        LOABot.getQueryHandler().updateProperty(event.getGuild().getId(), property, value);
                        event.reply("You updated the setting, the changes will take effect when the Config-Reload happens (in approximately " + Math.round((LOABot.nextUpdateTimestamp - System.currentTimeMillis()) / 1000D / 60D) + " minutes)").setEphemeral(true).queue();
                        LOABot.updateNotify.put(event.getUser(), event.getGuild().getId());
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
                String guildName = event.getGuild().getId();
                if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.permissions", guildName)) {
                    if(event.getOptions().isEmpty() || event.getOptions().size() < 2 || event.getOption("action") == null) {
                        event.reply("Missing arguments").setEphemeral(true).queue();
                        return;
                    }
                    String action = event.getOption("action").getAsString();
                    Member user = event.getOption("user").getAsMember();
                    if (event.getOptions().size() == 2) {
                        if(!action.equalsIgnoreCase("list")) {
                            event.reply("Please provide either another Action with a Permission or List to show the permissions of the given user").setEphemeral(true).queue();
                        } else {
                            List<String> perms = LOABot.getQueryHandler().getPermissionForServer(user.getId(),guildName);
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
                            System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " listed the permissions from "+ user.getEffectiveName() + " on " + guildName);
                        }
                    }else if(event.getOptions().size() == 3) {

                        if(user.isOwner() && !event.getMember().getId().equals(user.getId())) {
                            event.reply("You cannot change the permissions of the server-owner").setEphemeral(true).queue();
                            return;
                        }

                        if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("remove")) {
                            event.reply("Please provide one of the given Actions: add/remove").setEphemeral(true).queue();
                        }else {
                            String userId = user.getId();
                            String permission = event.getOption("permission").getAsString();

                            if(action.equalsIgnoreCase("add")) {
                                LOABot.getQueryHandler().insertUserProperty(guildName,userId,permission);
                                event.reply("Added the permission '" + permission + "' to " + user.getAsMention()).setEphemeral(true).queue();
                                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " added the permission '" + permission + "' to "+ user.getEffectiveName() + " on " + guildName);
                            }else if(action.equalsIgnoreCase("remove")) {
                                LOABot.getQueryHandler().removeUserProperty(guildName,userId,permission);
                                event.reply("Removed the permission '" + permission + "' from " + user.getAsMention()).setEphemeral(true).queue();
                                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " removed the permission '" + permission + "' from "+ user.getEffectiveName() + " on " + guildName);
                            }
                        }
                    }
                } else {
                    event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
                    System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/permissions");
                }
            } else {
                event.reply("Please use this command only on a Server").setEphemeral(true).queue();
                System.out.println("[" + new Date().toGMTString() + "]" + " " + event.getUser().getName() + " tried to execute " + "/permissions via PM");
            }
        }else if(event.getName().equalsIgnoreCase("vendor")) {

            if(!event.isGlobalCommand()) {
                event.reply("Please use this command only via PM").setEphemeral(true).queue();
                return;
            }

            if(event.getOptions().isEmpty() || event.getOptions().size() < 2 || event.getOption("action") == null) {
                event.reply("Missing arguments").setEphemeral(true).queue();
                return;
            }

            String action = event.getOption("action").getAsString();
            int cardid = event.getOption("cardid").getAsInt();

            List<Integer> userSelection = LOABot.getQueryHandler().getSelectedCardsForUser(event.getUser().getId());

            if(event.getOptions().size() == 2 && action.equalsIgnoreCase("server")) {
                switch (cardid) {
                    case 0:
                        LOABot.getQueryHandler().insertUserVendorProperty(event.getUser().getId(),-1);
                        event.reply("Server selected ⮕ Ealyn").setEphemeral(true).queue();
                        LOABot.ealynUsers.add(event.getUser());
                        break;
                    case 1:
                        LOABot.getQueryHandler().insertUserVendorProperty(event.getUser().getId(),-2);
                        event.reply("Server selected ⮕ Nia").setEphemeral(true).queue();
                        LOABot.niaUsers.add(event.getUser());
                        break;
                }
                return;
            }

            if(!userSelection.contains(-1) && !userSelection.contains(-2)) {
                event.reply("You did not register your Server yet, please provide this command with the correct Id ⮕ '/vendor server 0/1'\n"+"0 ⮕ Ealyn\n" + "1 ⮕ Nia").setEphemeral(true).queue();
                event.reply("").setEphemeral(true).queue();
            }

            if (event.getOptions().size() == 1) {
                if(!action.equalsIgnoreCase("show") && !action.equalsIgnoreCase("list")) {
                    event.reply("Please provide either another action with a cardId or use 'show' to show a list of available cards to select").setEphemeral(true).queue();
                } else {
                    if(action.equalsIgnoreCase("show")) {
                        StringBuilder builder = new StringBuilder();
                        int i = 0;
                        for (MerchantItem value : MerchantManager.allCardItems.values()) {
                            builder.append("Id: ").append(i).append(" ⮕ ").append(value.getName()).append(" (").append(value.getRarity().getDisplayName()).append(")").append("\n");
                            i++;
                        }
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setColor(MessageColor.getRandom().getColor());
                        embedBuilder.setDescription(builder.toString());
                        embedBuilder.setTitle("LostMerchants Card-list");
                        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    }else if(action.equalsIgnoreCase("list")) {
                        if(userSelection.isEmpty()) {
                            event.reply("You don't have any selected Cards").setEphemeral(true).queue();
                            return;
                        }
                        StringBuilder builder = new StringBuilder();
                        List<MerchantItem> items = new ArrayList<>(MerchantManager.allCardItems.values());
                        for (Integer integer : userSelection) {
                            MerchantItem value = items.get(integer);
                            builder.append("Id: ").append(integer).append(" ⮕ ").append(value.getName()).append(" (").append(value.getRarity().getDisplayName()).append(")").append("\n");
                        }
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setColor(MessageColor.getRandom().getColor());
                        embedBuilder.setDescription(builder.toString());
                        embedBuilder.setTitle("LostMerchants Selected-Card-list");
                        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                    }
                }
            }else if(event.getOptions().size() >= 2) {
                if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("remove")) {
                    event.reply("Please provide one of the given actions: add/remove").setEphemeral(true).queue();
                }else {
                    String userId = event.getUser().getId();
                    int server = LOABot.getQueryHandler().getServerForCardUser(userId);
                    if(action.equalsIgnoreCase("add")) {
                        if(!userSelection.contains(cardid)) {
                            LOABot.getQueryHandler().insertUserVendorProperty(userId, cardid);
                            updateUserVendorNotifications(event.getUser());
                            event.reply("Added the card with the id '" + cardid + "' to your notifications").setEphemeral(true).queue();
                            switch (server) {
                                case -1:
                                    if(!LOABot.neededCardIndexesEayln.contains(cardid)) {
                                        LOABot.neededCardIndexesEayln.add(cardid);
                                    }
                                    break;
                                case -2:
                                    if(!LOABot.neededCardIndexesNia.contains(cardid)) {
                                        LOABot.neededCardIndexesNia.add(cardid);
                                    }
                                    break;
                            }
                        }else{
                            event.reply("You already have that card selected").setEphemeral(true).queue();
                        }
                    }else if(action.equalsIgnoreCase("remove")) {
                        if(userSelection.contains(cardid)) {
                            LOABot.getQueryHandler().removeUserVendorProperty(userId, cardid);
                            updateUserVendorNotifications(event.getUser());
                            event.reply("Removed the card with the id '" + cardid + "' from your notifications").setEphemeral(true).queue();
                        }else{
                            event.reply("You do not have that card selected").setEphemeral(true).queue();
                        }
                    }
                }
            }
        }
    }

    private void updateUserVendorNotifications(User user) {
        LOABot.userCardNotifications.put(user,LOABot.getQueryHandler().getSelectedCardsForUser(user.getId()));
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
