package de.Skippero.LOA.features.merchants.receiver;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.features.merchants
Created by Skippero
on 31.10.2022 , 01:38

*/

public class RawMerchantUpdate {

    private double id;
    private String server;
    private String merchantName;
    private RawActiveMerchant[] activeMerchants;

    public double getId() {
        return id;
    }

    public String getServer() {
        return server;
    }

    public RawActiveMerchant[] getActiveMerchants() {
        return activeMerchants;
    }

    public String getMerchantName() {
        return merchantName;
    }
}
