package de.Skippero.LOA.features.raid;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.features.raid
Created by Skippero
on 01.02.2024 , 02:17

*/

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.utils.MessageColor;
import lombok.Getter;
import lombok.Setter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
@Setter
public class Raid {

    private long id;
    private long serverId;
    private long channelId;
    private long messageId;
    private int dpsCount;
    private int supportCount;
    private RaidMeta meta;
    private ArrayList<RaidMember> activeMembers;
    private ArrayList<RaidMember> benchedMembers;

    public Raid(TextChannel channel, RaidMeta meta, int dpsCount, int supportCount) throws SQLException {
        this.id = LOABot.getQueryHandler().getNewRaidId();
        this.meta = meta;
        this.dpsCount = dpsCount;
        this.supportCount = supportCount;
        activeMembers = new ArrayList<>();
        benchedMembers = new ArrayList<>();
        sendOrUpdateMessage(channel);
    }

    public Raid(long id, RaidMeta meta, int dpsCount, int supportCount) {
        this.id = id;
        this.meta = meta;
        this.dpsCount = dpsCount;
        this.supportCount = supportCount;
        activeMembers = new ArrayList<>();
        benchedMembers = new ArrayList<>();
        sendOrUpdateMessage(null);

        if(System.currentTimeMillis() > meta.getAutoDeletionTimeStamp()) {
            deleteRaid();
        }
    }

    public void deleteRaid() {
        LOABot.getQueryHandler().deleteRaid(id);
    }

    private void sendOrUpdateMessage(TextChannel text) {

        if(text != null) {
            sendMessage(text);
        }

        Guild guild = LOABot.jda.getGuildById(serverId);
        if(guild != null) {
            TextChannel channel = guild.getTextChannelById(channelId);
            if(channel != null) {
                channel.retrieveMessageById(messageId).queue((message) -> {
                    message.editMessageEmbeds(buildMessage()).queue();
                }, new ErrorHandler().handle(ErrorResponse.UNKNOWN_MESSAGE, (e) -> {
                    sendMessage(channel);
                }));
            }
        }
    }

    private MessageEmbed buildMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(meta.getName());

        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder
                .append(meta.getDescription())
                .append("\n")
                .append("\n")
                .append(":calendar_spiral: Date: **").append(meta.getStartDate()).append("**").append("\n")
                .append(":alarm_clock: Start: **").append(meta.getStartDiscordTimeStamp()).append("**").append("\n")
                .append(":clock3: Duration: **").append(meta.getDurationText()).append("**").append("\n")
                .append("\n")
                .append(":crossed_swords: Planned DPS: **").append(getDpsCount()).append("**").append("\n")
                .append(":green_heart: Planned Support: **").append(getSupportCount()).append("**").append("\n");

        if(!activeMembers.isEmpty()) {
            contentBuilder
                    .append("\n")
                    .append("Active Members:")
                    .append("\n")
                    .append("\n");

            for (RaidMember activeMember : activeMembers) {
                String badge = activeMember.isExp() ? ":military_medal:" : ":sunflower:";
                contentBuilder.append("- ").append(badge).append(activeMember.getUserName()).append(" ").append("(").append(activeMember.getUserClass()).append(")").append("\n");
            }
        }

        if(!benchedMembers.isEmpty()) {
            contentBuilder
                    .append("\n")
                    .append("Available:")
                    .append("\n")
                    .append("\n");

            for (RaidMember activeMember : benchedMembers) {
                String badge = activeMember.isExp() ? ":loaLick:" : ":loaLetsPlay:";
                contentBuilder.append("- ").append(badge).append(activeMember.getUserName()).append(" ").append("(").append(activeMember.getUserClass()).append(")").append("\n");
            }
        }

        embed.setFooter(String.valueOf(id));

        embed.setDescription(contentBuilder);
        embed.setColor(MessageColor.getRandom().getColor());

        return embed.build();
    }

    public void sendMessage(TextChannel channel) {
        Button joinRaidMokoko = Button.secondary("joinRaidMokoko","Join as Mokoko");
        Button joinRaidExp = Button.primary("joinRaidExp","Join as experienced");
        Button leaveRaid = Button.danger("leaveRaid","Leave");

        channel.sendMessageEmbeds(buildMessage()).setActionRow(joinRaidExp,joinRaidMokoko,leaveRaid).queue((message -> {
            this.messageId = message.getIdLong();
            this.channelId = channel.getIdLong();
            this.serverId = channel.getGuild().getIdLong();
            saveOrUpdateRaid();
        }));
    }

    private void saveOrUpdateRaid() {
        LOABot.getQueryHandler().saveOrUpdateRaid(this);
    }

    public boolean isMember(long userId) {
        boolean found = activeMembers.stream().anyMatch(o -> o.getUserId() == (userId));
        if(!found)
            found = benchedMembers.stream().anyMatch(o -> o.getUserId() == (userId));
        return found;
    }

    public void addMember(long userId, boolean asExp, String userClass, String userName) {
        long currentDPS = activeMembers.stream().filter(m->!m.getUserClass().contains("(S)")).count();
        long currentSupp = activeMembers.stream().filter(m->m.getUserClass().contains("(S)")).count();
        boolean isBenched = false;

        if(userClass.contains("(S)")) {
            if(currentSupp < supportCount) {
                activeMembers.add(new RaidMember(id,userId,userName,userClass,asExp, false));
            }else{
                benchedMembers.add(new RaidMember(id,userId,userName,userClass,asExp, true));
                isBenched = true;
            }
        }else{
            if(currentDPS < dpsCount) {
                activeMembers.add(new RaidMember(id,userId,userName,userClass,asExp, false));
            }else{
                benchedMembers.add(new RaidMember(id,userId,userName,userClass,asExp, true));
                isBenched = true;
            }
        }
        LOABot.getQueryHandler().addMemberToRaid(id, userId, userName, userClass, asExp, isBenched);
        sendOrUpdateMessage(null);
    }

    public void removeMember(long userId) {
        activeMembers.removeIf(m->m.getUserId() == userId);
        benchedMembers.removeIf(m->m.getUserId() == userId);
        LOABot.getQueryHandler().deleteMemberFromRaid(id, userId);
        sendOrUpdateMessage(null);
    }

}
