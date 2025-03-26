package de.Skippero.LOA.features.states;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.utils.MessageColor;
import de.Skippero.LOA.utils.Website;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.jsoup.nodes.Element;

import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ServerManager {

    private static final Map<String, Server> servers = new HashMap<>();
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public static void loadServers() {
        Optional<Website> optionalWebsite = Website.getWebsiteByUrl("https://www.playlostark.com/de-de/support/server-status");

        if (optionalWebsite.isEmpty()) {
            System.err.println("Fehler: Konnte keine Verbindung zur Server-Status-Seite herstellen.");
            return;
        }

        Website website = optionalWebsite.get();
        Element rootElement = website.getDoc().selectFirst("body > main > section > div > div.ags-ServerStatus-content-responses > div:nth-child(3)");

        if (rootElement == null || rootElement.children().isEmpty()) {
            System.err.println("Fehler: Serverstatus-Daten konnten nicht extrahiert werden.");
            return;
        }

        for (Element child : rootElement.children()) {
            if (child.className().equals("ags-ServerStatus-content-responses-response-server")) {
                Element stateChild = child.children().first().children().first();
                Element serverChild = child.children().last();

                if (stateChild == null || serverChild == null) continue;

                String serverName = serverChild.text().replaceAll(" ", "");
                State serverState = getStateFromClassName(stateChild.className());

                servers.computeIfAbsent(serverName, k -> new Server(serverName, serverState))
                       .Update(serverName, serverState);
            }
        }

        checkForUpdate();
    }

    private static void checkForUpdate() {
        boolean validForUpdate = servers.values().stream().allMatch(Server::IsValidStateUpdate);

        if (validForUpdate && !servers.isEmpty()) {
            buildAndSendUpdateMessage();
        }
    }

    private static void buildAndSendUpdateMessage() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(":loudspeaker: LostARK EU Server Status Update :loudspeaker:");
        eb.setColor(getStateMajorityColor().getColor());
        eb.setTimestamp(new Date().toInstant());

        for (Server server : servers.values()) {
            server.UpdateLastState(server.getState());
            eb.addField(server.getName(), getEmoteForState(server.getState()), true);
        }

        LOABot.pushNotificationChannels.forEach((s, textChannel) -> {
            if (textChannel.getGuild().getId().equals(s)) {
                textChannel.sendMessageEmbeds(eb.build()).queue();
            }
        });
    }

    private static void getStatus() {
        loadServers();
    }

    private static State getStateFromClassName(String className) {
        if (className == null) return State.FULL;
        if (className.contains("maintenance")) return State.MAINTENANCE;
        if (className.contains("busy")) return State.BUSY;
        if (className.contains("good")) return State.GOOD;
        return State.FULL;
    }

    public static MessageColor getStateMajorityColor() {
        Map<State, Long> stateCount = new EnumMap<>(State.class);
        Arrays.stream(State.values()).forEach(state -> stateCount.put(state, 0L));

        servers.values().forEach(server -> 
            stateCount.put(server.getState(), stateCount.get(server.getState()) + 1)
        );

        return stateCount.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> {
                    switch (entry.getKey()) {
                        case GOOD:
                            return MessageColor.GREEN;
                        case BUSY:
                            return MessageColor.ORANGE;
                        case FULL:
                            return MessageColor.RED;
                        case MAINTENANCE:
                            return MessageColor.CYAN;
                        default:
                            return MessageColor.GREEN;
                    }
                })
                .orElse(MessageColor.GREEN);
    }

    public static void init() {
        scheduler.scheduleAtFixedRate(ServerManager::getStatus, 5, 12, TimeUnit.SECONDS);
    }
}
