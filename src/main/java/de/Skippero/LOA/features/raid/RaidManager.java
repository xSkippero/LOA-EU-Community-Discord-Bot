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
        ResultSet set = LOABot.getQueryHandler().executeQuerySync("SELECT * FROM plannedRaids pr INNER JOIN plannedRaidsMeta prm ON prm.raidId = pr.raidId INNER JOIN raidMembers rm ON rm.raidId = pr.id");
        try{
            while(set.next()) {

            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
