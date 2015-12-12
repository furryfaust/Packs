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
                    message(commandSender, "&cYou must be a player to create a pack.");
                    return false;
                }

                if (pl.db.getPack(player.getUniqueId()) != null) {
                    message(player, "&cYou are already in a pack.");
                    return false;
                }

                if (args.length != 2) {
                    message(player, "&cIncorrect arguments length.");
                    return false;
                }

                pl.db.createPack(args[1], player.getUniqueId().toString());
                message(player, "&aYou have successfully created the pack " + args[1]);
                return true;
            case "info":
                if (args.length == 1 && player != null) {
                    Document pack = pl.db.getPack(player.getUniqueId());
                    if (pack == null) {
                        message(player, "&cYou are not in a pack.");
                        return false;
                    }

                    message(player, "");
                    return true;
                }
                return false;
            case "disband":
                if (player == null) {
                    message(commandSender, "&cYou must be a player to disband a pack.");
                    return false;
                }

                if (pl.db.getPack(player.getUniqueId()) == null) {
                    message(player, "&cYou are not in a pack.");
                    return false;
                }

                if (!pl.db.disbandPack(player.getUniqueId())) {
                    message(player, "&cYou are not the alpha of the pack.");
                    return false;
                }

                message(player, "&aYou have disbanded your pack.");
                return true;
            case "invite":
                if (player == null) {
                    message(commandSender, "&cYou must be a player to invite a player to a pack.");
                    return false;
                }

                if (args.length != 2) {
                    message(player, "&cIncorrect arguments length.");
                    return false;
                }

                Document pack = pl.db.getPack(player.getUniqueId());
                if (pack == null) {
                    message(player, "&cYou are not in a pack.");
                    return false;
                }

                if (player.getName() == args[1]) {
                    message(player, "&cYou cannot invite yourself to a pack.");
                    return false;
                }

                Player invited = pl.getServer().getPlayerExact(args[1]);
                if (invited == null) {
                    message(player, "&cYou cannot invite an offline player.");
                    return false;
                }

                if (pl.db.getPack(invited.getUniqueId()) != null) {
                    message(player, "&cYou cannot invite someone already in a pack.");
                    return false;
                }

                if (!pl.db.inviteToPack(invited.getUniqueId(), pack.getString("Name"))) {
                    message(player, "&cYou have already invited this player.");
                    return false;
                }

                message(player, "&aYou have invited " + args[1] + " to your pack.");
                message(invited, "&aYou have been invited to " + pack.getString("Name"));
                return true;
            case "join":

        }

        return false;
    }

    private void message(CommandSender sender, String message) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&9&lPacks&7] " + message));
    }

}
