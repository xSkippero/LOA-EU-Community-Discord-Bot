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
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.sql.SQLException;
import java.util.ArrayList;

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
                Message message = channel.getHistory().getMessageById(messageId);
                if(message == null) {
                    sendMessage(channel);
                    return;
                }
                channel.editMessageEmbedsById(messageId,buildMessage()).queue();
            }
        }
        deleteRaid();
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
                String badge = activeMember.isExp() ? ":loaLick:" : ":loaLetsPlay:";
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
        Button joinRaidMokoko = Button.primary("joinRaidMokoko","Join as Mokoko");
        Button joinRaidExp = Button.secondary("joinRaidExp","Join as experienced");
        Button leaveRaid = Button.danger("leaveRaid","Leave");

        Button delButton = Button.danger("del","Delete");

        channel.sendMessageEmbeds(buildMessage()).setActionRow(joinRaidExp,joinRaidMokoko,leaveRaid, delButton).queue((message -> {
            this.messageId = message.getIdLong();
            this.channelId = channel.getIdLong();
            this.serverId = channel.getGuild().getIdLong();
            //saveOrUpdateRaid();
        }));
    }

    private void saveOrUpdateRaid() {
        LOABot.getQueryHandler().saveOrUpdateRaid(this);
    }
}
