package de.Skippero.LOA.features.merchants;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.utils
Created by Skippero
on 30.10.2022 , 18:53

*/

public class Merchant {

    private final String server;
    private final String name;
    private final String zone;
    private MerchantItem rapportItem;
    private MerchantItem cardItem;

    public Merchant(String name, String server, String zone, MerchantItem rapportItem, MerchantItem cardItem) {
        this.name = name;
        this.server = server;
        this.zone = zone;
        this.rapportItem = rapportItem;
        this.cardItem = cardItem;
    }

    public String getName() {
        return this.name;
    }

    public MerchantItem getRapportItem() {
        return rapportItem;
    }

    public MerchantItem getCardItem() {
        return cardItem;
    }

    public String getServer() {
        return server;
    }

    public String getZone() {
        return zone;
    }

    public void setCardItem(MerchantItem item) {
        this.cardItem = item;
    }

    public void setRapportItem(MerchantItem item) {
        this.rapportItem = item;
    }
}


