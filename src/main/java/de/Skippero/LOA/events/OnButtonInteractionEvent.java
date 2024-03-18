package de.Skippero.LOA.events;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.events
Created by Skippero
on 18.03.2024 , 15:42

*/

import de.Skippero.LOA.features.raid.Raid;
import de.Skippero.LOA.features.raid.RaidManager;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.HashMap;
import java.util.Map;

public class OnButtonInteractionEvent extends ListenerAdapter {

    public static class ApplyProcess {
        public long userId;
        public long raidId;
        public boolean asExp;
    }
    public static Map<Long, ApplyProcess> applicants = new HashMap<>();

    //Event
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        StringSelectMenu.Builder builder = StringSelectMenu.create("raid:apply");
        builder.addOption("Berserker", "Berserker");
        builder.addOption("Paladin", "Paladin(S)");
        builder.addOption("Destroyer", "Destroyer");
        builder.addOption("Gunlancer", "Gunlancer");
        builder.addOption("Slayer", "Slayer");
        builder.addOption("Striker", "Striker");
        builder.addOption("Scrapper", "Scrapper");
        builder.addOption("Wardancer", "Wardancer");
        builder.addOption("Glaivier", "Glaivier");
        builder.addOption("Soulfist", "Soulfist");
        builder.addOption("Sorceress", "Sorceress");
        builder.addOption("Arcana", "Arcana");
        builder.addOption("Summoner", "Summoner");
        builder.addOption("Bard", "Bard(S)");
        builder.addOption("Deathblade", "Deathblade");
        builder.addOption("Shadowhunter", "Shadowhunter");
        builder.addOption("Reaper", "Reaper");
        builder.addOption("Souleater", "Souleater");
        builder.addOption("Deadeye", "Deadeye");
        builder.addOption("Artillerist", "Artillerist");
        builder.addOption("Machinist", "Machinist");
        builder.addOption("Sharpshooter", "Sharpshooter");
        builder.addOption("Gunslinger", "Gunslinger");
        builder.addOption("Artist", "Artist(S)");
        builder.addOption("Aeromancer", "Aeromancer");
        StringSelectMenu menu = builder.build();

        if(event.getButton().getId() != null) {
            switch (event.getButton().getId()) {
                case "del":
                    event.getInteraction().getMessage().delete().queue();
                    break;
                case "joinRaidMokoko":
                    if(startRaidAppliance(event, false)) {
                        event.reply("[Mokoko] Please select your class").setEphemeral(true).addActionRow(menu).queue();
                    }
                    break;
                case "joinRaidExp":
                    if(startRaidAppliance(event, true)) {
                        event.reply("[Experienced] Please select your class").setEphemeral(true).addActionRow(menu).queue();
                    }
                    break;
                case "leaveRaid":
                    manageRaidMemberTermination(event);
                    break;
            }
        }
    }

    //Methods
    private void manageRaidMemberTermination(ButtonInteractionEvent event) {
        Member member = event.getMember();
        MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        MessageEmbed.Footer footer = embed.getFooter();
        if(member == null)
            return;
        if(footer == null)
            return;
        String footerText = footer.getText();
        if(footerText == null)
            return;
        long raidId = Long.parseLong(footerText);
        Raid raid = RaidManager.getById(raidId);
        if(raid.isMember(member.getIdLong())) {
            event.reply("You successfully terminated your raid application").setEphemeral(true).queue();
            raid.removeMember(member.getIdLong());
        }else{
            event.reply("You are not member of this raid").setEphemeral(true).queue();
        }
    }
    private boolean startRaidAppliance(ButtonInteractionEvent event, boolean asExp) {
        Member member = event.getMember();
        MessageEmbed embed = event.getMessage().getEmbeds().get(0);
        MessageEmbed.Footer footer = embed.getFooter();
        if(member == null)
            return false;
        if(footer == null)
            return false;
        String footerText = footer.getText();
        if(footerText == null)
            return false;
        long raidId = Long.parseLong(footerText);
        Raid raid = RaidManager.getById(raidId);

        if(raid.isMember(member.getIdLong())) {
            event.reply("You already applied for this raid").setEphemeral(true).queue();
            return false;
        }

        ApplyProcess apply = new ApplyProcess();
        apply.asExp = asExp;
        apply.raidId = raidId;
        apply.userId = member.getIdLong();

        applicants.put(member.getIdLong(), apply);
        return true;
    }

}
