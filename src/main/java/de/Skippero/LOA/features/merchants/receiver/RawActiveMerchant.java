package de.Skippero.LOA.features.merchants.receiver;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.features.merchants
Created by Skippero
on 31.10.2022 , 01:40

*/

public class RawActiveMerchant {

    private String id;
    private String name;
    private String zone;
    private RawItem card;
    private RawItem rapport;
    private double votes;
    private String tradeskill;

    public String getZone() {
        return zone;
    }

    public String getId() {
        return id;
    }

    public double getVotes() {
        return votes;
    }

    public RawItem getCard() {
        return card;
    }

    public RawItem getRapport() {
        return rapport;
    }

    public String getName() {
        return name;
    }

    public void setCard(RawItem card) {
        this.card = card;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRapport(RawItem rapport) {
        this.rapport = rapport;
    }

    public void setVotes(double votes) {
        this.votes = votes;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public void setTradeskill(String tradeskill) {
        this.tradeskill = tradeskill;
    }
}
