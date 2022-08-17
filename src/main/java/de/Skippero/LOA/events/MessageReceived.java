package de.Skippero.LOA.events;

import de.Skippero.LOA.commands.Command;
import de.Skippero.LOA.utils.MessageColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;

public class MessageReceived extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String[] words = event.getMessage().getContentRaw().split(" ");

        if (words[0].startsWith("!s")) {
            event.getChannel().sendTyping().queue();
            String command = words[0].replaceFirst("!s", "");
            String[] args = new String[words.length - 1];
            System.arraycopy(words, 1, args, 0, words.length - 1);
            for (Command c : Command.commands) {
                if (c.getCommand().equalsIgnoreCase(command) || c.isAlias(command)) {
                    System.out.println("["+new Date()+"] " +  event.getAuthor().getName() + " executed " + event.getMessage().getContentRaw());
                    c.handle(event, args);
                    event.getMessage().delete().queue();
                    return;
                }
            }
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(MessageColor.RED.getColor());
            eb.setDescription("The given command does not exist!");
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }


}