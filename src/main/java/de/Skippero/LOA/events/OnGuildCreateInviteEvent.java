package de.Skippero.LOA.events;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.events
Created by Skippero
on 18.03.2024 , 15:43

*/

import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.List;

public class OnGuildCreateInviteEvent extends ListenerAdapter {

    public void OnGuildCreateInvite(GuildInviteCreateEvent event) {
        if(event.getInvite().getMaxAge() != 0) {
            return;
        }

        long userId = event.getInvite().getInviter().getIdLong();
        List<Role> userRoles = event.getGuild().getMemberById(userId).getRoles();
        if(userRoles.stream().noneMatch(r->r.getName().equalsIgnoreCase("Permanent Invites"))) {
            event.getInvite().getInviter().openPrivateChannel().flatMap(c->c.sendMessage("Sorry, but you do not have the required permissions to create permanent invite links, for a permanent link please use the one in the pinned message in #\uD83D\uDD14announcements")).queue();
            event.getInvite().delete().queue();
        }

    }

}