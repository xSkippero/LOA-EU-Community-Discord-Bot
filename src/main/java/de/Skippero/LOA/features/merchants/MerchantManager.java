package de.Skippero.LOA.features.merchants;

/*

Projekt: LOA-EUW-Status-Discord-Bot-
Package: de.Skippero.LOA.utils
Created by Skippero
on 30.10.2022 , 18:53

*/

import com.google.gson.Gson;
import com.microsoft.signalr.*;
import de.Skippero.LOA.LOABot;
import de.Skippero.LOA.features.merchants.receiver.RawActiveMerchant;
import de.Skippero.LOA.features.merchants.receiver.RawMerchantUpdate;
import de.Skippero.LOA.utils.MessageColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class MerchantManager {

    public static final Map<String,MerchantItem> requiredItems = new HashMap<>();
    public static final Map<String,MerchantItem> allCardItems = new HashMap<>();

    static {
        //Cards
        requiredItems.put("Wei",new MerchantItem("Wei", MerchantItemType.CARD, MerchantItemRarity.LEGENDARY, "Card for 'Light of Salvation'"));
        requiredItems.put("Balthorr",new MerchantItem("Balthorr", MerchantItemType.CARD, MerchantItemRarity.LEGENDARY, "Card for 'Light of Salvation'"));
        requiredItems.put("Delain_Armen",new MerchantItem("Delain Armen", MerchantItemType.CARD, MerchantItemRarity.LEGENDARY, "Card for 'Lostwind Cliff'"));
        requiredItems.put("Seria",new MerchantItem("Seria", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'Lostwind Cliff'"));
        requiredItems.put("Sian",new MerchantItem("Sian", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for the 'We’ll Meet Again' and 'You Have A Plan' Set"));
        requiredItems.put("Madnick",new MerchantItem("Madnick", MerchantItemType.CARD, MerchantItemRarity.EPIC,"Card for 'We’ll Meet Again'"));
        requiredItems.put("Mokamoka",new MerchantItem("Mokamoka", MerchantItemType.CARD, MerchantItemRarity.EPIC,"Card for 'Forest Of Giants'"));
        requiredItems.put("Bergstrom",new MerchantItem("Bergstrom", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'You Have A Plan'"));
        requiredItems.put("Piyer",new MerchantItem("Piyer", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'You Have A Plan'"));
        requiredItems.put("Krause",new MerchantItem("Krause", MerchantItemType.CARD, MerchantItemRarity.EPIC, "Card for the 'Deep Dive' and 'You Have A Plan' Set"));
        requiredItems.put("Thunder",new MerchantItem("Thunder", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'Deep Dive'"));
        requiredItems.put("Varut",new MerchantItem("Varut", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'Deep Dive'"));
        requiredItems.put("Meehan",new MerchantItem("Meehan", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'Deep Dive'"));
        requiredItems.put("Prideholme_Neria",new MerchantItem("Prideholme Neria", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for 'Deep Dive'"));


        //General Legendary Rapport
        requiredItems.put("Surprise_Chest",new MerchantItem("Surprise Chest", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Sky_Reflection_Oil",new MerchantItem("Sky Reflection Oil", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Chain_War_Chronicles",new MerchantItem("Chain War Chronicles", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Shy_Wind_Flower_Pollen",new MerchantItem("Shy Wind Flower Pollen", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Angler's_Fishing_Pole",new MerchantItem("Angler's Fishing Pole", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Fine_Gramophone",new MerchantItem("Fine Gramophone", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Vern's_Founding_Coin",new MerchantItem("Vern's Founding Coin", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Sirius's_Holy_Book",new MerchantItem("Sirius's Holy Book", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Sylvain_Queen's_Blessing",new MerchantItem("Sylvain Queen's Blessing", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Fargar's_Beer",new MerchantItem("Fargar's Beer", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Red_Moon_Tears",new MerchantItem("Red Moon Tears", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Oreha_Viewing_Stone",new MerchantItem("Oreha Viewing Stone", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Necromancer's_Records",new MerchantItem("Necromancer's Records", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Warm_Earmuffs",new MerchantItem("Warm Earmuffs", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
        requiredItems.put("Lucky_Starflower",new MerchantItem("Lucky Starflower", MerchantItemType.RAPPORT, MerchantItemRarity.LEGENDARY, "A common gift which gives 2000 points"));
    } //Cards for public

    static  {
        allCardItems.put("Cahni",new MerchantItem("Cahni",MerchantItemType.CARD,MerchantItemRarity.COMMON, "Vendor in Elgacia"));

        allCardItems.put("Siera",new MerchantItem("Siera",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Rethramis"));
        allCardItems.put("Giant_Worm",new MerchantItem("Giant Worm",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Yudia"));
        allCardItems.put("Morina",new MerchantItem("Morina",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Yudia"));
        allCardItems.put("Berhart",new MerchantItem("Berhart",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in West Luterra"));
        allCardItems.put("Cadogan",new MerchantItem("Cadogan",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in West Luterra"));
        allCardItems.put("Killian",new MerchantItem("Killian",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in South Vern"));
        allCardItems.put("Satra",new MerchantItem("Satra",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in South Vern"));
        allCardItems.put("Brinewt",new MerchantItem("Brinewt",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in East Luterra"));
        allCardItems.put("Morpheo",new MerchantItem("Morpheo",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in East Luterra"));
        allCardItems.put("Egg_of_Creation",new MerchantItem("Egg of Creation",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Tortoyk"));
        allCardItems.put("Madam_Moonscent",new MerchantItem("Madam Moonscent",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Anikka"));
        allCardItems.put("Sir_Druden",new MerchantItem("Sir Druden",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Anikka"));
        allCardItems.put("Sir_Valleylead",new MerchantItem("Sir Valleylead",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Anikka"));
        allCardItems.put("Javern",new MerchantItem("Javern",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Shushire"));

        allCardItems.put("Revellos",new MerchantItem("Revellos",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Rowen"));
        allCardItems.put("Rowen_Zenlord",new MerchantItem("Rowen Zenlord",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Rowen"));
        allCardItems.put("Euclid",new MerchantItem("Euclid",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Elgacia"));
        allCardItems.put("Great_Celestial_Serpent",new MerchantItem("Great Celestial Serpent",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Elgacia"));
        allCardItems.put("Kirke",new MerchantItem("Kirke",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Elgacia"));
        allCardItems.put("Prunya",new MerchantItem("Prunya",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Elgacia"));
        allCardItems.put("Sky_Whale",new MerchantItem("Sky Whale",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Elgacia"));
        allCardItems.put("Tienis",new MerchantItem("Tienis",MerchantItemType.CARD,MerchantItemRarity.UNCOMMON, "Vendor in Elgacia"));

        allCardItems.put("Cassleford",new MerchantItem("Cassleford",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in West Luterra"));
        allCardItems.put("Thunder",new MerchantItem("Thunder",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Yudia"));
        allCardItems.put("Prideholme_Neria",new MerchantItem("Prideholme Neria",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rethramis"));
        allCardItems.put("Varut",new MerchantItem("Varut",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rethramis"));
        allCardItems.put("Meehan",new MerchantItem("Meehan",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in East Luterra"));
        allCardItems.put("Nox",new MerchantItem("Nox",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in East Luterra"));
        allCardItems.put("Seria",new MerchantItem("Seria",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in East Luterra"));
        allCardItems.put("Eolh",new MerchantItem("Eolh",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Tortoyk"));
        allCardItems.put("Bergstrom",new MerchantItem("Bergstrom",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Arthetine"));
        allCardItems.put("Stern_Neria",new MerchantItem("Stern Neria",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Arthetine"));
        allCardItems.put("Gideon",new MerchantItem("Gideon",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in North Vern"));
        allCardItems.put("Payla",new MerchantItem("Payla",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in North Vern"));
        allCardItems.put("Sian",new MerchantItem("Sian",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Shushire"));
        allCardItems.put("Alifer",new MerchantItem("Alifer",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rohendel"));
        allCardItems.put("Lenora",new MerchantItem("Lenora",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rohendel"));
        allCardItems.put("Great_Castle_Neria",new MerchantItem("Great Castle Neria",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Yorn"));
        allCardItems.put("Piyer",new MerchantItem("Piyer",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Yorn"));
        allCardItems.put("Goulding",new MerchantItem("Goulding",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Feiton"));
        allCardItems.put("Levi",new MerchantItem("Levi",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Feiton"));
        allCardItems.put("Cicerra",new MerchantItem("Cicerra",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Punika"));
        allCardItems.put("Seto",new MerchantItem("Seto",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Punika"));
        allCardItems.put("Stella",new MerchantItem("Stella",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Punika"));
        allCardItems.put("Lujean",new MerchantItem("Lujean",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in South Vern"));
        allCardItems.put("Vern_Zenlord",new MerchantItem("Vern Zenlord",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in South Vern"));
        allCardItems.put("Xereon",new MerchantItem("Xereon",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in South Vern"));

        allCardItems.put("Anke",new MerchantItem("Anke",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Arno",new MerchantItem("Arno",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Baskia",new MerchantItem("Baskia",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Hanun",new MerchantItem("Hanun",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Marinna",new MerchantItem("Marinna",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Piela",new MerchantItem("Piela",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Sylus",new MerchantItem("Sylus",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Wilhelm",new MerchantItem("Wilhelm",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Rowen"));
        allCardItems.put("Azakiel",new MerchantItem("Azakiel",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Elgacia"));
        allCardItems.put("Belomet",new MerchantItem("Belomet",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Elgacia"));
        allCardItems.put("Diogenes",new MerchantItem("Diogenes",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Elgacia"));
        allCardItems.put("Dyna",new MerchantItem("Dyna",MerchantItemType.CARD,MerchantItemRarity.RARE, "Vendor in Elgacia"));

        allCardItems.put("Thunderwings",new MerchantItem("Thunderwings",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in East Luterra"));
        allCardItems.put("Mokamoka",new MerchantItem("Mokamoka",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Tortoyk"));
        allCardItems.put("Krause",new MerchantItem("Krause",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Arthetine"));
        allCardItems.put("Thar",new MerchantItem("Thar",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in North Vern"));
        allCardItems.put("Madnick",new MerchantItem("Madnick",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Shushire"));
        allCardItems.put("Gnosis",new MerchantItem("Gnosis",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Rohendel"));
        allCardItems.put("Kaysarr",new MerchantItem("Kaysarr",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Yorn"));
        allCardItems.put("Kaldor",new MerchantItem("Kaldor",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Feiton"));
        allCardItems.put("Albion",new MerchantItem("Albion",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Punika"));

        allCardItems.put("Danika",new MerchantItem("Danika",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Rowen"));
        allCardItems.put("Myun_Hidaka",new MerchantItem("Myun Hidaka",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Rowen"));
        allCardItems.put("Osphere",new MerchantItem("Osphere",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Rowen"));
        allCardItems.put("Ark_of_Eternity_Kayangel",new MerchantItem("Ark of Eternity Kayangel",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Elgacia"));
        allCardItems.put("Lauriel",new MerchantItem("Lauriel",MerchantItemType.CARD,MerchantItemRarity.EPIC, "Vendor in Elgacia"));

        allCardItems.put("Balthorr",new MerchantItem("Balthorr", MerchantItemType.CARD, MerchantItemRarity.LEGENDARY, "TBA"));
        allCardItems.put("Delain_Armen",new MerchantItem("Delain Armen", MerchantItemType.CARD, MerchantItemRarity.LEGENDARY, "TBA"));
        allCardItems.put("Wei",new MerchantItem("Wei",MerchantItemType.CARD,MerchantItemRarity.LEGENDARY, "Vendor in Anikka"));
        allCardItems.put("Vairgrys",new MerchantItem("Vairgrys",MerchantItemType.CARD,MerchantItemRarity.LEGENDARY, "Vendor in Elgacia"));
    } //Cards for users

    private static HubConnection hubConnection;

    public static void openConnection() {

        String url = "https://lostmerchants.com/MerchantHub";

        if(LOABot.DEVELOP)
            url = "https://test.lostmerchants.com/MerchantHub";

        hubConnection = HubConnectionBuilder.create(url).build();
        hubConnection.setKeepAliveInterval(60 * 1000);
        hubConnection.setServerTimeout(8 * 60 * 1000);
        hubConnection.onClosed((ex) -> {
            System.out.println("[" + new Date().toGMTString() + "] SignalR connection interrupted, restarting HubClient...");
            hubConnection.close();
            openConnection();
        });
        hubConnection.start();

        Timer timer = new Timer("signalR");
        long period = 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                if (hubConnection.getConnectionState().equals(HubConnectionState.CONNECTED)) {
                    System.out.println("[" + new Date().toGMTString() + "] SignalR -> " + hubConnection.getConnectionState() + " ID: " + hubConnection.getConnectionId());
                    hubConnection.invoke("SubscribeToServer", "Ealyn");
                    hubConnection.invoke("SubscribeToServer", "Nia");

                    hubConnection.on("UpdateVotes", (votes) -> {
                        //ignoring votes for now
                    }, Object.class);

                    hubConnection.on("UpdateMerchantGroup", (server, merchants) -> {

                        String result = String.valueOf(merchants).replaceAll("(?<!,)\\s+", "_").replaceAll(":","");

                        if(LOABot.DEVELOP)
                            System.out.println(result);

                        RawMerchantUpdate merchantUpdate = new Gson().fromJson(result, RawMerchantUpdate.class);
                        RawActiveMerchant activeMerchant = merchantUpdate.getActiveMerchants()[0];
                        MerchantItemRarity cardRarity = MerchantItemRarity.getByDouble(activeMerchant.getCard().getRarity());
                        MerchantItemRarity rapportRarity = MerchantItemRarity.getByDouble(activeMerchant.getRapport().getRarity());
                        final MerchantItem card = new MerchantItem(activeMerchant.getCard().getName(), MerchantItemType.CARD, cardRarity);
                        MerchantItem rapport = new MerchantItem(activeMerchant.getRapport().getName(), MerchantItemType.RAPPORT, rapportRarity);
                        boolean goodCard = false;
                        boolean goodRapport = false;
                        Merchant merchant = null;
                        if (requiredItems.containsKey(card.getName()))
                            goodCard = true;
                        if (requiredItems.containsKey(rapport.getName()))
                            goodRapport = true;

                        if (goodCard || goodRapport) {

                            merchant = new Merchant(activeMerchant.getName(), merchantUpdate.getServer(), activeMerchant.getZone(), rapport, card);

                            if (goodCard)
                                merchant.setCardItem(requiredItems.get(card.getName()));
                            if (goodRapport)
                                merchant.setRapportItem(requiredItems.get(rapport.getName()));
                        }

                        int index = IntStream.range(0, allCardItems.size()).filter(i -> new ArrayList<>(allCardItems.values()).get(i).getName().equals(card.getName())).findFirst().orElse(-1);

                        List<MerchantItem> allItemsList = new ArrayList<>(allCardItems.values());

                        switch (merchantUpdate.getServer()) {
                            case "Nia":
                                if(LOABot.neededCardIndexesNia.contains(index)) {
                                    for (String niaUser : LOABot.niaUsers) {
                                        List<Integer> required = LOABot.userCardNotifications.get(niaUser);
                                        if(required.contains(index)) {
                                            sendPrivateNotification(cardRarity,activeMerchant,allItemsList.get(index),niaUser);
                                        }
                                    }
                                }
                                break;
                            case "Ealyn":
                                if(LOABot.neededCardIndexesEayln.contains(index)) {
                                    for (String ealynUser : LOABot.ealynUsers) {
                                        List<Integer> required = LOABot.userCardNotifications.get(ealynUser);
                                        if(required.contains(index)) {
                                            sendPrivateNotification(cardRarity,activeMerchant,allItemsList.get(index),ealynUser);
                                        }
                                    }
                                }
                                break;
                        }

                        if (merchant != null) {
                            for (TextChannel value : LOABot.merchantChannels.values()) {
                                sendMerchantUpdate(merchant, goodCard, goodRapport, value);
                            }
                        }

                    }, Object.class, Object.class);
                    cancel();
                }
            }
        };
        timer.schedule(task, 1000, period);
    }

    public static void sendPrivateNotification(MerchantItemRarity cardRarity, RawActiveMerchant activeMerchant, MerchantItem card, String userId) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setColor(getColorByRarity(cardRarity).getColor());
        builder.setImage("http://Skippero.de/zones/" + activeMerchant.getZone().replaceAll("_","%20") + ".jpg");
        ZonedDateTime time = ZonedDateTime.now();
        long current = time.getMinute();
        long difference = current-30;
        long add = (25-difference) * 60;
        long until = (System.currentTimeMillis()/1000 + add);
        String builder1 = "Merchant: " + "**" + activeMerchant.getName() + "**" + "\n" + "Zone: " + "**" +
                activeMerchant.getZone().replaceAll("_", " ") + "**" + "\n\n" + "**" + card.getName() + "** ⮕ " +
                card.getDescription() + "\n\n" + "Expires: **<t:" + until + ":R>**";
        builder.setDescription(builder1);
        builder.setTitle(":loudspeaker: **Personal Merchant-Notification**");

        Button delButton = Button.danger("del","Delete");

        LOABot.jda.getUserById(userId).openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(builder.build()).setActionRow(delButton)).queue();
    }

    private static MessageColor getColorByRarity(MerchantItemRarity rarity) {
        switch (rarity) {
            case RARE:
                return MessageColor.BLUE;
            case EPIC:
                return MessageColor.MAGENTA;
            case LEGENDARY:
                return MessageColor.ORANGE;
            case UNCOMMON:
                return MessageColor.GREEN;
        }
        return MessageColor.WHITE;
    }

    public static void sendMerchantUpdate(Merchant merchant, boolean goodCard, boolean goodRapport, TextChannel channel) {
        EmbedBuilder builder = new EmbedBuilder();

        MessageColor color = MessageColor.BLUE;

        if(goodCard)
            color = getColorByRarity(merchant.getCardItem().getRarity());
        if(goodRapport) {
            color = getColorByRarity(merchant.getRapportItem().getRarity());
        }

        builder.setColor(color.getColor());

        MerchantItem card = merchant.getCardItem();
        MerchantItem rapport = merchant.getRapportItem();

        boolean deluxeCard = card.getName().equals("Wei");
//|
        if(!deluxeCard) {
            builder.setTitle(":loudspeaker: **"+merchant.getServer()+ "** ⮕ **Valueable Item**");
        }else{
            builder.setTitle(":star: **"+merchant.getServer()+ "** ⮕ **WEI CARD** :star:");
        }

        String cardText = card.getRarity().getDisplayName() + " Card: " + card.getName().replaceAll("_", " ");
        String rapportText = rapport.getRarity().getDisplayName() + " Rapport-Item: " + rapport.getName().replaceAll("_", " ");

        StringBuilder builder1 = new StringBuilder();

        ZonedDateTime time = ZonedDateTime.now();
        long current = time.getMinute();
        long difference = current-30;
        long add = (25-difference) * 60;
        long until = (System.currentTimeMillis()/1000 + add);

        builder1.append("Merchant: ").append("**").append(merchant.getName()).append("**").append("\n").append("Zone: ").append("**").append(merchant.getZone().replaceAll("_", " ")).append("**").append("\n\n")
                .append(goodCard ? "**" + cardText + " **" : cardText).append("\n")
                .append(!card.getDescription().equals("") ? card.getDescription() : "A fine card").append("\n\n")
                .append(goodRapport ? "**" + rapportText + "**" : rapportText).append("\n")
                .append(!rapport.getDescription().equals("") ? rapport.getDescription() : "A nice little gift")
                .append("\n\n").append("Expires: **<t:").append(until).append(":R>**");

        if(deluxeCard) {
            builder1 = new StringBuilder();
            builder1.append("Merchant: ").append("**").append(merchant.getName()).append("**").append("\n").append("Zone: ").append("**").append(merchant.getZone().replaceAll("_", " ")).append("**").append("\n\n")
                    .append(goodCard ? "**" + cardText + " **" : cardText).append("\n")
                    .append(!card.getDescription().equals("") ? card.getDescription() : "A fine card")
                    .append("\n\n\n").append("Expires: **<t:").append(until).append(":R>**");

            builder.setColor(MessageColor.ORANGE.getColor());
        }
        builder.setDescription(builder1);
        builder.setImage("http://Skippero.de/zones/" + merchant.getZone().replaceAll("_","%20") + ".jpg");
        MessageCreateAction msg = channel.sendMessageEmbeds(builder.build());
        msg.queue(message -> {
            Timer timer2 = new Timer(merchant.getName()+merchant.getServer()+UUID.randomUUID());
            TimerTask task2 = new TimerTask() {
                public void run() {
                    channel.deleteMessageById(message.getId()).queue();
                }
            };
            timer2.schedule(task2, (25-difference) * 60 * 1000);
        });
    }
}

