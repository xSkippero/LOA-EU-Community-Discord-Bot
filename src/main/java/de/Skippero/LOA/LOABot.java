package de.Skippero.LOA;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.Skippero.LOA.config.ConfigManager;
import de.Skippero.LOA.events.OnSlashCommandInteraction;
import de.Skippero.LOA.features.merchants.MerchantManager;
import de.Skippero.LOA.features.states.Server;
import de.Skippero.LOA.features.states.ServerManager;
import de.Skippero.LOA.features.states.State;
import de.Skippero.LOA.sql.QueryHandler;
import de.Skippero.LOA.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.IOException;
import java.util.*;

public class LOABot {

    public static long nextUpdateTimestamp;
    public static Map<User, String> updateNotify;
    private static ConfigManager configManager;
    private static QueryHandler queryHandler;
    private static Multimap<String, String[]> configurations;
    private static Map<String, TextChannel> statusChannels;
    public static Map<String, TextChannel> merchantChannels;
    private static Map<String, TextChannel> pushNotificationChannels;
    private static JDA jda;
    private static int errorCount = 0;

    public static TextChannel test;


    public static void main(String[] args) throws InterruptedException {

        if (args.length < 1) {
            System.err.println("Missing Token on Parameter 1 (Index 0)");
            System.exit(1);
        }

        System.out.println("Starting LOA-EUW-Status-Bot by Skippero");

        MerchantManager.openConnection();

        configManager = new ConfigManager();
        queryHandler = new QueryHandler();
        configurations = ArrayListMultimap.create();

        configurations = queryHandler.loadConfiguration(configurations);

        JDABuilder builder = JDABuilder.createDefault(args[0]);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setAutoReconnect(true);
        builder.setActivity(Activity.watching("LOA-EUW Server-Status"));
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);
        builder.addEventListeners(new OnSlashCommandInteraction());

        jda = builder.build();
        jda.awaitReady();
        jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        jda.upsertCommand("update", "Start the update-script").queue();
        jda.upsertCommand("about", "Prints out information about the bot").queue();
        jda.upsertCommand("reload", "Reload all server-configurations").queue();
        jda.upsertCommand("restart", "Restart the bot").queue();
        jda.upsertCommand("stop", "Stop the bot").queue();
        jda.upsertCommand("debug", "Developer command").queue();
        jda.upsertCommand("config", "Configure the Bot")
                .addOption(OptionType.STRING, "property", "The field you want to change", false)
                .addOption(OptionType.STRING, "value", "The value for the field you want to change", false)
                .setGuildOnly(true).queue();
        jda.upsertCommand("permissions", "Configure Guild permissions for the bot usage")
                .setGuildOnly(true).addOption(OptionType.STRING, "action", "What you want to do (add/remove/list)")
                .addOption(OptionType.USER, "user", "The user you want to affect")
                .addOption(OptionType.STRING, "permission", "The permission you want to add/remove", false).queue();

        System.out.println(" ");
        System.out.println("Bot is active on: ");
        jda.getGuilds().forEach(guild -> {
            System.out.println("- " + guild.getName());
            if (!serverExistsInDB(guild.getId())) {
                queryHandler.createDefaultDataBaseConfiguration(guild.getId());
            }
        });
        System.out.println(" ");

        pushNotificationChannels = new HashMap<>();
        statusChannels = new HashMap<>();
        updateNotify = new HashMap<>();
        merchantChannels = new HashMap<>();

