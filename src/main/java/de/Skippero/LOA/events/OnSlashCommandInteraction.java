package de.Skippero.LOA.events;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.features.merchants.*;
import de.Skippero.LOA.features.merchants.receiver.RawActiveMerchant;
import de.Skippero.LOA.utils.MessageColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.*;

public class OnSlashCommandInteraction extends ListenerAdapter {

    private final Map<String, Long> timer = new HashMap<>();

    private File readLogFile() {
        return new File("bot.log");
    }

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
            if(!event.isFromGuild()) {
                runDebug(event.getUser());
                event.reply("Debug performed!").setEphemeral(true).queue();
                return;
            }
            event.reply("Sending log").addFiles(FileUpload.fromData(readLogFile())).setEphemeral(true).queue();
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

            List<Integer> userSelection = LOABot.getQueryHandler().getSelectedCardsForUser(event.getUser().getId());

            if(LOABot.getQueryHandler().getServerForCardUser(event.getUser().getId()) == -3) {
                event.reply("You did not register your Server yet, please choose one down below").setActionRow(niaButton,ealynButton).setEphemeral(true).queue();
                return;
            }

            String action = "";
            OptionMapping actionPayLoad = event.getOption("action");
            if(actionPayLoad != null) {
                action = actionPayLoad.getAsString();
            }

