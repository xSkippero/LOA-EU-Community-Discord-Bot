package de.Skippero.LOA;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.Skippero.LOA.config.ConfigManager;
import de.Skippero.LOA.events.OnButtonInteractionEvent;
import de.Skippero.LOA.events.OnGuildCreateInviteEvent;
import de.Skippero.LOA.events.OnSlashCommandEvent;
import de.Skippero.LOA.events.OnStringSelectionEvent;
import de.Skippero.LOA.features.raid.RaidManager;
import de.Skippero.LOA.features.states.ServerManager;
import de.Skippero.LOA.sql.QueryHandler;
import lombok.Getter;
import lombok.Setter;
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
import java.text.ParseException;
import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
public class LOABot {

    public static long nextUpdateTimestamp;
    public static Map<User, String> updateNotify;
    public static JDA jda;
    public static String botVersion;
    public static Model buildInformation;
    @Getter
    private static ConfigManager configManager;
    @Getter
    private static QueryHandler queryHandler;
    private static Multimap<String, String[]> configurations;
    public static Map<String, TextChannel> statusChannels;
    public static Map<String, TextChannel> pushNotificationChannels;

    public static void main(String[] args) throws InterruptedException, IOException, XmlPullParserException, ParseException {

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
        builder.addEventListeners(new OnSlashCommandEvent());
        builder.addEventListeners(new OnButtonInteractionEvent());
        builder.addEventListeners(new OnGuildCreateInviteEvent());
        builder.addEventListeners(new OnStringSelectionEvent());

        jda = builder.build();
        jda.awaitReady();
        
        jda.upsertCommand("ping", "Calculate ping of the bot").queue();
        jda.upsertCommand("about", "Prints out information about the bot").queue();
        jda.upsertCommand("reload", "Reload all server-configurations").queue();
        jda.upsertCommand("restart", "Restart the bot").queue();
        jda.upsertCommand("stop", "Stop the bot").queue();
        jda.upsertCommand("config", "Configure the Bot")
                .addOption(OptionType.STRING, "property", "The field you want to change", false)
                .addOption(OptionType.STRING, "value", "The value for the field you want to change", false)
                .setGuildOnly(true).queue();
        jda.upsertCommand("permissions", "Configure Guild permissions for the bot usage")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "action", "What you want to do (add/remove/list)")
                .addOption(OptionType.USER, "user", "The user you want to affect")
                .addOption(OptionType.STRING, "permission", "The permission you want to add/remove", false).queue();
        jda.upsertCommand("raid", "Create a raid event where users can join to meetup for a lostark raid")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "name", "Title of the raid event", true)
                .addOption(OptionType.STRING, "desc", "Description of the raid event", true)
                .addOption(OptionType.INTEGER, "dpscount","Amount of planned DPS",true)
                .addOption(OptionType.INTEGER, "suppcount", "Amount of planned Supports", true)
                .addOption(OptionType.STRING, "startdate", "Date where the event starts 'DD.MM.YYYY HH:MM'",true)
                .addOption(OptionType.STRING, "duration","Duration how long the raid lasts",true).queue();
        jda.upsertCommand("movemembers","Move members from raid a to b")
                .setGuildOnly(true)
                .addOption(OptionType.INTEGER, "raida", "raid to move from", true)
                .addOption(OptionType.INTEGER, "raidb", "raid to move to", true).queue();
        jda.upsertCommand("deleteraid","Delete a raid")
                .setGuildOnly(true)
                .addOption(OptionType.INTEGER, "raidid", "raid to delete", true).queue();
        jda.upsertCommand("mergerole","Merge roleA into roleB")
                .setGuildOnly(true)
                .addOption(OptionType.STRING, "rolea", "role to merge", true)
                .addOption(OptionType.STRING, "roleb", "role to merge into", true).queue();

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

        RaidManager.loadRaids();

        ServerManager.init();
        startTimers(jda);
    }

    private static boolean serverExistsInDB(String name) {
        return configurations.containsKey(name);
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
            user.openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] Your configuration update for the discord server '**" + jda.getGuildById(s).getName() + "**' is now active :smile:").setActionRow(delButton)).queue(null, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    LOABot.log(user.getEffectiveName() + " blocked PM's, cannot send message");
                }
            });
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

    public static void log(String message) {
        System.out.println("[" + new Date() + "] " + message);
    }
}
