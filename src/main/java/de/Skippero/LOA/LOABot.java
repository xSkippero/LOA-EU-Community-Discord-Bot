package de.Skippero.LOA;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.Skippero.LOA.config.ConfigManager;
import de.Skippero.LOA.events.*;
import de.Skippero.LOA.features.raid.RaidManager;
import de.Skippero.LOA.features.states.ServerManager;
import de.Skippero.LOA.sql.QueryHandler;
import lombok.Getter;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.User;
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
import java.util.function.Consumer;

@Getter
public class LOABot {

    private static JDA jda;
    private static String botVersion;
    private static Model buildInformation;
    private static ConfigManager configManager;
    private static QueryHandler queryHandler;
    private static Multimap<String, String[]> configurations = ArrayListMultimap.create();
    
    private static final Map<String, TextChannel> statusChannels = new HashMap<>();
    private static final Map<String, TextChannel> pushNotificationChannels = new HashMap<>();
    private static final Map<User, String> updateNotify = new HashMap<>();
    
    private static long nextUpdateTimestamp;

    public static void main(String[] args) throws InterruptedException, IOException, XmlPullParserException {
        if (args.length < 1) {
            System.err.println("Missing Token on Parameter 1 (Index 0)");
            System.exit(1);
        }

        initBotVersion();
        log("Starting LOA-EU-Status-Bot v. " + botVersion + " by Skippero");

        configManager = new ConfigManager();
        queryHandler = new QueryHandler();
        configurations = queryHandler.loadConfiguration(configurations);

        jda = JDABuilder.createDefault(args[0])
                .enableIntents(GatewayIntent.GUILD_MEMBERS)
                .setAutoReconnect(true)
                .setStatus(OnlineStatus.ONLINE)
                .setActivity(Activity.watching("LOA-EU Server-Status"))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .addEventListeners(new OnSlashCommandEvent(), new OnButtonInteractionEvent(),
                        new OnGuildCreateInviteEvent(), new OnStringSelectionEvent())
                .build()
                .awaitReady();

        registerCommands();
        initializeServers();
        startTimers();
        
        log("Bot is fully operational.");
    }

    private static void initBotVersion() throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        buildInformation = reader.read(new FileReader("pom.xml"));
        botVersion = buildInformation.getVersion();
    }

    private static void registerCommands() {
        jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        jda.upsertCommand("about", "Prints out information about the bot").queue();
        jda.upsertCommand("reload", "Reload all server configurations").queue();
        jda.upsertCommand("restart", "Restart the bot").queue();
        jda.upsertCommand("stop", "Stop the bot").queue();
        
        addConfigurableCommand("config", "Configure the bot",
                new CommandOption(OptionType.STRING, "property", "Field to change", false),
                new CommandOption(OptionType.STRING, "value", "New value", false));

        addConfigurableCommand("permissions", "Configure Guild permissions",
                new CommandOption(OptionType.STRING, "action", "add/remove/list"),
                new CommandOption(OptionType.USER, "user", "User to affect"),
                new CommandOption(OptionType.STRING, "permission", "Permission to modify", false));

        addConfigurableCommand("raid", "Create a Lost Ark raid event",
                new CommandOption(OptionType.STRING, "name", "Title", true),
                new CommandOption(OptionType.STRING, "desc", "Description", true),
                new CommandOption(OptionType.INTEGER, "dpscount", "Planned DPS count", true),
                new CommandOption(OptionType.INTEGER, "suppcount", "Planned Support count", true),
                new CommandOption(OptionType.STRING, "startdate", "Start date 'DD.MM.YYYY HH:MM'", true),
                new CommandOption(OptionType.STRING, "duration", "Duration", true));

        addConfigurableCommand("movemembers", "Move members from one raid to another",
                new CommandOption(OptionType.INTEGER, "raida", "Raid to move from", true),
                new CommandOption(OptionType.INTEGER, "raidb", "Raid to move to", true));

        addConfigurableCommand("deleteraid", "Delete a raid",
                new CommandOption(OptionType.INTEGER, "raidid", "Raid ID", true));

        addConfigurableCommand("mergerole", "Merge roles",
                new CommandOption(OptionType.STRING, "rolea", "Role to merge", true),
                new CommandOption(OptionType.STRING, "roleb", "Target role", true));
    }

    private static void addConfigurableCommand(String name, String description, CommandOption... options) {
        CommandData command = Commands.slash(name, description);
        for (CommandOption option : options) {
            command.addOption(option.type(), option.name(), option.description(), option.required());
        }
        jda.upsertCommand(command).queue();
    }

    private static void initializeServers() {
        log("------------------------------------------------");
        log("Bot is active on:");

        jda.getGuilds().forEach(guild -> {
            guild.loadMembers(member -> {});
            log("- " + guild.getName());
            if (!configurations.containsKey(guild.getId())) {
                queryHandler.createDefaultDataBaseConfiguration(guild.getId());
            }
        });

        log("------------------------------------------------");

        RaidManager.loadRaids();
        ServerManager.init();
    }

    private static void startTimers() {
        Timer restartTimer = new Timer("RestartTimer");
        restartTimer.schedule(new TimerTask() {
            public void run() {
                restartBot();
            }
        }, 24 * 60 * 60 * 1000);

        Timer configTimer = new Timer("ConfigTimer");
        configTimer.schedule(new TimerTask() {
            public void run() {
                reloadConfig();
            }
        }, 5 * 1000, 2 * 60 * 60 * 1000);
    }

    private static void reloadConfig() {
        nextUpdateTimestamp = System.currentTimeMillis() + 2 * 60 * 60 * 1000;
        pushNotificationChannels.clear();
        statusChannels.clear();
        configurations = queryHandler.loadConfiguration(configurations);

        for (var guild : jda.getGuilds()) {
            String guildId = guild.getId();
            List<String[]> configList = new ArrayList<>(configurations.get(guildId));

            String pushChannelName = "loa-eu-notify";
            String statusChannelName = "loa-eu-status";
            boolean pushNotifications = false;

            for (String[] config : configList) {
                switch (config[0]) {
                    case "pushNotifications": pushNotifications = Boolean.parseBoolean(config[1]); break;
                    case "pushChannelName": pushChannelName = config[1]; break;
                    case "statusChannelName": statusChannelName = config[1]; break;
                }
            }

            if (pushNotifications) {
                guild.getTextChannelsByName(pushChannelName, true)
                        .stream().findFirst()
                        .ifPresent(channel -> pushNotificationChannels.put(guildId, channel));
            }

            guild.getTextChannelsByName(statusChannelName, true)
                    .stream().findFirst()
                    .ifPresent(channel -> statusChannels.put(guildId, channel));
        }

        log("Updated configurations on " + updateNotify.size() + " servers.");
        updateNotify.clear();
    }

    private static void restartBot() {
        try {
            jda.shutdown();
            log("Restarting bot...");
            Runtime.getRuntime().exec("./restart.sh");
        } catch (IOException e) {
            log("Failed to restart bot: " + e.getMessage());
        }
    }

    private static void log(String message) {
        System.out.println("[" + new Date() + "] " + message);
    }
}
