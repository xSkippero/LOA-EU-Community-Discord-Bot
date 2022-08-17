package de.Skippero.LOA.commands;

import de.Skippero.LOA.utils.MessageColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class UpdateCommand extends Command {

    private final Map<String, Long> timer = new HashMap<>();

    public UpdateCommand() {
        super("update", "Updates the Bot on the Hosts Server");
        System.out.println("[+] Update Command");
    }

    @Override
    public void handle(MessageReceivedEvent e, String[] args) {
        if (timer.containsKey(e.getAuthor().getId()) && timer.get(e.getAuthor().getId()) >= System.currentTimeMillis()) {
            timer.put(e.getAuthor().getId(), System.currentTimeMillis() + 3000);
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(MessageColor.RED.getColor());
            eb.setTitle("Update Starting");
            eb.setDescription(e.getAuthor().getAsMention() + " The Update script has started, the Bot will start itself again");
            e.getChannel().sendMessageEmbeds(eb.build()).queue();
            runUpdateScriptAsync();
        } else if (timer.containsKey(e.getAuthor().getId()) && timer.get(e.getAuthor().getId()) < System.currentTimeMillis()) {
            sendConfirm(e);
        } else if (!timer.containsKey(e.getAuthor().getId())) {
            sendConfirm(e);
        }
    }

    public void sendConfirm(MessageReceivedEvent e) {
        timer.put(e.getAuthor().getId(), System.currentTimeMillis() + 3000);
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(MessageColor.RED.getColor());
        eb.setTitle(":warning: Confirmation");
        eb.setDescription(e.getAuthor().getAsMention() + " Please confirm your choice to Update the Bot in the next 3 seconds, to do that enter the Command again");
        e.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    public void runUpdateScriptAsync() {
        try {
            Runtime.getRuntime().exec("./updateInScreen.sh");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

}
