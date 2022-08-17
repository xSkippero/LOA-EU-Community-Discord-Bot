package de.Skippero.LOA;

import de.Skippero.LOA.commands.HelpCommand;
import de.Skippero.LOA.commands.PingCommand;
import de.Skippero.LOA.commands.UpdateCommand;
import de.Skippero.LOA.events.MessageReceived;
import de.Skippero.LOA.utils.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LOABot {

    private static TextChannel statusChannel;
    private static TextChannel pushChannel;

    public static void main(String[] args) throws LoginException, InterruptedException {

        args = new String[]{"MTAwOTM4MTU4MTc4NzUwNDcyNg.GpvHeg.cq_85OXUqA00ZK7Z0RVUbd28ewEeTrBaRa9E9U"};

        if (args.length < 1) {
            System.err.println("Missing Token on Parameter 1 (Index 0)");
            System.exit(1);
        }

        System.out.println("Starting LOA-EUW-Status-Bot by Skippero");

        JDABuilder builder = JDABuilder.createDefault(args[0]);
        builder.setStatus(OnlineStatus.ONLINE);
        builder.enableIntents(GatewayIntent.GUILD_MEMBERS);
        builder.setAutoReconnect(true);
        builder.setActivity(Activity.watching("LOA-EUW Server-Status"));
        builder.addEventListeners(new MessageReceived());
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);

        JDA jda = builder.build();
        jda.awaitReady();

        System.out.println(" ");
        System.out.println("Bot is active on: ");
        jda.getGuilds().forEach(guild -> {
            System.out.println("- " + guild.getName());
        });
        System.out.println(" ");

        initCommands();

        List<TextChannel> stateChannels = jda.getTextChannelsByName("loa-euw-status", true);
        List<TextChannel> pushChannels = jda.getTextChannelsByName("loa-euw-notify", true);
        if(!stateChannels.isEmpty()) {
            statusChannel = stateChannels.get(0);
            if(!pushChannels.isEmpty()) {
                pushChannel = pushChannels.get(0);
                startTimers();
            }
        }
    }

    private static void startTimers() {
        Timer timer = new Timer("Statustimer");
        long period = 30 * 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                checkServerStatusAndPrintResults();
            }
        };
        timer.schedule(task, 5*1000,period);
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
                eb.setDescription(server + " is now Online");
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
        SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy hh:mm");
        Date date = new Date();
        eb.setTitle("Status Update " + dt.format(date));
        pushChannel.sendMessageEmbeds(eb.build()).queue();
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
            builder.append("All Servers are Offline");
        }
        eb.setDescription(builder.toString());
        try {
            MessageHistory history = new MessageHistory(statusChannel);
            List<Message> messageList = history.retrievePast(20).complete();
            if(!messageList.isEmpty()) {
                for (Message message : messageList) {
                    if(message.getAuthor().getIdLong() == 1009381581787504726L) {
                        statusChannel.deleteMessageById(message.getId()).queue();
                    }
                }
            }
            statusChannel.sendMessageEmbeds(eb.build()).queue();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void getStatus() {
        Website website = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");
        if(website.getDoc() == null) {
            return;
        }
        ServerManager.loadServers();
    }

    private static void initCommands() {
        System.out.println("Loading Commands");
        new HelpCommand();
        new PingCommand();
        new UpdateCommand();
    }

}

