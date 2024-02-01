package de.Skippero.LOA.features.raid;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.features.raid
Created by Skippero
on 01.02.2024 , 03:05

*/

import lombok.Getter;
import lombok.Setter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
public class RaidMeta {

    private String name;
    private String description;
    private String durationText;
    private String startDate;
    private String startDateDiscordTimeStamp;
    private String startDiscordTimeStamp;
    private long autoDeletionTimeStamp;

    public RaidMeta(String name, String description, String durationText, String startDate) {
        this.name = name;
        this.description = description;
        this.durationText = durationText;
        this.startDate = startDate;
        setupDate(startDate);
    }

    private void setupDate(String startDate) {
        String pattern = "dd.MM.yyyy HH:mm";
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        try {
            Date d = dateFormat.parse(startDate);
            long startTimeStamp = d.getTime();
            startDiscordTimeStamp = "<t:" + startTimeStamp/1000 + ":R>";
            startDateDiscordTimeStamp = "<t:" + startTimeStamp/1000 + ":f>";
            autoDeletionTimeStamp = startTimeStamp + (7 * 24 * 60 * 60 * 1000 /*1 Week*/);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
