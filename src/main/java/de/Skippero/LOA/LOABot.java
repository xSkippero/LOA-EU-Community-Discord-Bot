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
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class LOABot {

    public static long nextUpdateTimestamp;
    public static Map<User, String> updateNotify;
    public static Map<String, List<Integer>> userCardNotifications;
    public static List<Integer> neededCardIndexesEayln;
    public static List<Integer> neededCardIndexesNia;
    public static JDA jda;
    public static String botVersion;
    public static Model buildInformation;
    public static List<String> niaUsers;
    public static List<String> ealynUsers;
    public static Map<String, TextChannel> merchantChannels;
    private static ConfigManager configManager;
    private static QueryHandler queryHandler;
    private static Multimap<String, String[]> configurations;
    public static Map<String, TextChannel> statusChannels;
    public static Map<String, TextChannel> pushNotificationChannels;

    public static boolean DEVELOP = false;

    public static void main(String[] args) throws InterruptedException, IOException, XmlPullParserException {

        if (args.length < 1) {
            System.err.println("Missing Token on Parameter 1 (Index 0)");
            System.exit(1);
        }

        if(args.length >= 2) {
            if(args[1].equalsIgnoreCase("--develop")) {
                DEVELOP = true;
                System.out.println(" ");
                System.out.println("!!!RUNNING DEVELOPER MODE!!!");
                System.out.println("- Getting LostMerchants Info from Testserver");
                System.out.println(" ");
            }
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));
        buildInformation = model;
        botVersion = model.getVersion();

        System.out.println("[" + new Date().toGMTString() + "] Starting LOA-EU-Status-Bot v. " + botVersion + " by Skippero");

        //MerchantManager.openConnection();

        configManager = new ConfigManager();
        queryHandler = new QueryHandler();
        configurations = ArrayListMultimap.create();

        configurations = queryHandler.loadConfiguration(configurations);

        JDABuilder builder = JDABuilder.createDefault(args[0]);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setAutoReconnect(true);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.setActivity(Activity.watching("LOA-EU Server-Status"));
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
        jda.upsertCommand("vendor", "Configure LostMerchants personal notifications").addOption(OptionType.STRING, "action", "What you want to do (add/remove)")
                .addOption(OptionType.INTEGER, "cardid", "The id of the Card you want to add/remove", false).queue();
        jda.upsertCommand("survey", "Send a custom embed message").addOption(OptionType.STRING, "title", "Title of the embed message", true)
                .addOption(OptionType.STRING, "description", "description of the embed message", true).queue();
        jda.upsertCommand("updatenotify", "Send a custom embed message to all vendor users").addOption(OptionType.STRING, "title", "Title of the embed message", true)
                .addOption(OptionType.STRING, "description", "description of the embed message", true).queue();

        System.out.println("[" + new Date().toGMTString() + "] ------------------------------------------------");
        System.out.println("[" + new Date().toGMTString() + "] Bot is active on: ");
        jda.getGuilds().forEach(guild -> {
            guild.loadMembers(member -> {});
            System.out.println("[" + new Date().toGMTString() + "] - " + guild.getName());
            if (!serverExistsInDB(guild.getId())) {
                queryHandler.createDefaultDataBaseConfiguration(guild.getId());
            }
        });
        System.out.println("[" + new Date().toGMTString() + "] ------------------------------------------------");

        System.out.println("[" + new Date().toGMTString() + "] Loading members into Cache");

        pushNotificationChannels = new HashMap<>();
        statusChannels = new HashMap<>();
        updateNotify = new HashMap<>();
        merchantChannels = new HashMap<>();

        ealynUsers = new ArrayList<>();
        niaUsers = new ArrayList<>();
        userCardNotifications = new HashMap<>();
        neededCardIndexesEayln = new ArrayList<>();
        neededCardIndexesNia = new ArrayList<>();

        ServerManager.init();

        //loadUserNotifications();

        startTimers(jda);
    }

    private static void loadUserNotifications() {
        List<String> userIds = getQueryHandler().getAllVendorUserIds();
        for (String userId : userIds) {
            int server = getQueryHandler().getServerForCardUser(userId);
            List<Integer> selCards = getQueryHandler().getSelectedCardsForUser(userId);
            switch (server) {
                case -1:
                    ealynUsers.add(userId);
                    userCardNotifications.put(userId,selCards);
                    break;
                case -2:
                    niaUsers.add(userId);
                    userCardNotifications.put(userId,selCards);
                    break;
            }
            for (Integer selCard : selCards) {
                switch (server) {
                    case -1:
                        if(!neededCardIndexesEayln.contains(selCard)) {
                            neededCardIndexesEayln.add(selCard);
                        }
                        break;
                    case -2:
                        if(!neededCardIndexesNia.contains(selCard)) {
                            neededCardIndexesNia.add(selCard);
                        }
                        break;
                }
            }
        }
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

        Timer timer2 = new Timer("Configtimer");
        long period2 = 2 * 60 * 60 * 1000L;
        TimerTask task2 = new TimerTask() {
            public void run() {
                reloadConfig(jda);
            }
        };
        timer2.schedule(task2, 5 * 1000, period2);

        System.out.println("[" + new Date().toGMTString() + "] Successfully started the bot");
    }

    private static boolean startUp = true;

    private static void reloadConfig(JDA jda) {

        nextUpdateTimestamp = System.currentTimeMillis() + 2 * 60 * 60 * 1000;

        int errorCount = 0;

        pushNotificationChannels.clear();
        statusChannels.clear();
        merchantChannels.clear();
        configurations = queryHandler.loadConfiguration(configurations);
        for (Guild guild : jda.getGuilds()) {
            String guildName = guild.getId();
            boolean pushNotifications = false;
            String pushNotificationChannelName = "loa-eu-notify";
            String statusChannelName = "loa-eu-status";
            String merchantChannelName = "loa-eu-merchants";
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
                    if(!messages.isEmpty() && messages.size() > 1) {
                        _merchantChannels.get(0).deleteMessages(messages).queue();
                    }else if(!messages.isEmpty()) {
                        _merchantChannels.get(0).deleteMessageById(messages.get(0).getId()).queue();
                    }
                }
                merchantChannels.put(guildName, _merchantChannels.get(0));
            }
        }

        Button delButton = Button.danger("del","Delete");

        updateNotify.forEach((user, s) -> {
            user.openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] Your configuration update for the Discord Server '**" + jda.getGuildById(s).getName() + "**' is now active :smile:").setActionRow(delButton)).queue();
        });
        if (!updateNotify.isEmpty()) {
            System.out.println("[" + new Date().toGMTString() + "]" + " Updated configurations on " + updateNotify.size() + " servers");
        }
        updateNotify.clear();
        startUp = false;
    }

    public static void restartBot() {
        try {
            jda.shutdown();
            System.out.println("[" + new Date().toGMTString() + "]" + " Restarting Bot...");
            queryHandler.closeConnection();
            Runtime.getRuntime().exec("./restart.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void manualReload() {
        reloadConfig(jda);
    }
}

