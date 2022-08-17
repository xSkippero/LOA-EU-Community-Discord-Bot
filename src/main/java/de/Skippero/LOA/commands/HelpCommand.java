package de.Skippero.LOA.commands;

import de.Skippero.LOA.utils.MessageColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Shows all Commands");
        System.out.println("[+] Help Command");
    }

    @Override
    public void handle(MessageReceivedEvent e, String[] args) {

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(MessageColor.GREEN.getColor());
        eb.setTitle("LOA-EUW-Status-Bot - Help");

        StringBuilder sb = new StringBuilder();

        sb.append("Commands:\n\n");

        for (int i = 0; i < Command.commands.size(); i++) {
            Command cmd = Command.commands.get(i);
            sb.append("``").append("!s").append(cmd.getCommand());
            if (!cmd.syntax.equals("no syntax set")) {
                sb.append(" ").append(cmd.syntax);
            }
            sb.append("``\n");
            sb.append(cmd.getDescription()).append("\n");
            if (cmd.alias.length > 0) {
                sb.append("Alias: ");
                for (String s : cmd.alias) {
                    sb.append("``");
                    sb.append(s);
                    sb.append("`` ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        eb.setDescription(sb.toString());
        e.getChannel().sendMessageEmbeds(eb.build()).queue();

    }

}
