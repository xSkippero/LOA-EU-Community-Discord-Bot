package de.Skippero.LOA.features.merchants;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.utils
Created by Skippero
on 30.10.2022 , 18:55

*/

public enum MerchantItemType {

    CARD("Card"),
    RAPPORT("Rapport");

    private final String displayName;

    MerchantItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

}
