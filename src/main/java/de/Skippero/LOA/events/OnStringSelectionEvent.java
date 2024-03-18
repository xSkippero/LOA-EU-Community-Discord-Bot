package de.Skippero.LOA.events;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.events
Created by Skippero
on 18.03.2024 , 15:43

*/

import de.Skippero.LOA.features.raid.Raid;
import de.Skippero.LOA.features.raid.RaidManager;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;

public class OnStringSelectionEvent extends ListenerAdapter {

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("raid:apply")) {
            if(event.getMember() == null) {
                return;
            }
            long memberId = event.getMember().getIdLong();
            if(OnButtonInteractionEvent.applicants.containsKey(memberId)) {
                OnButtonInteractionEvent.ApplyProcess apply = OnButtonInteractionEvent.applicants.get(memberId);
                SelectOption selection = event.getInteraction().getSelectedOptions().get(0);
                Raid r = RaidManager.getById(apply.raidId);
                if(r != null) {
                    r.addMember(memberId,apply.asExp,selection.getValue(),event.getMember().getAsMention());
                }
                OnButtonInteractionEvent.applicants.remove(memberId);
            }
            event.getMessage().delete().queue();
            event.reply("Successfully applied to the raid").setEphemeral(true).queue();
        }
    }

}
