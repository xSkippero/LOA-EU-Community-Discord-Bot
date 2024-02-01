package de.Skippero.LOA.features.raid;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.features.raid
Created by Skippero
on 01.02.2024 , 05:12

*/

import de.Skippero.LOA.LOABot;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RaidManager {

    public static List<Raid> raids = new ArrayList<>();

    public static Raid getById(long raidId) {
        return raids.stream().filter(o -> o.getId() == raidId).findFirst().orElse(null);
    }

    public static void loadRaids() {
        ResultSet set = LOABot.getQueryHandler().executeQuerySync("SELECT * FROM plannedRaids pr INNER JOIN plannedRaidsMeta prm ON prm.raidId = pr.id INNER JOIN raidMembers rm ON rm.raidId = pr.id");
        try{
            while(set.next()) {
                long raidId = set.getLong("raidId");
                Raid raid = getById(raidId);
                if(raid == null) {
                    RaidMeta meta = new RaidMeta(
                            set.getString("name"),
                            set.getString("description"),
                            set.getString("duration"),
                            set.getString("startDate")
                    );
                    raid = new Raid(raidId,meta,set.getInt("dpsCount"),set.getInt("supportCount"));

                    raid.setServerId(set.getLong("serverId"));
                    raid.setChannelId(set.getLong("channelId"));
                    raid.setMessageId(set.getLong("messageId"));

                    raids.add(raid);
                }

                RaidMember member = new RaidMember(
                        raidId,
                        set.getLong("userId"),
                        set.getString("userName"),
                        set.getString("userClass"),
                        set.getInt("isExp") == 1,
                        set.getInt("isBenched") == 1
                );

                raid.addMemberToList(member);
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }

        raids.forEach(Raid::update);
    }


}
