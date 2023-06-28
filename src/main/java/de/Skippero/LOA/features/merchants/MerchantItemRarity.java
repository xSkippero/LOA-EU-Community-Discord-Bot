package de.Skippero.LOA.features.merchants;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.utils
Created by Skippero
on 30.10.2022 , 18:57

*/

public enum MerchantItemRarity {

    COMMON("Common"),
    UNCOMMON("Uncommon"),
    RARE("Rare"),
    EPIC("Epic"),
    LEGENDARY("Legendary");

    private final String displayName;

    MerchantItemRarity(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public static MerchantItemRarity getByDouble(double rarity) {
        switch ((int)rarity) {
            case 2:
                return RARE;
            case 3:
                return EPIC;
            case 4:
                return LEGENDARY;
            case 1:
                return UNCOMMON;
            default:
                return COMMON;
        }
    }

}
