package com.furryfaust.itw.packs;

import net.md_5.bungee.api.ChatColor;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Pack {

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

    public void info(CommandSender sender) {
        String name = getName(),
                alpha = getAlpha();

        List<String> online = getOnlineMembers(),
                offline = getOfflineMembers();

        ArrayList<String> info = new ArrayList<>();
        info.add("&a|---------[&b" + name + "&a]---------|");
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
