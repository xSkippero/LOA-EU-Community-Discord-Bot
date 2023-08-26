package de.Skippero.LOA.features.states;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.utils.MessageColor;
import de.Skippero.LOA.utils.Website;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jsoup.nodes.Element;

import java.time.temporal.TemporalAccessor;
import java.util.*;

public class ServerManager {
    private static final HashMap<String, State> lastStates = new HashMap<>();
    public static List<Server> servers = new LinkedList<Server>();
    public static int goodAmount;
    public static int busyAmount;
    public static int fullAmount;
    public static int maintenanceAmount;

    public static void loadServers() {
        servers.clear();

        goodAmount = 0;
        busyAmount = 0;
        fullAmount = 0;
        maintenanceAmount = 0;

        boolean stateChanged = false;

        Website website = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");

        Element rootElement = website.getDoc().selectFirst("body > main > section > div > div.ags-ServerStatus-content-responses > div:nth-child(3)");

        if (rootElement != null && !rootElement.children().isEmpty()) {
            for (Element child : rootElement.children()) {
                if (child.className().equals("ags-ServerStatus-content-responses-response-server")) {
                    Element stateChild = child.children().first().children().first();
                    Element serverChild = child.children().last();
                    Server server = new Server(serverChild.text().replaceAll(" ", ""), getStateFromClassName(stateChild.className()));
                    servers.add(server);
                    updateStateAmountForServers(server);
                    if (!lastStates.containsKey(server.getName())) {
                        lastStates.put(server.getName(), server.getState());
                    } else {
                        State lastState = lastStates.get(server.getName());
                        if (lastState != server.getState()) {
                            lastStates.put(server.getName(), server.getState());
                            stateChanged = true;
                        }
                    }
                }
            }
        }

        stateChanged = true;

        if(stateChanged) {
            pushStateUpdateNotify();
        }

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

    public static void pushStateUpdateNotify() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":loudspeaker: LostARK EU Server Status Update :loudspeaker:");
        //eb.setColor(getStateMajorityColor().getColor());
        eb.setColor(MessageColor.CYAN.getColor());
        eb.setTimestamp(new Date().toInstant());
        for (Server server : ServerManager.servers) {
           eb.addField(server.getName(),getEmoteForState(State.MAINTENANCE),true);
        }

        LOABot.pushNotificationChannels.forEach((s, textChannel) -> {
            if (textChannel.getGuild().getId().equals(s)) {
                textChannel.sendMessageEmbeds(eb.build()).queue();
            }
        });
    }

    private static void getStatus() {
        Website website = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");
        if (website.getDoc() == null) {
            return;
        }
        ServerManager.loadServers();
    }

    private static State getStateFromClassName(String className) {
        if (className.contains("maintenance")) {
            return State.MAINTENANCE;
        } else if (className.contains("busy")) {
            return State.BUSY;
        } else if (className.contains("good")) {
            return State.GOOD;
        } else {
            return State.FULL;
        }
    }

    private static void updateStateAmountForServers(Server server) {
        switch (server.getState()) {
            case FULL:
                fullAmount++;
                break;
            case BUSY:
                busyAmount++;
                break;
            case GOOD:
                goodAmount++;
                break;
            case MAINTENANCE:
                maintenanceAmount++;
                break;
        }
    }

    public static MessageColor getStateMajorityColor() {
        int max = goodAmount;
        MessageColor color = MessageColor.GREEN;
        if (busyAmount > max) {
            max = busyAmount;
            color = MessageColor.ORANGE;
        }
        if (fullAmount > max) {
            max = fullAmount;
            color = MessageColor.RED;
        }
        if (maintenanceAmount > max) {
            color = MessageColor.CYAN;
        }
        return color;
    }

    public static void init() {
        Timer timer = new Timer("Statustimer");
        long period = 60 * 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                getStatus();
            }
        };
        timer.schedule(task, 5 * 1000, period);
    }
}

