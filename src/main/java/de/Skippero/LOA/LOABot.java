package de.Skippero.LOA;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.Skippero.LOA.config.ConfigManager;
import de.Skippero.LOA.events.OnSlashCommandInteraction;
import de.Skippero.LOA.sql.QueryHandler;
import de.Skippero.LOA.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LOABot {

    private static ConfigManager configManager;
    private static QueryHandler queryHandler;
    private static Multimap<String, String[]> configurations;
    private static List<TextChannel> statusChannels;
    private static List<TextChannel> pushNotificationChannels;

    public static long nextUpdateTimestamp;
    public static Map<User,String> updateNotify;

    public static void main(String[] args) throws LoginException, InterruptedException {

        if (args.length < 1) {
            System.err.println("Missing Token on Parameter 1 (Index 0)");
            System.exit(1);
        }

        System.out.println("Starting LOA-EUW-Status-Bot by Skippero");

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

        JDA jda = builder.build();
        jda.awaitReady();
        jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        jda.upsertCommand("update", "Start the update-script").queue();
        jda.upsertCommand("about", "Prints out information about the bot").queue();
        jda.upsertCommand("config", "Configure the Bot").addOption(OptionType.STRING,"property","The field you want to change",false).addOption(OptionType.STRING,"value","The value for the field you want to change",false).queue();

        System.out.println(" ");
        System.out.println("Bot is active on: ");
        jda.getGuilds().forEach(guild -> {
            System.out.println("- " + guild.getName());
            if(!serverExistsInDB(guild.getName())) {
                queryHandler.createDefaultDataBaseConfiguration(guild.getName());
            }
        });
        System.out.println(" ");

        pushNotificationChannels = new ArrayList<>();
        statusChannels = new ArrayList<>();
        updateNotify = new HashMap<>();

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
        Timer timer = new Timer("Statustimer");
        long period = 60 * 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                checkServerStatusAndPrintResults();
            }
        };
        timer.schedule(task, 5*1000,period);

        Timer timer2 = new Timer("Configtimer");
        long period2 = 2 * 60 * 60 * 1000L;
        TimerTask task2 = new TimerTask() {
            public void run() {
                reloadConfig(jda);
            }
        };
        timer2.schedule(task2, 5*1000,period2);
    }

    private static void reloadConfig(JDA jda) {

        nextUpdateTimestamp = System.currentTimeMillis()+2*60*60*1000;

        errorCount = 0;

        pushNotificationChannels.clear();
        statusChannels.clear();
        configurations = queryHandler.loadConfiguration(configurations);

        for (Guild guild : jda.getGuilds()) {
            String guildName = guild.getName();
            boolean pushNotifications = false;
            String pushNotificationChannelName = "loa-euw-notify";
            String statusChannelName = "loa-euw-status";
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
                }
            }
            if(pushNotifications) {
                List<TextChannel> _pushChannels = jda.getTextChannelsByName(pushNotificationChannelName, true);
                if(!_pushChannels.isEmpty()) {
                    pushNotificationChannels.add(_pushChannels.get(0));
                }
            }
            List<TextChannel> _statusChannels = jda.getTextChannelsByName(statusChannelName, true);
            if(!_statusChannels.isEmpty()) {
                statusChannels.add(_statusChannels.get(0));
            }
        }

        updateNotify.forEach((user, s) -> {
            user.openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] Your configuration update for the Discord Server '**" + s + "**' is now active :smile:")).queue();
        });
        if(!updateNotify.isEmpty()) {
            System.out.println("["+new Date().toGMTString()+"]" + " Updated configurations on " + updateNotify.size() + " servers");
        }
        updateNotify.clear();
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
        SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        dt.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        eb.setTitle(getEmoteForState(newState) +  " Status Update " + dt.format(date));
        pushNotificationChannels.forEach(textChannel -> textChannel.sendMessageEmbeds(eb.build()).queue());
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
        if(ServerManager.servers.isEmpty()) {
            builder.append("All servers are offline");
        }
        eb.setDescription(builder.toString());
        statusChannels.forEach(textChannel -> {
            try {
                MessageHistory history = new MessageHistory(textChannel);
                List<Message> messageList = history.retrievePast(20).complete();
                if(!messageList.isEmpty()) {
                    for (Message message : messageList) {
                        if(message.getAuthor().getIdLong() == 1009381581787504726L) {
                            textChannel.deleteMessageById(message.getId()).queue();
                        }
                    }
                }
                textChannel.sendMessageEmbeds(eb.build()).queue();
            } catch (Exception ignored) {
                System.out.println("["+new Date().toGMTString()+"]" + " Discord was not responding (x"+errorCount+")");
                errorCount++;
                if(errorCount >= 10) {
                    System.out.println("["+new Date().toGMTString()+"]" + " Discord error-count was too high, restarting now");
                    restartBot();
                }
            }
        });
    }

    private static void restartBot() {
        try {
            System.out.println("["+new Date().toGMTString()+"]" + " Restarting Bot...");
            Runtime.getRuntime().exec("./restart.sh");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private static int errorCount = 0;

    private static void getStatus() {
        Website website = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");
        if(website.getDoc() == null) {
            return;
        }
        ServerManager.loadServers();
    }

}

