package de.Skippero.LOA.events;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.events
Created by Skippero
on 18.03.2024 , 15:43

*/

import de.Skippero.LOA.LOABot;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

public class OnGuildCreateInviteEvent extends ListenerAdapter {

    Button delButton = Button.danger("del","Delete");

    public void onGuildCreateInvite(GuildInviteCreateEvent event) {

        LOABot.log("Event triggered");

        if(event.getInvite().getMaxAge() != 0) {
            LOABot.log("Invite not maxage 0");
            return;
        }

        long userId = event.getInvite().getInviter().getIdLong();
        List<Role> userRoles = event.getGuild().getMemberById(userId).getRoles();
        if(userRoles.stream().noneMatch(r->r.getName().equalsIgnoreCase("Permanent Invites"))) {

            LOABot.log("None match");

            event.getInvite().getInviter().openPrivateChannel().flatMap(channel -> channel.sendMessage("[Automated Message] Sorry, but you do not have the required permissions to create permanent invite links," +
                    " for a permanent link please use the one in the pinned message in #\uD83D\uDD14announcements :smile:").setActionRow(delButton)).queue();
            event.getInvite().delete().queue();
        }

    }

}