        startTimers(jda);
    }

    private static boolean serverExistsInDB(String name) {
        return configurations.containsKey(name);
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static QueryHandler getQueryHandler() {
        return queryHandler;
    }

    private static void startTimers(JDA jda) {

        Timer restartTimer = new Timer("restartTimer");
        TimerTask restartTask = new TimerTask() {
            public void run() {
                restartBot();
            }
        };
        restartTimer.schedule(restartTask, 24 * 60 * 60 * 1000);


        Timer timer = new Timer("Statustimer");
        long period = 60 * 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                checkServerStatusAndPrintResults();
            }
        };
        timer.schedule(task, 5 * 1000, period);

        Timer timer2 = new Timer("Configtimer");
        long period2 = 2 * 60 * 60 * 1000L;
        TimerTask task2 = new TimerTask() {
            public void run() {
                reloadConfig(jda);
            }
        };
        timer2.schedule(task2, 5 * 1000, period2);
    }

    private static boolean startUp = true;

    private static void reloadConfig(JDA jda) {

        nextUpdateTimestamp = System.currentTimeMillis() + 2 * 60 * 60 * 1000;

        errorCount = 0;

        pushNotificationChannels.clear();
        statusChannels.clear();
        merchantChannels.clear();
        configurations = queryHandler.loadConfiguration(configurations);

        for (Guild guild : jda.getGuilds()) {
            String guildName = guild.getId();
            boolean pushNotifications = false;
            String pushNotificationChannelName = "loa-euw-notify";
            String statusChannelName = "loa-euw-status";
            String merchantChannelName = "loa-euw-merchants";
            for (String[] strings : configurations.get(guildName)) {
                switch (strings[0]) {
                    case "pushNotifications":
                        pushNotifications = Boolean.parseBoolean(strings[1]);
                        break;
                    case "pushChannelName":
                        pushNotificationChannelName = strings[1];
                        break;
                    case "statusChannelName":
                        statusChannelName = strings[1];
                        break;
                    case "merchantChannelName":
                        merchantChannelName = strings[1];
                        break;
                }
            }
            if (pushNotifications) {
                List<TextChannel> _pushChannels = guild.getTextChannelsByName(pushNotificationChannelName, true);
                if (!_pushChannels.isEmpty()) {
                    pushNotificationChannels.put(guildName, _pushChannels.get(0));
                }
            }
            List<TextChannel> _statusChannels = guild.getTextChannelsByName(statusChannelName, true);
            if (!_statusChannels.isEmpty()) {
                statusChannels.put(guildName, _statusChannels.get(0));
            }
            List<TextChannel> _merchantChannels = guild.getTextChannelsByName(merchantChannelName, true);
            if (!_merchantChannels.isEmpty()) {
                if(startUp) {
                    MessageHistory history = _merchantChannels.get(0).getHistory();
                    List<Message> messages;
                    messages = history.retrievePast(100).complete();
                    if(messages.isEmpty() && messages.size() > 1) {
                        _merchantChannels.get(0).deleteMessages(messages).queue();
                    }else if(!messages.isEmpty()) {
                        _merchantChannels.get(0).deleteMessageById(messages.get(0).getId()).queue();
                    }
                }
                merchantChannels.put(guildName, _merchantChannels.get(0));
            }
        }

        updateNotify.forEach((user, s) -> {
            user.openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] Your configuration update for the Discord Server '**" + jda.getGuildById(s).getName() + "**' is now active :smile:")).queue();
        });
        if (!updateNotify.isEmpty()) {
            System.out.println("[" + new Date().toGMTString() + "]" + " Updated configurations on " + updateNotify.size() + " servers");
        }
        updateNotify.clear();
        startUp = false;
    }

    private static String getEmoteForState(State state) {
        switch (state) {
            case FULL:
                return ":x:";
            case BUSY:
                return ":warning:";
            case GOOD:
                return ":white_check_mark:";
            case MAINTENANCE:
                return ":gear:";
        }
        return ":question:";
    }

    public static void pushStateUpdateNotify(String server, State newState) {
        EmbedBuilder eb = new EmbedBuilder();
        switch (newState) {
            case GOOD:
                eb.setColor(MessageColor.GREEN.getColor());
                eb.setDescription(server + " is now online");
                break;
            case BUSY:
                eb.setColor(MessageColor.ORANGE.getColor());
                eb.setDescription(server + " is currently a little bit busy");
                break;
            case FULL:
                eb.setColor(MessageColor.RED.getColor());
                eb.setDescription(server + " is completely full");
                break;
            case MAINTENANCE:
                eb.setColor(MessageColor.CYAN.getColor());
                eb.setDescription(server + " is now in maintenance");
                break;
        }
        long time = System.currentTimeMillis()/1000;
        eb.setTitle(getEmoteForState(newState)  + " Status Update <t:" + time + ">");
        pushNotificationChannels.forEach((s, textChannel) -> {
            if (textChannel.getGuild().getId().equals(s)) {
                textChannel.sendMessageEmbeds(eb.build()).queue();
            }
        });
    }

    private static void checkServerStatusAndPrintResults() {
        getStatus();
        MessageColor majorityStateColor = ServerManager.getStateMajorityColor();
        StringBuilder builder = new StringBuilder();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(majorityStateColor.getColor());
        eb.setTitle("LostARK EUW - Server Status");
        for (Server server : ServerManager.servers) {
            builder.append(server.getName()).append(" ⮕ ").append(getEmoteForState(server.getState())).append(" (").append(server.getStateName()).append(")").append("\n");
        }
        if (ServerManager.servers.isEmpty()) {
            builder.append("All servers are offline");
        }
        eb.setDescription(builder.toString());
        statusChannels.forEach((s, textChannel) -> {
            try {
                if (textChannel.getGuild().getId().equals(s)) {
                    MessageHistory history = new MessageHistory(textChannel);
                    List<Message> messageList = history.retrievePast(20).complete();
                    if (!messageList.isEmpty()) {
                        for (Message message : messageList) {
                            if (message.getAuthor().getIdLong() == 1009381581787504726L) {
                                textChannel.deleteMessageById(message.getId()).queue();
                            }
                        }
                    }
                    textChannel.sendMessageEmbeds(eb.build()).queue();
                }
            } catch (Exception ignored) {
                errorCount++;
                System.out.println("[" + new Date().toGMTString() + "]" + " Discord was not responding (x" + errorCount + ")");
                if (errorCount >= 10) {
                    System.out.println("[" + new Date().toGMTString() + "]" + " Discord error-count was too high, restarting now");
                    restartBot();
                }
            }
        });
    }

    public static void restartBot() {
        try {
            System.out.println("[" + new Date().toGMTString() + "]" + " Restarting Bot...");
            queryHandler.closeConnection();
            Runtime.getRuntime().exec("./restart.sh");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static void getStatus() {
        Website website = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");
        if (website.getDoc() == null) {
            return;
        }
        ServerManager.loadServers();
    }

    public static void manualReload() {
        reloadConfig(jda);
    }
}

