package com.furryfaust.itw.packs;

import net.md_5.bungee.api.ChatColor;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pack {

    public static final ItemStack CLAIM_COST = new ItemStack(Material.EMERALD_BLOCK, 1);
    public static final String CLAIM_COST_STRING = "one emerald block";

    private Document packInfo;

    public Pack(Document packInfo) {
        this.packInfo = packInfo;
    }

    public String getName() {
        return packInfo.getString("Name");
    }

    public boolean exists() {
        return packInfo != null;
    }

    public String getAlpha() {
        ArrayList<Document> members = (ArrayList<Document>) packInfo.get("Members");
        for (Document member : members) {
            if (member.getString("Role").equals("Alpha")) {
                return Bukkit.getServer().getOfflinePlayer(UUID.fromString(member.getString("Name"))).getName();
            }
        }
        return null;
    }

    public ArrayList<String> getElders() {
        ArrayList<String> elders = new ArrayList<>();

        ArrayList<Document> members = (ArrayList<Document>) packInfo.get("Members");
        for (Document doc : members) {
            if (doc.getString("Role").equals("Elder")) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(doc.getString("Name")));
                elders.add(player.getName());
            }
        }
        return elders;
    }


    public UUID getUUID(String playerName) {
        ArrayList<Document> members = (ArrayList<Document>) packInfo.get("Members");
        for (Document doc : members) {
            UUID uuid = UUID.fromString(doc.getString("Name"));
            if (Bukkit.getOfflinePlayer(uuid).getName().equals(playerName)) {
                return uuid;
            }
        }
        return null;
    }

    public List<String> getOnlineMembers() {
        List<String> players = new ArrayList<>();

        ArrayList<Document> members = (ArrayList<Document>) packInfo.get("Members");
        for (Document member : members) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(member.getString("Name")));
            if (!player.isOnline())
                continue;

            players.add(player.getName());
        }

        return players;
    }

    public List<String> getOfflineMembers() {
        List<String> players = new ArrayList<>();

        ArrayList<Document> members = (ArrayList<Document>) packInfo.get("Members");
        for (Document member : members) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(member.getString("Name")));
            if (player.isOnline())
                continue;

            players.add(player.getName());
        }

        return players;
    }

    public int getClaimCount() {
        return ((ArrayList<Document>) packInfo.get("Claims")).size();
    }

    public int getMaxClaims() {
        return getMemberCount() * 3;
    }

    public int getMemberCount() {
        return ((ArrayList<Document>) packInfo.get("Members")).size();
    }

    public void sendAll(String message) {
        message = ChatColor.translateAlternateColorCodes('&', "&7[&9&l" + getName() + "&7] " + message);
        List<String> onlineMembers = getOnlineMembers();
        for (String name : onlineMembers) {
            Bukkit.getPlayer(name).sendMessage(message);
        }
    }

    public void send(CommandSender sender, String message) {
        message = ChatColor.translateAlternateColorCodes('&', "&7[&9&l" + getName() + "&7] " + message);
        sender.sendMessage(message);
    }

    public void info(CommandSender sender) {
        String name = getName(),
                alpha = getAlpha();

        List<String> online = getOnlineMembers(),
                offline = getOfflineMembers();

        ArrayList<String> info = new ArrayList<>();
        info.add("&a|---------[&b&l" + name + "&a]---------|");
        info.add("&aMEMBERS    CLAIMS    MAX CLAIMS");
        info.add("&b" + getMemberCount() + "             " + getClaimCount() + "           " + getMaxClaims());
        StringBuilder onlineBuilder = new StringBuilder();
        for (String s : online) {
            onlineBuilder.append(" ").append(s.equals(alpha) ? "*" + s : s).append(" |");
        }

        info.add("&aOnline Members:&b" + onlineBuilder.toString());

        StringBuilder offlineBuilder = new StringBuilder();
        for (String s : offline) {
            offlineBuilder.append(" ").append(s.equals(alpha) ? "*" + s : s).append(" |");
        }

        info.add("&aOffline Members:&b" + offlineBuilder.toString());

        for (String s : info) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', s));
        }
    }

}
