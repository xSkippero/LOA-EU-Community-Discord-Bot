package de.Skippero.LOA.events;

import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.features.raid.Raid;
import de.Skippero.LOA.features.raid.RaidManager;
import de.Skippero.LOA.features.raid.RaidMember;
import de.Skippero.LOA.features.raid.RaidMeta;
import jdk.nashorn.internal.objects.annotations.Getter;
import jdk.nashorn.internal.objects.annotations.Setter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OnSlashCommandInteraction extends ListenerAdapter {

    public static class ApplyProcess {
        public long userId;
        public long raidId;
        public boolean asExp;
    }

    public Map<Long, ApplyProcess> applicants = new HashMap<>();

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

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("raid:apply")) {
            if(event.getMember() == null) {
                return;
            }
            long memberId = event.getMember().getIdLong();
            if(applicants.containsKey(memberId)) {
                ApplyProcess apply = applicants.get(memberId);
                SelectOption selection = event.getInteraction().getSelectedOptions().get(0);
                Raid r = RaidManager.getById(apply.raidId);
                if(r != null) {
                    r.addMember(memberId,apply.asExp,selection.getValue(),event.getMember().getAsMention());
                }
                applicants.remove(memberId);
            }
            event.getMessage().delete().queue();
            event.reply("Successfully applied to the raid").setEphemeral(true).queue();
        }
    }

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

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName().toLowerCase()) {
            case "ping":
                onCommandPing(event);
                break;
            case "about":
                onCommandAbout(event);
                break;
            case "reload":
                onCommandReload(event);
                break;
            case "restart":
                onCommandRestart(event);
                break;
            case "stop":
                onCommandStop(event);
                break;
            case "config":
                onCommandConfig(event);
                break;
            case "permissions":
                onCommandPermissions(event);
                break;
            case "raid":
                onCommandRaid(event);
                break;
            case "movemembers":
                onCommandMoveMembers(event);
                break;
            case "deleteraid":
                onCommandDeleteRaid(event);
                break;
            case "mergerole":
                onCommandMergeRole(event);
        }
        log(event.getUser().getName() + " entered /" + event.getName());
    }

    private void onCommandMergeRole(SlashCommandInteractionEvent event) {
        if(event.getGuild() == null) {
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.merge",event.getGuild().getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping roleAObj = event.getOption("rolea");
        OptionMapping roleBObj = event.getOption("roleb");

        String roleAs = roleAObj != null ? roleAObj.getAsString() : "";
        String roleBs = roleBObj != null ? roleBObj.getAsString() : "";

        if(event.getOptions().size() != 2) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        Guild g = event.getGuild();

        List<Role> rolesA = g.getRolesByName(roleAs,false);
        List<Role> rolesB = g.getRolesByName(roleBs,false);

        if(rolesA.isEmpty()) {
            event.reply("Role A -> '" + roleAs + "' does not exist!").setEphemeral(true).queue();
            return;
        }

        if(rolesB.isEmpty()) {
            event.reply("Role B -> '" + roleBs + "' does not exist!").setEphemeral(true).queue();
            return;
        }

        Role roleA = rolesA.get(0);
        Role roleB = rolesB.get(0);

        List<Member> membersA = g.getMembersWithRoles(roleA);

        log("Merging " + membersA.size() + " members into " + roleB.getName());

        for (Member member : membersA) {
            g.addRoleToMember(member,roleB).queue(done->log("Added " + member.getNickname() + " to role " + roleB.getName()));
        }

        log("Done starting addRoleToMember requests");

        event.reply("All merge requests started").setEphemeral(true).queue();
    }

    private void onCommandPing(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pong!").setEphemeral(true).flatMap(v -> event.getHook().editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)).queue();
    }

    private void onCommandAbout(SlashCommandInteractionEvent event) {
        event.reply("This bot checks the status page of LostARK (EU) at predefined intervals and displays any changes in a Discord channel\n"
                        + "Bot by Skippero, v. "
                        + LOABot.botVersion + "\n"
                        + "https://github.com/xSkippero/LOA-EUW-Status-Discord-Bot-")
                .setEphemeral(true).queue();
    }

    private void onCommandReload(SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() != 397006908424454147L) {
            event.reply("You do not have the required permissions to execute this command").setEphemeral(true).queue();
        } else {
            event.reply("Reloading Configs...").setEphemeral(true).queue();
            LOABot.manualReload();
            log("Manually reloaded all server-configurations");
        }
    }

    private void onCommandRestart(SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() != 397006908424454147L) {
            event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
        } else {
            event.reply("Restarting...").setEphemeral(true).queue();
            log("Manually restarted bot");
            LOABot.restartBot();
        }
    }

    private void onCommandStop(SlashCommandInteractionEvent event) {
        if (event.getUser().getIdLong() != 397006908424454147L) {
            event.reply("You dont have the required permissions to execute this command").setEphemeral(true).queue();
        } else {
            event.reply("Shutting down...").setEphemeral(true).queue();
            log("Manually shutting down bot");
            LOABot.getQueryHandler().closeConnection();
            LOABot.jda.shutdown();
            System.exit(0);
        }
    }

    private void onCommandConfig(SlashCommandInteractionEvent event) {
        if (event.isFromGuild() && event.getGuild() != null) {
            if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.config", event.getGuild().getId())) {
                if (event.getOptions().isEmpty()) {
                    event.reply("You entered the configuration menu\n" + "Usage:\n" + "/config <property> <value>\n\n" + "Properties:\n" + "pushNotifications: <'true','false'>\n" + "pushChannelName: <'value'>\n" + "statusChannelName: <'value'>").setEphemeral(true).queue();
                } else if (event.getOptions().size() == 1) {
                    event.reply("You are missing an argument").setEphemeral(true).queue();
                } else if (event.getOptions().size() == 2) {
                    OptionMapping propertyObj = event.getOption("property");
                    OptionMapping valueObj = event.getOption("value");
                    String property = propertyObj == null ? "" : propertyObj.getAsString();
                    String value = valueObj == null ? "" : valueObj.getAsString();

                    if(property.isEmpty() || value.isEmpty()) {
                        event.reply("Input values were found as empty, please report this if it happens again.").setEphemeral(true).queue(m->m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                        return;
                    }

                    if (!property.equals("pushNotifications") && !property.equals("pushChannelName") && !property.equals("statusChannelName") && !property.equals("merchantChannelName")) {
                        event.reply("Please provide a Property from the List").setEphemeral(true).queue();
                        return;
                    }
                    if (property.equals("pushNotifications")) {
                        if (!value.equals("true") && !value.equals("false")) {
                            event.reply("Please provide true or false").setEphemeral(true).queue();
                            return;
                        }
                    }
                    LOABot.getQueryHandler().updateProperty(event.getGuild().getId(), property, value);
                    event.reply("You updated the setting, the changes will take effect when the config-reload happens (in approximately " + Math.round((LOABot.nextUpdateTimestamp - System.currentTimeMillis()) / 1000D / 60D) + " minutes)").setEphemeral(true).queue();
                    LOABot.updateNotify.put(event.getUser(), event.getGuild().getId());
                    log("updated " + property + " to " + value + " on " + event.getGuild().getName());
                }
            } else {
                event.reply("You do not have the required permissions to execute this command").setEphemeral(true).queue();
            }
        } else {
            event.reply("Please use this command only on a server").setEphemeral(true).queue();
        }
    }

    private void onCommandPermissions(SlashCommandInteractionEvent event) {
        if (event.isFromGuild() && event.getGuild() != null) {
            String guildName = event.getGuild().getId();
            if (event.getMember() != null && event.getMember().isOwner() || LOABot.getQueryHandler().hasPermission(event.getMember().getId(), "loabot.permissions", guildName)) {
                if(event.getOptions().isEmpty() || event.getOptions().size() < 2 || event.getOption("action") == null) {
                    event.reply("Missing arguments").setEphemeral(true).queue();
                    return;
                }
                OptionMapping actionObj = event.getOption("action");
                OptionMapping userObj = event.getOption("user");
                String action = actionObj != null ? actionObj.getAsString() : "";
                Member user = userObj != null ? userObj.getAsMember() : null;

                if(action.isEmpty() || user == null) {
                    event.reply("Input values were found as empty, please report this if it happens again.").setEphemeral(true).queue(m->m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                    return;
                }

                if (event.getOptions().size() == 2) {
                    if(!action.equalsIgnoreCase("list")) {
                        event.reply("Please provide either another action with a permission or List to show the permissions of the given user").setEphemeral(true).queue();
                    } else {
                        List<String> perms = LOABot.getQueryHandler().getPermissionForServer(user.getId(),guildName);
                        if(!perms.isEmpty()) {
                            StringBuilder builder = new StringBuilder();
                            builder.append("Permissions from ").append(user.getAsMention()).append(":\n");
                            perms.forEach(perm -> {
                                builder.append("- ").append(perm).append("\n");
                            });
                            event.reply(builder.toString()).setEphemeral(true).queue();
                        }else{
                            event.reply(user.getAsMention() + " has no permissions").setEphemeral(true).queue();
                        }
                        log("listed the permissions for "+ user.getEffectiveName() + " on " + guildName);
                    }
                }else if(event.getOptions().size() == 3) {

                    if(user.isOwner() && !event.getMember().getId().equals(user.getId())) {
                        event.reply("You cannot change the permissions of the server-owner").setEphemeral(true).queue();
                        return;
                    }

                    if (!action.equalsIgnoreCase("add") && !action.equalsIgnoreCase("remove")) {
                        event.reply("Please provide one of the given actions: add/remove").setEphemeral(true).queue();
                    }else {
                        String userId = user.getId();
                        OptionMapping permissionObj = event.getOption("permission");
                        String permission = permissionObj != null ? permissionObj.getAsString() : "";

                        if(permission.isEmpty()) {
                            event.reply("Input values were found as empty, please report this if it happens again.").setEphemeral(true).queue(m->m.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
                            return;
                        }

                        if(action.equalsIgnoreCase("add")) {
                            LOABot.getQueryHandler().insertUserProperty(guildName,userId,permission);
                            event.reply("Added the permission '" + permission + "' to " + user.getAsMention()).setEphemeral(true).queue();
                            log("added the permission '" + permission + "' to "+ user.getEffectiveName() + " on " + guildName);
                        }else if(action.equalsIgnoreCase("remove")) {
                            LOABot.getQueryHandler().removeUserProperty(guildName,userId,permission);
                            event.reply("Removed the permission '" + permission + "' from " + user.getAsMention()).setEphemeral(true).queue();
                            log("removed the permission '" + permission + "' from "+ user.getEffectiveName() + " on " + guildName);
                        }
                    }
                }
            } else {
                event.reply("You do not have the required permissions to execute this command").setEphemeral(true).queue();
            }
        } else {
            event.reply("Please use this command only on a server").setEphemeral(true).queue();
        }
    }

    private void onCommandRaid(SlashCommandInteractionEvent event) {
        if(event.getGuild() == null) {
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.raid",event.getGuild().getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping nameObj = event.getOption("name");
        OptionMapping descObj = event.getOption("desc");
        OptionMapping dpsCountObj = event.getOption("dpscount");
        OptionMapping suppCountObj = event.getOption("suppcount");
        OptionMapping startDateObj = event.getOption("startdate");
        OptionMapping durationObj = event.getOption("duration");

        String name = nameObj != null ? nameObj.getAsString() : "";
        String desc = descObj != null ? descObj.getAsString() : "";
        int dpsCount = dpsCountObj != null ? dpsCountObj.getAsInt() : 0;
        int suppCount = suppCountObj != null ? suppCountObj.getAsInt() : 0;
        String startDate = startDateObj != null ? startDateObj.getAsString() : "";
        String duration = durationObj != null ? durationObj.getAsString() : "";

        if(event.getOptions().size() != 6) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        RaidMeta meta = new RaidMeta(name,desc,duration,startDate);
        try {
            Raid raid = new Raid(event.getChannel().asTextChannel(), meta, dpsCount, suppCount);
            event.reply("Created Raid with ID #" + raid.getId()).setEphemeral(true).queue();
            RaidManager.raids.add(raid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void onCommandMoveMembers(SlashCommandInteractionEvent event) {
        if(event.getGuild() == null) {
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.movemembers",event.getGuild().getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping raidAObj = event.getOption("raida");
        OptionMapping raidBObj = event.getOption("raidb");
        long raidAId = raidAObj != null ? raidAObj.getAsInt() : 0;
        long raidBId = raidBObj != null ? raidBObj.getAsInt() : 0;

        if(event.getOptions().size() != 2) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        Raid raidA = RaidManager.getById(raidAId);
        Raid raidB = RaidManager.getById(raidBId);

        if(raidA == null || raidB == null) {
            event.reply("Please input raidIds which exist").setEphemeral(true).queue();
            return;
        }

        raidB.getActiveMembers().stream().mapToLong(RaidMember::getId).forEach(raidB::removeMember);
        raidB.getBenchedMembers().stream().mapToLong(RaidMember::getId).forEach(raidB::removeMember);
        raidA.getActiveMembers().forEach(activeMember -> raidB.addMember(activeMember.getUserId(), activeMember.isExp(), activeMember.getUserClass(), activeMember.getUserName()));
        raidA.getBenchedMembers().forEach(activeMember -> raidB.addMember(activeMember.getUserId(), activeMember.isExp(), activeMember.getUserClass(), activeMember.getUserName()));

        event.reply("Copied all members from raid #" + raidAId + " to raid #"+raidBId).setEphemeral(true).queue();
    }

    private void onCommandDeleteRaid(SlashCommandInteractionEvent event) {
        if(event.getGuild() == null) {
            return;
        }

        if(!LOABot.getQueryHandler().hasPermission(event.getUser().getId(), "loabot.deleteraid",event.getGuild().getName())) {
            event.reply("You do not have the required permissions to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping raidIdObj = event.getOption("raidid");
        long raidId = raidIdObj != null ? raidIdObj.getAsInt() : 0;

        if(event.getOptions().size() != 1) {
            event.reply("Please put a value into each parameter").setEphemeral(true).queue();
            return;
        }

        Raid raid = RaidManager.getById(raidId);

        if(raid == null) {
            event.reply("Please input raidIds which exist").setEphemeral(true).queue();
            return;
        }

        raid.deleteRaid();
        raid.deleteMessage();

        event.reply("Successfully deleted the raid").setEphemeral(true).queue();
    }

    private void log(String message) {
        System.out.println("[" + new Date() + "] " + message);
    }

}