            if (event.getOptions().size() != 2) {
                event.reply("Usage:\n" +
                        "Show all available Cards ⮕ [All Cards]\n" +
                        "Show your selected Cards ⮕ [Your Cards]\n" +
                        "Clear your selected Cards ⮕ [Clear Cards]\n" +
                        "Add or remove selected Cards ⮕ /vendor <add/remove> <cardId>\n").setEphemeral(true).addActionRow(showButton,listButton,clearButton).queue();
            }else if(event.getOptions().size() >= 2) {
                if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("remove")) {
                    event.reply("Please provide one of the given actions: add/remove").setEphemeral(true).queue();
                }else {
                    int cardId = -999;
                    OptionMapping idPayLoad = event.getOption("cardid");
                    if(idPayLoad != null) {
                        cardId = idPayLoad.getAsInt();
                    }

                    if(cardId == -999) {
                        event.reply("Please provide a Id").setEphemeral(true).queue();
                        return;
                    }

                    String userId = event.getUser().getId();
                    int server = LOABot.getQueryHandler().getServerForCardUser(userId);
                    if(action.equalsIgnoreCase("add")) {
                        if(!userSelection.contains(cardId)) {
                            LOABot.getQueryHandler().insertUserVendorProperty(userId, cardId);
                            updateUserVendorNotifications(event.getUser());
                            event.reply("Added the card with the id '" + cardId + "' (" + new ArrayList<>(MerchantManager.allCardItems.values()).get(cardId).getName() + ") to your notifications").setEphemeral(true).queue();
                            LOABot.userCardNotifications.put(event.getUser().getId(), LOABot.getQueryHandler().getSelectedCardsForUser(userId));
                            switch (server) {
                                case -1:
                                    if(!LOABot.neededCardIndexesEayln.contains(cardId)) {
                                        LOABot.neededCardIndexesEayln.add(cardId);
                                    }
                                    break;
                                case -2:
                                    if(!LOABot.neededCardIndexesNia.contains(cardId)) {
                                        LOABot.neededCardIndexesNia.add(cardId);
                                    }
                                    break;
                            }
                        }else{
                            event.reply("You already have that card selected").setEphemeral(true).queue();
                        }
                    }else if(action.equalsIgnoreCase("remove")) {
                        if(userSelection.contains(cardId)) {
                            LOABot.getQueryHandler().removeUserVendorProperty(userId, cardId);
                            updateUserVendorNotifications(event.getUser());
                            LOABot.userCardNotifications.put(event.getUser().getId(), LOABot.getQueryHandler().getSelectedCardsForUser(userId));
                            event.reply("Removed the card with the id '" + cardId + "' (" + new ArrayList<>(MerchantManager.allCardItems.values()).get(cardId).getName() + ") from your notifications").setEphemeral(true).queue();
                        }else{
                            event.reply("You do not have that card selected").setEphemeral(true).queue();
                        }
                    }
                }
            }
        }else if(event.getName().equalsIgnoreCase("survey")) {
            String title = event.getOption("title").getAsString();
            String description = event.getOption("description").getAsString();

            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(MessageColor.getRandom().getColor());
            builder.setTitle(title);
            builder.setDescription(description);
            builder.setTimestamp(Instant.ofEpochMilli(System.currentTimeMillis()));

            Button joinButton = Button.success("join","Join");
            Button leaveButton = Button.danger("leave","Leave");
            Button delButton = Button.danger("del","Delete");

            event.getChannel().sendMessageEmbeds(builder.build()).setActionRow(joinButton,leaveButton,delButton).queue();
        }
    }

    private final Map<Long, List<User>> surveys = new HashMap<>();

    Button niaButton = Button.primary("server-nia","Nia");
    Button ealynButton = Button.primary("server-ealyn","Ealyn");

    Button showButton = Button.primary("btn-show","All Cards");
    Button listButton = Button.primary("btn-list","Your Cards");
    Button clearButton = Button.danger("btn-clear","Clear Cards");

    private void joinOrLeaveSurvey(Message message, ButtonInteractionEvent event, boolean joining) {
        if(!surveys.containsKey(message.getIdLong())) {
            List<User> userList = new ArrayList<User>();
            if(joining) {
                userList.add(event.getUser());
            }
            surveys.put(message.getIdLong(),userList);
        }else{
            List<User> userList = surveys.get(message.getIdLong());
            if(joining) {
                if(!userList.contains(event.getUser())) {
                    userList.add(event.getUser());
                }
            }else{
                userList.remove(event.getUser());
            }
            surveys.put(message.getIdLong(),userList);
        }
        List<User> userList = surveys.get(message.getIdLong());
        MessageEmbed messageEmbed = message.getEmbeds().get(0);
        EmbedBuilder embedBuilder = new EmbedBuilder(messageEmbed);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Players joining: ");
        for (User user : userList) {
            stringBuilder.append(user.getName()).append(", ");
        }
        String joiningPlayers = stringBuilder.toString();
        embedBuilder.setFooter(joiningPlayers.substring(joiningPlayers.length()-2));
        message.editMessageEmbeds().queue();
    }

    public void onButtonInteraction(ButtonInteractionEvent event) {

        List<Integer> userSelection = LOABot.getQueryHandler().getSelectedCardsForUser(event.getUser().getId());

        if(event.getButton().getId() != null) {
            Message message = event.getMessage();
            switch (event.getButton().getId()) {
                case "leave":
                    joinOrLeaveSurvey(message,event,false);
                    break;
                case "join":
                    joinOrLeaveSurvey(message,event,true);
                    break;
                case "del":
                    event.getInteraction().getMessage().delete().queue();
                    break;
                case "server-nia":
                    LOABot.getQueryHandler().insertUserVendorProperty(event.getUser().getId(),-2);
                    event.reply("Server selected ⮕ Nia").setEphemeral(true).queue();
                    LOABot.niaUsers.add(event.getUser().getId());
                    break;
                case "server-ealyn":
                    LOABot.getQueryHandler().insertUserVendorProperty(event.getUser().getId(),-1);
                    event.reply("Server selected ⮕ Ealyn").setEphemeral(true).queue();
                    LOABot.ealynUsers.add(event.getUser().getId());
                    break;
                case "btn-show":
                        StringBuilder builder = new StringBuilder();
                        ArrayList<MerchantItem> list = new ArrayList<>(MerchantManager.allCardItems.values());
                        Map<String, Integer> indexes = new HashMap<>();
                        int i = 0;
                        for (MerchantItem merchantItem : list) {
                            indexes.put(merchantItem.getName(),i);
                            i++;
                        }
                        list.sort(Comparator.comparing(MerchantItem::getName));
                        for (MerchantItem value : list) {
                            builder.append(getEmoteForRarity(value.getRarity())).append(" Id: ").append(indexes.get(value.getName())).append(" ⮕ ").append(value.getName()).append("\n");
                        }
                        EmbedBuilder embedBuilder = new EmbedBuilder();
                        embedBuilder.setColor(MessageColor.getRandom().getColor());
                        embedBuilder.setDescription(builder.toString());
                        embedBuilder.setTitle("LostMerchants Card-list");
                        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
                        break;
                case "btn-list":
                    if(userSelection.isEmpty()) {
                        event.reply("You don't have any selected Cards").setEphemeral(true).queue();
                        return;
                    }
                    StringBuilder builder2 = new StringBuilder();
                    List<MerchantItem> items = new ArrayList<>(MerchantManager.allCardItems.values());
                    for (Integer integer : userSelection) {
                        MerchantItem value = items.get(integer);
                        builder2.append(getEmoteForRarity(value.getRarity())).append(" Id: ").append(integer).append(" ⮕ ").append(value.getName()).append("\n");
                    }
                    EmbedBuilder embedBuilder2 = new EmbedBuilder();
                    embedBuilder2.setColor(MessageColor.getRandom().getColor());
                    embedBuilder2.setDescription(builder2.toString());
                    embedBuilder2.setTitle("LostMerchants Selected-Card-list");
                    event.replyEmbeds(embedBuilder2.build()).setEphemeral(true).queue();
                    break;
                case "btn-clear":
                    LOABot.getQueryHandler().clearUserVendorCards(event.getUser().getId());
                    event.reply("Cleared your notification list").setEphemeral(true).queue();
                    break;
            }
        }
    }

    private String getEmoteForRarity(MerchantItemRarity rarity) {
        switch (rarity) {
            case UNCOMMON:
                return ":green_square:";
            case RARE:
                return ":blue_square:";
            case EPIC:
                return ":purple_square:";
            case LEGENDARY:
                return ":yellow_square:";
        }
        return ":question:";
    }

    public static void runDebug(User user) {
        RawActiveMerchant activeMerchant = new RawActiveMerchant();
        activeMerchant.setName("Mac ");
        activeMerchant.setZone("Bitterwind_Hill");
        MerchantItem item = new MerchantItem("Wei",MerchantItemType.CARD,MerchantItemRarity.LEGENDARY,"Vendor in Anikka");
        MerchantManager.sendPrivateNotification(MerchantItemRarity.LEGENDARY,activeMerchant,item,user.getId());
    }

    private void updateUserVendorNotifications(User user) {
        LOABot.userCardNotifications.put(user.getId(),LOABot.getQueryHandler().getSelectedCardsForUser(user.getId()));
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
