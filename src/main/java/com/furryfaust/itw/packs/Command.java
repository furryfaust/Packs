package com.furryfaust.itw.packs;

import net.md_5.bungee.api.ChatColor;
import org.bson.Document;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {

    Packs pl;

    public Command(Packs pl) {
        this.pl = pl;
    }

    public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command command, String cmd, String[] args) {
        Player player = null;
        if (commandSender instanceof Player) {
            player = (Player) commandSender;
        }

        if (args.length == 0) {
            return true;
        }
        switch (args[0]) {
            case "create":
                if (player == null) {
                    warn(commandSender, "You must be a player to create a pack.");
                    return false;
                }

                if (pl.db.getPack(player.getUniqueId()) != null) {
                    warn(player, "You are already in a pack.");
                    return false;
                }

                if (args.length != 2) {
                    warn(player, "Incorrect argument length.");
                    return false;
                }

                pl.db.createPack(args[1], player.getUniqueId().toString());
                player.sendMessage(toPackMessage(args[1], "&aYou have successfully created a pack."));
                return true;
            case "info":
                if (args.length == 1 && player != null) {
                    Document pack = pl.db.getPack(player.getUniqueId());
                    if (pack == null) {
                        warn(player, "You are not in a pack.");
                        return false;
                    }

                    player.sendMessage(toPackMessage(pack.getString("Name"), ""));
                }
                return false;
        }

        return false;
    }

    public void warn(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&c" + message));
    }

    public String toPackMessage(String packName, String message) {
        return ChatColor.translateAlternateColorCodes('&', "&7[&9&l" + packName + "&7] " + message);
    }

}
