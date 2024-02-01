package de.Skippero.LOA;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.Skippero.LOA.config.ConfigManager;
import de.Skippero.LOA.events.OnSlashCommandInteraction;
import de.Skippero.LOA.features.states.ServerManager;
import de.Skippero.LOA.sql.QueryHandler;
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
    public static JDA jda;
    public static String botVersion;
    public static Model buildInformation;
    private static ConfigManager configManager;
    private static QueryHandler queryHandler;
    private static Multimap<String, String[]> configurations;
    public static Map<String, TextChannel> statusChannels;
    public static Map<String, TextChannel> pushNotificationChannels;

    public static void main(String[] args) throws InterruptedException, IOException, XmlPullParserException {

        if (args.length < 1) {
            System.err.println("Missing Token on Parameter 1 (Index 0)");
            System.exit(1);
        }

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read(new FileReader("pom.xml"));
        buildInformation = model;
        botVersion = model.getVersion();

        log("Starting LOA-EU-Status-Bot v. " + botVersion + " by Skippero");

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

        log("------------------------------------------------");
        log("Bot is active on: ");
        jda.getGuilds().forEach(guild -> {
            guild.loadMembers(member -> {});
            log("- " + guild.getName());
            if (!serverExistsInDB(guild.getId())) {
                queryHandler.createDefaultDataBaseConfiguration(guild.getId());
            }
        });
        log("------------------------------------------------");

        pushNotificationChannels = new HashMap<>();
        statusChannels = new HashMap<>();
        updateNotify = new HashMap<>();

        ServerManager.init();
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

        Timer timer2 = new Timer("Configtimer");
        long period2 = 2 * 60 * 60 * 1000L;
        TimerTask task2 = new TimerTask() {
            public void run() {
                reloadConfig(jda);
            }
        };
        timer2.schedule(task2, 5 * 1000, period2);

        log("Successfully started the bot");
    }

    private static void reloadConfig(JDA jda) {
        nextUpdateTimestamp = System.currentTimeMillis() + 2 * 60 * 60 * 1000;
        pushNotificationChannels.clear();
        statusChannels.clear();
        configurations = queryHandler.loadConfiguration(configurations);
        for (Guild guild : jda.getGuilds()) {
            String guildName = guild.getId();
            boolean pushNotifications = false;
            String pushNotificationChannelName = "loa-eu-notify";
            String statusChannelName = "loa-eu-status";
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
        }

        Button delButton = Button.danger("del","Delete");

        updateNotify.forEach((user, s) -> {
            user.openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] Your configuration update for the discord server '**" + jda.getGuildById(s).getName() + "**' is now active :smile:").setActionRow(delButton)).queue();
        });
        if (!updateNotify.isEmpty()) {
            log("updated configurations on " + updateNotify.size() + " servers");
        }
        updateNotify.clear();
    }

    public static void restartBot() {
        try {
            jda.shutdown();
            log("Restarting bot...");
            queryHandler.closeConnection();
            Runtime.getRuntime().exec("./restart.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void manualReload() {
        reloadConfig(jda);
    }

    private static void log(String message) {
        System.out.println("[" + new Date() + "] " + message);
    }
}

