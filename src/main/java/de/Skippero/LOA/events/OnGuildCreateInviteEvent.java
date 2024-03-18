package de.Skippero.LOA.events;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.events
Created by Skippero
on 18.03.2024 , 15:43

*/

import de.Skippero.LOA.LOABot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class OnGuildCreateInviteEvent extends ListenerAdapter {

    @Override
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {

        if(event.getInvite().getMaxAge() != 0) {
            return;
        }

        long userId = event.getInvite().getInviter().getIdLong();
        List<Role> userRoles = event.getGuild().getMemberById(userId).getRoles();
        if(userRoles.stream().noneMatch(r->r.getName().equalsIgnoreCase("Permanent Invites"))) {
            event.getInvite().getInviter().openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] You do not have the required role to create permanent invites, your link will be auto-deleted in 30 Minutes")).queue(null, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            LOABot.log(event.getInvite().getInviter().getEffectiveName() + " blocked PM's, cannot send message");
                        }
                    });
            LOABot.log(event.getInvite().getInviter().getEffectiveName() + " tried to create a perma-invite, deleting the invite in 30 minutes");
            event.getInvite().delete().queueAfter(1, TimeUnit.MINUTES);
        }

    }

}
