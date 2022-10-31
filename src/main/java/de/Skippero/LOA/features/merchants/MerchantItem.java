package de.Skippero.LOA.features.merchants;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.utils
Created by Skippero
on 30.10.2022 , 18:54

*/

public class MerchantItem {

    private final String name;
    private final String description;
    private final MerchantItemType type;
    private final MerchantItemRarity rarity;

    public MerchantItem(String name, MerchantItemType type, MerchantItemRarity rarity, String... description) {
        this.name = name;
        this.type = type;
        this.rarity = rarity;
        if(description.length != 0) {
            this.description = description[0];
        }else{
            this.description = "";
        }
    }

    public String getName() {
        return this.name;
    }

    public MerchantItemType getType() {
        return type;
    }

    public MerchantItemRarity getRarity() {
        return rarity;
    }

    public String getDescription() {
        return description;
    }
}
