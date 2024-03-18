package de.Skippero.LOA.events;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.commands.LOACommand;
import de.Skippero.LOA.commands.cdev.ReloadCommand;
import de.Skippero.LOA.commands.cdev.RestartCommand;
import de.Skippero.LOA.commands.cdev.StopCommand;
import de.Skippero.LOA.commands.cprotected.*;
import de.Skippero.LOA.commands.cpublic.AboutCommand;
import de.Skippero.LOA.commands.cpublic.PingCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.*;
import java.util.stream.Stream;

public class OnSlashCommandEvent extends ListenerAdapter {

    public List<LOACommand> commands = new ArrayList<>();

    {
        //DEV
        commands.add(new ReloadCommand("reload"));
        commands.add(new RestartCommand("restart"));
        commands.add(new StopCommand("stop"));

        //PROTECTED
        commands.add(new ConfigCommand("config"));
        commands.add(new PermissionsCommand("permissions"));
        commands.add(new MergeRoleCommand("mergerole"));
        commands.add(new RaidCommand("raid"));
        commands.add(new DeleteRaidCommand("deleteraid"));
        commands.add(new MoveMembersCommand("movemembers"));

        //PUBLIC
        commands.add(new AboutCommand("about"));
        commands.add(new PingCommand("ping"));
    }

    //Events
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        Stream<LOACommand> commandStream = commands.stream().filter(cmd->cmd.commandName.equalsIgnoreCase(event.getName()));
        Optional<LOACommand> first = commandStream.findFirst();
        if(first.isPresent()) {
            LOACommand command = first.get();
            command.onExecute(event);
            LOABot.log(event.getUser().getName() + " entered /" + command.commandName);
        }
    }
}
