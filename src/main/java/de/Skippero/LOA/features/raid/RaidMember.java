package de.Skippero.LOA.features.raid;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.features.raid
Created by Skippero
on 01.02.2024 , 02:50

*/
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RaidMember {

    private long id;
    private long raidId;
    private long userId;
    private boolean isBenched;
    private boolean isExp;
    private String userClass;
    private String userName;

    public RaidMember(long raidId, long userId, String userName, String userClass, boolean isExp, boolean isBenched) {
        this.raidId = raidId;
        this.userId = userId;
        this.userName = userName;
        this.userClass = userClass;
        this.isExp = isExp;
        this.isBenched = isBenched;
    }

}
