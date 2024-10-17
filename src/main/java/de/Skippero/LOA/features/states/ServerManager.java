package de.Skippero.LOA.features.states;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.utils.MessageColor;
import de.Skippero.LOA.utils.Website;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jsoup.nodes.Element;

import java.time.temporal.TemporalAccessor;
import java.util.*;

public class ServerManager {

    private static final Map<String, Server> servers = new HashMap<>();

    public static void loadServers() {
        Website website = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");
        Element rootElement = website.getDoc().selectFirst("body > main > section > div > div.ags-ServerStatus-content-responses > div:nth-child(3)");

        if (rootElement != null && !rootElement.children().isEmpty()) {
            for (Element child : rootElement.children()) {
                if (child.className().equals("ags-ServerStatus-content-responses-response-server")) {
                    Element stateChild = child.children().first().children().first();
                    Element serverChild = child.children().last();

                    String serverName = serverChild.text().replaceAll(" ", "");
                    State serverState = getStateFromClassName(stateChild.className());

                    Server server = servers.getOrDefault(serverName, new Server(serverName, serverState));
                    server.Update(serverName, serverState);
                    servers.put(serverName, server);
                }
            }
        }

        checkForUpdate();
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

    private static void checkForUpdate() {
        boolean validForUpdate = true;

        for (Server server : servers.values()) {
            if(!server.IsValidStateUpdate()) {
                validForUpdate = false;
            }
        }

        if(validForUpdate && !servers.isEmpty()) {
            buildAndSendUpdateMessage();
        }
    }

    private static void buildAndSendUpdateMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":loudspeaker: LostARK EU Server Status Update :loudspeaker:");
        eb.setColor(getStateMajorityColor().getColor());
        eb.setTimestamp(new Date().toInstant());

        for (Server server : ServerManager.servers.values()) {
            server.UpdateLastState(server.getState());
            eb.addField(server.getName(),getEmoteForState(server.getState()),true);
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

    public static MessageColor getStateMajorityColor() {
        long goodAmount = servers.values().stream().filter(server -> server.getState().equals(State.GOOD)).count();
        long busyAmount = servers.values().stream().filter(server -> server.getState().equals(State.BUSY)).count();
        long fullAmount = servers.values().stream().filter(server -> server.getState().equals(State.FULL)).count();
        long maintenanceAmount = servers.values().stream().filter(server -> server.getState().equals(State.MAINTENANCE)).count();
        MessageColor color = MessageColor.GREEN;
        if (busyAmount > goodAmount) {
            goodAmount = busyAmount;
            color = MessageColor.ORANGE;
        }
        if (fullAmount > goodAmount) {
            goodAmount = fullAmount;
            color = MessageColor.RED;
        }
        if (maintenanceAmount > goodAmount) {
            color = MessageColor.CYAN;
        }
        return color;
    }

    public static void init() {
        Timer timer = new Timer("Statustimer");
        long period = 12 * 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                getStatus();
            }
        };
        timer.schedule(task, 5 * 1000, period);
    }
}

