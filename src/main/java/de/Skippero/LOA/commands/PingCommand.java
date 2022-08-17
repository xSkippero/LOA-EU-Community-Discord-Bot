package de.Skippero.LOA.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PingCommand extends Command {

    public PingCommand() {
        super("ping", "Command to test if the Bot gets the Message");
        System.out.println("[+] Ping Command");
    }

    @Override
    public void handle(MessageReceivedEvent e, String[] args) {
        e.getChannel().sendMessage("Pong!").queue();
    }

}
