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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.apache.maven.model.Model;

import java.util.*;

public class MerchantManager {

    public static final Map<String,MerchantItem> requiredItems = new HashMap<>();

    static {
        //Cards
        requiredItems.put("Wei",new MerchantItem("Wei", MerchantItemType.CARD, MerchantItemRarity.LEGENDARY, "Card for the 'Light of Salvation' set (+15% DMG in total)"));
        requiredItems.put("Seria",new MerchantItem("Seria", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for the 'Lostwind Cliff' set (+7% Crit)"));
        requiredItems.put("Sian",new MerchantItem("Sian", MerchantItemType.CARD, MerchantItemRarity.RARE, "Card for the 'We’ll Meet Again' set (Reduced DMG and Heal on Low HP)"));
        requiredItems.put("Madnick",new MerchantItem("Madnick", MerchantItemType.CARD, MerchantItemRarity.EPIC,"Card for the 'We’ll Meet Again' set (Reduced DMG and Heal on Low HP)"));
        requiredItems.put("Mokamoka",new MerchantItem("Mokamoka", MerchantItemType.CARD, MerchantItemRarity.EPIC,"Card for the 'Forest Of Giants' set (+15% Health Recovery, better Armor)"));

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
    }

    private static HubConnection hubConnection;

    public static void openConnection() {
        hubConnection = HubConnectionBuilder.create("https://lostmerchants.com/MerchantHub").build();
        hubConnection.setKeepAliveInterval(60 * 1000);
        hubConnection.setServerTimeout(8 * 60 * 1000);
        hubConnection.onClosed((ex) -> {
            System.out.println("onClosed");
            if(ex != null) {
                sendErrorReportToDev(ex);
            }else{
                sendErrorReportToDev(new Exception("signalR Connection was closed, unable to get reason"));
            }
            LOABot.restartBot();
        });
        hubConnection.on("onFailure",(ex) -> {
            System.out.println("onFailure");
            sendErrorReportToDev(new Exception("signalR Connection was closed, unable to get reason"));
            LOABot.restartBot();
        }, Object.class);
        hubConnection.on("Failure",(ex) -> {
            System.out.println("Failure");
            sendErrorReportToDev(new Exception("signalR Connection was closed, unable to get reason"));
            LOABot.restartBot();
        }, Object.class);
        hubConnection.on("failure",(ex) -> {
            System.out.println("failure");
            sendErrorReportToDev(new Exception("signalR Connection was closed, unable to get reason"));
            LOABot.restartBot();
        }, Object.class);
        hubConnection.start();

        Timer timer = new Timer("signalR");
        long period = 1000L;
        TimerTask task = new TimerTask() {
            public void run() {
                if (hubConnection.getConnectionState().equals(HubConnectionState.CONNECTED)) {
                    System.out.println("SignalR -> " + hubConnection.getConnectionState());
                    hubConnection.invoke("SubscribeToServer", "Ealyn");
                    hubConnection.invoke("SubscribeToServer", "Nia");

                    hubConnection.on("UpdateVotes", (votes) -> {
                        //ignoring votes for now
                    }, Object.class);

                    hubConnection.on("UpdateMerchantGroup", (server, merchants) -> {

                        String result = String.valueOf(merchants).replaceAll("(?<!,)\\s+", "_");
                        RawMerchantUpdate merchantUpdate = new Gson().fromJson(result, RawMerchantUpdate.class);
                        RawActiveMerchant activeMerchant = merchantUpdate.getActiveMerchants()[0];
                        MerchantItemRarity cardRarity = MerchantItemRarity.getByDouble(activeMerchant.getCard().getRarity());
                        MerchantItemRarity rapportRarity = MerchantItemRarity.getByDouble(activeMerchant.getRapport().getRarity());
                        MerchantItem card = new MerchantItem(activeMerchant.getCard().getName(), MerchantItemType.CARD, cardRarity);
                        MerchantItem rapport = new MerchantItem(activeMerchant.getRapport().getName(), MerchantItemType.RAPPORT, rapportRarity);
                        boolean goodCard = false;
                        boolean goodRapport = false;
                        Merchant merchant = null;
                        if (requiredItems.containsKey(card.getName()))
                            goodCard = true;
                        if (requiredItems.containsKey(rapport.getName()))
                            goodRapport = true;

                        if (goodCard || goodRapport) {
                            if (goodCard)
                                card = requiredItems.get(card.getName());
                            if (goodRapport)
                                rapport = requiredItems.get(rapport.getName());

                            merchant = new Merchant(activeMerchant.getName(), merchantUpdate.getServer(), activeMerchant.getZone(), rapport, card);
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

    private static void sendErrorReportToDev(Exception e) {
        User dev = LOABot.jda.getUserById(397006908424454147L);
        if (dev != null) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setColor(MessageColor.RED.getColor());
            builder.setTitle("LOA-EUW-Status - signalR-crash-report");
            Model info = LOABot.buildInformation;
            String infoString = info.getGroupId() + "." + info.getArtifactId() + " v. " + info.getVersion();
            builder.setAuthor(infoString);
            Date date = new Date();
            String debugStack = "";
            Gson gson = new Gson();
            debugStack += "updateNotifys=" + gson.toJson(LOABot.updateNotify) + ";";
            debugStack += "jda(responseTotal&gatewayPing)=" + LOABot.jda.getResponseTotal() + "," + LOABot.jda.getGatewayPing() + ";";
            String signalRStack = "";
            signalRStack += "connectionId=" + hubConnection.getConnectionId() + ";";
            signalRStack += "connectionState=" + hubConnection.getConnectionState().name() + ";";
            builder.setDescription("Time: " + date.toGMTString() + "\n"
                    + "Timestamp:" + date.getTime() + "\n"
                    + "SignalR: " + signalRStack + "\n"
                    + "Debug: " + debugStack + "\n"
                    + "Error: " + (e != null ? Arrays.toString(e.getStackTrace()) : "undefined")
            );
            dev.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(builder.build())).queue();
        }
    }

    private static MessageColor getColorByRarity(MerchantItemRarity rarity) {
        switch (rarity) {
            case RARE:
                return MessageColor.BLUE;
            case EPIC:
                return MessageColor.MAGENTA;
            case LEGENDARY:
                return MessageColor.ORANGE;
        }
        return MessageColor.BLUE;
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

        if(!deluxeCard) {
            builder.setTitle(":loudspeaker: **"+merchant.getServer()+ "** ⮕ **Valueable Item**");
        }else{
            builder.setTitle(":star: **"+merchant.getServer()+ "** ⮕ **WEI CARD** :star:");
        }

        String cardText = card.getRarity().getDisplayName() + " Card: " + card.getName().replaceAll("_", " ");
        String rapportText = rapport.getRarity().getDisplayName() + " Rapport-Item: " + rapport.getName().replaceAll("_", " ");

        StringBuilder builder1 = new StringBuilder();

        builder1.append("Merchant: ").append("**").append(merchant.getName()).append("**").append("\n").append("Zone: ").append("**").append(merchant.getZone().replaceAll("_", " ")).append("**").append("\n\n")
                .append(goodCard ? "**" + cardText + " **" : cardText).append("\n")
                .append(!card.getDescription().equals("") ? card.getDescription() : "A fine card").append("\n\n")
                .append(goodRapport ? "**" + rapportText + "**" : rapportText).append("\n")
                .append(!rapport.getDescription().equals("") ? rapport.getDescription() : "A nice little gift");

        if(deluxeCard) {
            builder1 = new StringBuilder();
            builder1.append("Merchant: ").append("**").append(merchant.getName()).append("**").append("\n").append("Zone: ").append("**").append(merchant.getZone().replaceAll("_", " ")).append("**").append("\n\n")
                    .append(goodCard ? "**" + cardText + " **" : cardText).append("\n")
                    .append(!card.getDescription().equals("") ? card.getDescription() : "A fine card").append("\n")
                            .append("@everyone");

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
            timer2.schedule(task2, 25 * 60 * 1000);
        });
    }
}

