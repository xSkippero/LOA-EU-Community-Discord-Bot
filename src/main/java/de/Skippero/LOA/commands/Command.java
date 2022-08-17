package de.Skippero.LOA.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;

public abstract class Command {

    public static ArrayList<Command> commands;
    public String syntax = "no syntax set";
    protected String[] alias = {};
    private final String command;
    private final String description;

    public Command(String command, String description) {
        this.command = command;
        this.description = description;
        if (commands == null) commands = new ArrayList<>();
        commands.add(this);
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isAlias(String input) {
        for (String alias : alias) {
            if (input.equalsIgnoreCase(alias)) return true;
        }
        return false;
    }

    public String getDescription() {
        return this.description;
    }

    public abstract void handle(MessageReceivedEvent e, String[] args);

}
